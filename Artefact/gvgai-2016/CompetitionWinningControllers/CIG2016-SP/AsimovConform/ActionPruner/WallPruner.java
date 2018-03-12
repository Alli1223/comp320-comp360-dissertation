package AsimovConform.ActionPruner;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.KnowledgeBase.ObjectInfo;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;

public class WallPruner extends ActionPruner {

    @Override
    public boolean pruned(AsimovState state, Types.ACTIONS action) {
        Vector2d avatarPosition = state.getAvatarPosition().copy().mul(1.0 / state.getBlockSize());
        switch (action) {
            case ACTION_LEFT:
                avatarPosition.x -= 1;
                break;
            case ACTION_DOWN:
                avatarPosition.y += 1;
                break;
            case ACTION_UP:
                avatarPosition.y -= 1;
                break;
            case ACTION_RIGHT:
                avatarPosition.x += 1;
                break;
            default:
                // if it is no move action, we just return the same value, as if it were not solid
                // so only the movement to solid blocks will get a penalty
                return false;
        }
        ArrayList<ObjectInfo> objectInfos = Agent.KB.gridAnalyser.getInformations(avatarPosition);
        for (ObjectInfo objectInfo : objectInfos) {
            if (objectInfo.solid == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doPreCalculation(AsimovState as) {}
}
