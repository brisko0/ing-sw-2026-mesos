package it.polimi.ingsw.am56.model.card;

import it.polimi.ingsw.am56.model.GameModel;

public abstract class Card {
    private final int cardId;
    private final int era;
    private final int minPlayers;

    public Card(int cardId, int era, int minPlayers) {
        this.cardId = cardId;
        this.era = era;
        this.minPlayers = minPlayers;
    }

    public abstract void resolveEffect(GameModel gameModel);

    public int getEra() {
        return era;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public abstract CardType getCardType();
}