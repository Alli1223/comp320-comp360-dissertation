package specialGenerals.heatmaps;

import core.game.StateObservation;
import specialGenerals.Config;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for a heat map.
 *
 * @author jonas
 */
public abstract class AbstractHeatMap implements Cloneable {
    public final int blockSize;
    protected double oldInformation;
    protected final int nrBlocksX;
    protected final int nrBlocksY;
    protected double max;
    protected double min;

    protected List<List<Double>> heatmap;

    public AbstractHeatMap(StateObservation state) {
        blockSize = state.getBlockSize();
        nrBlocksX = state.getWorldDimension().width / blockSize;
        nrBlocksY = state.getWorldDimension().height / blockSize;
        oldInformation = 0;
        max = 0;
        min = 0;

        heatmap = new ArrayList<>(nrBlocksX);
        for (int i = 0; i < nrBlocksX; i++) {
            List<Double> column = new ArrayList<>(nrBlocksY);
            for (int j = 0; j < nrBlocksY; j++) {
                column.add(new Double(0));
            }
            heatmap.add(column);
        }
    }

    /**
     * Updates the heat map using the changes between oldState and newState.
     *
     * @param state
     */
    public void updateHeatMap(StateObservation state) {
        double newInformation = extractInformation(state);
        addHeat(newInformation - oldInformation, toPosition(state.getAvatarPosition()));
        double newHeat = getHeat(state.getAvatarPosition());
        min = Math.min(min, newHeat);
        max = Math.max(max, newHeat);
        oldInformation = newInformation;
    }

    /**
     * Frome state extract all information relevant for the specific heat map
     *
     * @param state
     * @return relevant information
     */
    protected abstract double extractInformation(StateObservation state);

    /**
     * @param position
     * @return heat at position
     */
    public double getHeat(Position position) {
        if (isInRange(position)) {
            return this.heatmap.get(position.x).get(position.y);
        } else {
            return 0;
        }
    }

    /**
     * @param vector
     * @return heat at position
     */
    public double getHeat(Vector2d vector) {
        return getHeat(toPosition(vector));
    }

    /**
     * Add heat to a specific position
     *
     * @param heat
     * @param position
     */
    public void addHeat(double heat, Position position) {
        if (isInRange(position)) {
            heatmap.get(position.x).set(position.y, getHeat(position) + heat);
        }
    }

    /**
     * Add heat to a specific position
     *
     * @param heat
     * @param vector
     */
    public void addHeat(double heat, Vector2d vector) {
        addHeat(heat, toPosition(vector));
    }

    /**
     * @param vector
     * @return
     */
    protected Position toPosition(Vector2d vector) {
        return new Position((int) Math.round(vector.x / blockSize), (int) Math.round(vector.y / blockSize));
    }

    @Override
    public AbstractHeatMap clone() {
        try {
            AbstractHeatMap clone = (AbstractHeatMap) super.clone();
            // Deep Copy all mutable objects manually
            clone.heatmap = deepCopy(heatmap);
            clone.max = max;
            clone.min = min;
            return clone;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    protected static List<List<Double>> deepCopy(List<List<Double>> heatmap) {
        List<List<Double>> clone = new ArrayList<List<Double>>();
        for (List<Double> list : heatmap) {
            clone.add(new ArrayList<>(list));
        }
        return clone;
    }

    protected boolean isInRange(Position position) {
        return position.x >= 0 && position.x < nrBlocksX && position.y >= 0 && position.y < nrBlocksY;
    }

    public int getNrBlocksX(){
        return nrBlocksX;
    }

    public int getNrBlocksY(){
        return nrBlocksY;
    }

    public double getMin(){
        return min;
    }

    public double getMax(){
        return max;
    }

    public void cooldown(){
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for(int x = 0; x < nrBlocksX; x++){
            for(int y = 0; y < nrBlocksY; y++){
                double old = heatmap.get(x).get(y);
                if(old > 0){
                    old -= Math.min(Config.HEATMAP_COOLDOWN, old);
                }else if(old < 0){
                    old -= Math.min(-Config.HEATMAP_COOLDOWN, old);
                }
                min = Math.min(min, old);
                max = Math.max(max, old);
                heatmap.get(x).set(y, old);
            }
        }
        this.min = min;
        this.max = max;
    }

}
