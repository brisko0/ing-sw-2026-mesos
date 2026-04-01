package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

public class InventorCharCard extends CharacterCard {
    private final String invention;

    public InventorCharCard(int cardId, int era, int minPlayers, String invention) {
        super( cardId, era, minPlayers);
        this.invention = invention;
    }

    @Override
    public void resolveEffect(GameModel gameModel){

        gameModel.getCurrentPlayer().getStats().getInventions().add(invention);
        gameModel.getCurrentPlayer().getStats().addInventor();
    }
}