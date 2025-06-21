package com.game.uaspbo_voidthreat_kapalangkasa.controller;

import java.util.Optional;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainMenuController {

    @FXML
    private void handlePlayGame(ActionEvent event){
        try{

            // Dialog untuk input nama
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Masukkan Nama");
            dialog.setHeaderText("Masukkan Nama Anda untuk memulai permainan");
            dialog.setContentText("Nama:");

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String playerName = result.get().trim();

                if (playerName.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Nama Tidak Valid");
                    alert.setHeaderText(null);
                    alert.setContentText("Nama tidak boleh kosong!");
                    alert.showAndWait();
                    return;
                }

                // Logika peluncuran game
                BorderPane root = new BorderPane();
                Scene scene = new Scene(root, 400,400);

                Stage gameStage = new Stage();
                gameStage.setTitle("Void Threat");

                GameController gameController = new GameController(playerName);

                gameController.start(gameStage);

                Stage currentStage = (Stage)((Node)event.getSource()).getScene().getWindow();
                currentStage.close();
            }
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
        System.exit(0);
    }
}