package specialGenerals.policies;

import core.game.StateObservation;
import ontology.Types;
import specialGenerals.algorithms.helpers.KnowledgeBase;

import java.util.List;

public class LastMovementPruner implements IPruner {

    protected final KnowledgeBase kb;

    public LastMovementPruner(KnowledgeBase kb) {
        this.kb = kb;
    }

    @Override
    public List<Types.ACTIONS> prune(StateObservation so, List<Types.ACTIONS> actions) {

        return kb.getLastMovePrunedActions(so, actions);

    }
}
