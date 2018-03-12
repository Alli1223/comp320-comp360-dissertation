package nereus248;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.Pair;
import tools.Vector2d;

public class AStarShortestPathProblem {

	public Vector2d startLocation;
	public int startLocation_x;
	public int startLocation_y;
	public Pair<Integer,Integer> startLocationI;
	private double intToDoubleCorrection_x;
	private double intToDoubleCorrection_y;
	
	public Vector2d goalLocation;
	public int goalLocation_x;
	public int goalLocation_y;
	public int goalLocationHash;
	public Pair<Integer,Integer> goalLocationI;
	
	public boolean[][] visited;
	public Float[][]shortestPathMapDistances;
	public ShortestPathDirection[][] shortestPathMapDirections;
	public PriorityQueue<OpenSetItem> openSet;
	
	public boolean is1D;
	public boolean solved;
	public boolean unreachable;
	public boolean pathConstructed;

	public int stepSize;
	public int worldWidth;
	public int worldHeight;
	public HashMap<Integer,Boolean> obstacleITypes;
	public ArrayList<Observation>[][] gameMap;
	private ArrayList<ACTIONS> availableActions;
	
	// Stores 2D locations along the map, which the agent can follow to reach the goal location
	public ArrayList<Vector2d> shortestPathFlagLocations;
	// Stores the min number of steps required from one of the flags to the goal location
	public ArrayList<Integer> shortestPathFlagDistances;
	public boolean[][] shortestPathBooleanMap;

	
	public AStarShortestPathProblem(StateObservation initialState, Vector2d goalLocation, HashMap<Integer,Boolean> obstacleTypes, AStarStateComparator stateComparator) {
		
		Dimension worldDimension = initialState.getWorldDimension();
		stepSize = initialState.getBlockSize();
		worldWidth = worldDimension.width/stepSize;
		worldHeight = worldDimension.height/stepSize;
		
		startLocation = initialState.getAvatarPosition();
		this.startLocation_x = (int)Math.floor(startLocation.x/stepSize);
		this.startLocation_y = (int)Math.floor(startLocation.y/stepSize);
		this.startLocationI = new Pair<Integer,Integer>(startLocation_x,startLocation_y);
		intToDoubleCorrection_x = startLocation_x*stepSize - startLocation.x;
		intToDoubleCorrection_y = startLocation_y*stepSize - startLocation.y;
		
		this.goalLocation = goalLocation;
		this.goalLocation_x = (int)Math.floor(goalLocation.x/stepSize);
		this.goalLocation_y = (int)Math.floor(goalLocation.y/stepSize);
		this.goalLocationI = new Pair<Integer,Integer>(goalLocation_x,goalLocation_y);
		this.goalLocationHash = worldWidth*goalLocation_y + goalLocation_x;

		// Figure out whether we can move in 2 Dimensions or only 1
		availableActions = new ArrayList<ACTIONS>();
		ArrayList<ACTIONS> availableActionsTemp = initialState.getAvailableActions(false);
		for (int i = 0; i < availableActionsTemp.size(); i++) {
			if (availableActionsTemp.get(i) == ACTIONS.ACTION_DOWN) {
				availableActions.add(ACTIONS.ACTION_DOWN);
			} else if (availableActionsTemp.get(i) == ACTIONS.ACTION_UP) {
				availableActions.add(ACTIONS.ACTION_UP);
			} else if (availableActionsTemp.get(i) == ACTIONS.ACTION_LEFT) {
				availableActions.add(ACTIONS.ACTION_LEFT);
			} else if (availableActionsTemp.get(i) == ACTIONS.ACTION_RIGHT) {
				availableActions.add(ACTIONS.ACTION_RIGHT);
			}
		}
		if(availableActions.size() < 3) {
			is1D = true;
		}
		
		this.obstacleITypes = obstacleTypes;
		this.gameMap = initialState.getObservationGrid();
		
		visited = new boolean[worldWidth][worldHeight];
		openSet = new PriorityQueue<OpenSetItem>(stateComparator);
		shortestPathMapDirections = new ShortestPathDirection[worldWidth][worldHeight];
		shortestPathMapDistances = new Float[worldWidth][worldHeight];
		
		// Add initial state to the priority queue
		Random rnd = new Random();
		
		ACTIONS firstAction = availableActions.get(rnd.nextInt((int)availableActions.size()));
		
		openSet.offer(new OpenSetItem(startLocation_x,startLocation_y,0.0f,0.0f,firstAction));
		
		solved = false;
		unreachable = false;
		pathConstructed = false;
	}
	
	public void calculatePath() {
		if(shortestPathMapDirections[goalLocation_x][goalLocation_y] == null) {
			return;
		}
		
		shortestPathFlagLocations = new ArrayList<Vector2d>();
		shortestPathFlagDistances = new ArrayList<Integer>();
		shortestPathBooleanMap = new boolean[worldWidth][worldHeight];
		double halfStepSize = stepSize*0.5;
		
		int x = goalLocation_x;
		int y = goalLocation_y;
		shortestPathBooleanMap[x][y] = true;
		
		int prevDir_x = 0;
		int prevDir_y = 0;
		int num_steps = 0;
		
		ArrayList<ACTIONS> actions = new ArrayList<ACTIONS>();
		
		while(!pathConstructed) {
			
			ShortestPathDirection direction = shortestPathMapDirections[x][y];
			if (direction == null) {
				int a = 0;
			}
			// As soon as we have a direction change, set an orientation flag
			if (direction.reverseDirection_x != prevDir_x || direction.reverseDirection_y != prevDir_y){
				shortestPathFlagLocations.add(new Vector2d(x*stepSize - intToDoubleCorrection_x, y*stepSize - intToDoubleCorrection_y));
				shortestPathFlagDistances.add(num_steps);
			}
			// Take a step back along the path from goal to initial state
			x += direction.reverseDirection_x;
			y += direction.reverseDirection_y;
			shortestPathBooleanMap[x][y] = true;
			
			actions.add(direction.action);
			
			prevDir_x = direction.reverseDirection_x;
			prevDir_y = direction.reverseDirection_y;
			num_steps++;
			
			
			// When we arrived at the initial state, set the last flag and terminate iteration
			if(x == startLocation_x && y == startLocation_y) {
				shortestPathFlagLocations.add(new Vector2d(x*stepSize - intToDoubleCorrection_x, y*stepSize - intToDoubleCorrection_y));
				shortestPathFlagDistances.add(num_steps);
				
				Collections.reverse(shortestPathFlagLocations);
				Collections.reverse(shortestPathFlagDistances);
				
				pathConstructed = true;
			}
		}
		Collections.reverse(actions);
		// System.out.println(actions);
	}
}
