package de.tum.cit.fop.maze.GameControl;

public class EncyclopediaEntry {
    private String id;
    private String name;
    private String description;
    private String texturePath; // 图片资源路径
    private boolean isSecret;

    public EncyclopediaEntry(String id, String name, String description, String texturePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.texturePath = texturePath;
        this.isSecret = false;
    }

    // Getters...
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getTexturePath() { return texturePath; }

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
