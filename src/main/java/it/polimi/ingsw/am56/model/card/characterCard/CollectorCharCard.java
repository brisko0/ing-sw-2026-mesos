package it.polimi.ingsw.am56.model.card.characterCard;


import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;


/**
 * Collector character card. On pick, reduces the number of mouths the player has to feed
 * during {@code SustenanceEvCard} resolution by {@code feedingSale}.
 */
public class CollectorCharCard extends CharacterCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int feedingSale;

    /**
     * Creates a collector character card.
     *
     * @param cardId      unique card id
     * @param era         era the card belongs to (1, 2 or 3)
     * @param minPlayers  minimum player count required for this card to be in the deck
     * @param feedingSale reduction applied to the player's feeding cost during sustenance events
     */
    public CollectorCharCard(int cardId, int era, int minPlayers, int feedingSale) {
        super(cardId, era, minPlayers);
        this.feedingSale = feedingSale;
    }

    /**
     * Always returns {@link CharacterType#COLLECTOR}.
     *
     * @return {@link CharacterType#COLLECTOR}
     */
    @Override
    public CharacterType getCharacterType() {
        return CharacterType.COLLECTOR;
    }

    /**
     * Adds {@code feedingSale} to the current player's feeding-discount counter.
     *
     * @param gameModel current game model
     */
    @Override
    public void resolveEffect(GameModel gameModel){

        gameModel.getCurrentPlayer().getStats().addFeedingSale(feedingSale);

    }
}
