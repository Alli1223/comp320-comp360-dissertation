package specialGenerals.policies;

import core.game.StateObservation;
import ontology.Types;

import java.util.List;

/**
 * Created by marco on 10.06.2016.
 */
public class PositionPruner implements IPruner {
    @Override
    public List<Types.ACTIONS> prune(StateObservation so, List<Types.ACTIONS> actions) {
        if(so.getAvatarPosition().x <= 0){
            actions.remove(Types.ACTIONS.ACTION_LEFT);
        }
        if(so.getAvatarPosition().y <= 0){
            actions.remove(Types.ACTIONS.ACTION_UP);
        }
        if(so.getAvatarPosition().x >= so.getWorldDimension().width - so.getBlockSize()){
            actions.remove(Types.ACTIONS.ACTION_RIGHT);
        }
        if(so.getAvatarPosition().y >= so.getWorldDimension().height - so.getBlockSize()){
            actions.remove(Types.ACTIONS.ACTION_DOWN);
        }
        return actions;
    }
}
