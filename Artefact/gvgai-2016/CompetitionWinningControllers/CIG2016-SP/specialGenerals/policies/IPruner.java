package specialGenerals.policies;

import core.game.StateObservation;
import ontology.Types;

import java.util.List;

/**
 * Created by marco on 26.05.2016.
 */
public interface IPruner {

    /**
     * Pruned unnoetige Aktionen aus einer Liste basierend auf einem State
     *
     * @param so      Aktueller Zustand
     * @param actions Moegliche Aktionen
     * @return Liste mit nicht-unnoetigen Aktionen
     */
    List<Types.ACTIONS> prune(StateObservation so, List<Types.ACTIONS> actions);

}
