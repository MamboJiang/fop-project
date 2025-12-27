package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.AI.Grid;

public class Ghost extends Enemy {

    public Ghost(float x, float y, Grid grid, Character target) {
        // Ghost is Row 2, Col 3 -> BlockCol=2, BlockRow=1 (Assuming 0-based)
        super(x, y, de.tum.cit.fop.maze.MapLoader.getMobAnimations(2, 1), grid, target);
    }
        
    @Override
    public void update(float delta) {
        stateTime += delta;
        inputVector.set(0, 0);

        // Distance Check
        // Wake up distance: 5 tiles = 80 pixels
        // Give up distance: 10 tiles = 160 pixels
        float dist = com.badlogic.gdx.math.Vector2.dst(getCenter().x, getCenter().y, getTargetCenter().x, getTargetCenter().y);
        
        // Logic: Hysteresis
        if (currentState == State.CHASE) {
            // Give up if too far
            if (dist > 160f) {
                currentState = State.PATROL; // Go back to sleep
                this.maxSpeed = 0f;
            } else {
                // Continue Chasing
                inputVector.set(getTargetCenter()).sub(getCenter()).nor();
                this.maxSpeed = 30f;
            }
        } else {
            // Asleep / Patrol
            this.maxSpeed = 0f;
            // Wake up if close
            if (dist < 80f) {
                currentState = State.CHASE;
            }
        }
        
        // Apply Physics
        updatePhysics(delta);
        
        // Move without collision checks (Ghost!)
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        
        // Update Hitbox Position
        bounds.setPosition(position.x+4, position.y+4);
        
        // Damage Check
        if (bounds.overlaps(target.getBounds())) {
             target.takeDamage();
        }
        
        // Animation
        com.badlogic.gdx.graphics.g2d.Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> currentAnim = walkDown;
        if (velocity.len() > 1f) {
            if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
                if (velocity.x > 0) currentAnim = walkRight;
                else currentAnim = walkLeft;
            } else {
                if (velocity.y > 0) currentAnim = walkUp;
                else currentAnim = walkDown;
            }
            this.textureRegion = currentAnim.getKeyFrame(stateTime, true);
        } else {
            this.textureRegion = walkDown.getKeyFrame(stateTime, true);
        }
    }
    
    @Override
    public void drawDebug(com.badlogic.gdx.graphics.glutils.ShapeRenderer sr) {
        // Draw Collision Box (Yellow)
        sr.setColor(com.badlogic.gdx.graphics.Color.YELLOW);
        sr.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        
        // Draw Ghost Range circles?
        // Wake Up Radius (Green)
        sr.setColor(com.badlogic.gdx.graphics.Color.GREEN);
        sr.circle(getCenter().x, getCenter().y, 80f);
        
        // Chase Radius (Red)
        sr.setColor(com.badlogic.gdx.graphics.Color.RED);
        sr.circle(getCenter().x, getCenter().y, 160f);

        // Draw Line to Target if Chasing
        if (currentState == State.CHASE) {
            sr.setColor(com.badlogic.gdx.graphics.Color.CYAN);
            sr.line(getCenter(), getTargetCenter());
        }
    }
}
