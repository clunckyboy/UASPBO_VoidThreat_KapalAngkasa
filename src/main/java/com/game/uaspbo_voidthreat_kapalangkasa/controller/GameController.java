package com.game.uaspbo_voidthreat_kapalangkasa.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;


import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode; // Import KeyCode
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

public class GameController {

    private String playerName;
    public GameController(String playerName) {
        this.playerName = playerName;
    }

    //variables
    private static final Random RAND = new Random();
    private static int WIDTH;
    private static int HEIGHT;
    private static final int PLAYER_SIZE = 60;

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
            new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/4.png").toExternalForm()),
            new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/5.png").toExternalForm()),
            new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/6.png").toExternalForm()),
            new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/7.png").toExternalForm()),
            new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/8.png").toExternalForm()),
            new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/9.png").toExternalForm()),
            new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/10.png").toExternalForm())
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
    private long lastFrameTime; // For Delta Time calculation

    // *** NEW VARIABLE *** to track if the special sound has been played
    private boolean wasInSpecialShotMode = false;

    // Variables for keyboard control
    private boolean goLeft, goRight, shootPressed;
    private final double PLAYER_SPEED = 200; // Kecepatan pergerakan pemain (pixels per second)

    // New variable for pause state
    private boolean paused = false;

    // --- NEW LIVES VARIABLES ---
    private int lives;
    private boolean invulnerable;
    private double invulnerableTimer;
    private static final double INVULNERABILITY_DURATION = 2.0; // 2 seconds of invulnerability
    private static final double RESPAWN_FLASH_INTERVAL = 0.1; // Flash every 0.1 seconds
    // --- END NEW LIVES VARIABLES ---


    /* Start Game */
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

        // Handle keyboard input
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
            if(!gameOver && !player.exploding && !paused) {
                if(shots.size() < MAX_SHOTS)
                    shots.add(player.shoot()); SoundManager.playSound("sfx1.wav");
            }

            if(gameOver) {
                gameLoop.stop();
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

    //setup the game
    private void setup() {
        univ = new ArrayList<>();
        shots = new ArrayList<>();
        Bombs = new ArrayList<>();
        player = new Rocket(WIDTH / 2.0, HEIGHT - 85, PLAYER_SIZE, PLAYER_IMG);
        score = 0;
        lives = 3; // Initialize lives to 3
        invulnerable = false; // Player not invulnerable at start
        invulnerableTimer = 0; // Reset invulnerability timer

        IntStream.range(0, MAX_BOMBS).mapToObj(i -> this.newBomb()).forEach(Bombs::add);

        goLeft = false;
        goRight = false;
        shootPressed = false;
        paused = false;
        wasInSpecialShotMode = false; // Reset the flag on setup
        gameOver = false; // Reset game over state
    }

    /*Run Graphics*/
    private void run(GraphicsContext gc, double deltaTime) {
        gc.setFill(Color.grayRgb(20));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font(30));
        gc.setFill(Color.WHITE);
        gc.fillText("Score: " + score, 75,  40);
        gc.fillText("Lives: " + lives, 75, 80); // Display lives

        player.posX = (int) mouseX;

        if(!gameOver && !paused) {
            univ.forEach(u -> u.draw(deltaTime));
            player.update(deltaTime);

            // Handle invulnerability
            if (invulnerable) {
                invulnerableTimer += deltaTime;
                if (invulnerableTimer >= INVULNERABILITY_DURATION) {
                    invulnerable = false;
                    invulnerableTimer = 0;
                }
                // Flashing effect for invulnerability
                if ((int)(invulnerableTimer / RESPAWN_FLASH_INTERVAL) % 2 == 0) {
                    player.draw(); // Draw player only on alternate flashes
                }
            } else {
                player.draw();
            }


            if (goLeft) player.posX -= PLAYER_SPEED * deltaTime;
            if (goRight) player.posX += PLAYER_SPEED * deltaTime;

            if (player.posX < 0) player.posX = 0;
            if (player.posX > WIDTH - PLAYER_SIZE) player.posX = WIDTH - PLAYER_SIZE;

            Bombs.stream().peek(bomb -> bomb.update(deltaTime)).peek(Rocket::draw).forEach(e -> {
                if(player.colide(e) && !player.exploding && !invulnerable) { // Check invulnerability
                    SoundManager.playSound("sfx3.wav");
                    player.explode();
                    lives--; // Decrement lives
                    if (lives <= 0) {
                        gameOver = true;
                    } else {
                        // Respawn effect
                        invulnerable = true;
                        invulnerableTimer = 0;
                        player.posX = WIDTH / 2.0; // Reset player position
                        player.posY = HEIGHT - 85;
                        player.exploding = false; // Stop explosion effect on respawn
                        player.destroyed = false; // Player is not destroyed if lives remain
                    }
                }
            });

            for (int i = shots.size() - 1; i >=0 ; i--) {
                Shot shot = shots.get(i);
                if(shot.posY < 0 || shot.toRemove)  {
                    shots.remove(i);
                    continue;
                }
                shot.update(deltaTime);
                shot.draw();
                for (Bomb bomb : Bombs) {
                    if(shot.colide(bomb) && !bomb.exploding) {
                        score++;
                        SoundManager.playSound("sfx2.wav");
                        bomb.explode();
                        shot.toRemove = true;
                    }
                }
            }

            for (int i = Bombs.size() - 1; i >= 0; i--){
                if(Bombs.get(i).destroyed)  {
                    Bombs.set(i, newBomb());
                }
            }

            // This logic is now handled in the main game loop, not per-bullet.
            boolean isInSpecialShotMode = (score >= 50 && score <= 70 || score >= 120);
            if (isInSpecialShotMode && !wasInSpecialShotMode) {
                // If we JUST entered the special mode, play the sound.
                SoundManager.playSound("sfx4.wav");
            }
            // Update the state for the next frame.
            this.wasInSpecialShotMode = isInSpecialShotMode;
            // gameOver is now set based on lives <= 0
            if(RAND.nextInt(10) > 2) {
                univ.add(new Universe());
            }

            for (int i = univ.size() - 1; i >= 0; i--) {
                if(univ.get(i).posY > HEIGHT)
                    univ.remove(i);
            }
        } else {
            univ.forEach(u -> u.draw(deltaTime));
            // Only draw player if not in game over and not in flashing invulnerable state
            if (!gameOver && (!invulnerable || (int)(invulnerableTimer / RESPAWN_FLASH_INTERVAL) % 2 == 0)) {
                player.draw();
            }
            Bombs.forEach(Rocket::draw);
            shots.forEach(Shot::draw);
        }

        if(gameOver) {
            gc.setFont(Font.font(35));
            gc.setFill(Color.YELLOW);
            gc.fillText("Game Over \n Your Score is: " + score + " \n Click to back to main menu", WIDTH / 2, HEIGHT /2.5);
        } else if (paused) {
            gc.setFont(Font.font(40));
            gc.setFill(Color.CYAN);
            gc.fillText("PAUSED", WIDTH / 2, HEIGHT / 2);
        }
    }

    /* Player */
    public class Rocket {
        double posX, posY;
        int size;
        boolean exploding, destroyed;
        Image img;

        private double explosionTimer = 0;
        private static final double EXPLOSION_DURATION = 0.7;


        public Rocket(double posX, double posY, int size, Image image){
            this.posX = posX;
            this.posY = posY;
            this.size = size;
            img = image;
        }

        public Shot shoot(){
            return new Shot(posX + size / 2.0 - Shot.size / 2.0, posY - Shot.size);
        }

        public void update(double deltaTime){
            if (exploding) {
                explosionTimer += deltaTime;
                // Corrected: Mark as destroyed once explosion animation is complete
                if (explosionTimer >= EXPLOSION_DURATION) {
                    destroyed = true;
                }
            }
            // Removed: player-specific 'destroyed' logic from here
        }

        public void draw(){
            if(exploding){
                int frame = (int)((explosionTimer / EXPLOSION_DURATION) * EXPLOSION_STEPS);

                if (frame >= EXPLOSION_STEPS) {
                    frame = EXPLOSION_STEPS - 1;
                }

                gc.drawImage(EXPLOSION_IMG, frame % EXPLOSION_COL * EXPLOSION_W,
                        (frame / EXPLOSION_ROWS) * EXPLOSION_H + 1, EXPLOSION_W, EXPLOSION_H,
                        (int)posX, (int)posY, size, size);
            } else {
                gc.drawImage(img, (int)posX, (int)posY, size, size);
            }
        }

        public boolean colide(Rocket other){
            int r = this.size / 2 + other.size / 2;
            return distanceSq(this.posX + size / 2.0, this.posY + size / 2.0,
                    other.posX + other.size / 2.0, other.posY + other.size / 2.0) < r * r;
        }

        public void explode(){
            exploding = true;
            explosionTimer = 0;
        }
    }

    public class Bomb extends Rocket {
        public double getSpeed() {
            return ((score / 5.0) + 2) * 20;
        }

        public Bomb(double posX, double posY, int size, Image image){
            super(posX,posY,size,image);
        }

        public void update(double deltaTime){
            super.update(deltaTime); // This will now correctly handle explosion-based 'destroyed'
            if(!exploding && !destroyed && !gameOver && !paused) posY += getSpeed() * deltaTime;
            if(posY > HEIGHT) destroyed = true; // Mark as destroyed if off-screen
        }
    }

    /* Peluru */
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

        public void update(double deltaTime){
            if (!gameOver && !paused) {
                double currentSpeed = speed;
                // The bullet only needs to know if it should be fast.
                // It is no longer responsible for playing the sound.
                if (score >= 50 && score <= 70 || score >= 120) {
                    currentSpeed = specialSpeed;
                }
                posY -= currentSpeed * deltaTime;
            }
        }

        public void draw(){
            gc.setFill(Color.RED);
            if(score >= 50 && score <= 70 || score >= 120){
                gc.setFill(Color.PEACHPUFF);
                gc.fillRect((int)posX-5, (int)posY-10, size+10, size+30);
            } else{
                gc.fillOval((int)posX, (int)posY, size, size);
            }
        }

        public boolean colide(Rocket Rocket){
            int r = Rocket.size / 2 + size / 2;
            return distanceSq(this.posX + size / 2.0, this.posY + size / 2.0,
                    Rocket.posX + Rocket.size / 2.0, Rocket.posY + Rocket.size / 2.0) < r * r;
        }
    }

    //environment
    public class Universe {
        double posX, posY;
        private int h,w,r,g,b;
        private double opacity;
        private final double speed = 200;

        public Universe(){
            posX = RAND.nextInt(WIDTH);
            posY = 0;
            w = RAND.nextInt(5) + 1;
            h = RAND.nextInt(5) + 1;
            r = RAND.nextInt(100) + 150;
            g = RAND.nextInt(100) + 150;
            b = RAND.nextInt(100) + 150;
            opacity = RAND.nextFloat();
            if(opacity < 0) opacity *= -1;
            if(opacity > 0.5) opacity = 0.5;
        }

        public void draw(double deltaTime){
            if(opacity > 0.8) opacity -= 0.01;
            if(opacity < 0.1) opacity += 0.01;
            gc.setFill(Color.rgb(r,g,b,opacity));
            gc.fillOval((int)posX, (int)posY, w, h);
            posY += speed * deltaTime;
        }
    }

    Bomb newBomb() {
        return new Bomb(50 + RAND.nextInt(WIDTH - 100), 0, PLAYER_SIZE,
                BOMBS_IMG[RAND.nextInt(BOMBS_IMG.length)]);
    }

    double distanceSq(double x1, double y1, double x2, double y2){
        return Math.pow((x1-x2),2) + Math.pow((y1 - y2), 2);
    }

    /* Simpan skor ke database */
    private void saveScoreToDatabase() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/void_threat", "postgres", "12345678");
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