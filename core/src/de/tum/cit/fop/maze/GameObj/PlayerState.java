package de.tum.cit.fop.maze.GameObj;

/**
 * Represents the persistent state of the player across runs or sessions.
 * Holds stats, level, xp, achievements, and upgrades.
 */
public class PlayerState {
    private String username;
    private int currentXP;
    private int healthLevel;
    private int speedLevel;
    private int defenseLevel;

    private java.util.ArrayList<String> completedLevels;

    private java.util.HashMap<String, Boolean> unlockedAchievements;
    private java.util.HashMap<String, Integer> achievementProgress;
    private int endlessWave = 1;

    private java.util.HashSet<String> discoveredEncyclopediaIds;

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
        this.discoveredEncyclopediaIds = new java.util.HashSet<>();
    }

    private int maxEndlessWave = 1;

    /**
     * Gets current endless wave.
     * @return Wave number
     */
    public int getEndlessWave() {
        return endlessWave;
    }

    /**
     * Sets endless wave.
     * @param wave Wave number
     */
    public void setEndlessWave(int wave) {
        this.endlessWave = wave;
        if (wave > maxEndlessWave)
            maxEndlessWave = wave;
    }

    /**
     * Resets endless wave to 1.
     */
    public void resetEndlessWave() {
        this.endlessWave = 1;
    }

    /**
     * Gets max endless wave reached.
     * @return Max wave
     */
    public int getMaxEndlessWave() {
        return maxEndlessWave;
    }

    /**
     * Sets max endless wave.
     * @param max Max wave
     */
    public void setMaxEndlessWave(int max) {
        this.maxEndlessWave = max;
    }

    private int maxEndlessFloor = 1;

    /**
     * Gets max endless floor.
     * @return Max floor
     */
    public int getMaxEndlessFloor() {
        return maxEndlessFloor;
    }

    /**
     * Sets max endless floor.
     * @param floor Floor number
     */
    public void setMaxEndlessFloor(int floor) {
        if (floor > maxEndlessFloor)
            maxEndlessFloor = floor;
    }

    private int currentRunScore = 0;
    private float currentRunHealth = -1;
    private int currentRunXP = 0;

    /**
     * Gets current run score.
     * @return Score
     */
    public int getCurrentRunScore() {
        return currentRunScore;
    }

    /**
     * Sets current run score.
     * @param score Score
     */
    public void setCurrentRunScore(int score) {
        this.currentRunScore = score;
    }

    /**
     * Gets current run health.
     * @return Health
     */
    public float getCurrentRunHealth() {
        return currentRunHealth;
    }

    /**
     * Sets current run health.
     * @param health Health
     */
    public void setCurrentRunHealth(float health) {
        this.currentRunHealth = health;
    }

    /**
     * Gets current run XP.
     * @return XP
     */
    public int getCurrentRunXP() {
        return currentRunXP;
    }

    /**
     * Adds to current run XP.
     * @param amount XP amount
     */
    public void addCurrentRunXP(int amount) {
        this.currentRunXP += amount;
    }

    /**
     * Sets current run XP.
     * @param xp XP
     */
    public void setCurrentRunXP(int xp) {
        this.currentRunXP = xp;
    }

    /**
     * Resets the temporary stats for a single run (Endless Mode).
     */
    public void resetRunState() {
        this.currentRunScore = 0;
        this.currentRunHealth = -1;
        this.currentRunXP = 0;
    }

    private boolean attackUnlocked = false;

    /**
     * Checks if attack is unlocked.
     * @return true if unlocked
     */
    public boolean isAttackUnlocked() {
        return attackUnlocked;
    }

    /**
     * Sets attack unlocked state.
     * @param unlocked Unlocked
     */
    public void setAttackUnlocked(boolean unlocked) {
        this.attackUnlocked = unlocked;
    }

    private boolean nonoUnlocked = false;

    /**
     * Checks if Nono is unlocked.
     * @return true if unlocked
     */
    public boolean isNonoUnlocked() {
        return nonoUnlocked;
    }

    /**
     * Sets Nono unlocked state.
     * @param unlocked Unlocked
     */
    public void setNonoUnlocked(boolean unlocked) {
        this.nonoUnlocked = unlocked;
    }

    /**
     * Gets unlocked achievements.
     * @return Achievement map
     */
    public java.util.HashMap<String, Boolean> getUnlockedAchievements() {
        return unlockedAchievements;
    }

    /**
     * Sets unlocked achievements.
     * @param achievements Achievement map
     */
    public void setUnlockedAchievements(java.util.HashMap<String, Boolean> achievements) {
        this.unlockedAchievements = achievements;
    }

    /**
     * Gets achievement progress.
     * @return Progress map
     */
    public java.util.HashMap<String, Integer> getAchievementProgress() {
        return achievementProgress;
    }

    /**
     * Sets achievement progress.
     * @param progress Progress map
     */
    public void setAchievementProgress(java.util.HashMap<String, Integer> progress) {
        this.achievementProgress = progress;
    }

    /**
     * Unlocks an achievement.
     * @param id Achievement ID
     */
    public void unlockAchievement(String id) {
        unlockedAchievements.put(id, true);
    }

    /**
     * Checks if achievement is unlocked.
     * @param id Achievement ID
     * @return true if unlocked
     */
    public boolean isAchievementUnlocked(String id) {
        return unlockedAchievements.getOrDefault(id, false);
    }

    /**
     * Sets username.
     * @param name Username
     */
    public void setUsername(String name) {
        this.username = name;
    }

    /**
     * Gets username.
     * @return Username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Marks a level as completed.
     * 
     * @param levelName The name of the level.
     */
    public void addCompletedLevel(String levelName) {
        if (!completedLevels.contains(levelName)) {
            completedLevels.add(levelName);
        }
    }

    /**
     * Checks if level is completed.
     * @param levelName Level name
     * @return true if completed
     */
    public boolean isLevelCompleted(String levelName) {
        return completedLevels.contains(levelName);
    }

    /**
     * Gets list of completed levels.
     * @return Level list
     */
    public java.util.ArrayList<String> getCompletedLevels() {
        return completedLevels;
    }

    /**
     * Adds XP to the player's total.
     * 
     * @param amount XP amount.
     */
    public void addXP(int amount) {
        currentXP += amount;
        System.out.println("Current XP: " + currentXP);
    }

    /**
     * Gets current XP.
     * @return XP amount
     */
    public int getCurrentXP() {
        return currentXP;
    }

    /**
     * Calculates the cost to upgrade a skill.
     * 
     * @param skillType "HEALTH", "SPEED", or "DEFENSE".
     * @return Cost in XP.
     */
    public int getUpgradeCost(String skillType) {
        int level = 0;
        switch (skillType) {
            case "HEALTH":
                level = healthLevel;
                break;
            case "SPEED":
                level = speedLevel;
                break;
            case "DEFENSE":
                level = defenseLevel;
                break;
        }
        return 100 * (level + 1);
    }

    /**
     * Attempts to upgrade a skill if enough XP is available.
     * 
     * @param skillType "HEALTH", "SPEED", or "DEFENSE".
     * @return True if successful.
     */
    public boolean upgradeSkill(String skillType) {
        int cost = getUpgradeCost(skillType);
        if (currentXP >= cost) {
            currentXP -= cost;
            switch (skillType) {
                case "HEALTH":
                    healthLevel++;
                    break;
                case "SPEED":
                    speedLevel++;
                    break;
                case "DEFENSE":
                    defenseLevel++;
                    break;
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

    /**
     * Gets health level.
     * @return Level
     */
    public int getHealthLevel() {
        return healthLevel;
    }

    /**
     * Gets speed level.
     * @return Level
     */
    public int getSpeedLevel() {
        return speedLevel;
    }

    /**
     * Gets defense level.
     * @return Level
     */
    public int getDefenseLevel() {
        return defenseLevel;
    }

    /**
     * Gets discovered encyclopedia IDs.
     * @return ID set
     */
    public java.util.HashSet<String> getDiscoveredEncyclopediaIds() {
        return discoveredEncyclopediaIds;
    }

    /**
     * Sets discovered encyclopedia IDs.
     * @param ids ID set
     */
    public void setDiscoveredEncyclopediaIds(java.util.HashSet<String> ids) {
        this.discoveredEncyclopediaIds = ids;
    }

    /**
     * Adds discovered ID.
     * @param id ID
     */
    public void addDiscoveredId(String id) {
        if (this.discoveredEncyclopediaIds == null) {
            this.discoveredEncyclopediaIds = new java.util.HashSet<>();
        }
        this.discoveredEncyclopediaIds.add(id);
    }
}