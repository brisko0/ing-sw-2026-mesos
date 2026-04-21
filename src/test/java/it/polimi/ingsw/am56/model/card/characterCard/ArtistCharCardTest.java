package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.PlayerStats;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.card.buildingCard.FoodPerInventorPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArtistCharCardTest {
    private GameModel gameModel;
    private ArtistCharCard artistCharCard;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        gameModel.getPlayers().add(new Player("a", "a"));
        artistCharCard = new ArtistCharCard(1, 1, 1);
    }

    @Test
    void shouldReturnCorrectCharType() {
        assertEquals(CharacterType.ARTIST,  artistCharCard.getCharacterType());
    }

    @Test
    void shouldReturnCorrectEvType() {
        PlayerStats initialStats = gameModel.getPlayers().getFirst().getStats();

        artistCharCard.resolveEffect(gameModel);

        assertEquals(initialStats, gameModel.getPlayers().getFirst().getStats());
    }

    // test per classe abstract CharacterCard

    @Test
    void shouldReturnCorrectCardType() {
        assertEquals(CardType.CHARACTER, artistCharCard.getCardType());
    }

    @Test
    void shouldAddCardToPlayer() {
        artistCharCard.addCardToPlayer(gameModel.getPlayers().getFirst());
        assertTrue(gameModel.getPlayers().getFirst().getStats().getCharacterCards().contains(artistCharCard));
    }

    @Test
    void shouldResolveBuildingEffect() {
        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getFood());
        gameModel.setCurrentPlayer(gameModel.getPlayers().getFirst());

        InventorCharCard inventor1 = new InventorCharCard(1, 1, 1, "PANE");
        InventorCharCard inventor2 = new InventorCharCard(2, 1, 1, "PANE");
        FoodPerInventorPair foodPerInventorPair = new FoodPerInventorPair(3, 1, 1, 0, 1, 3);

        foodPerInventorPair.addCardToPlayer(gameModel.getCurrentPlayer());
        foodPerInventorPair.resolveEffect(gameModel);

        inventor1.addCardToPlayer(gameModel.getCurrentPlayer());
        inventor1.resolveEffect(gameModel);
        inventor1.resolveBuildingsEffects(gameModel);

        inventor2.addCardToPlayer(gameModel.getCurrentPlayer());
        inventor2.resolveEffect(gameModel);
        inventor2.resolveBuildingsEffects(gameModel);

        assertEquals(3, gameModel.getPlayers().getFirst().getStats().getFood());
    }

}