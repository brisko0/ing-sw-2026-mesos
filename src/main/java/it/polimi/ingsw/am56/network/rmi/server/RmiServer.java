package it.polimi.ingsw.am56.network.rmi.server;

import it.polimi.ingsw.am56.controller.EndGameService;
import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.controller.PlayerVirtualView;
import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.rmi.client.VirtualServerRmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RMI side of the server: an exported {@link VirtualServerRmi} that clients look up in the registry to
 * join the game, send requests and ping the server. It shares the same {@link GameModel},
 * {@link GameController} and {@link EndGameService} as the socket server, so RMI and socket clients
 * play in one game.
 * <p>
 * For each joining client it creates an {@link RmiClientHandler} (the server-side connection) and a
 * {@link PlayerVirtualView} registered as a model observer, so the client receives updates. Requests
 * are executed on a cached thread pool; any {@link InvalidActionException} is reported back to the
 * originating client.
 */
public class RmiServer extends UnicastRemoteObject implements VirtualServerRmi {
    private final GameModel model;
    private final GameController controller;
    private final int port;
    private final EndGameService endGameService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Creates and exports the RMI server.
     *
     * @param port           the registry port the server will bind on when started
     * @param model          the shared game model
     * @param controller     the shared game controller that executes requests
     * @param endGameService the shared end-game service
     * @throws RemoteException if the object cannot be exported
     */
    public RmiServer(int port, GameModel model, GameController controller, EndGameService endGameService) throws RemoteException {
        super();
        this.port = port;
        this.model = model;
        this.controller = controller;
        this.endGameService = endGameService;
    }

    /**
     * Creates the RMI registry on the configured port and binds this server under the name
     * {@code "MesosServer"}. Startup errors are logged rather than thrown.
     */
    public void start() {
        try {
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("MesosServer", this);
            System.out.println("[RMI SERVER] RMI Server pronto sulla porta " + port);
        } catch (Exception e) {
            System.err.println("[RMI SERVER] Errore nell'avvio del server RMI: " + e.getMessage());
        }
    }

    /**
     * Registers a new RMI client: starts a {@link RmiClientHandler} for it and attaches a
     * {@link PlayerVirtualView} as a model observer so the client receives game updates. The
     * connection is refused (the call throws) once the game is in progress and no reconnection window
     * is open — see {@link GameController#acceptsNewConnections()} — so outsiders cannot join a game
     * that has already started.
     *
     * @param clientStub the joining client's remote view stub
     * @throws RemoteException if the remote call fails, or if the game is in progress and no new
     *                         connections are being accepted
     */
    @Override
    public void addClient(VirtualViewRmi clientStub) throws RemoteException {
        if (!controller.acceptsNewConnections()) {
            throw new RemoteException("Partita già in corso: non è possibile collegarsi.");
        }

        RmiClientHandler rmiConnection = new RmiClientHandler(clientStub, controller);
        new Thread(rmiConnection).start();
        PlayerVirtualView virtualView =
                new PlayerVirtualView(model, rmiConnection, endGameService);
        model.addObserver(virtualView);
    }

    /**
     * Executes a client request asynchronously on the controller. If it fails with an
     * {@link InvalidActionException}, the error is reported back to the originating client.
     *
     * @param request    the request to execute
     * @param clientStub the originating client's stub, used to report errors back
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void sendRequest(ClientRequest request, VirtualViewRmi clientStub) throws RemoteException {
        executor.submit(() -> {
            try {
                request.executeOnServer(controller);
            } catch (InvalidActionException e) {
                try {
                    clientStub.reportError(e.getMessage());
                } catch (RemoteException re) {
                    System.err.println("[SERVER] Impossibile notificare l'errore al client RMI.");
                }
            }
        });
    }

    /**
     * No-op remote method clients ping to verify the server is still alive.
     *
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void ping() throws RemoteException {
        // no-op: solo per verificare che il server sia vivo
    }
}