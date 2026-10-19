package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventorCharCardTest {

    private GameModel gameModel;
    private InventorCharCard inventorCharCard;
    private InventorCharCard inventorCharCard2;

    @BeforeEach
    void setUp() {
        Player p1 = new Player("a", "r");
        gameModel = new GameModel();
        inventorCharCard = new InventorCharCard(1, 1, 1, "PANE");
        inventorCharCard2 = new InventorCharCard(1, 1, 1, "COLLANA");
        gameModel.getPlayers().add(p1);
        gameModel.setCurrentPlayer(p1);
    }

    @Test
    void shouldAddInvention() {

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getInventions().size());

        inventorCharCard.resolveEffect(gameModel);

        assertEquals(1, gameModel.getCurrentPlayer().getStats().getInventions().size());
        assertTrue(gameModel.getCurrentPlayer().getStats().getInventions().contains("PANE"));
        assertFalse(gameModel.getCurrentPlayer().getStats().getInventions().contains("COLLANA"));
    }

    @Test
    void shouldAddBothInvention() {

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getInventions().size());

        inventorCharCard.resolveEffect(gameModel);
        inventorCharCard2.resolveEffect(gameModel);

        assertEquals(2, gameModel.getCurrentPlayer().getStats().getInventions().size());
        assertTrue(gameModel.getCurrentPlayer().getStats().getInventions().contains("PANE"));
        assertTrue(gameModel.getCurrentPlayer().getStats().getInventions().contains("COLLANA"));
    }



    @Test
    void shouldReturnCorrectCharType() {
        assertEquals(CharacterType.INVENTOR,  inventorCharCard.getCharacterType());
    }

}