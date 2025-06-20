package com.game.uaspbo_voidthreat_kapalangkasa.controller;

import com.game.uaspbo_voidthreat_kapalangkasa.model.PlayerRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


import java.sql.*;


import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.io.IOException;

public class RecordController {

    @FXML private TableView<PlayerRecord> scoreTable;
    @FXML private TableColumn<PlayerRecord, String> colNo;
    @FXML private TableColumn<PlayerRecord, String> colPlayerName;
    @FXML private TableColumn<PlayerRecord, String> colScore;

    private final ObservableList<PlayerRecord> scoreList = FXCollections.observableArrayList();

    @FXML
    public void initialize(){
        colNo.setCellValueFactory(new PropertyValueFactory<>("no"));
        colPlayerName.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));

        loadTopScoresFromDatabase();
        scoreTable.setItems(scoreList);
    }

    private void loadTopScoresFromDatabase() {
        String url = "jdbc:postgresql://ep-empty-cherry-a1ihzs91-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require";
        String user = "neondb_owner";
        String password = "npg_ANZe5TQK1gop";

        String query = "SELECT player_name, score FROM scores ORDER BY score DESC LIMIT 10";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int rowNum = 1;
            while (rs.next()) {
                String name = rs.getString("player_name");
                int score = rs.getInt("score");
                scoreList.add(new PlayerRecord(String.valueOf(rowNum), name, String.valueOf(score)));
                rowNum++;
            }

            // Tambah data kosong jika kurang dari 10
            while (rowNum <= 10) {
                scoreList.add(new PlayerRecord(String.valueOf(rowNum), "Empty", "n/a"));
                rowNum++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleBackButton(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/game/uaspbo_voidthreat_kapalangkasa/MainMenu.fxml"));
            Parent root = loader.load();

            // New Stage
            Stage newStage = new Stage();
            newStage.setTitle("Void Threat");
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.show();

            Stage currentStage = (Stage)((Node)event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



}
