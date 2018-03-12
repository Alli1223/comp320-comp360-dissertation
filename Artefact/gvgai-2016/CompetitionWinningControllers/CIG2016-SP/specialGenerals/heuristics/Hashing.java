package specialGenerals.heuristics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import core.game.Observation;
import core.game.StateObservation;
import specialGenerals.Config;
import specialGenerals.heatmaps.Position;

public class Hashing {

    public long SimpleHashing(StateObservation so) {
        long neutralObjects = 0;
        long kindofs = 0;
        int kindofCounter = 1;
        long positions = 0;
        long xpositions = 0;


        ArrayList<Observation>[] immovable = so.getImmovablePositions();
        ArrayList<Observation>[] movable = so.getMovablePositions();
        ArrayList<Observation>[] portals = so.getPortalsPositions();
        ArrayList<Observation>[] resources = so.getResourcesPositions();


        if (immovable != null) {
            neutralObjects += immovable.length;
            for (int i = 0; i < immovable.length; i++) {
                kindofs += immovable[i].size() * kindofCounter;
                kindofCounter += 1;
            }
        }
        if (movable != null) {
            neutralObjects += movable.length;
            for (int i = 0; i < movable.length; i++) {
                kindofs += movable[i].size() * kindofCounter;
                kindofCounter += 1;
                positions += sumUp(movable[i]);
                xpositions += sumUpX(movable[i]);
            }
        }
        if (portals != null) {
            neutralObjects += portals.length;
            for (int i = 0; i < portals.length; i++) {
                kindofs += portals[i].size() * kindofCounter;
                kindofCounter += 1;
                positions += sumUp(portals[i]);
                xpositions += sumUpX(portals[i]);
            }
        }
        if (resources != null) {
            neutralObjects += resources.length;
            for (int i = 0; i < resources.length; i++) {
                kindofs += resources[i].size() * kindofCounter;
                kindofCounter += 1;
                positions += sumUp(resources[i]);
                xpositions += sumUpX(resources[i]);
            }

        }

        return ((positions * 10000 + xpositions) * 100 + kindofs) * 100 + neutralObjects;
    }

    public long SimpleHashingWithAgent(StateObservation so) {
        long neutralObjects = 0;
        long kindofs = 0;
        int kindofCounter = 1;
        long positions = 0;
        long xpositions = 0;


        ArrayList<Observation>[] immovable = so.getImmovablePositions();
        ArrayList<Observation>[] movable = so.getMovablePositions();
        ArrayList<Observation>[] portals = so.getPortalsPositions();
        ArrayList<Observation>[] resources = so.getResourcesPositions();

        positions = (long) ((so.getAvatarPosition().x + so.getAvatarPosition().y) * (so.getAvatarPosition().x + so.getAvatarPosition().y));
        xpositions = (long) (so.getAvatarPosition().x * so.getAvatarPosition().x);


        if (immovable != null) {
            neutralObjects += immovable.length;
            for (int i = 0; i < immovable.length; i++) {
                kindofs += immovable[i].size() * kindofCounter;
                kindofCounter += 1;
            }
        }
        if (movable != null) {
            neutralObjects += movable.length;
            for (int i = 0; i < movable.length; i++) {
                kindofs += movable[i].size() * kindofCounter;
                kindofCounter += 1;
                positions += sumUp(movable[i]);
                xpositions += sumUpX(movable[i]);
            }
        }
        if (portals != null) {
            neutralObjects += portals.length;
            for (int i = 0; i < portals.length; i++) {
                kindofs += portals[i].size() * kindofCounter;
                kindofCounter += 1;
                positions += sumUp(portals[i]);
                xpositions += sumUpX(portals[i]);
            }
        }
        if (resources != null) {
            neutralObjects += resources.length;
            for (int i = 0; i < resources.length; i++) {
                kindofs += resources[i].size() * kindofCounter;
                kindofCounter += 1;
                positions += sumUp(resources[i]);
                xpositions += sumUpX(resources[i]);
            }

        }

        return ((positions * 10000 + xpositions) * 100 + kindofs) * 100 + neutralObjects;
    }

    private long sumUp(ArrayList<Observation> objects) {
        long sum = 0;
        for (int i = 0; i < objects.size(); i++) {
            sum += (objects.get(i).position.x + objects.get(i).position.y) * (objects.get(i).position.x + objects.get(i).position.y);
        }

        return sum;


    }

    private long sumUpX(ArrayList<Observation> objects) {
        long sum = 0;
        for (int i = 0; i < objects.size(); i++) {
            sum += (objects.get(i).position.x) * (objects.get(i).position.x);
        }

        return sum;


    }

    /**
     * Dieses Hashing ist darauf ausgelegt, dass größtenteils Positionen verglichen werden
     *
     * @param so
     * @return
     */
    public long positionHashing(StateObservation so) {
        if(Config.LOCAL_HASH){
            return positionHashingLocal(so);
        }

        long ctr = 17;
        HashMap<Integer, Integer> resources = so.getAvatarResources();
        List<Observation>[][] observations = so.getObservationGrid();
        ctr = shifting(ctr, so.getAvatarPosition().x);
        ctr = shifting(ctr, so.getAvatarSpeed());
        ctr = shifting(ctr, so.getAvatarHealthPoints());
        ctr = shifting(ctr, so.getAvatarType());
        ctr = shifting(ctr, so.getGameScore());
        for (int key : resources.keySet()) {
            ctr = shifting(ctr, key);
            ctr = shifting(ctr, resources.get(key));
        }
        for (int i = 0; i < observations.length; ++i) {
            List<Observation>[] obss = observations[i];
            for (int j = 0; j < obss.length; ++j) {
                List<Observation> obsList = obss[j];
                sortObsList(obsList);
                for (Observation obs : obsList) {
                    ctr = shifting(ctr, obs.obsID);
                    ctr = shifting(ctr, obs.category);
                    ctr = shifting(ctr, obs.itype);
                    ctr = shifting(ctr, i);
                    ctr = shifting(ctr, j);
                }
            }
        }
        ctr = shifting(ctr, so.getAvatarPosition().y);
        return ctr;
    }

    /**
     * Dieses Hashing ist darauf ausgelegt, dass das komische Verhalten von Lasern den Hash nicht beeinflusst
     *
     * @param so
     * @return
     */
    public long laserHashing(StateObservation so) {
        long ctr = 17;
        HashMap<Integer, Integer> resources = so.getAvatarResources();
        List<Observation>[][] observations = so.getObservationGrid();
        ctr = shifting(ctr, so.getAvatarPosition().x);
        ctr = shifting(ctr, so.getAvatarSpeed());
        ctr = shifting(ctr, so.getAvatarHealthPoints());
        ctr = shifting(ctr, so.getAvatarType());
        ctr = shifting(ctr, so.getGameScore());
        for (int key : resources.keySet()) {
            ctr = shifting(ctr, key);
            ctr = shifting(ctr, resources.get(key));
        }
        for (int i = 0; i < observations.length; ++i) {
            List<Observation>[] obss = observations[i];
            for (int j = 0; j < obss.length; ++j) {
                List<Observation> obsList = obss[j];
                sortObsList(obsList);
                for (Observation obs : obsList) {
                    ctr = shifting(ctr, obs.category);
                    ctr = shifting(ctr, obs.itype);
                    ctr = shifting(ctr, i);
                    ctr = shifting(ctr, j);
                }
            }
        }
        ctr = shifting(ctr, so.getAvatarPosition().y);
        return ctr;
    }

    /** 
     * Hashed nur die Objekte, welche höchstens Config.LOKAL_HASH_DIST vom avatar entfernt sind - also nur die lokale Umgebung
     * @param so
     * @return
     */
    public long positionHashingLocal(StateObservation so) {
        long ctr = 17;
        HashMap<Integer, Integer> resources = so.getAvatarResources();
        List<Observation>[][] observations = so.getObservationGrid();
        ctr = shifting(ctr, so.getAvatarPosition().x);
        ctr = shifting(ctr, so.getAvatarSpeed());
        ctr = shifting(ctr, so.getAvatarHealthPoints());
        ctr = shifting(ctr, so.getAvatarType());
        ctr = shifting(ctr, so.getGameScore());
        for (int key : resources.keySet()) {
            ctr = shifting(ctr, key);
            ctr = shifting(ctr, resources.get(key));
        }
        
        Position avatarPos = new Position(so.getAvatarPosition());
        int xMin = Math.max(0, avatarPos.x - Config.LOKAL_HASH_DIST);
        int xMax = Math.min(observations.length, avatarPos.x + Config.LOKAL_HASH_DIST);
        for (int x = xMin; x < xMax; ++x) {
            List<Observation>[] obss = observations[x];
            int yMin = Math.max(0, avatarPos.y - Config.LOKAL_HASH_DIST);
            int yMax = Math.min(obss.length, avatarPos.y + Config.LOKAL_HASH_DIST);
            for (int y = yMin; y < yMax; ++y) {
                List<Observation> obsList = obss[y];
                sortObsList(obsList);
                for (Observation obs : obsList) {
                    ctr = shifting(ctr, obs.obsID);
                    ctr = shifting(ctr, obs.category);
                    ctr = shifting(ctr, obs.itype);
                    ctr = shifting(ctr, x);
                    ctr = shifting(ctr, y);
                }
            }
        }
        ctr = shifting(ctr, so.getAvatarPosition().y);
        return ctr;
    }
    
    private void sortObsList(List<Observation> obsList){
        obsList.sort(new Comparator<Observation>() {
            @Override
            public int compare(Observation o1, Observation o2) {
                if(o1.category == o2.category){
                    if(o1.itype == o2.itype){
                        return Integer.compare(o1.obsID, o2.obsID);
                    }else{
                        return Integer.compare(o1.itype, o2.itype);
                    }
                }else{
                    return Integer.compare(o1.category, o2.category);
                }
            }
        });
    }

    private long shifting(long ctr, double newVal) {
        return shifting(ctr, (long) newVal);
    }

    private long shifting(long ctr, long newVal) {
        long newCtr = 0;
        for (int i = 1; i <= 7; ++i) {
            newCtr ^= (ctr << i) ^ (ctr >> (64 - i));
        }
        return newCtr + newVal;
    }

    public void positionHashingOutput(StateObservation so) {
        HashMap<Integer, Integer> resources = so.getAvatarResources();
        List<Observation>[][] observations = so.getObservationGrid();
        Config.log("Position.x\t" + so.getAvatarPosition().x);
        Config.log("Speed\t" + so.getAvatarSpeed());
        Config.log("Health\t" + so.getAvatarHealthPoints());
        Config.log("Type\t" + so.getAvatarType());
        Config.log("Score\t" + so.getGameScore());
        for (int key : resources.keySet()) {
            Config.log("ResKey\t" + key);
            Config.log("ResCnt\t" + resources.get(key));
        }
        for (int i = 0; i < observations.length; ++i) {
            List<Observation>[] obss = observations[i];
            for (int j = 0; j < obss.length; ++j) {
                List<Observation> obsList = obss[j];
                sortObsList(obsList);
                for (Observation obs : obsList) {
                    Config.log("ObsId\t" + obs.obsID);
                    Config.log("ObsCat\t" + obs.category);
                    Config.log("ObsType\t" + obs.itype);
                    Config.log("Obs.x\t" + i);
                    Config.log("Obs.y\t" + j);
                }
            }
        }
        Config.log("Position.y\t" + so.getAvatarPosition().y);
        Config.log("");
    }

    //Hashed die Positionen der Movable Objects um pushable festzustellen

    public long SimpleHashingMovable(StateObservation so) {
        long neutralObjects = 0;
        long kindofs = 0;
        int kindofCounter = 1;
        long positions = 0;
        long xpositions = 0;


        ArrayList<Observation>[] movable = so.getMovablePositions();

        positions = (long) ((so.getAvatarPosition().x + so.getAvatarPosition().y) * (so.getAvatarPosition().x + so.getAvatarPosition().y));
        xpositions = (long) (so.getAvatarPosition().x * so.getAvatarPosition().x);


        if (movable != null) {
            neutralObjects += movable.length;
            for (int i = 0; i < movable.length; i++) {
                kindofs += movable[i].size() * kindofCounter;
                kindofCounter += 1;
                positions += sumUp(movable[i]);
                xpositions += sumUpX(movable[i]);
            }
        }

        return ((positions * 10000 + xpositions) * 100 + kindofs) * 100 + neutralObjects;
    }

}
