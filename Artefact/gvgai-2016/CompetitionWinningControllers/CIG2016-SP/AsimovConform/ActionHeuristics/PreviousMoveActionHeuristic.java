package AsimovConform.ActionHeuristics;

import AsimovConform.Helper.AsimovState;
import ontology.Types.ACTIONS;

import java.util.ArrayList;

/**
 * If the move to evaluate is opposite of the last move, the action will get a worse score.
 */
public class PreviousMoveActionHeuristic extends ActionHeuristic {

    public PreviousMoveActionHeuristic(String id) {
        super(id);
    }


    public double evaluate(AsimovState state, ACTIONS action) {
        ArrayList<ACTIONS> actionHistory = state.getAdvanceHistory();
        // If more than two moves happened, check if they cancel each other out
        if (actionHistory.size() >= 1) {
            // the last move
            ACTIONS lastMove = actionHistory.get(actionHistory.size() - 1);

            if ((action == ACTIONS.ACTION_DOWN && lastMove == ACTIONS.ACTION_UP)
                    || (action == ACTIONS.ACTION_LEFT && lastMove == ACTIONS.ACTION_RIGHT)
                    || (action == ACTIONS.ACTION_UP && lastMove == ACTIONS.ACTION_DOWN)
                    || (action == ACTIONS.ACTION_RIGHT && lastMove == ACTIONS.ACTION_LEFT)) {
                //if (Agent.OUTPUT)
                //  System.out.println("Previous Move Action Heristic detected an opposite move: " + action);
                return 0;
            }
        }

        return 1;
    }

    @Override
    public void doPreCalculation(AsimovState as) {}
}
