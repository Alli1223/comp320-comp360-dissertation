package controllers.singlePlayer.bestFirstSearch;


import DissertationFiles.DataCollection;
import DissertationFiles.Visualisations;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.awt.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Alastair
 * Date: 04/05/18
 * Time: 22:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    private Visualisations vis = new Visualisations();
    private DataCollection dataCollection = new DataCollection();
    private static int MIN_TIME = 2;
    private LinkedList<TreeNode> actionQueue;		// Node list
    private HashSet<Long> exploredStates;
    BFS bestSearch = null;


    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        bestSearch = new BFS(stateObs);
        //bestSearch.search(stateObs,elapsedTimer);


    }

    /**
     *
     * Best First Search
     *
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */


    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        //! Edited Alli - 09/03/2018
        // Add game state to be collected
        dataCollection.AddGameStateToCollection(stateObs);


        return bestSearch.Run(stateObs,elapsedTimer);
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


