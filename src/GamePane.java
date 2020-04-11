import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

public class GamePane extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
    private Spaceship spaceship = new Spaceship();
    private Timer timer;
    private int delay = 8;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private List<Asteroid> asteroids = new ArrayList<>();
    private Timer asteroidGenerationTimer;
    private int asteroidGenerationDelay = 300;
    private Timer asteroidMoveTimer;
    private int asteroidMoveDelay = 8;
    private List<Bullet> bullets = new ArrayList<>();
    private Timer bulletMoveTimer;
    private int bulletMoveDelay = 1;
    private int shootingDelay = 1000;
    private Timer shootingTimer;
    private boolean canShoot = true;

    GamePane() {
        addKeyListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
//        timer = new Timer(delay, (event) -> {
//            timer.start();
//            repaint();
//        });
//        timer.start();
        initTimers();

        /*
         * "Space Theme"
         * by Ronald Kah https://ronaldkah.de/
         * https://soundcloud.com/ronaldkah/space-theme
         */
        new Thread(() -> {
            Clip clip = null;
            AudioInputStream audioInputStream;

            while(clip == null) {
                try {
                    audioInputStream = AudioSystem.getAudioInputStream(new File("theme.wav").getAbsoluteFile());
                    clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
                    System.err.println("Couldn't play music: " + e.getMessage());
                }
            }

            while (true) {
                clip.start();
                try {
                    System.out.println(clip.getMicrosecondLength());
                    Thread.sleep(clip.getMicrosecondLength() / 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                clip.setMicrosecondPosition(0);
            }
        }).start();

    }

    @Override
    public void paint(Graphics g) {
        g.setColor(new Color(50, 50, 100));
        g.fillRect(0, 0, WindowSize.WIDTH.getSize(), WindowSize.HEIGHT.getSize());
        spaceship.paint(g);
        asteroids.forEach(asteroid -> asteroid.paint(g));
        bullets.forEach(bullet -> bullet.paint(g));
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) { }

    @Override
    public synchronized void keyPressed(KeyEvent keyEvent) {
        pressedKeys.add(keyEvent.getKeyCode());
        if(!pressedKeys.isEmpty()) {
            for (Integer pressedKey : pressedKeys) {
                try {
                    switch (pressedKey) {
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_A:
                            spaceship.move(SpaceshipMove.LEFT);
                            break;
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_W:
                            spaceship.move(SpaceshipMove.UP);
                            break;
                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_D:
                            spaceship.move(SpaceshipMove.RIGHT);
                            break;
                        case KeyEvent.VK_DOWN:
                        case KeyEvent.VK_S:
                            spaceship.move(SpaceshipMove.DOWN);
                            break;
                        case KeyEvent.VK_SPACE:
                            if(canShoot) {
                                bullets.add(spaceship.getNewBullet());
                                canShoot = false;
                            }
                            break;
                    }
                } catch (OutOfScreenException e) {
                    e.printStackTrace();
                }
            }
        }
        repaint();
    }

    @Override
    public synchronized void keyReleased(KeyEvent keyEvent) {
        pressedKeys.remove(keyEvent.getKeyCode());
    }

    private void generateAsteroids() {
        asteroids.add(new Asteroid((int) (Math.random() * WindowSize.WIDTH.getSize()), -100, 50));
    }

    private void moveAsteroids() {
        asteroids = asteroids.stream().filter(asteroid -> {
            try {
                asteroid.move();
                return true;
            } catch (OutOfScreenException e) {
                return false;
            }
        }).collect(Collectors.toList());
    }

    private void moveBullets() {
        bullets = bullets.stream().filter(bullet -> {
            try {
                bullet.move();
                int asteroidsSize = asteroids.size();
                asteroids = asteroids
                        .stream()
                        .filter(asteroid -> !asteroid.overlaps(bullet))
                        .collect(Collectors.toList());
                return asteroidsSize == asteroids.size();
            } catch (OutOfScreenException e) {
                return false;
            }
        }).collect(Collectors.toList());
    }

    private void initTimers() {
        asteroidGenerationTimer = new Timer(asteroidGenerationDelay, event -> {
            asteroidGenerationTimer.start();
            generateAsteroids();
            repaint();
        });
        asteroidGenerationTimer.start();

        asteroidMoveTimer = new Timer(asteroidMoveDelay, event -> {
            asteroidMoveTimer.start();
            moveAsteroids();
            repaint();
        });
        asteroidMoveTimer.start();

        bulletMoveTimer = new Timer(bulletMoveDelay, event -> {
            bulletMoveTimer.start();
            moveBullets();
            repaint();
        });
        bulletMoveTimer.start();

        shootingTimer = new Timer(shootingDelay, event -> {
            shootingTimer.start();
            bullets.add(spaceship.getNewBullet());
        });
        shootingTimer.start();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        spaceship.move(mouseEvent.getX(), mouseEvent.getY());
        //System.out.println(mouseEvent.getX() + " " + mouseEvent.getY());
    }
}
