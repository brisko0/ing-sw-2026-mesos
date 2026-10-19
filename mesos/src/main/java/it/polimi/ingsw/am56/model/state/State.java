package it.polimi.ingsw.am56.model.state;

/**
 * Discriminator identifying which phase of the game the {@link GameState} state machine is
 * currently in. The flow is:
 * <ol>
 *   <li>{@link #PLAYERS_FILLING} — lobby; waiting for all players to join,</li>
 *   <li>{@link #TOTEM_PLACEMENT} — each player picks an offer tile in turn order,</li>
 *   <li>{@link #CARD_PICKING} — each player resolves their offer tile by picking cards,</li>
 *   <li>{@link #EXTRA_UPPER_CARD_PICKING} — eligible players resolve extra upper-row card picks,</li>
 *   <li>{@link #ROUND_ENDING} — end-of-round bookkeeping; loops back to totem placement until round 11,</li>
 *   <li>{@link #GAME_ENDING} — terminal; end-of-game scoring runs here.</li>
 * </ol>
 */
public enum State {
    /** Lobby phase, waiting for the configured number of players. */
    PLAYERS_FILLING,
    /** Players are placing their totems on offer tiles in turn order. */
    TOTEM_PLACEMENT,
    /** Players are picking cards in offer-tile order. */
    CARD_PICKING,
    /** Eligible players are picking extra cards from the upper row after normal card picking. */
    EXTRA_UPPER_CARD_PICKING,
    /** End-of-round bookkeeping is being performed. */
    ROUND_ENDING,
    /** Terminal phase; end-of-game scoring has run. */
    GAME_ENDING
}
