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

    /**
     * Constructor loads the config from file or creates a default one.
     */
    public ConfigManager() {
        this.json = new Json();
        loadConfig();
    }

    /**
     * Loads the configuration from the local file system.
     */
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

        validateKeyBindings();
    }

    /**
     * Creates a default configuration with standard key bindings.
     */
    private void createDefaultConfig() {
        config = new GameConfig();

        Map<String, Integer> keys = config.getKeyBindings();
        keys.put("UP", Keys.W);
        keys.put("DOWN", Keys.S);
        keys.put("LEFT", Keys.A);
        keys.put("RIGHT", Keys.D);
        keys.put("RIGHT", Keys.D);
        keys.put("PAUSE", Keys.ESCAPE);
        keys.put("CONSOLE", Keys.GRAVE);
        saveConfig();
    }

    /**
     * Ensures all necessary key bindings exist.
     */
    private void validateKeyBindings() {
        Map<String, Integer> keys = config.getKeyBindings();
        if (!keys.containsKey("UP"))
            keys.put("UP", Keys.W);
        if (!keys.containsKey("DOWN"))
            keys.put("DOWN", Keys.S);
        if (!keys.containsKey("LEFT"))
            keys.put("LEFT", Keys.A);
        if (!keys.containsKey("RIGHT"))
            keys.put("RIGHT", Keys.D);
        if (!keys.containsKey("PAUSE"))
            keys.put("PAUSE", Keys.ESCAPE);
        if (!keys.containsKey("CONSOLE"))
            keys.put("CONSOLE", Keys.GRAVE);
    }

    /**
     * Saves the current configuration to local storage.
     */
    public void saveConfig() {
        FileHandle file = Gdx.files.local(CONFIG_FILE);
        file.writeString(json.prettyPrint(config), false);
    }

    /**
     * @return Current music volume.
     */
    public float getMusicVolume() {
        return config.getMusicVolume();
    }

    /**
     * Sets the music volume.
     * @param volume New volume (0.0 - 1.0).
     */
    public void setMusicVolume(float volume) {
        config.setMusicVolume(volume);
        saveConfig();
    }

    /**
     * @return Current sound volume.
     */
    public float getSoundVolume() {
        return config.getSoundVolume();
    }

    /**
     * Sets the sound volume.
     * @param volume New volume (0.0 - 1.0).
     */
    public void setSoundVolume(float volume) {
        config.setSoundVolume(volume);
        saveConfig();
    }

    /**
     * Gets the key code for a specific action.
     * 
     * @param action The action name (e.g., "UP").
     * @return The key code, or Keys.UNKNOWN if not found.
     */
    public int getKey(String action) {
        return config.getKeyBindings().getOrDefault(action, Keys.UNKNOWN);
    }

    /**
     * Sets a key binding.
     * 
     * @param action  Action name.
     * @param keyCode Input.Keys key code.
     */
    public void setKey(String action, int keyCode) {
        config.getKeyBindings().put(action, keyCode);
        saveConfig();
    }
}
