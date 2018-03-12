package specialGenerals.heuristics;

import core.game.StateObservation;
import tools.ElapsedCpuTimer;

public class HealthHeuristic implements IHeuristic {

    @Override
    public double getValue(StateObservation so, ElapsedCpuTimer time) {
        return so.getAvatarHealthPoints();
    }

}
