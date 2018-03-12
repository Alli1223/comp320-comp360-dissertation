package AsimovConform.ActionPruner;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Helper.Vector2i;
import AsimovConform.KnowledgeBase.NPCInfo;
import AsimovConform.KnowledgeBase.ObjectInfo;
import core.game.Observation;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;

public class DeathActionPruner extends ActionPruner {

    @Override
    public boolean pruned(AsimovState state, Types.ACTIONS action) {
        Vector2i avatarPosition = state.getAvatarGridPosition();
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

        Vector2d avatarDPosition = new Vector2d(avatarPosition.x, avatarPosition.y);
        ArrayList<ObjectInfo> objectInfos = Agent.KB.gridAnalyser.getInformations(avatarDPosition);
        for (ObjectInfo objectInfo : objectInfos) {
            if (objectInfo.losesgame == 1) {
                return true;
            }
        }

        avatarDPosition.mul(state.getBlockSize());

        ArrayList<Observation>[] movablePositions = state.getStateObservation().getMovablePositions(avatarDPosition);
        if (movablePositions != null) {
            for (ArrayList<Observation> movablePosition : movablePositions) {
                for (Observation movable : movablePosition) {
                    ObjectInfo objectInfo = Agent.KB.gridAnalyser.seenObjects.get(movable.itype);
                    if (objectInfo != null
                            && objectInfo.losesgame == 1
                            && movable.position.dist(avatarDPosition) <= state.getBlockSize() * 1.6) {
                        return true;
                    }
                }
            }
        }

        ArrayList<Observation>[] npcPositions = state.getStateObservation().getNPCPositions(avatarDPosition);
        if (npcPositions != null) {
            for (ArrayList<Observation> npcPosition : npcPositions) {
                for (Observation npc : npcPosition) {
                    NPCInfo npcInfo = Agent.KB.gridAnalyser.npcs.get(npc.itype);
                    if (npcInfo != null
                            && npcInfo.losesgame == 1
                            && npc.position.dist(avatarDPosition) <= npcInfo.distance) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void doPreCalculation(AsimovState as) {}
}
