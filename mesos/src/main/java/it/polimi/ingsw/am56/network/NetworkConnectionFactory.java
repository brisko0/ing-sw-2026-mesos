package it.polimi.ingsw.am56.network;

import it.polimi.ingsw.am56.core.ConnectionFactory;
import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.network.rmi.client.RmiClient;
import it.polimi.ingsw.am56.network.rmi.client.VirtualServerRmi;
import it.polimi.ingsw.am56.network.socket.client.SocketClient;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Concrete {@link ConnectionFactory} that establishes the client's connection to the server over
 * either RMI or sockets, returning in both cases a {@link ServerAdapter} the client can talk through.
 * It hides the transport-specific setup (RMI registry lookup and stub registration, or opening a
 * socket) behind a uniform creation API.
 */
public class NetworkConnectionFactory implements ConnectionFactory {

    /**
     * Establishes an RMI connection: looks up the {@code MesosServer} stub in the registry at the
     * given address, creates an {@link RmiClient}, links it to the stub and registers it on the server.
     *
     * @param ip   the server host
     * @param port the RMI registry port
     * @return the connected RMI client as a {@link ServerAdapter}
     * @throws Exception if the registry lookup, stub binding or client registration fails
     */
    @Override
    public ServerAdapter createRmiConnection(String ip, int port) throws Exception {
        System.out.println("[FACTORY] Inizio procedura di connessione RMI...");

        Registry registry = LocateRegistry.getRegistry(ip, port);
        VirtualServerRmi serverStub = (VirtualServerRmi) registry.lookup("MesosServer");

        RmiClient rmiClient = new RmiClient();
        rmiClient.setServerStub(serverStub);

        serverStub.addClient(rmiClient);

        System.out.println("[FACTORY] Connessione RMI completata e registrata sul server.");
        return rmiClient;
    }

    /**
     * Establishes a socket connection by opening a {@link SocketClient} to the given address.
     *
     * @param ip   the server host
     * @param port the server's socket port
     * @return the connected socket client as a {@link ServerAdapter}
     * @throws Exception if the socket connection cannot be established
     */
    @Override
    public ServerAdapter createSocketConnection(String ip, int port) throws Exception {
        System.out.println("[FACTORY] Inizio procedura di connessione Socket...");

        SocketClient socketClient = new SocketClient(ip, port);


        System.out.println("[FACTORY] Connessione Socket stabilita.");
        return socketClient;
    }
}