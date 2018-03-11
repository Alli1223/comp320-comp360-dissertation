package DissertationFiles;

import controllers.singlePlayer.sampleMCTS.SingleMCTSPlayer;
import controllers.singlePlayer.sampleMCTS.SingleTreeNode;
import tools.Vector2d;

import java.awt.*;
import java.util.*;

public class Visualisations
{
    //! A Hashmap that stores the times a pont was visisted as well as the location of that point
    private HashMap<Vector2d, Integer> timesPointVisited = new HashMap<Vector2d, Integer>();
    //! A list of points used for drawing the search space
    private Vector<Vector2d> searchPoints = new Vector<Vector2d>();

    //! Booleans for choosing what should be rendered over the game
    private boolean drawAreaSearched = false;
    private boolean drawBestActionPath = false;
    private boolean drawPreviousLocations = true;

    //! Block offset for drawing in the center of the cells
    private int blockOffset = 0;

    //! Depth search testing variables
    private int searchDepthLevel = 0;
    private int deepestSearchLevel = 0;
    private int nodesInTree = 0;
    private SingleTreeNode deepestNode = null;

    //! Renders the tree searches over the search space
    public void renderSearchSpace(SingleMCTSPlayer MCTSPlayer, Graphics2D g)
    {
        int cellSize = 0;
        if (MCTSPlayer.m_root.state != null)
        {
            // Get the cellSize
            blockOffset = MCTSPlayer.m_root.state.getBlockSize() / 2;
            cellSize = MCTSPlayer.m_root.state.getBlockSize();
        }


        // Search the tree starting at the root
        recursivelySearchTree(MCTSPlayer.m_root);

        // Draw the area that is being searched
        if (drawAreaSearched)
        {
            for (Map.Entry<Vector2d, Integer> entry : timesPointVisited.entrySet())
            {
                Vector2d pos = entry.getKey();
                Integer visits = entry.getValue();
                if (visits < 3)
                    g.setPaint(new Color(10, 100, 0));
                if (visits >= 3 && visits <= 10)
                    g.setPaint(new Color(242, 233, 0));
                if (visits > 10)
                    g.setPaint(new Color(255, 20, 58));


                g.draw3DRect((int) pos.x, (int) pos.y, cellSize, cellSize, false);
            }
        }

        // Draw the path that is the best action to take
        if (drawBestActionPath)
        {
            Vector2d oldPos = new Vector2d();
            Vector2d originPoint = new Vector2d();
            originPoint.x = 0.0;
            originPoint.y = 0.0;

            // Get the path of nodes that were most visited
            Vector2d[] points = GetPathFromNode(MCTSPlayer.m_root);

            // Loop through the points and draw lines between them
            if (points.length > 1)
                for (int i = 0; i < points.length; i++)
                {
                    if (points[i] != null)
                        // If the point does not equal 0,0
                        if (!oldPos.equals(originPoint))
                        {
                            g.setStroke(new BasicStroke(10));
                            g.setPaint(new Color(200, 100, 0));
                            g.drawLine((int) oldPos.x + blockOffset, (int) oldPos.y + blockOffset, (int) points[i].x + blockOffset, (int) points[i].y + blockOffset);

                        }
                    oldPos = points[i];
                }
        }

        if (drawPreviousLocations)
        {
            DrawPreviousLocations(g);
        }


        //System.out.println(nodesInTree + " : " + searchPoints.size());
        //Reset the values for next search
        searchPoints.clear();
        timesPointVisited.clear();
        nodesInTree = 0;
        deepestSearchLevel = 0;
    }

    //Overloaded function to just render the overlay, for any game
    public void renderSearchSpace(Graphics2D g)
    {
        if (drawPreviousLocations)
        {
            DrawPreviousLocations(g);
        }
        // Do other stuff here for other controllers
    }

    private void DrawPreviousLocations(Graphics2D g)
    {
        int cellSize = DataCollection.getInstance().getBlockSize();

        // Loop through the hashMap and get the positions
        if (DataCollection.getInstance().cellsVisited != null)
        {
            for (Map.Entry<String, Integer> entry : DataCollection.getInstance().cellsVisited.entrySet())
            {
                String posString = entry.getKey();

                String[] parts = posString.split(":");

                int x1 = (int) Double.parseDouble(parts[0].trim());
                int y1 = (int) Double.parseDouble(parts[1].trim());


                Double value = Double.parseDouble(entry.getValue().toString());
                value /= DataCollection.getInstance().totalCellsExplored;
                value *= 1000;
                if (value > 254.0)
                    value = 254.0;

                g.setPaint(new Color(value.intValue(), 50, 50, 150));
                g.fillRect((int) x1, (int) y1, cellSize, cellSize);

                g.;

            }
        }

    }



    //! This function will run until it has searched the whole tree (Depth first search of the tree search)
    private SingleTreeNode recursivelySearchTree(SingleTreeNode node)
    {
        // IF the node has a state, get its values
        if (node.state != null) {
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


    //! Returns a list of points that the most visited nodes are
    private Vector2d[] GetPathFromNode(SingleTreeNode node)
    {
        // Create an array of points to return
        Vector2d[] res = new Vector2d[100];

        int j = 0;
        // Loop until there are no more nodes
        while (node != null)
        {
            int best = node.bestAction();

            if(node.children[best] != null) {
                res[j] = node.children[best].state.getAvatarPosition();

                // Temp test code
                if(node.children[best].children[node.children[best].bestAction()] != null)
                    res[j + 1] = node.children[best].children[node.children[best].bestAction()].state.getAvatarPosition();

                node = node.children[best];

                j++;
            }
            else
                break;
        }

        // Remove any empty values from the array
        res = Arrays.stream(res).filter(s -> (s != null)).toArray( Vector2d[]::new);

        return res;
    }
}
