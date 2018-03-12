package specialGenerals.policies;

import core.game.StateObservation;
import ontology.Types;
import specialGenerals.algorithms.helpers.KnowledgeBase;

import java.util.List;

/**
 * Created by marco on 26.05.2016.
 */
public class DeathPruner implements IPruner {

    protected final KnowledgeBase kb;

    public DeathPruner(KnowledgeBase kb) {
        this.kb = kb;
    }

    @Override
    public List<Types.ACTIONS> prune(StateObservation so, List<Types.ACTIONS> actions) {
        return kb.getDeathPrunedActions(so, actions);
    }


}
