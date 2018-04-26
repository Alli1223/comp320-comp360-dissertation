package RayMin;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.LinkedList;
import java.util.Random;
import tools.Utils;

public class SingleTreeNode 
{
    public static double epsilon = 1e-6;
    public static double egreedyEpsilon = 0.05;
    public StateObservation state;
    public SingleTreeNode parent;
    public SingleTreeNode[] children;
    public double totValue;
    public int nVisits;
    public static Random m_rnd;
    private int m_depth;
    private static double[] bounds = new double[]{0, 1};

    /*
     * Variables are claimed for the MMCTS
     */
    public static double balanceCoefficient = 0.1;
    public static int macroRepeat = 3;

    public static LinkedList<Vector2d> visited;
    public Vector2d curPosition;


    //The action picked in the last mcts run
    public static int lastRunAction;

    //The root of the tree
    private static SingleTreeNode root;


    public SingleTreeNode(Random rnd) {
        this(null, null, rnd);
        root = this;
    }

    public SingleTreeNode(StateObservation state, SingleTreeNode parent, Random rnd) {
        this.state = state;
        this.parent = parent;
        this.m_rnd = rnd;
        curPosition = state == null ? null : state.getAvatarPosition();
        children = new SingleTreeNode[Agent.NUM_ACTIONS];
        totValue = 0.0;
        if (parent != null) {
            m_depth = parent.m_depth + 1;
        } else {
            m_depth = 0;
        }
    }

    public void mctsSearch(ElapsedCpuTimer elapsedTimer) {

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;

        int remainingLimit = 5;
        //MR when remaining time is larger than the biger one 
        //between 2 times of average time has been taken and remainingLimit,
        while (remaining > 2 * avgTimeTaken && remaining > remainingLimit) {
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
            SingleTreeNode selected = treePolicy();
	
            double delta = selected.rollOut();

            backUp(selected, delta);

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis());

            avgTimeTaken = acumTimeTaken / numIters;
            remaining = elapsedTimer.remainingTimeMillis();
        }
    }

    public SingleTreeNode treePolicy() {

        SingleTreeNode cur = this;

        //MR when game is not over and current depth is smaller than overall depth,
        //   expend all state and get one by nextNode
        while (!cur.state.isGameOver() && cur.m_depth < Agent.ROLLOUT_DEPTH) 
        {
            //MR if treenode is not fully expanded, return its expansion; 
            //   if yes, return set its nextNode value as next treenode.
            if (cur.notFullyExpanded()) {
                return cur.expand();

            } else {
                SingleTreeNode next = cur.uct();
                //SingleTreeNode next = cur.egreedy();
                cur = next;
            }
        }

        return cur;
    }

    public SingleTreeNode expand() {

        int bestAction = 0;
        double bestValue = -1;

        //MR traverse children node.
        //   when random(0.0~1.0) value is larger than bestAction and node is null,
        //   this node is bestAction and bestValue is random value.
        for (int i = 0; i < children.length; i++) {
            double x = m_rnd.nextDouble();
            if (x > bestValue && children[i] == null) {
                bestAction = i;
                bestValue = x;
            }
        }

        //MR execute corresponding action above to get nextState
        StateObservation nextState = state.copy();
        nextState.advance(Agent.actions[bestAction]);

        //MR update corresponding children treenode with nextState's treenode
        SingleTreeNode tn = new SingleTreeNode(nextState, this, this.m_rnd);
        children[bestAction] = tn;
        return tn;

    }
 
    public SingleTreeNode uct() {

        SingleTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;

        Vector2d pos;

        for (SingleTreeNode child : this.children) {
            double hvVal = child.totValue;
            double childValue = hvVal / (child.nVisits + this.epsilon);
            
            childValue = balanceCoefficient * hvVal + (1 - balanceCoefficient) * childValue;
            childValue = Utils.normalise(childValue, bounds[0], bounds[1]);

            pos = child.curPosition;
            double loss = visited.contains(pos) ? 0.9 : 1;

            double uctValue = childValue +
                    Agent.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + this.epsilon))
                    + this.m_rnd.nextDouble() * this.epsilon;

            //apply the loss
            uctValue *= loss;

            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }

        if (selected == null)
        {
            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + this.children.length);
        }

        return selected;
    }

    public double rollOut() 
    {
        StateObservation rollerState = state.copy();
        int thisDepth = this.m_depth;

        //MR When reach the bottom of the tree, get a action randomly and execute it.
        while (!finishRollout(rollerState, thisDepth)) {

            int action = m_rnd.nextInt(Agent.NUM_ACTIONS);
            rollerState.advance(Agent.actions[action]);
            thisDepth++;
        }

        double delta = value(rollerState);
        
        //MR Update bounds with delta
        if(delta < bounds[0])
            bounds[0] = delta;

        if(delta > bounds[1])
            bounds[1] = delta;

        return delta;
    }

    public double value(StateObservation a_gameState) {

        boolean gameOver = a_gameState.isGameOver();
        Types.WINNER win = a_gameState.getGameWinner();
        double rawScore = a_gameState.getGameScore();

        if (gameOver && win == Types.WINNER.PLAYER_LOSES) {
            return 0;
        }

        if (gameOver && win == Types.WINNER.PLAYER_WINS) {
            return 1;
        }

        double deltaScore = rawScore - root.state.getGameScore();

        if (deltaScore > 0) {
            return 1 - Math.pow(0.25, deltaScore);
        }
        if (deltaScore == 0) {
            return 0.5;
        }

        return 0.25;

    }

    public boolean finishRollout(StateObservation rollerState, int depth) {
        if (depth >= Agent.ROLLOUT_DEPTH) // rollout end condition.
        {
            return true;
        }

        if (rollerState.isGameOver()) // end of game
        {
            return true;
        }

        return false;
    }

    public void backUp(SingleTreeNode node, double result) {
        SingleTreeNode n = node;
        while (n != null) {
            n.nVisits++;
            n.totValue += result;
            n = n.parent;
        }
    }

    public int mostVisitedAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        boolean allEqual = true;
        double first = -1;

        for (int i = 0; i < children.length; i++) {

            if (children[i] != null) {
                if (first == -1) {
                    first = children[i].nVisits;
                } else if (first != children[i].nVisits) {
                    allEqual = false;
                }

                double tieBreaker = m_rnd.nextDouble() * epsilon;
                if (children[i].nVisits + tieBreaker > bestValue) {
                    bestValue = children[i].nVisits + tieBreaker;
                    selected = i;
                }
            }
        }

        if (selected == -1) {
            //System.out.println("Unexpected selection!");
            selected = 0;
        } else if (allEqual) {
            // If all are equal, we opt to choose for the one with the best Q.
            selected = bestAction();
        }
        return selected;
    }

    public int bestAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;

        for (int i = 0; i < children.length; i++) {

            double tieBreaker = m_rnd.nextDouble() * epsilon;
            if (children[i] != null
                    && children[i].totValue + tieBreaker > bestValue) {
                bestValue = children[i].totValue + tieBreaker;
                selected = i;
            }
        }

        if (selected == -1) {
            System.out.println("Unexpected selection!");
            selected = 0;
        }

        return selected;
    }

    public boolean notFullyExpanded() {
        for (SingleTreeNode tn : children) {
            if (tn == null) {
                return true;
            }
        }

        return false;
    }
}
