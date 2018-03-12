package AsimovConform.Heuristics;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import ontology.Types;

/**
 * returns 1 if the game is orientation based and the last action was ACTION_USE
 * if the game is not orienatation based, no ACTION_USE is available or ACTION_USE is not the last used action 0 will be returned
 */
public class OrientedUseActionHeuristic extends Heuristic {
    public OrientedUseActionHeuristic(String id) {
        super(id);
    }

    public double evaluate(AsimovState state) {
        if (Agent.KB.orientationBased && Agent.KB.useActionExists) {
            int count = 0;
            for (Types.ACTIONS action : state.getAdvanceHistory()) {
                if (action == Types.ACTIONS.ACTION_USE) {
                    count++;
                }
            }

            return count;
        }

        return 0;
    }

    public void doPreCalculation(AsimovState as) {}
}