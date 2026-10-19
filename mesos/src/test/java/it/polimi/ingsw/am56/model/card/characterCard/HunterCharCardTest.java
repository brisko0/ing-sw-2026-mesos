package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HunterCharCardTest {

    private GameModel gameModel;
    private HunterCharCard hunterCharCard;
    private HunterCharCard hunterCharCard2;

    @BeforeEach
    void setUp() {
        Player p1 = new Player("a", "r");
        gameModel = new GameModel();
        hunterCharCard = new HunterCharCard(1, 1, 1, true);
        hunterCharCard2 = new HunterCharCard(1, 1, 1, false);
        gameModel.getPlayers().add(p1);
        gameModel.setCurrentPlayer(p1);
    }

    @Test
    void shouldAddFood() {

        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(hunterCharCard);
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(hunterCharCard2);

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getFood());

        hunterCharCard .resolveEffect(gameModel);

        assertEquals(2, gameModel.getCurrentPlayer().getStats().getFood());
    }

    @Test
    void shouldNotAddFood() {

        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(hunterCharCard);
        gameModel.getCurrentPlayer().getStats().getCharacterCards().add(hunterCharCard2);

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getFood());

        hunterCharCard2 .resolveEffect(gameModel);

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getFood());
    }


    @Test
    void shouldReturnCorrectCharType() {
        assertEquals(CharacterType.HUNTER,  hunterCharCard.getCharacterType());
    }

}