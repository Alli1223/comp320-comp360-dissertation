package nereus248;

import java.awt.Dimension;
import java.util.ArrayList;

import tools.Vector2d;

public class PathfindHeuristic {
	
	int nofIts;
	
	int numFlags;
	int blockSize;
	double stepSizeHalf;
	double stepSizeHalfSquared;
	int totalPathLength;
	int previousFlagIndex = 0;
	int nextFlagIndex = 1;
	int overnextFlagIndex = 2;
	private ArrayList<Integer> pathFlagDistances;
	private ArrayList<Vector2d> pathFlagLocations;
	
	private Dimension worldDimension;
	private double distanceNormalizationFactor;
	
	boolean goalReached;
		
	public PathfindHeuristic(ArrayList<Vector2d> pathFlagLocations, ArrayList<Integer> pathFlagDistances, int blockSize, Dimension worldDimension) 
	{
		nofIts = 0;
		goalReached = false;
		
		// Create a copy of the flag locations and distances
		this.pathFlagDistances = new ArrayList<Integer>(pathFlagDistances);
		this.pathFlagLocations = new ArrayList<Vector2d>(pathFlagLocations);;
		numFlags = this.pathFlagLocations.size();

		totalPathLength = this.pathFlagDistances.get(0);
		
		// Get world parameters
		this.blockSize = blockSize;
		this.stepSizeHalf = blockSize*0.5;
		this.stepSizeHalfSquared = blockSize*blockSize*0.25;
		this.distanceNormalizationFactor = 1.0/totalPathLength;//((worldDimension.width + worldDimension.height)/this.blockSize);
		this.worldDimension = worldDimension;
		
		// Initialize for pathfinding
		previousFlagIndex = 0;
		nextFlagIndex = 1;
		overnextFlagIndex = Math.min(2, pathFlagLocations.size()-1);
	}
	
	/**
	 * Calculates the new position describing flag locations according to the current position. Assumes that this
	 * function is called every time the avatar performs a step.
	 * @param currentLocation
	 */
	public void step(Vector2d currentLocation) 
	{
		nofIts++;
		
		if(goalReached) {
			return;
		}
		
		if (currentLocation.sqDist(pathFlagLocations.get(numFlags-1)) < stepSizeHalfSquared) {
			//// System.out.println("Goal flag reached");
			goalReached = true;
			return;
		}
		
		Vector2d previousFlagLocation = pathFlagLocations.get(previousFlagIndex);
		Vector2d nextFlagLocation = pathFlagLocations.get(nextFlagIndex);
		Vector2d overnextFlagLocation = pathFlagLocations.get(overnextFlagIndex);
		Vector2d nextFlagToOvernextFlagDirection = overnextFlagLocation.copy();
		nextFlagToOvernextFlagDirection = nextFlagToOvernextFlagDirection.subtract(nextFlagLocation);
				
		// Test whether we have already reached the next flag, resp. whether we are already on the steps 
		// towards the overnext flag
		boolean nextSegment = false;
		double currentToOvernextDistance = overnextFlagLocation.sqDist(currentLocation);
		double nextToOvernextDistance = overnextFlagLocation.sqDist(nextFlagLocation);
		if (currentToOvernextDistance < nextToOvernextDistance) {
			// If it's a horizontal path segment...
			if (Math.abs(nextFlagToOvernextFlagDirection.x) > Math.abs(nextFlagToOvernextFlagDirection.y)) {
				if (Math.abs(currentLocation.y - overnextFlagLocation.y) < stepSizeHalf)
					nextSegment = true;
			} else { // else if it's a vertical path segment
				if (Math.abs(currentLocation.x - overnextFlagLocation.x) < stepSizeHalf)
					nextSegment = true;
			}
		}

		double currentToNextDistance = currentLocation.sqDist(nextFlagLocation);
		double previousToNextDistance = nextFlagLocation.sqDist(previousFlagLocation);
		if(currentToNextDistance < stepSizeHalfSquared || nextSegment) {
			// We are in the next segment -> update active flags
			//// System.out.println("Flag reached: at avatar location " + currentLocation + " is flag " + nextFlagLocation);
			previousFlagIndex = nextFlagIndex;
			nextFlagIndex = overnextFlagIndex;
			overnextFlagIndex = Math.min(overnextFlagIndex+1, numFlags-1);
		} else if (currentToNextDistance > previousToNextDistance) {
			// We went back and are now in the previous segment -> update active flags
			//// System.out.println("Step back in pathfinder.");
			previousFlagIndex = Math.max(0,previousFlagIndex - 1);
			nextFlagIndex = previousFlagIndex + 1;
			overnextFlagIndex = Math.min(nextFlagIndex + 1, numFlags-1);
		}
	}
	
	/**
	 * Returns the heuristic value for the current location according to the progress along the 
	 * shortest path. Will also update the internal state of the pathfinder.
	 * TODO: Maybe: Would be more efficient when exchanging sqDistance with Manhattan distance...
	 * @param currentLocation
	 * @return
	 */
	public double heuristic(Vector2d currentLocation) 
	{
		// If we reached the goal, then this pathfinder is not to use anymore
		if(goalReached) {
			return 0;
		}
		
		// Get flag locations
		Vector2d previousFlagLocation = pathFlagLocations.get(previousFlagIndex);
		Vector2d nextFlagLocation = pathFlagLocations.get(nextFlagIndex);
		Vector2d overnextFlagLocation = pathFlagLocations.get(overnextFlagIndex);
		
		// Get step distances
		int previousFlagStepDistance = pathFlagDistances.get(previousFlagIndex);
		int previousToNextStepDistance = previousFlagStepDistance;
		
		// Get real world euclidean distances
		double currentToNextDistance = currentLocation.sqDist(nextFlagLocation);
		double previousToNextDistance = nextFlagLocation.sqDist(previousFlagLocation);
		
		// If we are in the last segment, then simplify the evaluation
		if (overnextFlagIndex == nextFlagIndex) {
			// Calculate heursitic value
			double val = previousToNextStepDistance*(currentToNextDistance/previousToNextDistance);
			return -val*distanceNormalizationFactor;
		}
		
		// Get more real world distances
		double currentToOvernextDistance = overnextFlagLocation.sqDist(currentLocation);
		double nextToOvernextDistance = overnextFlagLocation.sqDist(nextFlagLocation);
		
		// Get for the flag behind the avatar and the one ahead of the avatar 
		// how far away they are from the eventual goal (in steps)
		int nextFlagStepDistance = pathFlagDistances.get(nextFlagIndex);
		int overnextFlagStepDistance = pathFlagDistances.get(overnextFlagIndex);
		int nextToOvernextStepDistance = nextFlagStepDistance - overnextFlagStepDistance;

		// Calculate heuristic value
		double val = 0.8*previousToNextStepDistance*(currentToNextDistance/previousToNextDistance) + nextFlagStepDistance;
		val += 0.2*nextToOvernextStepDistance*(currentToOvernextDistance/nextToOvernextDistance) + overnextFlagStepDistance;
		// Return negative distance to goal
		return -val*distanceNormalizationFactor;
	}
	
	/**
	 * Returns an exact copy of the current object
	 * @return
	 */
	public PathfindHeuristic copy() {
		PathfindHeuristic clone = new PathfindHeuristic(pathFlagLocations, pathFlagDistances, blockSize, worldDimension);
		clone.previousFlagIndex = this.previousFlagIndex;
		clone.nextFlagIndex = this.nextFlagIndex;
		clone.overnextFlagIndex = this.overnextFlagIndex;
		clone.nofIts = this.nofIts;
		return clone;
	}

}
