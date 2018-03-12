package AsimovConform.Helper;

import AsimovConform.Agent;
import AsimovConform.KnowledgeBase.KnowledgeBase;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class AsimovState {

    public static int advanceCount;

    /**
     * History of actions, that have already been played.
     */
    public static ArrayList<ACTIONS> actionHistory = new ArrayList<>();
    private static Vector2i worldSize;
    private static boolean worldSizeSet;
    /**
     * History of actions, that have already been played.
     */
    public ArrayList<Vector2d> positionHistory = new ArrayList<Vector2d>(100);
    /**
     * The game state represented by this AsimovState.
     */
    private StateObservation stateObservation;
    /**
     * History of actions, that have been simulated using advance() up to this state.
     */
    private ArrayList<ACTIONS> advanceHistory = new ArrayList<>();
    // Avatar properties
    private Observation avatar;
    private int avatarType;
    private boolean avatarTypeSet;
    private Vector2d avatarPosition;
    private Vector2d avatarOrientation;
    private HashMap<Integer, Integer> avatarResources;
    private int avatarHealthPoints;
    private int avatarMaxHealthPoints;
    private int avatarLimitHealthPoints;
    private ArrayList<Observation>[] NPCPosition;
    private long hashCode;
    private ArrayList<Observation>[] movablePositions;
    private ArrayList<Observation>[] portalsPositions;
    private ArrayList<Observation>[] immovablePositions;
    private Types.WINNER gameWinner;
    private ArrayList<Observation>[] npcPositions;
    private ArrayList<Observation>[] resourcesPositions;

    private boolean avatarResourcesSet;
    private boolean avatarHealthPointsSet;
    private boolean avatarMaxHealthPointsSet;
    private boolean avatarLimitHealthPointsSet;
    private boolean hashCodeSet;
    private boolean moveablePositionsSet;
    private boolean portalsPositionsSet;
    private boolean immovablePositionsSet;
    private boolean gameWinnerSet;
    private boolean npcPositionSet;
    private boolean resourcesPositionsSet;

    // Level properties
    private Dimension worldDimension;
    private int blockSize;
    private boolean blockSizeSet;
    private ArrayList<Observation>[][] observationGrid;
    // HashCode


    public AsimovState(StateObservation stateObservation) {
        this.stateObservation = stateObservation;

        clearValues();
    }


    public AsimovState(StateObservation stateObservation, ArrayList<ACTIONS> advanceHistory, ArrayList<Vector2d> positionHistory) {
        this.stateObservation = stateObservation;
        this.positionHistory = positionHistory;
        this.advanceHistory = advanceHistory;

        clearValues();
    }


    public ArrayList<Vector2d> getPositionHistory() {
        return positionHistory;
    }

    public AsimovState copy() {
        return new AsimovState(stateObservation.copy(), new ArrayList<>(advanceHistory), new ArrayList<>(positionHistory));
    }


    public void advance(ACTIONS action) {
        if (!isGameOver()) {
            advanceHistory.add(action);
            positionHistory.add(stateObservation.getAvatarPosition().copy());
            stateObservation.advance(action);
            clearValues();

            if (Agent.OUTPUT)
                advanceCount++;
            if (Agent.DRAW) {
                Vector2d p = stateObservation.getAvatarPosition();
                //Agent.paintRollouts[Agent.currentRolloutList].add((int) ((int) p.x * 10000 + p.y));
            }
        }
    }

    public AsimovState copyAndAdvance(ACTIONS action) {
        if (!isGameOver()) {
            AsimovState temp = copy();
            temp.advance(action);
            return temp;
        }
        return this;
    }

    private void clearValues() {
        avatar = null;
        avatarTypeSet = false;
        avatarPosition = null;
        avatarOrientation = null;

        blockSizeSet = false;
        observationGrid = null;

        hashCodeSet = false;
        avatarResourcesSet = false;
        avatarHealthPointsSet = false;
        avatarMaxHealthPointsSet = false;
        avatarLimitHealthPointsSet = false;
        moveablePositionsSet = false;
        portalsPositionsSet = false;
        immovablePositionsSet = false;
        gameWinnerSet = false;
        npcPositionSet = false;
        resourcesPositionsSet = false;
    }

    public StateObservation getStateObservation() {
        return stateObservation;
    }

    public void setStateObservation(StateObservation stateObservation) {
        this.stateObservation = stateObservation;
    }

    public boolean isGameOver() {
        return stateObservation.isGameOver();
    }

    public double getGameScore() {
        return stateObservation.getGameScore();
    }

    public boolean isPlayerWinner() {
        return getGameWinner() == WINNER.PLAYER_WINS && isGameOver();
    }

    public boolean isPlayerLooser() {
        return getGameWinner() == WINNER.PLAYER_LOSES && isGameOver();
    }

    public Types.WINNER getGameWinner() {
        if (!gameWinnerSet) {
            gameWinner = stateObservation.getGameWinner();
            gameWinnerSet = true;
        }

        return gameWinner;
    }

    public int getGameTick() {
        return stateObservation.getGameTick();
    }

    public ArrayList<ACTIONS> getAvailableActions() {
        return stateObservation.getAvailableActions();
    }


    public ArrayList<ACTIONS> getAdvanceHistory() {
        return advanceHistory;
    }

    public ACTIONS getLastAction() {
        if (advanceHistory.size() == 0) {
            if (actionHistory.size() == 0) { // Nothing happened so far
                return ACTIONS.ACTION_NIL;
            }
            return actionHistory.get(actionHistory.size() - 1);
        }
        return advanceHistory.get(advanceHistory.size() - 1);
    }

    public ArrayList<Observation>[] getMovablePositions() {
        if (!moveablePositionsSet) {
            movablePositions = stateObservation.getMovablePositions();
            moveablePositionsSet = true;
        }

        return movablePositions;
    }

    public ArrayList<Observation>[] getMovablePositions(Vector2d reference) {
        getMovablePositions();
        if (movablePositions != null)
        for (ArrayList<Observation> movablePosition : movablePositions) {
            for (Observation obs : movablePosition) {
                obs.sqDist = obs.position.sqDist(reference);
            }

            Collections.sort(movablePosition);
        }

        return movablePositions;
    }

    public ArrayList<Observation>[] getMovablePositions(Vector2i reference) {
        reference = reference.mul(getBlockSize());
        return getMovablePositions(new Vector2d(reference.x, reference.y));
    }

    public ArrayList<Observation>[] getPortalsPositions() {
        if (!portalsPositionsSet) {
            portalsPositions = stateObservation.getPortalsPositions();
            portalsPositionsSet = true;
        }

        return portalsPositions;
    }

    public ArrayList<Observation>[] getPortalsPositions(Vector2d reference) {
        getPortalsPositions();
        for (ArrayList<Observation> portalPosition : portalsPositions) {
            for (Observation obs : portalPosition) {
                obs.sqDist = obs.position.sqDist(reference);
            }

            Collections.sort(portalPosition);
        }

        return portalsPositions;
    }

    public ArrayList<Observation>[] getPortalsPositions(Vector2i reference) {
        reference = reference.mul(getBlockSize());
        return getPortalsPositions(new Vector2d(reference.x, reference.y));
    }

    public ArrayList<Observation>[] getImmovablePositions() {
        if (!immovablePositionsSet) {
            immovablePositions = stateObservation.getImmovablePositions();
            immovablePositionsSet = true;
        }

        return immovablePositions;
    }

    public ArrayList<Observation>[] getImmovablePositions(Vector2d reference) {
        getImmovablePositions();
        for (ArrayList<Observation> immovablePosition : immovablePositions) {
            for (Observation obs : immovablePosition) {
                obs.sqDist = obs.position.sqDist(reference);
            }

            Collections.sort(immovablePosition);
        }

        return immovablePositions;
    }

    public ArrayList<Observation>[] getImmovablePositions(Vector2i reference) {
        reference = reference.mul(getBlockSize());
        return getImmovablePositions(new Vector2d(reference.x, reference.y));
    }

    public ArrayList<Observation>[] getNPCPositions() {
        if (!npcPositionSet) {
            npcPositions = stateObservation.getNPCPositions();
            npcPositionSet = true;
        }
        return npcPositions;
    }

    public ArrayList<Observation>[] getNPCPositions(Vector2d reference) {
        return stateObservation.getNPCPositions(reference);
        /*getNPCPositions();
        if (npcPositions != null)
            for (ArrayList<Observation> npcPosition : npcPositions) {
                for (Observation obs : npcPosition) {
                    obs.sqDist = obs.position.sqDist(reference);
                }

                Collections.sort(npcPosition);
            }

        return npcPositions;*/
    }

    public ArrayList<Observation>[] getNPCPositions(Vector2i reference) {
        reference = reference.mul(getBlockSize());
        return getNPCPositions(new Vector2d(reference.x, reference.y));
    }

    public ArrayList<Observation>[] getResourcesPositions() {
        if (!resourcesPositionsSet) {
            resourcesPositions = stateObservation.getResourcesPositions();
            resourcesPositionsSet = true;
        }
        return resourcesPositions;
    }

    public ArrayList<Observation>[] getResourcesPositions(Vector2d reference) {
        getResourcesPositions();
        for (ArrayList<Observation> resourcesPosition : resourcesPositions) {
            for (Observation obs : resourcesPosition) {
                obs.sqDist = obs.position.sqDist(reference);
            }

            Collections.sort(resourcesPosition);
        }

        return resourcesPositions;
    }

    public ArrayList<Observation>[] getResourcesPositions(Vector2i reference) {
        reference = reference.mul(getBlockSize());
        return getResourcesPositions(new Vector2d(reference.x, reference.y));
    }

    public ACTIONS getSecoundLastAction() {
        if (advanceHistory.size() < 2) {
            if (actionHistory.size() < 2) {
                return ACTIONS.ACTION_NIL;
            }
            return actionHistory.get(actionHistory.size() - 2);
        }
        return advanceHistory.get(advanceHistory.size() - 2);
    }

    public ArrayList<ACTIONS> getCompleteActionHistory() {
        ArrayList<ACTIONS> history = new ArrayList<>();
        history.addAll(actionHistory);
        history.addAll(advanceHistory);
        return history;
    }

    public ACTIONS getLastMovementAction() {
        ArrayList<ACTIONS> history = getCompleteActionHistory();
        if (history.size() != 0) {
            for (int i = history.size() - 1; i >= 0; --i) {
                ACTIONS action = history.get(i);
                if (action == ACTIONS.ACTION_DOWN
                        || action == ACTIONS.ACTION_LEFT
                        || action == ACTIONS.ACTION_RIGHT
                        || action == ACTIONS.ACTION_UP) {
                    return action;
                }
            }
        }
        return ACTIONS.ACTION_NIL;
    }

    public ACTIONS getSecondLastMovementAction() {
        ArrayList<ACTIONS> history = getCompleteActionHistory();
        if (history.size() != 0) {
            boolean firstMovementFound = false;
            for (int i = history.size() - 1; i >= 0; --i) {
                ACTIONS action = history.get(i);
                if (action == ACTIONS.ACTION_DOWN
                        || action == ACTIONS.ACTION_LEFT
                        || action == ACTIONS.ACTION_RIGHT
                        || action == ACTIONS.ACTION_UP) {
                    if (firstMovementFound)
                        return action;

                    firstMovementFound = true;
                }
            }
        }
        return ACTIONS.ACTION_NIL;
    }


    // ---
    // Avatar getters
    // ---

    public Observation getAvatar() {
        if (avatar == null) {
            int agentX = (int) getAvatarPosition().x / stateObservation.getBlockSize();
            int agentY = (int) getAvatarPosition().y / stateObservation.getBlockSize();
            int maxType = 0;
            if (agentX >= 0 && agentY >= 0
                    && agentX < getObservationGrid().length && agentY < getObservationGrid()[0].length) {
                for (Observation obs : getObservationGrid()[agentX][agentY]) {
                    if (obs.category == ontology.Types.TYPE_AVATAR) {
                        if (obs.itype > maxType) {
                            avatar = obs;
                            maxType = obs.itype;
                        }
                    }
                }
            }
        }
        return avatar;
    }

    public Vector2d getAvatarPosition() {
        if (avatarPosition == null) {
            avatarPosition = stateObservation.getAvatarPosition();
        }
        return avatarPosition.copy();
    }

    public int getAvatarX() {
        if (avatarPosition == null || !blockSizeSet) {
            avatarPosition = stateObservation.getAvatarPosition();
            blockSize = stateObservation.getBlockSize();
        }
        return (int) avatarPosition.x / blockSize;
    }

    public int getAvatarY() {
        if (avatarPosition == null || !blockSizeSet) {
            avatarPosition = stateObservation.getAvatarPosition();
            blockSize = stateObservation.getBlockSize();
        }
        return (int) avatarPosition.y / blockSize;
    }

    public Vector2i getAvatarGridPosition() {
        return new Vector2i(getAvatarX(), getAvatarY());
    }


    public int getAvatarType() {
        if (!avatarTypeSet) {
            avatarType = stateObservation.getAvatarType();
            avatarTypeSet = true;
        }
        return avatarType;
    }

    public Vector2d getAvatarOrientation() {
        if (avatarOrientation == null) {
            avatarOrientation = stateObservation.getAvatarOrientation();
        }
        return avatarOrientation;
    }

    public HashMap<Integer, Integer> getAvatarResources() {
        if (!avatarResourcesSet) {
            avatarResources = stateObservation.getAvatarResources();
        }

        return avatarResources;
    }

    public int getAvatarHealthPoints() {
        if (!avatarHealthPointsSet) {
            avatarHealthPoints = stateObservation.getAvatarHealthPoints();
        }

        return avatarHealthPoints;
    }

    public int getAvatarMaxHealthPoints() {
        if (!avatarMaxHealthPointsSet) {
            avatarMaxHealthPoints = stateObservation.getAvatarMaxHealthPoints();
        }

        return avatarMaxHealthPoints;
    }

    public int getAvatarLimitHealthPoints() {
        if (!avatarLimitHealthPointsSet) {
            avatarLimitHealthPoints = stateObservation.getAvatarLimitHealthPoints();
        }

        return avatarLimitHealthPoints;
    }
    // ---
    // Avatar getters END
    // ---


    // ---
    // Level getters
    // ---

    public Dimension getWorldDimension() {
        if (worldDimension == null) {
            worldDimension = stateObservation.getWorldDimension();
        }
        return worldDimension;
    }

    public int getBlockSize() {
        if (!blockSizeSet) {
            blockSize = stateObservation.getBlockSize();
            blockSizeSet = true;
        }
        return blockSize;
    }

    public ArrayList<Observation>[][] getObservationGrid() {
        if (observationGrid == null) {
            observationGrid = stateObservation.getObservationGrid();
        }
        return observationGrid;
    }

    public ArrayList<Observation> getObservationGrid(Vector2i position) {
        if (position.x < 0 || position.y < 0 || position.x >= getWorldSize().x || position.y >= getWorldSize().y)
            return null;

        return getObservationGrid()[position.x][position.y];
    }

    public ArrayList<Observation> getObservationGrid(Vector2d position) {
        Vector2i v = new Vector2i(position);
        return getObservationGrid(v.div(getBlockSize()));
    }

    public Vector2i getWorldSize() {
        if (!worldSizeSet) {
            worldSize = new Vector2i(getObservationGrid().length, getObservationGrid()[0].length);
            worldSizeSet = true;
        }

        return worldSize;
    }

    // ---
    // Level getters END
    // ---

    public AsimovState isMovingState() {
        AsimovState as = copy();
        Vector2i pos = new Vector2i(as.getAvatarX(), as.getAvatarY());
        Vector2i pos2, pos3;
        as.advance(ACTIONS.ACTION_NIL);
        pos2 = new Vector2i(as.getAvatarX(), as.getAvatarY());
        if (!pos.equals(pos2) && !as.isGameOver()) {
            return as;
        }
        as.advance(ACTIONS.ACTION_NIL);
        pos3 = new Vector2i(as.getAvatarX(), as.getAvatarY());
        if (!pos2.equals(pos3) && !as.isGameOver()) {
            return as;
        }

        return null;
    }

    public int hashCode() {
        return (int) getHashCode();
    }

    public long getHashCode() {
        if (!hashCodeSet) {
            long prime = 31;
            hashCode = 17;

            //player
            hashCode = hashCode * prime + getAvatarX();
            hashCode = hashCode * prime + getAvatarY();
            hashCode = hashCode * prime + getAvatarType();
            if (Agent.KB.orientationBased) {
                hashCode = hashCode * prime + Double.doubleToLongBits(getAvatarOrientation().x);
                hashCode = hashCode * prime + Double.doubleToLongBits(getAvatarOrientation().y);
            }

            //observations
            for (int i = 0; i < getObservationGrid().length; i++) {
                hashCode = hashCode * prime + i;
                for (int j = 0; j < getObservationGrid()[i].length; j++) {
                    hashCode = hashCode * prime + j;
                    for (Observation obs : getObservationGrid()[i][j]) {
                        if (obs.category != Types.TYPE_AVATAR) {
                            hashCode = hashCode * prime + obs.itype;
                            hashCode = hashCode * prime + obs.category;
                        }
                    }
                }
            }

            //inventory
            for (int itemId : getAvatarResources().keySet()) {
                hashCode = hashCode * prime + itemId;
                hashCode = hashCode * prime + getAvatarResources().get(itemId);
            }

            // health points
            if (getAvatarMaxHealthPoints() > 0) {
                if (getAvatarHealthPoints() != 0) {
                    hashCode = hashCode * prime * getAvatarHealthPoints();
                }
                hashCode = hashCode * prime * getAvatarMaxHealthPoints();
                hashCode = hashCode * prime * getAvatarLimitHealthPoints();
            }

        }
        return hashCode;
    }

    public boolean equals(Object as) {
        return as instanceof AsimovState && as.hashCode() == hashCode();
    }

    public String toString() {
        return "" + hashCode();
    }
}
