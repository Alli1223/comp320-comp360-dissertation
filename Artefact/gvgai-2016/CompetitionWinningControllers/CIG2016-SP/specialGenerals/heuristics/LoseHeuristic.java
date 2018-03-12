package specialGenerals.heuristics;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Uses loose as only criterium.
 *
 * @author jonas
 */
public class LoseHeuristic implements IHeuristic {

    @Override
    public double getValue(StateObservation so, ElapsedCpuTimer time) {
        if (so.getGameWinner().equals(Types.WINNER.PLAYER_LOSES)) {
            return -1;
        } else {
            return 0;
        }
    }

}
