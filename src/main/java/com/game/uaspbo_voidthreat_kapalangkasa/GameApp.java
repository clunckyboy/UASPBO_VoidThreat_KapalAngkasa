package com.game.uaspbo_voidthreat_kapalangkasa;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class GameApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Font font = Font.loadFont(getClass().getResource("/com/game/uaspbo_voidthreat_kapalangkasa/assets/PressStart2P-Regular.ttf").toExternalForm(), 12);

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