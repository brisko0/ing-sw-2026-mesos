package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.buildingCard.AddPointsEndGame;
import it.polimi.ingsw.am56.model.card.characterCard.ArtistCharCard;
import it.polimi.ingsw.am56.model.card.characterCard.InventorCharCard;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameEndingTest {

    private GameModel gameModel;

    @BeforeEach
    void setUp() throws InvalidActionException {
        gameModel = new GameModel();
        gameModel.initializeGame(2);
    }

    @Test
    void shouldReturnCorrectState() {
        assertEquals(State.GAME_ENDING, new GameEnding(gameModel).getState());
    }

    @Test
    void onEntryRunsEndGameActionsTest() {
        gameModel.getPlayers().add(new Player("a", "a"));
        gameModel.getPlayers().add(new Player("b", "b"));
        gameModel.getPlayers().add(new Player("c", "c"));

        gameModel.getPlayers().getFirst().getStats().addBuilderPoints(3);

        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(new InventorCharCard(1, 1, 1, "PANE"));
        gameModel.getPlayers().get(1).getStats().getInventions().add("PANE");
        gameModel.getPlayers().get(1).getStats().getBuildingCards().add(new AddPointsEndGame(1, 1, 1, 1, 9, 10));

        gameModel.getPlayers().get(2).getStats().getCharacterCards().add(new ArtistCharCard(1, 1, 1));
        gameModel.getPlayers().get(2).getStats().getCharacterCards().add(new ArtistCharCard(2, 1, 1));

        gameModel.setState(new GameEnding(gameModel));

        assertEquals(State.GAME_ENDING, gameModel.getCurrentState().getState());
        assertEquals(3, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(20, gameModel.getPlayers().get(1).getStats().getPoints());
        assertEquals(10, gameModel.getPlayers().get(2).getStats().getPoints());
    }

    @Test
    void furtherActionsRejectedTest() {
        Player p = new Player("a", "a");
        gameModel.getPlayers().add(p);
        gameModel.setState(new GameEnding(gameModel));

        assertThrows(InvalidActionException.class, () -> gameModel.addPlayer(new Player("b", "b")));
        assertThrows(InvalidActionException.class, () -> gameModel.pickUpCard(p, null));
        assertThrows(InvalidActionException.class,
                () -> gameModel.handleTotemPlacement(p, gameModel.getBoard().getOfferTiles().getFirst()));
        assertThrows(InvalidActionException.class, () -> gameModel.skipExtraUpperCardPick(p));
    }
}
