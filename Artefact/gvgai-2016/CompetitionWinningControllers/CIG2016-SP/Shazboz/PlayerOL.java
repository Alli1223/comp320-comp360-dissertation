package Shazboz;

import core.game.StateObservation;
import java.util.Random;
import ontology.Types;
import tools.ElapsedCpuTimer;

public class PlayerOL {

    Random rnd = new Random();

    /**
     * Makes a tree for n rounds then acts out, sets the root as the previous
     * action(so it keeps a portion of the tree).This concept didn't really work
     * in the end: either it just keeps on making a bigger tree (which means it
     * throws 1 depth away but keeps n-1,which I think becomes too outdated) or
     * it tries to build from the initial tree,which loses information too fast
     * so it just becomes an act1
     */
    int counter = 0;
    NodeOL root = null;

    public int actremembern(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int n) {
        elapsedTimer.setMaxTimeMillis(elapsedTimer.remainingTimeMillis());
        NodeOL.setRootState(stateObs);

        if (counter == 0) {
            root = new NodeOL(null, rnd);
            counter++;
            return -1;
        } else if (counter < n) {
            root.expandTree(elapsedTimer);
            counter++;
            return -1;
        } else {
            int action = root.search(elapsedTimer);
            root = root.children[action];
            root.parent = null;
            counter = 1;

            return action;
        }

    }

    /**
     * Makes a tree for n rounds then acts out, after it acts out it forgets the
     * tree
     */
    public int actforgetn(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int n) {
        elapsedTimer.setMaxTimeMillis(elapsedTimer.remainingTimeMillis());
        NodeOL.setRootState(stateObs);
        if (counter == 0) {
            root = new NodeOL(null, rnd);
            counter++;
            return -1;
        } else if (counter < n) {
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
        NodeOL.setRootState(stateObs);
        root = new NodeOL(null, rnd);
        return root.search(elapsedTimer);
    }

}
