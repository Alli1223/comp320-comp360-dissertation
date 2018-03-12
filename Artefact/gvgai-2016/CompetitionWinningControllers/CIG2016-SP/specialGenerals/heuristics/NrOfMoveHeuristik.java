package specialGenerals.heuristics;

import core.game.StateObservation;
import tools.ElapsedCpuTimer;

/**
 * The more more possibility one has, the better.
 *
 * @author jonas
 */
public class NrOfMoveHeuristik implements IHeuristic {

    @Override
    public double getValue(StateObservation so, ElapsedCpuTimer time) {
        return so.getAvailableActions().size();
    }

}
