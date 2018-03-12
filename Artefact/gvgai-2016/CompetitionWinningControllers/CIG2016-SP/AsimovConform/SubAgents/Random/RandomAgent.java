package AsimovConform.SubAgents.Random;

import AsimovConform.ActionHeuristics.ActionHeuristic;
import AsimovConform.ActionPruner.ActionPruner;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Heuristics.Heuristic;
import AsimovConform.SubAgents.AsimovAgent;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.Random;

public class RandomAgent extends AsimovAgent {
    private Random rnd;

    public RandomAgent(AsimovState as, Heuristic heuristic, ActionHeuristic actionHeuristic, ActionPruner actionPruner) {
        super(as, heuristic, actionHeuristic, actionPruner);

        rnd = new Random();
    }

    @Override
    public Types.ACTIONS act(AsimovState as, ElapsedCpuTimer timer) {
        Types.ACTIONS nextAction = Types.ACTIONS.ACTION_NIL;
        AsimovState nextState;

        while (timer.remainingTimeMillis() > 10) {
            nextAction = actions.get(rnd.nextInt(actions.size()));

            nextState = as.copyAndAdvance(nextAction);
            if (!nextState.isPlayerLooser()) {
                break;
            }
        }

        return nextAction;
    }

    @Override
    public int evaluate() {
        return -1000;
    }

    @Override
    public void clear() {

    }

    public String toString() {
        return "RandomAgent";
    }
}
