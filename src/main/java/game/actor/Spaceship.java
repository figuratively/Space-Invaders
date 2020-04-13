package game.actor;

import game.ui.WindowSize;

import java.awt.*;
import java.io.IOException;

public class Spaceship extends DynamicObject {
    private boolean isDestroyed = false;

    public Spaceship(int size, String defaultImagePath, String destroyedImagePath) throws IOException {
        super(512, 600, size, defaultImagePath, destroyedImagePath);
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(isDestroyed ? bufferedDestroyedImage : bufferedDefaultImage, x, y, size, size, null);
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
    }

    public boolean getIsDestroyed() {
        return isDestroyed;
    }

    public Bullet getNewBullet() {
        return new Bullet(x + size / 2, y, 10);
    }
}
