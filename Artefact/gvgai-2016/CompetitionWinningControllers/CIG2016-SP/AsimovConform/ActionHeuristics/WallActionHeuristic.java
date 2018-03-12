package AsimovConform.ActionHeuristics;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.KnowledgeBase.ObjectInfo;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;

/**
 * Checks the block in the direction, where the player is headed.
 * If it is not solid, the move will get a higher value.
 */
public class WallActionHeuristic extends ActionHeuristic {

    public WallActionHeuristic(String id) {
        super(id);
    }


    public double evaluate(AsimovState state, Types.ACTIONS action) {
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
                return 1;
        }
        ArrayList<ObjectInfo> objectInfos = Agent.KB.gridAnalyser.getInformations(avatarPosition);
        for (ObjectInfo objectInfo : objectInfos) {
            if (objectInfo.solid == 1) {
                // if (Agent.OUTPUT)
                //   System.out.println("Wall Action Heuristic detected a wall in position " + avatarPosition + " after move " + action);
                return 0;
            }
        }
        return 1;
    }

    @Override
    public void doPreCalculation(AsimovState as) {}

}
