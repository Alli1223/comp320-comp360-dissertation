package specialGenerals.algorithms.helpers;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import specialGenerals.heuristics.Hashing;

import java.util.ArrayList;
import java.util.HashMap;

public class ObjectType {
    /* nichts passiert
     * je haeufiger nichts passiert, desto weniger interessieren wir uns
     */
    //themore nichts passiert, desto weniger interessieren wir uns
    int visited = 0;
    /* themore nichts passiert, desto weniger interessieren wir uns
     */
    int no_action = 0;
    /* die Heuristic die benutzt wird um den Interessenwert zu bestimmen
     * wird als Integer angegeben
     */
    int heuristic;
    /* wie das Leben beeinflusst wird
     * Wert gibt den Lebensgewinn/Verlust an
     */
    int health_change = 0;
    /*ob das Objekt den Weg blockiert
     * the more wir nicht durchlaufen konnten, desto weniger interessant wird es
     */
    int blocking = 0;
    /*
     * ob es unpassierbar ist
     */
    boolean is_blocking = false;
    /*
     * ob es bewegbar aber teilweise unpassierbar ist
     */
    boolean is_blocking_movable = false;
    /*
     * ob direkt gewonnen wird
     */
    boolean victory = false;
    /*
     * ob direkt verloren wird
     */
    boolean defeat = false;
    /*
     * wie oft bereits verloren wurde
     */
    int index_defeat = 0;
    /*
     * wie oft bereits score gefunden wurde
     */
    int index_score = 0;
    /*gibt an, ob sich die Puntkzahl aendert
     * positiv faer positive Veraenderung negativ, faer negative Veraenderung
	 */

    double score_change = 0;
    /*
     * gibt an, ob ein Objekt theoretisch verschoben werden kann, setzt blocking auf 0
     */
    boolean pushable = false;
    /*
     * gibt an, ob ein Objekt zerstoert werden kann
     */
    boolean destroyed_by_use = false;
    /*
     * speichert die Items, die bei Zerstoerung vorhanden waren
     */
    HashMap<Integer, Integer> items_for_destruction = new HashMap<Integer, Integer>();

    boolean destroyed_without_items = false;
    /*
     * gibt an, ob ein Objekt passiert werden kann, wenn man ein bestimmtes Item hat
     * wird nur aktiviert, wenn blocking groesser 0 war
     */
    boolean passable_with_item = false;
    /*
     * speichert die Items, die beim Passieren vorhanden waren
     */
    HashMap<Integer, Integer> items_for_passage = new HashMap<Integer, Integer>();
    /*gibt an, ob man ein Objekt ins Inventar aufnimmt
     * der Wert gibt die Ressource an
     */
    int get_resources = -1;
    /*gibt an, ob man ein Objekt aus dem Inventar verliert
     * der Wert gibt die Ressource an
     */
    int loose_resources = -1;


    public ObjectType(int setheuristic, StateObservation testState, StateObservation nextState, Types.ACTIONS action, Observation obs) {
        setTouchEffect(testState, nextState, action, obs);
        heuristic = setheuristic;
    }

    public ObjectType(StateObservation testState, StateObservation nextState, Types.ACTIONS action, Observation obs) {
        setTouchEffect(testState, nextState, action, obs);
        heuristic = 0;
    }

    public int getValue() {
        return getValueFirstPolicy();
    }

    public int getFirstValueForHeatMap() {
        int value = getValueFirstPolicy();
        if (value > 50) {
            return 2;
        } else if (value > 10) {
            return 1;
        } else if (value < -50) {
            return -2;
        } else if (value < -10) {
            return -1;
        } else {
            return 0;
        }
    }

    private int getValueFirstPolicy() {
        int value = 0;
        if (victory) {
            value += 1000;
        } else if (defeat) {
            value -= 1000;
        }

        value += score_change * 10;
        value += health_change * 10;

        if (get_resources > -1) {
            value += 10;
        }
        if (loose_resources > -1) {
            value += 10;
        }
        value -= blocking;
        value -= no_action;
        return value;
    }


    public void setTouchEffect(StateObservation testState, StateObservation nextState, Types.ACTIONS action, Observation obs) {
        visited += 1;
        Hashing hash = new Hashing();

        Types.WINNER gameWinner = nextState.getGameWinner();

        if (gameWinner == Types.WINNER.PLAYER_WINS) {
            victory = true;
        }
        if (gameWinner == Types.WINNER.PLAYER_LOSES) {
            index_defeat += 1;
            if (index_defeat * 3 > visited) {
                defeat = true;
            } else {
                defeat = false;
            }
            // TODO Was bringt diese Abfrage?
            if (obs.itype == 0 && defeat) {
                index_defeat += 0;
            }
        }
        //Health
        if (testState.getAvatarHealthPoints() != nextState.getAvatarHealthPoints()) {
            health_change = nextState.getAvatarHealthPoints() - testState.getAvatarHealthPoints();
        }
        //Score
        if (testState.getGameScore() != nextState.getGameScore()) {
            index_score += 1;
            if (index_score * 2 > visited) {
                score_change = nextState.getGameScore() - testState.getGameScore();
            } else {
                score_change = 0;
            }
            if (obs.itype == 0 && score_change > 0) {
                index_score += 0;
            }
        }
        //Items
        int nextResources = getTotalResources(nextState);
        int actualResources = getTotalResources(testState);
        if (actualResources < nextResources) {
            get_resources = getnewResources(testState, nextState);
        }
        if (actualResources > nextResources) {
            loose_resources = getusedResources(testState, nextState);
        }
        //Grundtoleranz bei der Wanderkennung eingefuehrt
        if (!passable_with_item && !is_blocking_movable && !is_blocking && samePosition(testState, nextState) &&
                (action == Types.ACTIONS.ACTION_DOWN || action == Types.ACTIONS.ACTION_UP || action == Types.ACTIONS.ACTION_LEFT || action == Types.ACTIONS.ACTION_RIGHT)) {
            blocking += 1;
        }
        if (obs.category == Types.TYPE_STATIC && blocking > 10) {
            is_blocking = true;
        }
        if (obs.category == Types.TYPE_MOVABLE && blocking > 10) {
            is_blocking_movable = true;
        }
        if ((obs.category == Types.TYPE_MOVABLE) &&
                (hash.SimpleHashingMovable(testState) == hash.SimpleHashingMovable(nextState)) &&
                getNumMovables(testState) == getNumMovables(nextState) &&
                samePosition(testState, nextState) &&
                (action == Types.ACTIONS.ACTION_DOWN || action == Types.ACTIONS.ACTION_UP || action == Types.ACTIONS.ACTION_LEFT || action == Types.ACTIONS.ACTION_RIGHT)) {
            pushable = true;
        }

        /*
        if (hash.numberHashing(testState)!=hash.numberHashing(nextState) && removedObjectByUse(testState,nextState, action)){
        	destroyed_by_use =true;
        	if (!destroyed_without_items){
        		if (testState.getAvatarResources()!=null)
        			destroyed_without_items=true;
        		}else{
        			if (items_for_destruction.size()==0){
        				items_for_destruction=testState.getAvatarResources();
        			} else {
        			 //for each resource die leer ist, entferne aus items_for_destruction

        				for (int k :testState.getAvatarResources().keySet()){
        					if (testState.getAvatarResources().get(k)==0){
        						items_for_destruction.remove(k);
        					}
        				}
        		}
        		
        	}*/


        // TODO Sicher, dass die Bedingung so stimmt? (Weder gleiche Position noch Bewegungsaktion)
        if (obs.category == Types.TYPE_STATIC &&
                !(samePosition(testState, nextState) &&
                        (action == Types.ACTIONS.ACTION_DOWN || action == Types.ACTIONS.ACTION_UP || action == Types.ACTIONS.ACTION_LEFT || action == Types.ACTIONS.ACTION_RIGHT))) {
            passable_with_item = true;
            is_blocking = false;
            if (!destroyed_without_items) {
                if (testState.getAvatarResources() != null)
                    destroyed_without_items = true;
            } else {
                if (items_for_passage.size() == 0) {
                    items_for_passage = testState.getAvatarResources();
                } else {
                    //for each resource die leer ist, entferne aus items_for_destruction

                    for (int k : testState.getAvatarResources().keySet()) {
                        if (testState.getAvatarResources().get(k) == 0) {
                            items_for_passage.remove(k);
                        }
                    }
                }

            }
        }

        //no action

        if (blocking == 0 && pushable == false && score_change == 0 && health_change == 0 && victory == false && defeat == false && destroyed_by_use == false && get_resources == -1) {
            no_action += 1;
        } else {
            no_action = 0;
        }

    }

    private int getNumMovables(StateObservation so){
        ArrayList<Observation>[] l = so.getMovablePositions();
        if(l == null){
            return -1;
        }else{
            return l.length;
        }
    }

    private int getTotalResources(StateObservation so) {
        int resources = 0;
        if (so.getAvatarResources() != null) {
            for (int i = 0; i < so.getAvatarResources().size(); i++) {
                for (int k : so.getAvatarResources().values()) {
                    resources += k;
                }

            }
        }
        return resources;
    }

    private int getnewResources(StateObservation testState, StateObservation nextState) {


        for (int k : testState.getAvatarResources().keySet()) {
            if (!nextState.getAvatarResources().containsKey(k)) {
                return k;
            } else if (nextState.getAvatarResources().get(k) != testState.getAvatarResources().get(k)) {
                return k;
            }
        }
        return -1;
    }

    private int getusedResources(StateObservation testState, StateObservation nextState) {


        for (int k : nextState.getAvatarResources().keySet()) {
            if (!testState.getAvatarResources().containsKey(k)) {
                return k;
            } else if (nextState.getAvatarResources().get(k) != testState.getAvatarResources().get(k)) {
                return k;
            }
        }
        return -1;
    }


    private boolean samePosition(StateObservation testState, StateObservation nextState) {
        double x1 = testState.getAvatarPosition().x;
        double y1 = testState.getAvatarPosition().y;
        double x2 = nextState.getAvatarPosition().x;
        double y2 = nextState.getAvatarPosition().y;

        return x1 == x2 && y1 == y2;
    }

    private boolean removedObjectByUse(StateObservation testState, StateObservation nextState, Types.ACTIONS action) {
        if (action != Types.ACTIONS.ACTION_USE) {
            return false;
        }

        double x1 = testState.getAvatarPosition().x;
        double y1 = testState.getAvatarPosition().y;
        double xo = testState.getAvatarOrientation().x;
        double yo = testState.getAvatarOrientation().y;

        double nextfield_x = x1;
        double nextfield_y = y1;
        int blocksize = testState.getBlockSize();

        if (xo > 0) {
            nextfield_x += blocksize;
        } else if (xo < 0) {
            nextfield_x -= blocksize;
        } else if (yo > 0) {
            nextfield_y += blocksize;
        } else if (yo < 0) {
            nextfield_y += blocksize;
        }
        if (testState.getObservationGrid()[(int) nextfield_x / blocksize][(int) nextfield_y / blocksize].equals(nextState.getObservationGrid()[(int) nextfield_x / blocksize][(int) nextfield_y / blocksize])) {
            return false;
        }
        return true;
    }


}
