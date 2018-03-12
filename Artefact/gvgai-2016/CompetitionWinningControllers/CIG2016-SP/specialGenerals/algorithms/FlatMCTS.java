package specialGenerals.algorithms;

import core.game.StateObservation;
import ontology.Types;
import specialGenerals.Config;
import specialGenerals.algorithms.helpers.KnowledgeBase;
import specialGenerals.algorithms.helpers.Node;
import specialGenerals.algorithms.helpers.Rollout;
import specialGenerals.heuristics.IHeuristic;
import specialGenerals.policies.IPolicy;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 15.04.2016.
 */
public class FlatMCTS implements IAlgorithm {


    protected final IHeuristic heuristic;
    protected final IPolicy policy;
    protected final Rollout rollout;
    protected final KnowledgeBase kb;
    protected int searchDepth;
    protected final Boolean searchMax;

    protected Node root;

    public FlatMCTS(IHeuristic heuristic, IPolicy policy, KnowledgeBase kb, Boolean searchMax) {
        this.heuristic = heuristic;
        this.policy = policy;
        this.rollout = new Rollout(heuristic, policy, kb);
        this.kb = kb;
        this.searchDepth = 100;
        this.searchMax = searchMax;
    }

    public FlatMCTS(IHeuristic heuristic, IPolicy policy, KnowledgeBase kb) {
        this(heuristic, policy, kb, true);
    }

    @Override
    public void init(StateObservation so, ElapsedCpuTimer time) {
        root = Node.getRoot(so, kb);
    }

    @Override
    public Types.ACTIONS nextAction(StateObservation so, ElapsedCpuTimer time) {
        List<Types.ACTIONS> possibleActions = so.getAvailableActions();
        root = Node.getRoot(so, kb);
        Types.ACTIONS bestAction = possibleActions.get(0);
        List<Node> nextStates = new ArrayList<>();
        List<Double> points = new ArrayList<>();
        List<Integer> runs = new ArrayList<>();
        for (Types.ACTIONS action : possibleActions) {
            Node child = root.descendToChild(action);
            nextStates.add(child);
            if (searchMax) {
                points.add(Double.MIN_VALUE);
            } else {
                points.add(heuristic.getValue(child.getState(), time));
            }
            runs.add(1);
        }
        int depth = 1;
        while (time.remainingTimeMillis() > Config.SAFE_TIME) {
            for (int i = 0; i < nextStates.size(); i++) {
                Node nextState = nextStates.get(i);
                double gameValue = rollout.rollout(nextState, time, depth, false) - depth;
                double frac = 1.0 / (runs.get(i) + 1);
                double newValue;
                if (searchMax) {
                    newValue = Math.max(points.get(i), gameValue);
                } else {
                    newValue = points.get(i) * (1 - frac) + frac * gameValue;
                }

                if (isMax(points, newValue)) {
                    bestAction = possibleActions.get(i);
                }
                runs.set(i, runs.get(i) + 1);
                points.set(i, newValue);
            }
            depth++;
        }
        return bestAction;
    }

    /**
     * @param points
     * @param newValue
     * @return
     */
    private boolean isMax(List<Double> points, double newValue) {
        boolean isMax = true;
        for (double val : points) {
            if (newValue < val) {
                isMax = false;
            }
        }
        return isMax;
    }
}
