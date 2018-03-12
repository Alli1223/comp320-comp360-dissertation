package specialGenerals.heuristics;

import core.game.Observation;
import core.game.StateObservation;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;

/**
 * Created by marco on 12.06.2016.
 */
public class NrOfImmovableHeuristic implements IHeuristic {
    @Override
    public double getValue(StateObservation so, ElapsedCpuTimer time) {
        int counter = 0;
        ArrayList<Observation>[] immovablePositions = so.getImmovablePositions();
        if(immovablePositions!=null) {
            for (ArrayList<Observation> obsList : immovablePositions) {
                if (obsList != null) {
                    counter += obsList.size();
                }
            }
        }
        return counter;
    }
}
