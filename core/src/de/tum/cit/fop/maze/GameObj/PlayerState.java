package de.tum.cit.fop.maze.GameObj;

/**
 * Represents the persistent state of the player across runs or sessions.
 * Holds stats, level, xp, achievements, and upgrades.
 */
public class PlayerState {
    // 数据只存在于内存变量中
    private String username;
    private int currentXP;
    private int healthLevel;
    private int speedLevel;
    private int defenseLevel;

    private java.util.ArrayList<String> completedLevels;

    private java.util.HashMap<String, Boolean> unlockedAchievements;
    private java.util.HashMap<String, Integer> achievementProgress;
    private int endlessWave = 1;

    /**
     * Constructor for PlayerState with default values.
     */
    public PlayerState() {
        this.username = "Adventurer";
        this.currentXP = 0;
        this.healthLevel = 0;
        this.speedLevel = 0;
        this.defenseLevel = 0;
        this.completedLevels = new java.util.ArrayList<>();
        this.unlockedAchievements = new java.util.HashMap<>();
        this.achievementProgress = new java.util.HashMap<>();
        this.endlessWave = 1;
    }
    
    public int getEndlessWave() { return endlessWave; }
    public void setEndlessWave(int wave) { this.endlessWave = wave; }
    public void resetEndlessWave() { this.endlessWave = 1; }
    

    private int currentRunScore = 0;
    private float currentRunHealth = -1;
    private int currentRunXP = 0;

    public int getCurrentRunScore() { return currentRunScore; }
    public void setCurrentRunScore(int score) { this.currentRunScore = score; }

    public float getCurrentRunHealth() { return currentRunHealth; }
    public void setCurrentRunHealth(float health) { this.currentRunHealth = health; }
    
    public int getCurrentRunXP() { return currentRunXP; }
    public void addCurrentRunXP(int amount) { this.currentRunXP += amount; }
    public void setCurrentRunXP(int xp) { this.currentRunXP = xp; }

    /**
     * Resets the temporary stats for a single run (Endless Mode).
     */
    public void resetRunState() {
        this.currentRunScore = 0;
        this.currentRunHealth = -1;
        this.currentRunXP = 0;
    }

    private boolean attackUnlocked = false;
    public boolean isAttackUnlocked() { return attackUnlocked; }
    public void setAttackUnlocked(boolean unlocked) { this.attackUnlocked = unlocked; }
    
    public java.util.HashMap<String, Boolean> getUnlockedAchievements() {
        return unlockedAchievements;
    }
    
    public void setUnlockedAchievements(java.util.HashMap<String, Boolean> achievements) {
        this.unlockedAchievements = achievements;
    }
    
    public java.util.HashMap<String, Integer> getAchievementProgress() {
        return achievementProgress;
    }
    
    public void setAchievementProgress(java.util.HashMap<String, Integer> progress) {
        this.achievementProgress = progress;
    }
    
    public void unlockAchievement(String id) {
        unlockedAchievements.put(id, true);
    }
    
    public boolean isAchievementUnlocked(String id) {
        return unlockedAchievements.getOrDefault(id, false);
    }
    
    public void setUsername(String name) { this.username = name; }
    public String getUsername() { return username; }
    
    /**
     * Marks a level as completed.
     * @param levelName The name of the level.
     */
    public void addCompletedLevel(String levelName) {
        if (!completedLevels.contains(levelName)) {
            completedLevels.add(levelName);
        }
    }
    
    public boolean isLevelCompleted(String levelName) {
        return completedLevels.contains(levelName);
    }
    
    public java.util.ArrayList<String> getCompletedLevels() {
        return completedLevels;
    }

    /**
     * Adds XP to the player's total.
     * @param amount XP amount.
     */
    public void addXP(int amount) {
        currentXP += amount;
        System.out.println("Current XP: " + currentXP);
    }

    public int getCurrentXP() {
        return currentXP;
    }

    /**
     * Calculates the cost to upgrade a skill.
     * @param skillType "HEALTH", "SPEED", or "DEFENSE".
     * @return Cost in XP.
     */
    public int getUpgradeCost(String skillType) {
        int level = 0;
        switch (skillType) {
            case "HEALTH": level = healthLevel; break;
            case "SPEED": level = speedLevel; break;
            case "DEFENSE": level = defenseLevel; break;
        }
        return 100 * (level + 1);
    }

    /**
     * Attempts to upgrade a skill if enough XP is available.
     * @param skillType "HEALTH", "SPEED", or "DEFENSE".
     * @return True if successful.
     */
    public boolean upgradeSkill(String skillType) {
        int cost = getUpgradeCost(skillType);
        if (currentXP >= cost) {
            currentXP -= cost;
            switch (skillType) {
                case "HEALTH": healthLevel++; break;
                case "SPEED": speedLevel++; break;
                case "DEFENSE": defenseLevel++; break;
            }
            return true;
        }
        return false;
    }

    /**
     * @return Max lives dependent on health level.
     */
    public int getMaxLives() {
        return 4 + healthLevel;
    }

    /**
     * @return Speed multiplier from speed level.
     */
    public float getSpeedMultiplier() {
        return 1.0f + (speedLevel * 0.1f);
    }

    /**
     * @return Damage reduction chance from defense level.
     */
    public float getDamageReductionChance() {
        return defenseLevel * 0.1f;
    }

    public int getHealthLevel() { return healthLevel; }
    public int getSpeedLevel() { return speedLevel; }
    public int getDefenseLevel() { return defenseLevel; }
}