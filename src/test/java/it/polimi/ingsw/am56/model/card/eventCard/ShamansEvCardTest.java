package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShamansEvCardTest {

    private GameModel gameModel;
    private ShamansEvCard shamansEvCard;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        gameModel.getPlayers().add(new Player("a", "a"));
        gameModel.getPlayers().add(new Player("b", "b"));
        gameModel.getPlayers().add(new Player("c", "c"));
        shamansEvCard = new ShamansEvCard(1, 1, 1, 10, 5);
    }

    @Test
    void oneShouldWinOneShouldLose() {
        gameModel.getPlayers().getFirst().getStats().addShamanStars(5);
        gameModel.getPlayers().get(1).getStats().addShamanStars(2);
        gameModel.getPlayers().get(2).getStats().addShamanStars(1);

        shamansEvCard.resolveEffect(gameModel);

        assertEquals(10, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(0, gameModel.getPlayers().get(1).getStats().getPoints());
        assertEquals(-5, gameModel.getPlayers().get(2).getStats().getPoints());
    }

    @Test
    void twoShouldWinPoints() {
        gameModel.getPlayers().getFirst().getStats().addShamanStars(5);
        gameModel.getPlayers().get(1).getStats().addShamanStars(5);
        gameModel.getPlayers().get(2).getStats().addShamanStars(0);

        shamansEvCard.resolveEffect(gameModel);

        assertEquals(10, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(10, gameModel.getPlayers().get(1).getStats().getPoints());
    }

    @Test
    void twoShouldLosePoints() {
        gameModel.getPlayers().getFirst().getStats().addShamanStars(5);
        gameModel.getPlayers().get(1).getStats().addShamanStars(2);
        gameModel.getPlayers().get(2).getStats().addShamanStars(2);

        shamansEvCard.resolveEffect(gameModel);

        assertEquals(-5, gameModel.getPlayers().get(1).getStats().getPoints());
        assertEquals(-5, gameModel.getPlayers().get(2).getStats().getPoints());
    }

    @Test
    void shouldWinFirstThenLose(){
        gameModel.getPlayers().getFirst().getStats().addShamanStars(5);
        gameModel.getPlayers().get(1).getStats().addShamanStars(5);
        gameModel.getPlayers().get(2).getStats().addShamanStars(5);

        shamansEvCard.resolveEffect(gameModel);

        assertEquals(5, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(5, gameModel.getPlayers().get(1).getStats().getPoints());
        assertEquals(5, gameModel.getPlayers().get(2).getStats().getPoints());
    }

    @Test
    void shouldReturnCorrectEvType() {
        assertEquals(EvType.SHAMAN, shamansEvCard.getEventType());
    }

    @Test
    void shouldReturnCorrectWinPoints() {
        assertEquals(10, shamansEvCard.getWinPoints());
    }

    @Test
    void shouldReturnCorrectLosePoints() {
        assertEquals(5, shamansEvCard.getLosePoints());
    }

}