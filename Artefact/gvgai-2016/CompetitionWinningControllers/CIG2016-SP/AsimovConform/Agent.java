package AsimovConform;

import AsimovConform.ActionHeuristics.*;
import AsimovConform.ActionPruner.*;
import AsimovConform.Helper.Draw.AsimovDrawer;
import AsimovConform.Helper.Draw.DrawItem;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Heuristics.*;
import AsimovConform.KnowledgeBase.KnowledgeBase;
import AsimovConform.SubAgents.AsimovAgent;
import AsimovConform.SubAgents.AsimovAgentStatus;
import AsimovConform.SubAgents.BFS.BFS;
import AsimovConform.SubAgents.MCTS.SingleMCTSPlayer;
import AsimovConform.SubAgents.Random.RandomAgent;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;

public class Agent extends AbstractPlayer {
    public static final boolean MCTS_GIVEUPWHENDET_ENABLED = true;
    // enable/disable Agent output
    public static boolean OUTPUT = false;
    // enable/disable drawing
    public static boolean DRAW = false;
    public static AsimovDrawer drawer = null;


    public static int NUM_ACTIONS;
    public static int ROLLOUT_DEPTH = 15;
    public static double K = Math.sqrt(2);
    public static ACTIONS[] actions;

    public static KnowledgeBase KB;
    public static int agentInitTime = 20;
    public static boolean switchedSolver = false;
    private AsimovAgent currentPlayer;
    private ArrayList<AsimovAgent> solver;


    /**
     * Public constructor with state observation and time due.
     *
     * @param so           state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        AsimovState as = new AsimovState(so);
        // Get the actions in a static array.
        ArrayList<ACTIONS> act = so.getAvailableActions();
        actions = new ACTIONS[act.size()];
        for (int i = 0; i < actions.length; ++i) {
            actions[i] = act.get(i);
        }
        NUM_ACTIONS = actions.length;


        if (Agent.DRAW)
            drawer = new AsimovDrawer(as.getBlockSize());
        if (Agent.OUTPUT)
            AsimovState.advanceCount = 0;

        // Build the knowledge base
        KB = KnowledgeBase.currentKnowledgeBase;
        KB.init(as.copy(), elapsedTimer);

        Heuristic.initHeuristicProperties();
        ActionHeuristic.initHeuristicProperties();

        Heuristic bfsHeuristic = new SAddCombiner("bfscomb",
                new WinHeuristic("bfs.win").getWeightedHeuristic(1000000.0),
                new ScoreHeuristic("bfs.score").getWeightedHeuristic(100.0),
                new TimeTickHeuristic("bfs.ticks").getWeightedHeuristic(-0.1)
        );

        ActionPruner bfsActionPruner = new ActionPruningCombiner(
                new OrientationBasedMovementPruner(),
                new OrientationBasedUsePruner()
        );

        Heuristic mctsHeuristic = new SAddCombiner("mctscomb",
                new WinHeuristic("win").getWeightedHeuristic(10000000.0),
                new ScoreHeuristic("score").getWeightedHeuristic(1000.0),
                new HeatMapWithCooldownHeuristic("heatmap", 80, as.getWorldSize()).getWeightedHeuristic(-50.0),
                new HeatMapHeuristic("heatmapNoCD", as.getWorldSize()).getWeightedHeuristic(-10.0),
                new KillerHeuristic("kill").getWeightedHeuristic(10),
                new GoToSomethingInterestingHeuristic("interest", as).getWeightedHeuristic(50.0)
        );

        ActionHeuristic mctsActionHeuristic = new WeightedActionHeuristicCombiner("mctsactcomb",
                new PreviousMoveActionHeuristic("prevmove").getWeightedHeuristic(1.0),
                new WallActionHeuristic("wall").getWeightedHeuristic(100.0),
                new EpsilonActionHeuristic("epsilon").getWeightedHeuristic(0.01),
                new OrientationBasedMovementHeuristic("orientationMovement").getWeightedHeuristic(10.0)
        );

        ActionPruner mctsActionPruner = new ActionPruningCombiner(
                new OrientationBasedMovementPruner(),
                new WallPruner(),
                new DeathActionPruner()
        );

        ActionHeuristic mctsCombinedRollActionHeuristic = new WeightedActionHeuristicCombiner("mctsactcomb2",
                new PreviousMoveActionHeuristic("prevmove").getWeightedHeuristic(50.0)
        );


        // Create the player
        solver = new ArrayList<>();

        solver.add(new SingleMCTSPlayer(as.copy(), mctsHeuristic, mctsActionHeuristic, mctsActionPruner));
        solver.add(new BFS(as.copy(), bfsHeuristic, null, bfsActionPruner));
        solver.add(new RandomAgent(as.copy(), null, null, null));

        chooseSolver();

        // let the current player use the remaining constructor time
        currentPlayer.preAct(new AsimovState(so), elapsedTimer);

        if (Agent.OUTPUT)
            System.out.println("Remaining Time after constructor: " + elapsedTimer.remainingTimeMillis() + " | Advances done: " + AsimovState.advanceCount);
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     *
     * @param stateObs     Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        if (Agent.OUTPUT)
            AsimovState.advanceCount = 0;

        AsimovState state = new AsimovState(stateObs);
        switchedSolver = false;

        // if a agent gives up find the next best
        if (currentPlayer.getStatus() == AsimovAgentStatus.GIVE_UP) {
            AsimovAgent oldSolver = currentPlayer;
            currentPlayer.clear();
            chooseSolver();
            oldSolver.setStatus(AsimovAgentStatus.IDLE);

            if (Agent.OUTPUT)
                System.out.println("Switchted to solver " + currentPlayer + ".");
        }

        currentPlayer.heuristicAndPrunerPreCalculation(state);
        ACTIONS action = currentPlayer.act(state, elapsedTimer);

        // if there is enough time, check if player gives up
        if (elapsedTimer.remainingTimeMillis() > 5) {
            if (currentPlayer.getStatus() == AsimovAgentStatus.GIVE_UP) {
                AsimovAgent oldSolver = currentPlayer;
                currentPlayer.clear();
                chooseSolver();
                oldSolver.setStatus(AsimovAgentStatus.IDLE);

                if (Agent.OUTPUT)
                    System.out.println("Switchted to solver " + currentPlayer + ".");

                // if there is even more time, let new solver try
                if (elapsedTimer.remainingTimeMillis() > 10) {
                    action = currentPlayer.act(state, elapsedTimer);
                }
            }
        }

        AsimovState.actionHistory.add(action);

        //if (Agent.OUTPUT)
        //System.out.println("Tick " + stateObs.getGameTick() + " | " + action.toString() + " | Advances " + AsimovState.advanceCount + " | Remaining Time " + elapsedTimer.remainingTimeMillis());
        return action;
    }


    @Override
    public void draw(Graphics2D g) {
        super.draw(g);

        if (!DRAW || drawer == null)
            return;

        drawer.startDrawing();

        int halfBlockSize = drawer.getBlockSize() / 2;

        try {
            for (String id : drawer.getIds())
                for (DrawItem item : drawer.getListToPaint(id)) {
                    g.setColor(item.getColor());
                    g.fillRect(item.x, item.y, drawer.getBlockSize(), drawer.getBlockSize());
                    if(item.getText() != null) {
                        g.setColor(Color.white);
                        g.drawString(item.getText(), item.x + halfBlockSize, item.y + halfBlockSize);
                    }

                }


            for (int x = 0; x < Agent.KB.gridAnalyser.size.getX(); x++)
                for (int y = 0; y < Agent.KB.gridAnalyser.size.y; y++) {
                    boolean loose = Agent.KB.gridAnalyser.getInformations(new Vector2d(x, y)).stream().filter(o -> o.losesgame > 0).count() > 0;
                    boolean solid = Agent.KB.gridAnalyser.getInformations(new Vector2d(x, y)).stream().filter(o -> o.solid > 0).count() > 0;
                    boolean interactable = Agent.KB.gridAnalyser.getInformations(new Vector2d(x, y)).stream().filter(o -> o.collectable > 0 || o.usable > 0).count() > 0;
                    String info = "" + (loose ? "L" : "") + (solid ? "S" : "") + (interactable ? "I" : "");
                    g.setColor(Color.black);
                    g.fillRect(x * drawer.getBlockSize(), y * drawer.getBlockSize(), g.getFontMetrics().stringWidth(info), 10);
                    g.setColor(Color.orange);
                    g.drawString(info, x * drawer.getBlockSize(), 10 + y * drawer.getBlockSize());
                }
        } catch (Exception e) {

        }

        drawer.stopDrawing(true);
    }

    private void chooseSolver() {
        int evalValue = Integer.MIN_VALUE;
        currentPlayer = null;
        switchedSolver = true;

        for (AsimovAgent s : solver) {
            if (s.evaluate() > evalValue && s.getStatus() == AsimovAgentStatus.IDLE) {
                currentPlayer = s;
                evalValue = currentPlayer.evaluate();
            }
        }

        if (currentPlayer != null)
            currentPlayer.setStatus(AsimovAgentStatus.RUN);
    }

}
