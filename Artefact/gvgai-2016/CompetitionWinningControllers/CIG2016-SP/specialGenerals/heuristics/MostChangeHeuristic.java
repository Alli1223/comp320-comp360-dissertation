package specialGenerals.heuristics;

import core.game.StateObservation;
import specialGenerals.algorithms.helpers.Similarity;
import tools.ElapsedCpuTimer;

/**
 * Changing the state of the environment is good! We want to advance
 *
 * @author jonas
 */
public class MostChangeHeuristic implements IHeuristic {

    public static StateObservation previousState;

    @Override
    public double getValue(StateObservation so, ElapsedCpuTimer time) {
        // If state is very different to state before, that is good.

        if (previousState != null) {
            // if states are similar, return -1+0.5=-0.5 if states are not similar return 0+0.5= 0.5
            return -Similarity.measureState(so, previousState) + 0.5;
        }
        // No previous state 
        return 0;
    }

}
