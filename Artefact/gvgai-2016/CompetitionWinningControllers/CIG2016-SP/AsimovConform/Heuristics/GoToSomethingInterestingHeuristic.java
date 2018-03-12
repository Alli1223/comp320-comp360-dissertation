package AsimovConform.Heuristics;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Helper.Vector2i;
import AsimovConform.KnowledgeBase.ObjectInfo;
import core.game.Observation;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoToSomethingInterestingHeuristic extends Heuristic {
    private ArrayList<Observation> interestingObservation;
    private int maxDist;

    private boolean allDone;

    private Vector2i nextInterest;

    public GoToSomethingInterestingHeuristic(String id, AsimovState as) {
        super(id);

        nextInterest = null;
        allDone = false;

        interestingObservation = new ArrayList<>(10);
        maxDist = as.getWorldSize().x + as.getWorldSize().y - 2;

        loadObservations(as);

        if (Agent.OUTPUT) {
            System.out.print("GoToSomethingInterestingHeuristic: Found " + interestingObservation.size() + " interesting things at positions ");
            for (Observation obs : interestingObservation)
                System.out.print(new Vector2i(obs.position).div(as.getBlockSize()) + "type=" + obs.itype + " ");
            System.out.print("\n");
        }

    }

    private void loadObservations(AsimovState as) {
        ArrayList<Observation>[] movables = as.getMovablePositions();
        ArrayList<Observation>[] portals = as.getPortalsPositions();
        ArrayList<Observation>[] resources = as.getResourcesPositions();
        ArrayList<Observation>[] immovables = as.getImmovablePositions();

        //movables with less then 4 instances are interesting
        if (movables != null)
            for (ArrayList<Observation> movable : movables) {
                if (!movable.isEmpty() && movable.size() < 4 && inBounds(movable.get(0).position, 0, 0, as.getWorldDimension().width, as.getWorldDimension().height)) {
                    ObjectInfo objectInfo = Agent.KB.gridAnalyser.seenObjects.get(movable.get(0));
                    //if we havent seen the object, lets explore it!
                    if (objectInfo == null || objectInfo.getPositivity() > 0)
                        interestingObservation.add(movable.get(0));
                }
            }
        //immovables with less then 4 instances are interesting
        if (immovables != null)
            for (ArrayList<Observation> immovable : immovables) {
                if (!immovable.isEmpty() && immovable.size() < 4 && inBounds(immovable.get(0).position, 0, 0, as.getWorldDimension().width, as.getWorldDimension().height)) {
                    ObjectInfo objectInfo = Agent.KB.gridAnalyser.seenObjects.get(immovable.get(0));
                    //if we havent seen the object, lets explore it!
                    if (objectInfo == null || objectInfo.getPositivity() > 0)
                        interestingObservation.add(immovable.get(0));
                }
            }
        //every portal is interesting
        if (portals != null)
            for (ArrayList<Observation> portal : portals) {
                if (!portal.isEmpty() && inBounds(portal.get(0).position, 0, 0, as.getWorldDimension().width, as.getWorldDimension().height)) {
                    interestingObservation.add(portal.get(0));
                }
            }
        //every resource is interesting
        if (resources != null)
            for (ArrayList<Observation> resource : resources) {
                if (!resource.isEmpty() && inBounds(resource.get(0).position, 0, 0, as.getWorldDimension().width, as.getWorldDimension().height)) {
                    interestingObservation.add(resource.get(0));
                }
            }
    }

    public boolean isFieldInteresting(Vector2i pos) {
        List<ObjectInfo> obinfo = Agent.KB.gridAnalyser.getInformations(new Vector2d(nextInterest.x, nextInterest.y));
        //not collectable
        if (obinfo.stream().filter(o -> o != null && o.getPositivity() >= 0).count() != 0)
            return true;
        return false;
    }

    private boolean inBounds(Vector2d pos, int minX, int minY, int maxX, int maxY) {
        if (pos.x < minX)
            return false;
        if (pos.x >= maxX)
            return false;
        if (pos.y < minY)
            return false;
        if (pos.y >= maxY)
            return false;

        return true;
    }

    private void sortObservations(Vector2d avatarPos) {
        //good idea to change framework values?
        interestingObservation.forEach(obs -> obs.sqDist = obs.position.sqDist(avatarPos));
        Collections.sort(interestingObservation);
    }

    @Override
    public double evaluate(AsimovState state) {
        if (nextInterest == null)
            return 0;
        double dist = -state.getAvatarGridPosition().manDist(nextInterest);
        if (dist != 0 && isFieldInteresting(nextInterest))//check if it is currently interesting
            return 1 + Math.abs(dist / maxDist);//normalize to [0,1]
        if (interestingObservation.size() > 1 && isFieldInteresting(new Vector2i(interestingObservation.get(1).position).div(state.getBlockSize()))) {
            {
                dist = -state.getAvatarGridPosition().manDist(nextInterest);
                return 1 + Math.abs(dist / maxDist);
            }
        }


        return 0;
    }

    @Override
    public void doPreCalculation(AsimovState as) {

        if (nextInterest != null) {
            List<ObjectInfo> obinfo = Agent.KB.gridAnalyser.getInformations(new Vector2d(nextInterest.x, nextInterest.y));
            //not collectable
            if (obinfo.stream().filter(o -> o != null && o.collectable >= 0).count() == 0)
                //not portal
                if (obinfo.stream().filter(o -> o != null && o.portal >= 0).count() == 0)
                    //if we reached the target in a rollout remove it
                    if (nextInterest.equals(as.getAvatarGridPosition().add(new Vector2i(0, 1))) ||
                            nextInterest.equals(as.getAvatarGridPosition().add(new Vector2i(0, -1))) ||
                            nextInterest.equals(as.getAvatarGridPosition().add(new Vector2i(1, 0))) ||
                            nextInterest.equals(as.getAvatarGridPosition().add(new Vector2i(-1, 0)))) {
                        //System.out.println("removed interest, reached target" + nextInterest + as.getAvatarPosition());
                        nextInterest = null;
                    }
        }


        if (interestingObservation.size() == 0) {
            loadObservations(as);
            if (interestingObservation.size() == 0) {
                if (Agent.OUTPUT)
                    System.out.println("GoToSomethingInterestingHeuristic: Nothing is interesting anymore :(");
                allDone = true;
                return;
            }
        }

        sortObservations(as.getAvatarPosition());

        Observation obs = interestingObservation.get(0);
        nextInterest = new Vector2i(obs.position).div(as.getBlockSize());

        if (Agent.OUTPUT) {
            System.out.println("GoToSomethingInterestingHeuristic: Next Target at Position " + nextInterest);
        }
    }
}
