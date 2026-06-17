package it.polimi.ingsw.am56.view.TUI;

import it.polimi.ingsw.am56.network.message.classesDTO.BoardDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.PlayerStatsDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.TotemPositionDTO;
import it.polimi.ingsw.am56.network.message.update.GameEndedMessage;
import it.polimi.ingsw.am56.view.TUI.CardCatalog.ClassKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Terminal renderer for Mesos. Uses 256-color ANSI (\033[38;5;Nm) + Unicode
 * box-drawing/icons. Targets Windows Terminal, PowerShell 7+, iTerm2, modern
 * Linux. Legacy cmd.exe is not supported.
 *
 * <p>Adjacent-SGR rule: some Windows consoles drop the first of two adjacent
 * escape sequences ({@code "\033[31m\033[1m"} can render as {@code "[31m"}).
 * The convention here is one of:
 *   <ol>
 *     <li>Combine attributes into a single SGR ({@code "\033[1;31m"}).</li>
 *     <li>Wrap a single visual chunk in one {@link #ansi(String, String)} call;
 *         insert at least one printable character between two such calls.</li>
 *   </ol>
 * Width math uses {@link #stripAnsi(String)} which only counts visible chars.
 */
public class TUIDrawer {

    // ------------------------------------------------------------------
    // SGR constants
    // ------------------------------------------------------------------

    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String DIM = "\033[2m";
    private static final String ITALIC = "\033[3m";

    // Combined color+bold SGRs — single sequence (safer).
    private static final String BOLD_RED = "\033[1;31m";
    private static final String BOLD_GREEN = "\033[1;32m";
    private static final String BOLD_YELLOW = "\033[1;33m";
    private static final String BOLD_BLUE = "\033[1;34m";
    private static final String BOLD_MAGENTA = "\033[1;35m";
    private static final String BOLD_CYAN = "\033[1;36m";
    private static final String BOLD_WHITE = "\033[1;37m";
    private static final String BOLD_BLACK = "\033[1;30m";

    // 256-color palette. Every constant is a single SGR escape so callers can
    // pass it directly to {@link #ansi(String, String)} without producing two
    // adjacent SGR sequences in the byte stream.
    private static final String PARCHMENT = "\033[38;5;180m";
    private static final String PARCHMENT_DIM = "\033[38;5;137m";
    private static final String CLAY = "\033[38;5;130m";
    private static final String GOLD = "\033[38;5;220m";
    private static final String INK = "\033[38;5;94m";
    private static final String STONE = "\033[38;5;244m";
    private static final String SILVER = "\033[38;5;250m";

    // Pre-combined attribute+color SGRs (bold/dim/italic + foreground in one escape).
    private static final String B_PARCHMENT = "\033[1;38;5;180m";
    private static final String B_GOLD = "\033[1;38;5;220m";
    private static final String B_CLAY = "\033[1;38;5;130m";
    private static final String B_SILVER = "\033[1;38;5;250m";
    private static final String B_EVENT = "\033[1;38;5;207m";
    private static final String D_INK = "\033[2;38;5;94m";
    private static final String D_STONE = "\033[2;38;5;244m";
    private static final String I_INK = "\033[3;38;5;94m";

    // Per-class colors.
    private static final String COL_COLLECTOR = "\033[38;5;108m";
    private static final String COL_HUNTER = "\033[38;5;167m";
    private static final String COL_BUILDER = "\033[38;5;173m";
    private static final String COL_SHAMAN = "\033[38;5;105m";
    private static final String COL_ARTIST = "\033[38;5;170m";
    private static final String COL_INVENTOR = "\033[38;5;73m";
    private static final String COL_BUILDING = "\033[38;5;220m";
    private static final String COL_EVENT = "\033[38;5;207m";

    // Box-drawing.
    private static final char H = '─';
    private static final char V = '│';
    private static final char TL = '┌';
    private static final char TR = '┐';
    private static final char BL = '└';
    private static final char BR = '┘';

    private static final int WIDTH = 80;

    /** Mini-card width (must match the literal strings in {@link #renderMiniCard}). */
    private static final int CARD_W = 7;

    private final CardCatalog catalog;

    public TUIDrawer() { this(new CardCatalog()); }

    public TUIDrawer(CardCatalog catalog) { this.catalog = catalog; }

    // ------------------------------------------------------------------
    // Top-level primitives
    // ------------------------------------------------------------------

    public void clearScreen() {
        System.out.print("\033[H\033[2J\033[3J");
        System.out.flush();
    }

    public void printTitle() {
        // 47×6 block-letter MESOS, top 3 rows parchment, bottom 3 rows clay.
        String[] block = {
                " ███╗   ███╗ ███████╗ ███████╗  ██████╗  ███████╗",
                " ████╗ ████║ ██╔════╝ ██╔════╝ ██╔═══██╗ ██╔════╝",
                " ██╔████╔██║ █████╗   ███████╗ ██║   ██║ ███████╗",
                " ██║╚██╔╝██║ ██╔══╝   ╚════██║ ██║   ██║ ╚════██║",
                " ██║ ╚═╝ ██║ ███████╗ ███████║ ╚██████╔╝ ███████║",
                " ╚═╝     ╚═╝ ╚══════╝ ╚══════╝  ╚═════╝  ╚══════╝"
        };
        System.out.println();
        for (int i = 0; i < block.length; i++) {
            String shade = (i < 3) ? PARCHMENT : CLAY;
            System.out.println(center(ansi(block[i], shade), WIDTH));
        }
        System.out.println(center(ansi("Prehistoric Card Game", I_INK), WIDTH));
        System.out.println();
        System.out.println(center(ansi(repeat('─', 47), PARCHMENT_DIM), WIDTH));
        System.out.println();
    }

    public void printMessage(String message) {
        System.out.println(message);
    }

    public void printError(String error) {
        System.out.println(" " + ansi("✖ ERROR:", BOLD_RED) + "  " + ansi(error, "\033[31m"));
    }

    public void printSuccess(String message) {
        System.out.println(" " + ansi("✔", BOLD_GREEN) + "  " + ansi(message, "\033[32m"));
    }

    public void printPrompt(String prompt) {
        System.out.println();
        System.out.println(" " + ansi("▶", GOLD) + "  " + ansi(prompt, B_PARCHMENT));
    }

    public void printSubHeader(String text) {
        System.out.println();
        System.out.println(" " + ansi(repeat('─', 4) + " " + text + " " + repeat('─', WIDTH - 7 - text.length()),
                PARCHMENT_DIM));
    }

    public void printMenuHeader(String title) {
        openBox(title, PARCHMENT);
        closeBox();
    }

    // ------------------------------------------------------------------
    // Lobby
    // ------------------------------------------------------------------

    public void printLobbyState(List<String> playerNames, List<String> playerColors, int maxPlayers) {
        String header = "LOBBY  " + ansi("▸", PARCHMENT_DIM) + " " + "Waiting (" + playerNames.size()
                + "/" + maxPlayers + ")";
        openBox(header, PARCHMENT);
        for (int i = 0; i < playerNames.size(); i++) {
            String color = (playerColors != null && i < playerColors.size()) ? playerColors.get(i) : null;
            String tag = (i == 0) ? "   " + ansi("(creator)", D_INK) : "";
            String dot = (color == null) ? " " : ansi("●", colorSgrFor(color));
            boxRow(" " + dot + "  " + (i + 1) + ". " + ansi(playerNames.get(i), BOLD) + tag);
        }
        for (int i = playerNames.size(); i < maxPlayers; i++) {
            boxRow(" " + ansi("○", STONE) + "  " + (i + 1) + ". " + ansi("— waiting —", D_STONE));
        }
        closeBox();
        System.out.println();
        System.out.println("  " + ansi("Waiting for the game to start...", D_INK));
        System.out.println();
    }

    // ------------------------------------------------------------------
    // In-game header
    // ------------------------------------------------------------------

    public void printGameHeader(int era, int round, String currentPlayerNick,
                                String currentPlayerColor, String phaseLabel) {
        System.out.println();
        System.out.println(ansi("╔" + repeat('═', WIDTH - 2) + "╗", PARCHMENT));

        String line1 = " " + ansi("M·E·S·O·S", B_GOLD)
                + "   " + ansi("▸", PARCHMENT_DIM) + "  Era  " + ansi(String.valueOf(era), BOLD)
                + "   " + ansi("▸", PARCHMENT_DIM) + "  Round  " + ansi(String.valueOf(round), BOLD)
                + "   " + ansi("▸", PARCHMENT_DIM) + "  Phase:  " + ansi(phaseLabel, B_PARCHMENT);
        System.out.println(ansi("║", PARCHMENT) + " " + padRight(line1, WIDTH - 3) + ansi("║", PARCHMENT));

        String nick = currentPlayerNick == null ? "?" : currentPlayerNick.toUpperCase();
        String colorTag = currentPlayerColor == null ? "?" : currentPlayerColor;
        String line2 = " Turn:   " + ansi("◤", GOLD) + " "
                + ansi(nick + " [" + colorTag + "]", colorSgrFor(currentPlayerColor))
                + " " + ansi("◢", GOLD);
        System.out.println(ansi("║", PARCHMENT) + " " + padRight(line2, WIDTH - 3) + ansi("║", PARCHMENT));

        System.out.println(ansi("╚" + repeat('═', WIDTH - 2) + "╝", PARCHMENT));
        System.out.println();
    }

    // ------------------------------------------------------------------
    // Board
    // ------------------------------------------------------------------

    public void printBoard(BoardDTO board, List<TotemPositionDTO> totems) {
        List<Integer> eventIds = board.eventCardIds() != null ? board.eventCardIds() : List.of();

        printCardRow("Buildings (top)",    board.upperBuildingsIds(), eventIds);
        printCardRow("Tribe (top)",        board.upperCharEvIds(),    eventIds);

        System.out.println();
        System.out.println("  " + ansi("Offer:", B_PARCHMENT) + "  "
                + formatOfferTiles(board.offerTilesLetters(), totems));
        System.out.println("  " + ansi("Order:", B_PARCHMENT) + "  "
                + formatOrderTiles(board.orderTileCount(), totems));
        System.out.println();

        printCardRow("Tribe (bottom)",     board.lowerCharEvIds(),    eventIds);
        printCardRow("Buildings (bottom)", board.lowerBuildingsIds(), eventIds);
        System.out.println();
    }

    private void printCardRow(String label, List<Integer> ids, List<Integer> eventIds) {
        // Label takes the same 20-char column for every row so the cards line up vertically.
        String labelChunk = "  " + ansi(padRightPlain(label, 20), B_PARCHMENT);

        if (ids == null || ids.isEmpty()) {
            System.out.println(labelChunk + ansi("(empty)", D_STONE));
            return;
        }

        String emptyLabelChunk = "  " + repeat(' ', 20);

        StringBuilder top = new StringBuilder();
        StringBuilder mid = new StringBuilder();
        StringBuilder bot = new StringBuilder();
        boolean first = true;
        for (Integer id : ids) {
            if (!first) {
                top.append("  ");
                mid.append("  ");
                bot.append("  ");
            }
            boolean isEvent = eventIds != null && eventIds.contains(id);
            String[] card = renderMiniCard(id, isEvent);
            top.append(card[0]);
            mid.append(card[1]);
            bot.append(card[2]);
            first = false;
        }

        System.out.println(labelChunk + top);
        System.out.println(emptyLabelChunk + mid);
        System.out.println(emptyLabelChunk + bot);
    }

    /**
     * 3-line, 7-char-wide mini card. Each line is a single SGR-wrapped chunk —
     * no adjacent SGR sequences within a card. The era stripe shares the class
     * tint (no DIM nested SGR) to keep this guarantee.
     */
    private String[] renderMiniCard(int id, boolean isEvent) {
        ClassKind kind = isEvent ? ClassKind.EVENT : catalog.kindOf(id);
        int era = catalog.eraOf(id);
        String tint = colorFor(kind);
        String midSgr = isEvent ? B_EVENT : tint;   // events get bold; one SGR per line
        String icon = iconFor(kind);

        // Mid interior: icon (1 visible char) + " " + id (3-char right-aligned) = 5 chars.
        String idStr;
        if (id < 0 || id > 999) idStr = "???";
        else idStr = String.format("%3d", id);

        String eraStr = (era >= 1 && era <= 9) ? ("E" + era) : "E?";

        // Each line is one SGR escape + 7-char content + RESET — stripAnsi gives back 7.
        String top = tint   + "┌─────┐" + RESET;
        String mid = midSgr + "│" + icon + " " + idStr + "│" + RESET;
        String bot = tint   + "└──" + eraStr + "─┘" + RESET;

        return new String[] { top, mid, bot };
    }

    // ------------------------------------------------------------------
    // Player dashboards
    // ------------------------------------------------------------------

    public void printPlayerDashboard(PlayerStatsDTO stats, boolean isLocal, boolean isCurrent) {
        if (isLocal) printLocalDashboard(stats, isCurrent);
        else printOpponentLine(stats, isCurrent);
    }

    public void printOpponentsHeader() {
        System.out.println("  " + ansi("OPPONENTS", B_PARCHMENT));
    }

    private void printLocalDashboard(PlayerStatsDTO stats, boolean isCurrent) {
        String colorSgr = colorSgrFor(stats.color());
        String marker = isCurrent ? ansi("★", GOLD) : " ";
        String title = " " + marker + " "
                + ansi("YOU — " + stats.nickname().toUpperCase() + " [" + stats.color() + "]", colorSgr)
                + "        " + ansi("Food", D_INK) + " " + ansi(String.valueOf(stats.food()), BOLD)
                + "    " + ansi("Points", D_INK) + " " + ansi(String.valueOf(stats.points()), BOLD);

        openBox(title, colorSgr);

        boxRow("  " + categoryCell("Collectors", ClassKind.COLLECTOR, stats.collectorCards(), 24)
                + categoryCell("Builders",   ClassKind.BUILDER,   stats.builderCards(),   24)
                + categoryCell("Artists",    ClassKind.ARTIST,    stats.artistCards(),    24));
        boxRow("  " + categoryCell("Hunters", ClassKind.HUNTER,   stats.hunterCards(),    24)
                + categoryCell("Shamans",  ClassKind.SHAMAN,    stats.shamanCards(),    24)
                + categoryCell("Inventors",ClassKind.INVENTOR,  stats.inventorCards(),  24));
        boxRow("  " + categoryCell("Buildings", ClassKind.BUILDING, stats.ownedBuildingCards(), 72));

        boxSep();

        boxRow("  " + ansi("Feeding sale", D_INK) + " " + ansi(String.valueOf(stats.feedingSale()), BOLD)
                + "    " + ansi("Building sale", D_INK) + " " + ansi(String.valueOf(stats.buildingSale()), BOLD)
                + "    " + ansi("Shaman★", D_INK) + " " + ansi(String.valueOf(stats.shamanStars()), BOLD)
                + "    " + ansi("Inventions", D_INK) + " " + ansi(String.valueOf(stats.inventionCount()), BOLD));

        String pickInfo = "  " + ansi("Can pick:", D_INK)
                + "  " + ansi("▲", BOLD) + " " + boolBadge(stats.canPickUpper())
                + "    " + ansi("▼", BOLD) + " " + boolBadge(stats.canPickLower());
        if (stats.affordableBuildingIds() != null && !stats.affordableBuildingIds().isEmpty()) {
            pickInfo += "    " + ansi("Affordable:", D_INK) + " " + formatIdList(stats.affordableBuildingIds());
        }
        boxRow(pickInfo);

        closeBox();
        System.out.println();
    }

    private void printOpponentLine(PlayerStatsDTO stats, boolean isCurrent) {
        String colorSgr = colorSgrFor(stats.color());
        String marker = isCurrent ? ansi("★", GOLD) : " ";

        String summary = ansi(iconFor(ClassKind.HUNTER), COL_HUNTER)
                + ansi(String.valueOf(stats.hunterCards().size()), BOLD)
                + "  " + ansi(iconFor(ClassKind.COLLECTOR), COL_COLLECTOR)
                + ansi(String.valueOf(stats.collectorCards().size()), BOLD)
                + "  " + ansi(iconFor(ClassKind.BUILDER), COL_BUILDER)
                + ansi(String.valueOf(stats.builderCards().size()), BOLD)
                + "  " + ansi(iconFor(ClassKind.SHAMAN), COL_SHAMAN)
                + ansi(String.valueOf(stats.shamanCards().size()), BOLD)
                + "  " + ansi(iconFor(ClassKind.ARTIST), COL_ARTIST)
                + ansi(String.valueOf(stats.artistCards().size()), BOLD)
                + "  " + ansi(iconFor(ClassKind.INVENTOR), COL_INVENTOR)
                + ansi(String.valueOf(stats.inventorCards().size()), BOLD)
                + "  " + ansi(iconFor(ClassKind.BUILDING), COL_BUILDING)
                + ansi(String.valueOf(stats.ownedBuildingCards().size()), BOLD);

        String name = marker + " " + ansi(padRightPlain(stats.nickname(), 10), colorSgr)
                + " [" + ansi(stats.color(), colorSgr) + "]";

        System.out.println("  " + name
                + "   " + ansi("F", D_INK) + " " + ansi(String.format("%2d", stats.food()), BOLD)
                + "   " + ansi("P", D_INK) + " " + ansi(String.format("%2d", stats.points()), BOLD)
                + "   " + summary);
    }

    /**
     * Single-line "icon Label  ids" cell, padded out to {@code width} so multiple
     * cells line up horizontally in the dashboard.
     */
    private String categoryCell(String label, ClassKind kind, List<Integer> ids, int width) {
        String tint = colorFor(kind);
        String icon = iconFor(kind);
        String ownedStr;
        if (ids == null || ids.isEmpty()) {
            ownedStr = ansi("—", D_STONE);
        } else {
            StringBuilder sb = new StringBuilder();
            for (Integer id : ids) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(String.format("%02d", id));
            }
            ownedStr = ansi(sb.toString(), tint);
        }
        String cell = ansi(icon, tint) + " " + ansi(padRightPlain(label, 11), BOLD) + " " + ownedStr;
        return padRight(cell, width);
    }

    // ------------------------------------------------------------------
    // Recent-events log + legend
    // ------------------------------------------------------------------

    public void printRecentLog(List<String> entries) {
        if (entries == null || entries.isEmpty()) return;
        String header = "─ Recent events ";
        System.out.println("  " + ansi(header + repeat('─', Math.max(0, WIDTH - 4 - header.length())), PARCHMENT_DIM));
        for (String entry : entries) {
            System.out.println("  " + ansi("▸", GOLD) + " " + entry);
        }
        System.out.println();
    }

    public void printLegend() {
        System.out.println("  " + ansi("LEGEND", D_INK)
                + "  " + ansi("✿", COL_COLLECTOR) + " Collector"
                + "  " + ansi("⚔", COL_HUNTER) + " Hunter"
                + "  " + ansi("⚒", COL_BUILDER) + " Builder"
                + "  " + ansi("☾", COL_SHAMAN) + " Shaman"
                + "  " + ansi("♪", COL_ARTIST) + " Artist"
                + "  " + ansi("⚙", COL_INVENTOR) + " Inventor"
                + "  " + ansi("⌂", COL_BUILDING) + " Building"
                + "  " + ansi("✦", COL_EVENT) + " Event");
    }

    public void printEventNotice(int eventCardId) {
        System.out.println("  " + ansi("✦  Event resolved: card #" + eventCardId, B_EVENT));
    }

    // ------------------------------------------------------------------
    // End-game (podium + leaderboards)
    // ------------------------------------------------------------------

    public void printEndGame(GameEndedMessage msg, String localNick) {
        clearScreen();
        printTitle();

        System.out.println(ansi(center("─── GAME OVER ───", WIDTH), B_GOLD));
        System.out.println();

        List<EndGameDTO> ranking = msg.matchRanking() != null ? msg.matchRanking() : List.of();
        printPodium(ranking, localNick);

        openBox("MATCH RANKING", PARCHMENT);
        if (ranking.isEmpty()) {
            boxRow("  " + ansi("(no data)", D_INK));
        } else {
            for (EndGameDTO e : ranking) {
                String highlight = e.nickname().equals(localNick) ? "   " + ansi("← YOU", B_GOLD) : "";
                String crown = e.isWinner() ? " " + ansi("👑", GOLD) : "";
                boxRow("  " + e.rank() + ". " + ansi(padRightPlain(e.nickname(), 18), BOLD)
                        + "  " + ansi("points:", D_INK) + " " + ansi(String.format("%3d", e.score()), BOLD)
                        + "   " + ansi("food:", D_INK) + " " + e.food()
                        + crown + highlight);
            }
        }
        closeBox();
        System.out.println();

        openBox("GLOBAL LEADERBOARD  (game size " + msg.gameSize() + ")", PARCHMENT);
        if (msg.globalLeaderboard() == null || msg.globalLeaderboard().isEmpty()) {
            boxRow("  " + ansi("(unavailable — leaderboard DB unreachable)", D_INK));
        } else {
            for (EndGameDTO e : msg.globalLeaderboard()) {
                String highlight = e.nickname().equals(localNick) ? "   " + ansi("← YOU", B_GOLD) : "";
                boxRow("  " + e.rank() + ". " + ansi(padRightPlain(e.nickname(), 18), BOLD)
                        + "  " + ansi("points:", D_INK) + " " + ansi(String.format("%3d", e.score()), BOLD)
                        + "   " + ansi("date:", D_INK) + " " + e.date()
                        + highlight);
            }
        }
        closeBox();
        System.out.println();
    }

    private void printPodium(List<EndGameDTO> ranking, String localNick) {
        if (ranking.isEmpty()) return;

        EndGameDTO first  = ranking.size() > 0 ? ranking.get(0) : null;
        EndGameDTO second = ranking.size() > 1 ? ranking.get(1) : null;
        EndGameDTO third  = ranking.size() > 2 ? ranking.get(2) : null;

        String firstName  = podiumName(first, localNick);
        String secondName = podiumName(second, localNick);
        String thirdName  = podiumName(third, localNick);

        System.out.println("              " + ansi("┌───┐", GOLD));
        System.out.println("          " + ansi("┌───┤", SILVER) + " " + ansi("1", B_GOLD) + " "
                + ansi("├───", SILVER) + "  " + firstName);
        System.out.println("          " + ansi("│ ", SILVER) + ansi("2", B_SILVER)
                + ansi(" │", SILVER) + ansi("   │", GOLD) + "         " + secondName);
        System.out.println("      " + ansi("┌───┤", CLAY) + ansi("   │", SILVER)
                + ansi("   │", GOLD) + "             " + thirdName);
        System.out.println("      " + ansi("│ ", CLAY) + ansi("3", B_CLAY)
                + ansi(" │   │   │", CLAY));
        System.out.println("      " + ansi("└───┴───┴───┘", CLAY));
        System.out.println();
    }

    private String podiumName(EndGameDTO e, String localNick) {
        if (e == null) return ansi("—", D_STONE);
        String you = e.nickname().equals(localNick) ? "  " + ansi("(you)", B_GOLD) : "";
        return ansi(e.nickname(), BOLD) + "   " + ansi(e.score() + " pts", D_INK) + you;
    }

    // ------------------------------------------------------------------
    // Disconnect / abort banners
    // ------------------------------------------------------------------

    public void printAborted() {
        System.out.println();
        System.out.println(" " + ansi("✖ Game aborted: a player disconnected.", BOLD_RED));
        System.out.println();
    }

    public void printServerDown() {
        System.out.println();
        System.out.println(" " + ansi("✖ Server connection lost. Exiting.", BOLD_RED));
        System.out.println();
    }

    public void printReconnecting() {
        System.out.println(" " + ansi("⟳ Connection lost, reconnecting...", BOLD_YELLOW));
    }

    public void printReconnected() {
        System.out.println(" " + ansi("✔ Reconnected.", BOLD_GREEN));
    }

    // ------------------------------------------------------------------
    // Offer / Order formatters
    // ------------------------------------------------------------------

    private String formatOfferTiles(List<String> letters, List<TotemPositionDTO> totems) {
        if (letters == null || letters.isEmpty()) return ansi("(none)", D_STONE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < letters.size(); i++) {
            if (i > 0) sb.append("   ");
            String letter = letters.get(i);
            String occupant = totemColorAtOffer(letter, totems);
            String label = "[" + letter + "]";
            if (occupant != null) {
                sb.append(ansi(label, colorSgrFor(occupant)));
            } else {
                sb.append(label);
            }
        }
        return sb.toString();
    }

    private String formatOrderTiles(int orderTileCount, List<TotemPositionDTO> totems) {
        List<TotemPositionDTO> orderTotems = new ArrayList<>();
        if (totems != null) {
            for (TotemPositionDTO t : totems) {
                if ("ORDER".equals(t.locationType())) orderTotems.add(t);
            }
        }
        int total = orderTileCount;
        if (total <= 0) return ansi("(empty)", D_STONE);

        // Map 1-based slot index to totem color (GUI stores slots as "1", "2", ...).
        Map<Integer, String> slotColor = new HashMap<>();
        for (TotemPositionDTO t : orderTotems) {
            try { slotColor.put(Integer.parseInt(t.locationId()), t.totemColor()); }
            catch (NumberFormatException ignored) {}
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= total; i++) {
            if (i > 1) sb.append("   ");
            String label = "[" + i + "]";
            String color = slotColor.get(i);
            if (color != null) {
                sb.append(ansi(label, colorSgrFor(color)));
            } else {
                sb.append(label);
            }
        }
        return sb.toString();
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

    private String formatIdList(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return ansi("—", D_STONE);
        StringBuilder sb = new StringBuilder();
        for (Integer id : ids) {
            sb.append("[").append(String.format("%2d", id)).append("] ");
        }
        return sb.toString().trim();
    }

    // ------------------------------------------------------------------
    // Lookups
    // ------------------------------------------------------------------

    public String iconFor(ClassKind kind) {
        return switch (kind) {
            case COLLECTOR -> "✿";
            case HUNTER    -> "⚔";
            case BUILDER   -> "⚒";
            case SHAMAN    -> "☾";
            case ARTIST    -> "♪";
            case INVENTOR  -> "⚙";
            case BUILDING  -> "⌂";
            case EVENT     -> "✦";
            default        -> "?";
        };
    }

    public String labelFor(ClassKind kind) {
        return switch (kind) {
            case COLLECTOR -> "Collector";
            case HUNTER    -> "Hunter";
            case BUILDER   -> "Builder";
            case SHAMAN    -> "Shaman";
            case ARTIST    -> "Artist";
            case INVENTOR  -> "Inventor";
            case BUILDING  -> "Building";
            case EVENT     -> "Event";
            default        -> "card";
        };
    }

    private String colorFor(ClassKind kind) {
        return switch (kind) {
            case COLLECTOR -> COL_COLLECTOR;
            case HUNTER    -> COL_HUNTER;
            case BUILDER   -> COL_BUILDER;
            case SHAMAN    -> COL_SHAMAN;
            case ARTIST    -> COL_ARTIST;
            case INVENTOR  -> COL_INVENTOR;
            case BUILDING  -> COL_BUILDING;
            case EVENT     -> COL_EVENT;
            default        -> STONE;
        };
    }

    public String colorSgrFor(String color) {
        if (color == null) return BOLD_WHITE;
        return switch (color.toUpperCase()) {
            case "RED"             -> BOLD_RED;
            case "GREEN"           -> BOLD_GREEN;
            case "YELLOW"          -> BOLD_YELLOW;
            case "BLUE"            -> BOLD_BLUE;
            case "PURPLE", "MAGENTA" -> BOLD_MAGENTA;
            case "CYAN"            -> BOLD_CYAN;
            case "WHITE"           -> BOLD_WHITE;
            case "BLACK"           -> BOLD_BLACK;
            default                -> BOLD_WHITE;
        };
    }

    private String boolBadge(boolean v) {
        return v ? ansi("✓", BOLD_GREEN) : ansi("✗", BOLD_RED);
    }

    // ------------------------------------------------------------------
    // Box helpers
    // ------------------------------------------------------------------

    private void openBox(String title, String borderSgr) {
        String stripped = stripAnsi(title);
        int titleLen = stripped.length();
        int dashes = WIDTH - 4 - titleLen;
        if (dashes < 0) dashes = 0;
        System.out.println(ansi(TL + repeat('─', 2), borderSgr)
                + " " + ansi(title, BOLD)
                + " " + ansi(repeat('─', Math.max(0, dashes - 1)) + TR, borderSgr));
    }

    private void boxRow(String content) {
        String stripped = stripAnsi(content);
        int pad = WIDTH - 2 - stripped.length();
        if (pad < 0) pad = 0;
        System.out.println(" " + V + " " + content + repeat(' ', pad) + V);
    }

    private void boxSep() {
        System.out.println(" " + V + repeat('─', WIDTH - 2) + V);
    }

    private void closeBox() {
        System.out.println(" " + BL + repeat('─', WIDTH - 2) + BR);
    }

    // ------------------------------------------------------------------
    // String / ANSI helpers
    // ------------------------------------------------------------------

    /**
     * Wrap {@code body} in {@code sgr}…{@code RESET}. No trailing space — callers
     * must put a plain character (typically a space) between consecutive {@code ansi(…)}
     * results so the byte stream never has two adjacent SGR escapes.
     */
    private String ansi(String body, String sgr) {
        if (sgr == null || sgr.isEmpty()) return body;
        return sgr + body + RESET;
    }

    private String repeat(char c, int n) {
        return n <= 0 ? "" : String.valueOf(c).repeat(n);
    }

    private String center(String s, int width) {
        int len = stripAnsi(s).length();
        if (len >= width) return s;
        int left = (width - len) / 2;
        return " ".repeat(left) + s;
    }

    private String padRight(String s, int width) {
        int len = stripAnsi(s).length();
        int pad = width - len;
        if (pad <= 0) return s;
        return s + " ".repeat(pad);
    }

    private String padRightPlain(String s, int width) {
        int pad = width - s.length();
        return pad <= 0 ? s : s + " ".repeat(pad);
    }

    /**
     * Strip ANSI CSI sequences (including the leading ESC byte) so width math
     * counts visible characters only.
     */
    private String stripAnsi(String s) {
        return s.replaceAll("\\u001B\\[[;\\d]*[A-Za-z]", "");
    }
}
