package specialGenerals.algorithms.helpers;

import core.game.Observation;
import core.game.StateObservation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Measures the similarity between two given things. Things given may be:
 * states, avatar states,
 *
 * @author jonas
 */
public class Similarity {

    public final static double AVATAR_WEIGHT = 1;
    public final static double SURROUNDING_WEIGHT = 1;

    /**
     * Measures the similarity between states.
     *
     * @param state1
     * @param state2
     * @return If states are very similar, returns 1, if states are not similar
     * returns 0.
     */
    public static double measureState(StateObservation state1, StateObservation state2) {
        if (state1.isGameOver()) {
            return 0.5;
        }
        double difference = 0;
        difference += AVATAR_WEIGHT * 1 / measureAvatar(state1, state2);
        difference += SURROUNDING_WEIGHT * 1 / measureSurrounding(state1, state2);
        return difference;
    }

    /**
     * Measures the similarity between avatars on different states.
     *
     * @param state1
     * @param state2
     * @return If states of avatar are very similar returns 1, otherwise a
     * number closer to 0.
     */
    public static double measureAvatar(StateObservation state1, StateObservation state2) {
        if (state1.isGameOver()) {
            return 0.5;
        }
        double difference = 0;
        difference += Math.abs(state1.getAvatarPosition().dist(state2.getAvatarPosition()));
        difference += Math.abs(state1.getAvatarOrientation().dist(state2.getAvatarOrientation()));
        difference += Math.abs(state1.getAvatarSpeed() - state2.getAvatarSpeed());
        difference += Math.abs(state1.getAvatarHealthPoints() - state2.getAvatarHealthPoints());
        difference += Math.abs(state1.getAvatarType() - state2.getAvatarType());
        difference += Math.abs(differenceAvatarRessources(state1, state2));
        // state1.getFromAvatarSpritesPositions() is not needed
        return 1 / difference;
    }

    /**
     * Measures the similarity between NPCs on different states.
     *
     * @param state1
     * @param state2
     * @return If states of surrounding objects returns 1, otherwise a number
     * closer to 0.
     */
    public static double measureSurrounding(StateObservation state1, StateObservation state2) {
        if (state1.isGameOver()) {
            return 0.5;
        }
        final double observationOnlyInOneState = 42;
        double difference = 0;

        // For every observation id: get difference in position and other
        // properties
        for (int obsId : getObsIds(state1, state2)) {
            Observation o1 = getObservation(obsId, state1);
            Observation o2 = getObservation(obsId, state1);
            if (o1 != null && o2 != null) {
                // Both Observations exist
                difference += o1.position.dist(o2.position);
                // TODO: Kann man noch mehr Informationen aus den Observations
                // entnehmen?
            } else {
                // Observation ist nicht in beiden states enthalten
                // TODO Wie unterschiedlich sind states, in denen der eine NPC
                // nicht
                // mehr vorkommt?
                difference += observationOnlyInOneState;
            }
        }
        return 1 / difference;
    }

    private static Set<Integer> getObsIds(StateObservation state1, StateObservation state2) {
        Set<Integer> ids = new HashSet<>();
        if (state1.getNPCPositions() != null) {
            for (List<Observation> observations : state1.getNPCPositions()) {
                for (Observation o : observations) {
                    ids.add(o.obsID);
                }
            }
        }
        if (state2.getNPCPositions() != null) {
            for (List<Observation> observations : state2.getNPCPositions()) {
                for (Observation o : observations) {
                    ids.add(o.obsID);
                }
            }
        }

        return ids;
    }

    /**
     * @param obsId
     * @param state
     * @return observation with given id, or null if observation is not found
     */
    private static Observation getObservation(int obsId, StateObservation state) {
        for (List<Observation> observations : state.getNPCPositions()) {
            for (Observation o : observations) {
                if (o.obsID == obsId) {
                    return o;
                }
            }
        }

        // No Observation with given id found
        return null;
    }

    private static double differenceAvatarRessources(StateObservation state1, StateObservation state2) {
        double difference = 0;
        for (int ressource : state1.getAvatarResources().keySet()) {
            difference += Math.abs(getRessourceAmount(state1, ressource) - getRessourceAmount(state2, ressource));
        }
        return difference;
    }

    /**
     * @param state1
     * @param ressource
     * @return
     */
    private static Integer getRessourceAmount(StateObservation state1, int ressource) {
        if (state1.getAvatarResources().containsKey(ressource)) {
            return state1.getAvatarResources().get(ressource);
        } else {
            return 0;
        }
    }

}
