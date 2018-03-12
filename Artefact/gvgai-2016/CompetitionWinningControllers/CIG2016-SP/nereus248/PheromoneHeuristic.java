package nereus248;

import java.awt.Dimension;
import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import tools.Vector2d;


/**
 * This class simulates a pheromone trace left by the agent that keeps it from going back to already visited places
 * too quickly. Pheromone spreads to neighbouring cells but also dissipates again after some time.
 * As prposed in the paper ... TODO paper reference
 * @author Manuel
 *
 */
public class PheromoneHeuristic {
	
	private float sqrtTwoHalf;
	
	public float pheromoneSpread;
	public float pheromoneDispersion;
	public float pheromoneDissipation;
	
	// Current position of the player
	private int position_x;
	private int position_y;
	
	private int stepSize;
	private int worldWidth, worldHeight;
	private float pheromoneMap[][];
	
	private float maxVal;
	
	private Memory memory;
	
	/**
	 * Must be created at the beginning of the game with the initial position of the player 
	 * @param initialState
	 */
	public PheromoneHeuristic(StateObservation initialState, Memory memory) 
	{
		Dimension worldDimension = initialState.getWorldDimension();
		stepSize = initialState.getBlockSize();
		worldWidth = worldDimension.width/stepSize;
		worldHeight = worldDimension.height/stepSize;
		pheromoneMap = new float[worldWidth][worldHeight];
		
		Vector2d initialPosition = initialState.getAvatarPosition();
		position_x = (int)Math.floor(initialPosition.x/stepSize);
		position_y = (int)Math.floor(initialPosition.y/stepSize);
		
		sqrtTwoHalf = (float) Math.sqrt(2.0f)*0.5f;
		
		maxVal = 0;
		
		pheromoneSpread = 0.08f;
		pheromoneDispersion = 0.2f;
		pheromoneDissipation = 0.6f;
		
		this.memory = memory;
	}
	
	
	/**
	 * Updates the pheromone map (pheromone spreads, dissipates and accumulates at current position)
	 * @param newPosition
	 */
	public void advanceTimeStep(Vector2d newPosition, StateObservation currentState) {
		
		position_x = (int)Math.floor(newPosition.x/stepSize);
		position_y = (int)Math.floor(newPosition.y/stepSize);
		
		// Leave pheromone mark at the current position
		if (position_x >= 0 && position_x < worldWidth && position_y >= 0 && position_y < worldHeight)
			pheromoneMap[position_x][position_y] += pheromoneDispersion;
		
		float[][] tempPheromoneMap = new float[worldWidth][worldHeight];
		
		// Dissipation
		for (int y = 0; y < worldHeight; y++) {
			for(int x = 0; x < worldWidth; x++) {
				pheromoneMap[x][y] *= pheromoneDissipation;
			}
		}
		
		// Spread (neglect border for performance)
		for (int y = 1; y < worldHeight-1; y++) {
			for(int x = 1; x < worldWidth-1; x++) {
				float spread = pheromoneMap[x][y]*pheromoneSpread;
				float spreadDiag = spread*sqrtTwoHalf;
				tempPheromoneMap[x-1][y-1] += spreadDiag;
				tempPheromoneMap[x][y-1] += spread;
				tempPheromoneMap[x+1][y-1] += spreadDiag;
				tempPheromoneMap[x-1][y] += spread;
				tempPheromoneMap[x+1][y] += spread;
				tempPheromoneMap[x-1][y+1] += spreadDiag;
				tempPheromoneMap[x][y+1] += spread;
				tempPheromoneMap[x+1][y+1] += spreadDiag;
				
			}
		}
		ArrayList<Observation>[][] observationGrid = currentState.getObservationGrid();
		maxVal = 0;
		for (int y = 0; y < worldHeight; y++) {
			for(int x = 0; x < worldWidth; x++) {
				boolean obstacle = false;
				for (Observation o : observationGrid[x][y]) {
					if (memory.isSpriteTypeObstacle.getOrDefault(o.itype,true)) {
						obstacle = true;
						break;
					}
				}
				if (obstacle) continue;
				pheromoneMap[x][y] += tempPheromoneMap[x][y];
				maxVal = Math.max(maxVal, pheromoneMap[x][y]);
			}
		} 
	}
	
	/**
	 * Returns the pheromone density at the specified location
	 * @param location
	 * @return
	 */
	public float getPheromoneDensityAt(Vector2d location) {
		int x = (int)Math.floor(location.x/stepSize);
		int y = (int)Math.floor(location.y/stepSize);
		if( x < 0 || x >= worldWidth || y < 0 || y >= worldHeight)
			return 0;
		
		if (maxVal != 0)
			return pheromoneMap[x][y] / maxVal;
		else
			return pheromoneMap[x][y];
	}

}
