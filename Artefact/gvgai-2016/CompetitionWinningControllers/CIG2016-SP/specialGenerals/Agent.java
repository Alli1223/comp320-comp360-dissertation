package specialGenerals;


import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import specialGenerals.algorithms.IAlgorithm;
import specialGenerals.algorithms.helpers.KnowledgeBase;
import specialGenerals.heatmaps.AbstractHeatMap;
import specialGenerals.heatmaps.Position;
import specialGenerals.heuristics.IHeuristic;
import specialGenerals.policies.IMCTSPolicy;
import specialGenerals.policies.IPolicy;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: ssamot Date: 14/11/13 Time: 21:45 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    public static int NUM_ACTIONS;
    public static int ROLLOUT_DEPTH = 50;
    public static double K = Math.sqrt(2);
    public static Types.ACTIONS[] actions;

    protected IAlgorithm algorithm;
    protected IHeuristic heuristic;
    protected IPolicy policy;
    protected IMCTSPolicy mctsPolicy;
    protected KnowledgeBase kb;
    private int blockSize;
    private Vector2d playerPos;
    protected static AbstractHeatMap heatMap;

    protected static IAlgorithm defaultAlgorithm;
    protected static IHeuristic defaultHeuristic;
    protected static IPolicy defaultPolicy;
    protected static IMCTSPolicy defaultMctsPolicy;

    /**
     * Public constructor with state observation and time due.
     *
     * @param so           state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        heatMap = null;
        kb = new KnowledgeBase();
        kb.init(so, elapsedTimer, 500);
        algorithm = kb.getAlgorithm(so);
        algorithm.init(so, elapsedTimer);
        blockSize = so.getBlockSize();
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     *
     * @param stateObs     Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        try {
            playerPos = stateObs.getAvatarPosition();
            if (kb.needsAlgoChange(stateObs)) {
                algorithm = kb.getAlgorithm(stateObs);
                algorithm.init(stateObs, elapsedTimer);
                if (Config.GC) {
                    Runtime.getRuntime().gc();
                }
                return Types.ACTIONS.ACTION_NIL;
            } else {
                return algorithm.nextAction(stateObs, elapsedTimer);
            }
        } catch (Exception ex) {
            if(Config.DEBUG) {
                ex.printStackTrace();
            }
            return Types.ACTIONS.ACTION_NIL;
        }
    }

    /**
     * Function called when the game is over. This method must finish before
     * CompetitionParameters.TEAR_DOWN_TIME, or the agent will be DISQUALIFIED
     *
     * @param stateObservation the game state at the end of the game
     * @param elapsedCpuTimer  timer when this method is meant to finish.
     */
    public void result(StateObservation stateObservation, ElapsedCpuTimer elapsedCpuTimer) {
        // Include your code here to know how it all ended.
        // System.out.println("Game over? " + stateObservation.isGameOver());
        // System.out.println("Time per Advance: "+ kb.getAdvanceNanoTime() + " ns");
        algorithm = null;
        if (Config.GC) {
            Runtime.getRuntime().gc();
        }
    }

    public void draw(Graphics2D g) {
        if (Config.DRAW) {
            if(Config.TRACE){
                for (int i = 0; i < kb.visitedThisRound.size(); i++) {
                    Vector2d pos = kb.visitedThisRound.get(i);
                    g.setColor(new Color(255, 0, 0, 20));
                    if (pos != null && playerPos != null && !(pos.x == playerPos.x && pos.y == playerPos.y)) {
                        g.fillRect((int) pos.x, (int) pos.y, blockSize, blockSize);
                    }
                }
                for (int i = 0; i < kb.visitedThisRoundCopy.size(); i++) {
                    Vector2d pos = kb.visitedThisRoundCopy.get(i);
                    g.setColor(new Color(0, 0, 255, 20));
                    if (pos != null && !(pos.x == playerPos.x && pos.y == playerPos.y)) {
                        if(pos.x<blockSize&&pos.y<blockSize){
                            g.fillRect((int) pos.x + blockSize, (int) pos.y, blockSize, blockSize);
                        }else{
                            g.fillRect((int) pos.x, (int) pos.y, blockSize, blockSize);
                        }
                    }
                }
                kb.visitedThisRound.clear();
                kb.visitedThisRoundCopy.clear();
            }
            if(Config.HEATMAP){
                if(heatMap!=null) {
                    int b4 = (int)(blockSize/4.0);
                    int b2 = (int)(blockSize/2.0);
                    for (int x = 0; x < heatMap.getNrBlocksX(); ++x) {
                        for (int y = 0; y < heatMap.getNrBlocksY(); ++y) {
                            double percent = (heatMap.getHeat(new Position(x, y)) - heatMap.getMin()) / (heatMap.getMax() - heatMap.getMin());
                            g.setColor(new Color(0, 255, 0, (int) (Math.sqrt(percent) * 255)));
                            g.fillOval(x * blockSize + b4, y * blockSize + b4, blockSize - b2, blockSize - b2);
                        }
                    }
                }
            }
        }
    }

    /**
     * Setzt den Default-Algorithmus (null, um es dem Konstruktor zu überlassen)
     *
     * @param algorithm Algorithmus, mit dem gespielt werden soll
     */
    public static void setDefaultAlgorithm(IAlgorithm algorithm) {
        defaultAlgorithm = algorithm;
    }

    /**
     * Setzt die Default-Heuristik (null, um es dem Konstruktor zu überlassen)
     *
     * @param heuristic Heuristik, mit der gespielt werden soll
     */
    public static void setDefaultHeuristic(IHeuristic heuristic) {
        defaultHeuristic = heuristic;
    }

    /**
     * Setzt die Default-Policy (null, um es dem Konstruktor zu überlassen)
     *
     * @param policy Policy, mit der gespielt werden soll
     */
    public static void setDefaultPolicy(IPolicy policy) {
        defaultPolicy = policy;
    }

    /**
     * Setzt die Default-MCTS-Policy (null, um es dem Konstruktor zu überlassen)
     *
     * @param mctsPolicy MCTS-Policy, mit der gespielt werden soll
     */
    public static void setDefaultMctsPolicy(IMCTSPolicy mctsPolicy) {
        defaultMctsPolicy = mctsPolicy;
    }

    public static void setHeatMap(AbstractHeatMap heatMap1){
        heatMap = heatMap1;
    }

}
