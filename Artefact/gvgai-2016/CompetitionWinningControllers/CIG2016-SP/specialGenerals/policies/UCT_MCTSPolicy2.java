package specialGenerals.policies;

import ontology.Types;
import specialGenerals.algorithms.helpers.KnowledgeBase;
import specialGenerals.algorithms.helpers.Node;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 12.06.2016.
 */
public class UCT_MCTSPolicy2 extends UCT_MCTSPolicy {
    /**
     * Erstellt eine neue Policy für die Auswahl beim MCTS
     *
     * @param K      Tradeoff zwischen Exploration und Exploitation
     * @param kb
     * @param pruner
     */
    public UCT_MCTSPolicy2(double K, KnowledgeBase kb, IPruner pruner) {
        super(K, kb, pruner);
    }

    @Override
    public Types.ACTIONS getAction(Node node, ElapsedCpuTimer time) {
        int totalPlays = node.getVisited();
        double bestUCTValue = -Double.MAX_VALUE;
        ArrayList<Types.ACTIONS> availableActions = node.getState().getAvailableActions(false);
        List<Types.ACTIONS> usefulActions = getUsefulActions(node, availableActions);
        usefulActions = pruner.prune(node.getState(), usefulActions);
        swapElements(0, random.nextInt(usefulActions.size()), usefulActions);
        Types.ACTIONS bestAction = availableActions.get(random.nextInt(availableActions.size()));
        for (Types.ACTIONS action : usefulActions) {
            Node childNode = node.getChild2(action);
            double value = childNode.getScore();
            int plays = childNode.getVisited();
            double uctValue = getUCTValue(value, plays, K, totalPlays);
            if (uctValue > bestUCTValue && !childNode.isIgnoreNode()) {
                bestAction = action;
                bestUCTValue = uctValue;
            }
        }
        return bestAction;
    }
}
