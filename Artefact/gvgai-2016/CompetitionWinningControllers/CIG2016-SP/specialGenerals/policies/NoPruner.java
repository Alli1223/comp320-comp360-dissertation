package specialGenerals.policies;

import core.game.StateObservation;
import ontology.Types;

import java.util.List;

/**
 * Created by marco on 26.05.2016.
 */
public class NoPruner implements IPruner {
    @Override
    public List<Types.ACTIONS> prune(StateObservation so, List<Types.ACTIONS> actions) {
        return actions;
    }
}
