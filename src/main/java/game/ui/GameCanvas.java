package game.ui;

import game.actor.*;
import game.audio.AudioAdapter;
import game.util.GraphicsUtils;
import game.util.InvalidDateFormatException;
import game.util.TimeUtils;

import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


public class GameCanvas extends JPanel implements MouseMotionListener {
    private Spaceship spaceship;
    private List<Asteroid> asteroids = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private List<Star> stars = new ArrayList<>();
    private final int DEFAULT_ASTEROID_GENERATION_DELAY = 1000,
            DEFAULT_ASTEROID_MOVE_DELAY = 8,
            DEFAULT_BULLET_GENERATION_DELAY = 1000,
            DEFAULT_BULLET_MOVE_DELAY = 1,
            DEFAULT_STAR_MOVE = 50;
    private Timer asteroidGenerationTimer,
            asteroidMoveTimer,
            bulletMoveTimer,
            shootingTimer,
            elapsedTimer,
            starMoveTimer;
    private Font yourTimeFont,
            bestTimeFont,
            titleFont,
            errorFont;
    private String errorMessage = "";
    private final long gameStartMillis = System.currentTimeMillis();
    private long bestTime = 0, yourTime = 0;
    private final AudioAdapter[] crashSound = new AudioAdapter[6];
    private AudioAdapter themeSound;
    private AudioAdapter spaceshipCrash;

    GameCanvas() {
        addMouseMotionListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        initTimers();
        initBestTime();
        initFont();
        initSound();
        initStars();
        initSpaceship();
        playThemeSound();
    }

    @Override
    public void paint(Graphics g) {
        paintGameScreen(g);
        GraphicsUtils.paintTime(g, yourTimeFont, bestTimeFont, yourTime, bestTime);

        if(spaceship.getIsDestroyed())
            GraphicsUtils.paintGameOverScreen(g, titleFont);

        if(!"".equals(errorMessage))
            GraphicsUtils.paintErrorMessage(g, errorFont, errorMessage);
    }

    private void generateAsteroids() {
        try {
            asteroids.add(
                    new Asteroid((int) (Math.random() * (WindowSize.WIDTH.getSize() - 60)),
                            -120,
                            60,
                            "pictures/meteor.png",
                            "pictures/meteor_destroyed.png"));
        } catch (IOException e) {
            System.err.println("Couldn't load images.");
            System.exit(1);
        }
    }

    private void moveAsteroids() {
        asteroids = asteroids
                .stream()
                .filter(asteroid -> !asteroid.canDisappear())
                .filter(asteroid -> {
            if(!spaceship.getIsDestroyed() && spaceship.overlaps(asteroid)) {
                spaceship.destroy();
                playSpaceshipCrash();
                if(yourTime > bestTime)
                    saveTime(TimeUtils.getReadableTime(yourTime), "best_time");
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
                AtomicBoolean anyAsteroidDestroyed = new AtomicBoolean(false);
                asteroids = asteroids
                        .stream()
                        .filter(asteroid -> !asteroid.canDisappear())
                        .peek(asteroid -> {
                            if(asteroid.overlaps(bullet)) {
                                anyAsteroidDestroyed.set(true);
                                asteroid.destroy();
                                playMeteorCrashSound();
                            }
                        })
                        .collect(Collectors.toList());
                return !anyAsteroidDestroyed.get();
            } catch (OutOfScreenException e) {
                return false;
            }
        }).collect(Collectors.toList());
    }

    private void initTimers() {
        asteroidGenerationTimer = new Timer(countMeteorGenerationDelay(), event -> {
            asteroidGenerationTimer.setDelay(countMeteorGenerationDelay());
            asteroidGenerationTimer.start();
            generateAsteroids();
            repaint();
        });
        asteroidGenerationTimer.start();

        asteroidMoveTimer = new Timer(DEFAULT_ASTEROID_MOVE_DELAY, event -> {
            asteroidMoveTimer.start();
            moveAsteroids();
            repaint();
        });
        asteroidMoveTimer.start();

        bulletMoveTimer = new Timer(DEFAULT_BULLET_MOVE_DELAY, event -> {
            bulletMoveTimer.start();
            moveBullets();
            repaint();
        });
        bulletMoveTimer.start();

        shootingTimer = new Timer(countShootingDelay(), event -> {
            shootingTimer.setDelay(countShootingDelay());
            shootingTimer.start();
            if(!spaceship.getIsDestroyed())
                bullets.add(spaceship.getNewBullet());
        });
        shootingTimer.start();

        elapsedTimer = new Timer(1000, event -> {
            elapsedTimer.start();
            if(!spaceship.getIsDestroyed())
               yourTime =  getYourTime();
        });
        elapsedTimer.start();

        starMoveTimer = new Timer(DEFAULT_STAR_MOVE, event -> {
           starMoveTimer.start();

           int starsSize = stars.size();
           stars = stars.stream().filter(star -> {
               try {
                   star.move();
                   return true;
               } catch (OutOfScreenException e) {
                   return false;
               }
           }).collect(Collectors.toList());

           if(stars.size() < starsSize) {
               Random randomPosition = new Random();
               stars.add(new Star(
                       randomPosition.nextInt(WindowSize.WIDTH.getSize() - 10),
                       -10,
                       10)
               );
           }
        });
        starMoveTimer.start();
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

    private void saveTime(String readableTime, String fileName) {
        try (PrintWriter pr = new PrintWriter(fileName)) {
            pr.write("" + readableTime);
        } catch (IOException ioException) {
            errorMessage = "Couldn't save the time in the best_time file.";
            System.err.println(errorMessage);
        }
    }

    private void paintGameScreen(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WindowSize.WIDTH.getSize(), WindowSize.HEIGHT.getSize());
        stars.forEach(star -> star.paint(g));
        asteroids.forEach(asteroid -> asteroid.paint(g));
        bullets.forEach(bullet -> bullet.paint(g));
        spaceship.paint(g);
    }

    private void initBestTime() {
        try (Scanner bestTimeReader = new Scanner(new File("best_time"))) {
            String rawTime = bestTimeReader.nextLine();
            bestTime = TimeUtils.getMillisTime(rawTime);
        } catch (IOException ioException) {
            System.out.println("Not found best_time file.");
            createBestTimeFile();
        } catch (InvalidDateFormatException invalidDateFormatException) {
            System.err.println("Couldn't parse the date from best_time file.");
            createBestTimeFile();
        }
    }

    private void createBestTimeFile() {
        try (PrintWriter pr = new PrintWriter("best_time")) {
            pr.write("00:00");
            System.out.println("Created best_time file.");
        } catch (IOException ioCreationException) {
            System.err.println("Couldn't create best_time file.");
        }
    }

    private void initFont() {
        Font basicFont = new Font("Verdana", Font.PLAIN, 0);
        try {
            InputStream stream = ClassLoader
                    .getSystemClassLoader()
                    .getResourceAsStream("fonts/VCR_OSD_MONO.ttf");
            if(stream != null)
                basicFont = Font.createFont(Font.TRUETYPE_FONT, stream);
            else
                throw new IOException();

        } catch (IOException | FontFormatException ignored) {
            errorMessage = "Couldn't load custom font.";
            System.out.println(errorMessage + " Using Verdana instead.");
        } finally {
            yourTimeFont = basicFont.deriveFont(30f);
            bestTimeFont = basicFont.deriveFont(20f);
            titleFont = basicFont.deriveFont(50f);
            errorFont = basicFont.deriveFont(20f);
        }
    }

    private long getYourTime() {
        return System.currentTimeMillis() - gameStartMillis;
    }

    private int countShootingDelay() {
        int seconds = (int) getYourTime() / 1000;
        return (seconds >= 40 ? (seconds >= 60 ? (seconds >= 100 ? 400 : 550) : 700): DEFAULT_BULLET_GENERATION_DELAY);
    }

    private int countMeteorGenerationDelay() {
        int seconds = (int) getYourTime() / 1000;
        return Math.max(DEFAULT_ASTEROID_GENERATION_DELAY - seconds * 10, 50);
    }

    private void initSound() {
        themeSound = new AudioAdapter("music/theme.wav");
        crashSound[0] = new AudioAdapter("sfx/charge.wav");
        crashSound[1] = new AudioAdapter("sfx/charge_short.wav");
        crashSound[2] = new AudioAdapter("sfx/charge_shorter.wav");
        crashSound[3] = new AudioAdapter("sfx/grenade.wav");
        crashSound[4] = new AudioAdapter("sfx/big_bomb.wav");
        crashSound[5] = new AudioAdapter("sfx/blast.wav");
        spaceshipCrash = new AudioAdapter("sfx/spaceship_explosion.wav");
    }

    private void playMeteorCrashSound() {
        Random randomSound = new Random();
        try {
            crashSound[randomSound.nextInt(6)].play(0);
        } catch (Exception e) {
            System.err.println("Couldn't play sound effect.");
        }
    }

    private void playThemeSound() {
        try {
            themeSound.play(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.err.println("Couldn't play theme music.");
        }
    }

    private void playSpaceshipCrash() {
        try {
            spaceshipCrash.play(0);
        } catch (Exception e) {
            System.err.println("Couldn't play sound effect.");
        }
    }

    private void initSpaceship() {
        try {
            spaceship = new Spaceship(80, "pictures/spaceship.png", "pictures/spaceship_destroyed.png");
        } catch (IOException e) {
            System.err.println("Couldn't load images.");
            System.exit(1);
        }
    }

    private void initStars() {
        Random randomPosition = new Random();
        for(int i = 0; i < 100; i++)
            stars.add(new Star(
                    randomPosition.nextInt(WindowSize.WIDTH.getSize() - 10),
                    randomPosition.nextInt(WindowSize.HEIGHT.getSize() - 10),
                    10)
            );
    }
}