package specialGenerals.heuristics;

import core.game.Observation;
import core.game.StateObservation;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;


/**
 * Created by marco on 04.06.2016.
 */
public class NrOfMovablesHeuristic implements IHeuristic {
    @Override
    public double getValue(StateObservation so, ElapsedCpuTimer time) {
        int counter = 0;
        ArrayList<Observation>[] NPCs = so.getNPCPositions();
        if(NPCs!=null) {
            for (ArrayList<Observation> obsList : NPCs) {
                if (obsList != null) {
                    counter += obsList.size();
                }
            }
        }
        return counter;
    }
}
