package Shazboz;

import core.game.StateObservation;
import java.util.Random;
import ontology.Types;
import tools.ElapsedCpuTimer;

public class Player {

    Random rnd = new Random();

    /**
     * Makes a tree for n rounds then acts out, sets the root as the previous
     * action(so it keeps a portion of the tree).This concept didn't really work
     * in the end: either it just keeps on making a bigger tree (which means it
     * throws 1 depth away but keeps n-1,which I think becomes too outdated) or
     * it tries to build from the initial tree of n rounds,which loses
     * information too fast so it just becomes an act1
     */
    int counter = 0;
    Node root = null;

    public int actremembern(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int n) {
        elapsedTimer.setMaxTimeMillis(elapsedTimer.remainingTimeMillis());
        if (counter > n) {
            int action = root.search(elapsedTimer);
            root = root.children[root.getByVisits()];
            root.parent = null;
            counter = 1;
            return action;
        } else if (counter == 0) {
            root = new Node(null, rnd, stateObs);
            counter++;
            return -1;
        } else {
            root.expandTree(elapsedTimer);
            counter++;
            return -1;
        }

    }

    /**
     * Makes a tree for n rounds then acts out, after it acts out it forgets the
     * tree
     */
    public int actforgetn(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int n) {
        elapsedTimer.setMaxTimeMillis(elapsedTimer.remainingTimeMillis());
        if (counter == 0) {
            root = new Node(null, rnd, stateObs);
            counter++;
            return -1;
        } else if (counter < n) {
            if (root == null) {
                System.out.println("Null");
            }
            root.expandTree(elapsedTimer);
            counter++;
            return -1;
        } else {
            int action = root.search(elapsedTimer);
            counter = 0;
            return action;
        }

    }

    /**
     * Makes a tree for 1 round returns the action and forgets the tree
     */
    public int act1(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        elapsedTimer.setMaxTimeMillis(elapsedTimer.remainingTimeMillis());
        root = new Node(null, rnd, stateObs);
        return root.search(elapsedTimer);
    }

}
