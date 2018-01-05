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
    private HashMap numSearchTimes = new HashMap<Vector2d, Integer>();
    private Vector<Vector2d> searchPoints = new Vector<Vector2d>();
    private Vector<Vector2d> bestPath = new Vector<Vector2d>();
    private int nodesInTree = 0;
    public boolean drawAreaSearched = true;
    public boolean drawBestActionPath = true;


    public void renderSearchSpace(StateObservation SO, SingleMCTSPlayer MCTSPlayer, Graphics2D g)
    {

        // Search the tree starting at the root
        recursivelySearchTree(MCTSPlayer.m_root);

        // Draw the area that is being searched
        if(drawAreaSearched) {
            for (int i = 0; i < searchPoints.size(); i++) {
                int x = (int) searchPoints.get(i).x;
                int y = (int) searchPoints.get(i).y;
                g.draw3DRect((int) x, (int) y, SO.getBlockSize(), SO.getBlockSize(), false);
            }
        }

        // Draw the path that is the best action to take
        if(drawBestActionPath)
        {

            for (int i = 0; i < searchPoints.size(); i++)
            {
                if(i + 1 < searchPoints.size())
                    g.drawLine((int) searchPoints.elementAt(i).x + 25,(int)  searchPoints.elementAt(i).y + 25,(int) searchPoints.elementAt(i + 1).x + 25, (int) searchPoints.elementAt(i + 1).y + 25);
            }
        }

        //System.out.println(nodesInTree + " : " + searchPoints.size() + " : " + NumSearchTimes.size());
        g.drawString(String.valueOf(nodesInTree), 100, 50);
        //System.out.println(MCTSPlayer.num);
        //Reset the values for next search
        searchPoints.clear();
        nodesInTree = 0;


    }

    private SingleTreeNode recursivelySearchTree(SingleTreeNode node)
    {
        // IF the node has a state
        if (node.state != null) {

            searchPoints.add(node.state.getAvatarPosition());
            nodesInTree++;


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
