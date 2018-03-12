package AsimovConform.KnowledgeBase;

import AsimovConform.Agent;
import ontology.Types;

/**
 * Created by Admin on 10.05.2016.
 */
public class NPCInfo {
    public int name;
    public int winsgame;
    public int losesgame;
    public int distance;
    public double loselife;
    public double score;
    public int killabel;
    public Types.ACTIONS action;


    public NPCInfo(int name) {
        this.name = name;
        winsgame = 0;
        losesgame = 0;
        loselife = 0;
        distance = 0;
        killabel = 0;
        score = 0;
        action = null;
    }

    public void death() {
        losesgame = 1;

    }

    public void win() {
        winsgame = 1;
    }

    public void looslife(int beforLife, int afterLife) {
        loselife = beforLife - afterLife;

    }

    public void kill(double beforPoints, double afterPoints, Types.ACTIONS action) {
        killabel = 1;
        score = afterPoints - beforPoints;
        this.action = action;
    }

    public void noInterAction() {
        winsgame = -1;
        losesgame = -1;
        loselife = -1;
        killabel = -1;
    }

    public String toString() {
        return name + "\n" +
                "killabel: " + killabel + " with action " + action + "\n" +
                "distance: " + distance + "\n" +
                "winsgame: " + winsgame + "\n" +
                "losesgame: " + losesgame + "\n" +
                "loselife: " + loselife + "\n" +
                "score: " + score + "\n";
    }
}
