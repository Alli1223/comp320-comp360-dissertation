package specialGenerals.algorithms.helpers;

import java.util.Comparator;

/**
 * TODO Klasse umbenennen, das man weiß was sie bedeutet
 * Created by ann-sophie on 11.05.16.
 */
public class BFSQueueStruct implements Comparator<BFSQueueStruct>, Comparable<BFSQueueStruct> {
    private final int depth;
    private final double heuristicValue;
    private final Node node;

    public BFSQueueStruct(int depth, double heuristicValue, Node node) {
        this.depth = depth;
        this.heuristicValue = heuristicValue;
        this.node = node;
    }

    public int getDepth() {
        return depth;
    }

    public Node getNode() {
        return node;
    }

    public double getHeuristicValue() {
        return heuristicValue;
    }

    /**
     * Compares two QueueStructs and returns a positive number, if the first has
     * a higher heuristic value. If they have the same heuristic value the depth
     * (smaller depth preferred) serves as a tie breaker.
     */
    @Override
    public int compare(BFSQueueStruct d1, BFSQueueStruct d2) {
        if ((d1.getHeuristicValue() - d2.getHeuristicValue()) < 0.1) {
            if (d1.getDepth() < d2.getDepth()) {
                return -1;
            } else if (d1.getDepth() > d2.getDepth()) {
                return 1;
            } else {
                return 0;
            }
        } else if (d1.getHeuristicValue() < d2.getHeuristicValue()) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public int compareTo(BFSQueueStruct dOther) {
        return compare(this, dOther);
    }

}
