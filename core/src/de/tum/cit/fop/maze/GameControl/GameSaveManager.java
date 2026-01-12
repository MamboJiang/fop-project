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
        

        AchievementManager.getInstance().syncTo(state);
        
        file.writeString(json.prettyPrint(state), false);
        Gdx.app.log("GameSaveManager", "Game saved to slot " + slotIndex);
    }

    public static PlayerState loadGame(int slotIndex) {
        FileHandle file = Gdx.files.local(SAVE_DIR + SAVE_PREFIX + slotIndex + EXT);
        if (!file.exists()) return null;
        
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
    
    public static boolean hasSave(int slotIndex) {
        return Gdx.files.local(SAVE_DIR + SAVE_PREFIX + slotIndex + EXT).exists();
    }
    
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
    
    public static String getSaveSummary(int slotIndex) {
        PlayerState state = loadGame(slotIndex);
        if (state == null) return "Empty";
        return state.getUsername() + " (Lvl " + state.getCompletedLevels().size() + ")";
    }
}
