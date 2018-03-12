package specialGenerals.policies;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Created by marco on 15.04.2016.
 */
public interface IPolicy {

    /**
     * Returns an actions based on a given Policy
     *
     * @param so   State of the game
     * @param time ElapsedTime for this move
     * @return An Action according to a policy
     */
    Types.ACTIONS getAction(StateObservation so, ElapsedCpuTimer time, boolean ignorePruning);

}
