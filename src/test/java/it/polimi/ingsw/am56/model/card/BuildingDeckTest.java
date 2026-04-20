package it.polimi.ingsw.am56.model.card;

import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;
import it.polimi.ingsw.am56.model.factory.card.BuildingCardFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuildingDeckTest {
    private BuildingCardFactory factory;
    private BuildingDeck buildingDeck;


    @BeforeEach
    void setUp() {
        factory = new BuildingCardFactory();
        buildingDeck = new BuildingDeck(1, 1, factory.createCards(1, 1));
    }

    @Test
    void shouldHaveCreatedDeck() {

        assertFalse(buildingDeck.getRemainingCards().isEmpty());

        for(Card card : buildingDeck.getRemainingCards()) {
            assertEquals(CardType.BUILDING, card.getCardType());
            assertEquals(1, card.getEra());
        }
    }

    @Test
    void shouldDrawBuildingCard() {
        assertEquals(CardType.BUILDING, buildingDeck.drawCard().getCardType());
    }

}
