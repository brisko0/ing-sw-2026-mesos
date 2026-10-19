package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodPerSetTest {
    private GameModel gameModel;
    private FoodPerSet foodPerSet;
    private InventorCharCard inventorCharCard;
    private ArtistCharCard artistCharCard;
    private BuilderCharCard builderCharCard;
    private CollectorCharCard collectorCharCard;
    private ShamanCharCard shamanCharCard;
    private ShamanCharCard shamanCharCard2;
    private HunterCharCard hunterCharCard;


    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        for (int i = 0; i < 1; i++) {
            gameModel.getPlayers().add(new Player("a", "r"));
        }
        gameModel.setCurrentPlayer(gameModel.getPlayers().getFirst());
        foodPerSet = new FoodPerSet(1, 1, 1, 1, 0, 3);
        inventorCharCard = new InventorCharCard(1, 1, 1, "PANE");
        artistCharCard = new ArtistCharCard(1, 1, 1);
        builderCharCard = new BuilderCharCard(1, 1, 1, 1, 1);
        collectorCharCard = new CollectorCharCard(1, 1, 1, 1);
        shamanCharCard = new ShamanCharCard(1, 1, 1, 1);
        shamanCharCard2 = new ShamanCharCard(1, 1, 1, 1);
        hunterCharCard = new HunterCharCard(1, 1, 1, true);
    }

    @Test
    public void shouldAddFood() {
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(inventorCharCard);
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(artistCharCard);
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(builderCharCard);
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(collectorCharCard);
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(shamanCharCard);

        gameModel.getCurrentPlayer().getStats().addFood(1);
        assertEquals(1, gameModel.getCurrentPlayer().getStats().getFood());

        foodPerSet.resolveEffect(gameModel);

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getFood());

        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(hunterCharCard);
        foodPerSet.onCardPicked(gameModel, hunterCharCard);

        assertEquals(3, gameModel.getCurrentPlayer().getStats().getFood());
    }

    @Test
    public void shouldNotAddFood() {
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(inventorCharCard);
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(artistCharCard);
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(builderCharCard);
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(collectorCharCard);
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(shamanCharCard);

        gameModel.getCurrentPlayer().getStats().addFood(1);
        assertEquals(1, gameModel.getCurrentPlayer().getStats().getFood());

        foodPerSet.resolveEffect(gameModel);

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getFood());

        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(shamanCharCard2);
        foodPerSet.onCardPicked(gameModel, shamanCharCard2);

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getFood());
    }
}