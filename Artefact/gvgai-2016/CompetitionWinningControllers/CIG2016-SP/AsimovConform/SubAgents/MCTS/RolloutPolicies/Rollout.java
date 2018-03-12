package AsimovConform.SubAgents.MCTS.RolloutPolicies;

import AsimovConform.ActionHeuristics.ActionHeuristic;
import AsimovConform.ActionPruner.ActionPruner;
import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.Heuristics.Heuristic;
import AsimovConform.SubAgents.MCTS.SingleTreeNode;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

import java.awt.*;
import java.util.Random;

public class Rollout {

    public Random m_rnd;
    private int rlength;

    public Rollout() {
        m_rnd = new Random();
    }

    public double randomRoll(SingleTreeNode selected, ElapsedCpuTimer elapsedTimer, Heuristic heuristic) {
        AsimovState rollerState = selected.state.copy();
        int thisDepth = 0;
        rlength = m_rnd.nextInt(5) + 5;

        ACTIONS[] actions = Agent.actions;
        while (!finishRollout(rollerState, thisDepth) && actions.length > 0
                && elapsedTimer.remainingTimeMillis() > SingleTreeNode.time_threshold) {

            if(Agent.DRAW)
                Agent.drawer.add("rollouts", rollerState.getAvatarPosition(), new Color(0, 250, 0, 40));

            int rnd = m_rnd.nextInt(actions.length);

            AsimovState stateObs = rollerState.copy();
            rollerState.advance(actions[rnd]);
            Agent.KB.gridAnalyser.analyse(stateObs, rollerState);

            thisDepth++;

            if(Agent.DRAW)
                Agent.drawer.add("rollouts", rollerState.getAvatarPosition(), new Color(0, 250, 0, 40));
        }

        return heuristic.evaluate(rollerState);
    }

    public double heuristicRoll(SingleTreeNode selected, ElapsedCpuTimer elapsedTimer, Heuristic heuristic, ActionHeuristic actionHeuristic) {
        AsimovState rollerState = selected.state.copy();
        int thisDepth = 0;
        rlength = m_rnd.nextInt(10) + 10;

        while (elapsedTimer.remainingTimeMillis() > SingleTreeNode.time_threshold
                && !finishRollout(rollerState, thisDepth)) {
            double[] actionProbabilities = new double[Agent.actions.length];
            double sum = 0;
            for (int i = 0; i < Agent.actions.length; i++) {
                ACTIONS action = Agent.actions[i];
                double value = actionHeuristic.evaluate(rollerState, action);
                sum += value;
                actionProbabilities[i] = value;
            }

            int rnd_bounds = 10000;

            int[] bounds = new int[actionProbabilities.length];
            int boundsSum = 0;
            // normalize the probabilities, so they add up to 1
            for (int i = 0; i < actionProbabilities.length; i++) {
                actionProbabilities[i] = actionProbabilities[i] / sum;
                boundsSum += (int)(actionProbabilities[i] * rnd_bounds);
                bounds[i] = boundsSum;
                //if (Agent.OUTPUT)
                //  System.out.println(Agent.actions[i] + ": " + actionProbabilities[i] + " (" + boundsSum + ")");
            }
            bounds[bounds.length-1] = rnd_bounds;

            ACTIONS act = Agent.actions[0];
            int rndAct = m_rnd.nextInt(rnd_bounds);
            for (int i = 0; i < bounds.length; i++) {
                if (rndAct < bounds[i]) {
                    act = Agent.actions[i];
                    break;
                }
            }

            AsimovState stateObs = rollerState.copy();
            rollerState.advance(act);
            Agent.KB.gridAnalyser.analyse(stateObs, rollerState);

            thisDepth++;

            if(Agent.DRAW)
                Agent.drawer.add("rollouts", rollerState.getAvatarPosition(), new Color(0, 250, 0, 40));
        }

        return heuristic.evaluate(rollerState);
    }

    public double prunedRoll(SingleTreeNode selected, ElapsedCpuTimer elapsedTimer, Heuristic heuristic, ActionPruner actionPruner) {
        AsimovState rollerState = selected.state.copy();
        int thisDepth = 0;
        rlength = m_rnd.nextInt(12) + 12;

        while (elapsedTimer.remainingTimeMillis() > SingleTreeNode.time_threshold
                && !finishRollout(rollerState, thisDepth)) {
            ACTIONS[] actions = Agent.actions;
            int[] bounds = new int[actions.length];
            int sum = 0;
            for (int i = 0; i < actions.length; i++) {
                ACTIONS action = actions[i];
                int value = action == ACTIONS.ACTION_USE ? 70 : 100;


                if (actionPruner.pruned(rollerState, action))
                    value = 5;

                sum += value;
                bounds[i] = sum;
                //if (Agent.OUTPUT)
                    //System.out.println(Agent.actions[i] + ": " + bounds[i]);
            }

            ACTIONS act = ACTIONS.ACTION_NIL;
            int rndAct = m_rnd.nextInt(sum);
            for (int i = 0; i < bounds.length; i++) {
                if (rndAct < bounds[i]) {
                    act = actions[i];
                    break;
                }
            }
            //if (Agent.OUTPUT)
                //System.out.println("Rnd: " + rndAct + ", Action: " + act + "\n---");

            AsimovState stateObs = rollerState.copy();
            rollerState.advance(act);
            Agent.KB.gridAnalyser.analyse(stateObs, rollerState);

            thisDepth++;

            if (Agent.DRAW)
                Agent.drawer.add("rollouts", rollerState.getAvatarPosition(), new Color(0, 250, 0, 40));
        }

        return heuristic.evaluate(rollerState);
    }


    public double roll(AsimovState selected, ElapsedCpuTimer elapsedTimer, Heuristic heuristic, ActionPruner actionPruner, ActionHeuristic actionHeuristic) {
        AsimovState rollerState = selected.copy();
        int thisDepth = 0;
        rlength = m_rnd.nextInt(16) + 8;

        while (elapsedTimer.remainingTimeMillis() > SingleTreeNode.time_threshold
                && !finishRollout(rollerState, thisDepth)) {
            ACTIONS[] actions = Agent.actions;
            int[] bounds = new int[actions.length];
            int sum = 0;
            for (int i = 0; i < actions.length; i++) {
                ACTIONS action = actions[i];
                int value = action == ACTIONS.ACTION_USE ? 70 : 100;


                if (actionPruner.pruned(rollerState, action)) {
                    value = 5;
                }
                else { // if not pruned, we add evaluation, so better moves get higher probabilities
                    value += actionHeuristic.evaluate(rollerState, action);
                }

                sum += value;
                bounds[i] = sum;
                //if (Agent.OUTPUT)
                //System.out.println(Agent.actions[i] + ": " + bounds[i]);
            }

            ACTIONS act = ACTIONS.ACTION_NIL;
            int rndAct = m_rnd.nextInt(sum);
            for (int i = 0; i < bounds.length; i++) {
                if (rndAct < bounds[i]) {
                    act = actions[i];
                    break;
                }
            }
            //if (Agent.OUTPUT)
            //System.out.println("Rnd: " + rndAct + ", Action: " + act + "\n---");

            AsimovState stateObs = rollerState.copy();
            rollerState.advance(act);
            Agent.KB.gridAnalyser.analyse(stateObs, rollerState);

            thisDepth++;

            if (Agent.DRAW)
                Agent.drawer.add("rollouts", rollerState.getAvatarPosition(), new Color(0, 250, 0, 40));
        }

        return heuristic.evaluate(rollerState);
    }


    public double olRoll(AsimovState rootState, AsimovState selectedState, ElapsedCpuTimer elapsedTimer, Heuristic heuristic, ActionPruner actionPruner, ActionHeuristic actionHeuristic) {
        AsimovState rollerState = rootState.copy();

        // how many steps are between the selected state and the root state
        int olDiff = selectedState.getGameTick() - rootState.getGameTick();
        for (int i = olDiff; i > 0; --i) {
            // advance the actions, that were used between the root state and the selected state
            rollerState.advance(selectedState.getCompleteActionHistory().get(selectedState.getCompleteActionHistory().size() - i));
        }

        return roll(rollerState, elapsedTimer, heuristic, actionPruner, actionHeuristic);
    }


    private boolean finishRollout(AsimovState rollerState, int depth) {
        //rollout end condition.
        return depth >= rlength || rollerState.isGameOver();
    }
}

