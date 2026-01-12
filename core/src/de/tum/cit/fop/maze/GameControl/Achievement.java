package de.tum.cit.fop.maze.GameControl;

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
