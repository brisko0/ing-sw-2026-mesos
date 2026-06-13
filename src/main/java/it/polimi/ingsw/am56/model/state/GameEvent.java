package it.polimi.ingsw.am56.model.state;

/**
 * Discriminator for the events the model broadcasts through its observer channel. Listeners
 * (views, network senders, persistence) subscribe to receive these and react accordingly.
 * Values are grouped roughly by the phase that emits them:
 * <ul>
 *   <li>lobby — {@link #LOBBY_STATE_SYNC}, {@link #PLAYER_ADDED}, {@link #GAME_STARTED},</li>
 *   <li>per-turn — {@link #TOTEM_PLACED}, {@link #TURN_CHANGED}, {@link #STATE_CHANGED},
 *       {@link #CARD_PICKED}, {@link #PLAYER_STATS_UPDATED}, {@link #TOTEM_RESET},
 *       {@link #EVENT_RESOLVED},</li>
 *   <li>round / era / game boundaries — {@link #ROUND_ENDED}, {@link #ERA_CHANGED},
 *       {@link #GAME_ENDED},</li>
 *   <li>connectivity — {@link #PLAYER_UPDATE}, {@link #PLAYER_DISCONNECTED}.</li>
 * </ul>
 */
public enum GameEvent {
    /** Full lobby snapshot sent to a newly connected client. */
    LOBBY_STATE_SYNC,
    /** A new player has joined the lobby. */
    PLAYER_ADDED,
    /** The lobby is full and the game has just started. */
    GAME_STARTED,
    /** A totem has been placed on an offer tile. */
    TOTEM_PLACED,
    /** The active player has changed. */
    TURN_CHANGED,
    /** The state machine has transitioned to a new {@link State}. */
    STATE_CHANGED,
    /** A card has been picked off the board. */
    CARD_PICKED,
    /** A player's stats (food, points, hand, …) have changed. */
    PLAYER_STATS_UPDATED,
    /** Totems have been returned to their owners' order tiles at the end of card picking. */
    TOTEM_RESET,
    /** An event card has resolved its effect. */
    EVENT_RESOLVED,

    /** A round has ended; bookkeeping has run. */
    ROUND_ENDED,
    /** The era has advanced. */
    ERA_CHANGED,
    /** The game has reached its terminal state and final scores are available. */
    GAME_ENDED,
    /** A player object has been updated wholesale (e.g. after reconnection). */
    PLAYER_UPDATE,
    /** A player has disconnected. */
    PLAYER_DISCONNECTED,
}
