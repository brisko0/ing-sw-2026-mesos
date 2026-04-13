package it.polimi.ingsw.am56.model.factory.deck;

import it.polimi.ingsw.am56.model.card.Deck;
import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;
import it.polimi.ingsw.am56.model.factory.DeckFactory;
import it.polimi.ingsw.am56.model.factory.card.BuildingCardFactory;

import java.util.ArrayList;
import java.util.List;

public class BuildingDeckFactory implements DeckFactory {

    @Override
    public Deck createDeck(int era, int gameSize) {

        BuildingCardFactory buildingCardFactory = new BuildingCardFactory();

        List<BuildingCard> buildingCards = buildingCardFactory.createCards(era, gameSize);

        return new Deck(era, era, new ArrayList<>(buildingCards));
    }
}
