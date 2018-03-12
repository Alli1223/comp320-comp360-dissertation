package AsimovConform.Heuristics;

import AsimovConform.Helper.AsimovState;

/**
 * WeightedAddCombiner
 * Created by thi on 18.05.16.
 */
public class SAddCombiner extends HeuristicCombiner {

    public SAddCombiner(String id, WeightedHeuristic... hs) {
        super(id, hs);
    }

    @Override
    public double evaluate(AsimovState state) {
        double akku = 0;
        for (WeightedHeuristic h : heuristics) {
            akku += h.weight * h.heuristic.evaluate(state);
        }
        return akku;
    }

    @Override
    public void doPreCalculation(AsimovState as) {
        for(WeightedHeuristic h : heuristics) {
            h.heuristic.doPreCalculation(as);
        }
    }
}
