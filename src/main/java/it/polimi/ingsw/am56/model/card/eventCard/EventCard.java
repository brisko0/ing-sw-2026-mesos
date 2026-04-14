package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;

public abstract class EventCard extends Card {

    public EventCard(int cardId, int era, int minPlayers) {
        super(cardId, era, minPlayers);
    }

    public abstract EvType getEventType();

    @Override
    public void resolveBuildingsEffects(GameModel gameModel) {
        for(Player p : gameModel.getPlayers()){
            for(BuildingCard buildingCard : p.getStats().getBuildingCards()){
                buildingCard.onEventResolving(gameModel, this, p);
            }
        }
    }

    @Override
    public CardType getCardType(){
        return CardType.EVENT;
    }
}
