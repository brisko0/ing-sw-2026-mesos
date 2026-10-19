package it.polimi.ingsw.am56.model.card.characterCard;

/**
 * The six character archetypes a player can collect during the game. Each value is paired
 * with its concrete {@link CharacterCard} subclass.
 */
public enum CharacterType {
    /** Artist — counts toward Pitture Rupestri scoring; see {@link ArtistCharCard}. */
    ARTIST,
    /** Hunter — provides food at pick time and during the hunting event; see {@link HunterCharCard}. */
    HUNTER,
    /** Builder — grants a building-cost discount and end-of-game builder points; see {@link BuilderCharCard}. */
    BUILDER,
    /** Inventor — contributes a unique invention string to end-of-game scoring; see {@link InventorCharCard}. */
    INVENTOR,
    /** Shaman — accumulates shaman stars compared during the shaman event; see {@link ShamanCharCard}. */
    SHAMAN,
    /** Collector — grants a feeding-cost discount on sustenance events; see {@link CollectorCharCard}. */
    COLLECTOR
}
