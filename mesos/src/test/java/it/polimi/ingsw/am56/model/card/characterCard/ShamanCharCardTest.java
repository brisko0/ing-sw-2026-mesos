package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShamanCharCardTest {

    private GameModel gameModel;
    private ShamanCharCard shamanCharCard;

    @BeforeEach
    void setUp() {
        Player p1 = new Player("a", "r");
        gameModel = new GameModel();
        shamanCharCard = new ShamanCharCard(1, 1, 1, 2);
        gameModel.getPlayers().add(p1);
        gameModel.setCurrentPlayer(p1);
    }

    @Test
    void shouldAddShamanStars() {

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getShamanStars());

        shamanCharCard .resolveEffect(gameModel);

        assertEquals(2, gameModel.getCurrentPlayer().getStats().getShamanStars());
    }


    @Test
    void shouldReturnCorrectCharType() {
        assertEquals(CharacterType.SHAMAN,  shamanCharCard.getCharacterType());
    }

}