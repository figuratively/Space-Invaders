package game.actor;

import game.ui.WindowSize;

import java.awt.*;
import java.io.IOException;

public class Asteroid extends DynamicObject {
    long timeAfterDestruction = 0;
    boolean isDestroyed = false;
    private final int DESTRUCTION_DELAY = 200;

    public Asteroid(int x, int y, int size, String defaultImagePath, String destroyedImagePath) throws IOException {
        super(x, y, size, defaultImagePath, destroyedImagePath);
    }

    @Override
    public void move(Integer... coordinates) throws OutOfScreenException {
        if(!isDestroyed) y++;
        if(y >= WindowSize.HEIGHT.getSize()) throw new OutOfScreenException();
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(isDestroyed ? bufferedDestroyedImage : bufferedDefaultImage, x, y, size * (isDestroyed ? 2 : 1), size * 2, null);
    }

    public void destroy() {
        timeAfterDestruction = System.currentTimeMillis();
        isDestroyed = true;
    }

    public boolean canDisappear() {
        return isDestroyed &&
                System.currentTimeMillis() - timeAfterDestruction >= DESTRUCTION_DELAY;
    }
}
