package specialGenerals.algorithms;

import core.game.StateObservation;
import ontology.Types;
import specialGenerals.Config;
import specialGenerals.algorithms.helpers.BFSQueueStruct;
import specialGenerals.algorithms.helpers.KnowledgeBase;
import specialGenerals.algorithms.helpers.Node;
import specialGenerals.heuristics.Hashing;
import specialGenerals.heuristics.IHeuristic;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;


/**
 * Created by ann-sophie on 27.04.16.
 */
public class BFS implements IAlgorithm {

    protected final IHeuristic heuristic;
    protected KnowledgeBase kb;
    protected Node root;
    protected PriorityQueue<BFSQueueStruct> open;
    protected BFSQueueStruct best;
    protected BFSQueueStruct comparator;
    protected boolean foundWin;
    protected final Hashing hasher;
    protected HashMap<Long, Void> visitedStates;
    protected int failedRuns = 0;
    protected List<StateObservation> skipStates;

    public BFS(IHeuristic heuristic, KnowledgeBase kb) {
        this.heuristic = heuristic;
        this.kb = kb;
        foundWin = false;
        this.hasher = new Hashing();
        skipStates = new ArrayList<>();
    }

    @Override
    public void init(StateObservation so, ElapsedCpuTimer time) {
        StateObservation expectedRoot = firstWithoutChange(so, time);
        initRoot(expectedRoot);
        extendTree(time);
        if (Config.GC) {
            Runtime.getRuntime().gc();
        }
    }

    @Override
    public Types.ACTIONS nextAction(StateObservation so, ElapsedCpuTimer time) {
        if (!foundWin) {
            if(skipStates.size() > 0) {
                if (!equal(so, skipStates.get(0))) {
                    kb.setDeterministic(false);
                    Config.log("nonDeterministic (skipState)");
                }
                skipStates.remove(0);
            }else{
                if (!equal(so, root.getState())) {
                    kb.setDeterministic(false);
                    Config.log("nonDeterministic (root)");
                }
            }
            extendTree(time);
            if (Config.GC) {
                Runtime.getRuntime().gc();
            }
            return Types.ACTIONS.ACTION_NIL;
        } else {
            if(skipStates.size() > 0){
                skipStates.remove(0);
                return Types.ACTIONS.ACTION_NIL;
            }
            Node child = ascendToChildOfRoot(best);
            Node parent = child.getParent();
            if (parent != null) {
                Types.ACTIONS action = child.getReachedByAction();
                root = child;
                root.makeRoot();
                return action;
            }
        }
        kb.setBFSFailed(true);
        Config.log("BFS fail, Lösung ungültig");
        return Types.ACTIONS.ACTION_NIL;
    }

    /**
     * @param state1
     * @param state2
     * @return
     */
    private boolean equal(StateObservation state1, StateObservation state2) {
        return hasher.laserHashing(state1) == hasher.laserHashing(state2);
    }

    // TODO: This method may be moved to Node class, as soon as this branch is merged into master
    private Node ascendToChildOfRoot(BFSQueueStruct closed) {
        Node child = closed.getNode();
        Node parent = closed.getNode();

        while (parent.getParent() != null) {
            child = parent;
            parent = parent.getParent();
        }
        return child;
    }

    private void initRoot(StateObservation so) {
        root = Node.getRoot(so.copy(), kb);
        comparator = new BFSQueueStruct(0, 0, root);
        this.open = new PriorityQueue<>(30000, comparator);
        this.visitedStates = new HashMap<>(30000);
        open.add(new BFSQueueStruct(0, 0, root));
        long hash = hasher.positionHashing(root.getState());
        // System.out.println(hash);
        this.visitedStates.put(hash, null);
    }

    private void extendTree(ElapsedCpuTimer time) {
        int counter = 0;
        int maxDepth = 0;
        double avgDepth = 0;
        while (!open.isEmpty() && time.remainingTimeMillis() > Config.SAFE_TIME && !foundWin) {
            // Wenn nur noch wenig Speicher frei ist, soll BFS abbrechen
            long allocatedMemory =
                    (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
            if (presumableFreeMemory < Config.MEMORY_THRESHOLD) {
                kb.setBFSFailed(true);
                Config.log("BFS Memory-Fail");
                return;
            }
            BFSQueueStruct current = open.poll();
            current.getNode().setBFSVisited(true);
            counter++;
            maxDepth = Math.max(maxDepth, current.getDepth());
            avgDepth += current.getDepth();
            if (best != null) {
                if (current.compareTo(best) == -1) {
                    best = current;
                }
            } else {
                best = current;
            }

            StateObservation actualState = current.getNode().getState();
            if (actualState.isGameOver() && actualState.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                Config.log("Found Win at Depth " + current.getDepth());
                best = current;
                foundWin = true;
            }
            if (!actualState.isGameOver()) {
                // Add all possible child BFSStructs to open
                int numChildren = 0;
                boolean usedNil = false;
                for (Types.ACTIONS action : actualState.getAvailableActions(true)) {
                    Node child = current.getNode().getChild(action);
                    if (kb.needsDoubleAction(child.getState(), action)) {
                        if(addNodeToOpenList(child.getChild(action), current.getDepth() + 1, time)){
                            numChildren++;
                        }
                        if(addNodeToOpenList(child.getChild(Types.ACTIONS.ACTION_USE), current.getDepth() + 1, time)){
                            numChildren++;
                        }
                    }else{
                        if(addNodeToOpenList(child,current.getDepth() + 1, time)){
                            numChildren++;
                            if(action == Types.ACTIONS.ACTION_NIL){
                                usedNil = true;
                            }
                        }
                    }
                }
                // Keine Aktionen gefunden, versuche es mit NIL
                if (numChildren == 0) {
                    // Config.log("Trying NIL-Action");
                    Node child = current.getNode().getChild(Types.ACTIONS.ACTION_NIL);
                    if (current.getNode().getParent() == null || current.getNode().getParent().getReachedByAction() != Types.ACTIONS.ACTION_NIL) {
                        double heuristicValue = heuristic.getValue(child.getState(), time);
                        open.add(new BFSQueueStruct(current.getDepth() + 1, heuristicValue, child));
                    }
                }else{
                    if(!usedNil){
                        current.getNode().getChild(Types.ACTIONS.ACTION_NIL).removeState();
                    }
                }
            }

            if (!current.getNode().isRoot()) {
                current.getNode().removeState();
            }

        }
        Config.log("Looked at " + counter + " Nodes\t\twith max Depth " + maxDepth + "\tand avgDepth " + avgDepth / counter + "\twaiting "+ open.size());
        if (counter == 0) {
            failedRuns++;
        } else {
            failedRuns = 0;
        }
        if (failedRuns > 10) {
            kb.setBFSFailed(true);
            Config.log("BFS starvation");
        }
    }

    private boolean addNodeToOpenList(Node child, int newDepth, ElapsedCpuTimer time){
        long hash = hasher.positionHashing(child.getState());
        if (!visitedStates.containsKey(hash) && child.getState().getGameWinner() != Types.WINNER.PLAYER_LOSES) {
            visitedStates.put(hash, null);
            double heuristicValue = heuristic.getValue(child.getState(), time);
            open.add(new BFSQueueStruct(newDepth, heuristicValue, child));
            return true;
        }else{
            if(child.getReachedByAction() != Types.ACTIONS.ACTION_NIL) {
                child.removeState();
            }
            return false;
        }
    }

    String getPath(Node n) {
        if (n.getParent() == null) {
            return "root";
        } else {
            return getPath(n.getParent()) + "->" + n.getReachedByAction().toString().replace("ACTION_", "");
        }
    }

    StateObservation firstWithoutChange(StateObservation so, ElapsedCpuTimer time){
        StateObservation actual = so;
        StateObservation next = kb.measuredAdvance(actual, Types.ACTIONS.ACTION_NIL, true);
        while(hasher.laserHashing(actual) != hasher.laserHashing(next)){
            skipStates.add(actual);
            actual = next;
            next = kb.measuredAdvance(actual, Types.ACTIONS.ACTION_NIL, true);
            if(next.getGameWinner() == Types.WINNER.PLAYER_LOSES){
                kb.setBFSFailed(true);
                Config.log("Inevitable Death while determining laser-Effects");
            }
        }
        Config.log("Found "+ skipStates.size()+ " states to skip");
        return actual;
    }

    public boolean foundWin(){
        return foundWin;
    }

}
