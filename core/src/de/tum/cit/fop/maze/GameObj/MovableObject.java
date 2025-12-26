package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public abstract class MovableObject extends GameObject {
    
    // Physics properties
    protected Vector2 velocity = new Vector2();
    protected Vector2 inputVector = new Vector2();
    protected float acceleration = 800f;
    protected float friction = 800f;
    protected float maxSpeed = 100f;
    protected boolean isMoving = false;

    public MovableObject(float x, float y, float width, float height, TextureRegion textureRegion) {
        super(x, y, width, height, textureRegion);
    }
    
    /**
     * Updates velocity based on input, acceleration and friction.
     * Call this before applying movement.
     */
    protected void updatePhysics(float delta) {
        float targetX = inputVector.x * maxSpeed;
        float targetY = inputVector.y * maxSpeed;
        
        velocity.x = approach(velocity.x, targetX, (inputVector.x != 0 ? acceleration : friction) * delta);
        velocity.y = approach(velocity.y, targetY, (inputVector.y != 0 ? acceleration : friction) * delta);
        
        isMoving = velocity.len() > 10f;
    }
    
    protected float approach(float current, float target, float amount) {
        if (current < target) {
            return Math.min(current + amount, target);
        } else {
            return Math.max(current - amount, target);
        }
    }
    
    public Vector2 getVelocity() {
        return velocity;
    }
    
    public float getMaxSpeed() {
        return maxSpeed;
    }
    
    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
    
    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }
    
    public void setFriction(float friction) {
        this.friction = friction;
    }
}
