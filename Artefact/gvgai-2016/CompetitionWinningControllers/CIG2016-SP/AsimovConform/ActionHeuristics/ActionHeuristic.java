package AsimovConform.ActionHeuristics;

import AsimovConform.Helper.AsimovState;
import AsimovConform.Helper.HeuristicAndPrunerCalculation;
import ontology.Types.ACTIONS;

import java.util.HashMap;
import java.util.Map;

public abstract class ActionHeuristic implements HeuristicAndPrunerCalculation {

    /**
     * stores configuration values for heuristics
     * the keys are following the pattern id + ".parametername"
     * one might also use id+".heuristictype.parametername" to add further hirachies
     */
    public static final Map<String, Double> HeuristicParameters = new HashMap<>();
    /**
     * the id is the identifier of a set of heuristic instances.
     * This id can be used to train  and optimize the heuristic automatically.
     * An id can be the same for multiple heuristics as identifier, that their parameters
     * are strongly similar. (Or are assumed similar for optimization purposes.
     */
    public final String id;

    public ActionHeuristic(String id) {
        this.id = id;
    }

    public static void initHeuristicProperties() {
        HeuristicParameters.put("prevmove.weight", 1.0);
        HeuristicParameters.put("wall.weight", 100.0);
        HeuristicParameters.put("epsilon.weight", 0.01);
        HeuristicParameters.put("orientationMovement.weight", 10.0);
    }

    public abstract double evaluate(AsimovState state, ACTIONS action);

    /**
     * returns the value of the key if it is in the map
     *
     * @param key the key to look up
     * @param or  the alternative value, if the key isn't in the map
     * @return if the key is in the map the key, else or
     */
    public double getDoubleParameterOr(String key, Double or) {
        return HeuristicParameters.getOrDefault(key, or);
    }


    public WeightedActionHeuristic getWeightedHeuristic() {
        return new WeightedActionHeuristic(getDoubleParameterOr(id + ".weight", 1.0), this);
    }

    public WeightedActionHeuristic getWeightedHeuristic(double weight) {
        return new WeightedActionHeuristic(weight, this);
    }

}
