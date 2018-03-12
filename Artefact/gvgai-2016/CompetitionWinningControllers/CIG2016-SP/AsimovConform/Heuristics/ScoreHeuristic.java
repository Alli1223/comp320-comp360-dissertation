package AsimovConform.Heuristics;

import AsimovConform.Helper.AsimovState;

/**
 * score gives score
 */
public class ScoreHeuristic extends Heuristic {

    public ScoreHeuristic(String id) {
        super(id);
    }

    public double evaluate(AsimovState state) {
        return state.getGameScore();
    }

    public void doPreCalculation(AsimovState as) {}
}
