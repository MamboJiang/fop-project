package de.tum.cit.fop.maze.VFX;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

public class DamageNumber {
    // Target to follow
    private de.tum.cit.fop.maze.GameObj.MovableObject target;
    
    // Relative Offset (Animation)
    private float offsetY;
    private float offsetX; // Small random X jitter is okay, but mainly vertical
    
    private String text;
    private float stateTime;
    private float lifeTime;
    private boolean isFinished;
    
    // Animation Config
    private static final float PHASE_1_DURATION = 0.2f; // Pop Up
    private static final float PHASE_2_DURATION = 0.4f; // Float
    private static final float PHASE_3_DURATION = 0.2f; // Fade
    
    public DamageNumber(de.tum.cit.fop.maze.GameObj.MovableObject target, int value) {
        this.target = target;
        this.text = "-" + value;
        this.stateTime = 0f;
        this.lifeTime = PHASE_1_DURATION + PHASE_2_DURATION + PHASE_3_DURATION;
        this.isFinished = false;
        
        // Random slight X offset so multiple hits don't overlap perfectly
        this.offsetX = com.badlogic.gdx.math.MathUtils.random(-3f, 1f);
        this.offsetY = 0f;
    }
    
    public void update(float delta) {
        stateTime += delta;
        if (stateTime >= lifeTime) {
            isFinished = true;
            return;
        }
        
        // Animation Logic (Relative Y Offset)
        // Start at Center/Head and go UP
        float startHeight = target.getHeight() / 2; // Start from center
        float peakHeight = target.getHeight() + 4; // End above head
        
        if (stateTime < PHASE_1_DURATION) {
            // Phase 1: Pop up
            float alpha = stateTime / PHASE_1_DURATION;
            this.offsetY = Interpolation.pow2Out.apply(startHeight, peakHeight, alpha);
        } else if (stateTime < PHASE_1_DURATION + PHASE_2_DURATION) {
            // Phase 2: Slow float up
             float alpha = (stateTime - PHASE_1_DURATION) / PHASE_2_DURATION;
             // this.offsetY = peakHeight + Interpolation.linear.apply(0, 5, alpha);
        } else {
            // Phase 3: Float away
            float alpha = (stateTime - PHASE_1_DURATION - PHASE_2_DURATION) / PHASE_3_DURATION;
            this.offsetY = peakHeight + 5 + Interpolation.linear.apply(0, 10, alpha);
        }
    }
    
    public void render(SpriteBatch batch, BitmapFont font) {
        if (isFinished || target == null) return;
        
        float scale = 0.3f; // Base scale (Even Smaller)
        float alpha = 1.0f;
        
        // Pop effect
        if (stateTime < PHASE_1_DURATION) {
            float progress = stateTime / PHASE_1_DURATION;
            scale = Interpolation.swingOut.apply(0.0f, 0.3f, progress);
        } else if (stateTime > lifeTime - PHASE_3_DURATION) {
             // Fade out
             float progress = (stateTime - (lifeTime - PHASE_3_DURATION)) / PHASE_3_DURATION;
             alpha = 1.0f - progress;
             scale += progress * 0.05f; // Grow slightly while fading
        }
        
        // Safety Clean: Prevent scale 0 or negative which might crash font renderer
        if (scale < 0.01f) scale = 0.01f;
        
        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;
        
        try {
            font.getData().setScale(scale);
            font.setColor(1f, 0f, 0f, alpha); // Red
            
            // Calculate World Position (Target + Offset)
            float drawX = target.getPosition().x + target.getWidth()/2 + offsetX;
            float drawY = target.getPosition().y + offsetY;
            
            // Center Text roughly
            // 0.3 scale -> char width approx 3-4px
            float textWidth = text.length() * 3.5f;
            
            font.draw(batch, text, drawX - textWidth/2, drawY);
        } catch (Exception e) {
             // Prevent crash
        } finally {
            // Reset
            font.setColor(Color.WHITE);
            font.getData().setScale(oldScaleX, oldScaleY);
        }
    }
    
    public boolean isFinished() {
        return isFinished;
    }
}
