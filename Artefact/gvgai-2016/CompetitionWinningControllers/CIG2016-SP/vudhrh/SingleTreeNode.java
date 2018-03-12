package vudhrh;

import java.util.ArrayList;
import java.util.Random;

import core.ArcadeMachine;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;
import vudhrh.MCTSManager;
public class SingleTreeNode
{
	 private static final double HUGE_NEGATIVE = -10000000.0;
	    private static final double HUGE_POSITIVE =  10000000.0;
	    public static double epsilon = 1e-6;
	    public static double egreedyEpsilon = 0.05;
	    public StateObservation state;
	    public SingleTreeNode parent;
	    public SingleTreeNode[] children;
	    
	    //for roulette wheel selection
	    //public double[] weight;
	    
	    //can't find how to use yet
	    //public double prevTotValue;
	    public double totValue;
	    
	    //save parent node's selection
	    public int pSelected;
	    
	    public int nVisits;
	    public static Random m_rnd;
	    private int m_depth;
	    protected static double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};
	    public SingleTreeNode(Random rnd) {
	        this(null, null, rnd);
	    }

	    public static int totalIters = 0;

	    public SingleTreeNode(StateObservation state, SingleTreeNode parent, Random rnd) {
	        this.state = state;
	        this.parent = parent;
	        this.m_rnd = rnd;
	        children = new SingleTreeNode[Agent.NUM_ACTIONS];
	        
	        //init roulette wheel selection
	        //weight = new double[Agent.NUM_ACTIONS];
	        //not using yet
	        //prevTotValue = 0.0;
	        //init pSelected
	        pSelected = -1;
	        totValue = 0.0;
	        if(parent != null)
	            m_depth = parent.m_depth+1;
	        else{
	            m_depth = 0;
	        }
	    }


	    public void mctsSearch(ElapsedCpuTimer elapsedTimer) {

	        double avgTimeTaken = 0;
	        double acumTimeTaken = 0;
	        long remaining = elapsedTimer.remainingTimeMillis();
	        int numIters = 0;

	        int remainingLimit = 8;
	       // int Macro = 0;
	        while(remaining > 2*avgTimeTaken && remaining > remainingLimit){
	            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
	            SingleTreeNode selected = treePolicy();
	            double delta = selected.rollOut();
	            backUp(selected, delta);
	            
	            numIters++;
	            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;

	            avgTimeTaken  = acumTimeTaken/numIters;
	            remaining = elapsedTimer.remainingTimeMillis();
	            
	        //    Macro++;
	            //System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
	        }
	        //System.out.println("-- " + numIters + " -- ( " + avgTimeTaken + ")");
	        totalIters = numIters;
	        
	        //ArcadeMachine.performance.add(numIters);
	    }

	    public SingleTreeNode treePolicy() {

	        SingleTreeNode cur = this;

	        while (!cur.state.isGameOver() && cur.m_depth < Agent.ROLLOUT_DEPTH)
	        {
	            if (cur.notFullyExpanded()) {
	                return cur.expand();
	            	//for(int i = 0; i < 2; i++){
	            	//	cur = cur.expand();
	            	//}
	            	
	            } else {
	                SingleTreeNode next = cur.uct();
	                //SingleTreeNode next = cur.egreedy();
	                cur = next;
	            }
	        }

	        return cur;
	    }


	    public SingleTreeNode expand() {

	        int bestAction = 0;
	        double bestValue = -1;

	        
	        //double uctValue = 0.5 + Agent.K * Math.sqrt( 2* Math.log(this.nVisits + 1) / (child.nVisits + this.epsilon));
	        for (int i = 0; i < children.length; i++) {
	        	//if(i != pSelected){
	        		double x = m_rnd.nextDouble();
		            
	        		//StateObservation tmp = this.state.copy();
		            //tmp.advance(Agent.actions[i]);
		            //double x = value(tmp);
		            
	        	//	double x = 0.5 + Agent.K * Math.sqrt( 2 * Math.log(this.nVisits + 1) / (children.length + 1));
	        		if (x > bestValue && children[i] == null) {
		                bestAction = i;
		                bestValue = x;
		            }
	        	//}
	        }

	        StateObservation nextState = state.copy();
	        nextState.advance(Agent.actions[bestAction]);

	        SingleTreeNode tn = new SingleTreeNode(nextState, this, this.m_rnd);
	        children[bestAction] = tn;
	        this.pSelected = oppositeDirection(bestAction);
	        //this.pSelected = oppositeDirection(bestAction);
	        return tn; 

	    }

	    public SingleTreeNode uct() {

	        SingleTreeNode selected = null;
	        double bestValue = -Double.MAX_VALUE;
	        
	        MCTSManager m = MCTSManager.getInstance();
	        //int i = 0;
	        for (SingleTreeNode child : this.children)
	        {	        	
	            double hvVal = child.totValue;
	            double childValue =  hvVal / (child.nVisits + this.epsilon);
	            double Q_value = 0.25;

	            childValue = Utils.normalise(childValue, bounds[0], bounds[1]);
	            
	            // greedyUCB1
	            /* double uctValue = childValue +
	                    Agent.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + this.epsilon)) + hvVal; */
	            
	            // minimax backup - risk seeking
	            double uctValue = Q_value * hvVal + (1 - Q_value) * childValue +
	                    Agent.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + this.epsilon));
	            
	            // basic algorithm
	            /* double uctValue = childValue +
	                    Agent.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + this.epsilon)); */

	         	//if(m.isRecentlyVisited(tPos)){
	            //	uctValue *= 0.95;
	            //}
	             
	            
	            // small sampleRandom numbers: break ties in unexpanded nodes
	            uctValue = Utils.noise(uctValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly

	            // small sampleRandom numbers: break ties in unexpanded nodes
	            if (uctValue > bestValue) {
	                selected = child;
	                bestValue = uctValue;
	                //this.pSelected = oppositeDirection(i);
	            }
	        }

	        if (selected == null)
	        {
	            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + this.children.length);
	        }

	        return selected;
	    }

	    public SingleTreeNode egreedy() {


	        SingleTreeNode selected = null;

	        if(m_rnd.nextDouble() < egreedyEpsilon)
	        {
	            //Choose randomly
	            int selectedIdx = m_rnd.nextInt(children.length);
	            selected = this.children[selectedIdx];

	        }else{
	            //pick the best Q.
	            double bestValue = -Double.MAX_VALUE;
	            for (SingleTreeNode child : this.children)
	            {
	                double hvVal = child.totValue;
	                hvVal = Utils.noise(hvVal, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
	                // small sampleRandom numbers: break ties in unexpanded nodes
	                if (hvVal > bestValue) {
	                    selected = child;
	                    bestValue = hvVal;
	                }
	            }

	        }


	        if (selected == null)
	        {
	            throw new RuntimeException("Warning! returning null: " + this.children.length);
	        }

	        return selected;
	    }
	    
	    public int oppositeDirection(int action){
	    	int tuneAction = -1;
			if(Agent.NUM_ACTIONS == 3){
				switch(action){
				case 0:
					break;
				case 1:
					tuneAction = 2;
					break;
				case 2:
					tuneAction = 1;
					break;
				}
			}else if(Agent.NUM_ACTIONS == 4){
				switch(action){
				case 0:
					tuneAction = 1;
					break;
				case 1:
					tuneAction = 0;
					break;
				case 2:
					tuneAction = 3;
					break;
				case 3:
					tuneAction = 2;
					break;
				}
			}else if(Agent.NUM_ACTIONS == 5){
				switch(action){
				case 0:
					break;
				case 1:
					tuneAction = 2;
					break;
				case 2:
					tuneAction = 1;
					break;
				case 3:
					tuneAction = 4;
					break;
				case 4:
					tuneAction = 3;
					break;
				}
			}	
	    	return tuneAction;
	    }

	    public double rollOut()
	    {
	        StateObservation rollerState = this.state.copy();
	        int thisDepth = this.m_depth;
	        int rSelected = this.pSelected;
	        while (!finishRollout(rollerState,thisDepth)) {
	           /*
	        	double bestValue = -Double.MAX_VALUE;
	            double curValue = 0;
	            int bestAction = -1;
	            for(int i = 0; i < Agent.NUM_ACTIONS; i++){
	            	if( i != rSelected ){
	            		StateObservation tmpCurState = rollerState.copy();
	            		tmpCurState.advance(Agent.actions[i]);
		            	curValue = value(tmpCurState);
		            	if(bestValue < curValue){
		            		bestValue = curValue;
		            		bestAction = i;
		            		rSelected = oppositeDirection(bestAction);
		            	}
	            	}
	            }
	            
	        	rollerState.advance(Agent.actions[bestAction]);
	           */
	        	/*
	        	int action;
	        	do{
	        		action = m_rnd.nextInt(Agent.NUM_ACTIONS);
	        	}while(pSelected == action);
	        	pSelected = oppositeDirection(action);
	        	rollerState.advance(Agent.actions[action]);
	        	*/
	        	
	        	int action = m_rnd.nextInt(Agent.NUM_ACTIONS);
	        	rollerState.advance(Agent.actions[action]);
	        	
	            thisDepth++;
	        }

	        double delta = value(rollerState);
	        
	        if(delta < bounds[0])
	            bounds[0] = delta;

	        if(delta > bounds[1])
	            bounds[1] = delta;
	        return delta;
	    }

	    public double value(StateObservation a_gameState) {

	        boolean gameOver = a_gameState.isGameOver();
	        Types.WINNER win = a_gameState.getGameWinner();
	        double rawScore = a_gameState.getGameScore();
	       
	        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
	            rawScore += HUGE_NEGATIVE;

	        if(gameOver && win == Types.WINNER.PLAYER_WINS)
	            rawScore += HUGE_POSITIVE;

	        return rawScore;
	    }

	    public boolean finishRollout(StateObservation rollerState, int depth)
	    {
	        if(depth >= Agent.ROLLOUT_DEPTH)      //rollout end condition.
	            return true;

	        if(rollerState.isGameOver())               //end of game
	            return true;

	        return false;
	    }

	    public void backUp(SingleTreeNode node, double result)
	    {
	        SingleTreeNode n = node;
	        while(n != null)
	        {
	            n.nVisits++;
	            n.totValue += result;
	            n = n.parent;
	        }
	    }


	    public int mostVisitedAction() {
	        int selected = -1;
	        double bestValue = -Double.MAX_VALUE;
	        boolean allEqual = true;
	        double first = -1;

	        for (int i=0; i<children.length; i++) {

	            if(children[i] != null)
	            {
	                if(first == -1)
	                    first = children[i].nVisits;
	                else if(first != children[i].nVisits)
	                {
	                    allEqual = false;
	                }

	                double childValue = children[i].nVisits;
	                childValue = Utils.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
	                if (childValue > bestValue) {
	                    bestValue = childValue;
	                    selected = i;
	                }
	            }
	        }

	        if (selected == -1)
	        {
	            System.out.println("Unexpected selection!");
	            selected = 0;
	        }else if(allEqual)
	        {
	            //If all are equal, we opt to choose for the one with the best Q.
	            selected = bestAction();
	        }
	        return selected;
	    }

	    public int bestAction()
	    {
	        int selected = -1;
	        double bestValue = -Double.MAX_VALUE;

	        for (int i=0; i<children.length; i++) {

	            if(children[i] != null) {
	                double childValue = children[i].totValue / (children[i].nVisits + this.epsilon);
	                childValue = Utils.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
	                if (childValue > bestValue) {
	                    bestValue = childValue;
	                    selected = i;
	                }
	            }
	        }

	        if (selected == -1)
	        {
	            System.out.println("Unexpected selection!");
	            selected = 0;
	        }

	        return selected;
	    }


	    public boolean notFullyExpanded() {
	        for (SingleTreeNode tn : children) {
	            if (tn == null) {
	                return true;
	            }
	        }

	        return false;
	    }
}
