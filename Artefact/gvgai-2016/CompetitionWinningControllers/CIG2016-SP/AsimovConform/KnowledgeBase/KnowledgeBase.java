package AsimovConform.KnowledgeBase;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import core.game.Observation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Singleton class for a knowledge base.
 * Use KnowledgeBase.currentKnowledgeBase to do things.
 */
public class KnowledgeBase {

    private Random random = new Random();

    public static KnowledgeBase currentKnowledgeBase = new KnowledgeBase();


    public boolean deterministic;
    public boolean bfsSolvable;
    public boolean tickZeroBugHappnend;

    public boolean useActionExists;

    public boolean orientationBased;

    public GridAnalyser gridAnalyser;

    public boolean gameHasUseAction;

    public boolean[] iTypeFilter;


    private KnowledgeBase() {
    }


    /**
     * Initial setup. Called in Agent constructor. Finds out basic knowledge about the game.
     */
    public void init(AsimovState as, ElapsedCpuTimer elapsedTimer) {
        gridAnalyser = new GridAnalyser(as);

        tickZeroBugHappnend = false;
        isBFSSolvable(as);
        useActionExists(as);
        orientationBased = isOrientationBased(as);
        gameHasUseAction = as.getAvailableActions().contains(Types.ACTIONS.ACTION_USE);
        checkDeterminism(as);

        if (Agent.OUTPUT) {
            System.out.println("Game is deterministic: " + deterministic);
            System.out.println("Game is bfsSolvable: " + bfsSolvable);
            System.out.println("Game is orientation based: " + orientationBased);
        }
    }

    public boolean isOrientationBased(AsimovState as) {
        if ((as.getAvatarOrientation().x == 0.0 && as.getAvatarOrientation().y == 0.0)) {
            return false;
        }
        Types.ACTIONS[] list;

        if (as.getAvatarOrientation().x == 1 && as.getAvatarOrientation().y == 0) {
            list = new Types.ACTIONS[]{Types.ACTIONS.ACTION_DOWN, Types.ACTIONS.ACTION_LEFT, Types.ACTIONS.ACTION_UP, Types.ACTIONS.ACTION_RIGHT};
        } else if (as.getAvatarOrientation().x == 0 && as.getAvatarOrientation().y == -1) {
            list = new Types.ACTIONS[]{Types.ACTIONS.ACTION_RIGHT, Types.ACTIONS.ACTION_DOWN, Types.ACTIONS.ACTION_LEFT, Types.ACTIONS.ACTION_UP};
        } else if (as.getAvatarOrientation().x == -1 && as.getAvatarOrientation().y == 0) {
            list = new Types.ACTIONS[]{Types.ACTIONS.ACTION_UP, Types.ACTIONS.ACTION_RIGHT, Types.ACTIONS.ACTION_DOWN, Types.ACTIONS.ACTION_LEFT};
        } else {
            list = new Types.ACTIONS[]{Types.ACTIONS.ACTION_LEFT, Types.ACTIONS.ACTION_UP, Types.ACTIONS.ACTION_RIGHT, Types.ACTIONS.ACTION_DOWN};
        }

        AsimovState asCopy = as.copy();
        for (Types.ACTIONS action : list) {
            asCopy.advance(action);
            if (asCopy.isGameOver())
                return true;
            if (!as.getAvatarGridPosition().equals(asCopy.getAvatarGridPosition()))
                return false;
        }
        return true;
    }

    private void checkDeterminism(AsimovState as) {
        AsimovState as1 = as.copy();
        AsimovState as2 = as.copy();
        Types.ACTIONS action;

        for (int i = 0; i < 20; i++) {
            // the first halt we just stand and wait
            if (i < 20 / 2)
                action = Types.ACTIONS.ACTION_NIL;
                // the second half the walk in a random sequence
            else
                action = as.getAvailableActions().get(random.nextInt(as.getAvailableActions().size()));

            as1.advance(action);
            as2.advance(action);

            // if the two state are not equal the game is not deterministic
            if (!as1.equals(as2)) {
                deterministic = false;
                return;
            }

            // only need to check one of them because they are equal
            if (as1.isGameOver())
                break;
        }

        deterministic = true;
    }

    public void setITypeFilter(AsimovState oldState, AsimovState newState) {
        // we only analyse the movement of movables and npc
        if (newState.getMovablePositions() != null)
            for (ArrayList<Observation> observations : newState.getMovablePositions()) {
                for (Observation observation : observations) {
                    // if observation is not anymore at the same position like in the old state it has moved
                    if (!getObservationAtPositionWithId(oldState, observation.position, observation.obsID)) {
                        iTypeFilter[observation.itype] = true;
                        break;
                    }
                }
            }
        if (newState.getNPCPositions() != null)
            for (ArrayList<Observation> observations : newState.getNPCPositions()) {
                for (Observation observation : observations) {
                    // if observation is not anymore at the same position like in the old state it has moved
                    if (!getObservationAtPositionWithId(oldState, observation.position, observation.obsID)) {
                        iTypeFilter[observation.itype] = true;
                        break;
                    }
                }
            }
    }

    private boolean getObservationAtPositionWithId(AsimovState state, Vector2d position, int id) {
        if (state != null && position != null) {
            for (Observation observation : state.getObservationGrid(position))
                if (observation.obsID == id)
                    return true;
        } else {
            return true;
        }

        return false;
    }

    public boolean isBFSSolvable(AsimovState as) {
        return isBFSSolvable(as, 1);
    }

    /**
     * checks if bfsSolvable
     *
     * @param as    current state
     * @param tries the number of advances for which the hashcode in the game need to stay the same.
     * @return if it is assumed bfsSolvable
     */
    public boolean isBFSSolvable(AsimovState as, int tries) {
        // quick quit if tickZeroBug happened and boolean is set cause we already did this comparison
        if (tickZeroBugHappnend && as.getGameTick() == 0) {
            return bfsSolvable;
        }
        AsimovState newState = as;
        for (int i = 0; i < tries; i++)
            newState = as.copyAndAdvance(Types.ACTIONS.ACTION_NIL);
        bfsSolvable = as.hashCode() == newState.hashCode();

        // repair bug with different hashCodes in tick 0 and tick 1
        if (!bfsSolvable && as.getGameTick() == 0) {
            AsimovState oldState = as.copyAndAdvance(Types.ACTIONS.ACTION_NIL);
            newState = oldState.copyAndAdvance(Types.ACTIONS.ACTION_NIL);
            bfsSolvable = oldState.hashCode() == newState.hashCode();
            if (bfsSolvable) {
                tickZeroBugHappnend = true;

                if (Agent.OUTPUT)
                    System.out.println("+++ TickZeroBugHappend +++");
            }
        }
        return bfsSolvable;
    }

    private void useActionExists(AsimovState as) {
        useActionExists = as.getAvailableActions().contains(Types.ACTIONS.ACTION_USE);
    }

    public ArrayList<Types.ACTIONS> getAllowedMoves(AsimovState state) {
        ArrayList<Types.ACTIONS> forbiddenactions = new ArrayList<>();

        int x = state.getAvatarX();
        int y = state.getAvatarY();

        int solid = 1;

        for (int i = 0; i < Agent.actions.length; i++) {
            switch (Agent.actions[i]) {
                case ACTION_DOWN:
                    if (x > 0 && y + 1 > 0 && state.getObservationGrid().length > x && state.getObservationGrid()[x].length > y + 1) {
                        for (int ko = 0; ko < state.getObservationGrid()[x][y + 1].size(); ko++) {
                            if (gridAnalyser.seenObjects.get(state.getObservationGrid()[x][y + 1].get(ko).itype) != null &&
                                    gridAnalyser.seenObjects.get(state.getObservationGrid()[x][y + 1].get(ko).itype).solid == solid &&
                                    gridAnalyser.seenObjects.get(state.getObservationGrid()[x][y + 1].get(ko).itype).collectable != 1) {
                                forbiddenactions.add(Agent.actions[i]);
                                //System.out.println("Wand");
                            }
                        }
                    }
                    break;

                case ACTION_LEFT:
                    if (x - 1 > 0 && y > 0 && state.getObservationGrid().length > x - 1 && state.getObservationGrid()[x - 1].length > y) {
                        for (int ko = 0; ko < state.getObservationGrid()[x - 1][y].size(); ko++) {
                            if (gridAnalyser.seenObjects.get(state.getObservationGrid()[x - 1][y].get(ko).itype) != null &&
                                    gridAnalyser.seenObjects.get(state.getObservationGrid()[x - 1][y].get(ko).itype).solid == solid &&
                                    gridAnalyser.seenObjects.get(state.getObservationGrid()[x - 1][y].get(ko).itype).collectable != 1) {
                                forbiddenactions.add(Agent.actions[i]);
                                //System.out.println("Wand");
                            }
                        }
                    }
                    break;

                case ACTION_RIGHT:
                    if (x + 1 > 0 && y > 0 && state.getObservationGrid().length > x + 1 && state.getObservationGrid()[x + 1].length > y) {
                        for (int ko = 0; ko < state.getObservationGrid()[x + 1][y].size(); ko++) {
                            if (gridAnalyser.seenObjects.get(state.getObservationGrid()[x + 1][y].get(ko).itype) != null &&
                                    gridAnalyser.seenObjects.get(state.getObservationGrid()[x + 1][y].get(ko).itype).solid == solid &&
                                    gridAnalyser.seenObjects.get(state.getObservationGrid()[x + 1][y].get(ko).itype).collectable != 1) {
                                forbiddenactions.add(Agent.actions[i]);
                                //System.out.println("Wand");
                            }
                        }
                    }
                    break;

                case ACTION_UP:
                    if (x > 0 && y - 1 > 0 && state.getObservationGrid().length > x && state.getObservationGrid()[x].length > y - 1) {
                        for (int ko = 0; ko < state.getObservationGrid()[x][y - 1].size(); ko++) {
                            if (gridAnalyser.seenObjects.get(state.getObservationGrid()[x][y - 1].get(ko).itype) != null &&
                                    gridAnalyser.seenObjects.get(state.getObservationGrid()[x][y - 1].get(ko).itype).solid == solid &&
                                    gridAnalyser.seenObjects.get(state.getObservationGrid()[x][y - 1].get(ko).itype).collectable != 1) {
                                forbiddenactions.add(Agent.actions[i]);
                                //System.out.println("Wand");
                            }
                        }
                    }
                    break;

                default:
                    break;
                //allowedactions.add(Agent.actions[i]);
            }

        }


        ArrayList<Types.ACTIONS> allowed = new ArrayList<>();

        for (int i = 0; i < Agent.actions.length; i++) {
            if (!forbiddenactions.contains(Agent.actions[i])) {
                allowed.add(Agent.actions[i]);
            }
        }

        if (allowed.size() == 0) {
            Collections.addAll(allowed, Agent.actions);
        }

        return allowed;
    }


    public boolean canStepOn(AsimovState state, int x, int y) {
        //System.out.println(state.getObservationGrid()[x][y].isEmpty());
        return !(x >= 0 && x < state.getObservationGrid().length &&
                y >= 0 && y < state.getObservationGrid()[x].length) || state.getObservationGrid()[x][y].isEmpty();
    }

}
