package AsimovConform.Helper;

import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

public class AStar implements Comparable<AStar> {
    private Vector2i current, goal;
    private double distance;

    public static ArrayList<Types.ACTIONS> findPath(int x, int y) {
        ArrayList<Types.ACTIONS> path = null;
        PriorityQueue<AStar> openList = new PriorityQueue<AStar>(Collections.reverseOrder());

        return path;
    }

    AStar(Vector2i s, Vector2i g) {
        current = s;
        goal = g;
        distance = current.manDist(goal);
    }

    @Override
    public int compareTo(AStar o) {
        return  (int) Math.signum(distance - o.distance);
    }

    @Override
    public int hashCode() {
        int prime = 37;
        int result = 19 * prime * current.x;
        return result * prime * current.y;
    }
}
