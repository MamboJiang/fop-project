package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Abstract class for objects that can move and interact physically (take
 * damage, have velocity).
 * Extends GameObject with physics properties like velocity, acceleration, and
 * health.
 */
public abstract class MovableObject extends GameObject {

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

    /**
     * Applies damage to the object if not in cooldown.
     * 
     * @param amount The amount of damage to take.
     */
    public void takeDamage(int amount) {
        if (damageCooldownTimer <= 0) {
            this.health -= amount;
            this.damageFlashTime = FLASH_DURATION; // Short visual flash
            this.damageCooldownTimer = DAMAGE_COOLDOWN_DURATION; // Long invulnerability

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

    /**
     * @return True if health is less than or equal to 0.
     */
    public boolean isDead() {
        return health <= 0;
    }

    public boolean isDamaged() {
        return damageFlashTime > 0;
    }

    /**
     * Updates effect timers like damage flash and cooldowns.
     * 
     * @param delta Time since last frame.
     */
    public void updateDamageFlash(float delta) {
        if (damageFlashTime > 0) {
            damageFlashTime -= delta;
        }
        if (damageCooldownTimer > 0) {
            damageCooldownTimer -= delta;
        }
    }

    /**
     * Sets up the sprite batch for a white flash effect if damaged.
     * 
     * @param batch The SpriteBatch.
     */
    protected void setupDamageFlash(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        if (damageFlashTime > 0) {
            batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);
            batch.setColor(1, 1, 1, 1);
        }
    }

    /**
     * Resets the sprite batch blend function after drawing.
     * 
     * @param batch The SpriteBatch.
     */
    protected void endDamageFlash(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        if (damageFlashTime > 0) {
            batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                    com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(1, 1, 1, 1);
        }
    }

    /**
     * Constructor for MovableObject.
     * 
     * @param x             X coordinate.
     * @param y             Y coordinate.
     * @param width         Width.
     * @param height        Height.
     * @param textureRegion Texture.
     */
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

    /**
     * Helper to approach a target value by a given amount (linear interpolation
     * step).
     * 
     * @param current Current value.
     * @param target  Target value.
     * @param amount  Max change amount.
     * @return New value.
     */
    protected float approach(float current, float target, float amount) {
        if (current < target) {
            return Math.min(current + amount, target);
        } else {
            return Math.max(current - amount, target);
        }
    }

    /**
     * @return Current velocity vector.
     */
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
