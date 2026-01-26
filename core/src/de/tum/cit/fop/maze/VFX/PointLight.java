package de.tum.cit.fop.maze.VFX;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Data class representing a point light source.
 */
public class PointLight {
    public Vector2 position;
    public Color color;
    public float distance;
    public float intensity;

    /**
     * Constructor for PointLight.
     * @param x X position.
     * @param y Y position.
     * @param distance Radius of the light.
     * @param color Color of the light.
     * @param intensity Intensity (alpha).
     */
    public PointLight(float x, float y, float distance, Color color, float intensity) {
        this.position = new Vector2(x, y);
        this.distance = distance;
        this.color = new Color(color);
        this.intensity = intensity;
    }
    
    /**
     * Sets the position of the light.
     * @param x X coordinate.
     * @param y Y coordinate.
     */
    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }
}
