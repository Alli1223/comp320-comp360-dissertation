package specialGenerals.heatmaps;

import core.game.StateObservation;

public class ScoreHeatMap extends AbstractHeatMap {

    public ScoreHeatMap(StateObservation state) {
        super(state);
    }

    @Override
    protected double extractInformation(StateObservation state) {
        return state.getGameScore();
    }

}
