package nereus248;

import ontology.Types.ACTIONS;

public class OpenSetItem {

	public int x;
	public int y;
	public float gScore;
	public float fScore;
	public ACTIONS previousAction;
	
		
	public OpenSetItem(int x, int y, float f_score, float g_score, ACTIONS previousAction){
		this.x = x;
		this.y = y;
		this.gScore = g_score;
		this.fScore = f_score;
		this.previousAction = previousAction;
	}
	
}
