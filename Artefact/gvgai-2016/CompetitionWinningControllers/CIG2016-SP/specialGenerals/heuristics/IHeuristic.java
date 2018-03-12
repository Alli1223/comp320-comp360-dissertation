package specialGenerals.heuristics;

import core.game.StateObservation;
import tools.ElapsedCpuTimer;

/**
 * Created by marco on 15.04.2016.
 */
public interface IHeuristic {

    /**
     * Liefert einen numerischen Wert, der die Guete eines Zustandes bewertet
     *
     * @param so   Zustand
     * @param time Verbleibende Zeit
     * @return Bewertung
     */
    double getValue(StateObservation so, ElapsedCpuTimer time);

}
