package AsimovConform.ActionPruner;

import AsimovConform.Helper.AsimovState;
import AsimovConform.Helper.HeuristicAndPrunerCalculation;
import ontology.Types.ACTIONS;

public abstract class ActionPruner implements HeuristicAndPrunerCalculation {

    public abstract boolean pruned(AsimovState state, ACTIONS action);

}
