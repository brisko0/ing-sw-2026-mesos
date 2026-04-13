package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShamansEvCardTest {

    private GameModel gameModel;
    private Player p1;
    private Player p2;
    private Player p3;
    private ShamansEvCard shamansEvCard;

    @BeforeEach
    void setUp() {
        p1 = new Player("a", "r");
        p2 = new Player("b", "b");
        p3 = new Player("b", "b");
        gameModel = new GameModel();
        shamansEvCard = new ShamansEvCard(1, 1, 1, 10, 5);
        gameModel.getPlayers().add(p1);
        gameModel.getPlayers().add(p2);
        gameModel.getPlayers().add(p3);
    }

    @Test
    void oneShouldWinOneShouldLose() {
        p1.getStats().addShamanStars(5);
        p2.getStats().addShamanStars(2);
        p3.getStats().addShamanStars(1);

        shamansEvCard.resolveEffect(gameModel);

        assertEquals(10, p1.getStats().getPoints());
        assertEquals(0, p2.getStats().getPoints());
        assertEquals(-5, p3.getStats().getPoints());
    }

    @Test
    void twoShouldWinPoints() {
        p1.getStats().addShamanStars(5);
        p2.getStats().addShamanStars(5);
        p3.getStats().addShamanStars(0);

        shamansEvCard.resolveEffect(gameModel);

        assertEquals(10, p1.getStats().getPoints());
        assertEquals(10, p2.getStats().getPoints());
    }

    @Test
    void twoShouldLosePoints() {
        p1.getStats().addShamanStars(5);
        p2.getStats().addShamanStars(2);
        p3.getStats().addShamanStars(2);

        shamansEvCard.resolveEffect(gameModel);

        assertEquals(-5, p2.getStats().getPoints());
        assertEquals(-5, p3.getStats().getPoints());
    }

    @Test
    void shouldWinFirstThenLose(){
        p1.getStats().addShamanStars(5);
        p2.getStats().addShamanStars(5);
        p3.getStats().addShamanStars(5);

        shamansEvCard.resolveEffect(gameModel);

        assertEquals(5, p1.getStats().getPoints());
        assertEquals(5, p2.getStats().getPoints());
        assertEquals(5, p3.getStats().getPoints());
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