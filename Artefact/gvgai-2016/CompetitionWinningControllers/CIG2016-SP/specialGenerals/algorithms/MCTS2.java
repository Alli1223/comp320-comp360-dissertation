package specialGenerals.algorithms;

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
import tools.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by marco on 12.06.2016.
 */
public class MCTS2 implements IAlgorithm{

    protected Node root;
    protected final IHeuristic heuristic;
    protected final IPolicy policy;
    protected final IMCTSPolicy mctsPolicy;
    protected final Rollout rollout;
    protected final int maxDepth;
    protected final KnowledgeBase kb;
    protected final Random r;
    protected VisitedHeatMap heatMap;
    protected Vector2d lastOrientation;
    protected Vector2d preLastOrientation;
    protected static final Hashing HASHER = new Hashing();

    public MCTS2(IHeuristic heuristic, IPolicy policy, IMCTSPolicy mctsPolicy, KnowledgeBase kb, int maxDepth) {
        this.heuristic = heuristic;
        this.policy = policy;
        this.mctsPolicy = mctsPolicy;
        this.rollout = new Rollout(heuristic, policy, kb);
        this.maxDepth = maxDepth;
        this.kb = kb;
        this.r = new Random();
        lastOrientation = Types.NONE;
        preLastOrientation = Types.NONE;
    }


    @Override
    public void init(StateObservation so, ElapsedCpuTimer time) {
        heatMap = new VisitedHeatMap(so);
        initRoot(so, time);
        buildTree(time);
    }

    @Override
    public Types.ACTIONS nextAction(StateObservation so, ElapsedCpuTimer time) {
        heatMap.cooldown();
        heatMap.updateHeatMap(so);
        if (HASHER.positionHashing(so) != HASHER.positionHashing(root.getState())) {
            initRoot(so, time);
            kb.setDeterministic(false);
            if (Config.LOG_TREE_NEEDS_REBUILD) {
                Config.log("Baum muss neu berechnet werden, da erwarteter state != tatsaechlicher state");
            }
        }
        buildTree(time);
        if (Config.DRAW && Config.HEATMAP) {
            Agent.setHeatMap(heatMap);
        }
        Types.ACTIONS bestAction;
        if (Config.MCTS_BEST_ACTION_MOST_VISITED) {
            bestAction = root.getMostVisitedAction();
        } else {
            if (Config.MCTS_BEST_ACTION_FROM_UCT) {
                bestAction = mctsPolicy.getAction(root, time);
            } else {
                bestAction = root.getBestAction();
            }
        }
        if(bestAction != Types.ACTIONS.ACTION_USE) {
            preLastOrientation = lastOrientation;
            lastOrientation = root.getState().getAvatarOrientation();
        }
        Config.log(bestAction.toString());
        root = root.getChild(bestAction);
        root.makeRoot();
        return bestAction;
    }

    private void initRoot(StateObservation so, ElapsedCpuTimer time){
        root = Node.getRoot(so, kb);
        root.setVisitedHeatMap(heatMap);
        for(Types.ACTIONS action: so.getAvailableActions()){
            root.getChild2(action);
        }
    }

    private void pruneDoubleTurn(){
        if(kb.isOrientationBased()){
            if(orientationToAction(lastOrientation) != orientationToAction(root.getState().getAvatarOrientation())){
                for(Types.ACTIONS action: root.getState().getAvailableActions()){
                    if(isMovementAction(action) && action != orientationToAction(root.getState().getAvatarOrientation())){
                        root.getChild2(action).setIgnoreNode(true);
                    }
                }
            }
        }
    }

    private void buildTree(ElapsedCpuTimer time) {
        pruneDoubleTurn();
        while (time.remainingTimeMillis() > Config.SAFE_TIME) {
            long presumableFreeMemory = Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            if (presumableFreeMemory < Config.MEMORY_THRESHOLD) {
                Config.log("MCTS Memory fail, retrying next act");
                Runtime.getRuntime().gc();
                return;
            }
            Node expandedNode = selectAndExpand(time);
            simulate(expandedNode, time);
            expandedNode.backpropagateScore();
        }
    }

    private Node selectAndExpand(ElapsedCpuTimer time){
        Node current = root;
        while(current.getVisited() > 0 && !current.getState().isGameOver()){
            current = current.getChild(mctsPolicy.getAction(current, time));
        }
        current.enterNode();
        return current;
    }

    private void simulate(Node node, ElapsedCpuTimer time){
        HeatMapPolicy.heatmap = node.getVisitedHeatMap();
        HeatMapHeuristic.heatMap = node.getVisitedHeatMap();
        double simulationResult = rollout.rollout(node, time, maxDepth, false);
        node.setScore(simulationResult);
    }

    private boolean isMovementAction(Types.ACTIONS action){
        return action == Types.ACTIONS.ACTION_DOWN || action == Types.ACTIONS.ACTION_UP || action == Types.ACTIONS.ACTION_LEFT || action == Types.ACTIONS.ACTION_RIGHT;
    }

    private Types.ACTIONS orientationToAction(Vector2d v){
        if(v.equals(Types.DOWN)){
            return Types.ACTIONS.ACTION_DOWN;
        }else if(v.equals(Types.LEFT)){
            return Types.ACTIONS.ACTION_LEFT;
        }else if(v.equals(Types.RIGHT)){
            return Types.ACTIONS.ACTION_RIGHT;
        }else if(v.equals(Types.UP)){
            return Types.ACTIONS.ACTION_UP;
        }else{
            return Types.ACTIONS.ACTION_NIL;
        }
    }

}
