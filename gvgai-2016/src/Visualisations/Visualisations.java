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

    public void renderSearchSpace(StateObservation SO, SingleMCTSPlayer MCTSPlayer, Graphics2D g)
    {
        if(SO != null)
            g.draw3DRect((int) SO.getAvatarPosition().x, (int) SO.getAvatarPosition().y, SO.getBlockSize(), SO.getBlockSize(), false);
    }

    private void recursivelySearchTheTree()
    {

        
    }
}
