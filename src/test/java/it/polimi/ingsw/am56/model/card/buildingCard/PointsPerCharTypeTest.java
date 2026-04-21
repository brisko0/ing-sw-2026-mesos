package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.ArtistCharCard;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;
import it.polimi.ingsw.am56.model.card.characterCard.HunterCharCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointsPerCharTypeTest {

    private GameModel gameModel;
    private PointsPerCharType pointsPerCharTypeArtists;
    private PointsPerCharType pointsPerCharTypeHunters;
    private ArtistCharCard artistCharCard;
    private ArtistCharCard artistCharCard2;
    private HunterCharCard hunterCharCard;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        for (int i = 0; i < 2; i++) {
            gameModel.getPlayers().add(new Player("a", "r"));
        }
        pointsPerCharTypeArtists = new PointsPerCharType(1, 1, 1, 1, 1, CharacterType.ARTIST, 2);
        pointsPerCharTypeHunters = new PointsPerCharType(1, 1, 1, 1, 1, CharacterType.HUNTER, 1);
        artistCharCard = new ArtistCharCard(1, 1, 1);
        artistCharCard2 = new ArtistCharCard(1, 1, 1);
        hunterCharCard = new HunterCharCard(1, 1, 1, true);
    }

    @Test
    public void shouldAddPoints() {
        gameModel.getPlayers().getFirst().getStats().getCharacterCards().add(artistCharCard);
        gameModel.getPlayers().getFirst().getStats().getCharacterCards().add(artistCharCard2);
        gameModel.getPlayers().getFirst().getStats().getBuildingCards().add(pointsPerCharTypeArtists);

        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(hunterCharCard);
        gameModel.getPlayers().get(1).getStats().getBuildingCards().add(pointsPerCharTypeHunters);

        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(0, gameModel.getPlayers().get(1).getStats().getPoints());

        for (Player player : gameModel.getPlayers()) {
            for( BuildingCard buildingCard : player.getStats().getBuildingCards()) {
                buildingCard.onGameEnding(gameModel, player);
            }
        }

        assertEquals(5, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(2, gameModel.getPlayers().get(1).getStats().getPoints());
    }

    @Test
    public void shouldNotAddPoints() {
        gameModel.getPlayers().getFirst().getStats().getCharacterCards().add(artistCharCard);
        gameModel.getPlayers().getFirst().getStats().getCharacterCards().add(artistCharCard2);
        gameModel.getPlayers().getFirst().getStats().getBuildingCards().add(pointsPerCharTypeHunters);

        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(hunterCharCard);
        gameModel.getPlayers().get(1).getStats().getBuildingCards().add(pointsPerCharTypeArtists);

        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(0, gameModel.getPlayers().get(1).getStats().getPoints());

        for (Player player : gameModel.getPlayers()) {
            for( BuildingCard buildingCard : player.getStats().getBuildingCards()) {
                buildingCard.onGameEnding(gameModel, player);
            }
        }

        assertEquals(1, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(1, gameModel.getPlayers().get(1).getStats().getPoints());
    }
}