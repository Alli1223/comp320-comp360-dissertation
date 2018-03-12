package RayMin;

import core.game.StateObservation;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 07/11/13
 * Time: 17:13
 */
public class SingleMCTSPlayer
{
    /**
     * Root of the tree.
     */
    public SingleTreeNode m_root;

    /**
     * Random generator.
     */
    public Random m_rnd;

    // Record the visited positions
    private final int visitLimit = 5;
    private final LinkedList<Vector2d> visited;

    /**
     * Creates the MCTS player with a sampleRandom generator object.
     * @param a_rnd sampleRandom generator object.
     */
    public SingleMCTSPlayer(Random a_rnd)
    {
        m_rnd = a_rnd;
        m_root = new SingleTreeNode(a_rnd);
        //init the last node list
        visited = new LinkedList<Vector2d>();
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


    	//Set the list of recently visited nodes
    	SingleTreeNode.visited = visited;

        //Do the search within the available time.
        m_root.mctsSearch(elapsedTimer);

        //Determine the best action to take and return it.
        int action = m_root.mostVisitedAction();
        addVisited(m_root.children[action]);
        SingleTreeNode.lastRunAction = action;

        return action;
    }

	public void addVisited(SingleTreeNode node) {
		
		Vector2d pos = node.curPosition;
	
		if(visited.size() < visitLimit) {
			visited.add(pos);
			
		}
		else {
			visited.remove();
			visited.add(pos);
		}
		

	}

}
