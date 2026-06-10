package it.polimi.ingsw.am56.view.TUI;

import it.polimi.ingsw.am56.network.message.classesDTO.BoardDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.PlayerStatsDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.TotemPositionDTO;
import it.polimi.ingsw.am56.network.message.update.GameEndedMessage;

import java.util.ArrayList;
import java.util.List;

public class TUIDrawer {

    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String DIM = "\033[2m";
    private static final String RED = "\033[31m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String BLUE = "\033[34m";
    private static final String MAGENTA = "\033[35m";
    private static final String CYAN = "\033[36m";
    private static final String WHITE = "\033[37m";
    private static final String GRAY = "\033[90m";

    private static final char H = '─';
    private static final char V = '│';
    private static final char TL = '┌';
    private static final char TR = '┐';
    private static final char BL = '└';
    private static final char BR = '┘';

    private static final int WIDTH = 80;

    public void clearScreen() {
        System.out.print("\033[H\033[2J\033[3J");
        System.out.flush();
    }

    public void printTitle() {
        System.out.println();
        System.out.println(CYAN + BOLD + center("╔══════════════════════════════════════╗", WIDTH) + RESET);
        System.out.println(CYAN + BOLD + center("║          M  E  S  O  S               ║", WIDTH) + RESET);
        System.out.println(CYAN + BOLD + center("╚══════════════════════════════════════╝", WIDTH) + RESET);
        System.out.println();
    }

    public void printMessage(String message) {
        System.out.println(message);
    }

    public void printError(String error) {
        System.out.println(RED + BOLD + " ✖ ERRORE: " + RESET + RED + error + RESET);
    }

    public void printSuccess(String message) {
        System.out.println(GREEN + BOLD + " ✔ " + RESET + GREEN + message + RESET);
    }

    public void printLobbyState(List<String> playerNames, int maxPlayers) {
        openBox("LOBBY  ─  In attesa giocatori (" + playerNames.size() + "/" + maxPlayers + ")", CYAN);
        for (int i = 0; i < playerNames.size(); i++) {
            boxRow(" " + (i + 1) + ". " + BOLD + playerNames.get(i) + RESET);
        }
        for (int i = playerNames.size(); i < maxPlayers; i++) {
            boxRow(GRAY + " " + (i + 1) + ". (slot libero)" + RESET);
        }
        closeBox();
        System.out.println();
        System.out.println(DIM + "  In attesa che la partita inizi…" + RESET);
        System.out.println();
    }

    // ------------------------------------------------------------------
    // In-game rendering
    // ------------------------------------------------------------------

    public void printGameHeader(int era, int round, String currentPlayerNick,
                                String currentPlayerColor, String phaseLabel) {
        String c = ansiFor(currentPlayerColor);

        System.out.println();
        System.out.println(CYAN + BOLD + "╔" + repeat('═', WIDTH - 2) + "╗" + RESET);

        String line1 = " MESOS   ▸   Era " + BOLD + era + RESET + CYAN
                + "   ▸   Round " + BOLD + round + RESET + CYAN
                + "   ▸   " + BOLD + phaseLabel + RESET;
        System.out.println(CYAN + "║" + padRight(line1, WIDTH - 2, CYAN) + CYAN + "║" + RESET);

        String line2 = " Turno di: " + c + BOLD + (currentPlayerNick == null ? "?" : currentPlayerNick) + RESET;
        System.out.println(CYAN + "║" + padRight(line2, WIDTH - 2, CYAN) + CYAN + "║" + RESET);

        System.out.println(CYAN + BOLD + "╚" + repeat('═', WIDTH - 2) + "╝" + RESET);
        System.out.println();
    }

    public void printBoard(BoardDTO board, List<TotemPositionDTO> totems) {
        openBox("PLANCIA DI GIOCO", YELLOW);

        boxRow(BOLD + " Edifici riga alta:  " + RESET + formatIdList(board.upperBuildingsIds()));
        boxRow(BOLD + " Edifici riga bassa: " + RESET + formatIdList(board.lowerBuildingsIds()));
        boxSep();
        boxRow(BOLD + " Tribù/Eventi alti:  " + RESET + formatCharEvList(board.upperCharEvIds(), board.eventCardIds()));
        boxRow(BOLD + " Tribù/Eventi bassi: " + RESET + formatCharEvList(board.lowerCharEvIds(), board.eventCardIds()));
        boxSep();
        boxRow(BOLD + " Tessere Offerta:    " + RESET + formatOfferTiles(board.offerTilesLetters(), totems));
        boxRow(BOLD + " Tessere Ordine:     " + RESET + formatOrderTiles(board.orderTileCount(), totems));

        closeBox();
        System.out.println();
    }

    public void printPlayerDashboard(PlayerStatsDTO stats, boolean isLocal, boolean isCurrent) {
        String color = ansiFor(stats.color());
        String star = isCurrent ? (YELLOW + "★ " + RESET) : "  ";
        String me = isLocal ? (GREEN + "  (tu)" + RESET) : "";
        String title = star + color + BOLD + stats.nickname().toUpperCase() + " [" + stats.color() + "]" + RESET + me;

        openBox(title, color);

        boxRow("  Cibo: " + BOLD + pad(stats.food(), 3) + RESET
                + "  Punti: " + BOLD + pad(stats.points(), 3) + RESET
                + "  Invenzioni: " + BOLD + pad(stats.inventionCount(), 2) + RESET
                + "  Stelle Sciamano: " + BOLD + pad(stats.shamanStars(), 2) + RESET);
        boxRow("  Sconto cibo: " + BOLD + pad(stats.feedingSale(), 2) + RESET
                + "  Sconto edifici: " + BOLD + pad(stats.buildingSale(), 2) + RESET);

        boxSep();

        boxRow("  " + GREEN  + "Raccoglitori: " + RESET + formatOrDash(stats.collectorCards()));
        boxRow("  " + RED    + "Cacciatori:   " + RESET + formatOrDash(stats.hunterCards()));
        boxRow("  " + YELLOW + "Costruttori:  " + RESET + formatOrDash(stats.builderCards()));
        boxRow("  " + BLUE   + "Sciamani:     " + RESET + formatOrDash(stats.shamanCards()));
        boxRow("  " + MAGENTA+ "Artisti:      " + RESET + formatOrDash(stats.artistCards()));
        boxRow("  " + CYAN   + "Inventori:    " + RESET + formatOrDash(stats.inventorCards()));
        boxRow("  " + WHITE  + "Edifici:      " + RESET + formatOrDash(stats.ownedBuildingCards()));

        if (isLocal) {
            boxSep();
            boxRow("  " + DIM + "Puoi pescare riga alta: " + RESET + boolBadge(stats.canPickUpper())
                    + "   " + DIM + "riga bassa: " + RESET + boolBadge(stats.canPickLower()));
            if (stats.affordableBuildingIds() != null && !stats.affordableBuildingIds().isEmpty()) {
                boxRow("  " + DIM + "Edifici acquistabili: " + RESET + formatIdList(stats.affordableBuildingIds()));
            }
        }

        closeBox();
        System.out.println();
    }

    public void printEventNotice(int eventCardId) {
        System.out.println(MAGENTA + BOLD + "  ▸ Evento risolto: carta #" + eventCardId + RESET);
    }

    public void printEndGame(GameEndedMessage msg, String localNick) {
        System.out.println();
        System.out.println(CYAN + BOLD + "╔" + repeat('═', WIDTH - 2) + "╗" + RESET);
        System.out.println(CYAN + BOLD + "║" + center("PARTITA TERMINATA", WIDTH - 2) + "║" + RESET);
        System.out.println(CYAN + BOLD + "╚" + repeat('═', WIDTH - 2) + "╝" + RESET);
        System.out.println();

        openBox("CLASSIFICA DELLA PARTITA", YELLOW);
        if (msg.matchRanking() != null) {
            for (EndGameDTO e : msg.matchRanking()) {
                String highlight = e.nickname().equals(localNick) ? (GREEN + "  ← TU" + RESET) : "";
                String crown = e.isWinner() ? (YELLOW + " 👑" + RESET) : "";
                boxRow(String.format("  %d. %-18s  punti: %s%3d%s   cibo: %d%s%s",
                        e.rank(),
                        e.nickname(),
                        BOLD, e.score(), RESET,
                        e.food(),
                        crown,
                        highlight));
            }
        }
        closeBox();
        System.out.println();

        openBox("CLASSIFICA GLOBALE  (game size " + msg.gameSize() + ")", CYAN);
        if (msg.globalLeaderboard() == null || msg.globalLeaderboard().isEmpty()) {
            boxRow("  " + DIM + "(non disponibile — DB leaderboard non raggiungibile)" + RESET);
        } else {
            for (EndGameDTO e : msg.globalLeaderboard()) {
                String highlight = e.nickname().equals(localNick) ? (GREEN + "  ← TU" + RESET) : "";
                boxRow(String.format("  %d. %-18s  punti: %s%3d%s   data: %s%s",
                        e.rank(),
                        e.nickname(),
                        BOLD, e.score(), RESET,
                        e.date(),
                        highlight));
            }
        }
        closeBox();
        System.out.println();
    }

    public void printAborted() {
        System.out.println();
        System.out.println(RED + BOLD + " ✖ Partita interrotta: un giocatore si è disconnesso." + RESET);
        System.out.println();
    }

    public void printReconnecting() {
        System.out.println(YELLOW + BOLD + " ⟳ Connessione persa, attendo riconnessione…" + RESET);
    }

    public void printReconnected() {
        System.out.println(GREEN + BOLD + " ✔ Riconnesso al server." + RESET);
    }

    public void printPrompt(String prompt) {
        System.out.println();
        System.out.println(YELLOW + BOLD + " ▶ " + RESET + BOLD + prompt + RESET);
    }

    // ------------------------------------------------------------------
    // Formatting helpers
    // ------------------------------------------------------------------

    private String formatIdList(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return DIM + "(vuoto)" + RESET;
        StringBuilder sb = new StringBuilder();
        for (Integer id : ids) {
            sb.append("[").append(String.format("%2d", id)).append("] ");
        }
        return sb.toString().trim();
    }

    private String formatOrDash(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return DIM + "—" + RESET;
        return formatIdList(ids);
    }

    private String formatCharEvList(List<Integer> ids, List<Integer> eventIds) {
        if (ids == null || ids.isEmpty()) return DIM + "(vuoto)" + RESET;
        StringBuilder sb = new StringBuilder();
        for (Integer id : ids) {
            boolean isEvent = eventIds != null && eventIds.contains(id);
            if (isEvent) {
                sb.append(MAGENTA).append(BOLD).append("[E:")
                        .append(String.format("%2d", id)).append("]").append(RESET).append(" ");
            } else {
                sb.append("[").append(String.format("%2d", id)).append("] ");
            }
        }
        return sb.toString().trim();
    }

    private String formatOfferTiles(List<String> letters, List<TotemPositionDTO> totems) {
        if (letters == null || letters.isEmpty()) return DIM + "(nessuna)" + RESET;
        StringBuilder sb = new StringBuilder();
        for (String letter : letters) {
            String occupant = totemColorAtOffer(letter, totems);
            if (occupant == null) {
                sb.append("[").append(letter).append("]      ");
            } else {
                String c = ansiFor(occupant);
                sb.append(c).append(BOLD).append("[").append(letter).append(":")
                        .append(shortColor(occupant)).append("]").append(RESET).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private String formatOrderTiles(int orderTileCount, List<TotemPositionDTO> totems) {
        if (orderTileCount <= 0) return DIM + "(vuote)" + RESET;
        List<TotemPositionDTO> orderTotems = new ArrayList<>();
        if (totems != null) {
            for (TotemPositionDTO t : totems) {
                if ("ORDER".equals(t.locationType())) orderTotems.add(t);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(BOLD).append(orderTileCount).append(RESET).append(" disponibili");
        if (!orderTotems.isEmpty()) {
            sb.append("   coda: ");
            for (TotemPositionDTO t : orderTotems) {
                String c = ansiFor(t.totemColor());
                sb.append(c).append(BOLD).append("[#").append(t.locationId()).append(":")
                        .append(shortColor(t.totemColor())).append("]").append(RESET).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private String totemColorAtOffer(String letter, List<TotemPositionDTO> totems) {
        if (totems == null) return null;
        for (TotemPositionDTO t : totems) {
            if ("OFFER".equals(t.locationType()) && letter.equalsIgnoreCase(t.locationId())) {
                return t.totemColor();
            }
        }
        return null;
    }

    private String shortColor(String color) {
        if (color == null) return "?";
        String up = color.toUpperCase();
        return switch (up) {
            case "RED" -> "ROS";
            case "GREEN" -> "VRD";
            case "YELLOW" -> "GIA";
            case "BLUE" -> "BLU";
            case "PURPLE", "MAGENTA" -> "VIO";
            default -> up.substring(0, Math.min(3, up.length()));
        };
    }

    private String boolBadge(boolean v) {
        return v ? (GREEN + "✓" + RESET) : (RED + "✗" + RESET);
    }

    private String ansiFor(String color) {
        if (color == null) return WHITE;
        return switch (color.toUpperCase()) {
            case "RED" -> RED;
            case "GREEN" -> GREEN;
            case "YELLOW" -> YELLOW;
            case "BLUE" -> BLUE;
            case "PURPLE", "MAGENTA" -> MAGENTA;
            case "CYAN" -> CYAN;
            default -> WHITE;
        };
    }

    // ------------------------------------------------------------------
    // Box-drawing helpers
    // ------------------------------------------------------------------

    private void openBox(String title, String color) {
        String stripped = stripAnsi(title);
        int titleLen = stripped.length();
        int dashes = WIDTH - 4 - titleLen;
        if (dashes < 0) dashes = 0;
        System.out.println(color + TL + repeat(H, 2) + " " + RESET + BOLD + title + RESET
                + " " + color + repeat(H, dashes) + TR + RESET);
    }

    private void boxRow(String content) {
        String stripped = stripAnsi(content);
        int pad = WIDTH - 2 - stripped.length();
        if (pad < 0) pad = 0;
        System.out.println(" " + V + " " + content + repeat(' ', pad) + V);
    }

    private void boxSep() {
        System.out.println(" " + V + repeat(H, WIDTH - 2) + V);
    }

    private void closeBox() {
        System.out.println(" " + BL + repeat(H, WIDTH - 2) + BR);
    }

    private String repeat(char c, int n) {
        if (n <= 0) return "";
        return String.valueOf(c).repeat(n);
    }

    private String repeat(String s, int n) {
        if (n <= 0) return "";
        return s.repeat(n);
    }

    private String center(String s, int width) {
        int len = stripAnsi(s).length();
        if (len >= width) return s;
        int left = (width - len) / 2;
        int right = width - len - left;
        return " ".repeat(left) + s + " ".repeat(right);
    }

    private String padRight(String s, int width, String trailingColorContext) {
        int len = stripAnsi(s).length();
        int pad = width - len;
        if (pad <= 0) return s;
        return s + RESET + " ".repeat(pad) + trailingColorContext;
    }

    private String pad(int value, int width) {
        return String.format("%" + width + "d", value);
    }

    private String stripAnsi(String s) {
        return s.replaceAll("\\[[;\\d]*[A-Za-z]", "");
    }
}
