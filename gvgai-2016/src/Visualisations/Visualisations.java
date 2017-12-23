package Visualisations;

import controllers.singlePlayer.sampleMCTS.SingleMCTSPlayer;
import controllers.singlePlayer.sampleMCTS.SingleTreeNode;
import core.game.StateObservation;
import tools.Vector2d;

import java.awt.*;
import java.util.HashMap;
import java.util.Vector;

public class Visualisations
{
    private HashMap NumSearchTimes = new HashMap<Vector2d, Integer>();
    private Vector<Vector2d> searchPoints = new Vector<Vector2d>();
    private int nodesInTree = 0;
    public boolean drawAreaSearched = true;
    public boolean drawBestActionPath = false;


    public void renderSearchSpace(StateObservation SO, SingleMCTSPlayer MCTSPlayer, Graphics2D g)
    {

        recursivelySearchTree(MCTSPlayer.m_root);

        if(drawAreaSearched) {
            for (int i = 0; i < searchPoints.size(); i++) {
                int x = (int) searchPoints.get(i).x;
                int y = (int) searchPoints.get(i).y;
                g.draw3DRect((int) x, (int) y, SO.getBlockSize(), SO.getBlockSize(), false);
            }
        }

        if(drawBestActionPath)
        {
            for (int i = 0; i < searchPoints.size(); i++)
            {
                if(i + 1 < searchPoints.size())
                    g.drawLine((int) searchPoints.elementAt(i).x,(int)  searchPoints.elementAt(i).y,(int) searchPoints.elementAt(i + 1).x, (int) searchPoints.elementAt(i + 1).y);
            }
        }

        //System.out.println(nodesInTree + " : " + searchPoints.size() + " : " + NumSearchTimes.size());
        searchPoints.clear();

        nodesInTree = 0;

    }

    private SingleTreeNode recursivelySearchTree(SingleTreeNode node)
    {

        // IF the node has a state
        if (node.state != null) {

            searchPoints.add(node.state.getAvatarPosition());
            nodesInTree++;

            //if(node != null)
                //System.out.println(node.nVisits + "     " + node.bestAction());
        }




        // Search the nodes children
        for(int i = 0; i < node.children.length ; i++) {

            if(node.children[i] != null) {
                node = recursivelySearchTree(node.children[i]);
            }


        }

        // Return the node after searching its children
        return node;

    }

}
