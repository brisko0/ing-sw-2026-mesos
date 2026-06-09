package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;


/**
 * Inventor character card. On pick, adds an invention identifier to the current player's
 * set of inventions, which is read at end-of-game scoring.
 */
public class InventorCharCard extends CharacterCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String invention;

    /**
     * Creates an inventor character card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param minPlayers minimum player count required for this card to be in the deck
     * @param invention  identifier of the invention this card carries
     */
    public InventorCharCard(int cardId, int era, int minPlayers, String invention) {
        super( cardId, era, minPlayers);
        this.invention = invention;
    }

    /**
     * Always returns {@link CharacterType#INVENTOR}.
     *
     * @return {@link CharacterType#INVENTOR}
     */
    @Override
    public CharacterType getCharacterType() {
        return CharacterType.INVENTOR;
    }

    /**
     * Appends this card's invention identifier to the current player's inventions collection.
     *
     * @param gameModel current game model
     */
    @Override
    public void resolveEffect(GameModel gameModel){

        gameModel.getCurrentPlayer().getStats().getInventions().add(invention);

    }
}
