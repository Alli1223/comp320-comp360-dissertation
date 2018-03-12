package specialGenerals.policies;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.List;
import java.util.Random;

/**
 * Created by marco on 15.04.2016.
 */
public class RandomPolicy implements IPolicy {

    protected final Random r;
    protected final IPruner pruner;

    public RandomPolicy(IPruner pruner) {
        r = new Random();
        this.pruner = pruner;
    }

    @Override
    public Types.ACTIONS getAction(StateObservation so, ElapsedCpuTimer time, boolean ignorePruning) {
        List<Types.ACTIONS> actions;
        if(ignorePruning){
            actions = so.getAvailableActions();
        }else{
            actions = pruner.prune(so, so.getAvailableActions());
        }
        return actions.get(r.nextInt(actions.size()));
    }
}
