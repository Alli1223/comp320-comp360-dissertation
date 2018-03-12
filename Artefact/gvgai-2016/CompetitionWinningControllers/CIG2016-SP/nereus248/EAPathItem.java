package nereus248;

import java.util.ArrayList;

import ontology.Types.ACTIONS;

public class EAPathItem {

	public ArrayList<ACTIONS> path;
	public double score;
//	private int visits;
	
	public EAPathItem() {
		score = 0;
	//	visits = 0;
		path = new ArrayList<ACTIONS>();
	}
	
	@SuppressWarnings("unchecked")
	public EAPathItem copy() {
		EAPathItem copy = new EAPathItem();
		copy.score = score;
	//	copy.visits = visits;
		copy.path = new ArrayList<ACTIONS>(path);
		return copy;
	}
	
	public void extend(ACTIONS action) {
		path.add(action);
	}
	
	public void prependAction(ACTIONS action) {
		path.add(0,action);
	}
	
	public void chopFront() {
		path.remove(0);
	}
	
	public void mutateLocation(int idx, ACTIONS action) {
		if (idx > 0 && idx < path.size())
			path.set(idx, action);
	}
	
/*	public void setScore(double score) {
		if (visits == 0) {
			this.score = score;
			visits++;
		} else {
			this.score = 0.5*this.score + 0.5*score;
			visits++;
		}
	}*/
}
