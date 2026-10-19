package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;

/**
 * Shaman character card. On pick, adds {@code shamanStars} to the current player's
 * shaman-stars counter, which is compared across players during {@code ShamansEvCard}
 * resolution and at end-of-game scoring.
 */
public class ShamanCharCard extends CharacterCard{
    @Serial
    private static final long serialVersionUID = 1L;

    private final int shamanStars;

    /**
     * Creates a shaman character card.
     *
     * @param cardId      unique card id
     * @param era         era the card belongs to (1, 2 or 3)
     * @param minPlayers  minimum player count required for this card to be in the deck
     * @param shamanStars shaman-stars added to the picker's counter
     */
    public ShamanCharCard(int cardId, int era, int minPlayers, int shamanStars) {
        super( cardId, era, minPlayers);
        this.shamanStars = shamanStars;
    }

    /**
     * Always returns {@link CharacterType#SHAMAN}.
     *
     * @return {@link CharacterType#SHAMAN}
     */
    @Override
    public CharacterType getCharacterType() {
        return CharacterType.SHAMAN;
    }

    /**
     * Adds {@code shamanStars} to the current player's shaman-stars counter.
     *
     * @param gameModel current game model
     */
    @Override
    public void resolveEffect(GameModel gameModel){

        gameModel.getCurrentPlayer().getStats().addShamanStars(shamanStars);

    }
}
