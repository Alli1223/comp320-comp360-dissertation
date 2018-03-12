package specialGenerals.heatmaps;

import core.game.StateObservation;

/**
 * Created by marco on 24.05.2016.
 */
public class VisitedHeatMap extends AbstractHeatMap {

    public VisitedHeatMap(StateObservation state) {
        super(state);
    }

    @Override
    protected double extractInformation(StateObservation state) {
        return 1;
    }

    @Override
    public void updateHeatMap(StateObservation state) {
        addHeat(extractInformation(state), toPosition(state.getAvatarPosition()));
        double newHeat = getHeat(state.getAvatarPosition());
        min = Math.min(min, newHeat);
        max = Math.max(max, newHeat);
    }
}
