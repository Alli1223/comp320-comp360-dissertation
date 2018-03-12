package AsimovConform.SubAgents.BFS;

import AsimovConform.ActionHeuristics.ActionHeuristic;
import AsimovConform.ActionPruner.ActionPruner;
import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Heuristics.Heuristic;
import AsimovConform.SubAgents.AsimovAgent;
import AsimovConform.SubAgents.AsimovAgentStatus;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

public class BFS extends AsimovAgent {
    private final static int TIME_LIMIT = 5;

    private boolean initDone, initQueue2;
    private PriorityQueue<BFSItem> queue, queue2;
    private BFSQueueAvg avg, avg2;
    private int queue2start;
    private int imaginaryTick;
    private BFSItem winSolution;
    private int winSolutionFoundInTick;
    private int timeToFindSolution;
    private boolean searchForSolution;
    private boolean gaveUpCauseCouldntFindAnySolution;
    private boolean gaveUpCauseExecutedCompleteSolution;

    private AsimovState findEnd;
    private int endOfGame;
    private int remainingTicks;

    private Runtime rt;
    private long maxRamToUse;
    private double avgRating;
    private int avgCounter;

    private ArrayList<Long> overallLoop, overallLoop2;

    private boolean preRun;

    public BFS(AsimovState as, Heuristic heuristic, ActionHeuristic actionHeuristic, ActionPruner actionPruner) {
        super(as, heuristic, actionHeuristic, actionPruner);

        queue = new PriorityQueue<>(40000, Collections.<BFSItem>reverseOrder());
        queue2 = null;
        initDone = initQueue2 = false;
        imaginaryTick = 0;
        winSolution = null;
        winSolutionFoundInTick = 0;
        timeToFindSolution = 0;
        rt = Runtime.getRuntime();
        maxRamToUse = (long) (0.70 * rt.maxMemory());
        avg = new BFSQueueAvg();
        searchForSolution = true;
        overallLoop = new ArrayList<>();
        endOfGame = remainingTicks = 2001;
        preRun = false;
        gaveUpCauseCouldntFindAnySolution = false;
        gaveUpCauseExecutedCompleteSolution = false;
    }

    @Override
    public Types.ACTIONS act(AsimovState as, ElapsedCpuTimer timer) {
        // do init
        if (!initDone) {
            findEnd = as.copy();

            BFSItem temp = new BFSItem(as);
            queue.add(temp);
            if (Agent.KB.tickZeroBugHappnend) {
                temp = new BFSItem(queue.poll(), Types.ACTIONS.ACTION_NIL, preRun, heuristic);
                queue.add(temp);
            }
            overallLoop(temp);
            avg.addAvgRating(temp.getRating());
            initDone = true;
        }
        if (Agent.DRAW && searchForSolution && queue.peek() != null)
            Agent.drawer.add("BFS", queue.peek().state.getPositionHistory(), new Color(0, 0, 255, 60));

        // check if game is still bfsSolvable
        if (searchForSolution && !Agent.KB.isBFSSolvable(as)) {
            if (Agent.OUTPUT)
                System.out.println(">> Game is not longer solvable for BFS.");

            status = AsimovAgentStatus.GIVE_UP;
            return Types.ACTIONS.ACTION_NIL;
        }


        // start moving when time is over and collect some points at least
        if (endOfGame == 2001) {
            findEnd.advance(Types.ACTIONS.ACTION_NIL);
            if (findEnd.isGameOver()) {
                endOfGame = findEnd.getGameTick() - 1;

            } else {
                findEnd.advance(Types.ACTIONS.ACTION_NIL);
                if (findEnd.isGameOver()) {
                    endOfGame = findEnd.getGameTick() - 1;
                }
            }
        }
        remainingTicks = endOfGame - as.getGameTick();
        if (searchForSolution && winSolution != null) {
            if (remainingTicks < winSolution.getNumberOfActions()) {
                //winSolution = queue.peek();
                searchForSolution = false;

                if (Agent.OUTPUT)
                    System.out.println(">> Time is up. Starting with solution " + winSolution);
            }
            // if bfs do not find any solution with at least some score within the half number of ticks, bfs gives up
            if (winSolution == null && remainingTicks * 2 <= endOfGame) {
                if (Agent.OUTPUT)
                    System.out.println(">> BFS couldn't find any solution within the first " + remainingTicks + " ticks.");
                gaveUpCauseCouldntFindAnySolution = true;
                status = AsimovAgentStatus.GIVE_UP;
                return Types.ACTIONS.ACTION_NIL;
            }
        }


        // check free ram and prevent out of memory errors
        if (40 - TIME_LIMIT < timer.remainingTimeMillis()) {
            if (maxRamToUse <= rt.totalMemory() - rt.freeMemory() || queue.size() > 45000) {
                // first we check if a second run is active, if the cause was not to many items
                if (queue2 != null && queue.size() < 45000) {
                    clearQueue2();
                    System.gc();
                }

                // if we still have problems start deleting
                if (maxRamToUse <= rt.totalMemory() - rt.freeMemory() || queue.size() > 45000) {
                    int itemsInQueue = queue.size();
                    double cut = avg.calcAvgRating();
                    avg.resetAvg();
                    queue.removeIf(p -> deleteInQueue(p, cut, avg));
                    System.gc(); // we need to call GC or we get memory problems
                    if (Agent.OUTPUT)
                        System.out.println(">> BFS cleared memory: " + (itemsInQueue - queue.size()) + " items thrown away. " + queue.size() + " items remaining.");
                }
            }
            if (maxRamToUse <= rt.totalMemory() - rt.freeMemory() || queue.size() > 50000) {
                if (Agent.OUTPUT)
                    System.out.println(">> Not enough memory free: Do not compute new solutions.");
                return Types.ACTIONS.ACTION_NIL;
            }
        }
        if (timer.remainingTimeMillis() < TIME_LIMIT) {
            return Types.ACTIONS.ACTION_NIL;
        }

        // run clBFS
        if (searchForSolution) {

            if (!initQueue2 && winSolution != null && as.getGameTick() - winSolutionFoundInTick > endOfGame * 0.1) {
                initQueue2(as);
                if (timeToFindSolution == 0)
                    if (winSolutionFoundInTick == 0)
                        timeToFindSolution = (int) (endOfGame * 0.1);
                    else
                        timeToFindSolution = (int) Math.round(winSolutionFoundInTick * 1.5);

                if (Agent.OUTPUT)
                    System.out.println(">> Start new second run at tick " + as.getGameTick());
            }

            if (queue2 != null) {
                // if we couldn't find a better solution quit queue2
                if (as.getGameTick() - queue2start > timeToFindSolution) {
                    clearQueue2();
                    if (Agent.OUTPUT)
                        System.out.println(">> No better solution found after " + timeToFindSolution + " ticks -> quit second run");
                } else {
                    double currentBest = winSolution.getRealScore();
                    ElapsedCpuTimer timer2 = new ElapsedCpuTimer();
                    timer2.setMaxTimeMillis(Math.round(timer.remainingTimeMillis() * 0.65));

                    clBFS(as, timer2, true, queue2, overallLoop2, avg2);

                    // if we find a better solution use this solution as normal solution
                    if (winSolution.getRealScore() > currentBest || winSolution.isWinSolution()) {
                        timeToFindSolution = (int) Math.round((winSolutionFoundInTick - queue2start) * 1.5) + 20;
                        queue = queue2;
                        overallLoop = overallLoop2;
                        avg = avg2;
                        clearQueue2();
                    }
                }
            }

            if (!clBFS(as, timer, false, queue, overallLoop, avg))
                return Types.ACTIONS.ACTION_NIL;

        }

        return doAction(as, timer);
    }

    /**
     * a normal BFS
     *
     * @param as    the current state
     * @param timer the timer
     * @return true if everything is ok, false if bfs needs to quit fast
     */
    private boolean clBFS(AsimovState as, ElapsedCpuTimer timer, boolean ignoreTimeLimit, PriorityQueue<BFSItem> queue, ArrayList<Long> loop, BFSQueueAvg avg) {
        BFSItem currentItem, newItem;

        long timeRemaining = timer.remainingTimeMillis();
        double timeUseAvg = 0;
        long timeCurrent;

        while (searchForSolution && (ignoreTimeLimit || TIME_LIMIT < timeRemaining) && timeUseAvg * 2 < timeRemaining) {
            timeCurrent = timeRemaining;
            // ----------------------------------------------------------------

            // get current item and check if the predecessor state is equal
            currentItem = queue.poll();
            if (currentItem != null) {
                avg.subAvgRating(currentItem.getRating());

                while (currentItem.isMovingState()) {
                    if (2 > timer.remainingTimeMillis()) {
                        queue.add(currentItem);
                        return false;
                    }
                }

                // advance all possible actions
                for (Types.ACTIONS action : actions) {
                    if (actionPruner.pruned(currentItem.state, action) && !Agent.switchedSolver) {
                        continue;
                    }

                    newItem = new BFSItem(currentItem, action, preRun, heuristic);

                    // is this item has a win solution run it
                    if (newItem.isWinSolution()) {
                        if (AsimovConform.Agent.OUTPUT && winSolution != null)
                            System.out.println(">> Found a winsolution at tick " + (preRun ? "'constructor'" : as.getGameTick()) + " with score " + winSolution.getRealScore() + " and " + winSolution.getNumberOfActions() + " steps");

                        winSolution = newItem;
                        searchForSolution = false;
                        break;

                    } else if (remainingTicks > newItem.getNumberOfActions() && newItem.getRealScore() > 0 && (winSolution == null || newItem.getRealScore() > winSolution.getRealScore())) {
                        if (Agent.OUTPUT)
                            System.out.println(">> Found a new best solution at tick " + (preRun ? "'constructor'" : as.getGameTick()) + " with score " + newItem.getRealScore());
                        winSolution = newItem;
                        winSolutionFoundInTick = as.getGameTick();
                    }
                    // do not add to queue if a loop was detected
                    if (!overallLoop(newItem, true, loop) && !newItem.isLoopDetected() && !newItem.runIntoDeath()) {
                        queue.add(newItem);
                        avg.addAvgRating(newItem.getRating());
                    }
                }
            }

            // check if there is an possible solution left
            if (queue.size() == 0) {
                if (Agent.OUTPUT)
                    System.out.print(">> BFS queue is empty ");

                if (winSolution != null) {
                    if (Agent.OUTPUT)
                        System.out.print("but found a solution with score " + winSolution.getRealScore() + "\n");

                    searchForSolution = false;
                } else {
                    if (Agent.OUTPUT)
                        System.out.print("and no solution was found\n");

                    status = AsimovAgentStatus.GIVE_UP;
                    return false;
                }
                break;
            }

            // ----------------------------------------------------------------
            timeRemaining = timer.remainingTimeMillis();
            timeCurrent = timeCurrent - timeRemaining;
            timeUseAvg = (timeUseAvg + timeCurrent) / 2.0;
        }

        return true;
    }

    /**
     * BFS search while executing a solution.
     *
     * @param currentState The current State, will be root-node for BFS.
     * @param timer        the timer
     * @return a better solution than the current one or null
     */
    private BFSItem olBFS(AsimovState currentState, ElapsedCpuTimer timer) {
        PriorityQueue<BFSItem> queue = new PriorityQueue<>(500, Collections.<BFSItem>reverseOrder());
        ArrayList<Long> smallLoops = new ArrayList<>();
        BFSItem newItem, currentItem, newWinSolution = null;
        AsimovState currentStateCopy = currentState.copy();
        int tickOffset = 0;
        double bestScore = winSolution.getRealScore();
        boolean freeSearch = false;

        queue.add(new BFSItem(currentState));

        while (timer.remainingTimeMillis() > TIME_LIMIT) {
            if (queue.size() == 0) {
                if (freeSearch) return null;

                Types.ACTIONS nextAction = winSolution.getAction(imaginaryTick + tickOffset);
                if (nextAction == null) {
                    // if pruned search in finished, start an unPruned search
                    freeSearch = true;
                    queue.add(new BFSItem(currentState));
                } else {
                    tickOffset++;

                    currentStateCopy.advance(nextAction);
                    queue.add(new BFSItem(currentStateCopy));
                }
            }

            currentItem = queue.poll();
            for (Types.ACTIONS action : actions) {
                newItem = new BFSItem(currentItem, action, false, heuristic);

                if (newItem.isWinSolution() && newItem.getNumberOfActions() <= endOfGame - currentState.getGameTick() && newItem.getRealScore() > bestScore) {
                    newWinSolution = newItem;
                    bestScore = newItem.getRealScore();
                }

                if ((freeSearch || (!overallLoop(newItem, false, overallLoop) && !smallLoops.contains(newItem.state.getHashCode()) && !newItem.isLoopDetected())) && !newItem.runIntoDeath()) {
                    queue.add(newItem);
                    smallLoops.add(newItem.state.getHashCode());
                }
            }

        }

        return newWinSolution;
    }

    public void preAct(AsimovState as, ElapsedCpuTimer timer) {
        if (10 < timer.remainingTimeMillis()) {
            preRun = true;
            act(as, timer);
            preRun = false;
        }
    }

    @Override
    public int evaluate() {
        if (AsimovConform.Agent.KB.bfsSolvable && !gaveUpCauseCouldntFindAnySolution && !gaveUpCauseExecutedCompleteSolution) {
            return 100;
        } else {
            return -100;
        }
    }

    @Override
    public void clear() {
        clearQueue2();

        winSolution = null;
        searchForSolution = true;
        queue = new PriorityQueue<>(Collections.<BFSItem>reverseOrder());
        overallLoop = new ArrayList<>();
        initDone = false;
    }

    private void initQueue2(AsimovState as) {
        queue2 = new PriorityQueue<>(10000, Collections.<BFSItem>reverseOrder());
        overallLoop2 = new ArrayList<>();
        avg2 = new BFSQueueAvg();

        BFSItem temp = new BFSItem(as);
        queue2.add(temp);
        overallLoop2.add(temp.hash());
        queue2start = as.getGameTick();
        avg2.addAvgRating(temp.getRating());

        initQueue2 = true;
    }

    private void clearQueue2() {
        queue2 = null;
        overallLoop2 = null;
        avg2 = null;
        initQueue2 = false;
    }

    private Types.ACTIONS doAction(AsimovState as, ElapsedCpuTimer timer) {
        if (searchForSolution || preRun) {
            return Types.ACTIONS.ACTION_NIL;
        } else {
            if (Agent.KB.tickZeroBugHappnend && as.getGameTick() == 0) {
                return Types.ACTIONS.ACTION_NIL;
            }

            // while running let an olBFS search for a better solution
            BFSItem betterSolution = olBFS(as, timer);
            if (betterSolution != null) {
                imaginaryTick = 0;
                winSolution = betterSolution;

                if (Agent.OUTPUT)
                    System.out.println(">> BFS found a better solution while walking at tick " + as.getGameTick() + " with score " + winSolution.getRealScore() + " and " + winSolution.getNumberOfActions() + " steps");
            }

            // while running look n steps if something can kill us
            nStepLookAhead(2, as, imaginaryTick, timer);

            if (status != AsimovAgentStatus.GIVE_UP) {
                Types.ACTIONS action = winSolution.getAction(imaginaryTick++);
                if (action == null) {
                    status = AsimovAgentStatus.GIVE_UP;
                    gaveUpCauseExecutedCompleteSolution = true;
                    action = Types.ACTIONS.ACTION_NIL;

                    if (Agent.OUTPUT)
                        System.out.println(">> BFS gives up because the complete solution was executed");
                }
                return action;
            } else
                return Types.ACTIONS.ACTION_NIL;
        }
    }

    private void nStepLookAhead(int n, AsimovState current, int startTick, ElapsedCpuTimer timer) {
        if (winSolution == null)
            return;


        AsimovState as = current.copy();
        for (int i = 0; i < n && startTick + i <= endOfGame - current.getGameTick(); i++) {

            if (timer.remainingTimeMillis() < 2)
                return;


            Types.ACTIONS action = winSolution.getAction(startTick + i);
            if (action == null)
                break;

            as.advance(action);
            if (as.isPlayerLooser()) {
                status = AsimovAgentStatus.GIVE_UP;

                if (Agent.OUTPUT)
                    System.out.println(">> BFS gives up because he would loose within the next " + n + " steps of his solution.");

                break;
            }
        }
    }

    private boolean deleteInQueue(BFSItem item, double cut, BFSQueueAvg avg) {
        double rating = item.getRating();
        if (rating > cut) {
            avg.addAvgRating(rating);
            return false;
        } else {
            return true;
        }
    }

    private boolean overallLoop(BFSItem item) {
        return overallLoop(item, true, overallLoop);
    }

    private boolean overallLoop(BFSItem item, boolean addLoops, ArrayList<Long> loop) {
        if (loop.contains(item.hash())) {
            return true;
        } else {
            if (addLoops) loop.add(item.hash());
            return false;
        }
    }

    public String toString() {
        return "BFS";
    }
}
