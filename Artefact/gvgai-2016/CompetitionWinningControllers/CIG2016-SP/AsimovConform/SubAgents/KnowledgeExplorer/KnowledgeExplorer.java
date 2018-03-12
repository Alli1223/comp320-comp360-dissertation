package AsimovConform.SubAgents.KnowledgeExplorer;

import AsimovConform.Agent;
import AsimovConform.Helper.AsimovState;
import AsimovConform.KnowledgeBase.GridAnalyser;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

import java.util.Random;

public class KnowledgeExplorer {

    public boolean deterministic = true;
    private Random rnd = new Random();
    private AsimovState startState;
    private GridAnalyser gridAnalyser;

    public KnowledgeExplorer(AsimovState startState, GridAnalyser gridAnalyser) {
        this.startState = startState;
        this.gridAnalyser = gridAnalyser;
    }

    public void explore(ElapsedCpuTimer timer) {
        int width = startState.getWorldDimension().width / startState.getBlockSize();
        doActionRepeatedly(ACTIONS.ACTION_RIGHT, width, timer);
        doActionRepeatedly(ACTIONS.ACTION_LEFT, width, timer);

        int height = startState.getWorldDimension().height / startState.getBlockSize();
        doActionRepeatedly(ACTIONS.ACTION_UP, height, timer);
        doActionRepeatedly(ACTIONS.ACTION_DOWN, height, timer);

        AsimovState nilTryState = startState.copyAndAdvance(ACTIONS.ACTION_NIL);
        deterministic &= nilTryState.hashCode() == nilTryState.copyAndAdvance(ACTIONS.ACTION_NIL).hashCode();

        deterministic &= doActionSequenceRepeatedly(Agent.actions, 2, timer) == doActionSequenceRepeatedly(Agent.actions, 2, timer);

        ACTIONS[] rndSequence = new ACTIONS[100];
        for (int i = 0; i < rndSequence.length; ++i) {
            rndSequence[i] = Agent.actions[rnd.nextInt(Agent.actions.length)];
        }
        deterministic &= doActionSequenceRepeatedly(rndSequence, 1, timer) == doActionSequenceRepeatedly(rndSequence, 1, timer);

        for (int i = 0; i < rndSequence.length; ++i) {
            rndSequence[i] = Agent.actions[rnd.nextInt(Agent.actions.length)];
        }
        deterministic &= doActionSequenceRepeatedly(rndSequence, 1, timer) == doActionSequenceRepeatedly(rndSequence, 1, timer);
    }

    private int doActionRepeatedly(ACTIONS action, int repeats, ElapsedCpuTimer timer) {
        AsimovState state = startState.copy();
        for (int i = 0; i < repeats && Agent.agentInitTime > timer.remainingTimeMillis(); ++i) {
            AsimovState oldState = state;
            if (!state.isGameOver()) {
                state = state.copyAndAdvance(action);
                if (state.getStateObservation().getNPCPositions() != null
                        && state.getStateObservation().getNPCPositions().length != 0) {
                    // detected NPCs - non-bfsSolvable
                    deterministic = false;
                }
                gridAnalyser.analyse(oldState, state);
            }
        }
        return state.hashCode();
    }

    private int doActionSequenceRepeatedly(ACTIONS[] actions, int repeats, ElapsedCpuTimer timer) {
        AsimovState state = startState.copy();
        for (int i = 0; i < repeats; ++i) {
            for (ACTIONS action : actions) {
                if (Agent.agentInitTime > timer.remainingTimeMillis())
                    return state.hashCode();

                AsimovState oldState = state;
                if (!state.isGameOver()) {
                    state = state.copyAndAdvance(action);
                    gridAnalyser.analyse(oldState, state);
                }
            }
        }
        return state.hashCode();
    }

}
