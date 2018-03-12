package nereus248;

import java.util.ArrayList;
import java.util.HashMap;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Pair;
import tools.Vector2d;

public class AStar {

	double stepSize;
	double oneOverStepSize;
	int problemCounter;

	AStarStateComparator stateComparator;
	
	AStarShortestPathProblem currentProblem;
	
	
	HashMap<Integer,Integer> shortestPathProblemGoalStateMapping;
	ArrayList<AStarShortestPathProblem> shortestPathProblems;
	ArrayList<Integer> obstacleTypes;
	
	boolean is1D;

	public AStar(StateObservation someState) {
		
		this.stateComparator = new AStarStateComparator();
		this.shortestPathProblems = new ArrayList<AStarShortestPathProblem>();
		this.shortestPathProblemGoalStateMapping = new HashMap<Integer,Integer>();
		
		problemCounter = 0;
	}
	
	public void removeObstacleType(int obstacleType) {
		obstacleTypes.remove(obstacleType);
	}
	
	public void addObstacleType(int obstacleType) {
		if(!obstacleTypes.contains(obstacleType)) {
			obstacleTypes.add(obstacleType);
		}
	}
	
	public int createShortestPathProblem(StateObservation initialState, Vector2d goalLocation, HashMap<Integer,Boolean> obstacleTypes) 
	{
		shortestPathProblems.add(new AStarShortestPathProblem(initialState,goalLocation,obstacleTypes,stateComparator));
		shortestPathProblemGoalStateMapping.put(shortestPathProblems.get(problemCounter).goalLocationHash, problemCounter);
		return problemCounter++;
	}
	
	/**
	 * Return true if shortest path was found after the given time and false otherwise
	 * @param problem
	 * @param elapsedTimer
	 * @param allowedMilliseconds
	 * @return
	 */
	public boolean solveShortestPathProblem(int problemIndex, ElapsedCpuTimer elapsedTimer, double allowedMilliseconds) {
		
		int numIters = 0;
		currentProblem = shortestPathProblems.get(problemIndex);
		if (currentProblem == null) {
			return false;
		}

		if (currentProblem.openSet.isEmpty()) {
			return false;
		}
		
		if(currentProblem.openSet.peek().x == currentProblem.goalLocation_x && currentProblem.openSet.peek().y == currentProblem.goalLocation_y) {
			// If we already are there, then just return false
			currentProblem.unreachable = true;
			return false;
		}
		
		// If it has already been solved in the past, don't do any calculations
		if (currentProblem.solved && currentProblem.pathConstructed) {
			return true;
		}
		
		float minHScore = Float.MAX_VALUE;
		float bonus = -0.5f/(currentProblem.worldWidth*currentProblem.worldHeight);
		double remainingTime = elapsedTimer.remainingTimeMillis();
		double targetTime = remainingTime - allowedMilliseconds + 0.5;
		
		while(!currentProblem.openSet.isEmpty()) {

			OpenSetItem currentState = currentProblem.openSet.poll();
			int x = currentState.x;
			int y = currentState.y;
			
			/* Since the openSet can contain multiple entries of the same location, we have to perform this test */
			if(currentProblem.shortestPathMapDirections[x][y] != null && currentProblem.shortestPathMapDirections[x][y].gScore < currentState.gScore) {
				continue;
			}
			
			if (x == currentProblem.goalLocation_x && y == currentProblem.goalLocation_y) {
				// Goal found
				// System.out.println("Calculating path to location " + currentProblem.goalLocation_x + "," + currentProblem.goalLocation_y);
				// If we already are there
				if (currentProblem.shortestPathMapDirections[x][y] == null) {
					currentProblem.shortestPathMapDirections[x][y] = new ShortestPathDirection(ACTIONS.ACTION_NIL,0,0,0);
				}
				currentProblem.solved = true;
				currentProblem.calculatePath();
				return true;
			}
			
			ACTIONS action;
			ArrayList<Pair<Integer,Integer>> availableNeighbours = getNeighbours(x,y);
			
			// Loop over all possible actions at the current location
			for(Pair<Integer,Integer> nextLocation : availableNeighbours) {
				
				int next_x = nextLocation.first;
				int next_y = nextLocation.second;
				// If we have been here before, then it was over a faster path -> Discard this neighbour.
				if(currentProblem.shortestPathMapDirections[next_x][next_y] != null && currentProblem.shortestPathMapDirections[next_x][next_y].gScore < currentState.gScore) {
					continue;
				}

				// Calculate the action used and the reverse step necessary to backtrack once the goal state is reached
				int rev_x = 0;
				int rev_y = 0;
				
				if( next_x > x) {
					action = ACTIONS.ACTION_RIGHT; 
					rev_x = -1;
				} else if (next_x < x) {
					action = ACTIONS.ACTION_LEFT;
					rev_x = 1;
				} else if(next_y > y) {
					action = ACTIONS.ACTION_DOWN;	// the higher y, the lower at the bottom of the screen we are
					rev_y = -1;
				} else {
					action = ACTIONS.ACTION_UP;
					rev_y = 1;
				}
				
				
				// Calculate heuristic
				float h_score = heuristic(next_x,next_y, currentProblem.goalLocation_x, currentProblem.goalLocation_y);
				minHScore = Math.min(h_score, minHScore);
				// Try to guide the search as long as possible in the same direction (no zig-zag). Therefore we subtract
				// a little bit when the action is the same as in the previous step
				float g_score = currentState.gScore + 1;
				float g_score_bonus = (action == currentState.previousAction) ? bonus : 0.0f;
				g_score += g_score_bonus;
				float f_score = h_score + g_score + g_score_bonus;
				
				// Store the shortest path to location [next_x][next_y]
				currentProblem.shortestPathMapDirections[next_x][next_y] = new ShortestPathDirection(action,rev_x,rev_y,g_score);
				// Insert next step into the open set
				currentProblem.openSet.offer(new OpenSetItem(next_x,next_y,f_score,g_score,action));
			}

			// Keep track of the time
			numIters++;

        	// Return in time
			if (elapsedTimer.remainingTimeMillis() <= targetTime){
				// System.out.println("AStar out of time. Has performed " + numIters + "iterations");
				currentProblem.solved = false;
				return false;
			}
		}
		
		// There doesn't seem to be a way leading there
		currentProblem.unreachable = true;
		currentProblem.solved = false;
		// System.out.println("AStar problem unsolvable. MinHScore = "  + minHScore);
		//shortestPathProblems.set(problemIndex, null);
		return false;
	}
	
	/**
	 * Returns the orientation flags towards the goal and null if the problem has not been solved yet.
	 * @param problemIndex
	 * @return check for null!
	 */
	public ArrayList<Vector2d> getShortestPathFlagLocations(int problemIndex) {
		if(shortestPathProblems.get(problemIndex).pathConstructed) {
			return shortestPathProblems.get(problemIndex).shortestPathFlagLocations;
		}
		return null;
	}
	
	public ArrayList<Integer> getShortestPathLocationFlagDistances(int problemIndex) {
		if(shortestPathProblems.get(problemIndex).pathConstructed) {
			return shortestPathProblems.get(problemIndex).shortestPathFlagDistances;
		}
		return null;
	}
	
	public boolean isUnsolvable(int problemId) {
		if (problemId >= 0 && problemId < shortestPathProblems.size()) {
			return shortestPathProblems.get(problemId).unreachable;
		}
		return true;
	}
	
	/**
	 * Calculates the Manhattan distance between two points on the map
	 * @param currentLocation
	 * @param goalLocation
	 * @return
	 */
	private float heuristic(int x1, int y1, int x2, int y2) {
		return (float)(Math.abs(x1 - x2) + Math.abs(y1-y2));
	}
	
	/**
	 * Returns all reachable neighbours from the current location
	 * @param currentLocation
	 * @return
	 */
	private ArrayList<Pair<Integer,Integer>> getNeighbours(int currentX, int currentY) {
		ArrayList<Pair<Integer,Integer>> result = new ArrayList<Pair<Integer,Integer>>();
		if(is1D) {
			if(!isObstacle(currentX+1, currentY)) {
				result.add(new Pair<Integer,Integer>(currentX+1,currentY));
			}
			if(!isObstacle(currentX-1, currentY)) {
				result.add(new Pair<Integer,Integer>(currentX-1,currentY));
			}
		} else {
			if(!isObstacle(currentX+1, currentY)) {
				result.add(new Pair<Integer,Integer>(currentX+1,currentY));
			}
			if(!isObstacle(currentX-1, currentY)) {
				result.add(new Pair<Integer,Integer>(currentX-1,currentY));
			}
			if(!isObstacle(currentX, currentY+1)) {
				result.add(new Pair<Integer,Integer>(currentX,currentY+1));
			}
			if(!isObstacle(currentX, currentY-1)) {
				result.add(new Pair<Integer,Integer>(currentX,currentY-1));
			}
		}
		return result;
	}
	
	/**
	 * Returns whether the current location is an obstacle that cannot be penetrated.
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isObstacle(int x, int y)
    {
		// Maybe the goal location is still stored as obstacle sprite
		if(x==currentProblem.goalLocation_x && y==currentProblem.goalLocation_y) {
			return false;
		}
		// If outside the map vertically
        if(x < 0 || x >= currentProblem.gameMap.length) {
        	return true;
        }
        // If outside the map horizontally
        if(y < 0 || y >= currentProblem.gameMap[x].length) {
        	return true;
        }
        // Check whether there is an obstacle that cannot be penetrated
        for(Observation obs : currentProblem.gameMap[x][y])
        {
            if(currentProblem.obstacleITypes.getOrDefault(obs.itype,true)) {
                return true;
            }
        }
        return false;
    }
}



