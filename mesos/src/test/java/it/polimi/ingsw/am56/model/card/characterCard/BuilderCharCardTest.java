package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuilderCharCardTest {

    private GameModel gameModel;
    private BuilderCharCard builderCharCard;

    @BeforeEach
    void setUp() {
        Player p1 = new Player("a", "r");
        gameModel = new GameModel();
        builderCharCard = new BuilderCharCard(1, 1, 1, 3, 2);
        gameModel.getPlayers().add(p1);
        gameModel.setCurrentPlayer(p1);
    }

    @Test
    void shouldAddBuilderStats() {

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getBuildingSale());
        assertEquals(0, gameModel.getCurrentPlayer().getStats().getBuilderPoints());

        builderCharCard.resolveEffect(gameModel);

        assertEquals(3, gameModel.getCurrentPlayer().getStats().getBuildingSale());
        assertEquals(2, gameModel.getCurrentPlayer().getStats().getBuilderPoints());
    }

    @Test
    void shouldReturnCorrectCharType() {
        assertEquals(CharacterType.BUILDER,  builderCharCard.getCharacterType());
    }

}