package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.HunterCharCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HuntingEvCardTest {

    private GameModel gameModel;
    private Player p1;
    private Player p2;
    private HuntingEvCard huntingEvCard;
    private HunterCharCard hunterCharCard;
    private HunterCharCard hunterCharCard2;

    @BeforeEach
    void setUp() {
        p1 = new Player("a", "r");
        p2 = new Player("b", "b");
        gameModel = new GameModel();
        huntingEvCard = new HuntingEvCard(1, 1, 1, 2);
        hunterCharCard = new HunterCharCard(1, 1, 1, true);
        hunterCharCard2 = new HunterCharCard(1, 1, 1, false);
        gameModel.getPlayers().add(p1);
        gameModel.getPlayers().add(p2);
    }

    @Test
    void shouldAddFood() {

        p2.getStats().getCharacterCards().add(hunterCharCard);
        p2.getStats().getCharacterCards().add(hunterCharCard2);

        huntingEvCard.resolveEffect(gameModel);

        assertEquals(0, p1.getStats().getFood());
        assertEquals(2, p2.getStats().getFood());
    }

    @Test
    void shouldAddPoints() {

        p2.getStats().getCharacterCards().add(hunterCharCard);
        p2.getStats().getCharacterCards().add(hunterCharCard2);

        huntingEvCard.resolveEffect(gameModel);

        assertEquals(0, p1.getStats().getPoints());
        assertEquals(4, p2.getStats().getPoints());
    }



    @Test
    void shouldReturnCorrectEvType() {
        assertEquals(EvType.HUNTING, huntingEvCard.getEventType());
    }

}