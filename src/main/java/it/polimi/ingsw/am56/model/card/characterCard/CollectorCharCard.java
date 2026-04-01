package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

public class CollectorCharCard extends CharacterCard {
    private final int feedingSale;

    public CollectorCharCard(int cardId, int era, int minPlayers, int feedingSale) {
        super(cardId, era, minPlayers);
        this.feedingSale = feedingSale;
    }

    @Override
    public void resolveEffect(GameModel gameModel){

        gameModel.getCurrentPlayer().getStats().addFeedingSale(feedingSale);
    }
}