import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ControlListener implements KeyListener {
    private Spaceship spaceship;
    private JFrame frame;

    ControlListener(Spaceship spaceship, JFrame frame) {
        this.spaceship = spaceship;
        this.frame = frame;
    }

    @Override
    public void keyTyped(KeyEvent e) {

        System.out.println("a:" + e.getKeyCode());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case 37:
                //spaceship.moveLeft();
                //frame.repaint();
                break;
            case 38:
                //spaceship.moveUp();
                //frame.repaint();
                break;
            case 39:
                //spaceship.moveRight();
                //frame.repaint();
                break;
            case 40:
                //spaceship.moveDown();
                //frame.repaint();
        }
        System.out.println("b:" + e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        System.out.println("c:" + e.getKeyCode());
    }
}
