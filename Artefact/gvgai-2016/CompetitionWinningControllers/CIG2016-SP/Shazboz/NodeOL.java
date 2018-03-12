package Shazboz;

import core.game.StateObservation;
import java.util.Random;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * MCTS using open loop,which means each state is re-simulated(good for
 * non-deterministic games)
 */
public class NodeOL {

    Random rnd;
    static final double FAIL = -100000;
    static final double WIN = 100000;
    static final double C = Math.sqrt(2.0);
    double value;
    int visited;
    NodeOL parent;
    NodeOL[] children;
    int depth;
    static StateObservation state;
    int expandcount = 0;

    public static void setRootState(StateObservation state) {
        NodeOL.state = state;
    }

    public NodeOL(NodeOL parent, Random rnd) {
        this.rnd = rnd;
        this.parent = parent;
        value = 0.0;
        visited = 1;
        children = new NodeOL[Agent.NUM_ACTIONS];
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

    public void backpropagate(NodeOL n) {
        double value = n.value;
        n = n.parent;
        while (n.parent != null) {
            n.value = (n.value * n.visited + value) / (n.visited + 1);
            n.visited++;
            n = n.parent;
        }
    }

    public void expandTree() {
        NodeOL best = this;
        StateObservation newstate = state.copy();

        while (best.isFullyExpanded() && best.depth < 10) {
            int ucb = best.getByUCB();
            newstate.advance(Agent.actions[ucb]);
            best = best.children[ucb];
        }
        best.Expand(newstate);

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

    public void value(StateObservation state) {
        if (state.isGameOver()) {
            value = FAIL;
        } else if (state.getGameWinner() == Types.WINNER.PLAYER_WINS) {
            value = WIN;
        } else {
            value = (value * visited + state.getGameScore()) / (visited + 1);
        }
    }

    public NodeOL Expand(StateObservation newstate) {
        int selected = 0;
        double best = -1;
        int i = 0;
        for (NodeOL n : children) {
            if (n == null) {
                double random = rnd.nextDouble();
                if (random > best) {
                    best = random;
                    selected = i;
                }
            }
            i++;
        }
        newstate.advance(Agent.actions[selected]);
        children[selected] = new NodeOL(this, rnd);
        children[selected].value(newstate);
        backpropagate(children[selected]);
        return children[selected];
    }

    public int getByVisits() {
        int best = 0;
        int mostvisits = -1;
        int i = 0;
        double rando = 0;
        for (NodeOL n : children) {
            if (n != null) {
                //System.out.println(i+" "+n.visited);
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

    public int getByUCB() {
        int best = 0;
        double bestucb = -Double.MAX_VALUE;
        double rando = 0;
        int i = 0;
        for (NodeOL n : children) {
            if (n != null) {
                double UCB = n.value + C * Math.sqrt(Math.log(n.parent.visited) / n.visited);
                if (UCB > bestucb) {
                    best = i;
                    bestucb = UCB;
                    if (n.value == Node.FAIL) {
                        rando = -1;
                    } else {
                        rando = rnd.nextDouble();
                    }
                } else if (UCB == bestucb) {
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

    public boolean isFullyExpanded() {
        for (NodeOL n : children) {
            if (n == null) {
                return false;
            }
        }
        return true;
    }
}
