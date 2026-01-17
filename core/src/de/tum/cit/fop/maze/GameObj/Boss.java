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

    // 基础属性
    private float normalSpeed = 60f;
    private float dashSpeed = 400f;

    private float shootCooldown = 0f;
    private float dashCooldown = 1f;

    private List<Projectile> projectilesRef;
    private TextureRegion bulletTexture;

    // 缓存冲刺方向，防止冲刺途中拐弯
    private Vector2 dashDirection = new Vector2();

    public Boss(float x, float y, Animation<TextureRegion>[] anims, Grid grid, Character target, List<Projectile> projectiles, TextureRegion bulletTex) {
        super(x, y, anims, grid, target);
        this.health = 500;
        this.maxHealth = 500;
        this.width = 64;
        this.height = 64;
        this.projectilesRef = projectiles;
        this.bulletTexture = bulletTex;

        // Adjust collision bounds for larger Boss (proportionally smaller than visual size)
        this.bounds = new com.badlogic.gdx.math.Rectangle(x + 24, y + 24, 48, 48);
        
        // 调整物理属性，让Boss移动更顺滑
        this.acceleration = 1000f;
        this.friction = 800f;
    }

    @Override
    public void update(float delta) {
        stateTimer += delta;
        dashCooldown -= delta;

        // 重置输入
        inputVector.set(0, 0);

        // 状态机逻辑
        switch (bossState) {
            case IDLE: handleIdle(delta); break;
            case CHASE: handleChase(delta); break;
            case SHOOTING: handleShooting(delta); break;
            case DASHING: handleDashing(delta); break;
        }

        // 1. 计算物理速度
        updatePhysics(delta);

        // 2. 应用位移 (这是让 Boss 动起来的核心！)
        if (velocity.len() > 1f) {
            // --- X轴移动 ---
            float oldX = position.x;
            position.x += velocity.x * delta;
            updateBounds();

            if (checkCollision()) { // 这里直接调用下面的 checkCollision
                position.x = oldX;
                updateBounds();
            }

            // --- Y轴移动 ---
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
            // Check shield before dealing damage (same as regular enemies)
            if (!target.isShielded()) {
                target.takeDamage(1);
            }
        }

        if (damageFlashTime > 0) damageFlashTime -= delta;
    }

    // 碰撞检测方法
    // 因为 grid 已经是 protected 了，这里可以直接用 grid.isWalkable
    private boolean checkCollision() {
        int minX = (int) (bounds.x / 16);
        int maxX = (int) ((bounds.x + bounds.width) / 16);
        int minY = (int) (bounds.y / 16);
        int maxY = (int) ((bounds.y + bounds.height) / 16);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                // 【注意】这里直接使用了父类的 grid 变量
                if (!grid.isWalkable(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    // 根据速度方向选择动画帧
    private void updateAnimation(float delta) {
        // Increment stateTime for animation playback
        stateTime += delta;
        
        if (velocity.len() > 10f) {
            Animation<TextureRegion> currentAnim = walkDown;
            if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
                currentAnim = velocity.x > 0 ? walkRight : walkLeft;
            } else {
                currentAnim = velocity.y > 0 ? walkUp : walkDown;
            }
            // stateTime 在父类定义了
            this.textureRegion = currentAnim.getKeyFrame(stateTime, true);
        } else {
            // 站立时
            this.textureRegion = walkDown.getKeyFrame(stateTime, true);
        }
    }

    private void handleIdle(float delta) {
        // IDLE 状态下 inputVector 保持为 0，物理引擎会自动应用摩擦力减速

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

        // 【关键修改】设置 inputVector 而不是 velocity
        Vector2 dir = new Vector2(target.getPosition()).sub(position).nor();
        inputVector.set(dir);

        if (stateTimer > 1f) {
            switchState(BossState.IDLE);
        }
    }

    private void handleShooting(float delta) {
        // 射击时不设置 inputVector，Boss 会自动停下

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
            // 从 Boss 中心发射
            float centerX = position.x + width / 2 - 8; // Center bullet (assuming 16px bullet)
            float centerY = position.y + height / 2 - 8;

            // 使用角色的碰撞箱中心来瞄准，而不是位置中心
            com.badlogic.gdx.math.Rectangle targetBounds = target.getBounds();
            Vector2 targetCenter = new Vector2(
                targetBounds.x + targetBounds.width / 2, 
                targetBounds.y + targetBounds.height / 2
            );
            Vector2 origin = new Vector2(centerX, centerY);
            Vector2 dir = targetCenter.sub(origin).nor();

            projectilesRef.add(new Projectile(centerX, centerY, dir, bulletTexture, true));
        }
    }

    private void handleDashing(float delta) {
        // 持续维持最高速度设定，防止被其他逻辑覆盖
        this.maxSpeed = dashSpeed;

        // 持续给予输入，防止摩擦力让Boss停下来
        inputVector.set(dashDirection);

        // 也可以选择每帧都强制锁定速度，确保不受阻力影响（可选，更霸道）
        // velocity.set(dashDirection).scl(dashSpeed);

        // 冲刺结束判断
        if (stateTimer > 0.5f) {
            dashCooldown = 3.0f; // 重置冷却
            velocity.set(0, 0);  // 冲完急停，更有打击感
            switchState(BossState.CHASE); // 冲完接着追
        }
    }

    private void switchState(BossState newState) {
        this.bossState = newState;
        this.stateTimer = 0f;

        if (newState == BossState.DASHING) {
            // 1. 锁定方向
            if (target != null) {
                dashDirection = new Vector2(target.getPosition()).sub(position).nor();
            } else {
                dashDirection.set(1, 0); // 防御性代码
            }

            // 2. 【关键】瞬间赋予最大速度，不再等待加速度慢慢加上去
            velocity.set(dashDirection).scl(dashSpeed);

            System.out.println("DASH INIT! Vel: " + velocity);
        }
    }
    
    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        setupDamageFlash(batch);
        // Draw Boss at 96x96 size (centered offset by -8 to account for larger sprite)
        batch.draw(getTextureRegion(), getPosition().x - 8, getPosition().y - 8, 64, 64);
        endDamageFlash(batch);
    }

    
    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }
    
    public float getHealthPercentage() {
        return (float)health / maxHealth;
    }
}