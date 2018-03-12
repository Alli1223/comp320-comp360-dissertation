package specialGenerals.algorithms.helpers;

import core.game.StateObservation;
import ontology.Types;
import ontology.Types.ACTIONS;
import specialGenerals.Config;
import specialGenerals.heatmaps.VisitedHeatMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Created by marco on 16.04.2016.
 */
public class Node implements Comparable<Node> {

    protected Node parent;
    protected HashMap<Types.ACTIONS, Node> children;
    protected int visited;
    protected double score;
    protected StateObservation so;
    protected boolean BFSVisited;
    protected final Types.ACTIONS reachedByAction;

    protected KnowledgeBase kb;
    protected VisitedHeatMap visitedHeatMap;
    private static final double DISCOUNT_FACTOR = 1;
    protected boolean ignoreNode;

    /**
     * Erstellt einen neuen Knoten mit einem Eltern-Knoten
     *
     * @param parent
     */
    protected Node(Node parent, StateObservation so, KnowledgeBase kb, Types.ACTIONS action) {
        this.parent = parent;
        this.so = so;
        this.kb = kb;
        children = new HashMap<>();
        visited = 0;
        if (parent == null) {
            visitedHeatMap = new VisitedHeatMap(so);
        } else {
            visitedHeatMap = (VisitedHeatMap) parent.getVisitedHeatMap().clone();
        }
        BFSVisited = false;
        this.reachedByAction = action;
        ignoreNode = false;
    }

    /**
     * Erstellt einen neuen Root-Knoten für Baumsuche
     *
     * @return Neuer Root-Knoten
     */
    public static Node getRoot(StateObservation so, KnowledgeBase kb) {
        return new Node(null, so, kb, Types.ACTIONS.ACTION_NIL);
    }

    /**
     * Gibt den gemittelten Score für diesen Knoten inklusive aller Kinder
     * zurück
     *
     * @return Durchschnittswert einer Heuristik
     */
    public double getTotalScore() {
        if (visited == 0) {
            return 0;
        }
        double sumScore = score;
        for (Node n : children.values()) {
            sumScore += DISCOUNT_FACTOR * n.getTotalScore() * n.getVisited();
        }
        return sumScore / visited;
    }

    /**
     * Gibt die Action zurück, die die meisten Punkte verspricht
     *
     * @return Action
     */
    public Types.ACTIONS getBestAction() {
        Types.ACTIONS bestAction = null;
        double bestScore = -Double.MAX_VALUE;
        for (Types.ACTIONS action : children.keySet()) {
            Node n = children.get(action);
            double totalScore = n.getTotalScore();
            if (totalScore > bestScore) {
                bestScore = totalScore;
                bestAction = action;
            }
        }
        if (bestAction == null) {
            throw new IllegalArgumentException("getBestAction failed, Depth = " + getDepthOfTree());
        }
        return bestAction;
    }

    private int getDepthOfTree() {
        int depth = 0;
        Node actual = this;
        while ((actual = actual.parent) != null) {
            depth++;
        }
        return depth;
    }

    /**
     * Steigt im Baum zu einem Kind hinunter, das durch eine Action erzeugt wird
     * Hierbei wird der Zähler, wie häufig dieser Knoten besucht wurde, erhöht
     *
     * @param action Zug, der zu dem Kind-Knoten führt
     * @return Kindknoten
     */
    public Node descendToChild(Types.ACTIONS action) {
        visited += 1;
        return getChild(action);
    }

    /**
     * Gibt den Kindknoten zurück, der durch action erzeugt wird
     *
     * @param action Zug, der zu dem Kindknoten führt
     * @return Kindknoten (nicht für Rollout verwenden, nur bei descendToChild)
     */
    public Node getChild(Types.ACTIONS action) {
        if (children.containsKey(action)) {
            return children.get(action);
        } else {
            StateObservation nextState = kb.measuredAdvance(so, action, true);
            Node child = new Node(this, nextState, kb, action);
            child.getVisitedHeatMap().updateHeatMap(nextState);
            children.put(action, child);
            return child;
        }
    }

    public Node getChild2(ACTIONS action){
        if (children.containsKey(action)) {
            return children.get(action);
        } else {
            Node child = new Node(this, null, kb, action);
            children.put(action, child);
            return child;
        }
    }

    public void enterNode(){
        if(so==null) {
            so = kb.measuredAdvance(parent.getState(), getReachedByAction(), true);
            visitedHeatMap.updateHeatMap(so);
        }
    }
    
    /**
     * Removes a non promising branch to save memory
     */
    public void removeBadBranch() {
        double minScore = Double.MAX_VALUE;
        Node worstNode = null;
        
        // Find child node of root with lowest rating
        for(Entry<ACTIONS, Node> child : children.entrySet()){
            double score = child.getValue().getScore();
            if(score < minScore){
                minScore = score;
                worstNode = child.getValue();
            }
        }
        
        // Check if No worst Node found - this would be wrong!
        if (worstNode == null || minScore == Double.MAX_VALUE) {
            if (Config.DEBUG) {
                throw new RuntimeException("ERROR: No Node found, although we are out of memory");
            }
        }
        Config.log("Removing a bad branch because Free Memory = " + Runtime.getRuntime().freeMemory());
        children.remove(worstNode);
        if (Config.GC) {
            Runtime.getRuntime().gc();
        }
    }

    /**
     * Gibt zurück, ob dieser Knoten ein Blattknoten ohne Kinder ist
     *
     * @return
     */
    public boolean isLeaf() {
        return children.isEmpty() || visited == 0;
    }

    /**
     * Setzt den Score, der durch eine Heuristik ermittelt wurde (zB auch
     * Rollout)
     *
     * @param score Wert der Heuristik
     */
    public void setScore(double score) {
        this.score = score;
    }

    public void backpropagateScore(){
        visited++;
        double score = this.score;
        for(ACTIONS action: children.keySet()){
            score += children.get(action).score * children.get(action).visited;
        }
        this.score = score / visited;
        if(parent != null){
            parent.backpropagateScore();
        }
    }

    /**
     * Gibt den Score zurück, der diesem Knoten von der Heuristik zugewiesen
     * wurde.
     *
     * @return Value der Heuristik
     */
    public double getScore() {
        return score;
    }

    /**
     * Gibt zurück, wie häufig dieser Knoten besucht wurde
     *
     * @return
     */
    public int getVisited() {
        return visited;
    }


    public void setVisited(int value){
        visited = value;
    }

    /**
     * Gibt den Elternknoten zurück
     *
     * @return
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Gibt zurück, ob dieser Knoten die Wurzel des Baumes ist
     *
     * @return
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Macht diesen Knoten zur Wurzel des Baumes, indem der Elternknoten
     * vergessen wird
     */
    public void makeRoot() {
        parent = null;
    }

    /**
     * Gibt zurück, wie der Spielstatus in diesem Knoten ist
     *
     * @return
     */
    public StateObservation getState() {
        return so;
    }

    public void removeState(){
        this.so = null;
    }

    /**
     * @return positive value, iff <code>this</code> has a better totalScore
     */
    @Override
    public int compareTo(Node o) {
        return (int) Math.round(this.getTotalScore() - o.getTotalScore());
    }

    public VisitedHeatMap getVisitedHeatMap() {
        return visitedHeatMap;
    }

    public void setVisitedHeatMap(VisitedHeatMap heatMap){
        visitedHeatMap = heatMap;
    }

    public void setBFSVisited(boolean value) {
        BFSVisited = value;
    }

    public boolean isBFSVisited() {
        return BFSVisited;
    }

    public Types.ACTIONS getReachedByAction() {
        return reachedByAction;
    }

    public ACTIONS getMostVisitedAction() {
        int mostVisited = 0;
        ACTIONS mostVisitedAction = null;
        for(ACTIONS action: children.keySet()){
            Node child = getChild(action);
            if(child.getVisited() > mostVisited && !child.ignoreNode){
                mostVisited = child.getVisited();
                mostVisitedAction = action;
            }
        }
        if(mostVisitedAction == null){
            throw new IllegalArgumentException("There is no visited Child");
        }
        return mostVisitedAction;
    }

    public void setIgnoreNode(boolean value){
        ignoreNode = value;
    }

    public boolean isIgnoreNode(){
        return ignoreNode;
    }
}
