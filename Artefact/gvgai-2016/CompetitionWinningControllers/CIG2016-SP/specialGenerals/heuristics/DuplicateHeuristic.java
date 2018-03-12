package specialGenerals.heuristics;

import core.game.StateObservation;
import tools.ElapsedCpuTimer;

import java.util.HashSet;
import java.util.Set;

/**
 * Bereits besuchte States sollten mit einer niedrigeren Wahrscheinlichkeit angesteuert werden.
 *
 * @author jonas
 */
public class DuplicateHeuristic implements IHeuristic {

    private Set<Long> visitedStates;
    private Hashing hasher;

    public DuplicateHeuristic() {
        visitedStates = new HashSet<>();
        hasher = new Hashing();
    }

    @Override
    public double getValue(StateObservation so, ElapsedCpuTimer time) {
        // Gebe einen negativen value, falls state schonmal vorkam.
        if (visitedStates.contains(so)) {
            return -1;
        }
        return 0;
    }

    public void addState(StateObservation state) {

        visitedStates.add(hasher.SimpleHashing(state));
    }
}
