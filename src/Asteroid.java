import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Asteroid extends DynamicObject implements RigidBody<Bullet> {
    BufferedImage bufferedImage;

    Asteroid(int x, int y, int size) {
        super(x, y, size);
        try {
            bufferedImage = ImageIO.read(new File("meteor.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void move() throws OutOfScreenException {
        y++;
        if(y >= WindowSize.HEIGHT.getSize()) throw new OutOfScreenException();
    }

    @Override
    public void paint(Graphics g) {
        //g.setColor(new Color(120, 60, 0));
        //g.fillOval(x, y, 30, 30);
        g.drawImage(bufferedImage, x, y, size, size * 2, null);
    }

    @Override
    public boolean overlaps(Bullet bullet) {
        boolean isLeftOverlapping = bullet.x >= x && bullet.x <= x + size;
        boolean isTopOverlapping = bullet.y >= y && bullet.y + bullet.size <= y + size;
        boolean isRightOverlapping = bullet.x + bullet.size >= x && bullet.x + bullet.size <= x + size;
        return (isLeftOverlapping || isRightOverlapping) && isTopOverlapping;
    }
}
