package com.game.uaspbo_voidthreat_kapalangkasa.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.scene.control.TextInputDialog;
// Import ImageView if you were to use it here. For now, it's not needed in this file.
// import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


import java.util.Optional;


public class MainMenuController {

    @FXML
    private void handlePlayGame(ActionEvent event){
        try{

            // Dialog untuk ambil nama
            String playerName = null;
            TextInputDialog dialog = new TextInputDialog("Player");
            dialog.setTitle("Masukkan Nama");
            dialog.setHeaderText("Masukkan Nama Anda untuk memulai permainan");
            dialog.setContentText("Nama:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                playerName = result.get();

                // Pindahkan seluruh logika peluncuran game ke dalam blok ini
                BorderPane root = new BorderPane();
                Scene scene = new Scene(root, 400,400);

                //Game Stage
                Stage gameStage = new Stage();
                gameStage.setTitle("Void Threat");
                gameStage.setMaximized(false);

                // =================================================================================
                // IMPORTANT: CHANGE REQUIRED IN YOUR GameController.java
                //
                // Image smoothing must be disabled inside your GameController, where your
                // game's sprites (player, enemies, etc.) are created as ImageView objects.
                // You cannot set it from this MainMenuController file.
                //
                // Find the code in GameController.java where you create your ImageViews
                // and add the .setSmooth(false) method call.
                //
                // EXAMPLE (to be placed inside GameController.java):
                //
                // ImageView playerSprite = new ImageView(yourPlayerImage);
                // playerSprite.setSmooth(false); // <-- ADD THIS LINE FOR EACH SPRITE
                //
                // ImageView enemySprite = new ImageView(yourEnemyImage);
                // enemySprite.setSmooth(false); // <-- AND THIS ONE
                // =================================================================================
                GameController gameController = new GameController(playerName);

                gameController.start(gameStage);

                Stage currentStage = (Stage)((Node)event.getSource()).getScene().getWindow();
                currentStage.close();
            }
            // Jika result.isPresent() false (cancel ditekan), tidak ada yang terjadi,
            // dan aplikasi tetap di Main Menu.

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRecordButton(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/game/uaspbo_voidthreat_kapalangkasa/Record.fxml"));
            Parent root = loader.load();
            root.getStylesheets().add(getClass().getResource("/com/game/uaspbo_voidthreat_kapalangkasa/style.css").toExternalForm());

            // New Stage
            Stage newStage = new Stage();
            newStage.setTitle("Void Threat");
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.show();

            Stage currentStage = (Stage)((Node)event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        // Cara standar menutup aplikasi
        System.exit(0);

    }
}