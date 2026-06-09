package it.polimi.ingsw.am56.model.card;

/**
 * Discriminator for the three families of cards in the game.
 * <ul>
 *   <li>{@link #CHARACTER} — picked into a player's hand; see the {@code characterCard} package.</li>
 *   <li>{@link #EVENT} — drawn at end-of-round to apply a global effect; see the {@code eventCard} package.</li>
 *   <li>{@link #BUILDING} — purchased with food for ongoing or end-of-game effects; see the {@code buildingCard} package.</li>
 * </ul>
 */
public enum CardType {
    /** A character card. */
    CHARACTER,
    /** An event card. */
    EVENT,
    /** A building card. */
    BUILDING
}
