package game.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Frame extends JFrame {
    Frame() {
        setBounds(10, 10, WindowSize.WIDTH.getSize(), WindowSize.HEIGHT.getSize());
        setTitle("Game");
        setResizable(false);
        GameCanvas pane = new GameCanvas();
        add(pane);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // center window
        setLocationRelativeTo(null);

        // hide cursor
        setCursor(this.getToolkit().createCustomCursor(
                new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB ),
                new Point(),
                null));
    }

    public static void main(String... args) {
        SwingUtilities.invokeLater(Frame::new);
    }
}
