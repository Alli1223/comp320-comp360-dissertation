package AsimovConform.Heuristics;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Helper.Vector2i;

import java.awt.Color;

public class HeatMapWithCooldownHeuristic extends Heuristic {
    private int[][] heatmap;
    private int cooldown;

    /**
     * @param id
     * @param cd        Cooldown Time in GameTicks
     * @param worldSize size of Grid
     */
    public HeatMapWithCooldownHeuristic(String id, int cd, Vector2i worldSize) {
        super(id);

        cooldown = cd;
        heatmap = new int[worldSize.x][worldSize.y];
    }

    public double evaluate(AsimovState as) {
        if (inBounds(as)) {
            double heat = heatmap[as.getAvatarX()][as.getAvatarY()] - as.getGameTick();
            return heat >= 0 ? heat : 0;
        }

        return 0;
    }

    public void doPreCalculation(AsimovState as) {
        if (inBounds(as))
            heatmap[as.getAvatarX()][as.getAvatarY()] = as.getGameTick() + cooldown;


        if (Agent.DRAW) {
            int colorStep = 255 / cooldown;
            for (int x = 0; x < heatmap.length; x++)
                for (int y = 0; y < heatmap[0].length; y++) {
                    int heatColor = Math.max(0, Math.min(255, (heatmap[x][y] - as.getGameTick()) * colorStep));
                    if (heatColor != 0)
                        Agent.drawer.add("heatMap", new Vector2i(x, y), new Color(255, 255 - heatColor, 0, 100), true);
                }
        }

    }

    private boolean inBounds(AsimovState as) {
        return as.getAvatarX() >= 0 && as.getAvatarX() < as.getWorldSize().x &&
                as.getAvatarY() >= 0 && as.getAvatarY() < as.getWorldSize().y;
    }
}