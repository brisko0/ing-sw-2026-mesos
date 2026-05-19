package it.polimi.ingsw.am56.model.card.characterCard;


import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;


public class CollectorCharCard extends CharacterCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int feedingSale;

    public CollectorCharCard(int cardId, int era, int minPlayers, int feedingSale) {
        super(cardId, era, minPlayers);
        this.feedingSale = feedingSale;
    }

    @Override
    public CharacterType getCharacterType() {
        return CharacterType.COLLECTOR;
    }

    @Override
    public void resolveEffect(GameModel gameModel){

        gameModel.getCurrentPlayer().getStats().addFeedingSale(feedingSale);

    }
}
