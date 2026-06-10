package it.polimi.ingsw.am56.view.TUI.controllers;

import it.polimi.ingsw.am56.network.message.classesDTO.BoardDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.CurrentPlayerDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.EndRoundSnapshotDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.GameSnapshotDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.PlayerStatsDTO;
import it.polimi.ingsw.am56.network.message.classesDTO.TotemPositionDTO;
import it.polimi.ingsw.am56.network.message.request.CardPickRequest;
import it.polimi.ingsw.am56.network.message.request.ReconnectRequest;
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
import it.polimi.ingsw.am56.view.TUI.TUIContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameController extends BaseTUIController {

    private static final long RENDER_DEBOUNCE_MS = 80;

    private final Object lock = new Object();

    private BoardDTO board;
    private final Map<String, PlayerStatsDTO> playerStats = new LinkedHashMap<>();
    private List<TotemPositionDTO> totems = new ArrayList<>();
    private CurrentPlayerDTO currentPlayer;
    private String currentPhase;
    private int currentEra;
    private int currentRound;
    private long stateVersion = 0;

    private final Object renderLock = new Object();
    private boolean dirty = false;

    private volatile boolean gameOver = false;
    private Thread inputThread;
    private Thread renderThread;

    public GameController(TUIContext context, GameSnapshotMessage initialSnapshot) {
        super(context);
        applySnapshot(initialSnapshot.snapshot());
    }

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
    // Server-push handlers — only mutate state + signal renderer + bump version.
    // ------------------------------------------------------------------

    @Override
    public void visit(GameSnapshotMessage message) {
        synchronized (lock) {
            applySnapshot(message.snapshot());
            bumpAndNotify();
        }
        markDirty();
    }

    @Override
    public void visit(BoardUpdatedMessage message) {
        synchronized (lock) {
            this.board = message.board();
            bumpAndNotify();
        }
        markDirty();
    }

    @Override
    public void visit(PlayerStatsUpdateMessage message) {
        synchronized (lock) {
            playerStats.put(message.stats().nickname(), message.stats());
            bumpAndNotify();
        }
        markDirty();
    }

    @Override
    public void visit(TotemsStateMessage message) {
        synchronized (lock) {
            this.totems = message.totems() != null
                    ? new ArrayList<>(message.totems())
                    : new ArrayList<>();
            bumpAndNotify();
        }
        markDirty();
    }

    @Override
    public void visit(CurrentPlayerUpdateMessage message) {
        synchronized (lock) {
            this.currentPlayer = message.currentPlayer();
            bumpAndNotify();
        }
        markDirty();
    }

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
            bumpAndNotify();
        }
        markDirty();
    }

    @Override
    public void visit(EventResolvedMessage message) {
        drawer.printEventNotice(message.cardId());
    }

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

    @Override
    public void handleDisconnect() {
        drawer.printReconnecting();
    }

    @Override
    public void handleReconnected() {
        drawer.printReconnected();
        context.getServerAdapter().sendRequest(new ReconnectRequest(context.getLocalNickname()));
    }

    // ------------------------------------------------------------------
    // Render thread — coalesces bursts of messages into one redraw.
    // ------------------------------------------------------------------

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

    private void markDirty() {
        synchronized (renderLock) {
            dirty = true;
            renderLock.notifyAll();
        }
    }

    // ------------------------------------------------------------------
    // Input loop (runs on TUI-input thread)
    // ------------------------------------------------------------------

    private void runInputLoop() {
        try {
            while (!gameOver) {
                synchronized (lock) {
                    while (!gameOver && (!isLocalTurn() || !isActionablePhase())) {
                        lock.wait();
                    }
                    if (gameOver) return;
                }

                Thread.sleep(RENDER_DEBOUNCE_MS + 50);

                long versionAtPrompt;
                String phaseAtPrompt;
                synchronized (lock) {
                    if (!isLocalTurn() || !isActionablePhase()) {
                        continue;
                    }
                    versionAtPrompt = stateVersion;
                    phaseAtPrompt = currentPhase;
                }

                if (phaseAtPrompt.contains("TotemPlacement")) {
                    promptTotemMove();
                } else if (phaseAtPrompt.contains("CardPicking")) {
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
            drawer.printError("Nessuna tessera offerta disponibile.");
            return;
        }

        drawer.printPrompt("Posiziona il tuo totem su una tessera offerta libera");
        String letter = inputReader.readLetterFrom(allowed, "  Scegli la lettera");
        context.getServerAdapter().sendRequest(
                new TotemMoveRequest(context.getLocalNickname(), letter));
    }

    private void promptCardPick() {
        Set<Integer> allowed = new HashSet<>();
        synchronized (lock) {
            PlayerStatsDTO me = playerStats.get(context.getLocalNickname());
            if (me == null || board == null) {
                drawer.printError("Stato del giocatore non disponibile.");
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

        if (allowed.isEmpty()) {
            drawer.printError("Non hai carte pescabili al momento.");
            return;
        }

        drawer.printPrompt("Scegli una carta da pescare dalla plancia");
        int cardId = inputReader.readIntFromSet(allowed, "  ID carta");
        context.getServerAdapter().sendRequest(
                new CardPickRequest(context.getLocalNickname(), cardId));
    }

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

    private void renderAll() {
        BoardDTO boardCopy;
        List<TotemPositionDTO> totemsCopy;
        CurrentPlayerDTO currentCopy;
        String phaseCopy;
        int eraCopy, roundCopy;
        List<PlayerStatsDTO> playersCopy;
        synchronized (lock) {
            boardCopy = this.board;
            totemsCopy = new ArrayList<>(this.totems);
            currentCopy = this.currentPlayer;
            phaseCopy = this.currentPhase;
            eraCopy = this.currentEra;
            roundCopy = this.currentRound;
            playersCopy = new ArrayList<>(this.playerStats.values());
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
        for (PlayerStatsDTO p : playersCopy) {
            if (p.nickname().equals(me)) continue;
            drawer.printPlayerDashboard(p, false, isCurrent(p.nickname(), currentCopy));
        }
    }

    private String phaseLabel(String phase) {
        if (phase == null) return "?";
        if (phase.contains("TotemPlacement")) return "Posizionamento Totem";
        if (phase.contains("CardPicking")) return "Pescaggio Carte";
        if (phase.contains("RoundEnding")) return "Fine Round";
        if (phase.contains("GameEnding")) return "Fine Partita";
        if (phase.contains("PlayersFilling")) return "Attesa Giocatori";
        return phase;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

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

    private boolean isLocalTurn() {
        return currentPlayer != null
                && context.getLocalNickname() != null
                && context.getLocalNickname().equals(currentPlayer.nickname());
    }

    private boolean isActionablePhase() {
        return currentPhase != null
                && (currentPhase.contains("TotemPlacement") || currentPhase.contains("CardPicking"));
    }

    private boolean isCurrent(String nickname, CurrentPlayerDTO current) {
        return current != null && nickname.equals(current.nickname());
    }

    private boolean isLetterOccupied(String letter, List<TotemPositionDTO> totemsSnap) {
        for (TotemPositionDTO t : totemsSnap) {
            if ("OFFER".equals(t.locationType()) && letter.equalsIgnoreCase(t.locationId())) {
                return true;
            }
        }
        return false;
    }

    private void bumpAndNotify() {
        stateVersion++;
        lock.notifyAll();
    }

    private void sleepBriefly(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
