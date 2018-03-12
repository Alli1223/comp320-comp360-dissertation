package specialGenerals.policies;

import core.game.StateObservation;
import ontology.Types;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 26.05.2016.
 */
public class MultiPruner implements IPruner {

    protected final List<IPruner> pruners;

    public MultiPruner(List<IPruner> pruners) {
        this.pruners = pruners;
    }

    @Override
    public List<Types.ACTIONS> prune(StateObservation so, List<Types.ACTIONS> actions) {

        List<Types.ACTIONS> myActions = new ArrayList<Types.ACTIONS>();
        myActions.addAll(actions);
        for (IPruner p : pruners) {
            myActions = p.prune(so, myActions);
        }
        return myActions;
    }
}
