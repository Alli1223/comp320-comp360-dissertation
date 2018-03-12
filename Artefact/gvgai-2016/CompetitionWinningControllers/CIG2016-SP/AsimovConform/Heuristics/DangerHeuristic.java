package AsimovConform.Heuristics;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Helper.Vector2i;
import AsimovConform.KnowledgeBase.NPCInfo;
import core.game.Observation;

import java.util.ArrayList;


/**
 * Created by Timm on 26.05.2016.
 */
public class DangerHeuristic extends Heuristic {
    double wert = 1;

    public DangerHeuristic(String id) {

        super(id);
    }
    //// TODO: 26.05.2016  Add GridAnlyser to AsimovState 

    @Override
    public double evaluate(AsimovState state) {
        ArrayList<Observation>[] npcPositions = state.getStateObservation().getNPCPositions();
        int bs = state.getBlockSize();
        if (state.getStateObservation().getNPCPositions() != null) {
            for (ArrayList<Observation> array : npcPositions) {
                if (array.isEmpty())
                    continue;
                NPCInfo npc = Agent.KB.gridAnalyser.npcs.get(array.get(0).itype);
                if (npc == null)
                    continue;
                if (npc.loselife < 0 || npc.losesgame == 1) {
                    for (Observation o : array) {
                        Vector2i posE = new Vector2i((int) o.position.x / bs, (int) (o.position.y / bs));
                        Vector2i posA = new Vector2i(state.getAvatarX(), state.getAvatarY());
                        if (npc.distance < posA.manDist(posE)) {
                            return -wert;
                        }
                    }
                }
                if (npc.loselife > 0 || npc.winsgame == 1) {
                    for (Observation o : array) {
                        Vector2i posE = new Vector2i((int) o.position.x / bs, (int) (o.position.y / bs));
                        Vector2i posA = new Vector2i(state.getAvatarX(), state.getAvatarY());
                        if (npc.distance >= posA.manDist(posE)) {
                            return wert;
                        }
                    }
                }
            }
        }
        return 0;
    }

    public void doPreCalculation(AsimovState as) {}
}
