package nereus248;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.Vector2d;

public class MCTSNode {

	double nofVisits = 0;
	double totalScore = 0;
	double actualScore = 0;
	
	double gameScore = 0;
	double proximityScoreNonNormalized = 0;
	int NPCCount = 0;
	int nofResources = 0;
	double pathfinderScore = 0;
	double movementScore = 0;
	double pheromoneScore = 0;
	
	MCTSNode parent;
	MCTSNode[] children;
	public StateObservation state;
	
	int nofStepsUntilHighestScoreInThisDirection = 0;
	double highestScoreInThisDirection = -Double.MAX_VALUE;
	
	int depth;
	ACTIONS thisNodesAction;
	private int numAvailableActions;
	
	boolean fullyExpanded = false;
	
	double lowerScoreBound = Double.MAX_VALUE;
	double upperScoreBound = -Double.MAX_VALUE;
	
	MCTSRolloutEA mctsRolloutEA;
	
	
	/** 
	 * Initialize new MCTSNode
	 * @param parent : The parent node of this
	 * @param thisNodesState : The state of this node (i.e. parent node's state plus action of this)
	 */
	MCTSNode(MCTSNode parent, MCTSNode root, PathfindHeuristic pathfinder, PheromoneHeuristic pheromoneTrail, Memory memory, StateObservation thisNodesState, ACTIONS action) {
		
		if (parent == null) {
			depth = 0;
		}
		else {
			depth = parent.depth + 1;
		}
		this.parent = parent;
		this.state = thisNodesState.copy();
		thisNodesAction = action;
		numAvailableActions = MCTSTree.numAvailableActions;
		children = new MCTSNode[numAvailableActions];
		
		if(pathfinder != null) {
			pathfinder.step(state.getAvatarPosition());
		}
		
		calculateScores(pathfinder, pheromoneTrail, root, memory);
	}
	
	private void calculateScores(PathfindHeuristic pathfinder, PheromoneHeuristic pheromoneTrail, MCTSNode root, Memory memory) {

		ACTIONS action = state.getAvatarLastAction();
		Vector2d currentPosition = state.getAvatarPosition();
		
		if (root != null)
			this.gameScore = state.getGameScore() - root.state.getGameScore();
		
		// Get pathfinder reward
		if(pathfinder != null && !state.isGameOver()) {
			Vector2d currentLocation = state.getAvatarPosition();
			pathfinder.step(currentLocation);
			pathfinderScore = 10*pathfinder.heuristic(currentLocation);
		} 
		
		
		// Penalize standing still
		/*if (action == ACTIONS.ACTION_NIL) {
			movementScore -= 0.1;
		}*/
		
		// Penalize going back and forth
		if(parent != null) {
			StateObservation parentState = parent.state;
			// Penalize going back the last step (i.e. left<->right or up<->down)
			if (action == ACTIONS.ACTION_LEFT && parentState.getAvatarLastAction() == ACTIONS.ACTION_RIGHT ||
				action == ACTIONS.ACTION_RIGHT && parentState.getAvatarLastAction() == ACTIONS.ACTION_LEFT ||
				action == ACTIONS.ACTION_UP && parentState.getAvatarLastAction() == ACTIONS.ACTION_DOWN || 
				action == ACTIONS.ACTION_DOWN && parentState.getAvatarLastAction() == ACTIONS.ACTION_UP ) {
				movementScore -= 0.2;
			}
		}
		
		
		// Penalize running into obstacles
		Event e;
		int prevGameTick = state.getGameTick()-1;
		TreeSet<Event> eventHistory = state.getEventsHistory();
		while (!eventHistory.isEmpty()) {
			e = eventHistory.pollLast();
			if (e.gameStep < prevGameTick) {
				break;
			}
			if(memory.isSpriteTypeObstacle.getOrDefault(e.passiveTypeId, false)) {
				movementScore -= 0.3;
			}
		}
		

		// Get reward for proximity/distance to certain sprites
		if (!state.isGameOver()) {
			int nofNeighbours = 2;
			double totalCount = 0;
			double proximityScore = 0;
			int nofNPCs = 0;	// Minimize NPC count
			ArrayList<Observation>[] NPCPositions = state.getNPCPositions(currentPosition);
			if (NPCPositions != null) {
				for(int i = 0; i < NPCPositions.length; i++) {
					int observationCount = 0;
					nofNPCs += NPCPositions[i].size();
					for (Observation o : NPCPositions[i]) {
						if(observationCount >= nofNeighbours) {
							break;
						}
						observationCount++;
						proximityScore -= o.sqDist*memory.getSpriteTypeRewardFactor(state, o.itype);
					}
					totalCount += observationCount;
				}
			}
			ArrayList<Observation>[] portalPositions = state.getPortalsPositions(currentPosition);
			if (portalPositions != null) {
				for(int i = 0; i < portalPositions.length; i++) {
					int observationCount = 0;
					for (Observation o : portalPositions[i]) {
						if(observationCount >= nofNeighbours) {
							break;
						}
						observationCount++;
						proximityScore -= o.sqDist*memory.getSpriteTypeRewardFactor(state, o.itype);
					}
					totalCount += observationCount;
				}
			}
			
			ArrayList<Observation>[] resourcePositions = state.getResourcesPositions(currentPosition);
			if (resourcePositions != null) {
				for(int i = 0; i < resourcePositions.length; i++) {
					int observationCount = 0;
					for (Observation o : resourcePositions[i]) {
						if(observationCount >= nofNeighbours) {
							break;
						}
						observationCount++;
						proximityScore -= o.sqDist*memory.getSpriteTypeRewardFactor(state, o.itype);
					}
					totalCount += observationCount;
				}
			}
			if (totalCount != 0) {
				proximityScore /= totalCount;
				this.proximityScoreNonNormalized = proximityScore;
			}
			
			// Reward for minimizing the NPC count
			if (nofNPCs > 0) {
				NPCCount = nofNPCs;
			}
		}
		
		// Reward for maximizing number of gathered/spent resources
		if (root != null) {
			this.nofResources = Math.abs(state.getAvatarResources().size() - root.state.getAvatarResources().size());
		}
		
		// Reward for not remaining at the same location
		if (pheromoneTrail != null)
			this.pheromoneScore = pheromoneTrail.getPheromoneDensityAt(state.getAvatarPosition());
		
	}
	
	/**
	 * Check whether all children of this node have been expanded already
	 * @return
	 */
	public boolean isFullyExpanded() {
		
		if (fullyExpanded) {
			return fullyExpanded;
		}
		for(int i = 0; i < numAvailableActions; i++) {
			if (children[i] == null) {
				return false;
			}
		}
		fullyExpanded = true;
		return true;
	}
	
	/**
	 * Selects an unexpanded node at random, adds it to the tree and returns it.
	 * @param rnd
	 * @return the newly expanded node
	 */
	public MCTSNode expand(Random rnd, MCTSNode root, PathfindHeuristic pathfinder, PheromoneHeuristic pheromoneTrail, Memory memory) {
		
		double maxRandom = -Double.MAX_VALUE;
		int nodeIndexToExpand = -1;
		// Iterate over all available actions
		for(int i = 0; i < numAvailableActions; i++) {
			// If this action has not yet been performed from this node... 
			if (children[i] == null) {
				// ... then give it a chance to get expanded.
				double randomVal = rnd.nextDouble();
				if (randomVal > maxRandom) {
					maxRandom = randomVal;
					nodeIndexToExpand = i;
				}
			}
		}
		
		// Create the new node
		StateObservation newState = state.copy();
		ACTIONS action = MCTSTree.availableActions[nodeIndexToExpand];
		newState.advance(action);
		children[nodeIndexToExpand] = new MCTSNode(this, root, pathfinder, pheromoneTrail, memory, newState, action);
		if (mctsRolloutEA != null) {
			children[nodeIndexToExpand].setMCTSRolloutEA(new MCTSRolloutEA(mctsRolloutEA,action));
		}
		return children[nodeIndexToExpand];
		
	}
	
	public void setMCTSRolloutEA(MCTSRolloutEA mctsRolloutEA) {
		this.mctsRolloutEA = mctsRolloutEA;
	}
	
	
	
	
}
