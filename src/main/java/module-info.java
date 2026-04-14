module it.polimi.ingsw.am56 {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.compiler;
    requires com.google.gson;
    requires java.rmi;


    opens it.polimi.ingsw.am56 to javafx.fxml;
    exports it.polimi.ingsw.am56;
    exports it.polimi.ingsw.am56.model;
    opens it.polimi.ingsw.am56.model to javafx.fxml;
    exports it.polimi.ingsw.am56.model.board;
    opens it.polimi.ingsw.am56.model.board to javafx.fxml, com.google.gson;
}