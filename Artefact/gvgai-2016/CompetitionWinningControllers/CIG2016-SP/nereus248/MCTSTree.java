package nereus248;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Pair;
import tools.Utils;
import tools.Vector2d;

public class MCTSTree {
	
	// Random number generator
	private Random rnd;
	// Holds reference to the memory
	private Memory memory;

	static int rolloutDepth;
	// Maximum size of the MCTS tree (this number can be extended, if action prediction stays the same (i.e. when game is not very stochastic))
	private final int minTreeHeight = 2;
	private int currentMaxTreeHeight = 2;
	private final int maxTreeHeight = 3;
	private final ArrayList<Pair<MCTSNode,Integer>> predictedBestPath;
	private int nofCorrectPredictions = 0;	// How many correct predictions in a row
	private double correctTreePredictionThreshold = 2;
	private boolean treeGotShorter = false;
	
	// Minimum depth of rollout (random and non-random)
	private final int minRolloutDepth = 4;
	// Maximum depth of random rollout (random and non-random)
	private final int maxRolloutDepth = 8;
	
	// The time necessary to back propagate all rollout data and return a valid action
	private int remainingTimeLimit = 6;	
	
	private double epsilon = Math.pow(10,-5);
	private double explorationFactor = Math.sqrt(2.0);	
	
	// Rewards for winning/loosing
	private final double winReward = Math.pow(10,2);
	private final double losePenalty = -Math.pow(10,2);
	
	// All available actions of the game
	static int numAvailableActions;
	static ACTIONS[] availableActions;
		
	// Root of the MCTS Tree
	private MCTSNode root;
	// Current node of the non-random rollout
	private MCTSNode currentSelection;

	// Structures to store the winning path (if exists)
	private ArrayList<ACTIONS> winningPath;
	private boolean winningPathFound = false;
	private int winningPathLength = Integer.MAX_VALUE;
	// Stores whether the current winning path is from the previous call of act()
	private boolean winningPathFromLastIteration = false;

	// Temporary storage for current rollout
	private int rolloutPathLength;
	private ACTIONS[] rolloutPath;
	
	// Heuristic structures
	private PheromoneHeuristic pheromoneTrail;
	private PathfindHeuristic originalPathfinder;
	private PathfindHeuristic currentPathfinder;
	
	// Structures to store all the states of the rollout (incl. the chance that it is recorded and sent to memory)
	private double stateSequenceRecordingChance = 0.3;
	private StateObservation[] stateSequence;
	
	// Normalization factor for any square distances on this map
	private double sqDistNormalizationFactor;
	private double coveringGroundNormalizationFactor;
	private double manhattanDistanceNormalizationFactor;
	
	private double blockSize;

	private int maxNofNPCs;
	private int maxNofResources;
	
	private ACTIONS safestAction = ACTIONS.ACTION_NIL;
	
	/**
	 * Constructs a new MCTS and initializes all relevant datastructures
	 * @param availableActions
	 * @param rolloutDepth
	 * @param initialState
	 */
	public MCTSTree(ArrayList<ACTIONS> availableActions, int rolloutDepth, StateObservation initialState, Memory memory) 
	{
		this.memory = memory;
		
		// Initialize datastructures for the rollout (random nad non-random)
		MCTSTree.rolloutDepth = minRolloutDepth;
		rolloutPath = new ACTIONS[maxRolloutDepth];
		stateSequence = new StateObservation[maxRolloutDepth+1];
		
		// Store all available actions
		MCTSTree.numAvailableActions = availableActions.size();
		MCTSTree.availableActions = new ACTIONS[numAvailableActions];
		for (int i = 0; i < numAvailableActions; i++) {
			MCTSTree.availableActions[i] = availableActions.get(i);
		}
		
		// Calculate normalization factor for distances
		Dimension worldDimension = initialState.getWorldDimension();
		sqDistNormalizationFactor = 1.0/(worldDimension.getHeight()*worldDimension.getHeight() + worldDimension.getWidth()*worldDimension.getWidth());
		manhattanDistanceNormalizationFactor = 1.0/(worldDimension.height + worldDimension.width);
		blockSize = initialState.getBlockSize();
		
		// Initialize random number generator
		rnd = new Random();
		
		// Initialize datastructure to keep track of the predictions made in earlier iterations
		predictedBestPath = new ArrayList<Pair<MCTSNode,Integer>>();

		
		// Initialize tree
		root = new MCTSNode(null, null, null, null, memory, initialState, ACTIONS.ACTION_NIL);
		root.setMCTSRolloutEA(new MCTSRolloutEA(rolloutDepth - currentMaxTreeHeight, rnd, this));
		
		// Stores the maximal number of NPCs
		maxNofNPCs = 0;
		maxNofResources = 0;
	}
	
	/**
	 * Performs the Monte Carlo Tree Search for as long as possible and returns the best action.
	 * @param state : The current state of the game
	 * @param elapsedTimer
	 * @return
	 */
	public ACTIONS chooseAction(StateObservation state, ElapsedCpuTimer elapsedTimer, PheromoneHeuristic pheromoneHeuristic, PathfindHeuristic pathfinder) 
	{		
		// Reset and set pathfinder
		this.currentPathfinder = null;
		this.originalPathfinder = pathfinder;
		
		// Set pheromone trail heuristic
		pheromoneTrail = pheromoneHeuristic;
		
		// Update the tree to the most recent state (i.e. set new root)
		registerSelectedAction(state.getAvatarLastAction());
		// Update root with the current state
		root.state = state;
		root.depth = 0;
		// Initialize all other nodes of the tree (bring them up to date about the current state)
		initNodes(root);
		// Find safest action based on the previous rollout
		findSafestAction();
		// Remove all children from the root
		pruneTree();

		// Calculate normalization factor for the heuristic that tries to maximize the avatar's distance 
		// from the root after the rollout (has to be calculated as the rolloutDepth may vary)
		coveringGroundNormalizationFactor = 1.0/(blockSize*blockSize*rolloutDepth*rolloutDepth);
		
		// Set the first entry of the current state sequence (for the memory)
		stateSequence[0] = state.copy();
		

		// Datastructures to keep track of the time
		int numIters = 0;
		double totalTimeTaken = 0;
		double avgTimePerIteration = 0;
        long remainingTime = elapsedTimer.remainingTimeMillis();

        // Perform simulations until time runs out
        double rolloutScore = 0;
        while(remainingTime > remainingTimeLimit + 2*avgTimePerIteration) {
        	// Create new timer for each iteration
        	ElapsedCpuTimer iterationTimer = new ElapsedCpuTimer();
        	
        	// Start at the root for every sampling
        	currentSelection = root;
        	// Set initial state of pathfinder heuristic
        	if(originalPathfinder != null) {
        		currentPathfinder = originalPathfinder.copy();
        	}
        	// Go down the tree and select a leaf node
        	select();
        	// Perform random rollout from leaf node and get the heuristic value at the end location
        	rolloutScore = rollout();
        	backpropagateRolloutResult(rolloutScore);
        	
        	// Keep track of the time
        	numIters++;
        	totalTimeTaken += iterationTimer.elapsedMillis();
        	avgTimePerIteration = totalTimeTaken / numIters;
        	
        	remainingTime = elapsedTimer.remainingTimeMillis();
        	//// System.out.println("Iteration time = " +iterationTimer.elapsedMillis() + " totalTime =  " + totalTimeTaken + " remaining = " + remainingTime);
        }
        
        // System.out.println("-- " + numIters + " -- rolloutDepth = " + (rolloutDepth - root.depth) + " its -- avgTime = " + avgTimePerIteration);
        
        // If rollout does not cost much time, then deepen the random rollout
    	if (avgTimePerIteration < 0.4) {
    		rolloutDepth = Math.min(maxRolloutDepth, rolloutDepth+1);
    	}
    	// If it costs too much time, then reduce random rollout depth
    	else if (avgTimePerIteration > 0.5){
    		rolloutDepth = Math.max(minRolloutDepth, rolloutDepth-1);
    	}
        
    	// Chose an action to take
        ACTIONS bestAction = findBestAction();
        
        
        // Update datastructure that estimates how much stochastic events there are in the game
        boolean hasChildren = false;
        if(predictedBestPath.size() == 0) {
        	// If the list is empty (either first call of chooseAction() or there was a wrong prediction)
        	// then insert the very next step as first prediction). 
        	for(MCTSNode child : root.children) {
        		if (child != null && child.thisNodesAction == bestAction) {
        			hasChildren = true;
        			predictedBestPath.add(new Pair<MCTSNode,Integer>(child,1));
        			break;
        		}
        	}
        }
        if (hasChildren) {
	        // Insert as many predictions as necessary
	        MCTSNode nextPredictedNode = predictedBestPath.get(predictedBestPath.size()-1).first;
	        int predictionDepth = predictedBestPath.size();
	    	while(predictedBestPath.size() < currentMaxTreeHeight) {
	    		nextPredictedNode = getMostLikelySuccessorOFNode(nextPredictedNode);
	    		if(nextPredictedNode != null) {
	    			predictionDepth++;
	    			predictedBestPath.add(new Pair<MCTSNode,Integer>(nextPredictedNode,predictionDepth));
	    		} else {
	    			break;
	    		}
	    	}
        }
        //// System.out.println("CurrentMaxTreeHeight = " + currentMaxTreeHeight);
        winningPathFromLastIteration = true;

		return bestAction;
	}
	
	
	/**
	 * Removes all children from the root
	 */
	private void pruneTree() {
		// Remove all children from the root and reset the score of the root
		root.children = new MCTSNode[numAvailableActions];
		root.totalScore = 0;
		root.nofVisits = 0;
		root.fullyExpanded = false;
	}
	
	
	/**
	 * Called from the initNodes function, which knows, what action has been executed in the last step. 
	 * The action decides on how the tree develops (i.e. which branches survive and which not)
	 * @param action
	 */
	private void registerSelectedAction(ACTIONS action) {

		MCTSNode newRoot = root;
		// Find new root, discard rest of the tree.
		for ( MCTSNode child : root.children) {
			// Figure out which child was chosen
			if (child != null && child.thisNodesAction.equals(action)) {
				newRoot = child;
				break;
			}
		}
		// Update the root
		newRoot.parent = null;
		root = newRoot;
		
		// Check how predictable the current game is (i.e. does the selected action correspond to the one predicted some steps earlier)
		// It that is the case, then we can enlarge the tree depth
		if (predictedBestPath.size() != 0) {
			// Check whether the prediction was correct
			if (action == predictedBestPath.get(0).first.thisNodesAction) {
				// If it was correct, then update datastructures unless it was the return value of the chooseAction() function (level 1)
				if (predictedBestPath.get(0).second > 1) {
					nofCorrectPredictions++;
				}
				// Remove this prediction, which gives space for the next prediction at the end of the array
				predictedBestPath.remove(0);
				if (nofCorrectPredictions > correctTreePredictionThreshold) {
					currentMaxTreeHeight = Math.min(maxTreeHeight, currentMaxTreeHeight+1);
					correctTreePredictionThreshold *= 2;
				}
			} else {
				// Otherwise shorten the tree to the depth, where the wrong prediction occured
				nofCorrectPredictions = 0;
				int oldHeight = currentMaxTreeHeight;
				currentMaxTreeHeight = Math.max(minTreeHeight, predictedBestPath.get(0).second);
				if (currentMaxTreeHeight < oldHeight)
					treeGotShorter = true;
				correctTreePredictionThreshold = Math.pow(2, predictedBestPath.get(0).second);
				predictedBestPath.clear();
			}
		}
	}

	
	/**
	 * Updates all nodes at the begin of a new iteration.  
	 * @param node
	 */
	private void initNodes(MCTSNode node) {
		
		// Keep observations from previous iterations but weaken them
		node.nofVisits *= 0.3;
		node.totalScore *= 0.3;
		
		// Make sure that the node's state is up to date (state is propagated top down)
		if (node.parent != null) {
			node.depth = node.parent.depth + 1;
			// Take the already updated state of the parent node...
			StateObservation nextState = node.parent.state.copy();
			// ...and apply the action of this node
			nextState.advance(node.thisNodesAction);
			// Save the updated state.
			node.state = nextState;
			// Get the actual gamescore when going to this location
			node.actualScore = evaluateRollout(node.state);
			
			// Propagate the path to the highest score top down (needs a bottom up propagation later in order to see the result at the root)
			if (node.state.getGameWinner() != Types.WINNER.PLAYER_LOSES && node.actualScore >= node.parent.highestScoreInThisDirection ) {
				node.highestScoreInThisDirection = node.actualScore;
				node.nofStepsUntilHighestScoreInThisDirection = node.depth;
			}
			else {
				node.highestScoreInThisDirection = node.parent.highestScoreInThisDirection;
				node.nofStepsUntilHighestScoreInThisDirection = node.parent.nofStepsUntilHighestScoreInThisDirection;
			}
		}
		else
		{
			// Root node
			node.actualScore = evaluateRollout(node.state);
			node.highestScoreInThisDirection = node.actualScore;
			node.nofStepsUntilHighestScoreInThisDirection = 0;
		}

		// If the tree is getting shorter, then take all the best paths from the children
		// before setting them null in the next loop
		if (treeGotShorter && node.depth == currentMaxTreeHeight) {
			node.mctsRolloutEA.getEARolloutPathsFromChildren(node.children);
		}
		
		// Propagate init to node's children
		for (MCTSNode child : node.children) {
			// Stop once we reached the end of the tree
			if (child != null) {
				if (child.depth > currentMaxTreeHeight)
					// Remove child if we are reducing the tree height
					child = null;
				else {
					initNodes(child);
					// Backpropagate the safest path
					if (child.nofStepsUntilHighestScoreInThisDirection > node.nofStepsUntilHighestScoreInThisDirection) {
						node.highestScoreInThisDirection = child.highestScoreInThisDirection;
						node.nofStepsUntilHighestScoreInThisDirection = child.nofStepsUntilHighestScoreInThisDirection;
					}
				}
			}
		}
	}
	
	/**
	 * Select the next node to visit.
	 */
	private void select() {
		// Go as deep as possible in the search tree
		while (!currentSelection.state.isGameOver() && currentSelection.depth <= currentMaxTreeHeight)
        {
			// If the tree is not fully expanded at that depth, then expand it in one random direction
            if (!currentSelection.isFullyExpanded() && !currentSelection.state.isGameOver()) {
            	PathfindHeuristic pathfinder = null;
            	if (currentPathfinder != null) {
            		pathfinder = currentPathfinder.copy();
            	}
                currentSelection = currentSelection.expand(rnd, root, pathfinder, pheromoneTrail, memory);
                stateSequence[currentSelection.depth] = currentSelection.state;
                rolloutPath[currentSelection.depth-1] = currentSelection.thisNodesAction;
                return;	// Return
            } else {
            	// If tree is fully expanded at that depth, then dig deeper down (i.e. select branch and reiterate)
            	selectActionAtCurrentNode();
            	stateSequence[currentSelection.depth] = currentSelection.state;
            	if(currentPathfinder != null && !currentSelection.state.isGameOver()) {
            		currentPathfinder.step(currentSelection.state.getAvatarPosition());
            	}
            	rolloutPath[currentSelection.depth-1] = currentSelection.thisNodesAction;
            }
        }
	}
	
	
	/**
	 * Performs a random rollout of the game in the current state and returns the score after the rollout.
	 * @return heuristic score at end of rollout
	 */
	private double rollout() 
	{
		double random = rnd.nextDouble();
		
		// Get the current state and depth
		StateObservation currentState = currentSelection.state.copy();
		int currentDepth = currentSelection.depth;
		double rolloutScore = 0;
		
		if (!currentSelection.state.isGameOver()) {	
						
			
			// Get the necessary rollout path length
			int pathLength = rolloutDepth - currentDepth;
			// Set desired path length...
			currentSelection.mctsRolloutEA.setHeight(pathLength);
			// ...then get "random" path
			EAPathItem EARolloutPath = currentSelection.mctsRolloutEA.getRecombinationOfBestPath();
			
			
			ACTIONS action;
			for (int i = 0; i < pathLength; i++) {
				action = EARolloutPath.path.get(i);
				// Store action sequence
				rolloutPath[currentDepth] = action;
				// Apply forward model
				currentState.advance(action);
				if(random < stateSequenceRecordingChance) {
					stateSequence[currentDepth+1] = currentState.copy();
				}
				currentDepth++;
				if(currentState.isGameOver())
					break;
			}
			rolloutScore = evaluateRollout(currentState);
			
			// Set score...
			EARolloutPath.score = rolloutScore;
			// ... then update
			currentSelection.mctsRolloutEA.update();
			
		}
		else {
			rolloutScore = evaluateRollout(currentState);
		}

		rolloutPathLength = currentDepth;
		
		if(random < stateSequenceRecordingChance) {
			memory.registerEvents(stateSequence, currentDepth);
		}

		currentSelection.lowerScoreBound = Math.min(rolloutScore, currentSelection.lowerScoreBound);
		currentSelection.upperScoreBound = Math.max(rolloutScore, currentSelection.upperScoreBound);
		
		return rolloutScore;
	}
	
	
	/**
	 * Calculates a heuristic value for the final state of the rollout and returns that value
	 * @param rolloutFinalState
	 * @return
	 */
	private double evaluateRollout(StateObservation rolloutFinalState) {
		
		double gameScore = rolloutFinalState.getGameScore() - root.state.getGameScore();
		boolean isGameOver = rolloutFinalState.isGameOver();
		
		// If gameover, then add a winReward / lossPenalty
		if (isGameOver) {
			Types.WINNER gameOverState = rolloutFinalState.getGameWinner();
			if (gameOverState == Types.WINNER.PLAYER_LOSES) {
				gameScore += losePenalty;
			}
			else if (gameOverState == Types.WINNER.PLAYER_WINS) {
				winningPathFound = true;
				// If we found a path that leads to victory, then store it (the shortest/newest one)
				if (rolloutPathLength < winningPathLength || (winningPathFromLastIteration && rolloutPathLength <= winningPathLength)) {
					winningPathFromLastIteration = false;
					winningPathLength = rolloutPathLength;
					winningPath = new ArrayList<ACTIONS>(rolloutPathLength);
					for (int i = 0; i < rolloutPathLength; i++) {
						winningPath.add(rolloutPath[i]);
					}
				}
				gameScore += winReward;
			}
		}
		
		// Reward for proximity/distance to certain sprites
		// Would be better to shortest path distance than just euclidean/manhattan
		int nofNeighbours = 2;
		double totalCount = 0;
		double proximityScore = 0;
		int nofNPCs = 0;
		Vector2d currentPosition = rolloutFinalState.getAvatarPosition();
		ArrayList<Observation>[] NPCPositions = rolloutFinalState.getNPCPositions(currentPosition);
		if (NPCPositions != null) {
			for(int i = 0; i < NPCPositions.length; i++) {
				int observationCount = 0;
				nofNPCs += NPCPositions[i].size();
				for (Observation o : NPCPositions[i]) {
					if(observationCount >= nofNeighbours) {
						break;
					}
					observationCount++;
					proximityScore -= o.sqDist*memory.getSpriteTypeRewardFactor(rolloutFinalState, o.itype);
				}
				totalCount += observationCount;
			}
		}
		ArrayList<Observation>[] portalPositions = rolloutFinalState.getPortalsPositions(currentPosition);
		if (portalPositions != null) {
			for(int i = 0; i < portalPositions.length; i++) {
				int observationCount = 0;
				for (Observation o : portalPositions[i]) {
					if(observationCount >= nofNeighbours) {
						break;
					}
					observationCount++;
					proximityScore -= o.sqDist*memory.getSpriteTypeRewardFactor(rolloutFinalState, o.itype);
				}
				totalCount += observationCount;
			}
		}
		ArrayList<Observation>[] resourcePositions = rolloutFinalState.getResourcesPositions(currentPosition);
		if (resourcePositions != null) {
			for(int i = 0; i < resourcePositions.length; i++) {
				int observationCount = 0;
				for (Observation o : resourcePositions[i]) {
					if(observationCount >= nofNeighbours) {
						break;
					}
					observationCount++;
					proximityScore -= o.sqDist*memory.getSpriteTypeRewardFactor(rolloutFinalState, o.itype);
				}
				totalCount += observationCount;
			}
		}
		if (totalCount != 0) {
			proximityScore /= totalCount;
			proximityScore *= sqDistNormalizationFactor;
			gameScore += proximityScore;
		}
		
		// Reward for minimizing the NPC count
		if (nofNPCs > 0) {
			maxNofNPCs = Math.max(nofNPCs, maxNofNPCs);
			gameScore += nofNPCs / maxNofNPCs;
		}
		
		// Reward for maximizing number of gathered/spent resources
		int nofResources = rolloutFinalState.getAvatarResources().size() - root.state.getAvatarResources().size();
		if (nofResources > 0) {
			maxNofResources = Math.max(maxNofResources,nofResources);
			gameScore += nofResources/maxNofResources;
		}
		
		// Reward for not remaining at the same location
		gameScore -= pheromoneTrail.getPheromoneDensityAt(rolloutFinalState.getAvatarPosition());
		
		// Reward for covering as much ground as possible
		gameScore += 0.5*root.state.getAvatarPosition().sqDist(currentPosition)*coveringGroundNormalizationFactor;
		
		return gameScore;
	}
	
	/**
	 * Branching policy at the current selection (UCT branching)
	 */
	private void selectActionAtCurrentNode() {

		MCTSNode nextNode = currentSelection;
        double bestBranchingScore = -Double.MAX_VALUE;
        for (MCTSNode child : currentSelection.children)
        {
            double totalGameScore = evaluateSelection(child);
            double averageGameScore =  totalGameScore / (child.nofVisits + epsilon);	// epsilon to prevent division by zero

            averageGameScore = Utils.normalise(averageGameScore, currentSelection.lowerScoreBound, currentSelection.upperScoreBound);

            double uctValue = averageGameScore + explorationFactor * Math.sqrt(Math.log(currentSelection.nofVisits + 1) / (child.nofVisits + epsilon));

            // Add noise to the branchingValue in order to break ties
            uctValue = Utils.noise(uctValue, epsilon, rnd.nextDouble());     //break ties randomly

            // Select child with the highest score
            if (uctValue > bestBranchingScore) {
                nextNode = child;
                bestBranchingScore = uctValue;
            }
        }
        
        // Select node
        currentSelection = nextNode;
	}
	
	/**
	 * Returns a heuristic value for the current position in the MCTS Tree (i.e. non-random rollout)
	 * @param child
	 * @return
	 */
	private double evaluateSelection(MCTSNode child) 
	{
		double totalGameScore = child.totalScore;
		double gameScore = child.gameScore;
		double proximityScore = child.proximityScoreNonNormalized*sqDistNormalizationFactor;
		double nofNPCsScore = child.NPCCount;
		double nofResourcesScore = child.nofResources;
		double pathfinderScore = child.pathfinderScore;
		double movementScore = child.movementScore;
		double pheromoneScore = child.pheromoneScore;
		
		// Normalize NPC score
		if (nofNPCsScore > 0) {
			maxNofNPCs = Math.max((int)nofNPCsScore, maxNofNPCs);
			nofNPCsScore /= maxNofNPCs;
		}
	
		// Normalize resources score
		if (nofResourcesScore > 0) {
			maxNofResources = Math.max(maxNofResources,(int)nofResourcesScore);
			nofResourcesScore /= maxNofResources;
		}
		
		double score = totalGameScore + gameScore + proximityScore + nofNPCsScore + nofResourcesScore + pathfinderScore + movementScore - pheromoneScore;  
		return score;
		
	}
	
	/**
	 * Adds the final score of the rollout to all relevant nodes of the tree.  
	 * @param score
	 */
	private void backpropagateRolloutResult(double score) {
		
		// Count how often this node was visited
		currentSelection.nofVisits++;
		// Accumulate the total expected score of this node
		currentSelection.totalScore += score;

		// If we haven't reached the root yet, then continue backpropagation
		if (currentSelection.parent != null) {
			currentSelection = currentSelection.parent;
			backpropagateRolloutResult(score);
		}
		return;
	}
	

	/**
	 * Returns the best action according to the MCTS tree
	 * @return
	 */
	private ACTIONS findBestAction() {
				
		// If a winning strategy exists, then follow the winning strategy
		if (winningPathFound) {
			if (winningPath.size() > 0) {
				ACTIONS action = winningPath.get(0);
				// Remove the first step for the next iteration
				winningPath.remove(0);
				return action;
			} else {
				winningPathFound = false;
			}
		}
		
		// Iterate over all actions performable from the root and chose the one that
		// has the highest average score.
		double maxNofVisits = -1;
		double maxAverageScore = -Double.MAX_VALUE;
		ACTIONS currentMaxAction = availableActions[rnd.nextInt(numAvailableActions)];
		for (int i = 0; i < numAvailableActions; i++) {

			MCTSNode child = root.children[i];
			if(child == null)
				continue;
			
			// System.out.println(child.thisNodesAction + " nofVisits = " + child.nofVisits + " score = " + child.totalScore/child.nofVisits);
			double averageScore = child.totalScore / child.nofVisits;
			if (averageScore > maxAverageScore || (averageScore == maxAverageScore && child.nofVisits > maxNofVisits)) {
				maxNofVisits = child.nofVisits;
				maxAverageScore = averageScore;
				currentMaxAction = child.thisNodesAction;
			}
		}
		
		return currentMaxAction;
		
	}
	
	// Returns the most likely successor-node of the node 'node'
	MCTSNode getMostLikelySuccessorOFNode(MCTSNode node) 
	{		
		// Iterate over all actions performable from the root and chose the one that
		// has the highest average score.
		double maxNofVisits = -1;
		double maxAverageScore = -Double.MAX_VALUE;
		MCTSNode currentMaxChild = null;
		for (int i = 0; i < numAvailableActions; i++) {

			MCTSNode child = root.children[i];
			if(child == null)
				continue;
			
			//// System.out.println(child.thisNodesAction + " nofVisits = " + child.nofVisits + " score = " + child.totalScore/child.nofVisits);
			double averageScore = child.totalScore / child.nofVisits;
			if (averageScore > maxAverageScore || (averageScore == maxAverageScore && child.nofVisits > maxNofVisits)) {
				maxNofVisits = child.nofVisits;
				maxAverageScore = averageScore;
				currentMaxChild = child;
			}
		}
		
		return currentMaxChild;
		
	/*	double maxNofVisits = -1;
		ArrayList<Integer> candidates = new ArrayList<Integer>();
		// Iterate over all actions performable at the selected node 
		// and chose the one that has been visited most often.
		for (int i = 0; i < numAvailableActions; i++) {

			MCTSNode child = node.children[i];
			if(child == null)
				continue;
			
			// Check whether this child has been visited most often
			if (child.nofVisits > maxNofVisits) {
				maxNofVisits = child.nofVisits;
				candidates.clear();
				candidates.add(i);
			} else if (child.nofVisits == maxNofVisits) {
				candidates.add(i);
			}
		}
		
		// If there are some children that have been visited equally often, then decide according to 
		// the achievable score in these.
		if (candidates.size() > 1) {
			double maxAverageScore = -Double.MAX_VALUE;
			int maxCandidate = 0;
			for (int i = 0; i < candidates.size(); i++) {
				double averageScore = node.children[candidates.get(i)].totalScore / maxNofVisits + rnd.nextDouble();
				if (averageScore > maxAverageScore){
					maxAverageScore = averageScore;
					maxCandidate = candidates.get(i);
				}
			}
			return node.children[maxCandidate];
		}
		else {
			if (candidates.size() == 0) {
				return null;
			}
			// If there is only one candidate, then return this one
			return node.children[candidates.get(0)];
		}
		*/
	}
	
	private void findSafestAction() {
		int maxNofSafeSteps = 0;
		ACTIONS safestAction = ACTIONS.ACTION_NIL;
		for(MCTSNode child : root.children) {
			if (child != null && child.nofStepsUntilHighestScoreInThisDirection > maxNofSafeSteps) {
				safestAction = child.thisNodesAction;
				maxNofSafeSteps = child.nofStepsUntilHighestScoreInThisDirection;
			}
		}
		this.safestAction = safestAction;
	}
	
	public ACTIONS getSafestAction() {
		return safestAction;
	}
}
