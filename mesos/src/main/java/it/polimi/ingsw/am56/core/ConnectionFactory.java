package it.polimi.ingsw.am56.core;

/**
 * Factory that lets the client open a connection to the server without knowing the transport details.
 * It returns a {@link ServerAdapter} for either transport (socket or RMI), so callers (the GUI/TUI and
 * the reconnection logic) depend only on this abstraction.
 */
public interface ConnectionFactory {
    /**
     * Opens a socket connection to the server.
     *
     * @param ip   the server host
     * @param port the server's socket port
     * @return the connected adapter
     * @throws Exception if the connection cannot be established
     */
    ServerAdapter createSocketConnection(String ip, int port) throws Exception;

    /**
     * Opens an RMI connection to the server.
     *
     * @param ip   the server host
     * @param port the RMI registry port
     * @return the connected adapter
     * @throws Exception if the connection cannot be established
     */
    ServerAdapter createRmiConnection(String ip, int port) throws Exception;
}