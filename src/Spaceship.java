import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Spaceship {
    private BufferedImage bufferedImage;
    private Polygon polygon;
    private Polygon lastRenderedPolygon;
    private Color color;
    private int scale;
    private int xMovement = 0;
    private int yMovement = 0;
    private int velocity = 1;
    private float deltaTime;

    Spaceship() {
        this.scale = 1;
        initPolygon(scale);
        color = Color.BLACK;
        try {
            bufferedImage = ImageIO.read(new File("rocket.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Spaceship(int scale, Color color) {
        initPolygon(scale);
        this.scale = scale;
        this.color = color;
    }

    public void paint(Graphics g) {
        g.setColor(color);
        g.fillPolygon(polygon);
        lastRenderedPolygon = polygon;

        int x = lastRenderedPolygon.xpoints[1];
        int y = lastRenderedPolygon.ypoints[1];
        g.drawImage(bufferedImage, x - (int) (80 * 0.9), y, (int) (80 * 0.9), 80, null);
    }

    public Bullet getNewBullet() {
        int x = lastRenderedPolygon.xpoints[1];
        int y = lastRenderedPolygon.ypoints[1];
        return new Bullet(x, y, 10);
    }

    public void move(SpaceshipMove move) throws OutOfScreenException {
        int xMove = 0;
        int yMove = 0;
        switch (move) {
            case LEFT:
                xMove = -10;
                break;
            case UP:
                yMove = -10;
                break;
            case RIGHT:
                xMove = 10;
                break;
            case DOWN:
                yMove = 10;
                break;
        }

        Polygon wannaBePolygon = generatePolygon(scale, xMove, yMove);

        int leftSide = wannaBePolygon.xpoints[0];
        int rightSide = wannaBePolygon.xpoints[2];
        int topSide = wannaBePolygon.ypoints[1];
        int downSide = wannaBePolygon.ypoints[0];
        boolean isXOnTheScreen = leftSide >= 0 && rightSide <= WindowSize.WIDTH.getSize();
        boolean isYOnTheScreen = topSide >= 0 && downSide <= WindowSize.HEIGHT.getSize();

        if (isXOnTheScreen && isYOnTheScreen) {
            polygon = wannaBePolygon;
        } else {
            throw new OutOfScreenException();
        }
    }

    public void moveLeft() {
        polygon = generatePolygon(scale, -10, 0);
    }

    public void moveRight() {
        polygon = generatePolygon(scale, 10, 0);
    }

    public void moveUp() {
        polygon = generatePolygon(scale, 0, -10);
    }

    public void moveDown() {
        polygon = generatePolygon(scale, 0, 10);
    }

    public void move(int x, int y) {
        polygon = generatePolygon(scale, x, 600);
    }

    private void initPolygon(int scale) {
        polygon = new Polygon(
                new int[]{482, 482 + 30 * scale, 482 + 60 * scale},
                new int[]{622 + 60 * scale, 622, 622 + 60 * scale},
                3
        );
    }

    private Polygon generatePolygon(int scale, int x, int y) {
        return new Polygon(
                new int[]{x, x + 30 * scale, x + 60 * scale},
                new int[]{y + 60 * scale,y, y + 60 * scale},
                3
        );
    }
}
