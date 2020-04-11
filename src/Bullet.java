import java.awt.*;

public class Bullet extends DynamicObject {
    Bullet(int x, int y, int size) {
        super(x, y, size);
    }

    @Override
    public void move() throws OutOfScreenException {
        y--;
        if(y <= 0) throw new OutOfScreenException();
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(new Color(240, 60, 0));
        g.fillOval(x, y, 10, 10);
    }
}
