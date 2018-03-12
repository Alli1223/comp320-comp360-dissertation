package AsimovConform.Heuristics;

import AsimovConform.Helper.AsimovState;

/**
 * negativley scores time ticks
 * Created by thi on 27.05.16.
 */
public class TimeTickHeuristic extends Heuristic {

    public TimeTickHeuristic(String id) {
        super(id);
    }

    @Override
    public double evaluate(AsimovState state) {
        return state.getGameTick();
    }

    public void doPreCalculation(AsimovState as) {}
}
