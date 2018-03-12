package AsimovConform;

import core.game.StateObservation;
import tools.ElapsedCpuTimer;

public class AgentBFS extends Agent {

    public AgentBFS(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        super(so, elapsedTimer);
        System.out.println("Does not work anymore. Calls now any Agent.");
    }

    public void initSolvers(StateObservation so) {
        //solver.add(new BFS(new AsimovState(so)));
        //solver.add(new RandomAgent(new AsimovState(so)));
    }

}
