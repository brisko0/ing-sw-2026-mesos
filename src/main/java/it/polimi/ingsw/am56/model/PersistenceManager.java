package it.polimi.ingsw.am56.model;

import java.io.*;
import java.nio.file.*;

/**
 * Static helper that persists the live {@link GameModel} to disk so the server can recover
 * its state after a crash or restart. Saves go through a tmp-file + atomic-move pattern
 * (falling back to a non-atomic replace where the filesystem does not support
 * {@link StandardCopyOption#ATOMIC_MOVE}) so a half-written file cannot replace a good one.
 * <p>
 * The class is a utility — the private constructor prevents instantiation. Failures are
 * reported on {@code stdout} / {@code stderr} rather than thrown, because saving and loading
 * are best-effort: a failed save must not abort gameplay, and a corrupt load must not stop
 * the server from offering a fresh game.
 */
public class PersistenceManager {

    private static final String SAVE_FILE = "mesos_save.dat";
    private static final Path SAVE_PATH = Paths.get(SAVE_FILE);

    private PersistenceManager() {}

    /**
     * Serialises the given model to {@value #SAVE_FILE}. Writes to a sibling {@code .tmp}
     * file first and then atomically moves it over the destination; on a non-atomic
     * filesystem, falls back to a {@link StandardCopyOption#REPLACE_EXISTING} move. Errors are
     * logged and the tmp file is best-effort cleaned up — no exception escapes.
     *
     * @param model the model to snapshot
     */
    public static void save(GameModel model) {
        Path tmp = SAVE_PATH.resolveSibling(SAVE_FILE + ".tmp");
        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(tmp)))) {
                oos.writeObject(model);
                oos.flush();
            }
            try {
                Files.move(tmp, SAVE_PATH,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, SAVE_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("[PERSISTENCE] Game state saved to " + SAVE_PATH.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[PERSISTENCE] Failed to save game state: " + e.getMessage());
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
        }
    }

    /**
     * Reads the saved {@link GameModel} from {@value #SAVE_FILE}, if any. Returns {@code null}
     * when no save file exists, or when the file is unreadable or corrupt — in both cases the
     * caller is expected to start a fresh game.
     *
     * @return the deserialised model, or {@code null} if no usable save is available
     */
    public static GameModel load() {
        if (!Files.exists(SAVE_PATH)) {
            System.out.println("[PERSISTENCE] No save file found at " + SAVE_PATH.toAbsolutePath());
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(SAVE_PATH)))) {
            GameModel model = (GameModel) ois.readObject();
            System.out.println("[PERSISTENCE] Game state loaded successfully from " + SAVE_PATH.toAbsolutePath());
            return model;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[PERSISTENCE] Failed to load game state: " + e.getMessage()
                    + " — starting a fresh game.");
            return null;
        }
    }

    /**
     * Removes the save file if it exists. Called by {@link GameModel#endGameActions()} so a
     * cleanly finished game does not leave a stale save for the next server start. Errors are
     * logged and swallowed.
     */
    public static void deleteSave() {
        try {
            boolean deleted = Files.deleteIfExists(SAVE_PATH);
            if (deleted) {
                System.out.println("[PERSISTENCE] Save file deleted.");
            }
        } catch (IOException e) {
            System.err.println("[PERSISTENCE] Could not delete save file: " + e.getMessage());
        }
    }

    /**
     * Returns whether a save file is present on disk.
     *
     * @return {@code true} if {@value #SAVE_FILE} exists
     */
    public static boolean hasSave() {
        return Files.exists(SAVE_PATH);
    }
}
