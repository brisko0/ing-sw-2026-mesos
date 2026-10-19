package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShamanStarsBonusTest {
    private GameModel gameModel;
    private ShamanStarsBonus shamanStarsBonus;


    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        gameModel.getPlayers().add(new Player("a", "r"));
        gameModel.setCurrentPlayer(gameModel.getPlayers().getFirst());
        shamanStarsBonus = new ShamanStarsBonus(1, 1, 1, 1, 1, 3);
    }

    @Test
    public void shouldAddStars() {

        gameModel.getCurrentPlayer().getStats().addFood(1);

        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getShamanStars());
        assertEquals(1, gameModel.getPlayers().getFirst().getStats().getFood());


        shamanStarsBonus.resolveEffect(gameModel);

        assertEquals(3, gameModel.getPlayers().getFirst().getStats().getShamanStars());
        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getFood());

    }

    @Test
    public void shouldNotAddStars() {

        gameModel.getPlayers().add(new Player("a", "r"));
        gameModel.setCurrentPlayer(gameModel.getPlayers().get(1));

        gameModel.getCurrentPlayer().getStats().addFood(1);

        shamanStarsBonus.resolveEffect(gameModel);

        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getShamanStars());
    }
}