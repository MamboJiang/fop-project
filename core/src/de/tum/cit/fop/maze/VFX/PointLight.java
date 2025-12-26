package de.tum.cit.fop.maze.VFX;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class PointLight {
    public Vector2 position;
    public Color color;
    public float distance;
    public float intensity; // 0 to 1

    public PointLight(float x, float y, float distance, Color color, float intensity) {
        this.position = new Vector2(x, y);
        this.distance = distance;
        this.color = new Color(color);
        this.intensity = intensity;
    }
    
    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }
}
