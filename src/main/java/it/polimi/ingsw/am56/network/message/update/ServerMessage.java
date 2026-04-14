package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.rmi.client.RmiClient;
import java.io.Serializable;

public interface ServerMessage extends Serializable {
    void executeOnClient(RmiClient client);
}