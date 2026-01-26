package de.tum.cit.fop.maze.GameControl;

public class EncyclopediaEntry {
    private String id;
    private String name;
    private String description;
    private String texturePath;
    private boolean isSecret;

    /**
     * Constructor for EncyclopediaEntry.
     * 
     * @param id          Unique ID.
     * @param name        Display name.
     * @param description Lore description.
     * @param texturePath Path to the image asset.
     */
    public EncyclopediaEntry(String id, String name, String description, String texturePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.texturePath = texturePath;
        this.isSecret = false;
    }

    /**
     * @return The unique ID of the entry.
     */
    public String getId() {
        return id;
    }

    /**
     * @return The display name of the entry.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The description of the entry.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Path to the image asset.
     */
    public String getTexturePath() {
        return texturePath;
    }

    /**
     * Sets the ID of the entry.
     * @param id New ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the name of the entry.
     * @param name New name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the description of the entry.
     * @param description New description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the texture path for the entry image.
     * @param texturePath New texture path.
     */
    public void setTexturePath(String texturePath) {
        this.texturePath = texturePath;
    }

    /**
     * @return True if this entry is a secret achievement.
     */
    public boolean isSecret() {
        return isSecret;
    }

    /**
     * Sets whether this entry is secret.
     * @param secret Secret status.
     */
    public void setSecret(boolean secret) {
        isSecret = secret;
    }
}
