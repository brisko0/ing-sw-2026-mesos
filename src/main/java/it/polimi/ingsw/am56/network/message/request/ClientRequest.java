package it.polimi.ingsw.am56.network.message.request;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.io.Serializable;

public interface ClientRequest extends Serializable {
    // ogni request chiama il metodo giusto sul controller
    void executeOnServer(GameController controller) throws InvalidActionException;
}