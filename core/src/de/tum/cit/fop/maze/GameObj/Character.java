package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.List;
import de.tum.cit.fop.maze.GameObj.Enemy;

/**
 * The player character controlled by the user.
 * Handles movement input, collision logic with walls/items/exits, and
 * rendering.
 */
public class Character extends MovableObject {

    private boolean hasKey = false;
    private float speed;
    private static final float WALK_SPEED = 100f;
    private static final float RUN_SPEED = 200f;
    private float stateTime;
    private boolean isLevelCompleted = false;

    private Animation<TextureRegion> walkDown;
    private Animation<TextureRegion> walkRight;
    private Animation<TextureRegion> walkUp;
    private Animation<TextureRegion> walkLeft;

    private Direction currentDirection;

    private TextureRegion arrowRegion;
    private Vector2 targetPosition;
    private PlayerState playerState;
    private MazeRunnerGame game;
    private float footstepTimer = 0f;

    private boolean blockEffectRequested = false;

    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private static final float ATTACK_DURATION = 0.4f;

    private Animation<TextureRegion> attackDown;
    private Animation<TextureRegion> attackRight;
    private Animation<TextureRegion> attackUp;
    private Animation<TextureRegion> attackLeft;

    private Texture characterTexture;

    /**
     * Enum for movement direction.
     */
    public enum Direction {
        DOWN, RIGHT, UP, LEFT
    }

    /**
     * Constructor for Character.
     * 
     * @param x     Starting x.
     * @param y     Starting y.
     * @param state The player's persistent state (stats, inventory).
     * @param game  The main game instance.
     */
    public Character(float x, float y, PlayerState state, MazeRunnerGame game) {
        super(x, y, 16, 32, null);
        this.playerState = state;
        this.health = 4;
        this.speed = WALK_SPEED;

        this.maxSpeed = WALK_SPEED;
        this.acceleration = 800f;
        this.friction = 800f;

        this.currentDirection = Direction.DOWN;
        this.stateTime = 0f;

        loadAnimations();

        this.bounds = new Rectangle(x + 4, y + 4, 8, 8);

        int maxLives = state.getMaxLives();
        this.health = maxLives;

        float speedMult = state.getSpeedMultiplier();
        this.speed = WALK_SPEED * speedMult;

        this.maxSpeed = WALK_SPEED * speedMult;

        this.game = game;
    }

    /**
     * Sets the position and updates the bounding box.
     * 
     * @param x New x.
     * @param y New y.
     */
    public void setPosition(float x, float y) {
        this.position.set(x, y);
        updateBounds();
    }

    public boolean isLevelCompleted() {
        return isLevelCompleted;
    }

    /**
     * Resets temporary state (health, flags) for a new level.
     */
    public void resetForNewLevel() {
        this.isLevelCompleted = false;
        this.hasKey = false;
        this.targetPosition = null;

        this.shieldTime = 0f;
        this.invincibleTime = 0f;
        this.damageFlashTime = 0f;
        this.screenShakeRequested = false;
        this.damageNumberRequested = false;

        this.velocity.set(0, 0);
        this.acceleration = 800f;
    }

    /**
     * Loads the character sprites and animations.
     */
    private void loadAnimations() {
        characterTexture = new Texture(Gdx.files.internal("player/sprite/aligned_character.png"));

        Texture thingsTexture = new Texture(Gdx.files.internal("things.png"));
        TextureRegion[][] thingsTmp = TextureRegion.split(thingsTexture, 16, 16);
        arrowRegion = thingsTmp[4][0];

        TextureRegion[] downFrames = new TextureRegion[4];
        TextureRegion[] rightFrames = new TextureRegion[4];
        TextureRegion[] upFrames = new TextureRegion[4];
        TextureRegion[] leftFrames = new TextureRegion[4];

        TextureRegion[] attDownFrames = new TextureRegion[4];
        TextureRegion[] attRightFrames = new TextureRegion[4];
        TextureRegion[] attUpFrames = new TextureRegion[4];
        TextureRegion[] attLeftFrames = new TextureRegion[4];


        int frameW = 26;
        int frameH = 46;

        Texture attSheet = new Texture(Gdx.files.internal("player/sprite/aligned_character_mask_knife.png"));
        int attFrameW = 32;
        int attFrameH = 48;

        for (int i = 0; i < 4; i++) {
            downFrames[i] = new TextureRegion(characterTexture, i * frameW, 0, frameW, frameH);
            rightFrames[i] = new TextureRegion(characterTexture, i * frameW, frameH * 3, frameW, frameH);
            upFrames[i] = new TextureRegion(characterTexture, i * frameW, frameH * 2, frameW, frameH);
            leftFrames[i] = new TextureRegion(characterTexture, i * frameW, frameH, frameW, frameH);


            attDownFrames[i] = new TextureRegion(attSheet, i * attFrameW, 0, attFrameW, attFrameH);
            attRightFrames[i] = new TextureRegion(attSheet, i * attFrameW, attFrameH * 3, attFrameW, attFrameH);
            attUpFrames[i] = new TextureRegion(attSheet, i * attFrameW, attFrameH * 2, attFrameW, attFrameH);
            attLeftFrames[i] = new TextureRegion(attSheet, i * attFrameW, attFrameH, attFrameW, attFrameH);
        }

        walkDown = new Animation<>(0.1f, downFrames);
        walkRight = new Animation<>(0.1f, rightFrames);
        walkUp = new Animation<>(0.1f, upFrames);
        walkLeft = new Animation<>(0.1f, leftFrames);

        attackDown = new Animation<>(0.1f, attDownFrames);
        attackRight = new Animation<>(0.1f, attRightFrames);
        attackUp = new Animation<>(0.1f, attUpFrames);
        attackLeft = new Animation<>(0.1f, attLeftFrames);

        this.textureRegion = downFrames[0];
    }

    /**
     * Updates the appearance when the player wears a mask.
     */
    public void loadMaskAppearance() {
        if (characterTexture != null)
            characterTexture.dispose();
        characterTexture = new Texture(Gdx.files.internal("player/sprite/aligned_character_mask.png"));

        int frameW = 26;
        int frameH = 46;
        TextureRegion[] downFrames = new TextureRegion[4];
        TextureRegion[] rightFrames = new TextureRegion[4];
        TextureRegion[] upFrames = new TextureRegion[4];
        TextureRegion[] leftFrames = new TextureRegion[4];

        for (int i = 0; i < 4; i++) {
            downFrames[i] = new TextureRegion(characterTexture, i * frameW, 0, frameW, frameH);
            rightFrames[i] = new TextureRegion(characterTexture, i * frameW, frameH * 3, frameW, frameH);
            upFrames[i] = new TextureRegion(characterTexture, i * frameW, frameH * 2, frameW, frameH);
            leftFrames[i] = new TextureRegion(characterTexture, i * frameW, frameH, frameW, frameH);
        }
        walkDown = new Animation<>(0.1f, downFrames);
        walkRight = new Animation<>(0.1f, rightFrames);
        walkUp = new Animation<>(0.1f, upFrames);
        walkLeft = new Animation<>(0.1f, leftFrames);
    }

    public void attack() {
        if (!isAttacking) {
            isAttacking = true;
            attackTimer = 0f;
            stateTime = 0f;
        }
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public Direction getDirection() {
        return currentDirection;
    }

    /**
     * Handles collision reactions when hitting objects.
     * 
     * @param hitObject   The object collided with.
     * @param oldPosition The position before the move (to revert if wall).
     * @param isXAxis     Whether the movement was on the X axis.
     */
    private void collisionAddressing(GameObject hitObject, float oldPosition, boolean isXAxis) {
        if (hitObject != null) {
            if (hitObject.isMarkedForRemoval()) {
                return;
            }

            if (hitObject instanceof Wall || hitObject instanceof EntryPoint) {
                if (isXAxis) {
                    this.position.x = oldPosition;
                    this.velocity.x = 0;
                } else {
                    this.position.y = oldPosition;
                    this.velocity.y = 0;
                }
                updateBounds();
            } else if (hitObject instanceof Key) {
                this.hasKey = true;
                hitObject.setMarkedForRemoval(true);
                game.playPowerUpSound();
                System.out.println("Key collected!");
                de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance()
                        .onEvent(de.tum.cit.fop.maze.GameControl.EventType.COLLECT_ITEM, 1);
            } else if (hitObject instanceof Collectable && !(hitObject instanceof Heart)) {
                ((Collectable) hitObject).collect(this);
                game.playPowerUpSound();
            } else if (hitObject instanceof Heart) {
                ((Heart) hitObject).collect(this);
            } else if (hitObject instanceof Exit) {
                if (this.hasKey) {
                    this.isLevelCompleted = true;
                    System.out.println("Level Completed!");
                } else {
                    if (isXAxis) {
                        this.position.x = oldPosition;
                        this.velocity.x = 0;
                    } else {
                        this.position.y = oldPosition;
                        this.velocity.y = 0;
                    }
                    updateBounds();
                }
            } else if (hitObject instanceof Trap) {
                if (!isShielded()) {
                    this.takeDamage();
                    System.out.println("Stepped on a trap! Lives left: " + getLives());
                } else {
                    System.out.println("Shield protected against trap!");
                }
            }
        }
    }

    /**
     * Main update loop for the character.
     * Handles input, physics, collisions, and animation state.
     * 
     * @param delta         Time delta.
     * @param mapObjects    List of objects in the map.
     * @param configManager Configuration manager for key bindings.
     */
    public void update(float delta, List<GameObject> mapObjects, List<Enemy> enemies,
            de.tum.cit.fop.maze.GameControl.ConfigManager configManager) {
        stateTime += delta;

        handleInput(configManager);

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            if (isAttackUnlocked() && !isAttacking) {
                attack();
                game.playBlockSound();

                Rectangle attackBox = getAttackRect();
                Vector2 knockbackDir = new Vector2();

                switch (currentDirection) {
                    case UP:
                        knockbackDir.set(0, 1);
                        break;
                    case DOWN:
                        knockbackDir.set(0, -1);
                        break;
                    case LEFT:
                        knockbackDir.set(-1, 0);
                        break;
                    case RIGHT:
                        knockbackDir.set(1, 0);
                        break;
                }

                if (enemies != null) {
                    for (Enemy enemy : enemies) {
                        if (attackBox.overlaps(enemy.getBounds())) {
                            enemy.takeDamage(20);
                            enemy.knockback(knockbackDir);
                        }
                    }
                }
            }
        }

        if (isAttacking) {
            attackTimer += delta;

            if (attackTimer >= ATTACK_DURATION) {
                isAttacking = false;
            }
        }

        updatePhysics(delta);

        if (isMoving) {

            if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
                currentDirection = velocity.x > 0 ? Direction.RIGHT : Direction.LEFT;
            } else {
                currentDirection = velocity.y > 0 ? Direction.UP : Direction.DOWN;
            }
        }

        if (velocity.len() > 1f) {
            float oldX = position.x;
            float oldY = position.y;

            position.x += velocity.x * delta;
            updateBounds();
            GameObject colX = checkCollision(mapObjects);
            collisionAddressing(colX, oldX, true);

            position.y += velocity.y * delta;
            updateBounds();
            GameObject colY = checkCollision(mapObjects);
            collisionAddressing(colY, oldY, false);

            handleWallSliding(delta, mapObjects, colX, colY);
        }

        float stepInterval = (maxSpeed > 150f) ? 0.25f : 0.35f;

        if (velocity.len() > 5f) {
            footstepTimer += delta;

            if (footstepTimer >= stepInterval) {
                game.playFootstepSound();
                footstepTimer = 0f;
            }
        } else {

            footstepTimer = stepInterval;
        }

        Animation<TextureRegion> currentAnim;

        if (isAttacking) {
            switch (currentDirection) {
                case DOWN:
                    currentAnim = attackDown;
                    break;
                case RIGHT:
                    currentAnim = attackRight;
                    break;
                case UP:
                    currentAnim = attackUp;
                    break;
                case LEFT:
                    currentAnim = attackLeft;
                    break;
                default:
                    currentAnim = attackDown;
                    break;
            }
        } else {
            switch (currentDirection) {
                case DOWN:
                    currentAnim = walkDown;
                    break;
                case RIGHT:
                    currentAnim = walkRight;
                    break;
                case UP:
                    currentAnim = walkUp;
                    break;
                case LEFT:
                    currentAnim = walkLeft;
                    break;
                default:
                    currentAnim = walkDown;
                    break;
            }
        }

        if (isMoving || isAttacking) {
            this.textureRegion = currentAnim.getKeyFrame(stateTime, true);
        } else {
            this.textureRegion = currentAnim.getKeyFrame(0, true);
        }

        if (damageFlashTime > 0) {
            damageFlashTime -= delta;
        }

        if (invincibleTime > 0) {
            invincibleTime -= delta;
        }

        if (shieldTime > 0) {
            shieldTime -= delta;
        }

        updateTarget(mapObjects);
    }

    private float shieldTime = 0f;
    private Animation<TextureRegion> shieldAnimation;

    /**
     * Activates a temporary shield.
     * 
     * @param duration Duration in seconds.
     */
    public void activateShield(float duration) {
        if (shieldAnimation == null) {
            loadShieldAnimation();
        }
        this.shieldTime = duration;
    }

    private void loadShieldAnimation() {
        Texture texture = new Texture(Gdx.files.internal("selfmade/shielditem.png"));
        TextureRegion region = new TextureRegion(texture);
        TextureRegion[] frames = new TextureRegion[] { region };
        shieldAnimation = new Animation<>(0.1f, frames);
        shieldAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    public boolean isShielded() {
        return shieldTime > 0;
    }

    /**
     * Renders the character, shield, and navigation arrow.
     * 
     * @param batch The sprite batch.
     */
    public void draw(SpriteBatch batch) {

        setupDamageFlash(batch);

        if (isAttacking) {
            batch.draw(textureRegion, position.x - 2, position.y, 20, 32);
        } else {
            batch.draw(textureRegion, position.x, position.y, width, height);
        }

        endDamageFlash(batch);


        batch.setColor(Color.WHITE);

        if (shieldTime > 0 && shieldAnimation != null) {
            TextureRegion shieldFrame = shieldAnimation.getKeyFrame(stateTime, true);

            batch.setColor(1, 1, 1, 0.5f);

            float actualWidth = 26f;
            float actualHeight = 46f;

            float drawX = (position.x + 8) - (actualWidth / 2);
            float drawY = position.y + (32 - actualHeight) / 2;

            batch.draw(shieldFrame, drawX, drawY, actualWidth, actualHeight);

            batch.setColor(Color.WHITE);
        }
    }

    /**
     * Updates the logic for the navigation arrow (nearest interesting object).
     * 
     * @param mapObjects Objects to scan.
     */
    private void updateTarget(List<GameObject> mapObjects) {
        targetPosition = null;
        float minDst = Float.MAX_VALUE;

        Class<?> targetType = hasKey ? Exit.class : Key.class;

        for (GameObject obj : mapObjects) {
            if (targetType.isInstance(obj)) {
                float dst = Vector2.dst2(position.x, position.y, obj.getPosition().x, obj.getPosition().y);
                if (dst < minDst) {
                    minDst = dst;
                    targetPosition = obj.getPosition();
                }
            }
        }

        if (!hasKey && targetPosition == null) {
            targetType = Exit.class;
            for (GameObject obj : mapObjects) {
                if (targetType.isInstance(obj)) {
                    float dst = Vector2.dst2(position.x, position.y, obj.getPosition().x, obj.getPosition().y);
                    if (dst < minDst) {
                        minDst = dst;
                        targetPosition = obj.getPosition();
                    }
                }
            }
        }
    }

    /**
     * Draws the navigation arrow pointing to the target.
     * 
     * @param batch SpriteBatch.
     */
    public void drawArrow(SpriteBatch batch) {
        if (targetPosition == null)
            return;

        float angle = MathUtils.atan2(targetPosition.y - position.y, targetPosition.x - position.x)
                * MathUtils.radiansToDegrees;
        float radius = 20f;

        float cx = position.x + width / 2;
        float cy = position.y + height / 2;

        float arrowX = cx + MathUtils.cosDeg(angle) * (radius);
        float arrowY = cy + MathUtils.sinDeg(angle) * (radius);

        float w = 16;
        float h = 16;

        batch.draw(
                arrowRegion,
                arrowX - w / 2, arrowY - h / 2,
                w / 2, h / 2,
                w, h,
                1, 1,
                angle - 90);
    }

    /**
     * Reads keyboard input to set the movement vector.
     * 
     * @param configManager Config manager.
     */
    private void handleInput(de.tum.cit.fop.maze.GameControl.ConfigManager configManager) {
        float multiplier = (playerState != null) ? playerState.getSpeedMultiplier() : 1.0f;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            maxSpeed = RUN_SPEED * multiplier;
        } else {
            maxSpeed = WALK_SPEED * multiplier;
        }

        inputVector.set(0, 0);
        if (Gdx.input.isKeyPressed(configManager.getKey("UP")))
            inputVector.y = 1;
        if (Gdx.input.isKeyPressed(configManager.getKey("DOWN")))
            inputVector.y = -1;
        if (Gdx.input.isKeyPressed(configManager.getKey("LEFT")))
            inputVector.x = -1;
        if (Gdx.input.isKeyPressed(configManager.getKey("RIGHT")))
            inputVector.x = 1;

        if (inputVector.len2() > 0) {
            inputVector.nor();
        }
    }

    private void updateBounds() {
        this.bounds.setPosition(position.x + 4, position.y + 4);
    }

    private GameObject checkCollision(List<GameObject> mapObjects) {
        for (GameObject obj : mapObjects) {
            if (obj == this)
                continue;

            if (obj instanceof Wall || obj instanceof Key || obj instanceof Exit || obj instanceof Trap
                    || obj instanceof Collectable || obj instanceof EntryPoint) {
                if (bounds.overlaps(obj.getBounds())) {
                    return obj;
                }
            }
        }
        return null;
    }

    private boolean screenShakeRequested = false;
    private boolean damageNumberRequested = false;
    private float invincibleTime = 0f;
    private static final float INVINCIBLE_DURATION = 1.0f;
    private boolean infiniteHP = false;

    /**
     * Takes damage, checks for block chance, shields, or invincibility.
     * 
     * @param amount Damage amount.
     */
    @Override
    public void takeDamage(int amount) {
        if (invincibleTime > 0)
            return;

        if (playerState != null && com.badlogic.gdx.math.MathUtils.random() < playerState.getDamageReductionChance()) {
            System.out.println("Blocked!");
            game.playBlockSound();
            this.blockEffectRequested = true;
            damageNumberRequested = false;
            invincibleTime = 0.5f;
            return;
        }

        if (damageFlashTime <= 0) {
            if (!infiniteHP) {

                super.takeDamage(amount);
                damageNumberRequested = true;
                game.playHitSound();
                de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance()
                        .onEvent(de.tum.cit.fop.maze.GameControl.EventType.TAKE_DAMAGE, 1);
            } else {

                damageFlashTime = FLASH_DURATION;
                damageNumberRequested = true;
            }

            invincibleTime = INVINCIBLE_DURATION;
            screenShakeRequested = true;
        }
    }

    public void takeDamage() {
        takeDamage(1);
    }

    public void setInfiniteHP(boolean enabled) {
        this.infiniteHP = enabled;
    }

    public boolean isInfiniteHP() {
        return infiniteHP;
    }

    public boolean isDamageNumberRequested() {
        return damageNumberRequested;
    }

    public void clearDamageNumberRequest() {
        this.damageNumberRequested = false;
    }

    public boolean isScreenShakeRequested() {
        return screenShakeRequested;
    }

    public void clearScreenShakeRequest() {
        this.screenShakeRequested = false;
    }

    public int getLives() {
        return health;
    }

    public int getMaxLives() {
        if (playerState != null) {
            return playerState.getMaxLives();
        }
        return 4;
    }

    public boolean hasKey() {
        return hasKey;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    public void setAttackUnlocked(boolean unlocked) {
        if (playerState != null) {
            playerState.setAttackUnlocked(unlocked);
            game.saveGame();
        }
    }

    public boolean isAttackUnlocked() {
        return playerState != null && playerState.isAttackUnlocked();
    }

    public void setLives(int lives) {
        this.health = lives;
        int max = getMaxLives();
        if (this.health > max)
            this.health = max;
        if (this.health < 0)
            this.health = 0;
    }

    public void addLives(int amount) {
        this.health += amount;
        game.playPowerUpSound();
        int max = getMaxLives();
        if (this.health > max)
            this.health = max;
        if (this.health < 0)
            this.health = 0;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    /**
     * Handles "wall sliding" to help the player navigate around corners smoothly.
     */
    private void handleWallSliding(float delta, List<GameObject> mapObjects, GameObject colX, GameObject colY) {
        float SLIDE_THRESHOLD = 8.0f;
        float slideSpeed = 100f;

        if (colX instanceof Wall && Math.abs(inputVector.x) > 0 && Math.abs(inputVector.y) == 0) {
            Rectangle wallBounds = colX.getBounds();
            float overlapY = Math.min(bounds.y + bounds.height, wallBounds.y + wallBounds.height)
                    - Math.max(bounds.y, wallBounds.y);

            if (overlapY > 0 && overlapY <= SLIDE_THRESHOLD) {
                float centerY = bounds.y + bounds.height / 2;
                float wallCenterY = wallBounds.y + wallBounds.height / 2;
                float slideAmount = slideSpeed * delta;

                float newY = position.y;
                boolean slidingDown = centerY < wallCenterY;

                float checkY = slidingDown ? wallBounds.y - 1 : wallBounds.y + wallBounds.height + 1;

                Rectangle neighborCheck = new Rectangle(wallBounds.x, checkY, wallBounds.width, 1);

                if (!isWallAt(neighborCheck, mapObjects, colX)) {
                    if (slidingDown)
                        newY -= slideAmount;
                    else
                        newY += slideAmount;

                    if (isPositionFree(position.x, newY, mapObjects, this)) {
                        position.y = newY;
                        updateBounds();
                    }
                }
            }
        }

        if (colY instanceof Wall && Math.abs(inputVector.y) > 0 && Math.abs(inputVector.x) == 0) {
            Rectangle wallBounds = colY.getBounds();
            float overlapX = Math.min(bounds.x + bounds.width, wallBounds.x + wallBounds.width)
                    - Math.max(bounds.x, wallBounds.x);

            if (overlapX > 0 && overlapX <= SLIDE_THRESHOLD) {
                float centerX = bounds.x + bounds.width / 2;
                float wallCenterX = wallBounds.x + wallBounds.width / 2;
                float slideAmount = slideSpeed * delta;

                float newX = position.x;
                boolean slidingLeft = centerX < wallCenterX;

                float checkX = slidingLeft ? wallBounds.x - 1 : wallBounds.x + wallBounds.width + 1;
                Rectangle neighborCheck = new Rectangle(checkX, wallBounds.y, 1, wallBounds.height);

                if (!isWallAt(neighborCheck, mapObjects, colY)) {
                    if (slidingLeft)
                        newX -= slideAmount;
                    else
                        newX += slideAmount;

                    if (isPositionFree(newX, position.y, mapObjects, this)) {
                        position.x = newX;
                        updateBounds();
                    }
                }
            }
        }
    }

    private boolean isWallAt(Rectangle area, List<GameObject> mapObjects, GameObject ignoreSelf) {
        for (GameObject obj : mapObjects) {
            if (obj == ignoreSelf)
                continue;
            if (obj instanceof Wall) {
                if (area.overlaps(obj.getBounds()))
                    return true;
            }
        }
        return false;
    }

    private boolean isPositionFree(float x, float y, List<GameObject> mapObjects, GameObject ignoreSelf) {
        Rectangle testBounds = new Rectangle(x + 4, y + 4, 8, 8);
        for (GameObject obj : mapObjects) {
            if (obj == ignoreSelf)
                continue;
            if (obj instanceof Wall || obj instanceof Exit) {
                if (obj instanceof Exit && hasKey)
                    continue;

                if (testBounds.overlaps(obj.getBounds())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isBlockEffectRequested() {
        return blockEffectRequested;
    }

    public void clearBlockEffectRequest() {
        this.blockEffectRequested = false;
    }

    public float getCurrentHealth() {
        return (float) this.health;
    }

    public void setCurrentHealth(float health) {
        this.health = (int) health;

        int max = getMaxLives();
        if (this.health > max)
            this.health = max;
        this.health = 0;
    }

    /**
     * Calculates the attack hitbox based on current direction.
     * 
     * @return Rectangle representing the attack area.
     */
    public Rectangle getAttackRect() {
        Rectangle attackBox = new Rectangle(getBounds());
        float range = 32f;

        switch (currentDirection) {
            case UP:
                attackBox.y += attackBox.height;
                attackBox.height = range;
                break;
            case DOWN:
                attackBox.y -= range;
                attackBox.height = range;
                break;
            case LEFT:
                attackBox.x -= range;
                attackBox.width = range;
                break;
            case RIGHT:
                attackBox.x += attackBox.width;
                attackBox.width = range;
                break;
        }
        return attackBox;
    }

}
