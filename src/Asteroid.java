import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Asteroid extends DynamicObject {
    BufferedImage bufferedImage;
    long timeAfterDestruction = 0;
    boolean isDestroyed = false;
    private final int DESTRUCTION_DELAY = 200;

    Asteroid(int x, int y, int size) {
        super(x, y, size);
        try {
            bufferedImage = ImageIO.read(new File("meteor2.png"));
        } catch (IOException e) {
            e.printStackTrace();
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
            bufferedImage = ImageIO.read(new File("meteor_destroyed.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean canDisappear() {
        return isDestroyed &&
                System.currentTimeMillis() - timeAfterDestruction >= DESTRUCTION_DELAY;
    }
}
