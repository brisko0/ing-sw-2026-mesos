package it.polimi.ingsw.am56.network.message.update;

import java.io.Serializable;

public interface ServerMessage extends Serializable {
    // Ora accetta un client generico (che sia RMI o Socket)
    void accept(MessageVisitor visitor);
}