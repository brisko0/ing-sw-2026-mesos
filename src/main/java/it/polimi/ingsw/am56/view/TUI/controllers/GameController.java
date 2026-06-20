package it.polimi.ingsw.am56.view.TUI.controllers;

import it.polimi.ingsw.am56.model.state.State;
import it.polimi.ingsw.am56.network.message.classesDTO.BoardDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.CurrentPlayerDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.EndRoundSnapshotDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.EventOutcomeDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.GameSnapshotDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.PlayerStatsDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.TotemPositionDTO;
import it.polimi.ingsw.am56.network.message.request.CardPickRequest;
import it.polimi.ingsw.am56.network.message.request.ReconnectRequest;
import it.polimi.ingsw.am56.network.message.request.SkipExtraPickRequest;
import it.polimi.ingsw.am56.network.message.request.TotemMoveRequest;
import it.polimi.ingsw.am56.network.message.update.BoardUpdatedMessage;
import it.polimi.ingsw.am56.network.message.update.CurrentPlayerUpdateMessage;
import it.polimi.ingsw.am56.network.message.update.EndRoundUpdateMessage;
import it.polimi.ingsw.am56.network.message.update.EventResolvedMessage;
import it.polimi.ingsw.am56.network.message.update.GameAbortedMessage;
import it.polimi.ingsw.am56.network.message.update.GameEndedMessage;
import it.polimi.ingsw.am56.network.message.update.GameSnapshotMessage;
import it.polimi.ingsw.am56.network.message.update.GameStateUpdateMessage;
import it.polimi.ingsw.am56.network.message.update.PlayerStatsUpdateMessage;
import it.polimi.ingsw.am56.network.message.update.TotemsStateMessage;
import it.polimi.ingsw.am56.view.TUI.CardCatalog.ClassKind;
import it.polimi.ingsw.am56.view.TUI.TUIContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * In-game TUI screen. It keeps a local copy of the game state (board, per-player stats, totems, current
 * player, phase, era and round) that it updates from the server-push messages it visits, and renders it
 * to the terminal. It also reads the local player's moves and sends the corresponding requests.
 * <p>
 * Concurrency is split across two daemon threads guarded by two monitors. The render thread waits on
 * {@code renderLock} and, when state is marked {@code dirty}, coalesces the burst of messages from a
 * single server "transaction" into one redraw (debounced by {@link #RENDER_DEBOUNCE_MS}). The input
 * thread waits on {@code lock} until it is the local player's turn in an actionable phase, prompts for
 * the move, sends it and then waits for the state version to advance before prompting again. Incoming
 * messages mutate the local state under {@code lock}, bump the state version, wake both threads and
 * mark the screen dirty.
 * <p>
 * As a side effect of rendering, it derives a short player-facing event log by diffing successive
 * stats/totem snapshots (e.g. "X picked Builder #12", "RED placed totem on tile A").
 */
public class GameController extends BaseTUIController {

    /** Coalesce all messages from a server "transaction" into a single render. */
    private static final long RENDER_DEBOUNCE_MS = 80;

    /** Maximum number of recent-events entries shown in the in-game log. */
    private static final int LOG_LIMIT = 5;

    private final Object lock = new Object();

    // Local snapshot of game state, mutated only under lock.
    private BoardDTO board;
    private final Map<String, PlayerStatsDTO> playerStats = new LinkedHashMap<>();
    private List<TotemPositionDTO> totems = new ArrayList<>();
    private CurrentPlayerDTO currentPlayer;
    private State currentPhase;
    private int currentEra;
    private int currentRound;
    private long stateVersion = 0;

    /** Rolling deque of player-facing log lines (oldest at head). */
    private final Deque<String> recentLog = new ArrayDeque<>();

    // Render coalescer.
    private final Object renderLock = new Object();
    private boolean dirty = false;

    private volatile boolean gameOver = false;
    private Thread inputThread;
    private Thread renderThread;

    /**
     * Creates the in-game screen, initializing the local state from the initial snapshot.
     *
     * @param context         the shared TUI context
     * @param initialSnapshot the snapshot to start from
     */
    public GameController(TUIContext context, GameSnapshotMessage initialSnapshot) {
        super(context);
        applySnapshot(initialSnapshot.snapshot());
    }

    /**
     * Starts the daemon render and input threads and triggers the first render.
     */
    @Override
    public void onEnter() {
        renderThread = new Thread(this::runRenderLoop, "TUI-render");
        renderThread.setDaemon(true);
        renderThread.start();

        inputThread = new Thread(this::runInputLoop, "TUI-input");
        inputThread.setDaemon(true);
        inputThread.start();

        markDirty();
    }

    // ------------------------------------------------------------------
    // Server-push handlers
    // ------------------------------------------------------------------

    /**
     * Replaces the whole local state with a fresh snapshot.
     *
     * @param message the game snapshot
     */
    @Override
    public void visit(GameSnapshotMessage message) {
        synchronized (lock) {
            applySnapshot(message.snapshot());
            bumpAndNotify();
        }
        markDirty();
    }

    /**
     * Updates the local board from a board update.
     *
     * @param message the board update
     */
    @Override
    public void visit(BoardUpdatedMessage message) {
        synchronized (lock) {
            this.board = message.board();
            bumpAndNotify();
        }
        markDirty();
    }

    /**
     * Updates one player's stats, logging the picked card if the new stats reveal one was taken.
     *
     * @param message the player stats update
     */
    @Override
    public void visit(PlayerStatsUpdateMessage message) {
        synchronized (lock) {
            PlayerStatsDTO incoming = message.stats();
            PlayerStatsDTO previous = playerStats.get(incoming.nickname());
            String picked = diffPickedCard(previous, incoming);
            if (picked != null) appendLog(picked);
            playerStats.put(incoming.nickname(), incoming);
            bumpAndNotify();
        }
        markDirty();
    }

    /**
     * Updates the totem positions, logging a new offer-tile placement if one is detected.
     *
     * @param message the totems-state update
     */
    @Override
    public void visit(TotemsStateMessage message) {
        synchronized (lock) {
            List<TotemPositionDTO> incoming = message.totems() != null
                    ? new ArrayList<>(message.totems())
                    : new ArrayList<>();
            String placement = diffOfferPlacement(this.totems, incoming);
            if (placement != null) appendLog(placement);
            this.totems = incoming;
            bumpAndNotify();
        }
        markDirty();
    }

    /**
     * Updates the current player.
     *
     * @param message the current-player update
     */
    @Override
    public void visit(CurrentPlayerUpdateMessage message) {
        synchronized (lock) {
            this.currentPlayer = message.currentPlayer();
            bumpAndNotify();
        }
        markDirty();
    }

    /**
     * Updates the current game phase.
     *
     * @param message the game-state update
     */
    @Override
    public void visit(GameStateUpdateMessage message) {
        synchronized (lock) {
            if (message.gameState() != null) {
                this.currentPhase = message.gameState().gameState();
            }
            bumpAndNotify();
        }
        markDirty();
    }

    /**
     * Applies an end-of-round snapshot (board, all stats, current player, era and round) and logs the
     * round end.
     *
     * @param message the end-round update
     */
    @Override
    public void visit(EndRoundUpdateMessage message) {
        synchronized (lock) {
            EndRoundSnapshotDTO s = message.snapshot();
            this.board = s.board();
            playerStats.clear();
            for (PlayerStatsDTO p : s.playersStats()) {
                playerStats.put(p.nickname(), p);
            }
            this.currentPlayer = s.currentPlayer();
            this.currentEra = s.currentEra();
            this.currentRound = s.currentRound();
            appendLog("Round ended — Era " + currentEra + " Round " + currentRound);
            bumpAndNotify();
        }
        markDirty();
    }

    /**
     * Logs the outcome of a resolved event card.
     *
     * @param message the event-resolved update
     */
    @Override
    public void visit(EventResolvedMessage message) {
        synchronized (lock) {
            appendLog(formatEventLog(message));
            bumpAndNotify();
        }
        // Print the inline flash on the next render rather than now, so it isn't
        // wiped by the imminent clearScreen() from the coalesced redraw.
        markDirty();
    }

    /**
     * Builds the events-log line for a resolved event, e.g.
     * {@code "Event #94: mario: -2F · luigi: +3P"}. Players whose Food and Points
     * are both unchanged are omitted; if nobody changed, falls back to "resolved".
     */
    private String formatEventLog(EventResolvedMessage message) {
        StringBuilder parts = new StringBuilder();
        if (message.outcomes() != null) {
            for (EventOutcomeDTO o : message.outcomes()) {
                String delta = formatDelta(o.foodDelta(), o.pointsDelta());
                if (delta.isEmpty()) continue;
                if (parts.length() > 0) parts.append(" · ");
                parts.append(o.nickname()).append(": ").append(delta);
            }
        }
        return parts.length() > 0
                ? "Event #" + message.cardId() + ": " + parts
                : "Event #" + message.cardId() + " resolved";
    }

    /** Formats a Food/Points delta as e.g. {@code "+1F +2P"}, {@code "-2F"}, or {@code ""} if both zero. */
    private String formatDelta(int food, int points) {
        StringBuilder s = new StringBuilder();
        if (food != 0) s.append(food > 0 ? "+" : "").append(food).append("F");
        if (points != 0) {
            if (s.length() > 0) s.append(" ");
            s.append(points > 0 ? "+" : "").append(points).append("P");
        }
        return s.toString();
    }

    /**
     * Ends the game: stops the render/input loops, shows the final results and exits the client.
     *
     * @param message the game-ended message carrying the final results
     */
    @Override
    public void visit(GameEndedMessage message) {
        synchronized (lock) {
            gameOver = true;
            bumpAndNotify();
        }
        synchronized (renderLock) {
            dirty = false;
            renderLock.notifyAll();
        }
        drawer.clearScreen();
        drawer.printEndGame(message, context.getLocalNickname());
        sleepBriefly(800);
        System.exit(0);
    }

    /**
     * Aborts the game (e.g. a player disconnected): stops the loops, shows the aborted notice and exits
     * the client.
     *
     * @param message the game-aborted message
     */
    @Override
    public void visit(GameAbortedMessage message) {
        synchronized (lock) {
            gameOver = true;
            bumpAndNotify();
        }
        synchronized (renderLock) {
            dirty = false;
            renderLock.notifyAll();
        }
        drawer.printAborted();
        sleepBriefly(800);
        System.exit(0);
    }

    /**
     * On disconnection, shows the reconnecting indicator (overriding the default exit behaviour).
     */
    @Override
    public void handleDisconnect() {
        drawer.printReconnecting();
    }

    /**
     * On reconnection, shows the reconnected indicator and sends a reconnect request to resync with the
     * game in progress.
     */
    @Override
    public void handleReconnected() {
        drawer.printReconnected();
        context.getServerAdapter().sendRequest(new ReconnectRequest(context.getLocalNickname()));
    }

    // ------------------------------------------------------------------
    // Render thread
    // ------------------------------------------------------------------

    /**
     * Render loop: waits until the state is marked dirty, then debounces briefly to coalesce a burst of
     * messages into a single {@link #renderAll()}, repeating until the game is over.
     */
    private void runRenderLoop() {
        try {
            while (!gameOver) {
                synchronized (renderLock) {
                    while (!dirty && !gameOver) renderLock.wait();
                    if (gameOver) return;
                    dirty = false;
                }
                Thread.sleep(RENDER_DEBOUNCE_MS);
                synchronized (renderLock) {
                    dirty = false;
                }
                renderAll();
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Marks the state as dirty and wakes the render loop so it redraws.
     */
    private void markDirty() {
        synchronized (renderLock) {
            dirty = true;
            renderLock.notifyAll();
        }
    }

    // ------------------------------------------------------------------
    // Input loop
    // ------------------------------------------------------------------

    /**
     * Input loop: waits until it is the local player's turn in an actionable phase, lets the state
     * settle, prompts for the appropriate move (totem placement or card pick) and then waits for the
     * state version to advance before looping again.
     */
    private void runInputLoop() {
        try {
            while (!gameOver) {
                synchronized (lock) {
                    while (!gameOver && (!isLocalTurn() || !isActionablePhase())) {
                        lock.wait();
                    }
                    if (gameOver) return;
                }

                // Settle: server transactions emit several messages in a row.
                Thread.sleep(RENDER_DEBOUNCE_MS + 50);

                long versionAtPrompt;
                State phaseAtPrompt;
                synchronized (lock) {
                    if (!isLocalTurn() || !isActionablePhase()) {
                        continue;
                    }
                    versionAtPrompt = stateVersion;
                    phaseAtPrompt = currentPhase;
                }

                if (phaseAtPrompt == State.TOTEM_PLACEMENT) {
                    promptTotemMove();
                } else if (phaseAtPrompt == State.CARD_PICKING || phaseAtPrompt == State.EXTRA_UPPER_CARD_PICKING) {
                    promptCardPick();
                }

                synchronized (lock) {
                    while (!gameOver && stateVersion == versionAtPrompt) {
                        lock.wait();
                    }
                }
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Prompts the local player to place their totem on a free offer tile and sends the move request;
     * does nothing if no free tile is available.
     */
    private void promptTotemMove() {
        Set<String> allowed = new HashSet<>();
        List<String> offerLetters;
        List<TotemPositionDTO> totemsSnap;
        synchronized (lock) {
            offerLetters = board != null ? new ArrayList<>(board.offerTilesLetters()) : List.of();
            totemsSnap = new ArrayList<>(totems);
        }
        for (String letter : offerLetters) {
            if (!isLetterOccupied(letter, totemsSnap)) {
                allowed.add(letter.toUpperCase());
            }
        }
        if (allowed.isEmpty()) {
            drawer.printError("No free offer tiles available.");
            return;
        }

        drawer.printPrompt("Place your totem on a free offer tile");
        String letter = inputReader.readLetterFrom(allowed, "  Pick letter");
        context.getServerAdapter().sendRequest(
                new TotemMoveRequest(context.getLocalNickname(), letter));
    }

    /**
     * Computes which board cards the local player may currently pick (respecting upper/lower
     * permissions, excluding event cards and unaffordable buildings) and prompts for the choice,
     * sending a card-pick request. Delegates to {@link #promptExtraPick(Set)} during the bonus-pick
     * phase, where taking a card is optional.
     */
    private void promptCardPick() {
        boolean extraPick;
        Set<Integer> allowed = new HashSet<>();
        synchronized (lock) {
            extraPick = currentPhase == State.EXTRA_UPPER_CARD_PICKING;
            PlayerStatsDTO me = playerStats.get(context.getLocalNickname());
            if (me == null || board == null) {
                drawer.printError("Player state not available.");
                return;
            }
            List<Integer> eventIds = board.eventCardIds() != null ? board.eventCardIds() : List.of();
            List<Integer> affordable = me.affordableBuildingIds() != null
                    ? me.affordableBuildingIds() : List.of();

            if (me.canPickUpper()) {
                addPickable(allowed, board.upperCharEvIds(), eventIds, null);
                addPickable(allowed, board.upperBuildingsIds(), eventIds, affordable);
            }
            if (me.canPickLower()) {
                addPickable(allowed, board.lowerCharEvIds(), eventIds, null);
                addPickable(allowed, board.lowerBuildingsIds(), eventIds, affordable);
            }
        }

        // Bonus pick granted by a building: taking a card is optional, so always offer "skip".
        if (extraPick) {
            promptExtraPick(allowed);
            return;
        }

        if (allowed.isEmpty()) {
            drawer.printError("No pickable cards right now.");
            return;
        }

        drawer.printPrompt("Pick a card from the board");
        int cardId = inputReader.readIntFromSet(allowed, "  Card ID");
        context.getServerAdapter().sendRequest(
                new CardPickRequest(context.getLocalNickname(), cardId));
    }

    /**
     * Handles the optional bonus upper-row pick: auto-skips when nothing is affordable, otherwise lets
     * the player take one of the allowed cards or skip, sending the matching request.
     *
     * @param allowed the ids of the cards that may be picked in this bonus pick
     */
    private void promptExtraPick(Set<Integer> allowed) {
        if (allowed.isEmpty()) {
            drawer.printPrompt("Bonus pick: no affordable upper-row card — skipping.");
            context.getServerAdapter().sendRequest(
                    new SkipExtraPickRequest(context.getLocalNickname()));
            return;
        }
        String hint = allowed.stream().sorted().map(String::valueOf).collect(Collectors.joining(", "));
        drawer.printPrompt("Bonus pick: take 1 card from the UPPER row, or skip");
        while (true) {
            String input = inputReader.readString("  Card ID [" + hint + "] or 'S' to skip:");
            if (input.equalsIgnoreCase("S")) {
                context.getServerAdapter().sendRequest(
                        new SkipExtraPickRequest(context.getLocalNickname()));
                return;
            }
            try {
                int cardId = Integer.parseInt(input);
                if (allowed.contains(cardId)) {
                    context.getServerAdapter().sendRequest(
                            new CardPickRequest(context.getLocalNickname(), cardId));
                    return;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the error hint below
            }
            drawer.printError("Enter one of [" + hint + "] or 'S' to skip.");
        }
    }

    /**
     * Adds to {@code out} the pickable ids from {@code ids}, skipping event cards and, when an
     * affordability filter is given, ids not contained in it.
     *
     * @param out              the accumulating set of pickable ids
     * @param ids              the candidate card ids (a board row)
     * @param eventIds         the ids of event cards to exclude
     * @param affordableFilter ids the player can afford, or {@code null} to skip the affordability check
     */
    private void addPickable(Set<Integer> out, List<Integer> ids,
                             List<Integer> eventIds, List<Integer> affordableFilter) {
        if (ids == null) return;
        for (Integer id : ids) {
            if (eventIds.contains(id)) continue;
            if (affordableFilter != null && !affordableFilter.contains(id)) continue;
            out.add(id);
        }
    }

    // ------------------------------------------------------------------
    // Render
    // ------------------------------------------------------------------

    /**
     * Renders the full in-game screen from a consistent copy of the local state: header (era, round,
     * current player, phase), board with totems, the local player's dashboard, the opponents' summaries,
     * the recent-events log and the legend.
     */
    private void renderAll() {
        BoardDTO boardCopy;
        List<TotemPositionDTO> totemsCopy;
        CurrentPlayerDTO currentCopy;
        State phaseCopy;
        int eraCopy, roundCopy;
        List<PlayerStatsDTO> playersCopy;
        List<String> logCopy;
        synchronized (lock) {
            boardCopy = this.board;
            totemsCopy = new ArrayList<>(this.totems);
            currentCopy = this.currentPlayer;
            phaseCopy = this.currentPhase;
            eraCopy = this.currentEra;
            roundCopy = this.currentRound;
            playersCopy = new ArrayList<>(this.playerStats.values());
            logCopy = new ArrayList<>(this.recentLog);
        }

        drawer.clearScreen();
        drawer.printGameHeader(eraCopy, roundCopy,
                currentCopy != null ? currentCopy.nickname() : "?",
                currentCopy != null ? currentCopy.color() : null,
                phaseLabel(phaseCopy));

        if (boardCopy != null) {
            drawer.printBoard(boardCopy, totemsCopy);
        }

        String me = context.getLocalNickname();
        PlayerStatsDTO mine = null;
        for (PlayerStatsDTO p : playersCopy) {
            if (p.nickname().equals(me)) { mine = p; break; }
        }
        if (mine != null) {
            drawer.printPlayerDashboard(mine, true, isCurrent(mine.nickname(), currentCopy));
        }

        // Opponents are rendered as a single-line summary under a small header.
        boolean opponentsHeaderPrinted = false;
        for (PlayerStatsDTO p : playersCopy) {
            if (p.nickname().equals(me)) continue;
            if (!opponentsHeaderPrinted) {
                drawer.printOpponentsHeader();
                opponentsHeaderPrinted = true;
            }
            drawer.printPlayerDashboard(p, false, isCurrent(p.nickname(), currentCopy));
        }
        if (opponentsHeaderPrinted) System.out.println();

        drawer.printRecentLog(logCopy);
        drawer.printLegend();
    }

    /**
     * Maps a game phase to a human-readable label for the header.
     *
     * @param phase the current phase, may be {@code null}
     * @return the display label
     */
    private String phaseLabel(State phase) {
        if (phase == null) return "?";
        return switch (phase) {
            case TOTEM_PLACEMENT          -> "Totem Placement";
            case EXTRA_UPPER_CARD_PICKING -> "Bonus Pick";
            case CARD_PICKING             -> "Card Picking";
            case ROUND_ENDING             -> "Round End";
            case GAME_ENDING              -> "Game End";
            case PLAYERS_FILLING          -> "Waiting for Players";
        };
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Overwrites the entire local state from a game snapshot.
     *
     * @param snap the snapshot to apply
     */
    private void applySnapshot(GameSnapshotDTO snap) {
        this.board = snap.board();
        playerStats.clear();
        for (PlayerStatsDTO p : snap.playersStats()) {
            playerStats.put(p.nickname(), p);
        }
        this.totems = snap.totems() != null ? new ArrayList<>(snap.totems()) : new ArrayList<>();
        this.currentPlayer = snap.currentPlayer();
        this.currentPhase = snap.gameStateDTO() != null ? snap.gameStateDTO().gameState() : null;
        this.currentEra = snap.currentEra();
        this.currentRound = snap.currentRound();
    }

    /**
     * @return {@code true} if it is the local player's turn
     */
    private boolean isLocalTurn() {
        return currentPlayer != null
                && context.getLocalNickname() != null
                && context.getLocalNickname().equals(currentPlayer.nickname());
    }

    /**
     * @return {@code true} if the current phase requires an action from the player (totem placement or
     *         card picking)
     */
    private boolean isActionablePhase() {
        return currentPhase == State.TOTEM_PLACEMENT
                || currentPhase == State.CARD_PICKING
                || currentPhase == State.EXTRA_UPPER_CARD_PICKING;
    }

    /**
     * @param nickname the nickname to test
     * @param current  the current-player descriptor
     * @return {@code true} if {@code nickname} is the current player
     */
    private boolean isCurrent(String nickname, CurrentPlayerDTO current) {
        return current != null && nickname.equals(current.nickname());
    }

    /**
     * @param letter     the offer-tile letter to test
     * @param totemsSnap the totem positions to check against
     * @return {@code true} if a totem already occupies that offer tile
     */
    private boolean isLetterOccupied(String letter, List<TotemPositionDTO> totemsSnap) {
        for (TotemPositionDTO t : totemsSnap) {
            if ("OFFER".equals(t.locationType()) && letter.equalsIgnoreCase(t.locationId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Advances the state version and wakes any thread waiting on {@code lock} (notably the input loop).
     * Must be called while holding {@code lock}.
     */
    private void bumpAndNotify() {
        stateVersion++;
        lock.notifyAll();
    }

    /**
     * Sleeps for the given duration, restoring the interrupt flag if interrupted.
     *
     * @param ms milliseconds to sleep
     */
    private void sleepBriefly(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    // ------------------------------------------------------------------
    // Log + diffs (called under lock)
    // ------------------------------------------------------------------

    /**
     * Appends an entry to the recent-events log, evicting the oldest beyond {@link #LOG_LIMIT}.
     *
     * @param entry the log line to add
     */
    private void appendLog(String entry) {
        recentLog.addLast(entry);
        while (recentLog.size() > LOG_LIMIT) recentLog.removeFirst();
    }

    /**
     * Returns the new OFFER occupancy line if exactly one totem changed location
     * to a new OFFER tile compared to {@code before}. Move-back-to-order or
     * board-wide resets return null.
     */
    private String diffOfferPlacement(List<TotemPositionDTO> before, List<TotemPositionDTO> after) {
        if (after == null) return null;
        for (TotemPositionDTO neu : after) {
            if (!"OFFER".equals(neu.locationType())) continue;
            boolean wasThereBefore = false;
            if (before != null) {
                for (TotemPositionDTO old : before) {
                    if (old.totemColor().equals(neu.totemColor())
                            && "OFFER".equals(old.locationType())
                            && old.locationId().equalsIgnoreCase(neu.locationId())) {
                        wasThereBefore = true;
                        break;
                    }
                }
            }
            if (!wasThereBefore) {
                return neu.totemColor() + " placed totem on tile " + neu.locationId().toUpperCase();
            }
        }
        return null;
    }

    /**
     * Returns a "{NICK} picked {KIND} #{id}" line if exactly one of the player's
     * card-list fields grew compared to {@code before}. If {@code before} is null
     * (first sighting of this player), returns null — no pick to attribute.
     */
    private String diffPickedCard(PlayerStatsDTO before, PlayerStatsDTO after) {
        if (before == null || after == null) return null;
        Integer pickedId;
        ClassKind kind = null;

        pickedId = listGrowth(before.collectorCards(), after.collectorCards());
        if (pickedId != null) kind = ClassKind.COLLECTOR;
        if (pickedId == null) { pickedId = listGrowth(before.hunterCards(), after.hunterCards()); if (pickedId != null) kind = ClassKind.HUNTER; }
        if (pickedId == null) { pickedId = listGrowth(before.builderCards(), after.builderCards()); if (pickedId != null) kind = ClassKind.BUILDER; }
        if (pickedId == null) { pickedId = listGrowth(before.shamanCards(), after.shamanCards()); if (pickedId != null) kind = ClassKind.SHAMAN; }
        if (pickedId == null) { pickedId = listGrowth(before.artistCards(), after.artistCards()); if (pickedId != null) kind = ClassKind.ARTIST; }
        if (pickedId == null) { pickedId = listGrowth(before.inventorCards(), after.inventorCards()); if (pickedId != null) kind = ClassKind.INVENTOR; }
        if (pickedId == null) { pickedId = listGrowth(before.ownedBuildingCards(), after.ownedBuildingCards()); if (pickedId != null) kind = ClassKind.BUILDING; }
        if (pickedId == null) return null;

        return after.nickname() + " picked " + kindLabel(kind) + " #" + pickedId;
    }

    /** Returns the new id if {@code after} has exactly one more entry than {@code before}, else null. */
    private Integer listGrowth(List<Integer> before, List<Integer> after) {
        if (before == null || after == null) return null;
        if (after.size() != before.size() + 1) return null;
        for (Integer id : after) {
            if (!before.contains(id)) return id;
        }
        return null;
    }

    /**
     * Maps a card class kind to a human-readable label used in log lines.
     *
     * @param kind the card class kind, may be {@code null}
     * @return the display label ({@code "card"} if unknown/null)
     */
    private String kindLabel(ClassKind kind) {
        if (kind == null) return "card";
        return switch (kind) {
            case COLLECTOR -> "Collector";
            case HUNTER -> "Hunter";
            case BUILDER -> "Builder";
            case SHAMAN -> "Shaman";
            case ARTIST -> "Artist";
            case INVENTOR -> "Inventor";
            case BUILDING -> "Building";
            case EVENT -> "Event";
            default -> "card";
        };
    }
}
