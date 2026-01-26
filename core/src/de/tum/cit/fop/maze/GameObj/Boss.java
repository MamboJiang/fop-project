package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.AI.Grid;
import java.util.List;

public class Boss extends Enemy {

    private enum BossState {
        IDLE, CHASE, SHOOTING, DASHING
    }

    private BossState bossState = BossState.IDLE;
    private float stateTimer = 0f;
    private float maxHealth = 500f;
    private boolean active = false;

    private float normalSpeed = 60f;
    private float dashSpeed = 400f;

    private float shootCooldown = 0f;
    private float dashCooldown = 1f;

    private List<Projectile> projectilesRef;
    private TextureRegion bulletTexture;

    private Vector2 dashDirection = new Vector2();

    public Boss(float x, float y, Animation<TextureRegion>[] anims, Grid grid, Character target,
            List<Projectile> projectiles, TextureRegion bulletTex) {
        super(x, y, anims, grid, target);
        this.health = 500;
        this.maxHealth = 500;
        this.width = 64;
        this.height = 64;
        this.projectilesRef = projectiles;
        this.bulletTexture = bulletTex;

        this.bounds = new com.badlogic.gdx.math.Rectangle(x + 24, y + 24, 48, 48);

        this.acceleration = 1000f;
        this.friction = 800f;
    }

    @Override
    public void update(float delta) {
        if (!active) {
            updateAnimation(delta);
            return;
        }

        stateTimer += delta;
        dashCooldown -= delta;

        inputVector.set(0, 0);

        switch (bossState) {
            case IDLE:
                handleIdle(delta);
                break;
            case CHASE:
                handleChase(delta);
                break;
            case SHOOTING:
                handleShooting(delta);
                break;
            case DASHING:
                handleDashing(delta);
                break;
        }
        updatePhysics(delta);

        if (velocity.len() > 1f) {
            float oldX = position.x;
            position.x += velocity.x * delta;
            updateBounds();

            if (checkCollision()) {
                position.x = oldX;
                updateBounds();
            }

            float oldY = position.y;
            position.y += velocity.y * delta;
            updateBounds();

            if (checkCollision()) {
                position.y = oldY;
                updateBounds();
            }
        }

        updateAnimation(delta);
        if (this.getBounds().overlaps(target.getBounds())) {
            if (!target.isShielded()) {
                target.takeDamage(1);
            }
        }

        if (damageFlashTime > 0)
            damageFlashTime -= delta;
    }

    private boolean checkCollision() {
        int minX = (int) (bounds.x / 16);
        int maxX = (int) ((bounds.x + bounds.width) / 16);
        int minY = (int) (bounds.y / 16);
        int maxY = (int) ((bounds.y + bounds.height) / 16);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (!grid.isWalkable(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }


    private void updateAnimation(float delta) {

        stateTime += delta;

        if (velocity.len() > 10f) {
            Animation<TextureRegion> currentAnim = walkDown;
            if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
                currentAnim = velocity.x > 0 ? walkRight : walkLeft;
            } else {
                currentAnim = velocity.y > 0 ? walkUp : walkDown;
            }
            this.textureRegion = currentAnim.getKeyFrame(stateTime, true);
        } else {

            this.textureRegion = walkDown.getKeyFrame(stateTime, true);
        }
    }

    private void handleIdle(float delta) {


        if (stateTimer > 0.3f) {
            float dist = Vector2.dst(position.x, position.y, target.getPosition().x, target.getPosition().y);

            System.out.println("Dist: " + dist + " CD: " + dashCooldown);

            if (dashCooldown <= 0 && dist < 80) {
                switchState(BossState.DASHING);
            }

            else if (dist >= 80 && MathUtils.randomBoolean(0.7f)) {
                switchState(BossState.SHOOTING);
            }

            else {
                switchState(BossState.CHASE);
            }
        }
    }

    private void handleChase(float delta) {
        this.maxSpeed = normalSpeed;


        Vector2 dir = new Vector2(target.getPosition()).sub(position).nor();
        inputVector.set(dir);

        if (stateTimer > 1f) {
            switchState(BossState.IDLE);
        }
    }

    private void handleShooting(float delta) {


        shootCooldown -= delta;
        if (shootCooldown <= 0) {
            shootProjectile();
            shootCooldown = 0.2f;
        }

        if (stateTimer > 1.5f) {
            switchState(BossState.IDLE);
        }
    }

    private void shootProjectile() {
        if (projectilesRef != null) {

            float centerX = position.x + width / 2 - 8;
            float centerY = position.y + height / 2 - 8;


            com.badlogic.gdx.math.Rectangle targetBounds = target.getBounds();
            Vector2 targetCenter = new Vector2(
                    targetBounds.x + targetBounds.width / 2,
                    targetBounds.y + targetBounds.height / 2);
            Vector2 origin = new Vector2(centerX, centerY);
            Vector2 dir = targetCenter.sub(origin).nor();

            projectilesRef.add(new Projectile(centerX, centerY, dir, bulletTexture, true));
        }
    }

    private void handleDashing(float delta) {

        this.maxSpeed = dashSpeed;


        inputVector.set(dashDirection);


        if (stateTimer > 0.5f) {
            dashCooldown = 3.0f;
            velocity.set(0, 0);
            switchState(BossState.CHASE);
        }
    }

    private void switchState(BossState newState) {
        this.bossState = newState;
        this.stateTimer = 0f;

        if (newState == BossState.DASHING) {

            if (target != null) {
                dashDirection = new Vector2(target.getPosition()).sub(position).nor();
            } else {
                dashDirection.set(1, 0);
            }


            velocity.set(dashDirection).scl(dashSpeed);
        }
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        setupDamageFlash(batch);

        batch.draw(getTextureRegion(), getPosition().x - 8, getPosition().y - 8, 64, 64);
        endDamageFlash(batch);
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    public float getHealthPercentage() {
        return (float) health / maxHealth;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}