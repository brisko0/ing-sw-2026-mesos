package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.ArtistCharCard;
import it.polimi.ingsw.am56.model.card.characterCard.InventorCharCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodPerInventorPairTest {
    private GameModel gameModel;
    private FoodPerInventorPair foodPerInventorPair;
    private InventorCharCard inventorCharCard;
    private InventorCharCard inventorCharCard1;
    private InventorCharCard inventorCharCard2;
    private ArtistCharCard artistCharCard;


    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        for (int i = 0; i < 1; i++) {
            gameModel.getPlayers().add(new Player("a", "r"));
        }
        gameModel.setCurrentPlayer(gameModel.getPlayers().getFirst());
        foodPerInventorPair = new FoodPerInventorPair(1, 1, 1, 1, 0, 3);
        inventorCharCard = new InventorCharCard(1, 1, 1, "PANE");
        inventorCharCard1 = new InventorCharCard(1, 1, 1, "COLLANA");
        inventorCharCard2 = new InventorCharCard(1, 1, 1, "COLLANA");
        artistCharCard = new ArtistCharCard(1, 1, 1);
    }

    @Test
    public void shouldAddFood() {
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(inventorCharCard);
        inventorCharCard.resolveEffect(gameModel);
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(inventorCharCard1);
        inventorCharCard1.resolveEffect(gameModel);
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(artistCharCard);

        gameModel.getCurrentPlayer().getStats().addFood(1);

        assertEquals(1, gameModel.getCurrentPlayer().getStats().getFood());

        foodPerInventorPair.resolveEffect(gameModel);

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getFood());

        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(inventorCharCard2);
        inventorCharCard2.resolveEffect(gameModel);

        foodPerInventorPair.onCardPicked(gameModel, inventorCharCard2);

        assertEquals(3, gameModel.getCurrentPlayer().getStats().getFood());
    }

    @Test
    public void shouldNotAddFood() {
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(inventorCharCard);
        inventorCharCard.resolveEffect(gameModel);
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(artistCharCard);

        gameModel.getCurrentPlayer().getStats().addFood(1);

        assertEquals(1, gameModel.getCurrentPlayer().getStats().getFood());

        foodPerInventorPair.resolveEffect(gameModel);

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getFood());

        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(inventorCharCard1);
        inventorCharCard1.resolveEffect(gameModel);

        foodPerInventorPair.onCardPicked(gameModel, inventorCharCard1);

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getFood());
    }

}