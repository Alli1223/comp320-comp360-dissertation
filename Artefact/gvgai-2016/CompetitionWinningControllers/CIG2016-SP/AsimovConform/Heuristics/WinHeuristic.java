package AsimovConform.Heuristics;

import AsimovConform.Helper.AsimovState;
import ontology.Types;

/**
 * This is the most basic heuristic, which should be used in every game no matter what.
 * Decorators can be used to extend this behavior.
 */
public class WinHeuristic extends Heuristic {

    public WinHeuristic(String id) {
        super(id);
    }

    public double evaluate(AsimovState state) {
        boolean gameOver = state.isGameOver();
        Types.WINNER win = state.getGameWinner();

        if (gameOver && win == Types.WINNER.PLAYER_LOSES)
            return -1;

        if (gameOver && win == Types.WINNER.PLAYER_WINS)
            return 1;

        return 0;
    }

    public void doPreCalculation(AsimovState as) {}
}
