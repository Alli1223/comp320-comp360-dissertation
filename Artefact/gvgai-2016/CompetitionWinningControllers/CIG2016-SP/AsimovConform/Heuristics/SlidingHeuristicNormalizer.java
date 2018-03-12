package AsimovConform.Heuristics;

import AsimovConform.Helper.AsimovState;

/**
 * Normalizes heuristics by assuming the highest value ever seen as absolute maximum.
 * <p>
 * Attention: when keeping old scores and a higher score is seen in the meantime
 * the relation between two normalized heuristics might not be proper.
 */
public class SlidingHeuristicNormalizer extends Heuristic {

    public static double max = Double.MIN_VALUE;


    private Heuristic heuristic;


    public SlidingHeuristicNormalizer(Heuristic heuristic, String id) {
        super(id);
        this.heuristic = heuristic;
    }


    public double evaluate(AsimovState state) {
        double value = heuristic.evaluate(state);

        double absValue = Math.abs(value);
        if (max < absValue) {
            max = absValue;
        }

        return value / max;
    }

    public void doPreCalculation(AsimovState as) {}
}
