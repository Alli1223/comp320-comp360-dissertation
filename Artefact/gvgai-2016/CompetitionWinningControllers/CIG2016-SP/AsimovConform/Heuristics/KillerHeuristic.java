package AsimovConform.Heuristics;

import AsimovConform.Helper.AsimovState;
import core.game.Observation;

import java.util.ArrayList;

/**
 * Created by thi on 15.06.16.
 */
public class KillerHeuristic extends Heuristic {


    public KillerHeuristic(String id) {
        super(id);
    }

    @Override
    public double evaluate(AsimovState state) {
        double akku = 0;
        int counter = 1;
        double maxdist = state.getWorldSize().manDist();
        ArrayList<Observation>[] npcPositions = state.getNPCPositions(state.getAvatarPosition());
        if (npcPositions != null)
            for (ArrayList<Observation> type : npcPositions) {
                for (Observation ob : type) {
                    counter++;
                    akku += maxdist - ob.position.dist(state.getAvatarPosition());
                }

            }


        return akku / counter;
    }

    @Override
    public void doPreCalculation(AsimovState as) {

    }
}
