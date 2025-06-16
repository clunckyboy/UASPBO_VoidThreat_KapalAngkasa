package com.game.uaspbo_voidthreat_kapalangkasa.controller;

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
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

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

    static final Image PLAYER_IMG = new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/player.png").toExternalForm());
    static final Image EXPLOSION_IMG = new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/explosion.png").toExternalForm());

    static final int EXPLOSION_W = 128;
    static final int EXPLOSION_ROWS = 3;
    static final int EXPLOSION_COL = 3;
    static final int EXPLOSION_H = 128;
    static final int EXPLOSION_STEPS = 15;

    static final Image BOMBS_IMG[] = {
        new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/1.png").toExternalForm()),
        new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/2.png").toExternalForm()),
        new Image(GameController.class.getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/3.png").toExternalForm()),
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


    /* Start Game */
    public void start(Stage stage) throws Exception {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> run(gc)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        canvas.setCursor(Cursor.MOVE);
        canvas.setOnMouseMoved(e -> mouseX = e.getX());
        canvas.setOnMouseClicked(e -> {
            if(shots.size() < MAX_SHOTS) shots.add(player.shoot());
            if(gameOver) {
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
        player = new Rocket(WIDTH / 2, HEIGHT - PLAYER_SIZE, PLAYER_SIZE, PLAYER_IMG);
        score = 0;
        IntStream.range(0, MAX_BOMBS).mapToObj(i -> this.newBomb()).forEach(Bombs::add);
    }

    /*Run Graphics*/
    private void run(GraphicsContext gc) {
        gc.setFill(Color.grayRgb(20));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font(20));
        gc.setFill(Color.WHITE);
        gc.fillText("Score: " + score, 60,  20);


        if(gameOver) {
            gc.setFont(Font.font(35));
            gc.setFill(Color.YELLOW);
            gc.fillText("Game Over \n Your Score is: " + score + " \n Click to play again", WIDTH / 2, HEIGHT /2.5);
            //	return;
        }
        univ.forEach(Universe::draw);

        player.update();
        player.draw();
        player.posX = (int) mouseX;

        Bombs.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
            if(player.colide(e) && !player.exploding) {
                player.explode();
            }
        });


        for (int i = shots.size() - 1; i >=0 ; i--) {
            Shot shot = shots.get(i);
            if(shot.posY < 0 || shot.toRemove)  {
                shots.remove(i);
                continue;
            }
            shot.update();
            shot.draw();
            for (Bomb bomb : Bombs) {
                if(shot.colide(bomb) && !bomb.exploding) {
                    score++;
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
        for (int i = 0; i < univ.size(); i++) {
            if(univ.get(i).posY > HEIGHT)
                univ.remove(i);
        }
    }

    /* Player */
    public class Rocket {

        int posX, posY, size;
        boolean exploding, destroyed;
        Image img;
        int explosionStep = 0;

        public Rocket(int posX, int posY, int size, Image image){
            this.posX = posX;
            this.posY = posY;
            this.size = size;
            img = image;
        }

        public Shot shoot(){
            return new Shot(posX + size / 2 - Shot.size / 2, posY - Shot.size);
        }

        public void update(){
            if(exploding) explosionStep++;
            destroyed = explosionStep > EXPLOSION_STEPS;
        }

        public void draw(){
            if(exploding){
                gc.drawImage(EXPLOSION_IMG, explosionStep % EXPLOSION_COL * EXPLOSION_W,
                        (explosionStep / EXPLOSION_ROWS) * EXPLOSION_H + 1, EXPLOSION_W, EXPLOSION_H, posX, posY, size, size);
            } else {
                gc.drawImage(img, posX, posY, size, size);
            }
        }

        public boolean colide(Rocket other){
            int d = distance(this.posX + size / 2, this.posY + size / 2,
                    other.posX + other.size / 2, other.posY + other.size / 2);
            return d < other.size / 2 + this.size / 2;
        }

        public void explode(){
            exploding = true;
            explosionStep = -1;
        }
    }



    public class Bomb extends Rocket {

        int SPEED = (score/5) + 2;

        public Bomb(int posX, int posY, int size, Image image){
            super(posX,posY,size,image);
        }

        public void update(){
            super.update();
            if(!exploding && !destroyed) posY += SPEED;
            if(posY > HEIGHT) destroyed = true;
        }
    }

    /* Peluru */
    public class Shot {

        public boolean toRemove;

        int posX, posY, speed = 10;
        static final int size = 6;

        public Shot(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
        }

        public void update(){
            posY-=speed;
        }

        public void draw(){
            gc.setFill(Color.RED);
            if(score >= 50 && score <= 70 || score >= 120){
                gc.setFill(Color.PEACHPUFF);
                speed = 50;
                gc.fillRect(posX-5, posY-10, size+10, size+30);
            } else{
                gc.fillOval(posX, posY, size, size);
            }
        }

        public boolean colide(Rocket Rocket){
            int distance = distance(this.posX + size / 2, this.posY + size / 2,
                    Rocket.posX + Rocket.size / 2, Rocket.posY + Rocket.size / 2 );
            return distance < Rocket.size / 2 + size / 2;
        }
    }

    //environtment
    public class Universe {
        int posX, posY;
        private int h,w,r,g,b;
        private double opacity;

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

        public void draw(){
            if(opacity > 0.8) opacity -= 0.01;
            if(opacity < 0.1) opacity += 0.01;
            gc.setFill(Color.rgb(r,g,b,opacity));
            gc.fillOval(posX, posY, w, h);
            posY += 20;
        }
    }

    Bomb newBomb() {
        return new Bomb(50 + RAND.nextInt(WIDTH - 100), 0, PLAYER_SIZE,
                BOMBS_IMG[RAND.nextInt(BOMBS_IMG.length)]);
    }

    int distance(int x1, int y1, int x2, int y2){
        return (int) Math.sqrt(Math.pow((x1-x2),2) + Math.pow((y1 - y2), 2));
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
