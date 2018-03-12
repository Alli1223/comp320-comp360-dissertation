package AsimovConform.Heuristics;

/**
 * Heuristic weight combined with heuristic,
 * used in combiners.
 * Created by thi on 27.05.16.
 */
public class WeightedHeuristic {

    Heuristic heuristic;
    double weight;

    public WeightedHeuristic(double weight, Heuristic heuristic) {
        this.weight = weight;
        this.heuristic = heuristic;
    }

}
