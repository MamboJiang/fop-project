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
    protected int health;
    protected float damageFlashTime = 0f;
    protected float damageCooldownTimer = 0f;
    
    protected static final float FLASH_DURATION = 0.15f; 
    protected static final float DAMAGE_COOLDOWN_DURATION = 1.0f; // 1 second invulnerability
    
    public void takeDamage(int amount) {
        if (damageCooldownTimer <= 0) {
            this.health -= amount;
            this.damageFlashTime = FLASH_DURATION; // Short visual flash
            this.damageCooldownTimer = DAMAGE_COOLDOWN_DURATION; // Long invulnerability
            
            // Play generic sound? Or override?
            if (this.health <= 0) {
                this.health = 0;
                setMarkedForRemoval(true);
            }
        }
    }
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }
    
    public boolean isDead() {
        return health <= 0;
    }
    
    public boolean isDamaged() {
        return damageFlashTime > 0;
    }
    
    public void updateDamageFlash(float delta) {
        if (damageFlashTime > 0) {
            damageFlashTime -= delta;
        }
        if (damageCooldownTimer > 0) {
            damageCooldownTimer -= delta;
        }
    }
    
    // Helper to setup flash blending
    protected void setupDamageFlash(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        if (damageFlashTime > 0) {
            batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);
            batch.setColor(1, 1, 1, 1);
        }
    }
    
    // Helper to reset blending
    protected void endDamageFlash(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
         if (damageFlashTime > 0) {
            batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(1, 1, 1, 1);
        }
    }

    public MovableObject(float x, float y, float width, float height, TextureRegion textureRegion) {
        super(x, y, width, height, textureRegion);
        this.health = 100; // Default
    }
    
    // ... rest of physics code unchanged but copied here to be safe if replaced
    
    /**
     * Updates velocity based on input, acceleration and friction.
     */
    protected void updatePhysics(float delta) {
        updateDamageFlash(delta); // Auto update flash timer
        
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
