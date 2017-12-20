package Visualisations;

import controllers.singlePlayer.sampleMCTS.SingleMCTSPlayer;
import controllers.singlePlayer.sampleMCTS.SingleTreeNode;
import core.game.StateObservation;
import tools.Vector2d;

import java.awt.*;
import java.util.HashMap;

public class Visualisations
{
    private HashMap searchPoints = new HashMap<Vector2d, Vector2d>();

    private SingleTreeNode rootNode;

    public void renderSearchSpace(StateObservation SO, SingleMCTSPlayer MCTSPlayer, Graphics2D g)
    {
        rootNode = MCTSPlayer.m_root;
        recursivelySearchTheTree(rootNode);

        recursivelySearchTheTree(rootNode);
    }

    private SingleTreeNode recursivelySearchTheTree(SingleTreeNode rootNode)
    {

        SingleTreeNode node = rootNode;
        for(int i = 0; i < node.children.length; i++)
        {
            node = recursivelySearchTheTree(node.children[i]);
        }

        return node;
    }

    private void searchChildren(SingleTreeNode STN)
    {

    }
}
