import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Spaceship extends DynamicObject {
    private BufferedImage bufferedImage;
    private float deltaTime;
    private boolean isDestroyed = false;

    Spaceship(int size) {
        super(512, 600, size);
        try {
            bufferedImage = ImageIO.read(new File("rocket.png"));
        } catch (IOException e) {
            e.printStackTrace();
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
            bufferedImage = ImageIO.read(new File("rocket_destroyed.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getIsDestroyed() {
        return isDestroyed;
    }

//    public boolean overlaps(Asteroid asteroid) {
//        boolean left = asteroid.x >= x && asteroid.x <= x + size;
//        boolean right = asteroid.x + asteroid.size >= x && asteroid.x + asteroid.size <= x + size;
//        boolean top = asteroid.y >= y && asteroid.y <= y + size;
//        boolean down = asteroid.y + asteroid.size >= y && asteroid.y + asteroid.size <= y + size;
//
//        return (top && (right || left)) || (down && (left || right));
//    }

    public Bullet getNewBullet() {
        return new Bullet(x + size / 2, y, 10);
    }
}
