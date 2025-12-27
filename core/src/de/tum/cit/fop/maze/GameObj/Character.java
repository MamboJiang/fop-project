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
import java.util.List;

public class Character extends MovableObject {
    // private int lives; // Removed, using health
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
    
    // Damage VFX (Moved to MovableObject, but local fields removed)
    // private float damageFlashTime = 0f; 
    // private static final float DAMAGE_DURATION = 0.1f;

    // Navigation Arrow
    private TextureRegion arrowRegion;
    private Vector2 targetPosition; 

    public enum Direction {
        DOWN, RIGHT, UP, LEFT
    }

    public Character(float x, float y) {
        super(x, y, 16, 32, null); 
        this.health = 4; // Use inherited health
        this.speed = WALK_SPEED;
        
        // Physics Init
        this.maxSpeed = WALK_SPEED;
        this.acceleration = 800f;
        this.friction = 800f;
        
        this.currentDirection = Direction.DOWN;
        this.stateTime = 0f;

        loadAnimations();

        this.bounds = new Rectangle(x+4, y+4, 8, 8);
    }
    
    public void setPosition(float x, float y) {
        this.position.set(x, y);
        updateBounds();
    }

    // ... (isLevelCompleted and loadAnimations omitted, assumed unchanged if not in range) ...
    // NOTE: Replace does not support "..." expansion, so I must include everything I am replacing.
    // I will target the class definition up to updatePhysics usage.

    public boolean isLevelCompleted() {
        return isLevelCompleted;
    }
    
    public void resetForNewLevel() {
        this.isLevelCompleted = false;
        this.hasKey = false;
        this.targetPosition = null;
    }

    private void loadAnimations() {
        Texture texture = new Texture(Gdx.files.internal("character.png"));
        TextureRegion[][] tmp = TextureRegion.split(texture, 16, 32);

        Texture thingsTexture = new Texture(Gdx.files.internal("assets/things.png"));
        TextureRegion[][] thingsTmp = TextureRegion.split(thingsTexture, 16, 16);
        arrowRegion = thingsTmp[4][0];


        // Helper to extract first 4 frames
        TextureRegion[] downFrames = new TextureRegion[4];
        TextureRegion[] rightFrames = new TextureRegion[4];
        TextureRegion[] upFrames = new TextureRegion[4];
        TextureRegion[] leftFrames = new TextureRegion[4];

        int index = 0;
        for (int i = 0; i < 4; i++) {
            downFrames[i] = tmp[0][i];
            rightFrames[i] = tmp[1][i];
            upFrames[i] = tmp[2][i];
            leftFrames[i] = tmp[3][i];
        }

        // Row 0: Down
        walkDown = new Animation<>(0.1f, downFrames);
        // Row 1: Right
        walkRight = new Animation<>(0.1f, rightFrames);
        // Row 2: Up
        walkUp = new Animation<>(0.1f, upFrames);
        // Row 3: Left
        walkLeft = new Animation<>(0.1f, leftFrames);

        // Set initial texture
        this.textureRegion = downFrames[0];
    }

    private void collisionAddressing(GameObject hitObject, float oldPosition, boolean isXAxis){
        if (hitObject != null) {
            if(hitObject instanceof Wall){
                if (isXAxis) {
                    this.position.x = oldPosition;
                    this.velocity.x = 0;
                } else {
                    this.position.y = oldPosition;
                    this.velocity.y = 0;
                }
                updateBounds();
            }
            else if(hitObject instanceof Key){
                this.hasKey = true;
                hitObject.setMarkedForRemoval(true);
                System.out.println("Key collected!");
            }
            else if(hitObject instanceof Collectable){
                ((Collectable) hitObject).collect(this);
            }
            else if(hitObject instanceof Exit){
                if(this.hasKey){
                    this.isLevelCompleted = true;
                    System.out.println("Level Completed!");
                }
                else{
                    if (isXAxis) {
                        this.position.x = oldPosition;
                        this.velocity.x = 0;
                    } else {
                        this.position.y = oldPosition;
                        this.velocity.y = 0;
                    }
                    updateBounds();
                }
            }
            else if (hitObject instanceof Trap) {
                // 1. 扣血
                this.takeDamage();
                System.out.println("Stepped on a trap! Lives left: " + getLives());
            }
        }
    }

    public void update(float delta, List<GameObject> mapObjects, de.tum.cit.fop.maze.GameControl.ConfigManager configManager) {
        stateTime += delta;

        handleInput(configManager);

        // Physics update (Inherited)
        updatePhysics(delta);

        // Animation state
        if (isMoving) {
            // Update direction based on velocity
            if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
                currentDirection = velocity.x > 0 ? Direction.RIGHT : Direction.LEFT;
            } else {
                currentDirection = velocity.y > 0 ? Direction.UP : Direction.DOWN;
            }
        }

        if (velocity.len() > 1f) {
            float oldX = position.x;
            float oldY = position.y;

            // Move X
            position.x += velocity.x * delta;
            updateBounds();
            GameObject colX = checkCollision(mapObjects);
            collisionAddressing(colX, oldX, true);


            // Move Y
            position.y += velocity.y * delta;
            updateBounds();
            GameObject colY = checkCollision(mapObjects);
            collisionAddressing(colY, oldY, false);


            // Corner Sliding Logic
            handleWallSliding(delta, mapObjects, colX, colY);
        }

        // Update texture based on animation
        Animation<TextureRegion> currentAnim;
        switch (currentDirection) {
            case DOWN: currentAnim = walkDown; break;
            case RIGHT: currentAnim = walkRight; break;
            case UP: currentAnim = walkUp; break;
            case LEFT: currentAnim = walkLeft; break;
            default: currentAnim = walkDown; break;
        }

        if (isMoving) {
            this.textureRegion = currentAnim.getKeyFrame(stateTime, true);
        } else {
            this.textureRegion = currentAnim.getKeyFrame(0, true); // Stand still frame
        }

        // Handle damage flash
        if (damageFlashTime > 0) {
            damageFlashTime -= delta;
        }
        
        // Handle Invincibility
        if (invincibleTime > 0) {
            invincibleTime -= delta;
        }
        
        // Handle Shield
        if (shieldTime > 0) {
            shieldTime -= delta;
        }

        updateTarget(mapObjects);
    }
    
    // Shield Logic
    private float shieldTime = 0f;
    private Animation<TextureRegion> shieldAnimation;
    
    public void activateShield(float duration) {
        if (shieldAnimation == null) {
            loadShieldAnimation();
        }
        this.shieldTime = duration;
    }
    
    private void loadShieldAnimation() {
         Texture texture = new Texture(Gdx.files.internal("objects.png"));
         TextureRegion[][] tmp = TextureRegion.split(texture, 16, 16);
         TextureRegion[] frames = new TextureRegion[7];
         for (int i = 0; i < 7; i++) {
             frames[i] = tmp[3][4 + i];
         }
         shieldAnimation = new Animation<>(0.1f, frames);
         shieldAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }
    
    public boolean isShielded() {
        return shieldTime > 0;
    }

    public void draw(SpriteBatch batch) {
        // boolean isFlashing = damageFlashTime > 0; // inherited
        
        setupDamageFlash(batch);
        
        batch.draw(textureRegion, position.x, position.y, width, height);

        endDamageFlash(batch);
        
        drawArrow(batch);
        
        // Reset Color (Important! Otherwise everything else becomes transparent/tinted)
        batch.setColor(Color.WHITE);
        
            // Draw Shield Overlay
            if (shieldTime > 0 && shieldAnimation != null) {
                TextureRegion shieldFrame = shieldAnimation.getKeyFrame(stateTime, true);
                
                // Semi-transparent
                batch.setColor(1, 1, 1, 0.5f); // 50% opacity
                
                // User Request: Allow slight stretching and manual scale adjustment
                float scaleX = 1.2f; // Adjust this for horizontal stretch (e.g. 1.2f)
                float scaleY = 1.5f; // Adjust this for vertical stretch (e.g. 1.1f)
                
                float actualWidth = 16f * scaleX;
                float actualHeight = 16f * scaleY;
                
                // Center X: SpriteCenter (pos.x + 8) - HalfNewWidth
                float drawX = (position.x + 8) - (actualWidth / 2);
                
                // Bottom Y: Aligned with Hitbox Bottom (pos.y + 4) or slightly offset (+6)
                // Growing height keeps bottom fixed.
                float drawY = position.y + 5; 
                
                batch.draw(shieldFrame, drawX, drawY, actualWidth, actualHeight);
                
                batch.setColor(Color.WHITE);
            }
    }

    
    // approach helper removed as it is now in MovableObject
    
    private void updateTarget(List<GameObject> mapObjects) {
        targetPosition = null;
        float minDst = Float.MAX_VALUE;

        // If has Key, look for Exit. Else look for Key.
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

        // If searching for key but none found (e.g. all collected or none exist), target Exit
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

    public void drawArrow(SpriteBatch batch) {
        if (targetPosition == null) return;

        float angle = MathUtils.atan2(targetPosition.y - position.y, targetPosition.x - position.x) * MathUtils.radiansToDegrees;
        float radius = 20f;

        // Center of character
        float cx = position.x + width / 2;
        float cy = position.y + height / 2; 

        float arrowX = cx + MathUtils.cosDeg(angle) * (radius);
        float arrowY = cy + MathUtils.sinDeg(angle) * (radius);

        float w = 16;
        float h = 16;

        batch.draw(
                arrowRegion,
                arrowX - w/2, arrowY - h/2,
                w/2, h/2,
                w, h,
                1, 1,
                angle - 90 
        );
    }

    private void handleInput(de.tum.cit.fop.maze.GameControl.ConfigManager configManager) {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            maxSpeed = RUN_SPEED;
        } else {
            maxSpeed = WALK_SPEED;
        }

        inputVector.set(0, 0);
        if (Gdx.input.isKeyPressed(configManager.getKey("UP"))) inputVector.y = 1;
        if (Gdx.input.isKeyPressed(configManager.getKey("DOWN"))) inputVector.y = -1;
        if (Gdx.input.isKeyPressed(configManager.getKey("LEFT"))) inputVector.x = -1;
        if (Gdx.input.isKeyPressed(configManager.getKey("RIGHT"))) inputVector.x = 1;

        if (inputVector.len2() > 0) {
            inputVector.nor(); // Normalize for consistent diagonal speed
        }
    }
    
    // Bounds update logic stays here
    private void updateBounds() {
        this.bounds.setPosition(position.x+4, position.y+4);
    }

    private GameObject checkCollision(List<GameObject> mapObjects) {
        for (GameObject obj : mapObjects) {
            if (obj == this) continue;

            // Wall collision
            if (obj instanceof Wall || obj instanceof Key || obj instanceof Exit || obj instanceof Trap || obj instanceof Collectable) {
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
    
    // Override takeDamage to add invincibility and specific effects
    @Override
    public void takeDamage(int amount) {
        if (invincibleTime > 0) return; // Prevent damage if invincible
        
        if (damageFlashTime <= 0) {
            if (!infiniteHP) {
                // Character damage is usually 1 "life" regardless of amount unless specified
                // But let's respect amount if needed. For now default is 1.
                // Assuming amount is 1 for traps/enemies usually.
                super.takeDamage(amount);
            } else {
                 // Even with infinite HP, show flash? 
                 damageFlashTime = FLASH_DURATION;
            }
            
            // On top of base logic:
            invincibleTime = INVINCIBLE_DURATION; 
            screenShakeRequested = true;
            damageNumberRequested = true;
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
        return health; // In Character, health = lives
    }
    
    // isDead() inherited
    // isDamaged() inherited

    public boolean hasKey() {
        return hasKey;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }
    
    // Debug methods
    public void setLives(int lives) {
        this.health = lives;
        if (this.health > 4) this.health = 4;
        if (this.health < 0) this.health = 0;
    }
    
    public void addLives(int amount) {
        this.health += amount;
        if (this.health > 4) this.health = 4;
        if (this.health < 0) this.health = 0;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    private void handleWallSliding(float delta, List<GameObject> mapObjects, GameObject colX, GameObject colY) {
        float SLIDE_THRESHOLD = 8.0f; // User requested 8.0f
        float slideSpeed = 100f; 

        // Case 1: Hitting Vertical Wall (X-Collision), trying to move Horizontally
        if (colX instanceof Wall && Math.abs(inputVector.x) > 0 && Math.abs(inputVector.y) == 0) {
            Rectangle wallBounds = colX.getBounds();
            float overlapY = Math.min(bounds.y + bounds.height, wallBounds.y + wallBounds.height) - Math.max(bounds.y, wallBounds.y);
            
            if (overlapY > 0 && overlapY <= SLIDE_THRESHOLD) {
                float centerY = bounds.y + bounds.height/2;
                float wallCenterY = wallBounds.y + wallBounds.height/2;
                float slideAmount = slideSpeed * delta;
                
                float newY = position.y;
                boolean slidingDown = centerY < wallCenterY;
                
                // Continuity Check: Is there a wall in the direction we want to slide?
                // If sliding down, check directly below the current wall.
                // If sliding up, check directly above.
                float checkY = slidingDown ? wallBounds.y - 1 : wallBounds.y + wallBounds.height + 1;
                // Check a thin strip along the wall's vertical seam
                Rectangle neighborCheck = new Rectangle(wallBounds.x, checkY, wallBounds.width, 1);
                
                if (!isWallAt(neighborCheck, mapObjects, colX)) { // Only slide if NO wall there
                    if (slidingDown) newY -= slideAmount;
                    else newY += slideAmount;

                    if (isPositionFree(position.x, newY, mapObjects, this)) {
                        position.y = newY;
                        updateBounds();
                    }
                }
            }
        }

        // Case 2: Hitting Horizontal Wall (Y-Collision), trying to move Vertically
        if (colY instanceof Wall && Math.abs(inputVector.y) > 0 && Math.abs(inputVector.x) == 0) {
            Rectangle wallBounds = colY.getBounds();
            float overlapX = Math.min(bounds.x + bounds.width, wallBounds.x + wallBounds.width) - Math.max(bounds.x, wallBounds.x);
            
            if (overlapX > 0 && overlapX <= SLIDE_THRESHOLD) {
                float centerX = bounds.x + bounds.width/2;
                float wallCenterX = wallBounds.x + wallBounds.width/2;
                float slideAmount = slideSpeed * delta;
                
                float newX = position.x;
                boolean slidingLeft = centerX < wallCenterX;
                
                // Continuity Check
                float checkX = slidingLeft ? wallBounds.x - 1 : wallBounds.x + wallBounds.width + 1;
                Rectangle neighborCheck = new Rectangle(checkX, wallBounds.y, 1, wallBounds.height);
                
                if (!isWallAt(neighborCheck, mapObjects, colY)) {
                    if (slidingLeft) newX -= slideAmount;
                    else newX += slideAmount;

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
            if (obj == ignoreSelf) continue;
            if (obj instanceof Wall) {
                if (area.overlaps(obj.getBounds())) return true;
            }
        }
        return false;
    }

    private boolean isPositionFree(float x, float y, List<GameObject> mapObjects, GameObject ignoreSelf) {
        // Temporarily move bounds to check collision
        Rectangle testBounds = new Rectangle(x+4, y+4, 8, 8); // Match constructor logic
        for (GameObject obj : mapObjects) {
            if (obj == ignoreSelf) continue;
            if (obj instanceof Wall || obj instanceof Exit) {
                 if (obj instanceof Exit && hasKey) continue; // Passable if has key
                 
                 if (testBounds.overlaps(obj.getBounds())) {
                     return false;
                 }
            }
        }
        return true;
    }


}


