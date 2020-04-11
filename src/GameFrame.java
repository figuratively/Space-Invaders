import javax.swing.*;

public class GameFrame extends JFrame {
    GameFrame() {
        setBounds(10, 10, WindowSize.WIDTH.getSize(), WindowSize.HEIGHT.getSize());
        setTitle("Game");
        setResizable(false);
        GamePane pane = new GamePane();
        add(pane);
        setVisible(true);
        // center window
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}
