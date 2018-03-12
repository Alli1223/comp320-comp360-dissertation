package AsimovConform.ActionPruner;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import ontology.Types;

/**
 * Does not allow the Agent to use ACTION_USE twice at the same place with the same orientation.
 */
public class OrientationBasedUsePruner extends ActionPruner {

    @Override
    public boolean pruned(AsimovState state, Types.ACTIONS action) {
        if (!Agent.KB.orientationBased || !Agent.KB.gameHasUseAction)
            return false;

        return action == Types.ACTIONS.ACTION_USE && state.getLastAction() == Types.ACTIONS.ACTION_USE;

    }

    @Override
    public void doPreCalculation(AsimovState as) {

    }
}
