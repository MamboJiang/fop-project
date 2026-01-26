package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Projectile represents a bullet or energy blast.
 * It extends GameObject directly to avoid Character physics
 * (friction/acceleration).
 */
public class Projectile extends GameObject {
    private Vector2 velocity;
    private float speed = 250f;
    private float lifeTime = 5.0f;
    private boolean isEnemyProjectile;

    /**
     * Creates a Projectile.
     * @param x X pos
     * @param y Y pos
     * @param direction Direction vector
     * @param textureRegion Texture
     * @param isEnemy From enemy
     */
    public Projectile(float x, float y, Vector2 direction, TextureRegion textureRegion, boolean isEnemy) {
        super(x, y, 8, 8, textureRegion);
        this.isEnemyProjectile = isEnemy;

        this.velocity = new Vector2(direction).nor().scl(speed);

    }

    /**
     * Updates projectile position and life.
     * @param delta Time delta
     */
    public void update(float delta) {
        position.mulAdd(velocity, delta);

        if (bounds != null) {
            bounds.setPosition(position.x, position.y);
        }

        lifeTime -= delta;
        if (lifeTime <= 0) {
            setMarkedForRemoval(true);
        }
    }

    /**
     * Checks if projectile is from an enemy.
     * @return true if enemy's
     */
    public boolean isEnemyProjectile() {
        return isEnemyProjectile;
    }
}
