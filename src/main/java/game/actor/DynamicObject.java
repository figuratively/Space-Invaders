package game.actor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class DynamicObject {
    protected int x;
    protected int y;
    protected int size;
    protected BufferedImage bufferedDefaultImage;
    protected BufferedImage bufferedDestroyedImage;

    DynamicObject(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    DynamicObject(int x, int y, int size, String defaultImagePath, String destroyedImagePath) throws IOException {
        this(x, y, size);
        initImages(defaultImagePath, destroyedImagePath);
    }

    public abstract void paint(Graphics g);

    public abstract void move(Integer... coordinates) throws OutOfScreenException;

    public boolean overlaps(DynamicObject object) {
        boolean left = object.x >= x && object.x <= x + size;
        boolean right = object.x + object.size >= x && object.x + object.size <= x + size;
        boolean top = object.y >= y && object.y <= y + size;
        boolean down = object.y + object.size >= y && object.y + object.size <= y + size;

        return (top && (right || left)) || (down && (left || right));
    }

    private void initImages(String defaultImagePath, String destroyedImagePath) throws IOException {
        InputStream defaultImageStream = ClassLoader
                .getSystemClassLoader()
                .getResourceAsStream(defaultImagePath);
        InputStream destroyedImageStream = ClassLoader
                .getSystemClassLoader()
                .getResourceAsStream(destroyedImagePath);
        if(defaultImageStream != null && destroyedImageStream != null) {
            bufferedDefaultImage = ImageIO.read(new BufferedInputStream(defaultImageStream));
            bufferedDestroyedImage = ImageIO.read(new BufferedInputStream(destroyedImageStream));
        } else
            throw new IOException();
    }
}
