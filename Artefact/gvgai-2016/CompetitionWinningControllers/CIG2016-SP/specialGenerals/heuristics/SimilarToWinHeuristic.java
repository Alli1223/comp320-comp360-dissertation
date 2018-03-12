package specialGenerals.heuristics;

import core.game.StateObservation;
import specialGenerals.algorithms.helpers.Similarity;
import tools.ElapsedCpuTimer;

/**
 * Die Idee ist: Wenn wir irgendwann zufällig einen State finden, indem wir
 * gewinnen, bewerte wir ab diesem Zeitpunkt Zustände höher, welche
 * similar zu diesem winning State sind.
 *
 * @author jonas
 */
public class SimilarToWinHeuristic implements IHeuristic {
    public static StateObservation winningState;

    @Override
    public double getValue(StateObservation so, ElapsedCpuTimer time) {
        if (winningState != null) {
            return Similarity.measureState(so, winningState) - 0.5;
        }

        return 0;
    }

}
