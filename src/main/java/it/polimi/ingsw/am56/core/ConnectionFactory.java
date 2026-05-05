package it.polimi.ingsw.am56.core;

public interface ConnectionFactory {
    ServerAdapter createSocketConnection(String ip, int port) throws Exception;
    ServerAdapter createRmiConnection(String ip, int port) throws Exception;
}
