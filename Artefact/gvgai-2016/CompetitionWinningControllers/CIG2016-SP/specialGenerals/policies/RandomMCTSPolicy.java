package specialGenerals.policies;

import ontology.Types;
import specialGenerals.algorithms.helpers.Node;
import tools.ElapsedCpuTimer;


/**
 * Created by marco on 16.04.2016.
 */
public class RandomMCTSPolicy implements IMCTSPolicy {

    protected final RandomPolicy randomPolicy;
    protected final IPruner pruner;

    public RandomMCTSPolicy(IPruner pruner) {
        this.pruner = pruner;
        this.randomPolicy = new RandomPolicy(pruner);
    }

    @Override
    public Types.ACTIONS getAction(Node n, ElapsedCpuTimer time) {
        return randomPolicy.getAction(n.getState(), time, false);
    }
}
