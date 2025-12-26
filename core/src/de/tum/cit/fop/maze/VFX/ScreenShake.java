package de.tum.cit.fop.maze.VFX;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Handles screen shake effects for the camera.
 */
public class ScreenShake {
    private float shakeTimer = 0f;
    private float shakeIntensity = 0f;
    
    /**
     * Starts a screen shake.
     * @param duration Duration in seconds.
     * @param intensity Max offset in pixels.
     */
    public void start(float duration, float intensity) {
        this.shakeTimer = duration;
        this.shakeIntensity = intensity;
    }
    
    /**
     * Updates the shake timer and applies offset to the camera.
     * Call this AFTER setting the camera's base position.
     * @param delta Time delta
     * @param camera The camera to shake
     */
    public void update(float delta, Camera camera) {
        if (shakeTimer > 0) {
            shakeTimer -= delta;
            
            // Random offset
            float xOffset = MathUtils.random(-shakeIntensity, shakeIntensity);
            float yOffset = MathUtils.random(-shakeIntensity, shakeIntensity);
            
            // Apply to camera
            camera.position.add(xOffset, yOffset, 0);
            camera.update();
        }
    }
    
    public boolean isShaking() {
        return shakeTimer > 0;
    }
}
