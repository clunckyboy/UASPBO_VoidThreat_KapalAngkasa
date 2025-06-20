package com.game.uaspbo_voidthreat_kapalangkasa;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class GameApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        try (InputStream is = getClass().getResourceAsStream("/com/game/uaspbo_voidthreat_kapalangkasa/assets/PressStart2P-Regular.ttf")) {
            if (is == null) {
                System.out.println("Font not found via stream!");
            } else {
                Font font = Font.loadFont(is, 12);
                System.out.println("Font loaded: " + font.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        FXMLLoader fxmlLoader = new FXMLLoader(GameApp.class.getResource("MainMenu.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Void Threat");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}