package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.HunterCharCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HuntingEvCardTest {

    private GameModel gameModel;
    private HuntingEvCard huntingEvCard;
    private HunterCharCard hunterCharCard;
    private HunterCharCard hunterCharCard2;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        for(int i = 0; i < 2; i++) {
            gameModel.getPlayers().add(new Player("a", "a"));
        }
        huntingEvCard = new HuntingEvCard(1, 1, 1, false, 2);
        hunterCharCard = new HunterCharCard(1, 1, 1, true);
        hunterCharCard2 = new HunterCharCard(1, 1, 1, false);
    }

    @Test
    void shouldAddFood() {

        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(hunterCharCard);
        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(hunterCharCard2);

        huntingEvCard.resolveEffect(gameModel);

        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getFood());
        assertEquals(2, gameModel.getPlayers().get(1).getStats().getFood());
    }

    @Test
    void shouldAddPoints() {

        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(hunterCharCard);
        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(hunterCharCard2);

        huntingEvCard.resolveEffect(gameModel);

        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(4, gameModel.getPlayers().get(1).getStats().getPoints());
    }

    @Test
    void shouldReturnCorrectEvType() {
        assertEquals(EvType.HUNTING, huntingEvCard.getEventType());
    }

}