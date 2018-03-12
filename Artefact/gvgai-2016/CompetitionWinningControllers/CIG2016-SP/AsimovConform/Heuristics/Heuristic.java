package AsimovConform.Heuristics;

import AsimovConform.Helper.AsimovState;
import AsimovConform.Helper.HeuristicAndPrunerCalculation;

import java.util.HashMap;
import java.util.Map;

/**
 * Super Class of all Heuristics
 */
public abstract class Heuristic implements HeuristicAndPrunerCalculation {

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

    public Heuristic(String id) {
        this.id = id;
    }

    public static void initHeuristicProperties() {
        HeuristicParameters.put("win.weight", 1000000.0);
        HeuristicParameters.put("score.weight", 1000.0);
        HeuristicParameters.put("heatmap.weight", -50.0);
        HeuristicParameters.put("moved.weight", 20.0);
        HeuristicParameters.put("ticks.weight", 0.1);
        HeuristicParameters.put("danger.weight", 10.0);

        HeuristicParameters.put("bfs.win.weight", 1000000.0);
        HeuristicParameters.put("bfs.moved.weight", 20.0);
        HeuristicParameters.put("bfs.ticks.weight", 0.1);
        HeuristicParameters.put("bfs.score.weight", 1.0);
        HeuristicParameters.put("bfs.orientedUse.weight", 0.05);
        HeuristicParameters.put("bfs.corner", -30.0);
    }

    public abstract double evaluate(AsimovState state);

    /**
     * called in the begining of an act
     *
     * @param as
     */
    public abstract void doPreCalculation(AsimovState as);


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


    public WeightedHeuristic getWeightedHeuristic() {
        return new WeightedHeuristic(getDoubleParameterOr(id + ".weight", 1.0), this);
    }

    public WeightedHeuristic getWeightedHeuristic(double weight) {
        return new WeightedHeuristic(weight, this);
    }

}
