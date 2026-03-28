package it.polimi.ingsw.am56.model.card;

import it.polimi.ingsw.am56.model.GameModel;

public abstract class Card {
    private final int cardId;
    private final int era;
    private final int minPlayers;
    private final CardType type;

    public Card(int cardId, int era, int minPlayers,  CardType type) {
        this.cardId = cardId;
        this.era = era;
        this.minPlayers = minPlayers;
        this.type = type;
    }

    public abstract void resolveEffect(GameModel gameModel);

    public int getEra() {
        return era;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public CardType getCardType() { return type; }
}
