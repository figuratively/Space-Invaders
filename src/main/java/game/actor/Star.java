package game.actor;

import game.ui.WindowSize;

import java.awt.*;
import java.util.Random;

public class Star extends DynamicObject {
    int moveVelocity;
    int brightness;

    public Star(int x, int y, int size) {
        super(x, y, size);
        Random randomMove = new Random();
        moveVelocity = randomMove.nextInt(2) + 1;
        brightness = moveVelocity == 2 ? 30 : 15;
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(new Color(brightness, brightness, brightness));
        g.fillRect(x, y, size, size);
    }

    @Override
    public void move(Integer... coordinates) throws OutOfScreenException {
        y += moveVelocity;
        if(y >= WindowSize.HEIGHT.getSize()) throw new OutOfScreenException();
    }
}
