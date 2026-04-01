package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

public class ShamanCharCard extends CharacterCard{
    private final int shamanStars;

    public ShamanCharCard(int cardId, int era, int minPlayers, int shamanStars) {
        super( cardId, era, minPlayers);
        this.shamanStars = shamanStars;
    }

    @Override
    public void resolveEffect(GameModel gameModel){

        gameModel.getCurrentPlayer().getStats().addShamanStars(shamanStars);
    }
}