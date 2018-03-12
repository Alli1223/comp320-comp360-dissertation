package AsimovConform.KnowledgeBase;

import ontology.Types;
import tools.Vector2d;


public class ObjectInfo {

    public int name;
    public int winsgame;
    public int losesgame;
    public double life;
    public double score;
    public int solid;
    public int moveable;
    public int distance;
    public int collectable;
    public double collectWithScore = 0;
    public int portal;
    public int usable;
    public double usableWithScore;


    public ObjectInfo(int name) {

        this.name = name;
        this.solid = 0;
        moveable = 0;
        collectable = 0;
        winsgame = 0;
        losesgame = 0;
        life = 0;
        score = 0;
        distance = 0;
        portal = 0;
        usable = 0;
        usableWithScore = 0;
    }

    /**
     * @return returns 1 if it leads to positive effects
     * should return -1 if it kills
     */
    public int getPositivity() {
        return usableWithScore >= 0 || usable >= 0 || portal >= 0 || life >= 0 || score >= 0 || collectable >= 0 ? 1 : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectInfo that = (ObjectInfo) o;

        if (name != that.name) return false;
        if (winsgame != that.winsgame) return false;
        if (losesgame != that.losesgame) return false;
        if (Double.compare(that.life, life) != 0) return false;
        if (Double.compare(that.score, score) != 0) return false;
        if (solid != that.solid) return false;
        if (moveable != that.moveable) return false;
        if (distance != that.distance) return false;
        if (collectable != that.collectable) return false;
        if (Double.compare(that.collectWithScore, collectWithScore) != 0) return false;
        if (portal != that.portal) return false;
        if (usable != that.usable) return false;
        return Double.compare(that.usableWithScore, usableWithScore) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name;
        result = 31 * result + winsgame;
        result = 31 * result + losesgame;
        temp = Double.doubleToLongBits(life);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(score);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + solid;
        result = 31 * result + moveable;
        result = 31 * result + distance;
        result = 31 * result + collectable;
        temp = Double.doubleToLongBits(collectWithScore);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + portal;
        result = 31 * result + usable;
        temp = Double.doubleToLongBits(usableWithScore);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * sets the object information to solid
     */
    public void analyseSolid() {
        solid = 1;
        collectable = -1;
        moveable = -1;
        portal = -1;
    }

    /**
     * sets the object information to moveable
     * @param win
     */
    public void analysemovable(Types.WINNER win) {
        solid = -1;
        collectable = -1;
        moveable = 1;
        portal = -1;
        if (win == Types.WINNER.PLAYER_LOSES) {
            losesgame = 1;
        }
        if (win == Types.WINNER.PLAYER_WINS) {
            winsgame = 1;
        }
    }

    /**
     * sets the objectinformation to portal
     * @param beforPoints
     * @param afterPoints
     * @param beforLife
     * @param afterLife
     * @param beforPos
     * @param afterpos
     * @param win
     */
    public void analyseportal(double beforPoints, double afterPoints, double beforLife, double afterLife, Vector2d beforPos, Vector2d afterpos, Types.WINNER win) {
        solid = -1;
        collectable = -1;
        moveable = -1;
        portal = 1;
        score = afterPoints - beforPoints;
        life = afterLife - beforLife;
        if (win == Types.WINNER.PLAYER_WINS) {
            winsgame = 1;
        }
        if (win == Types.WINNER.PLAYER_LOSES) {
            losesgame = 1;
        }
        if (beforPos.dist(afterpos) == 0) {
            usable = -1;
        } else {
            usable = 1;
            usableWithScore = beforPoints;
        }
    }

    public void analyseCollectable(double beforPoints, double afterPoints, double beforLife, double afterLife, Types.WINNER win) {
        solid = -1;
        collectable = 1;
        moveable = -1;
        portal = -1;
        score = afterPoints - beforPoints;
        life = afterLife - beforLife;
        if (win == Types.WINNER.PLAYER_WINS) {
            winsgame = 1;
        }
        if (win == Types.WINNER.PLAYER_LOSES) {
            losesgame = 1;
        }
        collectWithScore = beforPoints;
    }

    public void analyseNoInteraction(double beforPoints, double afterPoints, double beforLife, double afterLife, Types.WINNER win) {
        solid = -1;
        collectable = -1;
        moveable = -1;
        portal = -1;
        score = afterPoints - beforPoints;
        life = afterLife - beforLife;
        if (win == Types.WINNER.PLAYER_WINS) {
            winsgame = 1;
        }
        if (win == Types.WINNER.PLAYER_LOSES) {
            losesgame = 1;
        }
    }


    public String toString() {
        return name + "\n" +
                "solid: " + solid + "\n" +
                "mobvabel: " + moveable + " distance " + distance + "\n" +
                "collectable: " + collectable + "  with score " + collectWithScore + "\n" +
                "portal: " + portal + "\n" +
                "winsgame: " + winsgame + "\n" +
                "losesgame: " + losesgame + "\n" +
                "loselife: " + life + "\n" +
                "score: " + score + "\n";
    }


}
