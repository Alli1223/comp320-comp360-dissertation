package controllers.singlePlayer.sampleFlatMCTS;

import java.awt.*;
import java.util.Random;

import DissertationFiles.DataCollection;
import DissertationFiles.Visualisations;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer {
	
	public static Random random;
	public static ACTIONS[] actions;
	public static int MAX_DEPTH;

	//! Edited by Alli 09/03/2018
	// List of variables for storing and rendering MCTS information
	private Visualisations vis = new Visualisations();
	private DataCollection dataCollection = new DataCollection();;
	//! Edit End
	
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
		random = new Random();
		actions = so.getAvailableActions().toArray(new ACTIONS[0]);
		MAX_DEPTH = 10;
	}
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		//! Edited Alli - 09/03/2018
		// Add game state to be collected
		dataCollection.AddGameStateToCollection(stateObs);
		// Edit End
		
		double worstCase = 10;
		double avgTime = 10;
		double totalTime = 0;
		double iteration = 0;
		int bestAction = -1;
		TreeNode root = new TreeNode(stateObs, null);
		
		while(elapsedTimer.remainingTimeMillis() > 2 * avgTime && elapsedTimer.remainingTimeMillis() > worstCase){
			ElapsedCpuTimer temp = new ElapsedCpuTimer();
			//treeSelect
			TreeNode node = root.SelectNode();
			
			//Simulate
			double value = node.ExploreNode();
			
			//RollBack
			node.UpdateNode(value);
			
			//Get the best action
			bestAction = root.GetBestChild();
			
			totalTime += temp.elapsedMillis();
			iteration += 1;
			avgTime = totalTime / iteration;
		}
		
		if(bestAction == -1){
			System.out.println("Out of time choosing random action");
			bestAction = random.nextInt(actions.length);
		}
		
		return actions[bestAction];
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
