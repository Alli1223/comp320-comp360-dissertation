package AsimovConform.Heuristics;

import AsimovConform.Helper.AsimovState;

/**
 *
 * wights the distance to an enemy expoenentially
 * Created by thi on 18.05.16.
 */
public class ExponentialEnemyDistanceHeuristic extends Heuristic {
    public ExponentialEnemyDistanceHeuristic(String id) {
        super(id);
    }

    @Override
    public double evaluate(AsimovState state) {
        return 0;
    }

    public void doPreCalculation(AsimovState as) {}
}
