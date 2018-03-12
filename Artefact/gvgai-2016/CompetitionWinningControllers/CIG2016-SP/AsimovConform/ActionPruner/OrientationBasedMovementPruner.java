package AsimovConform.ActionPruner;

import AsimovConform.Helper.AsimovState;
import AsimovConform.KnowledgeBase.KnowledgeBase;
import ontology.Types.ACTIONS;

public class OrientationBasedMovementPruner extends ActionPruner {

    @Override
    public boolean pruned(AsimovState state, ACTIONS action) {
        if (!KnowledgeBase.currentKnowledgeBase.orientationBased)
            return false;

        // the use action should get a value somewhere in between
        if (action == ACTIONS.ACTION_USE)
            return false;

        ACTIONS lastMove = state.getLastMovementAction();
        ACTIONS moveBeforeLast = state.getSecondLastMovementAction();

        // if the 2 last moves were equal, we definitely moved somewhere, so we don't have to penalise anything
        if (moveBeforeLast == lastMove)
            return false;

        // if the move to evaluate is the same as the last move, we give it a higher score, because that would
        // be an actual move in this direction and everything else would be spinning only
        return lastMove != action;
    }

    @Override
    public void doPreCalculation(AsimovState as) {}
}
