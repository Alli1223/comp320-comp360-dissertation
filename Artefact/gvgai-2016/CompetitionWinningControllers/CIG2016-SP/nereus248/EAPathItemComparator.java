package nereus248;

import java.util.Comparator;

public class EAPathItemComparator implements Comparator<EAPathItem> {

	@Override
    public int compare(EAPathItem item1, EAPathItem item2)
    {
        if (item1.score < item2.score){
            return -1;
        }
        else if (item1.score > item2.score) {
        	return 1;
        }
        return 0;
    }
}


