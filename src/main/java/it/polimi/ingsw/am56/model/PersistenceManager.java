package it.polimi.ingsw.am56.model;

import java.io.*;
import java.nio.file.*;

public class PersistenceManager {

    private static final String SAVE_FILE = "mesos_save.dat";
    private static final Path SAVE_PATH = Paths.get(SAVE_FILE);

    private PersistenceManager() {}

    public static void save(GameModel model) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(SAVE_PATH)))) {
            oos.writeObject(model);
            System.out.println("[PERSISTENCE] Game state saved to " + SAVE_PATH.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[PERSISTENCE] Failed to save game state: " + e.getMessage());
        }
    }

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

    public static boolean hasSave() {
        return Files.exists(SAVE_PATH);
    }
}