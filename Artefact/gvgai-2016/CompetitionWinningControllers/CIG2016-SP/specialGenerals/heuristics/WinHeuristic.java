package specialGenerals.heuristics;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Uses Win as only Criterium.
 *
 * @author marco
 */
public class WinHeuristic implements IHeuristic {

    @Override
    public double getValue(StateObservation so, ElapsedCpuTimer time) {
        if (so.getGameWinner().equals(Types.WINNER.PLAYER_WINS)) {
            return 1;
        } else {
            return 0;
        }
    }

}
