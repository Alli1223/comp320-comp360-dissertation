package controllers.singlePlayer.bestFirstSearch;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.*;

public class BFS
{
    boolean fenpty;
    boolean getmove;
    public LinkedList<Types.ACTIONS> path;
    public PriorityQueue<BFSNode> queue;
    public HashMap<Long, HashSet<Long>> visted;
    public int memory_used;
    public static long MAX_MEMORY = 1600000000;
    public BFSNode best_node;
    public BFSNode deepest_node;
    public Random rand;
    public int expanded;
    public int canceled;
    private BFSNode win_Node;
    boolean is_win;
    public BFSNode curr;
    public int act_count;
    int blockSize;
    int countBFS=0;
    private int Max_GameTick = 2000;
    public static boolean OneDimension = false;

    public static Types.ACTIONS[] actions = null;
    public static int NUM_ACTIONS = 0;

    public BFS(StateObservation so)
    {
        rand = new Random();
        getmove = false;
        Comparator<BFSNode> c = new Comparator<BFSNode>() {
            public int compare(BFSNode o1, BFSNode o2) {return (int) Math.signum(o1.getPriority() - o2.getPriority());}
        };
        queue = new PriorityQueue<BFSNode>(c);
        visted = new HashMap<Long, HashSet<Long>>();
        path = new LinkedList<Types.ACTIONS>();
        memory_used = 0;
        so.advance(Types.ACTIONS.ACTION_NIL);
        BFSNode first = new BFSNode(so, null, null, rand);
        //queue.add(first);
        curr = first;
        act_count = 0;
        first.HashMe();
        AddVisited(first.code);
        best_node = first;
        deepest_node = first;
        expanded = canceled = 0;
        fenpty = false;
        is_win = false;
        blockSize = so.getBlockSize();


        //Get the actions in a static array.
        ArrayList<Types.ACTIONS> actionList = so.getAvailableActions();

        actionList.add(Types.ACTIONS.ACTION_NIL); // allow action NIL

        NUM_ACTIONS = actionList.size();
        actions = new Types.ACTIONS[NUM_ACTIONS];
        int i = 0;
        int Dimension = 0;
        for (Types.ACTIONS action : actionList) {
            //System.out.println(action.name() + " ordinal:" + action.ordinal());
            actions[i++] = action;
            if(action == Types.ACTIONS.ACTION_DOWN || action == Types.ACTIONS.ACTION_UP
                    ||action == Types.ACTIONS.ACTION_LEFT || action == Types.ACTIONS.ACTION_RIGHT){
                Dimension++;
            }
        }
        if(Dimension<=2){
            OneDimension = true;
            System.out.println("OneDGame");
        }
        //isDetermin = UseHashCode(so);

    }

    public void reset(StateObservation so)
    {
        queue.clear();
        visted.clear();
        BFSNode first = new BFSNode(so, null, null, rand);
        best_node = first;
        deepest_node = first;
        first.HashMe();
        //queue.add(first);
        curr = first;
        act_count = 0;
        AddVisited(first.code);
        memory_used = 0;
        fenpty = false;
        canceled = expanded = 0;
    }

    public void AddVisited(long[] code)
    {
        HashSet<Long> set = visted.get(code[0]);
        if (set != null)
        {
            set.add(code[1]);
        }
        else
        {
            set = new HashSet<Long>();
            set.add(code[0]);
            visted.put(code[0], set);
        }
    }

    public boolean IsVisited(long[] code)
    {
        HashSet<Long> set = visted.get(code[0]);
        if (set != null)
        {
            if (set.contains(code[1]))
            {
                if (rand.nextDouble() < 0.05)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public Types.ACTIONS Run(StateObservation now, ElapsedCpuTimer timer) {
        BFSNode new_node;
        StateObservation so;

        if (getmove) {
            if ((double) canceled / (double) expanded < 0.05) {
                System.out.println("mincanceled");
                System.out.println("changeAlgorithm");
                reset(now);

                return Types.ACTIONS.ACTION_NIL;
            }

            if (path.isEmpty()) {
                getmove = false;
                reset(now);

                return Types.ACTIONS.ACTION_NIL;
            } else {
                Types.ACTIONS act;
                act = path.removeFirst();
                while (timer.remainingTimeMillis() > 8) ;
                return act;
            }
        }

        if (now.getGameTick() > Max_GameTick * 4 / 5) {

            if (is_win) {
                GetPath(win_Node);
                getmove = true;
                queue.clear();
                visted.clear();
                return Types.ACTIONS.ACTION_NIL;
            } else {
                GetPath(best_node);
                getmove = true;
                queue.clear();
                visted.clear();
                return Types.ACTIONS.ACTION_NIL;
            }
        }

        int countWalkAway = 0;
        while (timer.remainingTimeMillis() > 0) {
            countBFS++;
            if (blockSize < 50 && countBFS == 50000) {
                countBFS = 0;
            }

            // Calculate memory usage
            if ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) > MAX_MEMORY)//Memory Over
            {
                if (is_win) {
                    GetPath(win_Node);
                } else {
                    GetPath(best_node);
                }
                queue.clear();
                visted.clear();
                break;
            }
            if (curr == null) {
                if (queue.isEmpty()) {
                    if (fenpty) {
                        if (is_win) {
                            GetPath(win_Node);
                        } else {
                            GetPath(best_node);
                        }
                        getmove = true;
                        fenpty = false;
                    } else {
                        fenpty = true;
                    }
                    break;
                } else {
                    curr = queue.poll();
                }
            } else {


                if (act_count < now.getAvailableActions().size()) {
                    so = curr.so.copy();
                    so.advance(actions[act_count]);
                    new_node = new BFSNode(so, actions[act_count], curr, rand);
                    expanded++;
                    if (so.isGameOver())
                    {
                        if (so.getGameWinner() == Types.WINNER.PLAYER_WINS)
                        {
                            if (!is_win) {
                                is_win = true;
                                win_Node = new BFSNode(so, actions[act_count], curr, rand);
                                if (so.getGameTick() > Max_GameTick * 4 / 5)
                                {
                                    GetPath(win_Node);
                                    getmove = true;
                                } else {
                                    System.out.println("continue search");
                                }
                            } else {
                                if (so.getGameScore() > win_Node.rawvalue)
                                {
                                }
                                if (so.getGameTick() > Max_GameTick * 4 / 5)
                                {
                                    GetPath(win_Node);
                                    getmove = true;
                                }
                            }
                        }
                    } else {
                        new_node.HashMe();

                        if (!IsVisited(new_node.code))
                        {
                            //visted.add(new_node.hashcode);
                            AddVisited(new_node.code);
                            queue.add(new_node);
                            memory_used++;
                            if (new_node.value > best_node.value)
                            {
                                best_node = new_node;
                            }
                            if (new_node.deepth > deepest_node.deepth)
                            {
                                deepest_node = new_node;
                            }
                        } else {
                            canceled++;
                        }
                    }
                    so = null;
                    act_count++;
                } else {
                    curr.so = null;
                    curr = null;
                    act_count = 0;
                }
            }

        }
        return Types.ACTIONS.ACTION_NIL;
    }

    public void search(StateObservation now, ElapsedCpuTimer timer)
    {
        BFSNode new_node;
        StateObservation so;

        if (getmove)
        {
            return;
        }

        while(timer.remainingTimeMillis() > 10)
        {
            if ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) > MAX_MEMORY)//Memory Over
            {
                if(is_win){
                    GetPath(win_Node);
                }else{
                    GetPath(best_node);
                }
                getmove = true;
                queue.clear();
                visted.clear();
                break;
            }
            if (curr == null)
            {
                if (queue.isEmpty())
                {
                    if (fenpty)
                    {
                        if(is_win){
                            GetPath(win_Node);
                        }else{
                            GetPath(best_node);
                        }
                        getmove = true;
                        fenpty = false;
                    }
                    else
                    {
                        fenpty = true;
                    }
                    break;
                }
                else
                {
                    curr = queue.poll();
                }
            }
            else
            {
                if (act_count < now.getAvailableActions().size())
                {
                    so = curr.so.copy();
                    so.advance(actions[act_count]);
                    new_node = new BFSNode(so, actions[act_count], curr, rand);
                    expanded++;
                    if (so.isGameOver())
                    {
                        if (so.getGameWinner() == Types.WINNER.PLAYER_WINS)
                        {
                            if (!is_win) {
                                is_win= true;
                                win_Node = new BFSNode(so, actions[act_count], curr, rand);
                                if ( so.getGameTick() > Max_GameTick * 4 / 5)
                                {
                                    GetPath(win_Node);
                                    getmove = true;
                                    System.out.println(path);
                                } else {
                                    System.out.println("continue search");
                                }
                            } else {
                                if (so.getGameScore() > win_Node.rawvalue) {
                                    win_Node = new BFSNode(so, actions[act_count], curr, rand);
                                }
                                if (so.getGameTick() > Max_GameTick * 4 / 5) {
                                    GetPath(win_Node);
                                    getmove = true;
                                    System.out.println(path);
                                }
                            }
                        }
                    }
                    else
                    {
                        new_node.HashMe();

                        if (!IsVisited(new_node.code))
                        {
                            AddVisited(new_node.code);
                            queue.add(new_node);
                            memory_used++;
                            if (new_node.value > best_node.value)
                            {
                                best_node = new_node;
                            }
                            if (new_node.deepth > deepest_node.deepth)
                            {
                                deepest_node = new_node;
                            }
                        }
                        else
                        {
                            canceled++;
                        }
                    }
                    so = null;
                    act_count++;
                }
                else
                {
                    curr.so = null;
                    curr = null;
                    act_count = 0;
                }
            }
        }
        return;
    }

    public void GetPath(BFSNode goal)
    {
        for (Types.ACTIONS act : goal.actions)
        {
            path.add(act);
        }

    }



}
