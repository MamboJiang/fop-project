package de.tum.cit.fop.maze.GameObj;

public class PlayerState {
    // 数据只存在于内存变量中
    private int currentXP;
    private int healthLevel;
    private int speedLevel;
    private int defenseLevel;

    public PlayerState() {
        // 每次游戏启动初始化为 0
        this.currentXP = 0;
        this.healthLevel = 0;
        this.speedLevel = 0;
        this.defenseLevel = 0;
    }

    // --- XP 逻辑 ---
    public void addXP(int amount) {
        currentXP += amount;
        System.out.println("Current XP: " + currentXP);
    }

    public int getCurrentXP() {
        return currentXP;
    }

    // --- 升级逻辑 ---
    public int getUpgradeCost(String skillType) {
        int level = 0;
        switch (skillType) {
            case "HEALTH": level = healthLevel; break;
            case "SPEED": level = speedLevel; break;
            case "DEFENSE": level = defenseLevel; break;
        }
        return 100 * (level + 1);
    }

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

    // --- 属性加成计算 ---
    public int getMaxLives() {
        return 4 + healthLevel;
    }

    public float getSpeedMultiplier() {
        return 1.0f + (speedLevel * 0.1f);
    }

    public float getDamageReductionChance() {
        return defenseLevel * 0.1f;
    }

    // UI Getter
    public int getHealthLevel() { return healthLevel; }
    public int getSpeedLevel() { return speedLevel; }
    public int getDefenseLevel() { return defenseLevel; }
}