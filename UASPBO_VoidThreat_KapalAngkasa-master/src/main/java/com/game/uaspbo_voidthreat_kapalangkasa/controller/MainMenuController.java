package com.game.uaspbo_voidthreat_kapalangkasa.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.scene.control.TextInputDialog;
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
            }

            BorderPane root = new BorderPane();
            Scene scene = new Scene(root, 400,400);

            //Game Stage
            Stage gameStage = new Stage();
            gameStage.setTitle("Void Threat");
            gameStage.setMaximized(false);
            GameController gameController = new GameController(playerName);

            gameController.start(gameStage);

            Stage currentStage = (Stage)((Node)event.getSource()).getScene().getWindow();
            currentStage.close();
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
