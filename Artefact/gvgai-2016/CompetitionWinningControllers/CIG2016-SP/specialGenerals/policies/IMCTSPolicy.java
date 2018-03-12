package specialGenerals.policies;

import ontology.Types;
import specialGenerals.algorithms.helpers.Node;
import tools.ElapsedCpuTimer;

/**
 * Created by marco on 16.04.2016.
 */
public interface IMCTSPolicy {

    /**
     * Wählt eine Nachfolge-Aktion für MCTS aus
     *
     * @param n
     * @param time
     * @return
     */
    Types.ACTIONS getAction(Node n, ElapsedCpuTimer time);

}
