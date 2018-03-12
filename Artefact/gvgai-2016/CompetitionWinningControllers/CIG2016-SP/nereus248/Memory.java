package nereus248;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeSet;

import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Pair;
import tools.Utils;
import tools.Vector2d;

public class Memory {
	
	AStar aStarSearch;
	
	private Random rnd;

	Pair<Double,Double> scoreDifferenceBoundaries;
	
	HashMap<Integer,Boolean> isSpriteTypeObstacle;	// Stores for each sprite type whether it is an obstacle or not
	HashMap<Integer,Object> visitedSpriteIds;	// A list that keeps track of which sprites have already been visited. 
	HashMap<Integer,Pair<Double,Integer>> spriteTypeRewardFactor;	// Stores for each visited sprite type, whether it's worth going there or not and how often this sprite has been visited (integer hash should also consider avatar upgrades, resources etc)
	
	DeathMapEntry[][] deathMap;
	
	Dimension worldDimensions;
	
	ArrayList<PathfindHeuristic> pathfinders;
	PathfindHeuristic currentPathfinder;
	int currentPathfinderFrameCount;
	
	int currentSearchProblemId = -1;
	
	public Memory(StateObservation initialState) {
		rnd = new Random();
		aStarSearch = new AStar(initialState);
		spriteTypeRewardFactor = new HashMap<Integer,Pair<Double,Integer>>();
		visitedSpriteIds = new HashMap<Integer,Object>();
		isSpriteTypeObstacle = new HashMap<Integer,Boolean>();
		pathfinders = new ArrayList<PathfindHeuristic>();
		
		scoreDifferenceBoundaries = new Pair<Double,Double> (-1.0,1.0);
		
		currentPathfinder = null;
		
		worldDimensions = initialState.getWorldDimension();
		int blockSize = initialState.getBlockSize();
		int worldWidth = worldDimensions.width / blockSize;
		int worldHeight = worldDimensions.height / blockSize;
		deathMap = new DeathMapEntry[worldWidth][worldHeight];
		for (int x = 0; x < worldWidth; x++) {
			for (int y = 0; y < worldHeight; y++) {
				deathMap[x][y] = new DeathMapEntry();
			}
		}
	}
	
	
	/**
	 * Erases all data stored in this memory. This object can be reused.
	 */
	void clearMemory() {
		
	}
	
	/**
	 * Send a stateSequence from the rollout step for processing to the memory.
	 * Will try to extract a reward factor for visiting/proximity to some sprites
	 * and which sprites are obstacle and which aren't
	 * @param startFrame x
	 * @param endFrame x+n
	 * @param stateHistory size = n+1
	 */
	void registerEvents(StateObservation[] stateHistory, int lastIndex) 
	{
		if (lastIndex < 0)
			return;
		
		Event event;
		double scoreDiff;
		Vector2d currentPosition;
		Pair<Double,Integer> rewardEntry;
		
		int currentStateIndex = lastIndex;
		StateObservation currentState = stateHistory[currentStateIndex];
		StateObservation state_t_minus_1 = stateHistory[currentStateIndex-1];
		TreeSet<Event> eventHistory = currentState.getEventsHistory();
		
		int t = lastIndex;
		Vector2d positionHistory[] = new Vector2d[t+1];

		int lastFrame = currentState.getGameTick();
		int currentFrame = currentState.getGameTick();
		int previousFrame = currentState.getGameTick();
		int startFrame = stateHistory[0].getGameTick();
		
		scoreDiff = currentState.getGameScore() - state_t_minus_1.getGameScore();
		Utils.normalise(scoreDiff, -1.0, 1.0);
		
		Vector2d gameOverLocation = null;
		ArrayList<Integer> gameOverCausingSpriteTypeIds = new ArrayList<Integer>();
		
		if(currentState.isGameOver()) {
			Types.WINNER gameOverState = currentState.getGameWinner();
			
			if (gameOverState == Types.WINNER.PLAYER_LOSES) {
				scoreDiff = -1.1;
			}
			else if (gameOverState == Types.WINNER.PLAYER_WINS) {
				scoreDiff = 1.1;
			}
			Vector2d position = state_t_minus_1.getAvatarPosition();
			ArrayList<Observation>[][] observationGrid = currentState.getObservationGrid();
			int blockSize = currentState.getBlockSize();
			int x = (int)position.x / blockSize;
			int y = (int)position.y / blockSize;
			// This is not correct, if the avatar runs into a wall during the action...
			if (currentState.getAvatarLastAction() == ACTIONS.ACTION_UP) {
				y -= 1;
				position.y -= blockSize;
			} else if (currentState.getAvatarLastAction() == ACTIONS.ACTION_LEFT) {
				x -= 1;
				position.x -= blockSize;
			} else if (currentState.getAvatarLastAction() == ACTIONS.ACTION_RIGHT) {
				x += 1;
				position.x += blockSize;
			} else if (currentState.getAvatarLastAction() == ACTIONS.ACTION_DOWN) {
				y += 1;
				position.y += blockSize;
			}
			gameOverLocation = position;
			if(x >= 0 && x < observationGrid.length && y >= 0 && y < observationGrid[0].length) {
				deathMap[x][y].nofDeaths++;
				deathMap[x][y].nofVisits++;
				for(Observation o : observationGrid[x][y]) {
					int spriteTypeId = o.itype;
					rewardEntry = spriteTypeRewardFactor.getOrDefault(spriteTypeId, new Pair<Double,Integer>(0.0,0));
					double newScore = (0.8*rewardEntry.first + 0.2*scoreDiff);	// Update the reward value for that sprite
					spriteTypeRewardFactor.put(spriteTypeId, new Pair<Double,Integer>(newScore,rewardEntry.second+1));
				}
			}
		} else {
		// If game is not over, then also check for healthpoint loss
			int maxHealth = currentState.getAvatarMaxHealthPoints();
			if (maxHealth > 0) {
				double healthDiff = currentState.getAvatarHealthPoints() - state_t_minus_1.getAvatarHealthPoints();
				// Scale healthDiff into range of 0 and 1
				healthDiff /= maxHealth;
				scoreDiff += healthDiff;
				scoreDiff *= 0.5;
			}
			
			// Enter final location only into positionHistory if it's not the one from gameover state
			currentPosition = currentState.getAvatarPosition();
			positionHistory[lastIndex] = currentPosition.copy();
		}
		
		while(!eventHistory.isEmpty()) 
		{
			event = eventHistory.pollLast();
			currentFrame = event.gameStep;
			
			if(currentFrame >= lastFrame) {
				continue;
			}
			// Stop when we reached the very first step of the simulated action sequence
			if(currentFrame <= startFrame + 1) {
				break;
			}
			// If we went one tick back in time, then update the currentState and scoreDifference.
			if (currentFrame < previousFrame) {
				currentStateIndex--;
				t--;
				currentState = stateHistory[currentStateIndex];
				currentPosition = currentState.getAvatarPosition();
				positionHistory[t] = currentPosition.copy();
				if (currentStateIndex == 0) {
					int a = 0;
				}
				state_t_minus_1 = stateHistory[currentStateIndex-1];
				scoreDiff = currentState.getGameScore() - state_t_minus_1.getGameScore();
				
				Utils.normalise(scoreDiff, -1.0, 1.0);				

				// If game is not over, then also check for healthpoint loss
				if (!currentState.isGameOver()) {
					int maxHealth = currentState.getAvatarMaxHealthPoints();
					if (maxHealth > 0) {
						double healthDiff = currentState.getAvatarHealthPoints() - state_t_minus_1.getAvatarHealthPoints();
						// Scale healthDiff into range of 0 and 1
						healthDiff /= maxHealth;
						scoreDiff += healthDiff;
						scoreDiff *= 0.5;
					}
				}
			}
			
			int spriteTypeId = event.passiveTypeId;
			rewardEntry = spriteTypeRewardFactor.getOrDefault(spriteTypeId, new Pair<Double,Integer>(0.0,0));
			
			double newScore = (0.8*rewardEntry.first + 0.2*scoreDiff);	// Update the reward value for that sprite
			spriteTypeRewardFactor.put(spriteTypeId, new Pair<Double,Integer>(newScore,rewardEntry.second+1));

			// Test whether sprite can be moved/destroyed/etc (if avatar is at the collission event's location
			// at some point in the future, then we assume that the sprite has moved, was destroyed, etc
			boolean positionReachable = false;
			Vector2d colliderPosition = getSpritePosition(event.passiveSpriteId,event.passiveTypeId,currentState);
			//// System.out.println(colliderPosition);
			for (int i = t; i <= lastIndex; i++) {
				if (colliderPosition.equals(positionHistory[i])) {
					positionReachable = true;
					break;
				}
			}
			if(positionReachable) {
				// Avatar managed to reach the location where the sprite used to be -> it's not an obstacle
				isSpriteTypeObstacle.put(spriteTypeId, false);
			}

			// Set the frame counter of this iteration
			previousFrame = currentFrame;
		}
		
		if (gameOverCausingSpriteTypeIds.size() != 0) {
			for (int iType : gameOverCausingSpriteTypeIds) {
				boolean positionReachable = false;
				for (int i = t; i <= lastIndex; i++) {
					if (gameOverLocation.equals(positionHistory[i])) {
						positionReachable = true;
						break;
					}
				}
				if(positionReachable) {
					// Avatar managed to reach the location where the sprite used to be -> it's not an obstacle
					isSpriteTypeObstacle.put(iType, false);
				}
			}
		}
		
		// All the locations we have visited during the rollout are reachable and are now compared
		// with what was there during the first frame. All sprites on our journey are therefore non-obstacles
		currentState = stateHistory[0];
		ArrayList<Observation>[][] observationGrid = currentState.getObservationGrid();
		for (int i = 0; i <= lastIndex; i++) {
			if (positionHistory[i] == null)
				continue;
			int blockSize = currentState.getBlockSize();
			int x = (int)positionHistory[i].x / blockSize;
			int y = (int)positionHistory[i].y / blockSize;
			if(x >= 0 && x < observationGrid.length && y >= 0 && y < observationGrid[0].length) {
				deathMap[x][y].nofVisits++;
				for (Observation o : observationGrid[x][y]) {
					isSpriteTypeObstacle.put(o.itype, false);
				}
			}
		}
	}
	
	
	Vector2d getSpritePosition(int spriteId, int spriteTypeId, StateObservation currentState) {
		
		Vector2d currentPosition = currentState.getAvatarPosition();
		int n = 0;
		ArrayList<Observation>[] resourceSpritePositions = currentState.getResourcesPositions(currentPosition);
		if (resourceSpritePositions != null) {
			n = resourceSpritePositions.length;
			for (int i = 0; i < n; i++) {
				for(Observation o : resourceSpritePositions[i]) {
					if (o.itype != spriteTypeId)
						break;
					
					if (o.obsID == spriteId) {
						return o.position;
					}
				}
			}
		}
		ArrayList<Observation>[] portalPositions = currentState.getPortalsPositions(currentPosition);
		if(portalPositions != null) {
			n = portalPositions.length;
			for (int i = 0; i < n; i++) {
				for(Observation o : portalPositions[i]) {
					if (o.itype != spriteTypeId)
						break;
					
					if (o.obsID == spriteId) {
						return o.position;
					}
				}
			}
		}
		ArrayList<Observation>[] NPCSpritePositions = currentState.getNPCPositions(currentPosition);
		if (NPCSpritePositions != null) {
			n = NPCSpritePositions.length;
			for (int i = 0; i < n; i++) {
				for(Observation o : NPCSpritePositions[i]) {
					if (o.itype != spriteTypeId)
						break;
					
					if (o.obsID == spriteId) {
						return o.position;
					}
				}
			}
		}
		ArrayList<Observation>[] movableSpritePositions = currentState.getMovablePositions(currentPosition);
		if (movableSpritePositions != null) {
			n = movableSpritePositions.length;
			for (int i = 0; i < n; i++) {
				for(Observation o : movableSpritePositions[i]) {
					if (o.itype != spriteTypeId)
						break;
					
					if (o.obsID == spriteId) {
						return o.position;
					}
				}
			}
		}
		ArrayList<Observation>[] immmovableSpritePositions = currentState.getImmovablePositions(currentPosition);
		if (immmovableSpritePositions != null) {
			n = immmovableSpritePositions.length;
			for (int i = 0; i < n; i++) {
				for(Observation o : immmovableSpritePositions[i]) {
					if (o.itype != spriteTypeId)
						break;
					
					if (o.obsID == spriteId) {
						return o.position;
					}
				}
			}
		}
		ArrayList<Observation>[] fromAvatarSpritePositions = currentState.getFromAvatarSpritesPositions(currentPosition);
		if (fromAvatarSpritePositions != null) {
			n = fromAvatarSpritePositions.length;
			for (int i = 0; i < n; i++) {
				for(Observation o : fromAvatarSpritePositions[i]) {
					if (o.itype != spriteTypeId)
						break;
					
					if (o.obsID == spriteId) {
						return o.position;
					}
				}
			}
		}
		return new Vector2d(-1,-1);
	}
	

	/**
	 * Returns an object, describing a sprite, which the avatar should investigate
	 * @param currentState
	 */
	PathfindHeuristic getTargetSprite(StateObservation currentState, ElapsedCpuTimer elapsedTimer) 
	{
		Vector2d currentPosition = currentState.getAvatarPosition();

		// Test whether we already have a pathfinder and if so, whether the avatar has already done too many steps (i.e. there
		// seems to be something wrong with the pathfinder path to ther goal location -> recalculate path)
		if (currentPathfinder != null && currentPathfinder.goalReached == false && currentPathfinder.totalPathLength*1.2 > currentPathfinderFrameCount)
		{
			// If we already have a pathfinder and the avatar has not yet reached it, then return that one
			currentPathfinderFrameCount++;
			return currentPathfinder;
		}
		else {
			currentPathfinder = null;
		}
		
		if (currentSearchProblemId != -1) {
			// Continue the search from the last iteration
			boolean solved = aStarSearch.solveShortestPathProblem(currentSearchProblemId, elapsedTimer, 4);
			
			// Check whether there was no solution or whether we ran out of time 
			if (!solved && aStarSearch.isUnsolvable(currentSearchProblemId)) {
				currentSearchProblemId = -1;
				return null;
			} else if (solved) {
				// If A-Star found a path this time, then return the pathfinder for it
				// System.out.println(isSpriteTypeObstacle);
				// System.out.println("Shortest path found");
				// System.out.println("current avatar position = "+currentState.getAvatarPosition());
				// System.out.println("Flag locations : " + aStarSearch.getShortestPathFlagLocations(currentSearchProblemId));
				PathfindHeuristic pathfinder = new PathfindHeuristic(aStarSearch.getShortestPathFlagLocations(currentSearchProblemId),aStarSearch.getShortestPathLocationFlagDistances(currentSearchProblemId),currentState.getBlockSize(),currentState.getWorldDimension());
				// System.out.println("Path length = " + pathfinder.totalPathLength);
				currentPathfinder = pathfinder;
				pathfinders.add(pathfinder);
				currentPathfinderFrameCount = 0;
				currentSearchProblemId = -1;
				return pathfinder;
			}
		}
		
		HashMap<Integer,Observation> candidates = new HashMap<Integer,Observation>();
		HashMap<Integer,Double> candidateRewards = new HashMap<Integer,Double>();
		ArrayList<Integer> candidateIndices = new ArrayList<Integer>();
		
		double rewardFactor;
		int spriteTypeId;
		int n = 0;
		Observation targetObservation = null;
		
		ArrayList<Observation>[] resourceSpritePositions = currentState.getResourcesPositions(currentPosition);
		if (resourceSpritePositions != null) {
			n = resourceSpritePositions.length;
			//// System.out.println(n + "resources = " + resourceSpritePositions[0].get(0).category);
			for (int i = 0; i < n; i++) {
				for(Observation o : resourceSpritePositions[i]) {
					if (currentPosition.equals(o.position))
						continue;
					spriteTypeId = o.itype;
					rewardFactor = spriteTypeRewardFactor.getOrDefault(spriteTypeId, new Pair<Double,Integer>(0.0,0)).first;
					if (rewardFactor > candidateRewards.getOrDefault(spriteTypeId, -100000.0)) {
						candidateRewards.put(o.category,rewardFactor);
						candidates.put(o.category, o);
						candidateIndices.add(o.category);
					}
				}
			}
		}
		ArrayList<Observation>[] portalPositions = currentState.getPortalsPositions(currentPosition);
		if(portalPositions != null) {
			n = portalPositions.length;
			//// System.out.println(n + "Portals = " + portalPositions[0].get(0).position);
			for (int i = 0; i < n; i++) {
				for(Observation o : portalPositions[i]) {
					if (currentPosition.equals(o.position))
						continue;
					spriteTypeId = o.itype;
					rewardFactor = spriteTypeRewardFactor.getOrDefault(spriteTypeId, new Pair<Double,Integer>(0.0,0)).first;
					if (rewardFactor > candidateRewards.getOrDefault(spriteTypeId, -100000.0)) {
						candidateRewards.put(o.category,rewardFactor);
						candidates.put(o.category, o);
						candidateIndices.add(o.category);
					}
				}
			}
		}
	/*	ArrayList<Observation>[] NPCSpritePositions = currentState.getNPCPositions(currentPosition);
		if (NPCSpritePositions != null) {
			n = NPCSpritePositions.length;
			//// System.out.println(n + "NPCs = " + NPCSpritePositions[0].get(0).category);
			for (int i = 0; i < n; i++) {
				for(Observation o : NPCSpritePositions[i]) {
					if (currentPosition.equals(o.position))
						continue;
					spriteTypeId = o.itype;
					rewardFactor = spriteTypeRewardFactor.getOrDefault(spriteTypeId, new Pair<Double,Integer>(0.0,0)).first;
					if (rewardFactor > maxRewardFactor) {
						maxRewardFactor = rewardFactor;
						targetObservation = o;
					}
				}
			}
		}*/
		ArrayList<Observation>[] movableSpritePositions = currentState.getMovablePositions(currentPosition);
		if (movableSpritePositions != null) {
			n = movableSpritePositions.length;
			//// System.out.println(n + "movables = " + movableSpritePositions[0].get(0).category);
			for (int i = 0; i < n; i++) {
				//// System.out.println(movableSpritePositions[i].size() + " movables of type " + movableSpritePositions[i].get(0).itype);
				/*for(Observation o : movableSpritePositions[i]) {
					if (currentPosition.equals(o.position))
						continue;
					spriteTypeId = o.itype;
					rewardFactor = spriteTypeRewardFactor.getOrDefault(spriteTypeId, new Pair<Double,Integer>(0.0,0)).first;
					if (rewardFactor > maxRewardFactor) {
						maxRewardFactor = rewardFactor;
						targetObservation = o;
					}
				}*/
			}
		}
		ArrayList<Observation>[] immmovableSpritePositions = currentState.getImmovablePositions(currentPosition);
		if (immmovableSpritePositions != null) {
			n = immmovableSpritePositions.length;
			//// System.out.println(n + "immovables = " + immmovableSpritePositions[0].get(0).category);
			for (int i = 0; i < n; i++) {
				//// System.out.println(immmovableSpritePositions[i].size() + " immovables of type " + immmovableSpritePositions[i].get(0).itype);
				/*for(Observation o : immmovableSpritePositions[i]) {
					if (currentPosition.equals(o.position))
						continue;
					spriteTypeId = o.itype;
					rewardFactor = spriteTypeRewardFactor.getOrDefault(spriteTypeId, new Pair<Double,Integer>(0.0,0)).first;
					if (rewardFactor > maxRewardFactor) {
						maxRewardFactor = rewardFactor;
						targetObservation = o;
					}
				}*/
			}
		}
		
		
		//// System.out.println("Remaining time after looking at all sprites: " + elapsedTimer.remainingTimeMillis());

		boolean solved = false;
		//while(!solved && elapsedTimer.elapsedMillis() < 3) {
		if (candidateIndices.size() != 0) {
			int idx = rnd.nextInt(candidateIndices.size());
			targetObservation = candidates.get(candidateIndices.get(idx));
			// System.out.println("Trying to find path to " + targetObservation.position);
			currentSearchProblemId = aStarSearch.createShortestPathProblem(currentState, targetObservation.position, isSpriteTypeObstacle);
			solved = aStarSearch.solveShortestPathProblem(currentSearchProblemId, elapsedTimer, 3);
			
			// Check whether there was no solution or whether we ran out of time 
			if (!solved && aStarSearch.isUnsolvable(currentSearchProblemId)) {
				currentSearchProblemId = -1;
			}
		}
		//}
			// System.out.println(isSpriteTypeObstacle);
		if (solved) {
			// System.out.println(isSpriteTypeObstacle);
			// System.out.println("Shortest path found");
			// System.out.println("sprite category id = "+targetObservation.category);
			// System.out.println("current avatar position = "+currentState.getAvatarPosition());
			// System.out.println("target position = "+targetObservation.position);
			// System.out.println("Flag locations : " + aStarSearch.getShortestPathFlagLocations(currentSearchProblemId));
			PathfindHeuristic pathfinder = new PathfindHeuristic(aStarSearch.getShortestPathFlagLocations(currentSearchProblemId),aStarSearch.getShortestPathLocationFlagDistances(currentSearchProblemId),currentState.getBlockSize(),currentState.getWorldDimension());
			// System.out.println("Path length = " + pathfinder.totalPathLength);
			currentPathfinder = pathfinder;
			pathfinders.add(pathfinder);
			currentPathfinderFrameCount = 0;
			return pathfinder;
		}
		
		//// System.out.println("Remaining time after aStar: " + elapsedTimer.remainingTimeMillis());
		
		return null;
		
	}
	
	double getSpriteTypeRewardFactor(StateObservation currentState, int spriteTypeId) {
		// Returns the reward factor for the specified sprite. If the sprite is unknown, then return a small
		// positive value, which encourages exploration
		return spriteTypeRewardFactor.getOrDefault(spriteTypeId,new Pair<Double,Integer>(0.5,0)).first;
	}
	
	boolean isSpriteTypeObstacle(int spriteTypeId) {
		return isSpriteTypeObstacle.getOrDefault(spriteTypeId, true);
	}

}
