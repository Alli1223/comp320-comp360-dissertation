package nereus248;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Pair;

public class Agent extends AbstractPlayer {

	static int MCTSRolloutDepth = 10;
	static int maxLife;
	
	static int numActions;
	static ArrayList<ACTIONS> availableActionsInThisGame;
	
	static int blockSize;
	static Dimension worldDimension;
	
	static Memory memory;
	
	static MCTSTree mctsTree;
	static AStar aStarSearch;
	
	static PheromoneHeuristic pheromoneHeuristic;
	static PathfindHeuristic pathfinder;
	
	static boolean searchInProgress;
	static int searchProblemId;

	/**
	 * Initialize the parameters and construct the automated player
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		availableActionsInThisGame = stateObs.getAvailableActions(true);
		numActions = availableActionsInThisGame.size();
		maxLife = stateObs.getAvatarLimitHealthPoints();
		blockSize = stateObs.getBlockSize();
		worldDimension = stateObs.getWorldDimension();

		memory = new Memory(stateObs);
		mctsTree = new MCTSTree(availableActionsInThisGame,MCTSRolloutDepth,stateObs,memory);
		pheromoneHeuristic = new PheromoneHeuristic(stateObs, memory);
	}
	

	/**
	 * decide the next action to be done (either repeating same action or nil or deciding new action)
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 * @return the most suitable action
	 */
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

		PathfindHeuristic pathfinder = memory.getTargetSprite(stateObs, elapsedTimer);
		if (pathfinder != null) {
			pathfinder.step(stateObs.getAvatarPosition());
			//pathfinder = null;
			// System.out.println("Pathfinder has done " + pathfinder.nofIts + " iterations. Score is " + pathfinder.heuristic(stateObs.getAvatarPosition()));
		}

		pheromoneHeuristic.advanceTimeStep(stateObs.getAvatarPosition(), stateObs);
		ACTIONS action = mctsTree.chooseAction(stateObs, elapsedTimer, pheromoneHeuristic, pathfinder);
		
		// Make sure that MCTS does not lead player into certain death because of too many death samples everywhere
		StateObservation nextState = stateObs.copy();
		nextState.advance(action);

		// If the next step would lead us into death...
		if (nextState.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
			action = mctsTree.getSafestAction();
			// System.out.println("Rescued from certain death by " + action);
		}
		/*if (nextState.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
			action = mctsTree.getSafestAction();
			double maxNumSafeSteps = 0;
			// ...then find an action that does not
			int i;
			for (i = 0; i < numActions-1; i++) {
				double avgNumSafeSteps = getAvgNumSafeSteps(availableActionsInThisGame.get(i), stateObs.copy(), 1);
				// If we found an action with higher chance for survival, then go there
				if(avgNumSafeSteps > maxNumSafeSteps) {
					action = availableActionsInThisGame.get(i);
					maxNumSafeSteps = avgNumSafeSteps;
				}
			}
			// Last direction does not need the copy state constructor
			double avgNumSafeSteps = getAvgNumSafeSteps(availableActionsInThisGame.get(i), stateObs, 1);
			// If we found an action with higher chance for survival, then go there
			if(avgNumSafeSteps > maxNumSafeSteps) {
				action = availableActionsInThisGame.get(i);
				maxNumSafeSteps = avgNumSafeSteps;
			}
			// System.out.println("Rescued from certain death by " + action + ", which has " + maxNumSafeSteps + " safe steps.");
		} */
		
		// System.out.println(action);
		return action;
	}
	
	/**
	 * Returns for every possible action how many steps the avatar could still make on average before dying (or reaching the max rollout depth). 
	 * @param action
	 * @param currentState
	 * @param depth
	 * @return
	 */
	private double getAvgNumSafeSteps(ACTIONS action, StateObservation currentState, int depth) {
		
		// If target depth is reached, then return
		if(depth <= 0) {
			return 0.0;
		}
		currentState.advance(action);
		
		if (currentState.getGameWinner() == Types.WINNER.PLAYER_LOSES)
			// If dead, then return 0
			return 0.0;
		else {
			// If still alive, then set count to 1 and get the average from deeper rollouts
			double count = 1;
			double avg = 0;
			int i;
			for (i = 0; i < numActions-1; i++) {
				avg += getAvgNumSafeSteps(availableActionsInThisGame.get(i),currentState.copy(),depth-1);
			}
			// For the last action we do not have to perform the state.copy() operation
			avg += getAvgNumSafeSteps(availableActionsInThisGame.get(i),currentState,depth-1);
			avg /= (double)numActions;
			return count + avg;
		}
	}
	
	
	/**
     * Function called when the game is over. This method must finish before CompetitionParameters.TEAR_DOWN_TIME,
     *  or the agent will be DISQUALIFIED
     * @param stateObservation the game state at the end of the game
     * @param elapsedCpuTimer timer when this method is meant to finish.
     */
    public void result(StateObservation stateObservation, ElapsedCpuTimer elapsedCpuTimer)
    {
        //Include your code here to know how it all ended.
    	// System.out.println("obstacles: " + memory.isSpriteTypeObstacle);
    	HashMap<Integer,Pair<Double,Integer>> rewardFactors = memory.spriteTypeRewardFactor;
    	for(int i = 0; i < 100; i++) {
    		if (rewardFactors.containsKey(i)) {
    			// System.out.println("Reward factor = " + rewardFactors.get(i).first + " Visits = " + rewardFactors.get(i).second );
    		}
    	}
    	
		/*for (int y = 0; y<worldDimension.height/blockSize; y++) {
			for (int x = 0; x<worldDimension.width/blockSize; x++) {
    			System.out.print(memory.deathMap[x][y].nofDeaths + "/" +memory.deathMap[x][y].nofVisits + "  ");
    		}
    		System.out.print("\n");
    	}
    	System.out.println("Game over after " + stateObservation.getGameTick() + " ticks. Result : " + stateObservation.getGameWinner());
        // System.out.println("Game over? " + stateObservation.isGameOver());*/
    }
}

