package Visualisations;

import controllers.singlePlayer.sampleMCTS.SingleMCTSPlayer;
import controllers.singlePlayer.sampleMCTS.SingleTreeNode;
import core.game.StateObservation;
import tools.Vector2d;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Visualisations
{
    private HashMap<Vector2d, Integer> timesPointVisited = new HashMap<Vector2d, Integer>();
    private Vector<Vector2d> searchPoints = new Vector<Vector2d>();
    private Vector<Vector2d> bestPath = new Vector<Vector2d>();
    private int nodesInTree = 0;
    public boolean drawAreaSearched = true;
    public boolean drawBestActionPath = false;
    private int blockOffset = 0;

    //! Depth search testing
    private int searchDepthLevel = 0;
    private int deepestSearchLevel = 0;
    private SingleTreeNode deepestNode = null;


    public void renderSearchSpace(SingleMCTSPlayer MCTSPlayer, Graphics2D g)
    {
        if(MCTSPlayer.m_root.state != null)
            blockOffset = MCTSPlayer.m_root.state.getBlockSize() / 2;


        // Search the tree starting at the root
        recursivelySearchTree(MCTSPlayer.m_root);

        // Draw the area that is being searched
        if(drawAreaSearched) {
            for (Map.Entry<Vector2d, Integer> entry : timesPointVisited.entrySet()) {
                Vector2d pos = entry.getKey();
                Integer visits = entry.getValue();
                if(visits < 3)
                    g.setPaint(new Color(10, 100, 0));
                if(visits >= 3 && visits <= 10)
                    g.setPaint(new Color(242, 233, 0));
                if(visits > 10)
                    g.setPaint(new Color(255, 20, 58));


                g.draw3DRect((int) pos.x, (int) pos.y, MCTSPlayer.m_root.state.getBlockSize(), MCTSPlayer.m_root.state.getBlockSize(), false);
            }
            /*
            for (int i = 0; i < searchPoints.size(); i++) {
                int x = (int) searchPoints.get(i).x;
                int y = (int) searchPoints.get(i).y;
                g.draw3DRect((int) x, (int) y, MCTSPlayer.m_root.state.getBlockSize(), MCTSPlayer.m_root.state.getBlockSize(), false);
            }
            */

        }

        // Draw the path that is the best action to take
        if(drawBestActionPath)
        {

            Vector2d oldPos = new Vector2d();
            Vector2d originPoint = new Vector2d();
            originPoint.x = 0.0;
            originPoint.y = 0.0;


            Vector2d[] points = GetPathFromNode(deepestNode);


            /*
            for (int i = 0; i < points.length; i++) {
                if (points[i] != null)
                    if (!points[0].equals(originPoint)) {
                        g.setStroke(new BasicStroke(10));
                        g.setPaint(new Color(200, 100, 0));
                        g.drawLine((int) oldPos.x + blockOffset, (int) oldPos.y + blockOffset, (int) points[i].x + blockOffset, (int) points[i].y + blockOffset);

                    }
                oldPos = points[i];
            }
            */






            // Loop through the hashmap and draw the lines between the most visited points
            for (Map.Entry<Vector2d, Integer> entry : timesPointVisited.entrySet()) {
                Vector2d pos = entry.getKey();
                Integer visits = entry.getValue();

                // If the oldPoint isn't null then draw it
                if (!oldPos.equals(originPoint))
                {
                    g.setStroke(new BasicStroke(visits));
                    g.setPaint(new Color(visits,visits / 2, visits * 2));
                    g.drawLine((int) oldPos.x + blockOffset, (int) oldPos.y+ blockOffset, (int) pos.x+ blockOffset, (int) pos.y+ blockOffset);
                }

                oldPos = pos;
            }

        }

        //System.out.println(nodesInTree + " : " + searchPoints.size());
        g.drawString(String.valueOf("Current Search Depth Level: " + searchDepthLevel + ". Max Depth: " + deepestSearchLevel), 100, 50);



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
        if (node.state != null) {
            //System.out.println(node.children.length);
            searchPoints.add(node.state.getAvatarPosition());
            nodesInTree++;

            timesPointVisited.put(node.state.getAvatarPosition(), (int) node.totValue);



            // Create a copy of node and reset depthLevel
            SingleTreeNode depthTest = node;
            searchDepthLevel = 0;
            // If the node has a parent then check to see if it is the deepest node in the tree
            while (depthTest.parent != null)
            {
                if (searchDepthLevel >= deepestSearchLevel) {
                    deepestSearchLevel = searchDepthLevel;
                    deepestNode = depthTest;



                }



                depthTest = depthTest.parent;

                searchDepthLevel++;
            }

        }


        // Search the nodes children
        SingleTreeNode reNode = null;
        for(int i = 0; i < node.children.length; i++) {
            if (node.children[i] != null)
                reNode = recursivelySearchTree(node.children[i]);
        }

        // Return the node after searching its children
        return reNode;
    }


    private Vector2d[] GetPathFromNode(SingleTreeNode node)
    {
        // Create an array of points to return
        Vector2d[] res = new Vector2d[searchDepthLevel];

        int i = 0;
        while (node.parent != null) {
            if (node.state != null) {
                res[i] = node.state.getAvatarPosition();
                i++;
            }


            node = node.parent;


        }

        res = Arrays.stream(res)
                .filter(s -> (s != null))
                .toArray( Vector2d[]::new);

        return res;
    }

}
