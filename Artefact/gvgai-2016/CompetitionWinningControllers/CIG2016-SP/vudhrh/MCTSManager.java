package vudhrh;

import java.util.PriorityQueue;
import java.util.Queue;

import core.game.StateObservation;
import tools.Vector2d;

public class MCTSManager {
	private static MCTSManager instance = new MCTSManager();
	/*
	public double weight[];
	public double conditioning = 0.3;
	
	public void initWeight(){
		weight = new double[Agent.NUM_ACTIONS];
		for(int i = 0; i < weight.length; i++){
			if(i == 0 && Agent.NUM_ACTIONS != 4)
				weight[i] = 0.5;
			else
				weight[i] = 1;
		}
	}
	public void tuneWeight(int action){
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
		for(int i = 0; i < Agent.NUM_ACTIONS; i ++){
			if(weight[i] - conditioning / (Agent.NUM_ACTIONS - 1) < 0.1){
				return;
			}
			if(i != tuneAction){
				weight[i] += conditioning / (Agent.NUM_ACTIONS - 1);
			}else{
				weight[i] -= conditioning / (Agent.NUM_ACTIONS - 1);
			}
		}
	}*/
	Vector2d[] rVisited;
	int idx;
	int size;
	MCTSManager(){
		size = Agent.NUM_ACTIONS-1;
		//size = 5;
		rVisited = new Vector2d[size];
		idx = 0;
	}
	
	public void push(int action, StateObservation stateObs){
		StateObservation tObs = stateObs.copy();
		tObs.advance(Agent.actions[action]);
		rVisited[idx] = tObs.getAvatarPosition();
		idx++;
		
		if(idx >= size){
			idx = 0;
		}
	}
	
	public boolean isRecentlyVisited(Vector2d tPos){
		for(int i = 0; i < rVisited.length; i++){
			if(rVisited[i] == null){
				return false;
			}
			if(rVisited[i].x == tPos.x && rVisited[i].y == tPos.y){
				return true;
			}
		}
		return false;
	}
	
	public static MCTSManager getInstance(){
		if( instance != null )
			return instance;
		else{
			instance = new MCTSManager();
		}
		return instance;
	}
}
