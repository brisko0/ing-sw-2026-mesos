package it.polimi.ingsw.am56.network;

import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.rmi.RemoteException;

public interface VirtualServer {

    void addPlayer(String nickname, String totemColor) throws InvalidActionException, RemoteException;

    void startGame() throws InvalidActionException, RemoteException;

    void moveTotem(TotemMoveRequest request) throws InvalidActionException, RemoteException;

    void cardPick(CardPickRequest request) throws InvalidActionException, RemoteException;

}
