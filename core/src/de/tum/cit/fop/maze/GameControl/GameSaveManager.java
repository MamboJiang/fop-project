package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import de.tum.cit.fop.maze.GameObj.PlayerState;

public class GameSaveManager {
    private static final String SAVE_DIR = "saves/";
    private static final String SAVE_PREFIX = "save_";
    private static final String EXT = ".json";

    public static void saveGame(PlayerState state, int slotIndex) {
        Json json = new Json();
        FileHandle dir = Gdx.files.local(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();
        
        FileHandle file = Gdx.files.local(SAVE_DIR + SAVE_PREFIX + slotIndex + EXT);
        
        // Sync achievements to state before saving
        AchievementManager.getInstance().syncTo(state);
        
        file.writeString(json.prettyPrint(state), false);
        Gdx.app.log("GameSaveManager", "Game saved to slot " + slotIndex);
    }

    public static PlayerState loadGame(int slotIndex) {
        FileHandle file = Gdx.files.local(SAVE_DIR + SAVE_PREFIX + slotIndex + EXT);
        if (!file.exists()) return null;
        
        try {
            Json json = new Json();
            // Important: We need to handle the achievements field if we added it.
            PlayerState state = json.fromJson(PlayerState.class, file);
            Gdx.app.log("GameSaveManager", "Game loaded from slot " + slotIndex);
            
            // Sync achievements from state to manager
            AchievementManager.getInstance().resetAchievements();
            AchievementManager.getInstance().syncFrom(state);
            
            return state;
        } catch (Exception e) {
            Gdx.app.error("GameSaveManager", "Failed to load save slot " + slotIndex, e);
            return null;
        }
    }
    
    public static boolean hasSave(int slotIndex) {
        return Gdx.files.local(SAVE_DIR + SAVE_PREFIX + slotIndex + EXT).exists();
    }
    
    // Get the slot index of the most recently modified save, or -1 if none
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
    
    // Get summary for UI (e.g. "PlayerName - XP 500")
    public static String getSaveSummary(int slotIndex) {
        PlayerState state = loadGame(slotIndex);
        if (state == null) return "Empty";
        return state.getUsername() + " (Lvl " + state.getCompletedLevels().size() + ")";
    }
}
