package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoundEndingTest {

    private GameModel model;
    private RoundEnding roundEnding;

    @BeforeEach
    void setUp() throws InvalidActionException {
        model = new GameModel();
        model.initializeGame(2);
        model.addPlayer(new Player("a", "a"));
        roundEnding = new RoundEnding(model);
    }

    @Test
    void shouldDoEndRoundActions() {
        roundEnding.onEntry();

        assertEquals(State.TOTEM_PLACEMENT, model.getCurrentState().getState());
        assertEquals(model.getCurrentPlayer(), model.getPlayers().getFirst());
    }

    @Test
    void shouldEndgame() {
        while(model.getCurrentRound() < 10) {
            roundEnding.onEntry();
        }

        roundEnding.onEntry();

        assertEquals(State.GAME_ENDING, model.getCurrentState().getState());
    }

    @Test
    void shouldReturnCorrectState() {
        model.setState(roundEnding);
        assertEquals(State.ROUND_ENDING, roundEnding.getState());
    }
}