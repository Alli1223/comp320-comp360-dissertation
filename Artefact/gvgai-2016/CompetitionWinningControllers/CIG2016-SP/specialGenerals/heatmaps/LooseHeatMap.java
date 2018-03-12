package specialGenerals.heatmaps;

import core.game.StateObservation;
import ontology.Types;

/**
 * Wie häufig an diesem Feld verloren wird.
 *
 * @author jonas
 */
public class LooseHeatMap extends AbstractHeatMap {

    public LooseHeatMap(StateObservation state) {
        super(state);
    }

    @Override
    protected double extractInformation(StateObservation state) {
        if (state.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
            return 1;
        } else {
            return 0;
        }
    }

}
