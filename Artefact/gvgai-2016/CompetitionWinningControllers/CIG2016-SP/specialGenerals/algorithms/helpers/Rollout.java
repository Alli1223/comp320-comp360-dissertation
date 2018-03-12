package specialGenerals.algorithms.helpers;

import core.game.StateObservation;
import ontology.Types;
import specialGenerals.Config;
import specialGenerals.heatmaps.VisitedHeatMap;
import specialGenerals.heuristics.IHeuristic;
import specialGenerals.policies.IPolicy;
import tools.ElapsedCpuTimer;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by marco on 15.04.2016.
 */
public class Rollout {

    protected final IHeuristic heuristic;
    protected final IPolicy policy;
    protected final KnowledgeBase kb;

    public Rollout(IHeuristic heuristic, IPolicy policy, KnowledgeBase kb) {
        this.heuristic = heuristic;
        this.policy = policy;
        this.kb = kb;
    }

    /**
     * Spielt ein Spiel basierend auf der Policy zu Ende und gibt den Wert
     * zurück
     *
     * @param node Zustand, ab dem gespielt werden soll
     * @param time Zeit des Zuges
     * @return Wertung des gespielten Spieles basierend auf der Heuristik
     */
    public double rollout(Node node, ElapsedCpuTimer time, boolean ignorePruning) {
        return rollout(node, time, Integer.MAX_VALUE, ignorePruning);
    }

    /**
     * Spielt ein Spiel basierend auf der Policy bis zur gegebenen Tiefe und
     * gibt den Wert zurück
     *
     * @param node     Zustand, ab dem gespielt werden soll
     * @param time     Zeit des Zuges
     * @param maxDepth Maximale Zugtiefe, die gespielt werden soll
     * @return Durchschnittliche Wertung des gespielten Spieles basierend auf der Heuristik
     */
    public double rolloutAllStates(Node node, ElapsedCpuTimer time, int maxDepth, boolean ignorePruning) {
        List<Double> values = new LinkedList<>();
        StateObservation state = node.so.copy();
        VisitedHeatMap backupHeatmap = (VisitedHeatMap) node.getVisitedHeatMap().clone();
        int i = 0;
        while (i < maxDepth && !state.isGameOver() && time.remainingTimeMillis() > Config.SAFE_TIME) {
            node.getVisitedHeatMap().updateHeatMap(state);
            kb.measuredAdvance(state, policy.getAction(state, time, ignorePruning), false);
            values.add(heuristic.getValue(state, time));
            i++;
        }

        // Restore Heatmap of node as it was before the rollout
        node.visitedHeatMap = backupHeatmap;
        return average(values);
    }

    /**
     * Spielt ein Spiel basierend auf der Policy bis zur gegebenen Tiefe und
     * gibt die Werte aller besuchten states zurück
     *
     * @param node     Zustand, ab dem gespielt werden soll
     * @param time     Zeit des Zuges
     * @param maxDepth Maximale Zugtiefe, die gespielt werden soll
     * @return Wertung des gespielten Spieles basierend auf der Heuristik
     */
    public double rollout(Node node, ElapsedCpuTimer time, int maxDepth, boolean ignorePruning) {
        StateObservation state = node.so.copy();
        VisitedHeatMap backupHeatmap = (VisitedHeatMap) node.getVisitedHeatMap().clone();
        int i = 0;
        boolean needsExactTime = time.remainingTimeMillis() < Config.ROLLOUT_NEEDS_EXACT_TIME;
        // Shortcut-Or verhindert Ausführung von Zeitoperation
        while (i < maxDepth && !state.isGameOver() && (!needsExactTime || time.remainingTimeMillis() > Config.SAFE_TIME)) {
            node.getVisitedHeatMap().updateHeatMap(state);
            Types.ACTIONS action = policy.getAction(state, time, ignorePruning);
            kb.measuredAdvance(state, action, false);
            if(kb.needsDoubleAction(state, action)){
                kb.measuredAdvance(state, action, false);
            }
            i++;
        }
        // Restore Heatmap of node as it was before the rollout
        node.visitedHeatMap = backupHeatmap;
        return heuristic.getValue(state, time);
    }

    private double average(List<Double> values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.size();
    }

}
