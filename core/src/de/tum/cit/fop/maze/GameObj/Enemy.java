package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.AI.Grid;
import de.tum.cit.fop.maze.AI.PathFinder;

import java.util.List;

/**
 * Represents an enemy character in the game.
 * Uses a state machine (Patrol, Chase, Retreat, Confused) for AI behavior.
 */
public class Enemy extends MovableObject {

    protected enum State {
        PATROL, CHASE, RETREAT, CONFUSED
    }

    protected State currentState;
    protected float speed = 20f;

    protected Grid grid;
    protected Character target;
    protected List<Vector2> currentPath;
    protected int pathIndex = 0;
    protected float pathTimer = 0;
    protected static final float PATH_UPDATE_INTERVAL = 0.5f;
    protected float detectionRange = 64f; // 4 Tiles (16 * 4)

    protected float confusedTimer = 0f;
    protected float stunTimer = 0f;

    protected Animation<TextureRegion> walkDown;
    protected Animation<TextureRegion> walkLeft;
    protected Animation<TextureRegion> walkRight;
    protected Animation<TextureRegion> walkUp;

    protected float stateTime;

    protected float waitTimer = 0f;

    /**
     * Constructor for Enemy.
     * 
     * @param x          X coordinate.
     * @param y          Y coordinate.
     * @param animations Array of texture animations.
     * @param grid       The navigation grid.
     * @param target     The player character to chase.
     */
    public Enemy(float x, float y, Animation<TextureRegion>[] animations, Grid grid, Character target) {

        super(x, y, 16, 16, animations[0].getKeyFrame(0));

        this.walkDown = animations[0];
        this.walkLeft = animations[1];
        this.walkRight = animations[2];
        this.walkUp = animations[3];

        this.grid = grid;
        this.target = target;
        this.currentState = State.PATROL;

        this.bounds = new Rectangle(x + 4, y + 4, 8, 8);

        this.maxSpeed = speed;
        this.acceleration = 50f;
        this.friction = 50f;
    }

    public void knockback(Vector2 direction) {
        this.velocity.set(direction).scl(250f); // Knockback speed
        this.stunTimer = 0.5f; // 0.5s stun
    }

    /**
     * Overrides damage logic to use a specific cooldown for enemies,
     * allowing for faster consecutive hits compared to the player.
     */
    @Override
    public void takeDamage(int amount) {
        if (damageCooldownTimer <= 0) {
            this.health -= amount;
            this.damageFlashTime = FLASH_DURATION; 
            // Separate cooldown for enemies: 0.3s (allows combos)
            this.damageCooldownTimer = 0.3f; 

            if (this.health <= 0) {
                this.health = 0;
                setMarkedForRemoval(true);
            }
        }
    }

    /**
     * Updates the enemy's logic and physics.
     * 
     * @param delta Time delta.
     */
    public void update(float delta) {
        stateTime += delta;

        inputVector.set(0, 0);

        if (stunTimer > 0) {
            stunTimer -= delta;
            // Apply friction to slide while stunned
            if (velocity.len() > 0) {
                float friction = 300f;
                float speed = velocity.len();
                speed -= friction * delta;
                if (speed < 0)
                    speed = 0;
                velocity.setLength(speed);
            }
            updatePhysics(delta);
            // Handle collision while sliding
            if (velocity.len() > 1f) {
                float oldX = position.x;
                position.x += velocity.x * delta;
                updateBounds();
                if (checkCollision() != null) {
                    position.x = oldX;
                    updateBounds();
                }

                float oldY = position.y;
                position.y += velocity.y * delta;
                updateBounds();
                if (checkCollision() != null) {
                    position.y = oldY;
                    updateBounds();
                }
            }
            return; // Skip AI logic while stunned
        }

        if (currentState == State.CHASE || currentState == State.RETREAT) {
            this.maxSpeed = speed * 4f;
            this.acceleration = 500f;
            this.friction = 500f;
        } else {
            this.maxSpeed = speed;
            this.acceleration = 50f;
            this.friction = 50f;
        }

        switch (currentState) {
            case PATROL:
                updatePatrol(delta);
                break;
            case CHASE:
                updateChase(delta);
                break;
            case RETREAT:
                updateRetreat(delta);
                break;
            case CONFUSED:
                updateConfused(delta);
                break;
        }

        updateCombat(delta);

        if (currentPath != null && pathIndex < currentPath.size() && currentState != State.CONFUSED) {
            Vector2 targetNode = currentPath.get(pathIndex);

            float targetX = targetNode.x - width / 2;
            float targetY = targetNode.y - height / 2;

            float dist = Vector2.dst(position.x, position.y, targetX, targetY);

            if (dist < 5f) {
                pathIndex++;
            } else {

                if (inputVector.len2() == 0) {
                    inputVector.set(targetX, targetY).sub(position.x, position.y).nor();
                }
            }
        }

        updatePhysics(delta);

        if (velocity.len() > 1f) {
            Vector2 dir = velocity.cpy().nor();

            float oldX = position.x;
            position.x += velocity.x * delta;
            updateBounds();
            GameObject colX = checkCollision();
            if (colX != null) {
                position.x = oldX;
                updateBounds();
            }

            float oldY = position.y;
            position.y += velocity.y * delta;
            updateBounds();
            GameObject colY = checkCollision();
            if (colY != null) {
                position.y = oldY;
                updateBounds();
            }

            handleWallSliding(delta, inputVector.len() > 0 ? inputVector : dir, colX, colY);

            Animation<TextureRegion> currentAnim = walkDown;
            if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
                if (velocity.x > 0)
                    currentAnim = walkRight;
                else
                    currentAnim = walkLeft;
            } else {
                if (velocity.y > 0)
                    currentAnim = walkUp;
                else
                    currentAnim = walkDown;
            }
            this.textureRegion = currentAnim.getKeyFrame(stateTime, true);

        } else {

            this.textureRegion = walkDown.getKeyFrame(0.2f, true);
        }
        updateBounds();
    }

    protected Vector2 getCenter() {
        return new Vector2(position.x + width / 2, position.y + height / 2);
    }

    protected Vector2 getTargetCenter() {
        Rectangle tBounds = target.getBounds();
        return new Vector2(tBounds.x + tBounds.width / 2, tBounds.y + tBounds.height / 2);
    }

    private boolean isSuicide = false;

    public void setSuicide(boolean suicide) {
        this.isSuicide = suicide;
    }

    /**
     * Checks for collision with player to deal damage.
     * 
     * @param delta Time delta.
     */
    protected void updateCombat(float delta) {

        if (bounds.overlaps(target.getBounds())) {

            FileHandle damageSoundFile = Gdx.files.internal("assets/damage.mp3");
            if (damageSoundFile.exists()) {
                Gdx.audio.newSound(damageSoundFile).play();
            }

            Rectangle rect = getBounds();
            if (rect.overlaps(target.getBounds())) {
                if (!target.isShielded()) {
                    target.takeDamage();
                    if (isSuicide) {
                        setMarkedForRemoval(true);
                    }
                }
            }

            if (health <= 40) {
                currentState = State.RETREAT;
                return;
            }

            float jitterX = MathUtils.random(-4f, 4f);
            float jitterY = MathUtils.random(-4f, 4f);

            Vector2 combatTarget = getTargetCenter().add(jitterX, jitterY);

            inputVector.set(combatTarget).sub(getCenter()).nor();

        }
    }

    private void updatePatrol(float delta) {
        float distToPlayer = Vector2.dst(getCenter().x, getCenter().y, getTargetCenter().x, getTargetCenter().y);

        if (target != null && distToPlayer < detectionRange) {
            if (hasLineOfSight(getCenter(), getTargetCenter())) {
                currentState = State.CHASE;
                pathTimer = PATH_UPDATE_INTERVAL;
                return;
            }
        }

        if (currentPath == null || pathIndex >= currentPath.size()) {
            waitTimer -= delta;

            if (waitTimer <= 0) {
                pickRandomPatrolPoint();
                waitTimer = MathUtils.random(0f, 2f);
            }
        }
    }

    private void updateChase(float delta) {
        pathTimer += delta;
        if (pathTimer > PATH_UPDATE_INTERVAL) {
            pathTimer = 0;
            currentPath = PathFinder.findPath(grid, getCenter(), getTargetCenter());
            pathIndex = 0;
        }

        float distToPlayer = Vector2.dst(getCenter().x, getCenter().y, getTargetCenter().x, getTargetCenter().y);

        if (health <= 40) {
            currentState = State.RETREAT;
            return;
        }

        if (distToPlayer > detectionRange * 1.5f || !hasLineOfSight(getCenter(), getTargetCenter())) {
            if (distToPlayer > detectionRange * 2f) {
                currentState = State.CONFUSED;
                confusedTimer = 3.0f;
                currentPath = null;
            }
        }
    }

    private void updateRetreat(float delta) {
        pathTimer += delta;

        boolean pathFinished = currentPath == null || pathIndex >= currentPath.size();

        if (pathTimer > 1.5f || pathFinished) {
            pathTimer = 0;

            Vector2 center = getCenter();
            Vector2 playerCenter = getTargetCenter();
            Vector2 dirToPlayer = new Vector2(playerCenter).sub(center).nor();
            Vector2 fleeDir = dirToPlayer.scl(-1);

            Vector2 fleeTarget = new Vector2(center).mulAdd(fleeDir, 64f);

            if (isWalkable(fleeTarget)) {
                currentPath = PathFinder.findPath(grid, center, fleeTarget);
            } else {
                currentPath = findRetreatPathFallback();
            }
            pathIndex = 0;
        }

        float distToPlayer = Vector2.dst(getCenter().x, getCenter().y, getTargetCenter().x, getTargetCenter().y);

        if (distToPlayer > detectionRange * 2f) {
            currentState = State.CONFUSED;
            confusedTimer = 3.0f;
        }
    }

    private boolean isWalkable(Vector2 pos) {
        return grid.isWalkable((int) (pos.x / 16), (int) (pos.y / 16));
    }

    private List<Vector2> findRetreatPathFallback() {
        int cx = (int) (getCenter().x / 16);
        int cy = (int) (getCenter().y / 16);
        float currentDist = Vector2.dst2(getCenter().x, getCenter().y, getTargetCenter().x, getTargetCenter().y);

        for (int i = 0; i < 8; i++) {
            int tx = cx + MathUtils.random(-6, 6);
            int ty = cy + MathUtils.random(-6, 6);

            if (tx >= 0 && ty >= 0 && tx < grid.getWidth() && ty < grid.getHeight() && grid.isWalkable(tx, ty)) {
                Vector2 targetPos = new Vector2(tx * 16 + 8, ty * 16 + 8);
                float newDist = Vector2.dst2(targetPos.x, targetPos.y, getTargetCenter().x, getTargetCenter().y);

                if (newDist > currentDist + 256) {
                    List<Vector2> path = PathFinder.findPath(grid, getCenter(), targetPos);
                    if (path != null)
                        return path;
                }
            }
        }
        return null;
    }

    private void updateConfused(float delta) {
        confusedTimer -= delta;

        float distToPlayer = Vector2.dst(getCenter().x, getCenter().y, getTargetCenter().x, getTargetCenter().y);
        if (distToPlayer < detectionRange) {
            if (health <= 40) {
                currentState = State.RETREAT;
            } else {
                currentState = State.CHASE;
            }
            return;
        }

        if (confusedTimer <= 0) {
            currentState = State.PATROL;
        }
    }

    private void pickRandomPatrolPoint() {
        int cx = (int) (getCenter().x / 16);
        int cy = (int) (getCenter().y / 16);

        for (int i = 0; i < 10; i++) {
            int tx = cx + MathUtils.random(-3, 3);
            int ty = cy + MathUtils.random(-3, 3);

            if (grid.isWalkable(tx, ty)) {
                Vector2 targetPos = new Vector2(tx * 16 + 8, ty * 16 + 8);

                if (!hasLineOfSight(getCenter(), targetPos))
                    continue;

                currentPath = PathFinder.findPath(grid, getCenter(), targetPos);
                pathIndex = 0;
                if (currentPath != null)
                    break;
            }
        }
    }

    private boolean hasLineOfSight(Vector2 start, Vector2 end) {
        int x0 = (int) (start.x / 16);
        int y0 = (int) (start.y / 16);
        int x1 = (int) (end.x / 16);
        int y1 = (int) (end.y / 16);

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (!grid.isWalkable(x0, y0))
                return false;

            if (x0 == x1 && y0 == y1)
                break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
        return true;
    }

    private GameObject checkCollision() {

        int minX = (int) (bounds.x / 16);
        int maxX = (int) ((bounds.x + bounds.width) / 16);
        int minY = (int) (bounds.y / 16);
        int maxY = (int) ((bounds.y + bounds.height) / 16);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (!grid.isWalkable(x, y)) {

                    Rectangle tileBounds = new Rectangle(x * 16, y * 16, 16, 16);
                    if (bounds.overlaps(tileBounds)) {
                        return new de.tum.cit.fop.maze.GameObj.Wall(x * 16, y * 16, 16, 16, null);
                    }
                }
            }
        }
        return null;
    }

    private void handleWallSliding(float delta, Vector2 dir, GameObject colX, GameObject colY) {
        float SLIDE_THRESHOLD = 8.0f;
        float slideSpeed = speed * 1.5f;

        if (colX != null && Math.abs(dir.x) > 0 && Math.abs(dir.y) < 0.5f) {
            Rectangle wallBounds = colX.getBounds();
            float overlapY = Math.min(bounds.y + bounds.height, wallBounds.y + wallBounds.height)
                    - Math.max(bounds.y, wallBounds.y);

            if (overlapY > 0 && overlapY <= SLIDE_THRESHOLD) {
                float centerY = bounds.y + bounds.height / 2;
                float wallCenterY = wallBounds.y + wallBounds.height / 2;
                if (centerY < wallCenterY)
                    position.y -= slideSpeed * delta;
                else
                    position.y += slideSpeed * delta;
            }
        }

        if (colY != null && Math.abs(dir.y) > 0 && Math.abs(dir.x) < 0.5f) {
            Rectangle wallBounds = colY.getBounds();
            float overlapX = Math.min(bounds.x + bounds.width, wallBounds.x + wallBounds.width)
                    - Math.max(bounds.x, wallBounds.x);

            if (overlapX > 0 && overlapX <= SLIDE_THRESHOLD) {
                float centerX = bounds.x + bounds.width / 2;
                float wallCenterX = wallBounds.x + wallBounds.width / 2;
                if (centerX < wallCenterX)
                    position.x -= slideSpeed * delta;
                else
                    position.x += slideSpeed * delta;
            }
        }
    }

    protected void updateBounds() {
        this.bounds.setPosition(position.x + 4, position.y + 4);
    }

    /**
     * Draws debug information (bounds, path, view range).
     * 
     * @param sr ShapeRenderer.
     */
    public void drawDebug(ShapeRenderer sr) {
        sr.setColor(Color.YELLOW);

        sr.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        sr.setColor(Color.GREEN);
        sr.circle(getCenter().x, getCenter().y, 2);

        sr.setColor(Color.YELLOW);
        if (currentPath != null) {
            for (int i = 0; i < currentPath.size() - 1; i++) {
                Vector2 p1 = currentPath.get(i);
                Vector2 p2 = currentPath.get(i + 1);
                sr.line(p1, p2);
            }

            if (!currentPath.isEmpty()) {
                Vector2 target = currentPath.get(currentPath.size() - 1);
                sr.circle(target.x, target.y, 4);
            }
        }
    }

    public void draw(SpriteBatch batch) {
        setupDamageFlash(batch);
        batch.draw(getTextureRegion(), getPosition().x -2, getPosition().y-2, 20, 20);
        endDamageFlash(batch);
    }

    /**
     * Draws a status indicator (e.g., '!' for chase) above the enemy.
     * 
     * @param batch  SpriteBatch.
     * @param font   Font to use.
     * @param showHP Whether to show HP text.
     */
    public void drawStatus(SpriteBatch batch, com.badlogic.gdx.graphics.g2d.BitmapFont font, boolean showHP) {
        String statusText = null;
        Color color = Color.WHITE;

        switch (currentState) {
            case CHASE:
                statusText = "!";
                color = Color.RED;
                break;
            case CONFUSED:
                statusText = "?";
                color = Color.YELLOW;
                break;
            default:
                break;
        }

        if (showHP) {
            statusText = (statusText != null ? statusText + " " : "") + health;
            color = Color.CYAN;
        }

        if (statusText != null) {
            float oldScaleX = font.getData().scaleX;
            float oldScaleY = font.getData().scaleY;

            font.getData().setScale(0.3f);
            font.setColor(color);

            float drawX = position.x + width / 2 - 2;
            float drawY = position.y + height + 7;

            font.draw(batch, statusText, drawX, drawY);

            font.setColor(Color.WHITE);
            font.getData().setScale(oldScaleX, oldScaleY);
        }
    }

}
