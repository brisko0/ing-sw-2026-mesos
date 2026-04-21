package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointsPerSetTest {

    private GameModel gameModel;
    private PointsPerSet pointsPerSet;
    private InventorCharCard inventorCharCard;
    private ArtistCharCard artistCharCard;
    private BuilderCharCard builderCharCard;
    private CollectorCharCard collectorCharCard;
    private ShamanCharCard shamanCharCard;
    private HunterCharCard hunterCharCard;

    private InventorCharCard inventorCharCard2;
    private ArtistCharCard artistCharCard2;
    private BuilderCharCard builderCharCard2;
    private CollectorCharCard collectorCharCard2;
    private ShamanCharCard shamanCharCard2;
    private HunterCharCard hunterCharCard2;


    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        for (int i = 0; i < 2; i++) {
            gameModel.getPlayers().add(new Player("a", "r"));
        }
        pointsPerSet = new PointsPerSet(1, 1, 1, 1, 1, 5);
        inventorCharCard = new InventorCharCard(2, 1, 1, "PANE");
        artistCharCard = new ArtistCharCard(3, 1, 1);
        builderCharCard = new BuilderCharCard(4, 1, 1, 1, 1);
        collectorCharCard = new CollectorCharCard(5, 1, 1, 1);
        shamanCharCard = new ShamanCharCard(6, 1, 1, 1);
        hunterCharCard = new HunterCharCard(7, 1, 1, true);

        inventorCharCard2 = new InventorCharCard(8, 1, 1, "PANE");
        artistCharCard2 = new ArtistCharCard(9, 1, 1);
        builderCharCard2 = new BuilderCharCard(10, 1, 1, 1, 1);
        collectorCharCard2 = new CollectorCharCard(11, 1, 1, 1);
        shamanCharCard2 = new ShamanCharCard(12, 1, 1, 1);
        hunterCharCard2 = new HunterCharCard(13, 1, 1, true);
    }

    @Test
    public void shouldAddPoints() {
        for(Player player : gameModel.getPlayers()) {
            player.getStats().getCharacterCards().add(inventorCharCard);
            player.getStats().getCharacterCards().add(artistCharCard);
            player.getStats().getCharacterCards().add(builderCharCard);
            player.getStats().getCharacterCards().add(collectorCharCard);
            player.getStats().getCharacterCards().add(shamanCharCard);
            player.getStats().getCharacterCards().add(hunterCharCard);
            player.getStats().getBuildingCards().add(pointsPerSet);
            assertEquals(0, player.getStats().getPoints());
        }

        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(inventorCharCard2);
        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(artistCharCard2);
        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(builderCharCard2);
        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(collectorCharCard2);
        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(hunterCharCard2);
        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(shamanCharCard2);


        for (Player player : gameModel.getPlayers()) {
            for(BuildingCard buildingCard : player.getStats().getBuildingCards()) {
                buildingCard.onGameEnding(gameModel, player);
            }
        }

        assertEquals(6, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(11, gameModel.getPlayers().get(1).getStats().getPoints());
    }

    @Test
    public void shouldNotAddPoints() {
        for(Player player : gameModel.getPlayers()) {
            player.getStats().getCharacterCards().add(inventorCharCard);
            player.getStats().getCharacterCards().add(artistCharCard);
            player.getStats().getCharacterCards().add(builderCharCard);
            player.getStats().getCharacterCards().add(collectorCharCard);
            player.getStats().getCharacterCards().add(shamanCharCard);
            assertEquals(0, player.getStats().getPoints());
        }

        gameModel.getPlayers().getFirst().getStats().getCharacterCards().add(hunterCharCard);
        gameModel.getPlayers().get(1).getStats().getBuildingCards().add(pointsPerSet);

        for (Player player : gameModel.getPlayers()) {
            for(BuildingCard buildingCard : player.getStats().getBuildingCards()) {
                buildingCard.onGameEnding(gameModel, player);
            }
        }

        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(1, gameModel.getPlayers().get(1).getStats().getPoints());
    }

}