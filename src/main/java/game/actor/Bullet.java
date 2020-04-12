package game.actor;

import java.awt.*;

public class Bullet extends DynamicObject {
    public Bullet(int x, int y, int size) {
        super(x, y - 30, size);
    }

    @Override
    public void move(Integer... coordinates) throws OutOfScreenException {
        y--;
        if(y <= 0) throw new OutOfScreenException();
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(new Color(36, 240, 0, 166));
        g.fillRect(x, y + 30, size, size * 3);
    }
}
