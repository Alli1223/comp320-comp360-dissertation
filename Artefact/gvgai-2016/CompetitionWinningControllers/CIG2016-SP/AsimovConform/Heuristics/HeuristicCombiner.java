package AsimovConform.Heuristics;

import AsimovConform.Helper.AsimovState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Super class of all heuristic combiners
 * Created by thilo on 09.05.16.
 */
public abstract class HeuristicCombiner extends Heuristic {

    List<WeightedHeuristic> heuristics = new ArrayList<>();

    public HeuristicCombiner(String id, WeightedHeuristic... heurs) {
        super(id);
        Collections.addAll(heuristics, heurs);
    }

    public abstract void doPreCalculation(AsimovState as);

}
