package controllers.singlePlayer.DFS;


import DissertationFiles.DataCollection;
import DissertationFiles.Visualisations;
import controllers.singlePlayer.Heuristics.SimpleStateHeuristic;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

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
    private DataCollection dataCollection = new DataCollection();;
    //! Edit End

    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {




    }

    /**
     *
     * Depth First Search
     *
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */


    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        //! Edited Alli - 09/03/2018
        // Add game state to be collected
        dataCollection.AddGameStateToCollection(stateObs);
        // Edit End
        Types.ACTIONS bestAction = null;
        SimpleStateHeuristic heuristic =  new SimpleStateHeuristic(stateObs);
        for (Types.ACTIONS action : stateObs.getAvailableActions())
        {
            /*
            Queue queue = new LinkedList();
            queue.add(this.rootNode);
            printNode(this.rootNode);
            rootNode.visited = true;
            while(!queue.isEmpty()) {
                Node node = (Node)queue.remove();
                Node child=null;
                while((child=getUnvisitedChildNode(node))!=null) {
                    child.visited=true;
                    printNode(child);
                    queue.add(child);
                }
            }
            */

        }

        //System.out.println("======== "  + maxQ + " " + bestAction + "============");
        return bestAction;



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
