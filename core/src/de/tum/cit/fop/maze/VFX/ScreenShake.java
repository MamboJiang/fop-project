package de.tum.cit.fop.maze.VFX;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;


public class ScreenShake {
    private float shakeTimer = 0f;
    private float shakeIntensity = 0f;
    

    public void start(float duration, float intensity) {
        this.shakeTimer = duration;
        this.shakeIntensity = intensity;
    }
    

    public void update(float delta, Camera camera) {
        if (shakeTimer > 0) {
            shakeTimer -= delta;

            float xOffset = MathUtils.random(-shakeIntensity, shakeIntensity);
            float yOffset = MathUtils.random(-shakeIntensity, shakeIntensity);

            camera.position.add(xOffset, yOffset, 0);
            camera.update();
        }
    }
    
    public boolean isShaking() {
        return shakeTimer > 0;
    }
}
