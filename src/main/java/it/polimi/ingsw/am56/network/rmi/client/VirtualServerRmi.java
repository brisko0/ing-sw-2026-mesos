package it.polimi.ingsw.am56.network.rmi.client;

import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.network.VirtualServer;
import it.polimi.ingsw.am56.network.message.request.*;
import it.polimi.ingsw.am56.network.rmi.server.VirtualViewRmi;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface VirtualServerRmi extends Remote, VirtualServer {

    @Override
    void addPlayer(String nickname, String totemColor) throws InvalidActionException, RemoteException;

    @Override
    void startGame() throws InvalidActionException, RemoteException;

    @Override
    void moveTotem(TotemMoveRequest request) throws InvalidActionException, RemoteException;

    @Override
    void cardPick(CardPickRequest request) throws InvalidActionException, RemoteException;



    void addClient(VirtualViewRmi client) throws RemoteException;
}