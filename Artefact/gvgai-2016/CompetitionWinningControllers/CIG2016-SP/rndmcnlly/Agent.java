package rndmcnlly;

import java.util.ArrayList;
import java.util.Random;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer {

	private Random random;
	
	public Agent (StateObservation so, ElapsedCpuTimer elapsedTimer) {
		
		random = new Random(0);
    }
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		ACTIONS[] actions = stateObs.getAvailableActions(true).toArray(new ACTIONS[0]);
		
		double[] accumulators = new double[actions.length];
		final int horizon = 10;
		
		StateObservation root = stateObs;
		
		int attempts = 0;
		attemptLoop:
		while (elapsedTimer.remainingTimeMillis() > 5) {
			int actionIndex = attempts++%actions.length;
			
			ACTIONS action = actions[actionIndex];
			StateObservation currentState = root.copy();
			currentState.advance(action);
			int sampledHorizon = random.nextInt(2*horizon);
			for(int step = 0; step < sampledHorizon; step++) {
				if (currentState.isGameOver()) {
					Types.WINNER win = currentState.getGameWinner();
					if (win == Types.WINNER.PLAYER_WINS) {
						accumulators[actionIndex] += 1;
					} else {
						accumulators[actionIndex] -= 1;
					}
					continue attemptLoop;
				} else {
					accumulators[actionIndex] += 0; // for now
				}
				ArrayList<ACTIONS> nextActions = currentState.getAvailableActions(true);
				currentState.advance(nextActions.get(random.nextInt(nextActions.size())));
			};
		}
		
		double bestAccumulator = Double.NEGATIVE_INFINITY;
		ACTIONS bestAction = ACTIONS.ACTION_NIL;
		for (int i = 0; i < actions.length; i++) {
			if (accumulators[i] > bestAccumulator) {
				bestAccumulator = accumulators[i];
				bestAction = actions[i];
			}
		}
		
		return bestAction;
		
	}

}
