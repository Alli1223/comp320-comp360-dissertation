package specialGenerals.policies;

import core.game.StateObservation;
import ontology.Types;
import specialGenerals.heatmaps.AbstractHeatMap;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by marco on 17.05.2016.
 */
public class HeatMapPolicy implements IPolicy {

    public static AbstractHeatMap heatmap;
    protected final Random r;
    protected final IPruner pruner;

    public HeatMapPolicy(IPruner pruner) {
        this.pruner = pruner;
        r = new Random();
    }

    @Override
    public Types.ACTIONS getAction(StateObservation so, ElapsedCpuTimer time, boolean ignorePruning) {
        Vector2d pos = so.getAvatarPosition();

        List<Types.ACTIONS> actions = so.getAvailableActions(false);
        if(!ignorePruning) {
            actions = pruner.prune(so, actions);
        }
        List<Double> heats = new ArrayList<>();
        List<Double> probabilities = new ArrayList<>();
        double sumOfProb = 0;
        double minHeat = Double.MAX_VALUE;
        double maxHeat = -Double.MAX_VALUE;
        int blockSize = so.getBlockSize();
        for (Types.ACTIONS action : actions) {
            double heat = getHeat(pos, action, blockSize);
            heats.add(heat);
            minHeat = Math.min(minHeat, heat);
            maxHeat = Math.max(maxHeat, heat);
        }
        for (int i = 0; i < heats.size(); ++i) {
            // Verschiebung des Intervalls
            double trimmed = (heats.get(i) - maxHeat) / (minHeat - maxHeat) * 90 + 10;
            if(maxHeat-minHeat < .01){
                trimmed = 100;
            }
            if(actions.get(i)== Types.ACTIONS.ACTION_USE){
                trimmed = 75;
            }
            probabilities.add(trimmed);
            sumOfProb += trimmed;
        }
        double randVal = r.nextDouble() * sumOfProb;
        sumOfProb = 0;
        for (int i = 0; i < probabilities.size(); ++i) {
            sumOfProb += probabilities.get(i);
            if (randVal <= sumOfProb) {
                return actions.get(i);
            }
        }
        return Types.ACTIONS.ACTION_NIL;
    }

    private double getHeat(Vector2d pos, Types.ACTIONS action, int blockSize) {
        switch (action) {
            case ACTION_UP:
                return heatmap.getHeat(pos.add(0, -blockSize));
            case ACTION_DOWN:
                return heatmap.getHeat(pos.add(0, blockSize));
            case ACTION_LEFT:
                return heatmap.getHeat(pos.add(-blockSize, 0));
            case ACTION_RIGHT:
                return heatmap.getHeat(pos.add(blockSize, 0));
            default:
                return heatmap.getHeat(pos);
        }
    }

}
