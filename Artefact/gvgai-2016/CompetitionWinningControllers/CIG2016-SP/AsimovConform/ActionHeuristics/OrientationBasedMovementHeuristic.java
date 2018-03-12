package AsimovConform.ActionHeuristics;

import AsimovConform.Helper.AsimovState;
import AsimovConform.KnowledgeBase.KnowledgeBase;
import ontology.Types.ACTIONS;

import java.util.ArrayList;

/**
 * This is supposed to penalise turning around on the same spot.
 */
public class OrientationBasedMovementHeuristic extends ActionHeuristic {

    public OrientationBasedMovementHeuristic(String id) {
        super(id);
    }


    public double evaluate(AsimovState state, ACTIONS action) {
        if (!KnowledgeBase.currentKnowledgeBase.orientationBased)
            return 0;

        ACTIONS lastMove = state.getLastMovementAction();
        ACTIONS moveBeforeLast = state.getSecondLastMovementAction();

        // if the 2 last moves were equal, we definitely moved somewhere, so we don't have to penalise anything
        if (moveBeforeLast == lastMove)
            return 0;

        // the use action should get a value somewhere in between
        if (action == ACTIONS.ACTION_USE)
            return 0.5;

        // if the move to evaluate is the same as the last move, we give it a higher score, because that would
        // be an actual move in this direction and everything else would be spinning only
        if (lastMove == action)
            return 1;

        return 0;
    }

    @Override
    public void doPreCalculation(AsimovState as) {}
}
