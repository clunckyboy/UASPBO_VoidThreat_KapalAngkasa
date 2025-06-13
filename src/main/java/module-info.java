module com.game.uaspbo_voidthreat_kapalangkasa {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.game.uaspbo_voidthreat_kapalangkasa to javafx.fxml;
    exports com.game.uaspbo_voidthreat_kapalangkasa;
    exports com.game.uaspbo_voidthreat_kapalangkasa.controller;
    opens com.game.uaspbo_voidthreat_kapalangkasa.controller to javafx.fxml;
}