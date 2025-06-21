package com.game.uaspbo_voidthreat_kapalangkasa.controller;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

public class GameController {

    private String playerName;
    private Font gameFont;

    public GameController(String playerName) {
        this.playerName = playerName;
    }

    private static final Random RAND = new Random();
    private static int WIDTH;
    private static int HEIGHT;
    private static final int PLAYER_SIZE = 60;
    private static final int MEGADIHROCKET_SIZE = 120;

    static final Image PLAYER_IMG = new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/Dihrocket.png").toExternalForm());
    static final Image EXPLOSION_IMG = new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/Dihplosion.png").toExternalForm());

    static final int EXPLOSION_W = 32;
    static final int EXPLOSION_ROWS = 7;
    static final int EXPLOSION_COL = 7;
    static final int EXPLOSION_H = 32;
    static final int EXPLOSION_STEPS = 8;

    static final Image BOMBS_IMG[] = {
            new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/Dihroids.png").toExternalForm()),
            new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/dihazard1.png").toExternalForm()),
            new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/dihazard2.png").toExternalForm()),
            new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/dihroids2.png").toExternalForm()),
            new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/megadihrocket.png").toExternalForm()),
    };


    final int MAX_BOMBS = 10, MAX_SHOTS = MAX_BOMBS * 2;
    boolean gameOver = false;
    private GraphicsContext gc;

    Rocket player;
    List<Shot> shots;
    List<Universe> univ;
    List<Bomb> Bombs;

    private double mouseX;
    private int score;
    private long lastFrameTime;

    private boolean wasInSpecialShotMode = false;
    private boolean goLeft, goRight, shootPressed;
    private final double PLAYER_SPEED = 200;
    private boolean paused = false;
    private int lives;
    private boolean invulnerable;
    private double invulnerableTimer;
    private static final double INVULNERABILITY_DURATION = 2.0;
    private static final double RESPAWN_FLASH_INTERVAL = 0.1;

    // Permainan Musik
    private SoundManager soundManager;

    public void start(Stage stage) throws Exception {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        WIDTH = (int) primaryScreenBounds.getWidth();
        HEIGHT = (int) primaryScreenBounds.getHeight();

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        gc.setImageSmoothing(false);

        lastFrameTime = System.nanoTime();
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;
                run(gc, deltaTime);
            }
        };
        gameLoop.start();

        StackPane rootPane = new StackPane(canvas);
        Scene scene = new Scene(rootPane);

        canvas.setCursor(Cursor.MOVE);
        canvas.setOnMouseMoved(e -> mouseX = e.getX());

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) {
                goLeft = true;
            } else if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) {
                goRight = true;
            } else if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.SPACE) {
                if (!gameOver && !paused) {
                    shootPressed = true;
                    if (shots.size() < MAX_SHOTS) {
                        shots.add(player.shoot());
                        SoundManager.playSound("sfx1.wav");
                    }
                }
            } else if (e.getCode() == KeyCode.P) {
                if (!gameOver) {
                    paused = !paused;
                    if (paused){
                        soundManager.pauseMusic();
                    } else {
                        soundManager.resumeMusic();
                    }
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) {
                goLeft = false;
            } else if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) {
                goRight = false;
            } else if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.SPACE) {
                shootPressed = false;
            }
        });

        canvas.setOnMouseClicked(e -> {
            if (!gameOver && !player.exploding && !paused) {
                if (shots.size() < MAX_SHOTS)
                    shots.add(player.shoot());
                SoundManager.playSound("sfx1.wav");
            }

            if (gameOver) {
                gameLoop.stop();
                soundManager.stopMusic();
                saveScoreToDatabase();
                backToMainMenu(stage);
            }
        });

        setup();
        stage.setScene(scene);
        stage.setTitle("Void Threat");
        stage.setMaximized(true);
        stage.show();

        scene.getRoot().requestFocus();
    }

    private void setup() {

        try (InputStream is = getClass().getResourceAsStream("/com/game/uaspbo_voidthreat_kapalangkasa/assets/PressStart2P-Regular.ttf")) {
            if (is == null) {
                System.out.println("Font tidak ditemukan");
            } else {
                this.gameFont = Font.loadFont(is, 12);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        soundManager = new SoundManager();

        univ = new ArrayList<>();
        shots = new ArrayList<>();
        Bombs = new ArrayList<>();
        player = new Rocket(WIDTH / 2.0, HEIGHT - 85, PLAYER_SIZE, PLAYER_IMG);
        score = 0;
        lives = 3;
        invulnerable = false;
        invulnerableTimer = 0;

        IntStream.range(0, MAX_BOMBS).mapToObj(i -> this.newBomb()).forEach(Bombs::add);

        goLeft = false;
        goRight = false;
        shootPressed = false;
        paused = false;
        wasInSpecialShotMode = false;
        gameOver = false;

        soundManager.playIntroThenLoop("bgm2.wav", "bgm1.wav");
    }

    private void run(GraphicsContext gc, double deltaTime) {

        gc.setFill(Color.grayRgb(20));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);

        univ.forEach(u -> u.draw(deltaTime));
        for (int i = univ.size() - 1; i >= 0; i--) {
            if (univ.get(i).posY > HEIGHT)
                univ.remove(i);
        }
        if (!paused && RAND.nextInt(10) > 2) {
            univ.add(new Universe());
        }

        Bombs.forEach(bomb -> bomb.update(deltaTime));

        for (int i = Bombs.size() - 1; i >= 0; i--) {
            if (Bombs.get(i).destroyed) {
                Bombs.set(i, newBomb());
            }
        }

        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            if (shot.posY < 0 || shot.toRemove) {
                shots.remove(i);
                continue;
            }

            shot.update(deltaTime);
            shot.draw();

            for (Bomb bomb : Bombs) {
                if (shot.colide(bomb) && !bomb.exploding) {
                    score++;
                    SoundManager.playSound("sfx2.wav");
                    bomb.explode();
                    shot.toRemove = true;
                }
            }
        }

        if (!gameOver && !paused) {
            player.posX = (int) mouseX;

            player.update(deltaTime);

            if (invulnerable) {
                invulnerableTimer += deltaTime;
                if (invulnerableTimer >= INVULNERABILITY_DURATION) {
                    invulnerable = false;
                    invulnerableTimer = 0;
                }
                if ((int) (invulnerableTimer / RESPAWN_FLASH_INTERVAL) % 2 == 0) {
                    player.draw();
                }
            } else {
                player.draw();
            }

            if (goLeft) player.posX -= PLAYER_SPEED * deltaTime;
            if (goRight) player.posX += PLAYER_SPEED * deltaTime;

            if (player.posX < 0) player.posX = 0;
            if (player.posX > WIDTH - PLAYER_SIZE) player.posX = WIDTH - PLAYER_SIZE;

            Bombs.forEach(bomb -> {
                if (player.colide(bomb) && !player.exploding && !invulnerable) {
                    SoundManager.playSound("sfx3.wav");
                    player.explode();
                    lives--;
                    if (lives > 0) {
                        invulnerable = true;
                        invulnerableTimer = 0;
                        player.posX = WIDTH / 2.0;
                        player.posY = HEIGHT - 85;
                        player.exploding = false;
                        player.destroyed = false;
                    }
                }
            });

            if (player.destroyed) {
                gameOver = true;
                soundManager.stopMusic();
            }


            boolean isInSpecialShotMode = (score >= 50 && score <= 70 || score >= 120);
            if (isInSpecialShotMode && !wasInSpecialShotMode) {
                SoundManager.playSound("sfx4.wav");
            }
            this.wasInSpecialShotMode = isInSpecialShotMode;

        } else {
            if (!gameOver && (!invulnerable || (int) (invulnerableTimer / RESPAWN_FLASH_INTERVAL) % 2 == 0)) {
                player.draw();
            }
        }

        Bombs.forEach(Rocket::draw);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font(gameFont.getFamily(), 25));
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Score: " + score, 30, 60);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("Lives: " + lives, WIDTH - 30, 60);

        if (gameOver) {
            gc.setFont(Font.font(gameFont.getFamily(), 30));
            gc.setFill(Color.YELLOW);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Game Over \n Your Score is: " + score + " \n Click to back to main menu", WIDTH / 2, HEIGHT / 2.5);
        } else if (paused) {
            gc.setFont(Font.font(gameFont.getFamily(), 50));
            gc.setFill(Color.CYAN);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("PAUSED", WIDTH / 2, HEIGHT / 2);
        }
    }

    public class Rocket {
        double posX, posY;
        int size;
        boolean exploding, destroyed;
        Image img;

        private double explosionTimer = 0;
        private static final double EXPLOSION_DURATION = 0.7;

        public Rocket(double posX, double posY, int size, Image image) {
            this.posX = posX;
            this.posY = posY;
            this.size = size;
            img = image;
        }

        public Shot shoot() {
            return new Shot(posX + size / 2.0 - Shot.size / 2.0, posY - Shot.size);
        }

        public void update(double deltaTime) {
            if (exploding) {
                explosionTimer += deltaTime;
                if (explosionTimer >= EXPLOSION_DURATION) {
                    destroyed = true;
                }
            }
        }

        public void draw() {
            if (exploding) {
                int frame = (int) ((explosionTimer / EXPLOSION_DURATION) * EXPLOSION_STEPS);

                if (frame >= EXPLOSION_STEPS) {
                    frame = EXPLOSION_STEPS - 1;
                }

                gc.drawImage(EXPLOSION_IMG, frame % EXPLOSION_COL * EXPLOSION_W,
                        (frame / EXPLOSION_ROWS) * EXPLOSION_H + 1, EXPLOSION_W, EXPLOSION_H,
                        (int) posX, (int) posY, size, size);
            } else {
                gc.drawImage(img, (int) posX, (int) posY, size, size);
            }
        }

        public boolean colide(Rocket other) {
            double r = this.size / 2.0 + other.size / 2.0;
            return distanceSq(this.posX + size / 2.0, this.posY + size / 2.0,
                    other.posX + other.size / 2.0, other.posY + other.size / 2.0) < r * r;
        }

        public void explode() {
            exploding = true;
            explosionTimer = 0;
        }
    }

    public class Bomb extends Rocket {
        public double getSpeed() {
            return ((score / 5.0) + 2) * 20;
        }

        public Bomb(double posX, double posY, int size, Image image) {
            super(posX, posY, size, image);
        }

        public void update(double deltaTime) {
            super.update(deltaTime);
            if (!exploding && !destroyed && !paused) posY += getSpeed() * deltaTime;
            if (posY > HEIGHT) destroyed = true;
        }
    }

    public class Shot {
        public boolean toRemove;
        double posX, posY;
        double speed = 100;
        double specialSpeed = 500;
        static final int size = 6;

        public Shot(double posX, double posY) {
            this.posX = posX;
            this.posY = posY;
        }

        public void update(double deltaTime) {
            if (!paused) {
                double currentSpeed = speed;
                if (score >= 50 && score <= 70 || score >= 120) {
                    currentSpeed = specialSpeed;
                }
                posY -= currentSpeed * deltaTime;
            }
        }

        public void draw() {
            gc.setFill(Color.RED);
            if (score >= 50 && score <= 70 || score >= 120) {
                gc.setFill(Color.PEACHPUFF);
                gc.fillRect((int) posX - 5, (int) posY - 10, size + 10, size + 30);
            } else {
                gc.fillOval((int) posX, (int) posY, size, size);
            }
        }

        public boolean colide(Rocket Rocket) {
            double r = Rocket.size / 2.0 + size / 2.0;
            return distanceSq(this.posX + size / 2.0, this.posY + size / 2.0,
                    Rocket.posX + Rocket.size / 2.0, Rocket.posY + Rocket.size / 2.0) < r * r;
        }
    }

    public class Universe {
        double posX, posY;
        private int w, h, b, g, r;
        private double opacity;
        private final double speed = 200;

        public Universe() {
            posX = RAND.nextInt(WIDTH);
            posY = 0;
            w = RAND.nextInt(5) + 1;
            h = RAND.nextInt(5) + 1;
            r = RAND.nextInt(100) + 150;
            g = RAND.nextInt(100) + 150;
            b = RAND.nextInt(100) + 150;
            opacity = RAND.nextFloat();
            if (opacity < 0) opacity *= -1;
            if (opacity > 0.5) opacity = 0.5;
        }

        public void draw(double deltaTime) {
            if (opacity > 0.8) opacity -= 0.01;
            if (opacity < 0.1) opacity += 0.01;
            gc.setFill(Color.rgb(r, g, b, opacity));
            gc.fillOval((int) posX, (int) posY, w, h);

            if (!paused){
                posY += speed * deltaTime;
            }
        }

    }

    Bomb newBomb() {
        int bombTypeIndex = RAND.nextInt(BOMBS_IMG.length);
        Image bombImage = BOMBS_IMG[bombTypeIndex];
        int bombSize = PLAYER_SIZE;

        if (bombTypeIndex == BOMBS_IMG.length - 1) {
            bombSize = MEGADIHROCKET_SIZE;
        }

        return new Bomb(50 + RAND.nextInt(WIDTH - 100), 0, bombSize, bombImage);
    }

    double distanceSq(double x1, double y1, double x2, double y2) {
        return Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2);
    }

    private void saveScoreToDatabase() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://ep-empty-cherry-a1ihzs91-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require", "neondb_owner", "npg_ANZe5TQK1gop");
            String sql = "INSERT INTO scores (player_name, score) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, playerName);
            stmt.setInt(2, score);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void backToMainMenu(Stage currentStage) {
        if (soundManager != null) {
            soundManager.stopMusic();
        }

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/game/uaspbo_voidthreat_kapalangkasa/MainMenu.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle("Void Threat");
                stage.setScene(new Scene(root));
                stage.setMaximized(true);
                stage.show();

                currentStage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}