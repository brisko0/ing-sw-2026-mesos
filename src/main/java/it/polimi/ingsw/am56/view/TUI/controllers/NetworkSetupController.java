package it.polimi.ingsw.am56.view.TUI.controllers;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.network.ConnectionManager;
import it.polimi.ingsw.am56.view.TUI.*;

public class NetworkSetupController extends BaseTUIController {

    public NetworkSetupController(TUIContext context) {
        super(context);
    }

    @Override
    public void onEnter() {
        drawer.printMenuHeader("CONNECT TO SERVER");

        drawer.printMessage("  Choose connection mode:");
        // Color the two options to match what they represent (RMI = teal, Socket = clay).
        drawer.printMessage("    \033[38;5;73m1) RMI\033[0m       — Java Remote Method Invocation");
        drawer.printMessage("    \033[38;5;130m2) Socket\033[0m    — Raw TCP socket");
        int choice = inputReader.readIntWithRange("  Selection:", 1, 2);

        String ip = inputReader.readString("  Server IP (press Enter for localhost):");
        if (ip.isEmpty()) ip = "127.0.0.1";

        try {
            if (choice == 1) {
                int port = readPort("RMI", 1099);
                ServerAdapter raw = context.getFactory().createRmiConnection(ip, port);
                context.setServerAdapter(new ConnectionManager(ip, port, true, context.getFactory(), raw));
            } else {
                int port = readPort("Socket", 12345);
                ServerAdapter raw = context.getFactory().createSocketConnection(ip, port);
                context.setServerAdapter(new ConnectionManager(ip, port, false, context.getFactory(), raw));
            }

            drawer.printSuccess("Connected successfully!");
            context.changeState(new PreLobbyController(context));

        } catch (Exception e) {
            drawer.printError("Cannot connect: " + e.getMessage());
            drawer.printMessage("  Retrying...");

            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {}
            onEnter();
        }
    }

    private int readPort(String label, int defaultPort) {
        String input = inputReader.readString("  " + label + " port (press Enter for default " + defaultPort + "):");
        if (input.isEmpty()) return defaultPort;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            drawer.printError("Invalid port, using default " + defaultPort);
            return defaultPort;
        }
    }
}
