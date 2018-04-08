package controllers.singlePlayer.breadthFirstSearch2;


import DissertationFiles.DataCollection;
import DissertationFiles.Visualisations;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import jdk.nashorn.api.tree.Tree;
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
public class Agent extends AbstractPlayer
{

    //! Edited by Alli 09/03/2018
    // List of variables for storing and rendering MCTS information
    private Visualisations vis = new Visualisations();
    private DataCollection dataCollection = new DataCollection();
    public double max_Score;
    public LinkedList<Types.ACTIONS> path;
    private LinkedList<TreeNode> actionQueue;		// Node list
    private HashSet<Long> exploredStates;

    private static HashMap<Integer, Types.ACTIONS> LOOKUP_INT_ACTION;
    private static HashMap<Types.ACTIONS, Integer> LOOKUP_ACTION_INT;
    private static int NUM_ACTIONS;
    private LinkedList<Integer> calculatedActions;	// Actions that will be executed
    private double prevScore;
    private double currentScore;					// Score of the live frame
    private double targetScore;						// Score that will be reached when the calculated actions are executed
    private int blockSize;
    private static int MIN_TIME = 2;

    private StateObservation prevStateObs;
    private boolean moved;
    private boolean reduceNodeCount;
    private int newNodeCount;
    TreeNode bestNode;
    //! Edit End


    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        NUM_ACTIONS = stateObs.getAvailableActions().size();

        actionQueue = new LinkedList<TreeNode>();
        exploredStates = new HashSet<Long>();

        calculatedActions = new LinkedList<Integer>();
        prevScore = stateObs.getGameScore();
        currentScore = stateObs.getGameScore();
        blockSize = stateObs.getBlockSize();


        prevStateObs = stateObs;
        moved = false;
        reduceNodeCount = false;
        newNodeCount = 0;

        TreeNode initialNode = new TreeNode(stateObs.copy(), new LinkedList<Integer>(), NUM_ACTIONS);
        actionQueue.add(initialNode);

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
        long remaining;
        currentScore = stateObs.getGameScore();


        checkForEnd(stateObs);

        remaining = elapsedTimer.remainingTimeMillis();

        while (remaining >= MIN_TIME)
        {
            expand();

            if (actionQueue.size() == 0) {
                break;
            }

            remaining = elapsedTimer.remainingTimeMillis();
        }

        return getAction(stateObs);
    }


    private void expand()
    {
        if(actionQueue.size() == 0)
        {
            return;
        }

        TreeNode node = actionQueue.getFirst();

        StateObservation state = node.getCurrentState();
        int actionId = node.getUnexploredAction();

        state = advanceState(state, actionId);

        // Explored state
        if(state == null)
        {
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
    }

    private StateObservation advanceState(StateObservation stateObs, int actionId)
    {
        stateObs = stateObs.copy();
        prevScore = stateObs.getGameScore();

        stateObs.advance(LOOKUP_INT_ACTION.get(actionId));

        if(prevScore < stateObs.getGameScore()) {
            reduceNodeCount = true;

        }

        // Dead end
        if(stateObs.isGameOver() && stateObs.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
            return null;
        }

        long stateId = stateObs.hashCode();

        // State already explored
        if(stateObs.getGameScore() < prevScore) {// ||  exploredStates.contains(stateId){
            return null;
        }

        exploredStates.add(stateId);

        return stateObs;
    }

    private void checkNode(TreeNode node)
    {
        if(node.check())
        {
            if(actionQueue.size() == 1)
            {
                if(node.getCurrentState().getGameScore() > currentScore)
                    executeActionNode(actionQueue.getFirst());
                else
                    clear();
            }
            else {
                node.clear();
                actionQueue.removeFirst();
            }
        }
    }


    private void evaluate(StateObservation stateObs, TreeNode node)
    {
        if (stateObs.isGameOver() && stateObs.getGameWinner() == Types.WINNER.PLAYER_WINS)
        {
            transferLinkedList(calculatedActions, node.getActionHistory());
            targetScore = Integer.MAX_VALUE;

            clear();
        }
    }
    private void transferLinkedList(LinkedList<Integer> destList, LinkedList<Integer> sourceList)
    {
        Iterator<Integer> iter = sourceList.iterator();
        while(iter.hasNext())
        {
            destList.addLast(iter.next());
        }
    }
    private LinkedList<Integer> cloneLinkedList(LinkedList<Integer> list) {
        LinkedList<Integer> newList = new LinkedList<Integer>();

        Iterator<Integer> iter = list.iterator();
        while(iter.hasNext()) {
            newList.add(iter.next());
        }

        return newList;
    }

    private void executeActionNode(TreeNode node) {
        if(node == null) {
            clear();
            return;
        }

        StateObservation state = node.getCurrentState().copy();
        transferLinkedList(calculatedActions, node.getActionHistory());
        targetScore = state.getGameScore();

        clear();

        // This state becomes the new default state
        prevScore = state.getGameScore();

        TreeNode initialNode = new TreeNode(state, new LinkedList<Integer>(), NUM_ACTIONS);
        actionQueue.add(initialNode);
    }

    private Types.ACTIONS getAction(StateObservation stateObs)
    {
        if(calculatedActions.size() != 0)
        {
            // Reached target score, no more progress from here on out
            if(stateObs.getGameScore() == targetScore)
            {
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

            if(stateObs.getGameWinner() == Types.WINNER.PLAYER_LOSES)
            {
                return Types.ACTIONS.ACTION_NIL;
            }

            return action;
        }

        return Types.ACTIONS.ACTION_NIL;
    }

    private void checkForEnd(StateObservation stateObs)
    {
        // Is the game nearly over
        if (actionQueue.size() > 0 && stateObs.getGameTick() + actionQueue.getFirst().getCurrentState().getGameTick() + 10 >= 2000) {
            executeActionNode(getOptimalActionNode());
        }
        // No nodes in the queue
        else if (actionQueue.size() == 0 && calculatedActions.size() == 0) {
            newNodeCount++;
            clear();


            TreeNode newNode = new TreeNode(stateObs.copy(), new LinkedList<Integer>(), NUM_ACTIONS);
            actionQueue.add(newNode);

        }
    }

    private TreeNode getOptimalActionNode() {
        if(actionQueue.size() == 0) {
            return null;
        }

        TreeNode optNode = actionQueue.getFirst();
        double maxScore = prevScore;
        int minEventCount = Integer.MAX_VALUE;
        Iterator<TreeNode> iter = actionQueue.iterator();

        StateObservation state;
        double score;
        int eventCount;
        while(iter.hasNext()) {
            TreeNode node = iter.next();
            state = node.getCurrentState();
            score = state.getGameScore();
            eventCount = state.getEventsHistory().size();

            // Choose the highest score with minimal events
            if(score >= maxScore) {
                if(score > maxScore || eventCount < minEventCount) {
                    maxScore = score;
                    minEventCount = eventCount;
                    optNode = node;
                }
            }
        }

        // No progress was made
        if(maxScore <= currentScore) {
            return null;
        }

        return optNode;
    }

    private void clear() {
        Iterator<TreeNode> iter = actionQueue.iterator();
        while(iter.hasNext()) {
            TreeNode n = iter.next();
            n.clear();
        }
        actionQueue.clear();
        exploredStates.clear();

        // This state becomes the new default state
        actionQueue = new LinkedList<TreeNode>();
        exploredStates = new HashSet<Long>();
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
