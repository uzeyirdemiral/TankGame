module com.example.tank_game {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.tank_game to javafx.fxml;
    exports com.example.tank_game;
}