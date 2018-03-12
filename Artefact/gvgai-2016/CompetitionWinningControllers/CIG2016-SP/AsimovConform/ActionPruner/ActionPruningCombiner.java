package AsimovConform.ActionPruner;

import AsimovConform.Helper.AsimovState;
import ontology.Types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActionPruningCombiner extends ActionPruner {

    private List<ActionPruner> pruners = new ArrayList<>();

    public ActionPruningCombiner(ActionPruner... prunes) {
        Collections.addAll(pruners, prunes);
    }


    @Override
    public boolean pruned(AsimovState state, Types.ACTIONS action) {
        boolean pruned = false;

        for (ActionPruner pruner : pruners) {
            pruned |= pruner.pruned(state, action);
        }

        return pruned;
    }

    @Override
    public void doPreCalculation(AsimovState as) {
        for(ActionPruner pruner : pruners) {
            pruner.doPreCalculation(as);
        }
    }
}
