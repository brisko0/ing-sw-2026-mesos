package it.polimi.ingsw.am56.model;

import it.polimi.ingsw.am56.model.card.buildingCard.AddPointsEndGame;
import it.polimi.ingsw.am56.model.card.characterCard.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerStatsTest {

    private PlayerStats playerStats;

    private ArtistCharCard artistCharCard;
    private ShamanCharCard shamanCharCard;
    private InventorCharCard inventorCharCard;
    private BuilderCharCard builderCharCard;
    private HunterCharCard hunterCharCard;
    private CollectorCharCard collectorCharCard;

    private AddPointsEndGame addPointsEndGame;

    @BeforeEach
    void setUp(){
        playerStats = new PlayerStats();

        artistCharCard = new ArtistCharCard(1, 1, 1);
        shamanCharCard = new ShamanCharCard(1, 1, 1, 1);
        inventorCharCard = new InventorCharCard(1, 1, 1, "PANE");
        builderCharCard = new BuilderCharCard(1, 1, 1, 1, 1);
        hunterCharCard = new HunterCharCard(1, 1, 1, true);
        collectorCharCard = new CollectorCharCard(1, 1, 1, 3);

        addPointsEndGame = new AddPointsEndGame(1, 1, 1, 1, 1, 1);
    }

    @Test
    void foodTest() {
        assertEquals(0, playerStats.getFood());
        playerStats.addFood(2);
        assertEquals(2, playerStats.getFood());
    }

    @Test
    void pointsTest() {
        assertEquals(0, playerStats.getPoints());
        playerStats.addPoints(3);
        assertEquals(3, playerStats.getPoints());
    }

    @Test
    void shouldCountCharType() {
        playerStats.getCharacterCards().add(artistCharCard);
        playerStats.getCharacterCards().add(artistCharCard);

        playerStats.getCharacterCards().add(shamanCharCard);
        playerStats.getCharacterCards().add(shamanCharCard);
        playerStats.getCharacterCards().add(shamanCharCard);

        assertEquals(2, playerStats.getCharacterTypeNum(CharacterType.ARTIST));
        assertEquals(3, playerStats.getCharacterTypeNum(CharacterType.SHAMAN));
    }

    @Test
    void shamanStarsTest() {
        assertEquals(0, playerStats.getShamanStars());
        playerStats.addShamanStars(2);
        assertEquals(2, playerStats.getShamanStars());
    }

    @Test
    void feedingSaleTest() {
        assertEquals(0, playerStats.getFeedingSale());
        playerStats.addFeedingSale(3);
        assertEquals(3, playerStats.getFeedingSale());
    }

    @Test
    void buildingSaleTest() {
        assertEquals(0, playerStats.getBuildingSale());
        playerStats.addFeedingSale(5);
        assertEquals(5, playerStats.getFeedingSale());
    }

    @Test
    void builderPointsTest() {
        assertEquals(0, playerStats.getBuilderPoints());
        playerStats.addBuilderPoints(4);
        assertEquals(4, playerStats.getBuilderPoints());
    }

    @Test
    void buildingCardsTest() {
        assertEquals(0, playerStats.getBuildingCards().size());
        playerStats.getBuildingCards().add(addPointsEndGame);
        assertEquals(1, playerStats.getBuildingCards().size());
        assertTrue(playerStats.getBuildingCards().contains(addPointsEndGame));
    }

    @Test
    void testInventionsGetter() {
        assertEquals(0, playerStats.getInventions().size());

        playerStats.getInventions().add("PANE");
        playerStats.getInventions().add("PANE");
        playerStats.getInventions().add("CANOA");

        assertEquals(2, playerStats.getInventions().size());
        assertTrue(playerStats.getInventions().contains("PANE"));
        assertTrue(playerStats.getInventions().contains("CANOA"));
    }

    @Test
    void shouldCountSets() {

        assertEquals(0, playerStats.getCompletedSets());

        playerStats.getCharacterCards().add(artistCharCard);
        playerStats.getCharacterCards().add(shamanCharCard);
        playerStats.getCharacterCards().add(inventorCharCard);
        playerStats.getCharacterCards().add(hunterCharCard);
        playerStats.getCharacterCards().add(builderCharCard);

        assertEquals(0, playerStats.getCompletedSets());

        playerStats.getCharacterCards().add(collectorCharCard);

        assertEquals(1, playerStats.getCompletedSets());
    }

}