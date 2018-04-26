package controllers.singlePlayer.breadthFirstSearch;


import DissertationFiles.DataCollection;
import DissertationFiles.Visualisations;
import controllers.singlePlayer.Heuristics.SimpleStateHeuristic;
import controllers.singlePlayer.sampleMCTS.SingleMCTSPlayer;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.awt.*;
import java.util.ArrayList;
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

    // List of variables for storing and rendering MCTS information
    private Visualisations vis = new Visualisations();
    private DataCollection dataCollection = new DataCollection();
    public double max_Score;
    public LinkedList<Types.ACTIONS> path;
    public static int NUM_ACTIONS;
    public static Types.ACTIONS[] actions;
    private SingleBrFSPlayer brFSPlayer;
    //! The action to use
    private int action = 0;


    //! Create the breadth first agent
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {

        //Get the actions in a static array.
        ArrayList<Types.ACTIONS> act = stateObs.getAvailableActions();
        actions = new Types.ACTIONS[act.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = act.get(i);
        }
        NUM_ACTIONS = actions.length;

        //Create the player.
        brFSPlayer = new SingleBrFSPlayer();
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
        // Add game state to be collected
        dataCollection.AddGameStateToCollection(stateObs);

        // Initialise breadth first player
        brFSPlayer.init(stateObs);
        // Run the tree search
        action = brFSPlayer.run(elapsedTimer);

        return actions[action];
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
        vis.renderSearchSpace(brFSPlayer.m_root, g);
    }

}
