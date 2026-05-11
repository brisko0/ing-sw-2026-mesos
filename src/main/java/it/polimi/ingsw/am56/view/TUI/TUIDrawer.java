package it.polimi.ingsw.am56.view.TUI;

import it.polimi.ingsw.am56.network.message.classesDTO.BoardDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.PlayerStatsDTO;

import java.util.List;

public class TUIDrawer {

    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String RED = "\033[31m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String BLUE = "\033[34m";
    private static final String CYAN = "\033[36m";

    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void printTitle() {
        System.out.println(CYAN + BOLD + "========================================" + RESET);
        System.out.println(CYAN + BOLD + "              M E S O S                 " + RESET);
        System.out.println(CYAN + BOLD + "========================================" + RESET);
        System.out.println();
    }

    public void printMessage(String message) {
        System.out.println(message);
    }

    public void printError(String error) {
        System.out.println(RED + "[ERRORE] " + error + RESET);
    }

    public void printSuccess(String message) {
        System.out.println(GREEN + "[OK] " + message + RESET);
    }

    public void printLobbyState(List<String> playerNames, int maxPlayers) {
        System.out.println(BOLD + "--- LOBBY D'ATTESA ---" + RESET);
        System.out.println("Giocatori connessi: " + playerNames.size() + "/" + maxPlayers);
        for (int i = 0; i < playerNames.size(); i++) {
            System.out.println(" " + (i + 1) + ". " + playerNames.get(i));
        }
        System.out.println("In attesa degli altri giocatori...\n");
    }

    public void printBoard(BoardDTO board) {
        System.out.println(YELLOW + BOLD + "\n--- PLANCIA DI GIOCO ---" + RESET);
        System.out.println("Carte Edifici (Alto): " + board.upperBuildingsIds());
        System.out.println("Carte Edifici (Basso): " + board.lowerBuildingsIds());
        System.out.println("Carte Tribù/Eventi (Alto): " + board.upperCharEvIds());
        System.out.println("Carte Tribù/Eventi (Basso): " + board.lowerCharEvIds());
        System.out.println("Tessere Ordine: " + board.orderTileCount());
        System.out.println("Tessere Offerta: " + board.offerTilesLetters());
        System.out.println();
    }

    public void printPlayerStats(PlayerStatsDTO stats) {
        System.out.println(BLUE + BOLD + "\n--- STATISTICHE: " + stats.nickname().toUpperCase() + " (" + stats.color() + ") ---" + RESET);
        System.out.println("Punti: " + stats.points() + " | Cibo: " + stats.food());
        System.out.println("Invenzioni: " + stats.inventionCount());
        System.out.println();
    }
}