package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DoubleBuildersPointsTest {

    private GameModel gameModel;
    private ArtistEventFoodBonus artistEventFoodBonus;
    private DoubleBuildersPoints doubleBuildersPoints;

    @BeforeEach
    void setUp() throws InvalidActionException {
        gameModel = new GameModel();
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            players.add(new Player("a", "r"));
            gameModel.getPlayers().add(players.get(i));
        }
        artistEventFoodBonus = new ArtistEventFoodBonus(1, 1, 1, 1, 0);
        doubleBuildersPoints = new DoubleBuildersPoints(1, 1, 1, 1, 0);
    }

    @Test
    void shouldAddDoubleBuilderPoints(){
        Player p1 = gameModel.getPlayers().get(0);
        Player p2 = gameModel.getPlayers().get(1);
        p1.getStats().getBuildingCards().add(doubleBuildersPoints);
        p2.getStats().getBuildingCards().add(artistEventFoodBonus);
        p1.getStats().addBuilderPoints(6);

        for (Player p : gameModel.getPlayers()) {
            assertEquals(0, p.getStats().getPoints());
        }

        for (Player p : gameModel.getPlayers()) {
            for (BuildingCard b : p.getStats().getBuildingCards()) {
                b.onGameEnding(gameModel, p);
            }
        }

        assertEquals(6, p1.getStats().getPoints());
        assertEquals(0, p2.getStats().getPoints());


    }

}