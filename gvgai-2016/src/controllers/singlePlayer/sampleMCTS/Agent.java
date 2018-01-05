package controllers.singlePlayer.sampleMCTS;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import Visualisations.Visualisations;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

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
    private StateObservation SO;
    private  Visualisations vis;
    private SingleMCTSPlayer visPlayerCopy;

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
        vis = new Visualisations();
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
        // StateObservation set for draw function
        SO = stateObs;
        ArrayList<Observation> obs[] = stateObs.getFromAvatarSpritesPositions();
        ArrayList<Observation> grid[][] = stateObs.getObservationGrid();

        //Set the state observation object as the new root of the tree.
        mctsPlayer.init(stateObs);

        //Determine the action using MCTS...
        action = mctsPlayer.run(elapsedTimer);
        visPlayerCopy = mctsPlayer;

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
        //stateObservation.getAvatarPosition().x;
    }


    //! Edited by Alli 05/12/2017
    // Draws graphics to the screen
    public void draw(Graphics2D g)
    {


        vis.renderSearchSpace(visPlayerCopy, g);



        //OLD CODE
        /*
        for(int i = 0; i < mctsPlayer.m_root.children.length; i++) {
            if (mctsPlayer.m_root.children[i] != null) {
                StateObservation SOChild = mctsPlayer.m_root.children[i].state;
                if (SOChild != null) {
                    int x = (int) SOChild.getAvatarPosition().x;
                    int y = (int) SOChild.getAvatarPosition().y;


                    if (mctsPlayer.m_root.children[i].children != null) {
                        for (int j = 0; j < mctsPlayer.m_root.children[i].children.length; j++) {
                            SingleTreeNode grandchild = mctsPlayer.m_root.children[i].children[j];
                            if (grandchild != null) {
                                StateObservation SOGrandchild = grandchild.state;
                                int x1 = (int) SOGrandchild.getAvatarPosition().x;
                                int y1 = (int) SOGrandchild.getAvatarPosition().y;

                                g.drawLine((int) SO.getAvatarPosition().x + 25, (int) SO.getAvatarPosition().y + 25, x + 25, y + 25);
                                g.drawLine((int) SO.getAvatarPosition().x + 25, (int) SO.getAvatarPosition().y + 25, x1 + 25, y1 + 25);
                            }
                        }
                    }
                }
            }
        }
        */
    }
}
