package AsimovConform.ActionHeuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ActionHeuristicCombiner extends ActionHeuristic {

    List<WeightedActionHeuristic> heuristics = new ArrayList<>();

    public ActionHeuristicCombiner(String id, WeightedActionHeuristic... heurs) {
        super(id);
        Collections.addAll(heuristics, heurs);
    }

}
