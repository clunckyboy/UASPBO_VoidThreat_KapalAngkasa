module com.game.uaspbo_voidthreat_kapalangkasa {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.game.uaspbo_voidthreat_kapalangkasa to javafx.fxml;
    opens com.game.uaspbo_voidthreat_kapalangkasa.model to javafx.base;
    exports com.game.uaspbo_voidthreat_kapalangkasa;
    exports com.game.uaspbo_voidthreat_kapalangkasa.controller;
    opens com.game.uaspbo_voidthreat_kapalangkasa.controller to javafx.fxml;
}