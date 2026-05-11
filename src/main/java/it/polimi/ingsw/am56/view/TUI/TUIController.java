package it.polimi.ingsw.am56.view.TUI;

import it.polimi.ingsw.am56.core.ConnectionFactory;
import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.network.message.classesDTO.*;
import it.polimi.ingsw.am56.network.message.request.CardPickRequest;
import it.polimi.ingsw.am56.network.message.request.TotemMoveRequest;
import it.polimi.ingsw.am56.network.message.update.*;

import java.util.List;

public class TUIController implements UpdateHandler, MessageVisitor {

    private final ConnectionFactory factory;
    private ServerAdapter serverAdapter;
    private final TUIDrawer drawer;
    private final TUIInputReader inputReader;

    private String localNickname;
    private BoardDTO currentBoard;
    private PlayerStatsDTO localStats;
    private String currentPlayerNickname;

    public TUIController(ConnectionFactory factory) {
        this.factory = factory;
        this.drawer = new TUIDrawer();
        this.inputReader = new TUIInputReader(drawer);
    }

    public void start() {
        drawer.clearScreen();
        drawer.printTitle();

        int choice = inputReader.readIntWithRange("Scegli la connessione:\n1. RMI\n2. Socket\nScelta:", 1, 2);
        String ip = inputReader.readString("Inserisci IP Server (invio per localhost):");

        if (ip.isEmpty()) {
            ip = "127.0.0.1";
        }

        try {
            if (choice == 1) {
                serverAdapter = factory.createRmiConnection(ip, 1099);
            } else {
                serverAdapter = factory.createSocketConnection(ip, 12345);
            }

            serverAdapter.setUpdateHandler(this);
            drawer.printSuccess("Connessione stabilita con successo!");

            this.localNickname = inputReader.readString("Inserisci il tuo Nickname:");
            drawer.printMessage("Attesa inizio partita...");

            startInputLoop();

        } catch (Exception e) {
            drawer.printError("Impossibile connettersi: " + e.getMessage());
            inputReader.close();
        }
    }

    @Override
    public void handleUpdate(ServerMessage message) {
        drawer.clearScreen();
        drawer.printTitle();
        message.accept(this);
    }

    @Override
    public void visit(GameSnapshotMessage message) {
        GameSnapshotDTO snap = message.snapshot();
        this.currentBoard = snap.board();
        this.currentPlayerNickname = snap.currentPlayer().nickname();
        updateLocalStats(snap.playersStats());

        drawer.printBoard(this.currentBoard);
        if (this.localStats != null) {
            drawer.printPlayerStats(this.localStats);
        }
        printTurnInfo();
    }

    @Override
    public void visit(BoardUpdatedMessage message) {
        this.currentBoard = message.board();
        drawer.printBoard(this.currentBoard);
        if (this.localStats != null) {
            drawer.printPlayerStats(this.localStats);
        }
        printTurnInfo();
    }

    @Override
    public void visit(CurrentPlayerUpdateMessage message) {
        this.currentPlayerNickname = message.currentPlayer().nickname();
        if (this.currentBoard != null) {
            drawer.printBoard(this.currentBoard);
        }
        if (this.localStats != null) {
            drawer.printPlayerStats(this.localStats);
        }
        printTurnInfo();
    }

    @Override
    public void visit(PlayerStatsUpdateMessage message) {
        updateSingleStat(message.stats());
        if (this.currentBoard != null) {
            drawer.printBoard(this.currentBoard);
        }
        if (this.localStats != null) {
            drawer.printPlayerStats(this.localStats);
        }
        printTurnInfo();
    }

    @Override
    public void visit(LobbyStateMessage message) {
        List<String> names = message.players().stream().map(PlayerDTO::nickname).toList();
        drawer.printLobbyState(names, message.gameSize());
    }

    @Override
    public void visit(TemporaryDefaultMessage message) {
        drawer.printMessage("Messaggio di default ricevuto.");
    }

    private void updateLocalStats(List<PlayerStatsDTO> allStats) {
        for (PlayerStatsDTO stat : allStats) {
            if (stat.nickname().equals(this.localNickname)) {
                this.localStats = stat;
                break;
            }
        }
    }

    private void updateSingleStat(PlayerStatsDTO newStat) {
        if (newStat.nickname().equals(this.localNickname)) {
            this.localStats = newStat;
        }
    }

    private void printTurnInfo() {
        if (currentPlayerNickname != null) {
            if (currentPlayerNickname.equals(localNickname)) {
                drawer.printSuccess("È IL TUO TURNO!");
            } else {
                drawer.printMessage("È il turno di: " + currentPlayerNickname.toUpperCase());
            }
        }
        System.out.println("Digita un comando ('help' per la lista): ");
    }

    private void startInputLoop() {
        while (true) {
            String input = inputReader.readString("");

            if (input.equalsIgnoreCase("exit")) {
                drawer.printMessage("Uscita dal gioco...");
                inputReader.close();
                System.exit(0);
            }

            if (input.equalsIgnoreCase("help")) {
                printHelpMenu();
                continue;
            }

            if (input.startsWith("pesca ")) {
                try {
                    int cardId = Integer.parseInt(input.split(" ")[1]);
                    serverAdapter.sendRequest(new CardPickRequest(localNickname, cardId));
                } catch (Exception e) {
                    drawer.printError("Formato non valido. Usa: pesca <id_carta>");
                }
            } else if (input.startsWith("totem ")) {
                try {
                    String letter = input.split(" ")[1];
                    serverAdapter.sendRequest(new TotemMoveRequest(localNickname, letter));
                } catch (Exception e) {
                    drawer.printError("Formato non valido. Usa: totem <lettera_offerta>");
                }
            } else {
                drawer.printError("Comando sconosciuto.");
            }
        }
    }

    private void printHelpMenu() {
        System.out.println("\n--- COMANDI DISPONIBILI ---");
        System.out.println("pesca <id>   - Pesca una carta dalla plancia");
        System.out.println("totem <lett> - Posiziona il totem sulla tessera offerta");
        System.out.println("exit         - Chiudi il gioco");
        System.out.println("---------------------------\n");
    }
}