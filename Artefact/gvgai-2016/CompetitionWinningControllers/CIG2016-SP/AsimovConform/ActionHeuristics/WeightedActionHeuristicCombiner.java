package AsimovConform.ActionHeuristics;

import AsimovConform.Helper.AsimovState;
import AsimovConform.Heuristics.WeightedHeuristic;
import ontology.Types.ACTIONS;

public class WeightedActionHeuristicCombiner extends ActionHeuristicCombiner {

    public WeightedActionHeuristicCombiner(String id, WeightedActionHeuristic... hs) {
        super(id, hs);
    }

    @Override
    public double evaluate(AsimovState state, ACTIONS action) {
        double akku = 0;
        for (WeightedActionHeuristic h : heuristics) {
            akku += h.weight * h.heuristic.evaluate(state, action);
        }
        return akku;
    }

    @Override
    public void doPreCalculation(AsimovState as) {
        for(WeightedActionHeuristic weightedHeuristic : heuristics) {
            weightedHeuristic.heuristic.doPreCalculation(as);
        }
    }
}
