package nereus248;

import java.util.Comparator;

import core.game.StateObservation;
import tools.Pair;
import tools.Vector2d;

public class AStarStateComparator implements Comparator<OpenSetItem>
{
	@Override
    public int compare(OpenSetItem item1, OpenSetItem item2)
    {
        if (item1.fScore < item2.fScore){
            return -1;
        }
        else if (item1.fScore > item2.fScore) {
        	return 1;
        }
        return 0;
    }
}
