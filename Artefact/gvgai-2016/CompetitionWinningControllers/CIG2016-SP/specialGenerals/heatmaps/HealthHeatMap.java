package specialGenerals.heatmaps;

import core.game.StateObservation;

/**
 * Wo wird health gewonnen, wo wird health verloren?
 *
 * @author jonas
 */
public class HealthHeatMap extends AbstractHeatMap {

    public HealthHeatMap(StateObservation state) {
        super(state);
    }

    @Override
    public double extractInformation(StateObservation state) {
        return state.getAvatarHealthPoints();
    }

}
