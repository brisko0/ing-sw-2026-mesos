package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectorCharCardTest {

    private GameModel gameModel;
    private CollectorCharCard collectorCharCard;

    @BeforeEach
    void setUp() {
        Player p1 = new Player("a", "r");
        gameModel = new GameModel();
        collectorCharCard = new CollectorCharCard(1, 1, 1, 2);
        gameModel.getPlayers().add(p1);
        gameModel.setCurrentPlayer(p1);
    }

    @Test
    void shouldAddFeedingSale() {

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getFeedingSale());

        collectorCharCard .resolveEffect(gameModel);

        assertEquals(2, gameModel.getCurrentPlayer().getStats().getFeedingSale());
    }


    @Test
    void shouldReturnCorrectCharType() {
        assertEquals(CharacterType.COLLECTOR,  collectorCharCard.getCharacterType());
    }

}