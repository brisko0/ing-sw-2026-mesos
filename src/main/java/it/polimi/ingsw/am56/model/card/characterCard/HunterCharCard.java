package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;


/**
 * Hunter character card. When {@code providesFood} is true, picking the card immediately
 * grants the current player one food per hunter they own (including this one). Hunters also
 * score during the {@code HuntingEvCard} event regardless of this flag.
 */
public class HunterCharCard extends CharacterCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final boolean providesFood;

    /**
     * Creates a hunter character card.
     *
     * @param cardId       unique card id
     * @param era          era the card belongs to (1, 2 or 3)
     * @param minPlayers   minimum player count required for this card to be in the deck
     * @param providesFood whether picking the card grants the on-pick food bonus
     */
    public HunterCharCard(int cardId, int era, int minPlayers, boolean providesFood) {
        super( cardId, era, minPlayers);
        this.providesFood = providesFood;
    }

    /**
     * Always returns {@link CharacterType#HUNTER}.
     *
     * @return {@link CharacterType#HUNTER}
     */
    @Override
    public CharacterType getCharacterType() {
        return CharacterType.HUNTER;
    }

    /**
     * If {@code providesFood} is true, grants the current player one food per hunter they
     * now own; otherwise the pick has no immediate effect.
     *
     * @param gameModel current game model
     */
    @Override
    public void resolveEffect(GameModel gameModel){

        if(providesFood){
            gameModel.getCurrentPlayer().getStats().addFood(
                    gameModel.getCurrentPlayer().getStats().getCharacterTypeNum(CharacterType.HUNTER));
        }
    }
}
