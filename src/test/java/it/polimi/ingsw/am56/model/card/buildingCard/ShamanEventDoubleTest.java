package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.eventCard.ArtistsEvCard;
import it.polimi.ingsw.am56.model.card.eventCard.ShamansEvCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShamanEventDoubleTest {
    private GameModel gameModel;
    private ShamanEventDouble shamanEventDouble;
    private ShamansEvCard shamansEvCard;
    private ArtistsEvCard artistsEvCard;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        gameModel.getPlayers().add(new Player("a", "a"));
        gameModel.getPlayers().add(new Player("a", "a"));
        shamanEventDouble = new ShamanEventDouble(1, 1, 1, 1, 1);
        shamansEvCard = new ShamansEvCard(1, 1, 1, 5, 0);
        artistsEvCard = new ArtistsEvCard(1, 1, 1, 1, 1, 1);
    }

    @Test
    public void shouldWinDouble() {
        gameModel.getPlayers().getFirst().getStats().addShamanStars(2);

        gameModel.getPlayers().getFirst().getStats().getBuildingCards().add(shamanEventDouble);

        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getPoints());

        assertEquals(0, gameModel.getPlayers().get(1).getStats().getShamanStars());

        shamansEvCard.resolveEffect(gameModel);

        assertEquals(5, gameModel.getPlayers().getFirst().getStats().getPoints());

        for(Player player : gameModel.getPlayers()) {
            for(BuildingCard buildingCard : player.getStats().getBuildingCards()) {
                buildingCard.onEventResolving(gameModel, shamansEvCard, player);
            }
        }

        assertEquals(10, gameModel.getPlayers().getFirst().getStats().getPoints());
    }

    @Test
    public void shouldNotApplyEffect() {

        gameModel.getPlayers().getFirst().getStats().getBuildingCards().add(shamanEventDouble);

        gameModel.getPlayers().get(1).getStats().addShamanStars(3);

        assertEquals(0, gameModel.getPlayers().get(1).getStats().getPoints());

        for(Player player : gameModel.getPlayers()) {
            for(BuildingCard buildingCard : player.getStats().getBuildingCards()) {
                buildingCard.onEventResolving(gameModel, shamansEvCard, player);
            }
        }

        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getPoints());
    }

    @Test
    public void shouldNotResolveEffect() {

        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getPoints());

        gameModel.getPlayers().getFirst().getStats().getBuildingCards().add(shamanEventDouble);

        for(Player player : gameModel.getPlayers()) {
            for(BuildingCard buildingCard : player.getStats().getBuildingCards()) {
                buildingCard.onEventResolving(gameModel, artistsEvCard, player);
            }
        }

        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getPoints());
    }
}