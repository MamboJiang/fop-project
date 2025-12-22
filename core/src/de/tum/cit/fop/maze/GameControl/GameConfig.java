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

    public GameConfig() {
        // Default values
        this.musicVolume = 1.0f;
        this.soundVolume = 1.0f;
        this.keyBindings = new HashMap<>();
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public void setSoundVolume(float soundVolume) {
        this.soundVolume = soundVolume;
    }

    public Map<String, Integer> getKeyBindings() {
        return keyBindings;
    }

    public void setKeyBindings(Map<String, Integer> keyBindings) {
        this.keyBindings = keyBindings;
    }
}
