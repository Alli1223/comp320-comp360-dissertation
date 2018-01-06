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

    //! Depth search testing
    private int searchDepthLevel = 0;
    private int deepestSearchLevel = 0;


    public void renderSearchSpace(SingleMCTSPlayer MCTSPlayer, Graphics2D g)
    {
        if(MCTSPlayer.m_root.state != null)
            blockOffset = MCTSPlayer.m_root.state.getBlockSize() / 2;

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
        if(drawBestActionPath) {
            Vector2d oldPos = new Vector2d();
            Vector2d originPoint = new Vector2d();
            originPoint.x = 0.0;
            originPoint.y = 0.0;


            // Loop through the hashmap and draw the lines between the most visited points
            for (Map.Entry<Vector2d, Integer> entry : timesPointVisited.entrySet()) {
                Vector2d pos = entry.getKey();
                Integer visits = entry.getValue();

                // If the oldPoint isn't null then draw it
                if (!oldPos.equals(originPoint))
                {
                    g.setStroke(new BasicStroke(visits));
                    g.setPaint(new Color(200,100,0));
                    g.drawLine((int) oldPos.x + blockOffset, (int) oldPos.y+ blockOffset, (int) pos.x+ blockOffset, (int) pos.y+ blockOffset);
                }

                oldPos = pos;
            }

        }

        //System.out.println(nodesInTree + " : " + searchPoints.size());
        g.drawString(String.valueOf(timesPointVisited.size()), 100, 50);
        System.out.println(searchDepthLevel);


        //Reset the values for next search
        searchPoints.clear();

        timesPointVisited.clear();
        nodesInTree = 0;
        deepestSearchLevel = 0;
    }



    //! This function will run until it has searched the whole tree
    private SingleTreeNode recursivelySearchTree(SingleTreeNode node)
    {
        // IF the node has a state
        if (node.state != null)
        {
            //System.out.println(node.children.length);
            searchPoints.add(node.state.getAvatarPosition());
            nodesInTree++;
            timesPointVisited.put(node.state.getAvatarPosition(), node.nVisits);

            SingleTreeNode depthTest = node;
            searchDepthLevel = 0;
            while(depthTest.parent != null)
            {
                depthTest = depthTest.parent;
                if(searchDepthLevel >= deepestSearchLevel)
                    deepestSearchLevel = searchDepthLevel;
                searchDepthLevel++;
            }
        }


        // Search the nodes children
        SingleTreeNode reNode = null;
        for(int i = 0; i < node.children.length; i++) {
            if(node.children[i] != null) {
                reNode = recursivelySearchTree(node.children[i]);

            }
        }

        // Return the node after searching its children
        return reNode;
    }

}
