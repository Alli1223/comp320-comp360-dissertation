package controllers.singlePlayer.sampleonesteplookahead;


import DissertationFiles.DataCollection;
import DissertationFiles.Visualisations;
import controllers.singlePlayer.Heuristics.SimpleStateHeuristic;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.awt.*;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    public static double epsilon = 1e-6;
    public static Random m_rnd;

    //! Edited by Alli 09/03/2018
    // List of variables for storing and rendering MCTS information
    private Visualisations vis = new Visualisations();
    private DataCollection dataCollection = new DataCollection();;
    //! Edit End

    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        m_rnd = new Random();


    }

    /**
     *
     * Very simple one step lookahead agent.
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
        double maxQ = Double.NEGATIVE_INFINITY;
        SimpleStateHeuristic heuristic =  new SimpleStateHeuristic(stateObs);
        for (Types.ACTIONS action : stateObs.getAvailableActions()) {

            StateObservation stCopy = stateObs.copy();
            stCopy.advance(action);
            double Q = heuristic.evaluateState(stCopy);
            Q = Utils.noise(Q, this.epsilon, this.m_rnd.nextDouble());

            //System.out.println("Action:" + action + " score:" + Q);
            if (Q > maxQ) {
                maxQ = Q;
                bestAction = action;
            }


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
