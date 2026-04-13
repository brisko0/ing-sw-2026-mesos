package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;

public abstract class BuildingCard extends Card {
    private final int cost;
    private final int points;

    public BuildingCard(int cardId, int era, int numPlayers, int cost, int points) {
        super(cardId,era, numPlayers);
        this.cost = cost;
        this.points = points;
    }

    public void onCardPicked(GameModel gameModel, CharacterCard characterCard){}
    public void onEventResolving(GameModel gameModel, EventCard evCard, Player player) {}
    public void onOwnTurnEnding(GameModel gameModel) {}
    public void onGameEnding(GameModel gameModel, Player player) {
        player.getStats().addPoints(points);
    }

    public int getCost() {
        return cost;
    }

    @Override
    public void resolveEffect(GameModel  gameModel) {
        gameModel.getCurrentPlayer().getStats().addFood(-Math.max(0, cost - gameModel.getCurrentPlayer().getStats().getBuildingSale()));
    }

    @Override
    public void addCardToPlayer(Player player) {
        player.getStats().getBuildingCards().add(this);
    }

    @Override
    public CardType getCardType() {
        return CardType.BUILDING;
    }
}
