module it.polimi.ingsw.am56 {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.compiler;


    opens it.polimi.ingsw.am56 to javafx.fxml;
    exports it.polimi.ingsw.am56;
}