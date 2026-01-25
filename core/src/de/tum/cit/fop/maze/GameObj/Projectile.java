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
    private float speed = 250f; // Speed
    private float lifeTime = 5.0f; // Disappear after 5s
    private boolean isEnemyProjectile; // Fired by enemy?

    public Projectile(float x, float y, Vector2 direction, TextureRegion textureRegion, boolean isEnemy) {
        super(x, y, 8, 8, textureRegion);
        this.isEnemyProjectile = isEnemy;

        // Calculate velocity vector: direction * speed
        this.velocity = new Vector2(direction).nor().scl(speed);

        // Optional: Rotate texture based on direction if needed
        // float angle = this.velocity.angleDeg();
    }

    public void update(float delta) {
        // Simple linear movement
        position.mulAdd(velocity, delta);

        // Update bounds position
        if (bounds != null) {
            bounds.setPosition(position.x, position.y);
        }

        // Lifetime countdown
        lifeTime -= delta;
        if (lifeTime <= 0) {
            setMarkedForRemoval(true);
        }
    }

    public boolean isEnemyProjectile() {
        return isEnemyProjectile;
    }
}
