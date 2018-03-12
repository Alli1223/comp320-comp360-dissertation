package specialGenerals.algorithms;

import java.util.Random;

import core.game.StateObservation;
import ontology.Types;
import specialGenerals.Agent;
import specialGenerals.Config;
import specialGenerals.algorithms.helpers.KnowledgeBase;
import specialGenerals.algorithms.helpers.Node;
import specialGenerals.algorithms.helpers.Rollout;
import specialGenerals.heatmaps.VisitedHeatMap;
import specialGenerals.heuristics.Hashing;
import specialGenerals.heuristics.HeatMapHeuristic;
import specialGenerals.heuristics.IHeuristic;
import specialGenerals.policies.HeatMapPolicy;
import specialGenerals.policies.IMCTSPolicy;
import specialGenerals.policies.IPolicy;
import tools.ElapsedCpuTimer;

/**
 * Created by marco on 16.04.2016.
 */
public class MCTS implements IAlgorithm {

    protected final IHeuristic heuristic;
    protected final IPolicy policy;
    protected final IMCTSPolicy mctsPolicy;
    protected final Rollout rollout;
    protected final KnowledgeBase kb;
    protected final int maxDepth;
    protected VisitedHeatMap heatMap;
    protected Random r;

    protected static final Hashing HASHER = new Hashing();
    protected Node root;

    public MCTS(IHeuristic heuristic, IPolicy policy, IMCTSPolicy mctsPolicy, KnowledgeBase kb) {
        this(heuristic, policy, mctsPolicy, kb, Integer.MAX_VALUE);
    }

    public MCTS(IHeuristic heuristic, IPolicy policy, IMCTSPolicy mctsPolicy, KnowledgeBase kb, int maxDepth) {
        this.heuristic = heuristic;
        this.policy = policy;
        this.mctsPolicy = mctsPolicy;
        this.rollout = new Rollout(heuristic, policy, kb);
        this.maxDepth = maxDepth;
        this.kb = kb;
        this.r = new Random();
    }

    @Override
    public void init(StateObservation so, ElapsedCpuTimer time) {
        // Hier schonmal Baum vorberechnen
        heatMap = new VisitedHeatMap(so);
        heatMap.updateHeatMap(so);
        initRoot(so, time);
        extendTree(time);
    }

    @Override
    public Types.ACTIONS nextAction(StateObservation so, ElapsedCpuTimer time) {
        heatMap.cooldown();
        heatMap.updateHeatMap(so);
        // Prüfen, ob vorberechneter Baum mit aktuellem State zusammenpasst
        if (!equal(so, root.getState())) {
            // Wenn nicht, alles neu berechnen
            initRoot(so, time);
            kb.setDeterministic(false);
            if (Config.LOG_TREE_NEEDS_REBUILD) {
                Config.log("Baum muss neu berechnet werden, da erwarteter state != tatsaechlicher state");
            }
        }
        // Baum weiter berechnen
        extendTree(time);
        // Besten Zug ermitteln
        Types.ACTIONS bestAction;
        if(Config.MCTS_BEST_ACTION_FROM_UCT) {
            bestAction = mctsPolicy.getAction(root, time);
        }else if(Config.MCTS_BEST_ACTION_MOST_VISITED){
            bestAction = root.getMostVisitedAction();
        }else{
            bestAction = root.getBestAction();
        }
        if(Config.DRAW && Config.HEATMAP){
            Agent.setHeatMap(heatMap);
        }
        // Baum für den besten Zug behalten
        root = root.getChild(bestAction);
        root.makeRoot();
        return bestAction;
    }

    /**
     * @param state1
     * @param state2
     * @return
     */
    private boolean equal(StateObservation state1, StateObservation state2) {
        return HASHER.positionHashing(state1) == HASHER.positionHashing(state2);
    }

    private void extendTree(ElapsedCpuTimer time) {
        while (time.remainingTimeMillis() > Config.SAFE_TIME) {
            long presumableFreeMemory = Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            if (presumableFreeMemory < Config.MEMORY_THRESHOLD) {
                if(Config.LOW_MEMORY_SOLUTION_REMOVE_BRANCH){
                    root.removeBadBranch();
                } else {
                    // LOW_MEMORY_SOLUTION: Stop search
                    Config.log("MCTS Memory fail, retrying next act");
                    return;
                }
                
            }
            Node current = root;
            // Bis zu einem Blattknoten absteigen
            while (!current.isLeaf() && !current.getState().isGameOver()) {
                current = current.descendToChild(mctsPolicy.getAction(current, time));
            }
            if (!current.getState().isGameOver()) {
                current = current.descendToChild(mctsPolicy.getAction(current, time));
            }
            current.getVisitedHeatMap().updateHeatMap(current.getState());
            // Durchspielen
            HeatMapPolicy.heatmap = current.getVisitedHeatMap();
            HeatMapHeuristic.heatMap = current.getVisitedHeatMap();
            boolean ignorePruning = r.nextDouble() < Config.IGNORE_PRUNING;
            double result = rollout.rollout(current, time, maxDepth, ignorePruning);
            // Ergebnis speichern
            current.setScore(result);
            current.setVisited(1);
        }
    }

    private void initRoot(StateObservation so, ElapsedCpuTimer time) {
        root = Node.getRoot(so, kb);
        root.setVisitedHeatMap(heatMap);
        for (Types.ACTIONS action : so.getAvailableActions()) {
            StateObservation nextAction = kb.measuredAdvance(so, action, true);
            double score = heuristic.getValue(nextAction, time);
            if (!kb.isDeterministic()) {
                for (int i = 0; i < 4; i++) {
                    nextAction = kb.measuredAdvance(so, action, true);
                    score += heuristic.getValue(nextAction, time);
                }
                score /= 5;
            }
            Node child = root.descendToChild(action);
            child.setScore(score);
            child.setVisited(1);
            child.getVisitedHeatMap().updateHeatMap(child.getState());
        }
    }

}
