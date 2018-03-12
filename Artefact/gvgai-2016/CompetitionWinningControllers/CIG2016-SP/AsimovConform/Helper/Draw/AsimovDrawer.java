package AsimovConform.Helper.Draw;


import AsimovConform.Helper.Vector2i;
import tools.Vector2d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class AsimovDrawer {
    private HashMap<String, ArrayList<DrawItem>> data;
    private int blockSize;
    private boolean isNowDrawing;

    public AsimovDrawer(int bs) {
        clear();
        blockSize = bs;
        isNowDrawing = false;
    }


    public void add(String id, Vector2d v, Color c) {
        add(id, new Vector2i(v), c, false, null);
    }

    public void add(String id, Vector2i v, Color c) {
        add(id, v, c, false, null);
    }

    public void add(String id, ArrayList<Vector2d> list, Color c) {
        for (Vector2d v : list)
            add(id, new Vector2i(v), c, false, null);
    }

    public void add(String id, ArrayList<Vector2d> list, Color c, boolean drawNumbers) {
        for (int i = 0; i < list.size(); i++)
            add(id, new Vector2i(list.get(i)), c, false, drawNumbers ? (i+1)+"" : null);
    }

    public void add(String id, Vector2i v, Color c, boolean isGridPosition) {
        add(id, v, c, isGridPosition, null);
    }

    public void add(String id, Vector2d v, Color c, String t) {
        add(id, new Vector2i(v), c, false, t);
    }

    public void add(String id, Vector2i v, Color c, boolean isGridPosition, String t) {
        if(isNowDrawing)
            return;

        if (!data.containsKey(id))
            data.put(id, new ArrayList<>());

        if (isGridPosition)
            data.get(id).add(new DrawItem(v.mul(blockSize), c, t));
        else
            data.get(id).add(new DrawItem(v, c, t));
    }


    public Set<String> getIds() {
        return data.keySet();
    }

    public ArrayList<DrawItem> getListToPaint(String id) {
        return data.get(id);
    }

    public void clear() {
        data = new HashMap<>();
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void startDrawing() {
        isNowDrawing = true;
    }

    public void stopDrawing(boolean withClear) {
        if(withClear)
            clear();

        isNowDrawing = false;
    }
}
