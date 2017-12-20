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
    //private HashMap searchPoints = new HashMap<Vector2d, StateObservation>();
    private Vector<Vector2d> searchPoints = new Vector<Vector2d>();
    private int nodesInTree = 0;

    public void renderSearchSpace(StateObservation SO, SingleMCTSPlayer MCTSPlayer, Graphics2D g)
    {
        SingleTreeNode rootNode = MCTSPlayer.m_root;
        recursivelySearchTheTree(MCTSPlayer.m_root);

        for(int i = 0; i < searchPoints.size(); i++)
        {
            int x = (int) searchPoints.get(i).x;
            int y = (int) searchPoints.get(i).y;
            g.draw3DRect((int) x, (int) y, SO.getBlockSize(), SO.getBlockSize(), false);
        }
        searchPoints.clear();
        System.out.println(nodesInTree);
        nodesInTree = 0;

    }

    private SingleTreeNode recursivelySearchTheTree(SingleTreeNode node)
    {

        for(int i = 0; i < node.children.length; i++)
        {
            if(node.children[i] != null)
            {
                Vector2d pos = new Vector2d();

                pos.x = (int) node.state.getAvatarPosition().x;
                pos.y = (int) node.state.getAvatarPosition().y;

                searchPoints.add(pos);
                nodesInTree++;
                node = recursivelySearchTheTree(node.children[i]);
            }

        }

        return node;
    }

}
