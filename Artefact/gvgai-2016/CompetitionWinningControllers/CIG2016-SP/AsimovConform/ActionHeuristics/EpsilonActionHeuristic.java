package AsimovConform.ActionHeuristics;

import AsimovConform.Helper.AsimovState;
import ontology.Types;

import java.util.Random;

/**
 * Returns a random number between 1.0 and 2.0
 */
public class EpsilonActionHeuristic extends ActionHeuristic {

    public EpsilonActionHeuristic(String id) {
        super(id);
    }


    public double evaluate(AsimovState state, Types.ACTIONS action) {
        return (new Random().nextDouble()) + 1;
    }

    @Override
    public void doPreCalculation(AsimovState as) {}
}
