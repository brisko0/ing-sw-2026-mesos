package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;


public class HunterCharCard extends CharacterCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final boolean providesFood;

    public HunterCharCard(int cardId, int era, int minPlayers, boolean providesFood) {
        super( cardId, era, minPlayers);
        this.providesFood = providesFood;
    }

    @Override
    public CharacterType getCharacterType() {
        return CharacterType.HUNTER;
    }

    @Override
    public void resolveEffect(GameModel gameModel){

        if(providesFood){
            gameModel.getCurrentPlayer().getStats().addFood(
                    gameModel.getCurrentPlayer().getStats().getCharacterTypeNum(CharacterType.HUNTER));
        }
    }
}
