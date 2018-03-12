package AsimovConform.Heuristics;

import AsimovConform.Helper.AsimovState;

/**
 * weights gathering of resources positive
 * Created by thi on 18.05.16.
 */
public class GreedyResourceHeuristic extends Heuristic {


    public GreedyResourceHeuristic(String id) {
        super(id);
    }

    @Override
    public double evaluate(AsimovState state) {
        return 0;
    }

    public void doPreCalculation(AsimovState as) {}
}
