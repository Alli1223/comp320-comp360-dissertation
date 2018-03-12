package specialGenerals.heatmaps;

import tools.Vector2d;

public class Position implements Cloneable {
    public int x;
    public int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(Vector2d vector, int blockSize) {
        this.x = (int) Math.round(vector.x / blockSize);
        this.y = (int) Math.round(vector.y / blockSize);
    }

    public Position(Vector2d vector) {
        this.x = (int) Math.round(vector.x);
        this.y = (int) Math.round(vector.y);
    }

    @Override
    public Position clone() {
        return new Position(x, y);
    }

}
