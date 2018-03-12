package AsimovConform.Heuristics;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Helper.Vector2i;

import java.awt.Color;

public class HeatMapHeuristic extends Heuristic {
    private int[][] heatmap;

    /**
     *
     * @param id
     * @param worldSize size of Grid
     */
    public HeatMapHeuristic(String id, Vector2i worldSize) {
        super(id);

        heatmap = new int[worldSize.x][worldSize.y];
    }

    public double evaluate(AsimovState as) {
        if(inBounds(as))
            return heatmap[as.getAvatarX()][as.getAvatarY()];

        return 0;
    }

    public void doPreCalculation(AsimovState as) {
        if(inBounds(as))
            heatmap[as.getAvatarX()][as.getAvatarY()]++;

        if (Agent.DRAW) {
            for (int x = 0; x < heatmap.length; x++)
                for (int y = 0; y < heatmap[0].length; y++) {
                    int heatColor = Math.min(255, heatmap[x][y] * 10);
                    if (heatColor != 0)
                        Agent.drawer.add("heatMap2", new Vector2i(x, y), new Color(heatColor, 0, 0, 50), true);
                }
        }
    }

    private boolean inBounds(AsimovState as) {
        return as.getAvatarX() >= 0 && as.getAvatarX() < as.getWorldSize().x &&
                as.getAvatarY() >= 0 && as.getAvatarY() < as.getWorldSize().y;
    }
}