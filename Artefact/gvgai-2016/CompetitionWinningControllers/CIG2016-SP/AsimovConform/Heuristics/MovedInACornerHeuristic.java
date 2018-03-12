package AsimovConform.Heuristics;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Helper.Vector2i;
import core.game.Observation;
import ontology.Types;

import java.util.ArrayList;

/**
 * Created by Timm on 10.06.2016.
 */
public class MovedInACornerHeuristic extends Heuristic {

    public MovedInACornerHeuristic(String id) {
        super(id);
    }

    @Override
    public double evaluate(AsimovState state) {

        ArrayList<Observation>[][] grid = state.getObservationGrid();

        Types.ACTIONS a = state.getLastAction();
        Vector2i kpos;
        if (a == Types.ACTIONS.ACTION_DOWN) {
            kpos = new Vector2i(state.getAvatarX(), state.getAvatarY() + 1);
        } else if (a == Types.ACTIONS.ACTION_LEFT) {
            kpos = new Vector2i(state.getAvatarX() - 1, state.getAvatarY());
        } else if (a == Types.ACTIONS.ACTION_RIGHT) {
            kpos = new Vector2i(state.getAvatarX() + 1, state.getAvatarY());
        } else {
            kpos = new Vector2i(state.getAvatarX(), state.getAvatarY() - 1);
        }
        if (kpos.x < 0 || kpos.x >= grid[0].length || kpos.y < 0 || kpos.y >= grid.length || grid[kpos.y][kpos.x].get(0) == null) {
            return 0;
        }

        Observation o = grid[kpos.y][kpos.x].get(0);

        if (Agent.KB.gridAnalyser.seenObjects.get(o.itype).moveable == 1) {
            int counter = 0;
            if (Agent.KB.gridAnalyser.seenObjects.get(grid[kpos.y + 1][kpos.x].get(0).itype).solid == 1 || Agent.KB.gridAnalyser.seenObjects.get(grid[kpos.y + 1][kpos.x].get(0).itype).moveable == 1) {
                counter++;
            }
            if (Agent.KB.gridAnalyser.seenObjects.get(grid[kpos.y - 1][kpos.x].get(0).itype).solid == 1 || Agent.KB.gridAnalyser.seenObjects.get(grid[kpos.y + 1][kpos.x].get(0).itype).moveable == 1) {
                counter++;
            }
            if (Agent.KB.gridAnalyser.seenObjects.get(grid[kpos.y][kpos.x + 1].get(0).itype).solid == 1 || Agent.KB.gridAnalyser.seenObjects.get(grid[kpos.y + 1][kpos.x].get(0).itype).moveable == 1) {
                counter++;
            }
            if (Agent.KB.gridAnalyser.seenObjects.get(grid[kpos.y][kpos.x - 1].get(0).itype).solid == 1 || Agent.KB.gridAnalyser.seenObjects.get(grid[kpos.y + 1][kpos.x].get(0).itype).moveable == 1) {
                counter++;
            }
            if (counter > 1)
                return 1;

        }
        return 0;
    }

    public void doPreCalculation(AsimovState as) {}
}
