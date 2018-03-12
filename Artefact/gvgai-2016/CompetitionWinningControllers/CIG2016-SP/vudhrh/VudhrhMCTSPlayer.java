package vudhrh;


import java.util.Random;

import core.game.StateObservation;
import tools.ElapsedCpuTimer;
import vudhrh.MCTSManager;
/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 07/11/13
 * Time: 17:13
 */
public class VudhrhMCTSPlayer
{
    /**
     * Root of the tree.
     */
    public SingleTreeNode m_root;

    /**
     * Random generator.
     */
    public Random m_rnd;

    public static int iters = 0, num = 0;

    /**
     * Creates the MCTS player with a sampleRandom generator object.
     * @param a_rnd sampleRandom generator object.
     */
    public VudhrhMCTSPlayer(Random a_rnd)
    {
        m_rnd = a_rnd;
        m_root = new SingleTreeNode(a_rnd);
        //MCTSManager m = MCTSManager.getInstance();
        //m.initWeight();
    }

    /**
     * Inits the tree with the new observation state in the root.
     * @param a_gameState current state of the game.
     */
    public void init(StateObservation a_gameState)
    {
        //Set the game observation to a newly root node.
        m_root = new SingleTreeNode(m_rnd);
        m_root.state = a_gameState;

    }

    /**
     * Runs MCTS to decide the action to take. It does not reset the tree.
     * @param elapsedTimer Timer when the action returned is due.
     * @return the action to execute in the game.
     */
    public int run(ElapsedCpuTimer elapsedTimer)
    {
        //Do the search within the available time.
        m_root.mctsSearch(elapsedTimer);

        iters += SingleTreeNode.totalIters;
        num ++;
        //Determine the best action to take and return it.
        int action = m_root.mostVisitedAction();
        //m.prevNode = m_root.children[action];
        //m.prevDirection = action;
        //int action = m_root.bestAction();
        //MCTSManager m = MCTSManager.getInstance();
        //m.push(action);
        return action;
    }

}
