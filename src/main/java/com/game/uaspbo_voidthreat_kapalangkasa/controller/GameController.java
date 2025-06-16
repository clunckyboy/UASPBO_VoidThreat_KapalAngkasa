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

public class GameController {

    private String playerName;
    public GameController(String playerName) {
        this.playerName = playerName;
    }

    //variables
    private static final Random RAND = new Random();
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
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

    /* Start Game */
    public void start(Stage stage) throws Exception {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        // **MODIFICATION**: Use AnimationTimer for a smooth game loop and delta time calculation
        lastFrameTime = System.nanoTime();
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Calculate time elapsed since last frame, in seconds
                double deltaTime = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;
                run(gc, deltaTime);
            }
        };
        gameLoop.start();

        canvas.setCursor(Cursor.MOVE);
        canvas.setOnMouseMoved(e -> mouseX = e.getX());
        canvas.setOnMouseClicked(e -> {
            if(shots.size() < MAX_SHOTS)
                shots.add(player.shoot()); SoundManager.playSound("sfx1.wav");
            if(gameOver) {
                // Stop the game loop before changing scenes
                gameLoop.stop();
                saveScoreToDatabase();
                backToMainMenu(stage); // kembali ke Main Menu
            }
        });

        setup();
        stage.setScene(new Scene(new StackPane(canvas)));
        stage.setTitle("Void Threat");
        stage.show();
    }

    //setup the game
    private void setup() {
        univ = new ArrayList<>();
        shots = new ArrayList<>();
        Bombs = new ArrayList<>();
        player = new Rocket(WIDTH / 2.0, HEIGHT - PLAYER_SIZE, PLAYER_SIZE, PLAYER_IMG);
        score = 0;
        IntStream.range(0, MAX_BOMBS).mapToObj(i -> this.newBomb()).forEach(Bombs::add);
    }

    /*Run Graphics*/
    // **MODIFICATION**: Run method now accepts deltaTime to keep game speed consistent
    private void run(GraphicsContext gc, double deltaTime) {
        gc.setFill(Color.grayRgb(20));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font(20));
        gc.setFill(Color.WHITE);
        gc.fillText("Score: " + score, 60,  20);

        if(gameOver) {
            gc.setFont(Font.font(35));
            gc.setFill(Color.YELLOW);
            gc.fillText("Game Over \n Your Score is: " + score + " \n Click to back to main menu", WIDTH / 2, HEIGHT /2.5);
            return; // Stop processing the game loop when the game is over
        }

        // **MODIFICATION**: Pass deltaTime to objects that need to update their position
        univ.forEach(u -> u.draw(deltaTime));

        player.update(deltaTime);
        player.draw();
        player.posX = (int) mouseX;

        Bombs.stream().peek(bomb -> bomb.update(deltaTime)).peek(Rocket::draw).forEach(e -> {
            if(player.colide(e) && !player.exploding) {
                SoundManager.playSound("sfx3.wav");
                player.explode();
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

        gameOver = player.destroyed;
        if(RAND.nextInt(10) > 2) {
            univ.add(new Universe());
        }

        // **MODIFICATION**: Optimized removal loop by iterating backwards
        for (int i = univ.size() - 1; i >= 0; i--) {
            if(univ.get(i).posY > HEIGHT)
                univ.remove(i);
        }
    }

    /* Player */
    /* Player */
    public class Rocket {
        // **MODIFICATION**: Use double for position for precision
        double posX, posY;
        int size;
        boolean exploding, destroyed;
        Image img;

        // --- NEW AND MODIFIED VARIABLES FOR ANIMATION SPEED ---
        // This timer tracks how long the explosion has been active.
        private double explosionTimer = 0;

        // **THE IMPORTANT PART**: This is your new control knob.
        // Set how long the explosion animation should last, in seconds.
        // Smaller number = faster explosion. Larger number = slower explosion.
        private static final double EXPLOSION_DURATION = 0.7; // e.g., 0.7 seconds total duration


        public Rocket(double posX, double posY, int size, Image image){
            this.posX = posX;
            this.posY = posY;
            this.size = size;
            img = image;
        }

        public Shot shoot(){
            return new Shot(posX + size / 2.0 - Shot.size / 2.0, posY - Shot.size);
        }

        // **MODIFICATION**: The update method now needs deltaTime to update the timer.
        public void update(double deltaTime){
            if (exploding) {
                // If exploding, add the elapsed time to our timer.
                explosionTimer += deltaTime;
            }
            // The rocket is considered destroyed once the timer has passed the desired duration.
            destroyed = explosionTimer > EXPLOSION_DURATION;
        }

        public void draw(){
            if(exploding){
                // --- NEW ANIMATION LOGIC ---
                // Calculate which frame to show based on time, not frame count.
                int frame = (int)((explosionTimer / EXPLOSION_DURATION) * EXPLOSION_STEPS);

                // Make sure we don't try to draw a frame that doesn't exist.
                if (frame >= EXPLOSION_STEPS) {
                    frame = EXPLOSION_STEPS - 1;
                }

                // Draw the calculated frame.
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
            // Reset the timer to 0 when the explosion starts.
            explosionTimer = 0;
        }
    }

    public class Bomb extends Rocket {
        // **MODIFICATION**: Speed is now in pixels-per-second
        public double getSpeed() {
            // Original logic was ((score/5) + 2) pixels-per-frame at 10fps.
            // We multiply by 10 to get an equivalent pixels-per-second speed.
            return ((score / 5.0) + 2) * 20;
        }

        public Bomb(double posX, double posY, int size, Image image){
            super(posX,posY,size,image);
        }

        // **MODIFICATION**: Update method uses deltaTime for consistent speed
        public void update(double deltaTime){
            super.update(deltaTime);
            if(!exploding && !destroyed) posY += getSpeed() * deltaTime;
            if(posY > HEIGHT) destroyed = true;
        }
    }

    /* Peluru */
    public class Shot {
        public boolean toRemove;
        // **MODIFICATION**: Position is double, speed is in pixels-per-second
        double posX, posY;
        double speed = 100; // 10 pixels/frame at 10fps -> 100 pixels/second
        double specialSpeed = 500; // A faster speed for the power-up
        static final int size = 6;

        public Shot(double posX, double posY) {
            this.posX = posX;
            this.posY = posY;
        }

        // **MODIFICATION**: Uses deltaTime for consistent speed
        public void update(double deltaTime){
            double currentSpeed = speed;
            if (score >= 50 && score <= 70 || score >= 120) {
                currentSpeed = specialSpeed;
            }
            posY -= currentSpeed * deltaTime;
        }

        public void draw(){
            // **MODIFICATION**: Cast to int for drawing. Logic moved to update().
            gc.setFill(Color.RED);
            if(score >= 50 && score <= 70 || score >= 120){
                gc.setFill(Color.PEACHPUFF);
                gc.fillRect((int)posX-5, (int)posY-10, size+10, size+30);
            } else{
                gc.fillOval((int)posX, (int)posY, size, size);
            }
        }

        // **MODIFICATION**: Uses faster squared-distance collision check
        public boolean colide(Rocket Rocket){
            int r = Rocket.size / 2 + size / 2;
            return distanceSq(this.posX + size / 2.0, this.posY + size / 2.0,
                    Rocket.posX + Rocket.size / 2.0, Rocket.posY + Rocket.size / 2.0) < r * r;
        }
    }

    //environment
    public class Universe {
        double posX, posY; // **MODIFICATION**: Position is double
        private int h,w,r,g,b;
        private double opacity;
        // **MODIFICATION**: Speed in pixels-per-second
        private final double speed = 200; // 20 pixels/frame at 10fps -> 200 pixels/second

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

        // **MODIFICATION**: draw() now also updates position using deltaTime
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

    // **OPTIMIZATION**: New method to calculate squared distance, avoids expensive Math.sqrt()
    double distanceSq(double x1, double y1, double x2, double y2){
        return Math.pow((x1-x2),2) + Math.pow((y1 - y2), 2);
    }

    /* Simpan skor ke database */
    private void saveScoreToDatabase() {
        try {
            // It's good practice to use try-with-resources for DB connections
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