package AsimovConform.Heuristics;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Helper.Vector2i;
import AsimovConform.KnowledgeBase.ObjectInfo;
import core.game.Observation;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;

/**
 * scores that you moved an object
 * Created by thi on 27.05.16.
 */
public class MovedAnObjectHeuristic extends Heuristic {

    public MovedAnObjectHeuristic(String id) {
        super(id);
    }


    @Override
    public double evaluate(AsimovState state) {
        //todo eval orientation
        if (state.getLastAction() == Types.ACTIONS.ACTION_UP)
            return getMovVal(state, new Vector2i(0, -1));
        if (state.getLastAction() == Types.ACTIONS.ACTION_DOWN)
            return getMovVal(state, new Vector2i(0, 1));
        if (state.getLastAction() == Types.ACTIONS.ACTION_LEFT)
            return getMovVal(state, new Vector2i(-1, 0));
        if (state.getLastAction() == Types.ACTIONS.ACTION_RIGHT)
            return getMovVal(state, new Vector2i(1, 0));
        return 0;

    }

    private int getMovVal(AsimovState state, Vector2i mov) {
        int x = state.getAvatarX() + mov.x;
        int y = state.getAvatarY() + mov.y;
        if (x < 0 || x >= state.getObservationGrid()[0].length || y < 0 || y >= state.getObservationGrid().length)
            return 0;

        for (Observation obs : state.getObservationGrid()[y][x]) {
            if (obs.category == Types.TYPE_MOVABLE)
                return 1;
        }
        return 0;

        /*ArrayList<ObjectInfo> obs = Agent.KB.gridAnalyser.getInformations(state.getAvatarPosition().add(p));
        for (ObjectInfo o : obs)
            if (o.moveabel == 1) return 1;
        return -1;*/
    }

    public void doPreCalculation(AsimovState as) {}
}
