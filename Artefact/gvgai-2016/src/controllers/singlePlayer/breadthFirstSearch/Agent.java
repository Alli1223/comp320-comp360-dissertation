package controllers.singlePlayer.breadthFirstSearch;


import DissertationFiles.DataCollection;
import DissertationFiles.Visualisations;
import controllers.singlePlayer.Heuristics.SimpleStateHeuristic;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created with IntelliJ IDEA.
 * User: Alastair
 * Date: 04/05/18
 * Time: 22:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    //! Edited by Alli 09/03/2018
    // List of variables for storing and rendering MCTS information
    private Visualisations vis = new Visualisations();
    private DataCollection dataCollection = new DataCollection();
    public double max_Score;
    public LinkedList<Types.ACTIONS> path;
    TreeNode bestNode;
    //! Edit End

    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {




    }

    /**
     *
     * breadth First Search
     *
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */


    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        //! Edited Alli - 09/03/2018
        // Add game state to be collected
        dataCollection.AddGameStateToCollection(stateObs);
        // Edit End


        TreeNode root = new TreeNode(stateObs, null);
        root.isExplored = true;
        traverse(elapsedTimer, root);

        Types.ACTIONS bestAction = null;
        bestAction = traverse(elapsedTimer, root);

        //System.out.println("======== "  + maxQ + " " + bestAction + "============");
        return bestAction;


    }


    private Types.ACTIONS traverse(ElapsedCpuTimer timeElapsed, TreeNode rootNode)
    {

        Types.ACTIONS bestAction = null;
        Queue<TreeNode> queue = new LinkedList<TreeNode>();

        ((LinkedList<TreeNode>) queue).add(rootNode);
        while (queue.size() != 0 && timeElapsed.remainingTimeMillis() > 10)
        {
            TreeNode node = (TreeNode)queue.remove();
            TreeNode child = null;
            node.score = node.currentState.getGameScore();
            while ((child = getUnvisitedChild(node)) != null)
            {
                child.isExplored = true;
                queue.add(child);
                if(child.score > bestNode.score)
                {
                    bestNode = child;
                }
            }
        }

        if(bestNode != null)
        {
            GetPath(bestNode);
            bestAction = path.getFirst();
        }
        return bestAction;
    }

    public void GetPath(TreeNode goal)
    {
        //visted.clear();
        for (Types.ACTIONS act : goal.actions)
        {
            path.add(act);
        }
//		queue.clear();
    }

    private TreeNode getUnvisitedChild(TreeNode node)
    {
        TreeNode returnNode = null;
        for (int i = 0; i < node.children.length; i++)
        {
            if(node.children[i] != null)
                if(!node.children[i].isExplored)
                {
                    returnNode = node.children[i];
                }
        }
        return returnNode;
    }




    public void result(StateObservation stateObservation, ElapsedCpuTimer elapsedCpuTimer)
    {
        //Collect data at end game state
        dataCollection.AddGameEndStats(stateObservation);
    }
    //! Edited by Alli 05/12/2017
    // Draws graphics to the screen
    public void draw(Graphics2D g)
    {
        //! Visualise the trees search space
        vis.renderSearchSpace(g);
    }

}
