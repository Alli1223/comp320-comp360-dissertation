package AsimovConform;

import AsimovConform.Helper.AsimovState;
import AsimovConform.SubAgents.Random.RandomAgent;
import core.game.StateObservation;
import tools.ElapsedCpuTimer;

public class AgentRandom extends Agent {

    public AgentRandom(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        super(so, elapsedTimer);
        System.out.println("Does not work anymore. Calls now any Agent.");
    }

    public void initSolvers(StateObservation so) {
        //solver.add(new RandomAgent(new AsimovState(so)));
    }

}
