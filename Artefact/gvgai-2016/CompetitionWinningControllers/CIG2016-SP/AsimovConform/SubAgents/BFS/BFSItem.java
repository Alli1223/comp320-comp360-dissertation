package AsimovConform.SubAgents.BFS;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Heuristics.Heuristic;
import ontology.Types;

import java.util.ArrayList;
import java.util.Random;

class BFSItem implements Comparable<BFSItem> {
    protected AsimovState state;
    private double rating;
    private ArrayList<Long> stateHistory;
    private int loopPosition;

    private static Random random = new Random();

    BFSItem(AsimovState as) {
        state = as;
        rating = 0;
        stateHistory = new ArrayList<>();
        stateHistory.add(hash());
        loopPosition = -1;
    }

    BFSItem(BFSItem old, Types.ACTIONS action, boolean preRun, Heuristic heuristic) {
        state = old.state.copyAndAdvance(action);
        if(preRun) {
            Agent.KB.gridAnalyser.analyse(old.state, state);
        }

        stateHistory = new ArrayList<>(old.stateHistory);

        rating = heuristic.evaluate(state) + (random.nextDouble() * 0.00001);

        if (!state.isGameOver()) {
            loopPosition = stateHistory.indexOf(hash());
            stateHistory.add(hash());
        }
    }

    int getTick() {
        return state.getGameTick();
    }

    Types.ACTIONS getAction(int tick) {
        int size = state.getAdvanceHistory().size();

        if (tick >= size) {
            return null;
        }

        return state.getAdvanceHistory().get(tick);
    }

    double getRealScore() {
        return state.getGameScore();
    }

    int getNumberOfActions() {
        int size = state.getAdvanceHistory().size();
        if (Agent.KB.tickZeroBugHappnend && size > 0) {
            return size - 1;
        }

        return size;
    }

    boolean isWinSolution() {
        return state.isPlayerWinner();
    }

    boolean runIntoDeath() {
        return state.isPlayerLooser();
    }

    boolean isLoopDetected() {
        return !isWinSolution() && loopPosition != -1;
    }

    double getRating() {
        return rating;
    }

    boolean isMovingState() {
        AsimovState newState = state.isMovingState();
        if (newState == null) {
            return false;
        } else {
            state = newState;
            return true;
        }
    }

    long hash() {
        return state.getHashCode();
    }

    @Override
    public int compareTo(BFSItem o) {
        return (int) Math.signum(rating - o.rating);
    }

    public boolean equals(Object o) {
        if (o instanceof BFSItem) {
            BFSItem i = (BFSItem) o;
            return i.state.hashCode() == state.hashCode();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return state.getAdvanceHistory().toString();
    }
}
