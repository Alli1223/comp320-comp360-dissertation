package specialGenerals.heuristics;

import core.game.StateObservation;
import tools.ElapsedCpuTimer;

import java.util.Random;

public class NoiseHeuristic implements IHeuristic {
    private static final Random r = new Random();

    @Override
    public double getValue(StateObservation so, ElapsedCpuTimer time) {
        return r.nextDouble();
    }

}
