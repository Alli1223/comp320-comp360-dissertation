package specialGenerals.algorithms.helpers;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import specialGenerals.Config;
import specialGenerals.algorithms.BFS;
import specialGenerals.algorithms.IAlgorithm;
import specialGenerals.algorithms.MCTS2;
import specialGenerals.heatmaps.Position;
import specialGenerals.heuristics.*;
import specialGenerals.policies.*;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Hier speichern wir so viel Eigenschaften des Spieles wie moeglich, damit die
 * Heuristiken dieses Wissen nutzen koennen. Created by marco on 21.04.2016.
 */
public class KnowledgeBase {

    protected boolean deterministic = true;
    protected boolean orientationBased = false;
    protected double advanceNanoTime = -1;
    protected double maxAdvanceTime = -Double.MAX_VALUE;
    protected int advanced = 0;
    /**
     * Probability of rollout resulting in a win. If probability is high,
     * increasing the score is desirable
     */
    protected double chanceOfWinning = 0;
    protected boolean needsAlgoChange = true;
    protected boolean BFSFailed = false;

    public List<Vector2d> visitedThisRound;
    public List<Vector2d> visitedThisRoundCopy;

    /**
     * Effekt, wenn Avatar gegen dieses Sprite type laeuft
     */
    
   /* Struct asdf;
    {
    	boolean win;
    	boolean lose;
    	boolean addpoint;
    	boolean subpoint;
    	boolean pushforward;
    	boolean blockway;
    	int blocking;
    	boolean health_inc;
    	boolean health_dec;
    	boolean item;
    	boolean destroyed_by_use;
    }*/


    enum Effect {
        /**
         * Es passiert nichts
         */
        NOTHING,
        /**
         * Gewinn durch Beruehrung
         */
        WIN,
        /**
         * Verlieren durch Beruehrung
         */
        LOSE,
        /**
         * Man bekommt Punkte
         */
        ADDPOINT,
        /**
         * Man verliert Punkte
         */
        SUBPOINT,
        /**
         * Objekt wird verschoben
         */
        PUSHFORWARD,
        /**
         * Objekt blockiert den Weg
         */
        BLOCKWAY,
        /**
         * Leben erhoeht sich
         */
        HEALTH_INC,
        /**
         * Leben verringert sich
         */
        HEALTH_DEC,
        /**
         * Wird durch USE zerstoert
         */
        DESTROYED_BY_USE,
        /**
         * Wird ins Inventar aufgenommen
         */
        ITEM
        // TODO Erweitern?
    }

    enum ScoreSystem {
        /**
         * Loose or win
         */
        BOOL,

        /**
         * Score increase through different actions
         */
        INCREMENTAL,

        /**
         * Some actions give small score increase, some actions give score
         * increase of higher magnitude
         */
        DISCONTINUOUS
    }

    enum Termination {
        /**
         * Game ends after a specific number of tics.
         */
        timeout,

        /**
         * Game ends, when avatar reaches certain sprite
         */
        exit,

        /**
         * Game ends, when some number is low/high enough (e.g. 10 resources, 0
         * enemies,...)
         */
        counter
    }

    /**
     * spriteTouchEffect: observation hash, list of effects
     */
    protected HashMap<Long, ObjectType> spriteTouchEffect;

    public KnowledgeBase() {
        spriteTouchEffect = new HashMap<>();
        if (Config.DRAW && Config.TRACE) {
            visitedThisRound = new ArrayList<>(10000);
            visitedThisRoundCopy = new ArrayList<>(10000);
        } else {
            visitedThisRound = new ArrayList<>();
            visitedThisRoundCopy = new ArrayList<>();
        }
    }

    /**
     * Findet folgende Eigenschaften heraus:
     * <ul>
     * <li>dauer eines advance schrittes
     * <li>ob das spiel deterministisch oder stochastisch ist
     * <li>...
     * </ul>
     *
     * @param so
     * @param time
     * @param stopMilis
     */
    public void init(StateObservation so, ElapsedCpuTimer time, int stopMilis) {
        StateObservation nextState = measuredAdvance(so, Types.ACTIONS.ACTION_NIL, true);
        Hashing h = new Hashing();
        
        // Anfangsstates erzeugen für determinismus check
        List<StateObservation> testStates = new ArrayList<>();
        for(int i = 0; i < Config.NR_OF_STATES_PARALLEL_DETERMINISM_CHECK; i++){
            testStates.add(nextState.copy());
        }
        
        //
        for (int i = 0; i < Config.DEPTH_DETERMINISTIC_CHECK && deterministic; i++) {
            nextState = measuredAdvance(nextState, Types.ACTIONS.ACTION_NIL, false);
            long nextHash = h.positionHashing(nextState);
            for (StateObservation testState: testStates){
                testState = measuredAdvance(testState, Types.ACTIONS.ACTION_NIL, false);
                long testHash = h.positionHashing(testState);
                if (testHash != nextHash) {
                    deterministic = false;
                    break;
                }
            }
        }

        int otherDirection = 0;
        Vector2d richtung = so.getAvatarOrientation();
        for(Types.ACTIONS action: so.getAvailableActions()){
            StateObservation orientationTest = measuredAdvance(so, action, true);
            if(richtung.x != orientationTest.getAvatarOrientation().x || richtung.y != orientationTest.getAvatarOrientation().y){
                otherDirection++;
            }
        }
        if(otherDirection>=2){
            orientationBased = true;
            Config.log("Is Orientation Based");
        }

        // TODO Effekte, was bei Beruehrung von Objekten passiert, austesten und
        // speichern

        // TODO Get List of interesting objects
        while (false && time.remainingTimeMillis() > stopMilis + Config.SAFE_TIME) {
            // TODO Advance to each interesting object and test effects
        }
    }

    public boolean needsDoubleAction(StateObservation so, Types.ACTIONS action){
        if(orientationBased){
            if(action == Types.ACTIONS.ACTION_RIGHT && !so.getAvatarOrientation().equals(Types.RIGHT)){
                return true;
            }
            if(action == Types.ACTIONS.ACTION_LEFT && !so.getAvatarOrientation().equals(Types.LEFT)){
                return true;
            }
            if(action == Types.ACTIONS.ACTION_UP && !so.getAvatarOrientation().equals(Types.UP)){
                return true;
            }
            if(action == Types.ACTIONS.ACTION_DOWN && !so.getAvatarOrientation().equals(Types.DOWN)){
                return true;
            }
        }
        return false;
    }

    private ArrayList<Observation> getCollisionObject(StateObservation so, Types.ACTIONS action) {
        Position collision = new Position(so.getAvatarPosition());
        Position orient = new Position(so.getAvatarOrientation());

        int b = so.getBlockSize();

        if (action == Types.ACTIONS.ACTION_DOWN) {
            collision.y += b;
        } else if (action == Types.ACTIONS.ACTION_LEFT) {
            collision.x -= b;
        } else if (action == Types.ACTIONS.ACTION_RIGHT) {
            collision.x += b;
        } else if (action == Types.ACTIONS.ACTION_UP) {
            collision.y -= b;
        } else if (action == Types.ACTIONS.ACTION_USE) {
            if (orient.x > 0) {
                collision.x += b;
            } else if (orient.x < 0) {
                collision.x -= b;
            } else if (orient.y > 0) {
                collision.y += b;
            } else if (orient.y < 0) {
                collision.y -= b;
            }
        }

        long max_x = so.getWorldDimension().width;
        long max_y = so.getWorldDimension().height;
        if (collision.x >= max_x || collision.x < 0 || collision.y >= max_y || collision.y < 0) {
            ArrayList<Observation> emptyList = new ArrayList<>();
            return emptyList;
        }

        ArrayList<Observation> collisions = so.getObservationGrid()[collision.x / b][collision.y / b];
        return collisions;

    }

    /**
     * Misst die Zeit, die ein advance benoetigt.
     *
     * @param so state
     * @param action action
     */
    public StateObservation measuredAdvance(StateObservation so, Types.ACTIONS action, boolean copy) {
        if (copy) {
            long t = System.nanoTime();
            StateObservation clone = so.copy();
            clone.advance(action);
            t = System.nanoTime() - t;
            advanceNanoTime = (t + advanceNanoTime * advanced) / (double) (advanced + 1);
            maxAdvanceTime = Math.max(maxAdvanceTime, t);
            advanced++;

            if(Config.OBJECT_DETECTION){
                startObjectDetection(so, clone, action);
            }
            if (Config.DRAW && Config.TRACE) {
                visitedThisRoundCopy.add(clone.getAvatarPosition());
            }
            return clone;
        } else {
            long t = System.nanoTime();
            so.advance(action);
            t = System.nanoTime() - t;
            advanceNanoTime = (t + advanceNanoTime * advanced) / (double) (advanced + 1);
            maxAdvanceTime = Math.max(maxAdvanceTime, t);
            advanced++;
            if (Config.DRAW && Config.TRACE) {
                visitedThisRound.add(so.getAvatarPosition());
            }
            return so;
        }
    }

    public boolean isDeterministic() {
        return deterministic;
    }

    /**
     * @return type of the score system.
     */
    public ScoreSystem getScoreSystem() {
        // TODO
        return null;
    }

    /**
     * @return type of termination.
     */
    public Termination getTermination() {
        // TODO
        return null;
    }

    /**
     * @return true, if resources play a role in this game
     */
    public boolean resurcesExist() {
        // TODO
        return false;
    }

    /**
     * @param observation
     * @return was passiert, wenn der Avatar dagegen laeuft.
     */
    public ObjectType getEffectOf(Observation observation) {
        return spriteTouchEffect.get(getObservationHash(observation));
    }

    private Long getObservationHash(Observation observation) {
        return (long) observation.category * 100 + observation.itype;
    }

    /**
     * @return dauer eines advance Schrittes in Nanosekunden
     */
    public double getAdvanceNanoTime() {
        return advanceNanoTime;
    }

    public IAlgorithm getAlgorithm(StateObservation so) {
        needsAlgoChange = false;
        IPruner pruner = new MultiPruner(Config.getPruners(this));
        IPolicy policy = new HeatMapPolicy(pruner);
        boolean isBigField = isBigField(so);
        int gameTick = so.getGameTick();
        IHeuristic heuristic = new HeuristicCombiner(
                Config.getHeuristics(gameTick, deterministic, isBigField, BFSFailed));

        List<IPruner> mctsPruners = new ArrayList<>();
        mctsPruners.add(new PositionPruner());
        mctsPruners.add(new LastMovementPruner(this));
        IMCTSPolicy mctsPolicy = new UCT_MCTSPolicy2(Config.K, this, new MultiPruner(mctsPruners));

        if (!deterministic) {
            Config.log("!deterministic => MCTS");
            return new MCTS2(heuristic, policy, mctsPolicy, this, Config.maxDepth);
        } else {
            if (isBigField || BFSFailed || gameTick == Config.LATEST_BFS_HANDOVER) {
                Config.log("deterministic && (bigField || BFSFail || GameTick > Config.LATEST_BFS_HANDOVER) => MCTS");
                return new MCTS2(heuristic, policy, mctsPolicy, this, Config.maxDepth+5);
            } else {
                Config.log("deterministic && !bigField => BFS");
                return new BFS(heuristic, this);
            }
        }

    }

    public boolean isOrientationBased(){
        return orientationBased;
    }

    public boolean needsAlgoChange(StateObservation so) {
        if (!isBigField(so) && !BFSFailed && so.getGameTick() == Config.LATEST_BFS_HANDOVER && deterministic) {
            needsAlgoChange = true;
        }
        return needsAlgoChange;
    }

    public void setDeterministic(boolean value) {
        if (value == false && deterministic == true) {
            needsAlgoChange = true;
        }
        deterministic = value;
    }

    public void setBFSFailed(boolean value) {
        if (value != BFSFailed) {
            needsAlgoChange = true;
            BFSFailed = value;
        }
    }

    private boolean isBigField(StateObservation so) {
        Dimension d = so.getWorldDimension();
        double blockSize = so.getBlockSize();
        return d.height / blockSize * d.width / blockSize > Config.MAX_WORLD_DIM_BFS;
    }

    public void startObjectDetection(StateObservation so, StateObservation clone, Types.ACTIONS action) {
        if (Config.OBJECT_DETECTION) {
            ArrayList<Observation> observations = getCollisionObject(so, action);
            for (int i = 0; i < observations.size(); i++) {
                Observation obs = observations.get(i);
                if (obs != null && obs.category != 0) {
                    Long hash = getObservationHash(obs);
                    if (spriteTouchEffect.containsKey(hash)) {
                        spriteTouchEffect.get(hash).setTouchEffect(so, clone, action, obs);
                    } else {
                        ObjectType obj = new ObjectType(so, clone, action, obs);
                        spriteTouchEffect.put(hash, obj);
                    }
                }

            }
        }
    }

    private boolean isImpassible(ArrayList<Observation> obs, ArrayList<Observation> obs_next) {
        if (obs == null) {
            return false;
        }
        long hash;
        for (int i = 0; i < obs.size(); i++) {
            hash = getObservationHash(obs.get(i));
            if (spriteTouchEffect.containsKey(hash)) {

                // nur fuers debuggen
                ObjectType obj = spriteTouchEffect.get(hash);

                if (spriteTouchEffect.get(hash).is_blocking) {
                    return true;
                }

                // falls es blockiert aber pushable ist, naechstes Feld danach
                // mitpruefen

                if (spriteTouchEffect.get(hash).pushable) {

                    for (int j = 0; j < obs_next.size(); j++) {
                        long hash_next;
                        hash_next = getObservationHash(obs_next.get(j));
                        if (spriteTouchEffect.containsKey(hash_next)) {

                            if (spriteTouchEffect.get(hash_next).is_blocking
                                    || spriteTouchEffect.get(hash_next).is_blocking_movable) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isDeadly(ArrayList<Observation> obs) {
        if (obs == null) {
            return false;
        }
        long hash;
        for (int i = 0; i < obs.size(); i++) {
            hash = getObservationHash(obs.get(i));
            if (spriteTouchEffect.containsKey(hash)) {

                // nur fuers debuggen
                ObjectType obj = spriteTouchEffect.get(hash);

                if (spriteTouchEffect.get(hash).defeat) {
                    return true;
                }
            }
        }
        return false;
    }

    private ArrayList<Observation> getField(StateObservation so, int x, int y) {

        int blocksize = so.getBlockSize();

        double max_x = so.getWorldDimension().getWidth() / blocksize - 1;
        double max_y = so.getWorldDimension().getHeight() / blocksize - 1;
        double x1 = so.getAvatarPosition().x / blocksize;
        double y1 = so.getAvatarPosition().y / blocksize;

        double field_x = Math.max(0, Math.min(max_x, x1 + x));
        double field_y = Math.max(0, Math.min(max_y, y1 + y));

        return so.getObservationGrid()[(int) field_x][(int) field_y];
    }

    /**
     * @return the list of all actions which does not make you walk into a wall
     */
    public List<Types.ACTIONS> getBlockPrunedActions(StateObservation so, List<Types.ACTIONS> goodActions) {

        // nur zum debuggen
        ArrayList<Observation>[][] obs = so.getObservationGrid();

        if (isImpassible(getField(so, 0, 1), getField(so, 0, 2))) {
            goodActions.remove(Types.ACTIONS.ACTION_DOWN);
        }
        if (isImpassible(getField(so, 0, -1), getField(so, 0, -2))) {
            goodActions.remove(Types.ACTIONS.ACTION_UP);
        }
        if (isImpassible(getField(so, 1, 0), getField(so, 2, 0))) {
            goodActions.remove(Types.ACTIONS.ACTION_RIGHT);
        }
        if (isImpassible(getField(so, -1, 0), getField(so, -2, 0))) {
            goodActions.remove(Types.ACTIONS.ACTION_LEFT);
        }

        return goodActions;
    }

    /**
     * @return the list of all actions which wont kill you
     */
    public List<Types.ACTIONS> getDeathPrunedActions(StateObservation so, List<Types.ACTIONS> goodActions) {

        // nur zum debuggen
        ArrayList<Observation>[][] obs = so.getObservationGrid();

        if (isDeadly(getField(so, 0, 1))) {
            goodActions.remove(Types.ACTIONS.ACTION_DOWN);
        }
        if (isDeadly(getField(so, 0, -1))) {
            goodActions.remove(Types.ACTIONS.ACTION_UP);
        }
        if (isDeadly(getField(so, 1, 0))) {
            goodActions.remove(Types.ACTIONS.ACTION_RIGHT);
        }
        if (isDeadly(getField(so, -1, 0))) {
            goodActions.remove(Types.ACTIONS.ACTION_LEFT);
        }

        return goodActions;
    }

    /**
     * @return the list of all actions except the undo of the last movement
     *         action, if we have at least one movement action left
     */
    public List<Types.ACTIONS> getLastMovePrunedActions(StateObservation so, List<Types.ACTIONS> goodActions) {
        Types.ACTIONS lastaction = so.getAvatarLastAction();

        int leftMoves = goodActions.size();
        if (goodActions.contains(Types.ACTIONS.ACTION_NIL)) {
            leftMoves -= 1;
        }
        if (goodActions.contains(Types.ACTIONS.ACTION_USE)) {
            leftMoves -= 1;
        }
        if (goodActions.contains(Types.ACTIONS.ACTION_ESCAPE)) {
            leftMoves -= 1;
        }
        if (leftMoves > 0 && lastaction == Types.ACTIONS.ACTION_LEFT) {
            goodActions.remove(Types.ACTIONS.ACTION_RIGHT);
        }
        if (leftMoves > 0 && lastaction == Types.ACTIONS.ACTION_RIGHT) {
            goodActions.remove(Types.ACTIONS.ACTION_LEFT);
        }
        if (leftMoves > 0 && lastaction == Types.ACTIONS.ACTION_DOWN) {
            goodActions.remove(Types.ACTIONS.ACTION_UP);
        }
        if (leftMoves > 0 && lastaction == Types.ACTIONS.ACTION_UP) {
            goodActions.remove(Types.ACTIONS.ACTION_DOWN);
        }

        return goodActions;
    }
}
