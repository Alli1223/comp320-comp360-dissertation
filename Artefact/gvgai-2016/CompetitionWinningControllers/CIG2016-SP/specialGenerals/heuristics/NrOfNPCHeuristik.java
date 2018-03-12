package specialGenerals.heuristics;

import core.game.Observation;
import core.game.StateObservation;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;

/**
 * The more more possibility one has, the better.
 *
 * @author jonas
 */
public class NrOfNPCHeuristik implements IHeuristic {

    @Override
    public double getValue(StateObservation so, ElapsedCpuTimer time) {
        int counter = 0;
        ArrayList<Observation>[] movables = so.getMovablePositions();
        if(movables!=null) {
            for (ArrayList<Observation> obsList : movables) {
                if (obsList != null) {
                    counter += obsList.size();
                }
            }
        }
        return counter;
    }

}
