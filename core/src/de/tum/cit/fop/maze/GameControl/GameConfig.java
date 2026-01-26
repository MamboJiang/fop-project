package de.tum.cit.fop.maze.GameControl;

import java.util.HashMap;
import java.util.Map;

/**
 * Data class to store game configuration settings.
 * This class is serializable to JSON.
 */
public class GameConfig {
    private float musicVolume;
    private float soundVolume;
    private Map<String, Integer> keyBindings;

    /**
     * Constructor initializes default headers.
     */
    public GameConfig() {

        this.musicVolume = 1.0f;
        this.soundVolume = 1.0f;
        this.keyBindings = new HashMap<>();
    }

    /**
     * @return Music volume (0.0 to 1.0).
     */
    public float getMusicVolume() {
        return musicVolume;
    }

    /**
     * @param musicVolume New music volume (0.0 to 1.0).
     */
    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
    }

    /**
     * @return Sound volume (0.0 to 1.0).
     */
    public float getSoundVolume() {
        return soundVolume;
    }

    /**
     * @param soundVolume New sound volume (0.0 to 1.0).
     */
    public void setSoundVolume(float soundVolume) {
        this.soundVolume = soundVolume;
    }

    /**
     * @return Map of action names to key codes.
     */
    public Map<String, Integer> getKeyBindings() {
        return keyBindings;
    }

    /**
     * Sets the key bindings map.
     * @param keyBindings New key bindings.
     */
    public void setKeyBindings(Map<String, Integer> keyBindings) {
        this.keyBindings = keyBindings;
    }
}
