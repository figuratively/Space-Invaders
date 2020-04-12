package game.actor;

import java.awt.*;

public abstract class DynamicObject {
    protected int x;
    protected int y;
    protected int size;

    DynamicObject(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public abstract void paint(Graphics g);

    public abstract void move(Integer... coordinates) throws OutOfScreenException;

    public boolean overlaps(DynamicObject object) {
        boolean left = object.x >= x && object.x <= x + size;
        boolean right = object.x + object.size >= x && object.x + object.size <= x + size;
        boolean top = object.y >= y && object.y <= y + size;
        boolean down = object.y + object.size >= y && object.y + object.size <= y + size;

        return (top && (right || left)) || (down && (left || right));
    }
}
