package specialGenerals.heuristics;

import core.game.StateObservation;
import specialGenerals.heatmaps.AbstractHeatMap;
import tools.ElapsedCpuTimer;

/**
 * Zu einem Feld zu gehen, wo häufig score gemacht wird ist gut.
 *
 * @author jonas
 */
public class ScoreHeatMapHeuristik implements IHeuristic {
    AbstractHeatMap heatmap;

    public ScoreHeatMapHeuristik(AbstractHeatMap heatmap) {
        this.heatmap = heatmap;
    }

    @Override
    public double getValue(StateObservation so, ElapsedCpuTimer time) {
        return heatmap.getHeat(so.getAvatarPosition());
    }

}
