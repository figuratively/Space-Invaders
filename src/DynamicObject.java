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

    public abstract void move() throws OutOfScreenException;
}
