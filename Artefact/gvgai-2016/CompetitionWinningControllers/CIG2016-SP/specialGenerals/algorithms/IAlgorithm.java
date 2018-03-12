package specialGenerals.algorithms;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Created by marco on 15.04.2016.
 */
public interface IAlgorithm {

    /**
     * Initialisiert den Algorithmus, lässt Vorberechnungen zu
     *
     * @param so   Aktueller Zustand
     * @param time Aktuelle Zeit
     */
    void init(StateObservation so, ElapsedCpuTimer time);

    /**
     * Liefert die Aktion zurück, die dem Algorithmus entsprechend am besten passt
     *
     * @param so   Aktueller Zustand
     * @param time Aktuelle Zeit
     * @return Aktion, die am vielversprechendsten ist
     */
    Types.ACTIONS nextAction(StateObservation so, ElapsedCpuTimer time);

}
