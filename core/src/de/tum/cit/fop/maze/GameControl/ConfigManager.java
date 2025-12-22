package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import java.util.Map;

/**
 * Manages the loading, saving, and access of game configuration.
 */
public class ConfigManager {
    private static final String CONFIG_FILE = "game_config.json";
    private GameConfig config;
    private final Json json;

    public ConfigManager() {
        this.json = new Json();
        loadConfig();
    }

    private void loadConfig() {
        FileHandle file = Gdx.files.local(CONFIG_FILE);
        if (file.exists()) {
            try {
                config = json.fromJson(GameConfig.class, file);
            } catch (Exception e) {
                Gdx.app.error("ConfigManager", "Error loading config, using defaults", e);
                createDefaultConfig();
            }
        } else {
            createDefaultConfig();
        }
        
        // Ensure defaults if keys are missing from loaded config
        validateKeyBindings();
    }

    private void createDefaultConfig() {
        config = new GameConfig();
        // Set default key bindings
        Map<String, Integer> keys = config.getKeyBindings();
        keys.put("UP", Keys.W);
        keys.put("DOWN", Keys.S);
        keys.put("LEFT", Keys.A);
        keys.put("RIGHT", Keys.D);
        keys.put("PAUSE", Keys.ESCAPE);
        saveConfig();
    }
    
    private void validateKeyBindings() {
        Map<String, Integer> keys = config.getKeyBindings();
        if (!keys.containsKey("UP")) keys.put("UP", Keys.W);
        if (!keys.containsKey("DOWN")) keys.put("DOWN", Keys.S);
        if (!keys.containsKey("LEFT")) keys.put("LEFT", Keys.A);
        if (!keys.containsKey("RIGHT")) keys.put("RIGHT", Keys.D);
        if (!keys.containsKey("PAUSE")) keys.put("PAUSE", Keys.ESCAPE);
    }

    public void saveConfig() {
        FileHandle file = Gdx.files.local(CONFIG_FILE);
        file.writeString(json.prettyPrint(config), false);
    }

    // Getters and Setters

    public float getMusicVolume() {
        return config.getMusicVolume();
    }

    public void setMusicVolume(float volume) {
        config.setMusicVolume(volume);
        saveConfig();
    }

    public float getSoundVolume() {
        return config.getSoundVolume();
    }

    public void setSoundVolume(float volume) {
        config.setSoundVolume(volume);
        saveConfig();
    }

    public int getKey(String action) {
        return config.getKeyBindings().getOrDefault(action, Keys.UNKNOWN);
    }

    public void setKey(String action, int keyCode) {
        config.getKeyBindings().put(action, keyCode);
        saveConfig();
    }
}
