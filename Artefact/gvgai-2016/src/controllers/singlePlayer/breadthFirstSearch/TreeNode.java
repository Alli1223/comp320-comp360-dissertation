/**
 * Node used by the BFS
 * 
 * @author Florian Kirchgessner (Alias: Number27)
 * @version customGA3
 */

package controllers.singlePlayer.breadthFirstSearch;

import core.game.StateObservation;
import ontology.Types;

import java.util.LinkedList;

public class TreeNode
{
	public StateObservation currentState;
	public boolean isExplored = false;
	public double score = 0;
    private TreeNode parent;
    public TreeNode[] children = new TreeNode[8];
    private static double winScore = 100000.0;

    //! Stores the actions
	public LinkedList<Types.ACTIONS> actions;
	
	
	public TreeNode(StateObservation stateObs, TreeNode parentNode)
	{
		this.isExplored = true;
        this.parent = parentNode;
        this.score = stateObs.getGameScore();

        //If the state is good or bad
        if(stateObs.getGameWinner() == Types.WINNER.PLAYER_WINS)
        	this.score = winScore;
        else if (stateObs.isGameOver())
        	this.score = -winScore;
		this.currentState = stateObs;

	}

}