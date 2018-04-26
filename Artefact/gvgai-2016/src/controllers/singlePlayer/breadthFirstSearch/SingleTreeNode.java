package controllers.singlePlayer.breadthFirstSearch;

import controllers.singlePlayer.breadthFirstSearch.Agent;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.util.Random;

public class SingleTreeNode
{
    private static final double HUGE_NEGATIVE = -10000000.0;
    private static final double HUGE_POSITIVE =  10000000.0;
    public static double epsilon = 1e-6;
    public static double egreedyEpsilon = 0.05;
    public StateObservation state;
    public SingleTreeNode parent;
    public SingleTreeNode[] children;
    public double totValue;
    public int nVisits;
    public static Random m_rnd;
    private int m_depth;
    protected static double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};
    public SingleTreeNode () { this(null, null, null); }

    //public int mctsIterations;



    public static int totalIters = 0;

    public SingleTreeNode (StateObservation state, SingleTreeNode parent, Random rnd)
    {
        this.state = state;
        this.parent = parent;
        this.m_rnd = rnd;
        children = new SingleTreeNode[Agent.NUM_ACTIONS];
        totValue = 0.0;
        if(parent != null)
            m_depth = parent.m_depth+1;
        else
            m_depth = 0;
    }


    public void breadthFirstSearch (ElapsedCpuTimer elapsedTimer)
    {

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;

        int remainingLimit = 10;
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit)
        {
            SingleTreeNode selected = treePolicy();

            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis());

            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
            //System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
        }
        //System.out.println("-- " + numIters + " -- ( " + avgTimeTaken + ")");
        totalIters = numIters;

        //! Edited Alli - 11/12/2017
        //mctsIterations = numIters;

        //ArcadeMachine.performance.add(numIters);
    }

    public SingleTreeNode treePolicy()
    {

        SingleTreeNode cur = this;

        while (!cur.state.isGameOver())
        {
            // Expand the current node
            if (cur.notFullyExpanded())
            {
                return cur.expand();


            } else { // Otherwise expand the next node
                SingleTreeNode next = cur.nextNode();
                //SingleTreeNode next = cur.egreedy();
                cur = next;
            }
        }

        return cur;
    }


    public SingleTreeNode expand()
    {

        int Action = 0;
        for (int i = 0; i < children.length; i++)
        {
            if (children[i] == null)
            {
                Action = i;
                break;
            }
        }

        StateObservation nextState = state.copy();
        nextState.advance(Agent.actions[Action]);

        SingleTreeNode tn = new SingleTreeNode(nextState, this, this.m_rnd);
        children[Action] = tn;
        return tn;
    }

    public SingleTreeNode nextNode ()
    {
        SingleTreeNode selected = null;
        for (SingleTreeNode child : this.children)
        {
            selected = child;
            break;
        }

        return selected;
    }


    public double value(StateObservation a_gameState) {

        boolean gameOver = a_gameState.isGameOver();
        Types.WINNER win = a_gameState.getGameWinner();
        double rawScore = a_gameState.getGameScore();

        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            rawScore += HUGE_NEGATIVE;

        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            rawScore += HUGE_POSITIVE;

        return rawScore;
    }

    public void backUp(SingleTreeNode node, double result)
    {
        SingleTreeNode n = node;
        while(n != null)
        {
            n.nVisits++;
            n.totValue += result;
            n = n.parent;
        }
    }


    public int getAction () {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        boolean allEqual = true;
        double first = -1;

        for (int i = 0; i < children.length; i++)
        {
            if(children[i] != null)
            {
                if(first == -1)
                    first = children[i].state.getGameScore();
                else if(first != children[i].state.getGameScore())
                {
                    allEqual = false;
                }

                double childValue = children[i].state.getGameScore();
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }else if(allEqual)
        {
            //If all are equal, we opt to choose for the one with the best Q.
            selected = bestAction();
        }
        return selected;
    }

    public int bestAction()
    {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;

        for (int i=0; i<children.length; i++)
        {
            if(children[i] != null)
            {
                double childValue = children[i].totValue / (children[i].nVisits + this.epsilon);
                if (childValue > bestValue)
                {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }

        return selected;
    }


    public boolean notFullyExpanded()
    {
        if(children == null)
            return false;
        for (SingleTreeNode tn : children)
        {
            if (tn == null)
            {
                return true;
            }
        }

        return false;
    }
}
