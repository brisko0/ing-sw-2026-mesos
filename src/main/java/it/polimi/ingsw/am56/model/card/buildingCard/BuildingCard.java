package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;

import java.io.Serial;

public abstract class BuildingCard extends Card {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int cost;
    private final int points;

    public BuildingCard(int cardId, int era, int minPlayers, int cost, int points) {
        super(cardId,era, minPlayers);
        this.cost = cost;
        this.points = points;
    }

    @Override
    public int  getCost() {
        return cost;
    }

    public void onCardPicked(GameModel gameModel, CharacterCard c) {}

    public void onEventResolving(GameModel gameModel, EventCard evCard, Player player) {}

    public void onOwnTurnEnding(GameModel gameModel) {}

    public void onGameEnding(GameModel gameModel, Player player) {
        player.getStats().addPoints(points);
    }

    @Override
    public void addCardToPlayer(Player player){
        player.getStats().getBuildingCards().add(this);
    }

    @Override
    public void resolveEffect(GameModel  gameModel) {
        gameModel.getCurrentPlayer().getStats().addFood(-Math.max(0, cost - gameModel.getCurrentPlayer().getStats().getBuildingSale()));
    }

    @Override
    public CardType getCardType() {
        return CardType.BUILDING;
    }
}
