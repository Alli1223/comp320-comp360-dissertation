package AsimovConform;

import AsimovConform.Helper.AsimovState;
import AsimovConform.SubAgents.MCTS.SingleMCTSPlayer;
import core.game.StateObservation;
import tools.ElapsedCpuTimer;

public class AgentMCTS extends Agent {

    public AgentMCTS(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        super(so, elapsedTimer);
        System.out.println("Does not work anymore. Calls now any Agent.");
    }

    public void initSolvers(StateObservation so) {
        //solver.add(new SingleBrFSPlayer(new AsimovState(so)));
    }

}
