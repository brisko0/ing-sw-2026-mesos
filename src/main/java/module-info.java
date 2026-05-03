module it.polimi.ingsw.am56 {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.compiler;
    requires com.google.gson;
    requires java.rmi;

    // Configurazione JavaFX
    opens it.polimi.ingsw.am56 to javafx.fxml;
    exports it.polimi.ingsw.am56;

    exports it.polimi.ingsw.am56.view.GUI to javafx.graphics;
    opens it.polimi.ingsw.am56.view.GUI to javafx.fxml;

    // Configurazione Model
    exports it.polimi.ingsw.am56.model;
    opens it.polimi.ingsw.am56.model to javafx.fxml;
    exports it.polimi.ingsw.am56.model.board;
    opens it.polimi.ingsw.am56.model.board to javafx.fxml, com.google.gson;
    exports it.polimi.ingsw.am56.model.exceptions;

    // Configurazione Network per RMI
    // Esportiamo i pacchetti a java.rmi affinché possa vedere le interfacce remote
    exports it.polimi.ingsw.am56.network to java.rmi;
    exports it.polimi.ingsw.am56.network.rmi.client to java.rmi;
    exports it.polimi.ingsw.am56.network.rmi.server to java.rmi;

    // Apriamo i pacchetti a java.rmi per consentire la creazione dei proxy via reflection
    opens it.polimi.ingsw.am56.network to java.rmi;
    opens it.polimi.ingsw.am56.network.rmi.client to java.rmi;
    opens it.polimi.ingsw.am56.network.rmi.server to java.rmi;

    opens it.polimi.ingsw.am56.model.card to com.google.gson;
    opens it.polimi.ingsw.am56.model.card.characterCard to com.google.gson;
    opens it.polimi.ingsw.am56.model.card.buildingCard to com.google.gson;
    opens it.polimi.ingsw.am56.model.card.eventCard to com.google.gson;
    exports it.polimi.ingsw.am56.network.message.request to java.rmi;
    opens it.polimi.ingsw.am56.network.message.request to java.rmi;
    exports it.polimi.ingsw.am56.network.message.update to java.rmi;
    opens it.polimi.ingsw.am56.network.message.update to java.rmi;
    exports it.polimi.ingsw.am56.core;
    opens it.polimi.ingsw.am56.core to javafx.fxml;
    exports it.polimi.ingsw.am56.view.GUI.controllers to javafx.graphics;
    opens it.polimi.ingsw.am56.view.GUI.controllers to javafx.fxml;
}