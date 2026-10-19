package it.polimi.ingsw.am56.network.message.update;

/**
 * Visitor for handling {@link ServerMessage}s on the client side, with one {@code visit} overload
 * per concrete message type. A message dispatches itself here through its
 * {@link ServerMessage#accept(MessageVisitor)} method.
 * <p>
 * Every overload has an empty default body, so a client only needs to override the messages it
 * actually cares about; unhandled message types are silently ignored.
 */
public interface MessageVisitor {
    /** Handles the pre-lobby state (what nicknames/colours are taken, whether a game exists). */
    default void visit(PreLobbyStateMessage message) {}
    /** Handles a lobby update (players currently joined and the target game size). */
    default void visit(LobbyStateMessage message) {}
    /** Handles a board update sent after the board changed (e.g. a card was picked). */
    default void visit(BoardUpdatedMessage message) {}
    /** Handles an update to a single player's in-game stats. */
    default void visit(PlayerStatsUpdateMessage message) {}
    /** Handles an update to the totem positions on the board. */
    default void visit(TotemsStateMessage message) {}
    /** Handles a change of the player whose turn it is. */
    default void visit(CurrentPlayerUpdateMessage message) {}
    /** Handles a full game snapshot (game start or resync). */
    default void visit(GameSnapshotMessage message) {}
    /** Handles a change of the current game phase. */
    default void visit(GameStateUpdateMessage message) {}
    /** Handles the end-of-round snapshot. */
    default void visit(EndRoundUpdateMessage message) {}
    /** Handles the resolution of an Event card and its per-player outcomes. */
    default void visit(EventResolvedMessage message) {}
    /** Handles the end of the game, carrying the final rankings. */
    default void visit(GameEndedMessage message) {}
    /** Handles a game aborted because a player disconnected. */
    default void visit(GameAbortedMessage message) {}
    /** Handles an error reported for a rejected action. */
    default void visit(ErrorMessage message) {}
    /** Handles a heartbeat ping; carries no information and is ignored by every client. */
    default void visit(PingMessage message) {}
}