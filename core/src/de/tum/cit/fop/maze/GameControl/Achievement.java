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

    public Achievement() {
    }

    /**
     * Constructor for Achievement.
     * @param id Unique ID.
     * @param name Display name.
     * @param description Requirements description.
     * @param type Event type that triggers progress.
     * @param target distinct value to reach.
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

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public EventType getType() { return type; }
    public int getTarget() { return target; }
    public boolean isHidden() { return hidden; }
    
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
}
