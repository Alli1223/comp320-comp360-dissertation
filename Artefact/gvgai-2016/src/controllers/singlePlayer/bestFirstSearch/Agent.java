package controllers.singlePlayer.bestFirstSearch;


import DissertationFiles.DataCollection;
import DissertationFiles.Visualisations;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.awt.*;
import java.util.HashSet;
import java.util.LinkedList;

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
        long remaining;

        /*
        remaining = elapsedTimer.remainingTimeMillis();
        while(remaining >= MIN_TIME)
        {
            expand();

            if(actionQueue.size() == 0) {
                break;
            }

            remaining = elapsedTimer.remainingTimeMillis();
        } ;
        */



        //System.out.println("======== "  + maxQ + " " + bestAction + "============");
        return getAction(stateObs);



    }

    private void expand()
    {/*
        if(actionQueue.size() == 0) {
            return;
        }

        TreeNode node = actionQueue.getFirst();

        StateObservation state = node.getCurrentState();
        int actionId = node.getUnexploredAction();

        state = advanceState(state, actionId);

        // Explored state
        if(state == null) {
            checkNode(node);
        }
        // Unexplored state
        else {
            // Create new node
            LinkedList<Integer> actionHistory = cloneLinkedList(node.getActionHistory());
            actionHistory.add(actionId);

            TreeNode followingNode = new TreeNode(state, actionHistory, NUM_ACTIONS);

            actionQueue.add(followingNode);

            checkNode(node);

            evaluate(state, followingNode);
        }
    */}

    private Types.ACTIONS getAction(StateObservation stateObs)
    {/*
        if(calculatedActions.size() != 0) {
            // Reached target score, no more progress from here on out
            if(stateObs.getGameScore() == targetScore && (newNodeCount <= MAX_NEW_NODE_COUNT || calculatedActions.size() > 20)) {
                calculatedActions.clear();
                return Types.ACTIONS.ACTION_NIL;
            }

            Types.ACTIONS action = LOOKUP_INT_ACTION.get(calculatedActions.removeFirst());
            moved = true;

            stateObs = stateObs.copy();
            stateObs.advance(action);
            if(calculatedActions.size() != 0)
            {
                stateObs.advance(LOOKUP_INT_ACTION.get(calculatedActions.getFirst()));
            }

            if(stateObs.getGameWinner() == WINNER.PLAYER_LOSES) {
                switchController = true;
                return Types.ACTIONS.ACTION_NIL;
            }

            return action;
        }
*/
        return Types.ACTIONS.ACTION_NIL;
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
