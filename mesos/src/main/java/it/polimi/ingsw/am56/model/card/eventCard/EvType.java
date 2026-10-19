package it.polimi.ingsw.am56.model.card.eventCard;

/**
 * Discriminator for the four kinds of event cards in the game.
 * <ul>
 *   <li>{@link #ARTIST} — the "Pitture Rupestri" event, {@link ArtistsEvCard}.</li>
 *   <li>{@link #SHAMAN} — the shaman-stars comparison event, {@link ShamansEvCard}.</li>
 *   <li>{@link #SUSTENANCE} — the feed-your-tribe event, {@link SustenanceEvCard}.</li>
 *   <li>{@link #HUNTING} — the food / points per hunter event, {@link HuntingEvCard}.</li>
 * </ul>
 */
public enum EvType {
    /** Pitture Rupestri (artists) event. */
    ARTIST,
    /** Shaman-stars comparison event. */
    SHAMAN,
    /** Sustenance / feeding event. */
    SUSTENANCE,
    /** Hunting event. */
    HUNTING
}
