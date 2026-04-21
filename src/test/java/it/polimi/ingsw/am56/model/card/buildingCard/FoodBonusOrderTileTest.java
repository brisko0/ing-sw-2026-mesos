package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OrderTile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FoodBonusOrderTileTest {

    private GameModel gameModel;
    private FoodBonusOrderTile foodBonusOrderTile;
    private OrderTile orderTile1;
    private OrderTile orderTile2;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        for (int i = 0; i < 2; i++) {
            gameModel.getPlayers().add(new Player("a", "r"));
        }
        foodBonusOrderTile = new FoodBonusOrderTile(1, 1, 1, 1, 0, 1);
        orderTile1 = new OrderTile(1, 1, 0, 1, true);
        orderTile2 = new OrderTile(1, 0, 0, 1, true);
    }

    @Test
    void shouldApplyFeedingSale(){
        Player p1 = gameModel.getPlayers().get(0);
        Player p2 = gameModel.getPlayers().get(1);
        gameModel.getPlayerToOrderTile().put(p1, orderTile1);
        p1.getStats().addFood(1);
        gameModel.getPlayerToOrderTile().put(p2, orderTile2);
        p1.getStats().getBuildingCards().add(foodBonusOrderTile);
        p2.getStats().getBuildingCards().add(foodBonusOrderTile);

        assertEquals(1, p1.getStats().getFood());
        assertEquals(0, p2.getStats().getFood());

        for (Player p : gameModel.getPlayers()) {
            gameModel.setCurrentPlayer(p);
            for (BuildingCard b : p.getStats().getBuildingCards()) {
                b.onOwnTurnEnding(gameModel);
            }
        }

        assertEquals(2, p1.getStats().getFood());
        assertEquals(0, p2.getStats().getFood());
    }

}