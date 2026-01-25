package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.Map;
import de.tum.cit.fop.maze.GameObj.PlayerState;

/**
 * Singleton manager for tracking and updating achievements.
 */
public class AchievementManager {
    private static AchievementManager instance;
    private Map<String, Achievement> achievements;
    private HUD hud;

    private AchievementManager() {
        achievements = new HashMap<>();
        loadAchievements();
    }

    /**
     * @return The singleton instance of AchievementManager.
     */
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
        createDefaultAchievements();

    }
    

    /**
     * Resets all achievements to locked state.
     */
    public void resetAchievements() {
        for (Achievement a : achievements.values()) {
            a.setUnlocked(false);
            a.setProgress(0);
        }
    }
    

    /**
     * Syncs achievement state from PlayerState.
     * @param state The player state to load from.
     */
    public void syncFrom(PlayerState state) {
        if (state == null) return;
        
        java.util.HashMap<String, Boolean> unlocked = state.getUnlockedAchievements();
        java.util.HashMap<String, Integer> progress = state.getAchievementProgress();
        
        for (Achievement a : achievements.values()) {
            if (unlocked.containsKey(a.getId())) {
                a.setUnlocked(unlocked.get(a.getId()));
            }
            if (progress.containsKey(a.getId())) {
                a.setProgress(progress.get(a.getId()));
            }
        }
    }

    /**
     * Syncs achievement state to PlayerState for saving.
     * @param state The player state to write to.
     */
    public void syncTo(PlayerState state) {
        if (state == null) return;
        
        java.util.HashMap<String, Boolean> unlocked = new java.util.HashMap<>();
        java.util.HashMap<String, Integer> progress = new java.util.HashMap<>();
        
        for (Achievement a : achievements.values()) {
            unlocked.put(a.getId(), a.isUnlocked());
            progress.put(a.getId(), a.getProgress());
        }
        
        state.setUnlockedAchievements(unlocked);
        state.setAchievementProgress(progress);
    }

    private void createDefaultAchievements() {
        achievements.clear();
        addDefault("first_blood", "First Blood", "Kill your first enemy.", EventType.KILL_ENEMY, 1);
        addDefault("power_up", "Power Up", "Upgrade a skill for the first time.", EventType.UPGRADE_SKILL, 1);
        addDefault("serial_killer", "Serial Killer", "Kill 10 enemies.", EventType.KILL_ENEMY, 10);
        addDefault("story_boss_slayer", "Boss Slayer", "Defeat a Boss in Story Mode.", EventType.KILL_BOSS_STORY, 1);

        addDefault("rich", "Treasure Hunter", "Collect 5 items.", EventType.COLLECT_ITEM, 5);
        addDefault("escape_artist", "Escape Artist", "Complete your first level.", EventType.LEVEL_COMPLETE, 1);
        addDefault("survivor_novice", "Undying Warrior", "Kill 3 Bosses in Endless Mode.", EventType.KILL_BOSS_ENDLESS, 3);
        addDefault("story_champion", "Savior", "Complete the Story Mode for the first time.", EventType.GAME_COMPLETE, 1);

    }
    
    private void addDefault(String id, String name, String desc, EventType type, int target) {
        Achievement a = new Achievement(id, name, desc, type, target);
        achievements.put(id, a);
    }


    private void saveAchievements() {
    }

    /**
     * Triggers progress for an event type.
     * @param type Event type (e.g., KILL_ENEMY).
     * @param amount Amount to increment.
     */
    public void onEvent(EventType type, int amount) {
        for (Achievement a : achievements.values()) {
            if (a.isUnlocked()) continue;
            if (a.getType() == type) {
                a.setProgress(a.getProgress() + amount);
                checkUnlock(a);
            }
        }
    }

    public void onStatusUpdate(EventType type, int value) {
         for (Achievement a : achievements.values()) {
            if (a.isUnlocked()) continue;
            if (a.getType() == type) {

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
