package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import de.tum.cit.fop.maze.GameObj.PlayerState;

/**
 * Handles saving and loading of game state to JSON files.
 */
public class GameSaveManager {
    private static final String SAVE_DIR = "saves/";
    private static final String SAVE_PREFIX = "save_";
    private static final String EXT = ".json";

    /**
     * Saves the current player state to a file.
     * 
     * @param state     The state to save.
     * @param slotIndex The save slot index.
     */
    public static void saveGame(PlayerState state, int slotIndex) {
        Json json = new Json();
        FileHandle dir = Gdx.files.local(SAVE_DIR);
        if (!dir.exists())
            dir.mkdirs();

        FileHandle file = Gdx.files.local(SAVE_DIR + SAVE_PREFIX + slotIndex + EXT);

        AchievementManager.getInstance().syncTo(state);

        file.writeString(json.prettyPrint(state), false);
        Gdx.app.log("GameSaveManager", "Game saved to slot " + slotIndex);
    }

    /**
     * Loads player state from a file.
     * 
     * @param slotIndex The save slot index.
     * @return The loaded PlayerState, or null if failed.
     */
    public static PlayerState loadGame(int slotIndex) {
        FileHandle file = Gdx.files.local(SAVE_DIR + SAVE_PREFIX + slotIndex + EXT);
        if (!file.exists())
            return null;

        try {
            Json json = new Json();

            PlayerState state = json.fromJson(PlayerState.class, file);
            Gdx.app.log("GameSaveManager", "Game loaded from slot " + slotIndex);

            AchievementManager.getInstance().resetAchievements();
            AchievementManager.getInstance().syncFrom(state);

            return state;
        } catch (Exception e) {
            Gdx.app.error("GameSaveManager", "Failed to load save slot " + slotIndex, e);
            return null;
        }
    }

    /**
     * Checks if a save slot exists.
     * 
     * @param slotIndex The slot index.
     * @return True if exists.
     */
    public static boolean hasSave(int slotIndex) {
        return Gdx.files.local(SAVE_DIR + SAVE_PREFIX + slotIndex + EXT).exists();
    }

    /**
     * @return The slot index with the most recent modification time, or -1.
     */
    public static int getLatestSaveSlot() {
        long lastModified = -1;
        int bestSlot = -1;

        for (int i = 0; i < 3; i++) {
            FileHandle file = Gdx.files.local(SAVE_DIR + SAVE_PREFIX + i + EXT);
            if (file.exists()) {
                if (file.lastModified() > lastModified) {
                    lastModified = file.lastModified();
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }

    /**
     * Sets the default save slot index.
     * 
     * @param slotIndex The slot index to be the default.
     */
    public static void setDefaultSave(int slotIndex) {
        com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("MazeRunnerGlobalPrefs");
        prefs.putInteger("default_save_slot", slotIndex);
        prefs.flush();
    }

    /**
     * Gets the default save slot index.
     * 
     * @return The default slot index, or -1 if not set or invalid.
     */
    public static int getDefaultSaveSlot() {
        com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("MazeRunnerGlobalPrefs");
        int slot = prefs.getInteger("default_save_slot", -1);
        if (slot != -1 && !hasSave(slot)) {
            return -1;
        }
        return slot;
    }

    /**
     * Deletes a save file.
     * 
     * @param slotIndex The slot index to delete.
     */
    public static void deleteSave(int slotIndex) {
        FileHandle file = Gdx.files.local(SAVE_DIR + SAVE_PREFIX + slotIndex + EXT);
        if (file.exists()) {
            file.delete();

            if (getDefaultSaveSlot() == slotIndex) {
                com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("MazeRunnerGlobalPrefs");
                prefs.remove("default_save_slot");
                prefs.flush();
            }
        }
    }

    /**
     * Gets a summary string for a save slot.
     * 
     * @param slotIndex The slot index.
     * @return Summary string (Name + Level count) or "Empty".
     */
    public static String getSaveSummary(int slotIndex) {
        PlayerState state = loadGame(slotIndex);
        if (state == null)
            return "Empty";

        String summary = state.getUsername() + " (Lvl " + state.getCompletedLevels().size() + ")";
        if (slotIndex == getDefaultSaveSlot()) {
            summary += " [Default]";
        }
        return summary;
    }
}
