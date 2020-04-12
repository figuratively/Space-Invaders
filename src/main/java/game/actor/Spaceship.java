package game.actor;

import game.WindowSize;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Spaceship extends DynamicObject {
    private BufferedImage bufferedImage;
    private boolean isDestroyed = false;

    public Spaceship(int size) {
        super(512, 600, size);
        try {
            InputStream stream = ClassLoader
                    .getSystemClassLoader()
                    .getResourceAsStream("pictures/spaceship.png");
            if(stream != null)
                bufferedImage = ImageIO.read(new BufferedInputStream(stream));
            else
                throw new IOException();
        } catch (IOException e) {
            System.err.println("Couldn't load picture.");
            System.exit(1);
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(bufferedImage, x, y, size, size, null);
    }

    @Override
    public void move(Integer... coordinates) throws OutOfScreenException {
        int x = coordinates[0];
        boolean isXOnTheScreen = x >= 0 && x + size <= WindowSize.WIDTH.getSize();

        if (isXOnTheScreen) {
            this.x = x;
        } else {
            throw new OutOfScreenException();
        }
    }

    public void destroy() {
        isDestroyed = true;
        try {
            InputStream stream = ClassLoader
                    .getSystemClassLoader()
                    .getResourceAsStream("pictures/spaceship_destroyed.png");
            if(stream != null)
                bufferedImage = ImageIO.read(new BufferedInputStream(stream));
            else
                throw new IOException();
        } catch (IOException e) {
            System.err.println("Couldn't load picture.");
        }
    }

    public boolean getIsDestroyed() {
        return isDestroyed;
    }

    public Bullet getNewBullet() {
        return new Bullet(x + size / 2, y, 10);
    }
}
