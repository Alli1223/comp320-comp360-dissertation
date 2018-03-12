package AsimovConform.SubAgents;

import AsimovConform.ActionHeuristics.ActionHeuristic;
import AsimovConform.ActionPruner.ActionPruner;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Heuristics.Heuristic;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;

public abstract class AsimovAgent {
    protected ArrayList<Types.ACTIONS> actions;
    protected AsimovAgentStatus status;

    protected Heuristic heuristic;
    protected ActionHeuristic actionHeuristic;
    protected ActionPruner actionPruner;

    public AsimovAgent(AsimovState as, Heuristic h, ActionHeuristic ah, ActionPruner ap) {
        actions = as.getAvailableActions();
        status = AsimovAgentStatus.IDLE;

        heuristic = h;
        actionHeuristic = ah;
        actionPruner = ap;
    }

    public void heuristicAndPrunerPreCalculation(AsimovState as) {
        if(heuristic != null) {
            heuristic.doPreCalculation(as);
        }

        if(actionHeuristic != null) {
            actionHeuristic.doPreCalculation(as);
        }

        if(actionPruner != null) {
            actionPruner.doPreCalculation(as);
        }
    }

    public AsimovAgentStatus getStatus() {
        return status;
    }

    public void setStatus(AsimovAgentStatus s) {
        status = s;
    }

    public void preAct(AsimovState as, ElapsedCpuTimer timer) {

    }

    public abstract Types.ACTIONS act(AsimovState as, ElapsedCpuTimer timer);

    public abstract int evaluate();

    public abstract void clear();

    public abstract String toString();
}
