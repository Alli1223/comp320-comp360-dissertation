package specialGenerals.heuristics;

import core.game.StateObservation;
import tools.ElapsedCpuTimer;

import java.util.LinkedList;
import java.util.List;

/**
 * To be able to use multiple heurisitic, this heurisitic uses a number of
 * heurisitics.
 *
 * @author jonas
 */
public class HeuristicCombiner implements IHeuristic {

    // TODO der public modifier ist eine Übergangslösung
    private List<WeightedHeuristic> heuristics;

    public HeuristicCombiner() {
        this.heuristics = new LinkedList<>();
    }

    public HeuristicCombiner(List<WeightedHeuristic> heuristics) {
        this.heuristics = heuristics;
    }

    @Override
    public double getValue(StateObservation so, ElapsedCpuTimer time) {
        double value = 0;
        for (WeightedHeuristic heuristic : heuristics) {
            value += heuristic.getHeuristic().getValue(so, time) * heuristic.getWeight();
        }
        return value;
    }

    public static class WeightedHeuristic {
        private final IHeuristic heuristic;
        private final double weight;

        public WeightedHeuristic(IHeuristic heuristic, double weight) {
            this.heuristic = heuristic;
            this.weight = weight;
        }

        public IHeuristic getHeuristic() {
            return heuristic;
        }

        public double getWeight() {
            return weight;
        }

    }

}
