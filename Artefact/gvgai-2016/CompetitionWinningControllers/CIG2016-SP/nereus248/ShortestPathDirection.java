package nereus248;

import ontology.Types.ACTIONS;

public class ShortestPathDirection {
	
	int reverseDirection_x;
	int reverseDirection_y;
	ACTIONS action;
	float gScore;
	
	public ShortestPathDirection(ACTIONS action, int reverse_x, int reverse_y, float gScore) {
		this.reverseDirection_x = reverse_x;
		this.reverseDirection_y = reverse_y;
		this.action = action;
		this.gScore = gScore;
	}
}
