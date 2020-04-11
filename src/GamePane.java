import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GamePane extends JPanel implements MouseMotionListener {
    private Spaceship spaceship = new Spaceship(80);
    private List<Asteroid> asteroids = new ArrayList<>();
    private Timer asteroidGenerationTimer;
    private int asteroidGenerationDelay = 1000;
    private Timer asteroidMoveTimer;
    private int asteroidMoveDelay = 8;
    private List<Bullet> bullets = new ArrayList<>();
    private Timer bulletMoveTimer;
    private int bulletMoveDelay = 1;
    private int shootingDelay = 1000;
    private Timer shootingTimer;
    private int score = 0, bestScore = 0;
    private Font yourScoreFont, bestScoreFont, titleFont, errorFont;
    private String errorMessage = "";

    GamePane() {
        addMouseMotionListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        initTimers();

        try (Scanner scoreReader = new Scanner(new File("best_score"))) {
            bestScore = scoreReader.nextInt();
        } catch (IOException ioReadingException) {
            System.out.println("Not found best_score file.");
            try (PrintWriter pr = new PrintWriter("best_score")) {
                pr.write(0);
                System.out.println("Created best_score file.");
            } catch (IOException ioCreationException) {
                System.out.println("Couldn't create best_score file.");
            }
        } catch (NoSuchElementException | NumberFormatException numberFormatException) {
            System.out.println("Number in best_score file is incorrect. Using 0 instead.");
        }

        // @TODO use class loader and add Maven
        // InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("roboto-bold.ttf")
        // Font font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(48f)

        /*
         * "VCR OSD Mono"
         * by mrmanet
         * https://www.1001freefonts.com/vcr-osd-mono.font
         */
        try {
            Font basicFont = Font.createFont(Font.TRUETYPE_FONT, new File("VCR_OSD_MONO.ttf"));
            yourScoreFont = basicFont.deriveFont(30f);
            bestScoreFont = basicFont.deriveFont(20f);
            titleFont = basicFont.deriveFont(50f);
            errorFont = basicFont.deriveFont(20f);
        } catch (IOException | FontFormatException ignored) { }

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
                    errorMessage = "Can't play music.";
                    System.err.println(errorMessage);
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
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WindowSize.WIDTH.getSize(), WindowSize.HEIGHT.getSize());
        spaceship.paint(g);
        asteroids.forEach(asteroid -> asteroid.paint(g));
        bullets.forEach(bullet -> bullet.paint(g));

        g.setFont(yourScoreFont);
        g.setColor(score > bestScore ? Color.RED : Color.WHITE);
        g.drawString("SCORE: " + score, 800, 40);
        g.setColor(new Color(238, 189, 76));
        g.setFont(bestScoreFont);
        g.drawString("BEST: " + Math.max(bestScore, score), 800, 60);

        if(spaceship.getIsDestroyed()) {
            g.setColor(new Color(50, 50 ,50, 146));
            g.fillRect(0, 0, WindowSize.WIDTH.getSize(), WindowSize.HEIGHT.getSize());
            g.setColor(Color.WHITE);
            g.setFont(titleFont);
            g.drawString("GAME OVER", WindowSize.WIDTH.getSize() / 2 - 150, WindowSize.HEIGHT.getSize() / 2);
        }

        if(!"".equals(errorMessage)) {
            g.setColor(Color.RED);
            g.setFont(errorFont);
            g.drawString(errorMessage, 40, WindowSize.HEIGHT.getSize() - 50);
        }
    }

    private void generateAsteroids() {
        asteroids.add(new Asteroid((int) (Math.random() * (WindowSize.WIDTH.getSize() - 60)), -100, 60));
    }

    private void moveAsteroids() {
        asteroids = asteroids.stream().filter(asteroid -> {
            if(!spaceship.getIsDestroyed() && spaceship.overlaps(asteroid)) {
                spaceship.destroy();
                if(score > bestScore)
                    saveScore(score, "best_score");
                return false;
            }

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
                if(asteroidsSize == asteroids.size()) {
                    return true;
                } else {
                    score++;
                    return false;
                }
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
            if(!spaceship.getIsDestroyed())
                bullets.add(spaceship.getNewBullet());
        });
        shootingTimer.start();
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) { }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        if(!spaceship.getIsDestroyed()) {
            try {
                spaceship.move(mouseEvent.getX());
            } catch (OutOfScreenException ignored) {
            }
        }
    }

    private void saveScore(int score, String fileName) {
        try (PrintWriter pr = new PrintWriter(fileName)) {
            pr.write("" + score);
        } catch (IOException ioException) {
            errorMessage = "Couldn't save the score in the best_score file.";
            System.out.println(errorMessage);
        }
    }

}