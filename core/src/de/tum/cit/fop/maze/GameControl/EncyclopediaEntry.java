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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * @return Path to the image asset.
     */
    public String getTexturePath() {
        return texturePath;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTexturePath(String texturePath) {
        this.texturePath = texturePath;
    }

    public boolean isSecret() {
        return isSecret;
    }

    public void setSecret(boolean secret) {
        isSecret = secret;
    }
}
