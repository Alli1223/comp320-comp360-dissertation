package specialGenerals.policies;

import core.game.StateObservation;
import ontology.Types;
import specialGenerals.algorithms.helpers.KnowledgeBase;

import java.util.List;

/**
 * Created by marco on 26.05.2016.
 */
public class BlockPruner implements IPruner {

    protected final KnowledgeBase kb;

    public BlockPruner(KnowledgeBase kb) {
        this.kb = kb;
    }

    @Override
    public List<Types.ACTIONS> prune(StateObservation so, List<Types.ACTIONS> actions) {

        return kb.getBlockPrunedActions(so, actions);

    }
}
