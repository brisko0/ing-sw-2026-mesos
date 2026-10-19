package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AddPointsEndGameTest {

    private GameModel gameModel;
    private AddPointsEndGame addPointsEndGame;
    private ArtistEventFoodBonus artistEventFoodBonus;
    private FeedingSaleCharType feedingSaleCharType;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            players.add(new Player("a", "r"));
            gameModel.getPlayers().add(players.get(i));
        }
        addPointsEndGame = new AddPointsEndGame(1, 1, 1, 1, 0, 25);
        artistEventFoodBonus = new ArtistEventFoodBonus(1, 1, 1, 1, 0);
        feedingSaleCharType = new FeedingSaleCharType(1, 1, 1, 1, 0, CharacterType.HUNTER);
    }

    @Test
    void shouldAddBonusIfCardOwned(){
        gameModel.getPlayers().get(0).getStats().getBuildingCards().add(artistEventFoodBonus);
        gameModel.getPlayers().get(1).getStats().getBuildingCards().add(addPointsEndGame);
        gameModel.getPlayers().get(2).getStats().getBuildingCards().add(feedingSaleCharType);

        for (Player p : gameModel.getPlayers()) {
            assertEquals(0, p.getStats().getPoints());
        }

        for (Player p : gameModel.getPlayers()) {
            for (BuildingCard b : p.getStats().getBuildingCards()) {
                b.onGameEnding(gameModel, p);
            }
        }

        assertEquals(0, gameModel.getPlayers().get(0).getStats().getPoints());
        assertEquals(25, gameModel.getPlayers().get(1).getStats().getPoints());
        assertEquals(0, gameModel.getPlayers().get(2).getStats().getPoints());


    }
}