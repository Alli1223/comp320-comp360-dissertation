package specialGenerals.heatmaps;

import core.game.StateObservation;
import ontology.Types;

/**
 * Wie häufig an diesem Feld gewonnen wird.
 *
 * @author jonas
 */
public class WinHeatMap extends AbstractHeatMap {

    public WinHeatMap(StateObservation state) {
        super(state);
    }

    @Override
    protected double extractInformation(StateObservation state) {
        if (state.getGameWinner() == Types.WINNER.PLAYER_WINS) {
            return 1;
        } else {
            return 0;
        }
    }
}
