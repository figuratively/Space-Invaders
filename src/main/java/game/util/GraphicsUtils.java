package game.util;

import game.ui.WindowSize;

import java.awt.*;

public class GraphicsUtils {
    public static void paintGameOverScreen(Graphics g, Font font) {
        g.setColor(new Color(50, 50 ,50, 146));
        g.fillRect(0, 0, WindowSize.WIDTH.getSize(), WindowSize.HEIGHT.getSize());
        g.setColor(Color.WHITE);
        g.setFont(font);
        g.drawString("GAME OVER", WindowSize.WIDTH.getSize() / 2 - 130, WindowSize.HEIGHT.getSize() / 2);
    }

    public static void paintErrorMessage(Graphics g, Font font, String message) {
        g.setColor(Color.RED);
        g.setFont(font);
        g.drawString(message, 40, WindowSize.HEIGHT.getSize() - 50);
    }

    public static void paintTime(Graphics g, Font yourTimeFont, Font bestTimeFont, long yourTime, long bestTime) {
        g.setFont(yourTimeFont);
        g.setColor(yourTime > bestTime ? Color.RED : Color.WHITE);
        g.drawString("TIME: " + TimeUtils.getReadableTime(yourTime), 785, 40);
        g.setColor(new Color(238, 189, 76));
        g.setFont(bestTimeFont);
        g.drawString("BEST: " + TimeUtils.getReadableTime(Math.max(bestTime, yourTime)), 785, 60);
    }
}
