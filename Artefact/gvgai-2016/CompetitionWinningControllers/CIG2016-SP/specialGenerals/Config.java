package specialGenerals;

import specialGenerals.algorithms.helpers.KnowledgeBase;
import specialGenerals.heuristics.*;
import specialGenerals.policies.*;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public static final boolean LOG_GAME_RESULT = true;
    public static final boolean DEBUG = false;
    public static final boolean LOG = false;
    public static final boolean LOG_TREE_NEEDS_REBUILD = false;
    public static final boolean LOG_OVERSPENT = true;
    
    
    /**
     * wieviele millis vor dem Ende aufgehört werden soll
     */
    public static  int SAFE_TIME = 10; // MUST BE ABOVE ZERO
    public static  long ROLLOUT_NEEDS_EXACT_TIME = SAFE_TIME + 1; // MUST BE ABOVE ZERO
    public static  boolean DRAW = false;
    public static  boolean HEATMAP = true;
    public static  boolean TRACE = true;
    public static  double K = 8.43;
    public static  int maxDepth = 41; // max Rollout tiefe bei MCTS
    public static  boolean OBJECT_DETECTION = true;
    /** When the BFS has not found a solution after 500 ticks, MCTS is used */
    public static  int LATEST_BFS_HANDOVER = 500;
    /** After a curtain field size, MCTS shall be used */
    public static  double MAX_WORLD_DIM_BFS = 400;
    public static  double IGNORE_PRUNING = 0.829;
    public static  boolean MCTS_BEST_ACTION_FROM_UCT = false;
    public static  boolean MCTS_BEST_ACTION_MOST_VISITED = true;

    /** 500 MB freihalten, sonst schmiert es auf dem Server ab */
    public static final long MEMORY_THRESHOLD = 500 * 1024 * 1024;
    /** If there is not enough memory, remove nodes, which have a bad rating */
    public static final boolean LOW_MEMORY_SOLUTION_REMOVE_BRANCH = false;

    /** How many staes are look at parallel to do determinism check of game*/
    public static final int NR_OF_STATES_PARALLEL_DETERMINISM_CHECK = 3;    
    /** Nr of advances used for determine determinism of game */
    public static final int DEPTH_DETERMINISTIC_CHECK = 50;

    /** If Hash function should be local */
    public static final boolean LOCAL_HASH = false;
    /** How far (in sprites) the local hash function should look */
    public static int LOKAL_HASH_DIST = 7;
    public static final boolean GC = true;
    public static double HEATMAP_COOLDOWN = .888;
    

    // Weights of Heuristics
    public static double WEIGHT_Health = 7.36;
    public static double WEIGHT_Score = 9.66;
    public static double WEIGHT_Lose = 26308;
    public static double WEIGHT_win = 17580;
    public static double WEIGHT_NrOfImmovable = -3.97;

    // !deterministic
    public static double WEIGHT_NONDET_NrOfMovable = 4.22;
    public static double WEIGHT_NONDET_NrOfNPC = -3.896;
    public static double WEIGHT_NONDET_HeatMap = 0;
    public static double WEIGHT_NONDET_Noise = .001;
    public static double WEIGHT_NONDET_Depth = -.463;

    // deterministic with MCTS
    public static double WEIGHT_DET_MCTS_NrOfMovable = 4.22;
    public static double WEIGHT_DET_MCTS_HeatMap = 0;
    public static double WEIGHT_DET_MCTS_Noise = .001;
    public static double WEIGHT_DET_MCTS_Depth_Heuristic = -.463;

    // deterministic with BFS
    public static double WEIGHT_DET_BFS_NrOfMovable = -.5;


    public static List<IPruner> getPruners(KnowledgeBase kb) {
        List<IPruner> pruners = new ArrayList<>();
        pruners.add(new BlockPruner(kb));
        pruners.add(new DeathPruner(kb));
        pruners.add(new LastMovementPruner(kb));
        return pruners;
    }

    public static List<HeuristicCombiner.WeightedHeuristic> getHeuristics(int gameTick, boolean deterministic,
                                                                          boolean isSmallField, boolean BFSFailed) {
        List<HeuristicCombiner.WeightedHeuristic> heuristics = new ArrayList<>();
        heuristics.add(new HeuristicCombiner.WeightedHeuristic(new WinHeuristic(), WEIGHT_win));
        heuristics.add(new HeuristicCombiner.WeightedHeuristic(new LoseHeuristic(), WEIGHT_Lose));
        heuristics.add(new HeuristicCombiner.WeightedHeuristic(new ScoreHeuristic(), WEIGHT_Score));
        heuristics.add(new HeuristicCombiner.WeightedHeuristic(new HealthHeuristic(), WEIGHT_Health));
        heuristics.add(new HeuristicCombiner.WeightedHeuristic(new NrOfImmovableHeuristic(), WEIGHT_NrOfImmovable));

        if (!deterministic) {
            heuristics.add(new HeuristicCombiner.WeightedHeuristic(new DepthHeuristic(), WEIGHT_NONDET_Depth));
            heuristics.add(new HeuristicCombiner.WeightedHeuristic(new NoiseHeuristic(), WEIGHT_NONDET_Noise));
            heuristics.add(new HeuristicCombiner.WeightedHeuristic(new HeatMapHeuristic(), WEIGHT_NONDET_HeatMap));
            heuristics.add(new HeuristicCombiner.WeightedHeuristic(new NrOfNPCHeuristik(), WEIGHT_NONDET_NrOfNPC));
            heuristics.add(new HeuristicCombiner.WeightedHeuristic(new NrOfMovablesHeuristic(), WEIGHT_NONDET_NrOfMovable));
        } else {
            if (isSmallField || BFSFailed || gameTick == Config.LATEST_BFS_HANDOVER) {
                heuristics.add(new HeuristicCombiner.WeightedHeuristic(new DepthHeuristic(), WEIGHT_DET_MCTS_Depth_Heuristic));
                heuristics.add(new HeuristicCombiner.WeightedHeuristic(new NoiseHeuristic(), WEIGHT_DET_MCTS_Noise));
                heuristics.add(new HeuristicCombiner.WeightedHeuristic(new HeatMapHeuristic(), WEIGHT_DET_MCTS_HeatMap));
                heuristics.add(new HeuristicCombiner.WeightedHeuristic(new NrOfMovablesHeuristic(), WEIGHT_DET_MCTS_NrOfMovable));
            }else{
                heuristics.add(new HeuristicCombiner.WeightedHeuristic(new NrOfMovablesHeuristic(), WEIGHT_DET_BFS_NrOfMovable));
            }
        }
        return heuristics;
    }

    public static void log(String logMSG) {
        if (LOG) {
            System.out.println(logMSG);
        }
    }
}
