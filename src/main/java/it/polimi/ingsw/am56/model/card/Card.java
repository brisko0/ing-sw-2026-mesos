package it.polimi.ingsw.am56.model.card;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;

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

    public void addCardToPlayer(Player player){}

    public int getCardId() {
        return cardId;
    }

    public int getEra() {
        return era;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public abstract CardType getCardType();
}