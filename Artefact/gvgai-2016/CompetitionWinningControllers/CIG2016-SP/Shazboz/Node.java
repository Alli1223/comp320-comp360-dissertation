package Shazboz;

import core.game.StateObservation;
import java.util.Random;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * MCTS without using open loop,which means the state stays the same when it's
 * been simulated once(good for deterministic games)
 */
public class Node {

    Random rnd;
    static final double FAIL = -100000;
    static final double WIN = 100000;
    static final double C = Math.sqrt(2.0);
    double value;
    int visited;
    Node parent;
    Node[] children;
    int depth;
    StateObservation state;
    int expandcount = 0;

    public Node(Node parent, Random rnd, StateObservation state) {
        this.rnd = rnd;
        this.state = state;
        this.parent = parent;
        value = 0.0;
        visited = 1;
        children = new Node[Agent.NUM_ACTIONS];
        if (parent == null) {
            depth = 0;
        } else {
            depth = parent.depth + 1;
        }
    }

    /**
     * Expands tree while it can,then returns the best result when it runs out
     * of time
     */
    public int search(ElapsedCpuTimer elapsedTimer) {
        long ms = elapsedTimer.remainingTimeMillis();
        while (ms > 10) {
            this.expandTree();
            expandcount++;
            ms = elapsedTimer.remainingTimeMillis();
        }
        return this.getByVisits();
    }

    /**
     * Updates parents with new values
     */
    public void backpropagate(Node n) {
        double value = n.value;
        n = n.parent;
        while (n.parent != null) {
            n.value = (n.value * n.visited + value) / (n.visited + 1);
            n.visited += 1;
            n = n.parent;
        }
    }

    public void expandTree() {
        Node best = this;
        while (best.isFullyExpanded() && best.depth < 10) {
            best = best.getByUCB();
        }
        best.Expand();

    }

    /**
     * Expands tree while it can,without returning anything
     */
    public void expandTree(ElapsedCpuTimer elapsedTimer) {
        long ms = elapsedTimer.remainingTimeMillis();
        while (ms > 10) {
            this.expandTree();
            expandcount++;
            ms = elapsedTimer.remainingTimeMillis();
        }
    }

    public boolean isLeaf() {
        return children == null;
    }

    public void value() {
        if (state.isGameOver()) {
            value = FAIL;
        } else if (state.getGameWinner() == Types.WINNER.PLAYER_WINS) {
            value = WIN;
        } else {
            value = (value * visited + state.getGameScore()) / (visited + 1);
        }
    }

    public Node Expand() {
        int selected = 0;
        double best = -1;
        int i = 0;
        for (Node n : children) {
            if (n == null) {
                double random = rnd.nextDouble();
                if (random > best) {
                    best = random;
                    selected = i;
                }
            }
            i++;
        }
        StateObservation newstate = state.copy();
        newstate.advance(Agent.actions[selected]);
        children[selected] = new Node(this, rnd, newstate);
        children[selected].value();
        backpropagate(children[selected]);
        return children[selected];
    }

    public int getByVisits() {
        int best = 0;
        int mostvisits = -1;
        int i = 0;
        double rando = 0;
        for (Node n : children) {
            if (n != null) {
                if (n.visited > mostvisits) {
                    best = i;
                    mostvisits = n.visited;
                    rando = rnd.nextDouble();

                } else if (n.visited == mostvisits) {
                    double x = rnd.nextDouble();
                    if (x > rando) {
                        best = i;
                        rando = x;
                    }
                }

            }
            i++;
        }
        return best;
    }

    public Node getByUCB() {
        Node best = null;
        double bestucb = -Double.MAX_VALUE;
        double rando = 0;
        for (Node n : children) {
            if (n != null) {
                double UCB = n.value + C * Math.sqrt(Math.log(n.parent.visited) / n.visited);
                if (UCB > bestucb) {
                    best = n;
                    bestucb = UCB;
                    if (n.value == Node.FAIL) {
                        rando = -1;
                    } else {
                        rando = rnd.nextDouble();
                    }
                } else if (UCB == bestucb) {
                    double x = rnd.nextDouble();
                    if (x > rando) {
                        best = n;
                        rando = x;
                    }

                }
            }
        }
        return best;

    }

    public boolean isFullyExpanded() {
        for (Node n : children) {
            if (n == null) {
                return false;
            }
        }
        return true;
    }
}
