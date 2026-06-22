package it.polimi.ingsw.am56.model;

import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.model.state.GameEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceManagerTest {

    private static final Path SAVE = Paths.get("mesos_save.dat");
    private static final Path BACKUP = Paths.get("mesos_save.dat.testbak");

    @BeforeEach
    void setUp() throws IOException {
        if (Files.exists(SAVE)) {
            Files.move(SAVE, BACKUP, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(SAVE);
        if (Files.exists(BACKUP)) {
            Files.move(BACKUP, SAVE, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private GameModel newGame() throws InvalidActionException {
        GameModel model = new GameModel();
        model.initializeGame(2);
        model.getPlayers().add(new Player("a", "a"));
        return model;
    }

    @Test
    void saveThenLoadRoundTripTest() throws InvalidActionException {
        GameModel model = newGame();

        PersistenceManager.save(model);
        assertTrue(PersistenceManager.hasSave());

        GameModel loaded = PersistenceManager.load();
        assertNotNull(loaded);
        assertEquals(model.getGameSize(), loaded.getGameSize());
        assertEquals(model.getPlayers().size(), loaded.getPlayers().size());
        assertEquals(model.getCurrentState().getState(), loaded.getCurrentState().getState());

        loaded.addObserver(new GameObserver() {
            @Override
            public void onModelChanged(GameEvent event) {}
        });
        assertDoesNotThrow(loaded::syncLobbyState);
    }

    @Test
    void loadWithNoFileReturnsNullTest() {
        PersistenceManager.deleteSave();
        assertFalse(PersistenceManager.hasSave());
        assertNull(PersistenceManager.load());
    }

    @Test
    void hasSaveReflectsFileTest() throws InvalidActionException {
        GameModel model = newGame();

        PersistenceManager.deleteSave();
        assertFalse(PersistenceManager.hasSave());

        PersistenceManager.save(model);
        assertTrue(PersistenceManager.hasSave());

        PersistenceManager.deleteSave();
        assertFalse(PersistenceManager.hasSave());
    }

    @Test
    void deleteSaveIsIdempotentTest() {
        PersistenceManager.deleteSave();
        assertDoesNotThrow(PersistenceManager::deleteSave);
        assertFalse(PersistenceManager.hasSave());
    }

    @Test
    void loadCorruptFileReturnsNullTest() throws IOException {
        Files.write(SAVE, "this is not a serialized GameModel".getBytes());
        assertTrue(PersistenceManager.hasSave());
        assertNull(PersistenceManager.load());
    }
}
