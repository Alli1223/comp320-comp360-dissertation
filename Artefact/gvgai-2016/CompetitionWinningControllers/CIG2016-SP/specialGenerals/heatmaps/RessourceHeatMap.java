package specialGenerals.heatmaps;

import core.game.StateObservation;

/**
 * Heatmap zeigt an, wo Ressourcen aufgesammelt wurden.
 * <p/>
 * Hinweis: Zeigt NICHT an, wo ressourcen veringert wurden!
 *
 * @author jonas
 */
public class RessourceHeatMap extends AbstractHeatMap {

    public RessourceHeatMap(StateObservation state) {
        super(state);
    }

    /* TODO: UpdateHeatMap in extractInformation umwandeln!
    @Override
    public void updateHeatMap(StateObservation oldState, StateObservation newState) {
        double amount = 0;
        for (int resId : oldState.getAvatarResources().keySet()) {
            // TODO Verlust von Ressourcen wird hier auch als Ressourcengewinn vermerkt!
            amount += Math.abs(newState.getAvatarResources().get(resId) - oldState.getAvatarResources().get(resId));
        }
        this.addHeat(amount, newState.getAvatarPosition());
    }
    */

    @Override
    protected double extractInformation(StateObservation state) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

}
