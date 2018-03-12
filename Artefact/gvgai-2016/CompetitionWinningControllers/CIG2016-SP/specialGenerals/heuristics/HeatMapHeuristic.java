package specialGenerals.heuristics;

import core.game.StateObservation;
import specialGenerals.heatmaps.AbstractHeatMap;
import tools.ElapsedCpuTimer;

/**
 * Created by marco on 10.06.2016.
 */
public class HeatMapHeuristic implements IHeuristic {

    public static AbstractHeatMap heatMap;

    @Override
    public double getValue(StateObservation so, ElapsedCpuTimer time) {
        if(heatMap==null) {
            return 0;
        }else{
            return heatMap.getHeat(so.getAvatarPosition());
        }
    }
}
