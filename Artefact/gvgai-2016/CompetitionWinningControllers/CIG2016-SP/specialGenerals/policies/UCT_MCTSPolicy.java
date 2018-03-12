package specialGenerals.policies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ontology.Types;
import ontology.Types.ACTIONS;
import specialGenerals.Config;
import specialGenerals.algorithms.helpers.KnowledgeBase;
import specialGenerals.algorithms.helpers.Node;
import tools.ElapsedCpuTimer;

/**
 * Created by marco on 16.04.2016.
 */
public class UCT_MCTSPolicy implements IMCTSPolicy {

    protected final double K;
    protected final KnowledgeBase kb;
    protected final IPruner pruner;
    protected static final Random random = new Random();
    
    /**
     * Erstellt eine neue Policy für die Auswahl beim MCTS
     *
     * @param K Tradeoff zwischen Exploration und Exploitation
     */
    public UCT_MCTSPolicy(double K, KnowledgeBase kb, IPruner pruner) {
        this.K = K;
        this.kb = kb;
        this.pruner = pruner;
    }

    @Override
    public Types.ACTIONS getAction(Node node, ElapsedCpuTimer time) {
        int totalPlays = node.getVisited();
        double bestUCTValue = -Double.MAX_VALUE;
        ArrayList<ACTIONS> availableActions = node.getState().getAvailableActions();
        List<ACTIONS> usefulActions = getUsefulActions(node, availableActions);
        usefulActions = pruner.prune(node.getState(), usefulActions);
        Types.ACTIONS bestAction = usefulActions.get(random.nextInt(usefulActions.size()));
        swapElements(0, random.nextInt(usefulActions.size()), usefulActions);
        for (Types.ACTIONS action : usefulActions) {
            Node childNode = node.getChild(action);
            double value = childNode.getScore();
            int plays = childNode.getVisited();
            double uctValue = getUCTValue(value, plays, K, totalPlays);
            if (uctValue > bestUCTValue) {
                bestAction = action;
                bestUCTValue = uctValue;
            }
        }
        return bestAction;
    }

    protected double getUCTValue(double value, int plays, double explorationParameter, int totalPlays) {
        if(plays < 1){
            return Double.MAX_VALUE;
        }
        if(totalPlays < 1){
            totalPlays = 1;
        }
        return value + explorationParameter * Math.sqrt(Math.log(totalPlays) / plays);
    }

    protected <T extends Object> void  swapElements(int i, int j, List<T> l){
        T temp = l.get(i);
        l.set(i, l.get(j));
        l.set(j,temp);
    }
    /**
     * @param node
     * @param availableActions
     * @return
     */
    protected List<Types.ACTIONS> getUsefulActions(Node node, ArrayList<Types.ACTIONS> availableActions) {
        List<Types.ACTIONS> usefulActions = new ArrayList<>();
        if (kb.needsDoubleAction(node.getState(), node.getReachedByAction())) {
            usefulActions.add(node.getReachedByAction());
            if (availableActions.contains(Types.ACTIONS.ACTION_USE)) {
                usefulActions.add(Types.ACTIONS.ACTION_USE);
            }
            // usefulActions.add(Types.ACTIONS.ACTION_NIL);
        } else {
            usefulActions = node.getState().getAvailableActions(false);
        }
        return usefulActions;
    }

}
