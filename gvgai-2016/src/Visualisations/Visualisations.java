package Visualisations;

import controllers.singlePlayer.sampleMCTS.SingleMCTSPlayer;
import controllers.singlePlayer.sampleMCTS.SingleTreeNode;
import core.game.StateObservation;
import tools.Vector2d;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Visualisations
{
    private HashMap<Vector2d, Integer> timesPointVisited = new HashMap<Vector2d, Integer>();
    private Vector<Vector2d> searchPoints = new Vector<Vector2d>();
    private Vector<Vector2d> bestPath = new Vector<Vector2d>();
    private int nodesInTree = 0;
    public boolean drawAreaSearched = true;
    public boolean drawBestActionPath = true;
    private int blockOffset = 0;


    public void renderSearchSpace(SingleMCTSPlayer MCTSPlayer, Graphics2D g)
    {
        blockOffset = MCTSPlayer.m_root.state.getBlockSize();

        // Search the tree starting at the root
        recursivelySearchTree(MCTSPlayer.m_root);

        // Draw the area that is being searched
        if(drawAreaSearched) {
            for (int i = 0; i < searchPoints.size(); i++) {
                int x = (int) searchPoints.get(i).x;
                int y = (int) searchPoints.get(i).y;
                g.draw3DRect((int) x, (int) y, MCTSPlayer.m_root.state.getBlockSize(), MCTSPlayer.m_root.state.getBlockSize(), false);
            }
        }

        // Draw the path that is the best action to take
        if(drawBestActionPath)
        {
            Vector2d oldPos = new Vector2d();
            Vector2d originPoint = new Vector2d();
            originPoint.x = 0.0;
            originPoint.y = 0.0;
            for(Map.Entry<Vector2d, Integer> entry : timesPointVisited.entrySet()) {
                Vector2d pos = entry.getKey();
                Integer visits = entry.getValue();


                if(pos != oldPos && oldPos != null && oldPos != originPoint)
                {
                    g.setStroke(new BasicStroke(visits));
                    g.drawLine((int) oldPos.x, (int)oldPos.y, (int) pos.x, (int) pos.y);
                }


                oldPos = pos;
            }
        }

        //System.out.println(nodesInTree + " : " + searchPoints.size());
        g.drawString(String.valueOf(nodesInTree), 100, 50);
        //System.out.println(MCTSPlayer.num);
        //Reset the values for next search
        searchPoints.clear();
        //timesPointVisited.clear();
        nodesInTree = 0;


    }

    private SingleTreeNode recursivelySearchTree(SingleTreeNode node)
    {
        // IF the node has a state
        if (node.state != null) {

            searchPoints.add(node.state.getAvatarPosition());
            nodesInTree++;
            timesPointVisited.put(node.state.getAvatarPosition(), node.nVisits);
        }
        else
            return null;

        int nodesChildren = 0;
        // Search the nodes children
        for(int i = 0; i < node.children.length; i++) {

            if(node.children[i] != null) {

                node = recursivelySearchTree(node.children[i]);
                nodesChildren++;
            }
        }
        System.out.println(nodesChildren);

        // Return the node after searching its children
        return node;

    }

}
