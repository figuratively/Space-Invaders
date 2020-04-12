import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GamePane extends JPanel implements MouseMotionListener {
    private final Spaceship spaceship = new Spaceship(80);
    private List<Asteroid> asteroids = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private final int DEFAULT_ASTEROID_GENERATION_DELAY = 1000,
            DEFAULT_ASTEROID_MOVE_DELAY = 8,
            DEFAULT_BULLET_GENERATION_DELAY = 1000,
            DEFAULT_BULLET_MOVE_DELAY = 1;
    private Timer asteroidGenerationTimer,
            asteroidMoveTimer,
            bulletMoveTimer,
            shootingTimer,
            elapsedTimer;
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

    GamePane() {
        addMouseMotionListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        initTimers();
        initBestTime();

        // @TODO use class loader and add Maven
        // InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("roboto-bold.ttf")
        // Font font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(48f)
        initFont();
        initSound();
        playThemeSound();
    }

    @Override
    public void paint(Graphics g) {
        paintGameScreen(g);
        paintTime(g);

        if(spaceship.getIsDestroyed())
            paintGameOverScreen(g);

        if(!"".equals(errorMessage))
            paintErrorMessage(g);
    }

    private void generateAsteroids() {
        asteroids.add(new Asteroid((int) (Math.random() * (WindowSize.WIDTH.getSize() - 60)), -100, 60));
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
                    saveTime(getReadableTime(yourTime), "best_time");
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
            System.out.println(errorMessage);
        }
    }

    private void paintGameOverScreen(Graphics g) {
        g.setColor(new Color(50, 50 ,50, 146));
        g.fillRect(0, 0, WindowSize.WIDTH.getSize(), WindowSize.HEIGHT.getSize());
        g.setColor(Color.WHITE);
        g.setFont(titleFont);
        g.drawString("GAME OVER", WindowSize.WIDTH.getSize() / 2 - 130, WindowSize.HEIGHT.getSize() / 2);
    }


    private void paintErrorMessage(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(errorFont);
        g.drawString(errorMessage, 40, WindowSize.HEIGHT.getSize() - 50);
    }

    private void paintTime(Graphics g) {
        g.setFont(yourTimeFont);
        g.setColor(yourTime > bestTime ? Color.RED : Color.WHITE);
        g.drawString("TIME: " + getReadableTime(yourTime), 785, 40);
        g.setColor(new Color(238, 189, 76));
        g.setFont(bestTimeFont);
        g.drawString("BEST: " + getReadableTime(Math.max(bestTime, yourTime)), 785, 60);
    }

    private void paintGameScreen(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WindowSize.WIDTH.getSize(), WindowSize.HEIGHT.getSize());
        spaceship.paint(g);
        asteroids.forEach(asteroid -> asteroid.paint(g));
        bullets.forEach(bullet -> bullet.paint(g));
    }

    private void initBestTime() {
        try (Scanner bestTimeReader = new Scanner(new File("best_time"))) {
            String rawTime = bestTimeReader.nextLine();
            bestTime = getMillisTime(rawTime);
        } catch (IOException ioException) {
            System.out.println("Not found best_time file.");
            createBestTimeFile();
        } catch (InvalidDateFormatException invalidDateFormatException) {
            System.out.println("Couldn't parse the date from best_time file.");
            createBestTimeFile();
        }
    }

    private void createBestTimeFile() {
        try (PrintWriter pr = new PrintWriter("best_time")) {
            pr.write("00:00");
            System.out.println("Created best_time file.");
        } catch (IOException ioCreationException) {
            System.out.println("Couldn't create best_time file.");
        }
    }

    private void initFont() {
        Font basicFont = new Font("Verdana", Font.PLAIN, 0);
        try {
            /*
             * "VCR OSD Mono"
             * by mrmanet
             * https://www.1001freefonts.com/vcr-osd-mono.font
             */
            basicFont = Font.createFont(Font.TRUETYPE_FONT, new File("VCR_OSD_MONO.ttf"));
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

    private String getReadableTime(long timeMillis) {
        long timeSeconds = timeMillis / 1000;
        long minutes = timeSeconds / 60;
        long seconds = timeSeconds % 60;
        return (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    private long getYourTime() {
        return System.currentTimeMillis() - gameStartMillis;
    }

    private long getMillisTime(String readableTime) throws InvalidDateFormatException {
        Pattern timePattern = Pattern.compile("[0-5][0-9]:[0-5][0-9]");
        Matcher timeMatcher = timePattern.matcher(readableTime);
        if(timeMatcher.matches()) {
            String matchedReadableTime = timeMatcher.group();
            try {
                String[] splitReadableTime = matchedReadableTime.split(":");
                int minutes = Integer.parseInt(splitReadableTime[0]);
                int seconds = Integer.parseInt(splitReadableTime[1]);
                return (minutes * 60 + seconds) * 1000;
            } catch (Exception e) {
                throw new InvalidDateFormatException();
            }
        } else {
            throw new InvalidDateFormatException();
        }
    }

    private int countShootingDelay() {
        int seconds = (int) getYourTime() / 1000;
        System.out.println("shooting delay: " + (seconds >= 40 ? (seconds >= 60 ? (seconds >= 100 ? 400 : 550) : 700): DEFAULT_BULLET_GENERATION_DELAY));
        return (seconds >= 40 ? (seconds >= 60 ? (seconds >= 100 ? 400 : 550) : 700): DEFAULT_BULLET_GENERATION_DELAY);
    }

    private int countMeteorGenerationDelay() {
        int seconds = (int) getYourTime() / 1000;
        System.out.println("meteor delay: " + Math.max(DEFAULT_ASTEROID_GENERATION_DELAY - seconds * 10, 10));
        return Math.max(DEFAULT_ASTEROID_GENERATION_DELAY - seconds * 10, 50);
    }

    private void initSound() {
        try{
            /*
             * title: "Space Theme"
             * author: Ronald Kah https://ronaldkah.de/
             * license: CC BY 3.0 https://creativecommons.org/licenses/by/3.0/
             * link: https://soundcloud.com/ronaldkah/space-theme
             */
            themeSound = new AudioAdapter("theme.wav");

            /*
             * title: "Depth Charge"
             * author: Mike Koenig
             * license: Personal Use Only
             * link: http://soundbible.com/1472-Depth-Charge.html
             */
            crashSound[0] = new AudioAdapter("charge.wav");

            /*
             * title: "Depth Charge Short"
             * author: Mike Koenig
             * license: Personal Use Only
             * link: http://soundbible.com/1469-Depth-Charge-Short.html
             */
            crashSound[1] = new AudioAdapter("charge_short.wav");

            /*
             * title: "Depth Charge Shorter"
             * author: Mike Koenig
             * license: Personal Use Only
             * link: http://soundbible.com/1468-Depth-Charge-Shorter.html
             */
            crashSound[2] = new AudioAdapter("charge_shorter.wav");

            /*
             * title: "Grenade"
             * author: Mike Koenig
             * license: CC BY 3.0 https://creativecommons.org/licenses/by/3.0/
             * link: http://soundbible.com/1151-Grenade.html
             */
            crashSound[3] = new AudioAdapter("grenade.wav");

            /*
             * title: "Big Bomb"
             * author: Sandyrb
             * license: Sampling Plus 1.0 https://creativecommons.org/licenses/sampling+/1.0/
             * link: http://soundbible.com/1461-Big-Bomb.html
             */
            crashSound[4] = new AudioAdapter("big_bomb.wav");

            /*
             * title: "Blast"
             * author: Mike Koenig
             * license: CC BY 3.0 https://creativecommons.org/licenses/by/3.0/
             * link: http://soundbible.com/538-Blast.html
             */
            crashSound[5] = new AudioAdapter("blast.wav");

            /*
             * title: "Explosion"
             * author: unknown
             * license: Personal Use Only
             * link: http://soundbible.com/483-Explosion.html
             */
            spaceshipCrash = new AudioAdapter("spaceship_explosion.wav");
        } catch (AudioAdapterException audioAdapterException) {
            errorMessage = "Couldn't load sound effects.";
            System.out.println(errorMessage);
        }
    }

    private void playMeteorCrashSound() {
        Random randomSound = new Random();
        try {
            crashSound[randomSound.nextInt(6)].play(0);
        } catch (AudioAdapterException audioAdapterException) {
            System.out.println("Couldn't play sound effect.");
        }
    }

    private void playThemeSound() {
        try {
            themeSound.play(Clip.LOOP_CONTINUOUSLY);
        } catch (AudioAdapterException audioAdapterException) {
            System.out.println("Couldn't play theme music.");
        }
    }

    private void playSpaceshipCrash() {
        try {
            spaceshipCrash.play(0);
        } catch (AudioAdapterException audioAdapterException) {
            System.out.println("Couldn't play sound effect.");
        }
    }
}