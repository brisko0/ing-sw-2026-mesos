package it.polimi.ingsw.am56.model.card;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.io.Serial;
import java.io.Serializable;

public abstract class Card implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;


    private final int cardId;
    private final int era;
    private final int minPlayers;

    public Card(int cardId, int era, int minPlayers) {
        this.cardId = cardId;
        this.era = era;
        this.minPlayers = minPlayers;
    }

    public abstract void resolveEffect(GameModel gameModel);

    public void addCardToPlayer(Player player) throws InvalidActionException {
        throw new InvalidActionException("Non è possibile pescare una carta evento!");
    }

    public abstract CardType getCardType();

    public int getEra() {
        return era;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void resolveBuildingsEffects(GameModel gameModel) {}

    public int getCost() {
        return 0;
    }

    public int getCardId() {
        return cardId;
    }
}
