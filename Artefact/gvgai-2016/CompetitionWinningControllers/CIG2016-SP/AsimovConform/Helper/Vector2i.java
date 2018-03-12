package AsimovConform.Helper;

import tools.Vector2d;

public class Vector2i {
    public int x;
    public int y;

    public Vector2i() {
        x = 0;
        y = 0;
    }

    public Vector2i(Vector2i v) {
        x = v.x;
        y = v.y;
    }

    public Vector2i(int xx, int yy) {
        x = xx;
        y = yy;
    }

    public Vector2i(Vector2d v) {
        x = (int) v.x;
        y = (int) v.y;
    }

    public boolean equals(Object o) {
        if (o instanceof Vector2i) {
            Vector2i v = (Vector2i) o;
            return x == v.x && y == v.y;
        } else {
            return false;
        }
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public void set(Vector2i v) {
        x = v.x;
        y = v.y;
    }

    public void set(Vector2d v) {
        x = (int) v.x;
        y = (int) v.y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i add(int a) {
        x = x + a;
        y = y + a;
        return this;
    }

    public Vector2i add(Vector2i v) {
        x = x + v.x;
        y = y + v.y;
        return this;
    }

    public Vector2i mul(int m) {
        x = x * m;
        y = y * m;
        return this;
    }

    public Vector2d mul(double m) {
        Vector2d v = new Vector2d(x, y);
        return v.mul(m);
    }

    public Vector2i div(int d) {
        x = x / d;
        y = y / d;
        return this;
    }

    public void setX(int xx) {
        x = xx;
    }

    public void setY(int yy) {
        y = yy;
    }

    public Vector2i copy() {
        return new Vector2i(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int manDist() {
        return manDist(0, 0);
    }

    public int manDist(Vector2i v) {
        return manDist(v.x, v.y);
    }

    public int manDist(int xx, int yy) {
        return Math.abs(x - xx) + Math.abs(y - yy);
    }
}
