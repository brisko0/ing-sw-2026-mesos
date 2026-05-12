package it.polimi.ingsw.am56.view.TUI.controllers;

import it.polimi.ingsw.am56.view.TUI.*;

public class NetworkSetupController extends BaseTUIController {

    public NetworkSetupController(TUIContext context) {
        super(context);
    }

    @Override
    public void onEnter() {
        drawer.clearScreen();
        drawer.printTitle();

        int choice = inputReader.readIntWithRange("Scegli la connessione:\n1. RMI\n2. Socket\nScelta:", 1, 2);
        String ip = inputReader.readString("Inserisci IP Server (invio per localhost):");

        if (ip.isEmpty()) {
            ip = "127.0.0.1";
        }

        try {
            if (choice == 1) {
                int port = readPort("RMI", 1099);
                context.setServerAdapter(context.getFactory().createRmiConnection(ip, port));
            } else {
                int port = readPort("Socket", 12345);
                context.setServerAdapter(context.getFactory().createSocketConnection(ip, port));
            }

            drawer.printSuccess("Connessione stabilita con successo!");

        } catch (Exception e) {
            drawer.printError("Impossibile connettersi: " + e.getMessage());
            drawer.printMessage("Riprovo...");

            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {}
            onEnter();
        }
    }

    private int readPort(String tipo, int defaultPort) {
        String input = inputReader.readString("Inserisci la porta " + tipo + " (invio per default " + defaultPort + "):");
        if (input.isEmpty()) {
            return defaultPort;
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            drawer.printError("Porta non valida, uso il default " + defaultPort);
            return defaultPort;
        }
    }
}