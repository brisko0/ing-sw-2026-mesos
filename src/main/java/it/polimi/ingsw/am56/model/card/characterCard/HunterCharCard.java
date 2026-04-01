package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

public class HunterCharCard extends CharacterCard {
    private final boolean providesFood;

    public HunterCharCard(int cardId, int era, int minPlayers, boolean providesFood) {
        super( cardId, era, minPlayers);
        this.providesFood = providesFood;
    }

    @Override
    public void resolveEffect(GameModel gameModel){

        gameModel.getCurrentPlayer().getStats().addHunter();

        if(providesFood){
            gameModel.getCurrentPlayer().getStats().addFood(
                    gameModel.getCurrentPlayer().getStats().getHuntersNum());
        }
    }
}
