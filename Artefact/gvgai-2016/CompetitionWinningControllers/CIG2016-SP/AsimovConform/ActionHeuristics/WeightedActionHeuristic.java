package AsimovConform.ActionHeuristics;

public class WeightedActionHeuristic {

    ActionHeuristic heuristic;
    double weight;

    public WeightedActionHeuristic(double weight, ActionHeuristic heuristic) {
        this.weight = weight;
        this.heuristic = heuristic;
    }

}
