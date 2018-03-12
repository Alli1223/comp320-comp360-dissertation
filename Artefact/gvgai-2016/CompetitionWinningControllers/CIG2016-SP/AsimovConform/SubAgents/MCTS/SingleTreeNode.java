package AsimovConform.SubAgents.MCTS;

import AsimovConform.ActionHeuristics.ActionHeuristic;
import AsimovConform.ActionPruner.ActionPruner;
import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Heuristics.Heuristic;
import AsimovConform.SubAgents.MCTS.RolloutPolicies.Rollout;
import tools.ElapsedCpuTimer;

import java.util.Random;

public class SingleTreeNode {

    public static int PRUNED_ROLLOUT_CHANCE = 50;
    public static int HEUR_ROLLOUT_CHANCE = 40;
    public static int time_threshold = 3;

    public static double epsilon = 1e-6;

    public static Random m_rnd;
    protected static double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};
    public AsimovState state;
    public SingleTreeNode parent;
    public SingleTreeNode[] children;
    private double totValue;
    private int nVisits;
    private int m_depth;
    private Rollout rollout;

    private Heuristic heuristic;
    private ActionHeuristic actionHeuristic;
    private ActionPruner actionPruner;
    private ActionHeuristic combinedRollActionHeuristic;

    public SingleTreeNode(AsimovState state, Heuristic h, ActionHeuristic ah, ActionPruner ap, ActionHeuristic crah) {
        this(state, null);

        heuristic = h;
        actionHeuristic = ah;
        actionPruner = ap;
        combinedRollActionHeuristic = crah;
    }

    public SingleTreeNode(AsimovState state, SingleTreeNode parent) {
        this.state = state;
        this.parent = parent;
        this.rollout = new Rollout();

        if (parent != null) {
            heuristic = parent.heuristic;
            actionHeuristic = parent.actionHeuristic;
            actionPruner = parent.actionPruner;
            combinedRollActionHeuristic = parent.combinedRollActionHeuristic;
        }

        children = new SingleTreeNode[Agent.actions.length];
        totValue = 0.0;
        if (parent != null)
            m_depth = parent.m_depth + 1;
        else
            m_depth = 0;
    }


    public void mctsSearch(ElapsedCpuTimer elapsedTimer) {
        if (Agent.KB.orientationBased) {
            PRUNED_ROLLOUT_CHANCE = 80;
            HEUR_ROLLOUT_CHANCE = 15;
        }
        while (elapsedTimer.remainingTimeMillis() > time_threshold) {
            SingleTreeNode selected = treePolicy();

            double delta;
            /* legacy: different rollout types with different probabilities
            int decider = m_rnd.nextInt(100);
            if (decider < PRUNED_ROLLOUT_CHANCE) { // 50% of the time, we do long pruned Rollouts
                delta = rollout.prunedRoll(selected, elapsedTimer, heuristic, actionPruner);
            } else if (decider < PRUNED_ROLLOUT_CHANCE + HEUR_ROLLOUT_CHANCE) { // 40% of the time, we do medium sized heuristic Rollouts
                delta = rollout.heuristicRoll(selected, elapsedTimer, heuristic, actionHeuristic);
            } else { // 10% of the time, we do short random paintRollouts
                delta = rollout.randomRoll(selected, elapsedTimer, heuristic);
            }
            */
            if (Agent.KB.deterministic) {
                delta = rollout.roll(selected.state, elapsedTimer, heuristic, actionPruner, combinedRollActionHeuristic);
            }
            else { // if the game is not deterministic, switch to OLMCTS
                delta = rollout.olRoll(getRootNode().state, selected.state, elapsedTimer, heuristic, actionPruner, combinedRollActionHeuristic);
            }

            if (delta < bounds[0])
                bounds[0] = delta;

            if (delta > bounds[1])
                bounds[1] = delta;

            backUp(selected, delta);
        }
    }

    public int mostVisitedAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        boolean allEqual = true;
        double first = -1;

        for (int i = 0; i < children.length; ++i) {
            if (children[i] != null) {
                if (first == -1)
                    first = children[i].nVisits;
                else if (first != children[i].nVisits)
                    allEqual = false;

                if (children[i].nVisits + m_rnd.nextDouble() * epsilon > bestValue) {
                    bestValue = children[i].nVisits;
                    selected = i;
                }
            }
        }

        if (selected == -1) {
            selected = 0;
        } else if (allEqual) {
            //If all are equal, we opt to choose for the one with the best Q.
            selected = bestAction();
        }
        //this = children[selected];
        return selected;
    }

    public int bestAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;

        for (int i = 0; i < children.length; ++i) {
            if (children[i] != null && children[i].totValue + m_rnd.nextDouble() * epsilon > bestValue) {
                bestValue = children[i].totValue;
                selected = i;
            }
        }

        if (selected == -1) {
            System.out.println("Unexpected selection!");
            selected = 0;
        }

        return selected;
    }


    private SingleTreeNode treePolicy() {
        SingleTreeNode cur = this;

        while (!cur.state.isGameOver() && cur.m_depth < Agent.ROLLOUT_DEPTH) {
            if (cur.notFullyExpanded()) {
                return cur.expand();
            } else {
                cur = cur.uct();
            }
        }

        return cur;
    }

    private SingleTreeNode expand() {
        int bestAction = m_rnd.nextInt(Agent.actions.length);
        double bestValue = -1;

        for (int i = 0; i < children.length; ++i) {
            double x = 0;
            if (children[i] != null) {
                x = children[i].totValue;
            }
            if (x > bestValue && children[i] == null) {
                bestAction = i;
                bestValue = x;
            }
        }

        AsimovState nextState = state.copy();
        nextState.advance(Agent.actions[bestAction]);


        SingleTreeNode tn = new SingleTreeNode(nextState, this);
        children[bestAction] = tn;
        return tn;
    }

    private SingleTreeNode uct() {

        SingleTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;

        for (SingleTreeNode child : this.children) {
            double hvVal = child.totValue;
            double childValue = hvVal / (child.nVisits + epsilon);

            childValue = normalise(childValue, bounds[0], bounds[1]);

            double uctValue = childValue +
                    Agent.K * Math.sqrt(Math.log(nVisits + 1) / (child.nVisits + epsilon));

            uctValue = noise(uctValue, m_rnd.nextDouble());


            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }

        if (selected == null) {
            return this.children[m_rnd.nextInt(this.children.length)];
        }

        return selected;
    }


    private void backUp(SingleTreeNode node, double result) {
        SingleTreeNode n = node;
        while (n != null) {
            n.nVisits++;
            n.totValue += result;
            n = n.parent;
        }
    }

    private boolean notFullyExpanded() {
        for (SingleTreeNode tn : children) {
            if (tn == null) {
                return true;
            }
        }

        return false;
    }

    private double normalise(double a_value, double a_min, double a_max)  {
        if (a_min < a_max)
            return (a_value - a_min)/(a_max - a_min);
        else
            return a_value;
    }

    private double noise(double input, double random) {
        return (input + epsilon) * (1.0 + epsilon * (random - 0.5));
    }


    private SingleTreeNode getRootNode() {
        SingleTreeNode node = this;
        while (node.parent != null) {
            node = node.parent;
        }
        return node;
    }

}
