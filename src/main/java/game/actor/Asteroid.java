package game.actor;

import game.WindowSize;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Asteroid extends DynamicObject {
    BufferedImage bufferedImage;
    long timeAfterDestruction = 0;
    boolean isDestroyed = false;
    private final int DESTRUCTION_DELAY = 200;

    public Asteroid(int x, int y, int size) {
        super(x, y, size);
        try {
            InputStream stream = ClassLoader
                    .getSystemClassLoader()
                    .getResourceAsStream("pictures/meteor2.png");
            if(stream != null)
                bufferedImage = ImageIO.read(new BufferedInputStream(stream));
            else
                throw new IOException();
        } catch (IOException e) {
            System.err.println("Couldn't load picture.1");
            System.exit(1);
        }
    }

    @Override
    public void move(Integer... coordinates) throws OutOfScreenException {
        if(!isDestroyed) y++;
        if(y >= WindowSize.HEIGHT.getSize()) throw new OutOfScreenException();
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(bufferedImage, x, y, size * (isDestroyed ? 2 : 1), size * 2, null);
    }

    public void destroy() {
        timeAfterDestruction = System.currentTimeMillis();
        isDestroyed = true;
        try {
            InputStream stream = ClassLoader
                    .getSystemClassLoader()
                    .getResourceAsStream("pictures/meteor_destroyed.png");
            if(stream != null)
                bufferedImage = ImageIO.read(new BufferedInputStream(stream));
            else
                throw new IOException();
        } catch (IOException e) {
            System.err.println("Couldn't load picture.");
            System.exit(1);
        }
    }

    public boolean canDisappear() {
        return isDestroyed &&
                System.currentTimeMillis() - timeAfterDestruction >= DESTRUCTION_DELAY;
    }
}
