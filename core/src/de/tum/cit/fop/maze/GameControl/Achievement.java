package de.tum.cit.fop.maze.GameControl;

/**
 * Represents a single achievement in the game.
 * Tracks progress and unlock status.
 */
public class Achievement {
    private String id;
    private String name;
    private String description;
    private EventType type;
    private int target;
    private boolean hidden;

    private int progress;
    private boolean unlocked;

    /**
     * Default constructor for Json serialization.
     */
    public Achievement() {
    }

    /**
     * Constructor for Achievement.
     * 
     * @param id          Unique ID.
     * @param name        Display name.
     * @param description Requirements description.
     * @param type        Event type that triggers progress.
     * @param target      distinct value to reach.
     */
    public Achievement(String id, String name, String description, EventType type, int target) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.target = target;
        this.hidden = false;
        this.progress = 0;
        this.unlocked = false;
    }

    /**
     * @return The unique ID of the achievement.
     */
    public String getId() {
        return id;
    }

    /**
     * @return The display name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The description of requirements.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The event type that triggers progress.
     */
    public EventType getType() {
        return type;
    }

    /**
     * @return The target value to reach.
     */
    public int getTarget() {
        return target;
    }

    /**
     * @return True if the achievement is hidden.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * @return Current progress value.
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Sets the current progress.
     * 
     * @param progress The new progress value.
     */
    public void setProgress(int progress) {
        this.progress = progress;
    }

    /**
     * @return True if the achievement is unlocked.
     */
    public boolean isUnlocked() {
        return unlocked;
    }

    /**
     * Sets the unlocked status.
     * 
     * @param unlocked The new unlocked status.
     */
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}
