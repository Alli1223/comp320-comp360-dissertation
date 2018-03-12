package AsimovConform.Helper.Draw;


import AsimovConform.Helper.Vector2i;
import tools.Vector2d;

import java.awt.Color;

public class DrawItem extends Vector2i {
    private Color color;
    private String text;

    DrawItem(Vector2i v, Color c, String t) {
        super(v);

        color = c;
        text = t;
    }

    DrawItem(Vector2i v, Color c) {
        this(v, c, null);
    }

    DrawItem(Vector2d v, Color c) {
        this(new Vector2i(v), c, null);
    }

    public Color getColor() {
        return color;
    }

    public String getText() {
        return text;
    }
}
