package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.Map;

public class AchievementManager {
    private static AchievementManager instance;
    private Map<String, Achievement> achievements;
    private HUD hud; // To notify UI

    private AchievementManager() {
        achievements = new HashMap<>();
        loadAchievements();
    }

    public static AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }
    
    public void setHUD(HUD hud) {
        this.hud = hud;
    }

    private void loadAchievements() {
        Json json = new Json();
        // Try local file first (editable/user persistence)
        FileHandle file = Gdx.files.local("achievements.json");
        
        if (!file.exists()) {
             // Try internal assets as fallback source
             FileHandle internalFile = Gdx.files.internal("achievements.json");
             if (internalFile.exists()) {
                 try {
                     file.writeString(internalFile.readString(), false);
                     Gdx.app.log("AchievementManager", "Copied internal achievements to local storage.");
                 } catch (Exception e) {
                     Gdx.app.error("AchievementManager", "Failed to copy internal achievements", e);
                     createDefaultAchievements(); // Fallback to hardcoded defaults
                     return;
                 }
             } else {
                 Gdx.app.log("AchievementManager", "No internal achievements found. Creating defaults.");
                 createDefaultAchievements();
                 return; // createDefault calls save, so we are done or can reload
             }
        }

        if (file.exists()) {
            try {
                @SuppressWarnings("unchecked")
                Array<Achievement> list = json.fromJson(Array.class, Achievement.class, file);
                
                if (list != null) {
                    for (Achievement a : list) {
                        achievements.put(a.getId(), a);
                    }
                    Gdx.app.log("AchievementManager", "Loaded " + list.size + " achievements from " + file.path());
                }
            } catch (Exception e) {
                Gdx.app.error("AchievementManager", "Error loading achievements, creating defaults", e);
                createDefaultAchievements();
            }
        }
    }

    private void createDefaultAchievements() {
        achievements.clear();
        // Define defaults code-side if JSON fails
        addDefault("first_blood", "First Blood", "Kill your first enemy.", EventType.KILL_ENEMY, 1);
        addDefault("serial_killer", "Serial Killer", "Kill 10 enemies.", EventType.KILL_ENEMY, 10);
        addDefault("survivor_novice", "Survivor", "Reach Difficulty 3 in Infinite Mode.", EventType.REACH_DIFFICULTY, 3);
        addDefault("rich", "Treasure Hunter", "Collect 5 items.", EventType.COLLECT_ITEM, 5);
        addDefault("escape_artist", "Escape Artist", "Complete your first level.", EventType.LEVEL_COMPLETE, 1);
        
        saveAchievements();
    }
    
    private void addDefault(String id, String name, String desc, EventType type, int target) {
        Achievement a = new Achievement(id, name, desc, type, target);
        achievements.put(id, a);
    }

    private void saveAchievements() {
        Json json = new Json();
        FileHandle file = Gdx.files.local("achievements.json");
        // Convert map values to array
        Array<Achievement> list = new Array<>();
        for (Achievement a : achievements.values()) {
            list.add(a);
        }
        file.writeString(json.prettyPrint(list), false);
        Gdx.app.log("AchievementManager", "Saved achievements to " + file.path());
    }

    public void onEvent(EventType type, int amount) {
        for (Achievement a : achievements.values()) {
            if (a.isUnlocked()) continue;
            if (a.getType() == type) {
                a.setProgress(a.getProgress() + amount);
                checkUnlock(a);
            }
        }
    }
    
    // For absolute values (e.g. Reach Difficulty 3, not "add 1 difficulty")
    public void onStatusUpdate(EventType type, int value) {
         for (Achievement a : achievements.values()) {
            if (a.isUnlocked()) continue;
            if (a.getType() == type) {
                // For status, we check if value >= target
                // We don't "add" to progress, we just set it or check it.
                // Simple implementation:
                if (value >= a.getTarget()) {
                    a.setProgress(value);
                    unlock(a);
                }
            }
        }
    }

    private void checkUnlock(Achievement a) {
        if (a.getProgress() >= a.getTarget()) {
            unlock(a);
        }
    }

    private void unlock(Achievement a) {
        a.setUnlocked(true);
        Gdx.app.log("Achievement", "Unlocked: " + a.getName());
        if (hud != null) {
            hud.showAchievementPopup(a);
        }
        // TODO: Save progress
    }
    
    public void debugUnlock(String id) {
        Achievement a = achievements.get(id);
        if (a != null) {
             unlock(a);
        } else {
             Gdx.app.log("Achievement", "ID not found: " + id);
        }
    }
    
    public java.util.Collection<Achievement> getAchievements() {
        return achievements.values();
    }
}
