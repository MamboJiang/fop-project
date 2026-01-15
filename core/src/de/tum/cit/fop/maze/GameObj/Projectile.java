package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Projectile represents a bullet or energy blast.
 * It extends GameObject directly to avoid Character physics (friction/acceleration).
 */
public class Projectile extends GameObject{
    private Vector2 velocity;
    private float speed = 250f;     // 子弹速度
    private float lifeTime = 5.0f;  // 5秒后自动消失
    private boolean isEnemyProjectile; // 标记是敌人发射的还是玩家发射的


    public Projectile(float x, float y, Vector2 direction, TextureRegion textureRegion, boolean isEnemy) {
        super(x, y, 8, 8, textureRegion); // 假设子弹大小 16x16，可调整
        this.isEnemyProjectile = isEnemy;

        // 计算速度向量：方向 * 速度
        this.velocity = new Vector2(direction).nor().scl(speed);

        // 根据飞行方向旋转贴图（可选，如果你的子弹是长条形的）
        // float angle = this.velocity.angleDeg();
        // 旋转逻辑需要在 draw 时处理，或者由 TextureRegion 支持
    }


    public void update(float delta) {
        // 简单的匀速直线运动
        position.mulAdd(velocity, delta);

        // 更新碰撞箱位置
        if (bounds != null) {
            bounds.setPosition(position.x, position.y);
        }

        // 生命周期倒计时
        lifeTime -= delta;
        if (lifeTime <= 0) {
            setMarkedForRemoval(true);
        }
    }

    public boolean isEnemyProjectile() {
        return isEnemyProjectile;
    }
}
