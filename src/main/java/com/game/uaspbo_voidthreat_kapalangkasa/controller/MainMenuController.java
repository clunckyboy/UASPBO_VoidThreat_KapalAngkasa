package com.game.uaspbo_voidthreat_kapalangkasa.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class MainMenuController {
    @FXML
    private Label welcomeText;

//    @FXML
//    protected void onHelloButtonClick() {
//        welcomeText.setText("Welcome to JavaFX Application!");
//    }

    @FXML
    private void handleExit() {
        // Cara standar menutup aplikasi
        System.exit(0);

        // Alternatif: jika kamu ingin menutup window saja, bukan seluruh app
        // Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        // stage.close();
    }
}