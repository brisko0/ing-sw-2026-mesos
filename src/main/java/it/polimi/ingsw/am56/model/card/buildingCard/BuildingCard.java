package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;

public abstract class BuildingCard extends Card {
    private final int cost;
    private final int points;

    public BuildingCard(int cardId, int era, int numPlayers, int cost, int points) {
        super(cardId,era, numPlayers);
        this.cost = cost;
        this.points = points;
    }
    public void onCardPicked(GameModel gameModel, CharacterCard characterCard){}
    @Override
    public void resolveEffect(GameModel  gameModel) {
        gameModel.getCurrentPlayer().getStats().addFood(-Math.max(0, cost - gameModel.getCurrentPlayer().getStats().getBuildingSale()));
    }

    @Override
    public CardType getCardType() {
        return CardType.BUILDING;
    }
}
