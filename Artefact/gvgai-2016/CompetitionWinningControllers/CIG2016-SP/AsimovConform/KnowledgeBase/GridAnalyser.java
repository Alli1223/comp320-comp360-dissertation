package AsimovConform.KnowledgeBase;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Helper.Vector2i;
import core.game.Observation;
import ontology.Types;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class GridAnalyser {
    public static int bs;
    public Dimension dim;
    public Vector2i size;
    public HashMap<Integer, NPCInfo> npcs = new HashMap<>(25);
    public HashMap<Integer, ObjectInfo> seenObjects = new HashMap<>(25);
    public double[][] pointsMap;


    private ArrayList<Observation>[][] grid;
    private ArrayList<Observation>[][] copygrid;
    private Types.ACTIONS currentAction;
    private Vector2d copypos;
    private Vector2d pos;

    private int analyzeCounter = 0;

    public GridAnalyser(AsimovState so) {
        bs = so.getBlockSize();
        dim = so.getWorldDimension();
        size = so.getWorldSize();
        grid = so.getObservationGrid();
        pointsMap = new double[dim.height / bs][dim.width / bs];
        startAnalyse(grid, so);

    }


    public void analyse(AsimovState so, AsimovState st) {
        //runs less often, the more often it got called. The first 50 times it is guaranteed to get called
        if ((50 / (analyzeCounter + 1) + 0.05) < Math.random()) return;
        analyzeCounter++;
        grid = so.getObservationGrid();
        copygrid = st.getObservationGrid();
        currentAction = st.getLastAction();
        pos = so.getAvatarPosition().copy();//.mul(1/st.getBlockSize());
        copypos = st.getAvatarPosition().copy();//.mul(1/st.getBlockSize());
        npcAnalyse(so, st);
        try {
            if (currentAction == Types.ACTIONS.ACTION_NIL) {

            } else if (currentAction == Types.ACTIONS.ACTION_USE) {

            } else {

                Vector2i vec = calculateDifference(so);
                if (vec.x < 0 || vec.y < 0 || vec.x >= grid.length || vec.y >= grid[vec.x].length) return;
                ArrayList<Observation> array = grid[vec.x][vec.y];

                if (array != null) {

                    for (Observation o : array) {
                        //System.out.println(grid[(int) vec.x][(int) vec.y].get(0).category);
                        int itype = o.itype;
                        int category = o.category;
                        if (category == Types.TYPE_RESOURCE) {
                            if (!seenObjects.containsKey(itype)) {
                                seenObjects.put(itype, new ObjectInfo(itype));
                            }

                        }/* else if (category == Types.TYPE_PORTAL) {
                            if (!seenObjects.containsKey(itype)) {
                                seenObjects.put(itype, new ObjectInfo(itype));
                            }
                            if (seenObjects.get(itype).portal == 0) {
                                seenObjects.get(itype).analyseportal(so.getGameScore(), st.getGameScore(), so.getAvatarHealthPoints(), st.getAvatarHealthPoints(), so.getAvatarPosition(), st.getAvatarPosition(), st.getGameWinner());
                            }*/ else if (category == Types.TYPE_NPC) {

                            if (!npcs.containsKey(itype)) {
                                npcs.put(itype, new NPCInfo(itype));
                            }
                            NPCInfo n = npcs.get(itype);
                            if (st.isGameOver()) {
                                if (st.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
                                    n.death();
                                } else if (st.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                                    n.win();
                                }
                            } else if (so.getAvatarHealthPoints() != st.getAvatarHealthPoints()) {
                                n.looslife(so.getAvatarHealthPoints(), st.getAvatarHealthPoints());

                            } else if (copygrid[vec.x][vec.y] == null || copygrid[vec.x][vec.y].isEmpty())
                                //contains no element
                                return;
                            else if (copygrid[vec.x][vec.y].get(0).category != 3) {
                                n.kill(so.getGameScore(), st.getGameScore(), st.getLastAction());
                            }
                        } else if (category == Types.TYPE_STATIC) {
                            if (!seenObjects.containsKey(itype)) {
                                seenObjects.put(itype, new ObjectInfo(itype));
                            }

                            if (!(seenObjects.get(itype).collectable == 1)) {

                                if (!st.isGameOver() && copypos.equals(pos)
                                        && so.getAvatarOrientation().equals(st.getAvatarOrientation()) && seenObjects.get(itype).solid == 0) {
                                    int anz = 0;
                                    for (ArrayList<Observation> oArray : so.getImmovablePositions()) {

                                        if (oArray.size() != 0 && oArray.get(0).itype == itype) {
                                            anz = anz + oArray.size();
                                            break;
                                        }
                                    }
                                    if (anz > 0.1 * dim.height / bs * dim.width / bs)
                                        seenObjects.get(itype).analyseSolid();
                                } else if (!copypos.equals(pos) && copygrid[vec.x][vec.y].size() != 0 &&
                                        copygrid[vec.x][vec.y].get(0).category == 4) {
                                    seenObjects.get(itype).analyseNoInteraction(so.getGameScore(), st.getGameScore(), so.getAvatarHealthPoints(), st.getAvatarHealthPoints(), st.getGameWinner());
                                } else if (!copypos.equals(pos) && copygrid[vec.x][vec.y].size() != 0 &&
                                        copygrid[vec.x][vec.y].get(0).category != 4) {
                                    seenObjects.get(itype).analyseCollectable(so.getGameScore(), st.getGameScore(), so.getAvatarHealthPoints(), st.getAvatarHealthPoints(), st.getGameWinner());
                                }
                            }

                         /*else if (category == Types.TYPE_FROMAVATAR) {
                            if (!seenObjects.containsKey(itype)) {
                                seenObjects.put(itype, new ObjectInfo(itype));
                            }*/
                        } else if (category == Types.TYPE_MOVABLE) {
                            if (!seenObjects.containsKey(itype)) {
                                seenObjects.put(itype, new ObjectInfo(itype));

                            }
                            seenObjects.get(itype).analysemovable(st.getGameWinner());
                        }
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public ArrayList<ObjectInfo> getInformations(Vector2d pos) {
        ArrayList<ObjectInfo> ret = new ArrayList<>();
        if (grid.length <= (int) pos.x || (int) pos.x < 0 || grid[(int) pos.x].length <= (int) pos.y || (int) pos.y < 0)
            return ret;
        ArrayList<Observation> arrayO = grid[(int) pos.x][(int) pos.y];
        for (Observation o : arrayO) {
            int type = o.itype;
            if (!seenObjects.containsKey(type)) {
                seenObjects.put(type, new ObjectInfo(type));
                ret.add(seenObjects.get(type));
            } else {
                ret.add(seenObjects.get(type));
            }
        }

        return ret;
    }

    private void npcAnalyse(AsimovState so, AsimovState st) {
        ArrayList<Observation>[] npcbefor = so.getNPCPositions();
        ArrayList<Observation>[] npcafter = st.getNPCPositions();
        if (!(npcbefor == null) && !(npcafter == null)) {
            for (ArrayList<Observation> arrayb : npcbefor) {
                for (ArrayList<Observation> arraya : npcafter) {
                    for (Observation o1 : arrayb) {
                        for (Observation o2 : arraya) {
                            if (o1.obsID == o2.obsID) {
                                if (!npcs.containsKey(o1.itype)) {
                                    npcs.put(o1.itype, new NPCInfo(o1.itype));
                                }
                                Vector2i v1 = new Vector2i((int) o1.position.x, (int) o1.position.y);
                                Vector2i v2 = new Vector2i((int) o2.position.x, (int) o2.position.y);
                                npcs.get(o1.itype).distance = v1.manDist(v2);
                            }
                        }
                    }
                }
            }
        }
    }

    public void InterestMapCalulate(AsimovState so) {
        for (int j = 0; j < dim.height / bs; j++) {
            for (int i = 0; i < dim.width / bs; i++) {
                pointsMap[j][i] = 0.0;
            }
        }
        double interest = 5.0;

        ArrayList<Observation>[] inmovabel = so.getImmovablePositions();
        ArrayList<Observation>[] protal = so.getPortalsPositions();
        ArrayList<Observation>[] movabel = so.getMovablePositions();

        if (inmovabel != null) {
            for (ArrayList<Observation> array : inmovabel) {
                if (array != null) {
                    int itype = array.get(0).itype;
                    if (seenObjects.containsKey(itype)) {
                        ObjectInfo info = seenObjects.get(itype);
                        double score = info.score;
                        if (score != 0) {
                            for (Observation o : array) {
                                int x = (int) o.position.x / bs;
                                int y = (int) o.position.y / bs;
                                Vector2i pos = new Vector2i(x, y);

                                for (int i = 0; i < pointsMap.length; i++) {
                                    for (int j = 0; j < pointsMap[0].length; j++) {
                                        pointsMap[i][j] += score / (1 + pos.manDist(i, j));
                                    }
                                }
                            }
                        }
                        if (info.solid == 1) {
                            for (Observation o : array) {
                                pointsMap[(int) o.position.y / bs][(int) o.position.x / bs] = -111;//Integer.MIN_VALUE;
                            }
                        }
                        if (info.losesgame == 1) {
                            for (Observation o : array) {
                                pointsMap[(int) o.position.y / bs][(int) o.position.x / bs] = -1111;//Integer.MIN_VALUE;
                            }
                        }

                    } else {
                        for (Observation o : array) {
                            int x = (int) o.position.x / bs;
                            int y = (int) o.position.y / bs;
                            Vector2i pos = new Vector2i(x, y);

                            for (int i = 0; i < pointsMap.length; i++) {
                                for (int j = 0; j < pointsMap[0].length; j++) {
                                    pointsMap[i][j] += interest / (1 + pos.manDist(i, j));
                                }
                            }
                        }
                    }
                }
            }
        }
        if (protal != null) {
            for (ArrayList<Observation> array : protal) {
                if (array != null) {
                    int itype = array.get(0).itype;
                    if (seenObjects.containsKey(itype)) {
                        double score = seenObjects.get(itype).score;
                        if (score > 0) {
                            for (Observation o : array) {
                                int x = (int) o.position.x / bs;
                                int y = (int) o.position.y / bs;
                                Vector2i pos = new Vector2i(x, y);

                                for (int i = 0; i < pointsMap.length; i++) {
                                    for (int j = 0; j < pointsMap[0].length; j++) {
                                        pointsMap[i][j] += score / (1 + pos.manDist(i, j));
                                    }
                                }
                            }
                        }
                    } else {
                        for (Observation o : array) {
                            int x = (int) o.position.x / bs;
                            int y = (int) o.position.y / bs;
                            Vector2i pos = new Vector2i(x, y);

                            for (int i = 0; i < pointsMap.length; i++) {
                                for (int j = 0; j < pointsMap[0].length; j++) {
                                    pointsMap[i][j] += interest / (1 + pos.manDist(i, j));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * call with startpos ,0 , empty
     *
     * @param pos
     * @param dis
     */
    private void dis_rec(Vector2i pos, HashSet<Integer> seen, int dis, int[][] distances) {
        seen.add(pos.x * 1000 + pos.y);
        distances[pos.y][pos.x] = dis;
        Vector2i[] next = new Vector2i[]{new Vector2i(pos.x, pos.y - 1), new Vector2i(pos.x, pos.y + 1), new Vector2i(pos.x + 1, pos.y), new Vector2i(pos.x - 1, pos.y)};
        for (Vector2i n : next)
            if (n.x < 0 || n.y < 0 || n.x >= (int) dim.width / bs || n.y >= (int) dim.height / bs)
                n.x = -100;
            else if (seen.contains(n.x * 1000 + n.y))
                n.x = -100;
            else seen.add(n.x * 1000 + n.y);
        for (Vector2i n : next) {
            if (n.x == -100)
                continue;
            if (grid[n.x][n.y] == null)
                if (seenObjects.containsKey(grid[n.x][n.y].get(0).itype))
                    if (seenObjects.get(grid[n.x][n.y].get(0).itype).solid == 1 || seenObjects.get(grid[n.x][n.y].get(0).itype).losesgame == 1)
                        continue;

            //distance of n to start is dis+1

            dis_rec(n, seen, dis + 1, distances);
        }

    }

    private void startAnalyse(ArrayList<Observation>[][] grid, AsimovState so) {
        InterestMapCalulate(so);
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] != null) {
                    ArrayList<Observation> array = grid[i][j];
                    for (Observation o : array) {
                        int type = o.itype;
                        int category = o.category;
                       /* if (category == 2) {
                            if (!seenObjects.containsKey(type)) {
                                seenObjects.put(type, new ObjectInfo(type));
                                seenObjects.get(type).portal = 1;
                                seenObjects.get(type).moveabel = -1;
                                seenObjects.get(type).collectable = -1;
                                seenObjects.get(type).solid = -1;
                            }

                        }*/
                        if (category == 3) {
                            if (!npcs.containsKey(type)) {
                                npcs.put(type, new NPCInfo(type));
                            }

                        } else if (category == 6) {
                            if (!seenObjects.containsKey(type)) {
                                seenObjects.put(type, new ObjectInfo(type));
                                seenObjects.get(type).portal = -1;
                                seenObjects.get(type).moveable = 1;
                                seenObjects.get(type).collectable = -1;
                                seenObjects.get(type).solid = -1;
                            }
                        }
                    }
                }
            }
        }
    }


    private Vector2i calculateDifference(AsimovState state) {
        Vector2i vec = new Vector2i();
        if (currentAction == Types.ACTIONS.ACTION_UP) {
            vec.set(state.getAvatarX(), state.getAvatarY() - 1);
        } else if (currentAction == Types.ACTIONS.ACTION_DOWN) {
            vec.set(state.getAvatarX(), state.getAvatarY() + 1);

        } else if (currentAction == Types.ACTIONS.ACTION_LEFT) {
            vec.set(state.getAvatarX() - 1, state.getAvatarY());

        } else if (currentAction == Types.ACTIONS.ACTION_RIGHT) {
            vec.set(state.getAvatarX() + 1, state.getAvatarY());
        }
        return vec;
    }

}
