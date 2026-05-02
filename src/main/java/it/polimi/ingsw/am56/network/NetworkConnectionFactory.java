package it.polimi.ingsw.am56.network;

import it.polimi.ingsw.am56.core.ConnectionFactory;
import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.network.rmi.client.RmiClient;
import it.polimi.ingsw.am56.network.rmi.client.VirtualServerRmi;
import it.polimi.ingsw.am56.network.socket.client.SocketClient; // Quando l'avrai creata

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class NetworkConnectionFactory implements ConnectionFactory {

    @Override
    public ServerAdapter createRmiConnection(String ip, int port) throws Exception {
        System.out.println("[FACTORY] Inizio procedura di connessione RMI...");

        // per ora MesosServer hardcodato
        Registry registry = LocateRegistry.getRegistry(ip, port);
        VirtualServerRmi serverStub = (VirtualServerRmi) registry.lookup("MesosServer");

        RmiClient rmiClient = new RmiClient();
        rmiClient.setServerStub(serverStub);

        serverStub.addClient(rmiClient);

        System.out.println("[FACTORY] Connessione RMI completata e registrata sul server.");

        // restituisco il client alla GUI
        return rmiClient;
    }

    @Override
    public ServerAdapter createSocketConnection(String ip, int port) throws Exception {
        System.out.println("[FACTORY] Inizio procedura di connessione Socket...");

        SocketClient socketClient = new SocketClient(ip, port);


        System.out.println("[FACTORY] Connessione Socket stabilita.");
        return socketClient;
    }
}