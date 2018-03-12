package AsimovConform.SubAgents.MCTS;

import AsimovConform.ActionHeuristics.ActionHeuristic;
import AsimovConform.ActionHeuristics.PreviousMoveActionHeuristic;
import AsimovConform.ActionHeuristics.WeightedActionHeuristicCombiner;
import AsimovConform.ActionPruner.ActionPruner;
import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Heuristics.Heuristic;
import AsimovConform.SubAgents.AsimovAgent;
import AsimovConform.SubAgents.AsimovAgentStatus;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

import java.util.Arrays;
import java.util.Random;

public class SingleMCTSPlayer extends AsimovAgent {

    private int ticksToCheck = 30;
    /**
     * Root of the tree.
     */
    private SingleTreeNode m_root;

    /**
     * Creates the MCTS player with a sampleRandom generator object.
     */
    public SingleMCTSPlayer(AsimovState as, Heuristic heuristic, ActionHeuristic actionHeuristic, ActionPruner actionPruner) {
        super(as, heuristic, actionHeuristic, actionPruner);

        SingleTreeNode.m_rnd = new Random();
    }

    @Override
    public ACTIONS act(AsimovState state, ElapsedCpuTimer elapsedTimer) {
        init(state);
        if (Agent.MCTS_GIVEUPWHENDET_ENABLED)
            if (state.getGameTick() > 100 && state.getGameTick() % ticksToCheck == 0) {//check every 30th tick and dont give up a second time
                //only when no npcs present
                boolean doesntExist = state.getNPCPositions() == null || state.getNPCPositions().length == 0;
                boolean containsNoNPC = state.getNPCPositions() == null || (Arrays.stream(state.getNPCPositions()).filter(l -> l != null && !l.isEmpty()).count() == 0);
                if (doesntExist || containsNoNPC)
                    if (Agent.KB.isBFSSolvable(state, 10)) {//check if nothing happens for 10 ticks
                        ticksToCheck *= 2;
                        status = AsimovAgentStatus.GIVE_UP;
                        if (Agent.OUTPUT)
                            System.out.println(">>MCTS Gave Up because game is bfsSolvable now, hopefully!");
                        return ACTIONS.ACTION_NIL;
                    }
            }

        // Determine the action using MCTS...
        int action = run(elapsedTimer);

        ACTIONS act = Agent.actions[action];
        if (state.copyAndAdvance(act).isPlayerLooser()) {
            return ACTIONS.ACTION_NIL;
        }
        return act;
    }

    @Override
    public int evaluate() {
        return 0;
    }

    @Override
    public void clear() {

    }

    /**
     * Inits the tree with the new observation state in the root.
     *
     * @param state current state of the game.
     */
    public void init(AsimovState state) {
        // Set the game observation to a newly root node.
        m_root = new SingleTreeNode(state, heuristic, actionHeuristic, actionPruner,
                new WeightedActionHeuristicCombiner("mctsactcomb2",
                        new PreviousMoveActionHeuristic("prevmove").getWeightedHeuristic(50.0)
        ));
    }

    /**
     * Runs MCTS to decide the action to take. It does not reset the tree.
     *
     * @param elapsedTimer Timer when the action returned is due.
     * @return the action to execute in the game.
     */
    public int run(ElapsedCpuTimer elapsedTimer) {
        // Do the search within the available time.
        m_root.mctsSearch(elapsedTimer);

        // Determine the best action to take and return it.
        return m_root.mostVisitedAction();
    }

    public String toString() {
        return "MCTS";
    }

}
