package controllers.singlePlayer.sampleMCTS;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import DissertationFiles.DataCollection;
import DissertationFiles.Visualisations;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer
{

    public static int NUM_ACTIONS;
    public static int ROLLOUT_DEPTH = 50;
    public static double K = Math.sqrt(2);
    public static Types.ACTIONS[] actions;
    private int action = 0;




    //! Edited by Alli 05/12/2017
    // List of variables for storing and rendering MCTS information
    private  Visualisations vis = new Visualisations();
    private DataCollection dataCollection = new DataCollection();;

    /**
     * Random generator for the agent.
     */
    private SingleMCTSPlayer mctsPlayer;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {

        //Get the actions in a static array.
        ArrayList<Types.ACTIONS> act = so.getAvailableActions();
        actions = new Types.ACTIONS[act.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = act.get(i);
        }
        NUM_ACTIONS = actions.length;

        //Create the player.
        mctsPlayer = new SingleMCTSPlayer(new Random());

    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        //! Edited Alli - 16/01/2018
        // Add game state to be collected
        dataCollection.AddGameStateToCollection(stateObs);
        // Edit End

        ArrayList<Observation> obs[] = stateObs.getFromAvatarSpritesPositions();
        ArrayList<Observation> grid[][] = stateObs.getObservationGrid();

        //Set the state observation object as the new root of the tree.
        mctsPlayer.init(stateObs);

        //Determine the action using MCTS...
        action = mctsPlayer.run(elapsedTimer);


        //... and return it.
        return actions[action];
    }

    /**
     * Function called when the game is over. This method must finish before CompetitionParameters.TEAR_DOWN_TIME,
     *  or the agent will be DISQUALIFIED
     * @param stateObservation the game state at the end of the game
     * @param elapsedCpuTimer timer when this method is meant to finish.
     */
    public void result(StateObservation stateObservation, ElapsedCpuTimer elapsedCpuTimer)
    {
        System.out.println("MCTS avg iters: " + SingleMCTSPlayer.iters / SingleMCTSPlayer.num);
        //Include your code here to know how it all ended.
        //System.out.println("Game over? " + stateObservation.isGameOver());

        //Collect data at end game state
        dataCollection.AddGameEndStats(stateObservation);

    }


    //! Edited by Alli 05/12/2017
    // Draws graphics to the screen
    public void draw(Graphics2D g)
    {

        //! Visualise the trees search space
        vis.renderSearchSpace(mctsPlayer.m_root, g);



    }
}
