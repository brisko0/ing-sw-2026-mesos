package it.polimi.ingsw.am56.core;


// interfaccia per permettere alla GUI di richiedere una connessione senza conoscere i dettagli implementativi
// dei protocolli

public interface ConnectionFactory {
    ServerAdapter createSocketConnection(String ip, int port) throws Exception;
    ServerAdapter createRmiConnection(String ip, int port) throws Exception;
}
