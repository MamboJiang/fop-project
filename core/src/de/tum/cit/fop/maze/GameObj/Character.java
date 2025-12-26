package de.tum.cit.fop.maze.GameObj;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import java.util.List;

public class Character extends GameObject {
    private int lives;
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
    private boolean isMoving;

    // Damage VFX
    private float damageFlashTime = 0f;
    private static final float DAMAGE_DURATION = 1.0f;

    // Navigation Arrow
    private TextureRegion arrowRegion;
    private Vector2 targetPosition; // Position of closest key or exit

    // Physics
    private Vector2 velocity = new Vector2();
    private float acceleration = 800f;
    private float friction = 800f;
    private float maxSpeed = WALK_SPEED;
    private Vector2 inputVector = new Vector2();

    public enum Direction {
        DOWN, RIGHT, UP, LEFT
    }

    public Character(float x, float y) {
        super(x, y, 16, 32, null); // width 16, height 32
        this.lives = 4; // Default lives
        this.speed = WALK_SPEED;
        this.currentDirection = Direction.DOWN;
        this.stateTime = 0f;

        loadAnimations();

        // precise collision bounds (smaller than texture to allow overlap with head/feet properly)
        // Adjusting bounds to be the bottom part of the character for better perspective
        this.bounds = new Rectangle(x+4, y+4, 8, 8);
    }

    public boolean isLevelCompleted() {
        return isLevelCompleted;
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

        // Physics update
        float targetX = inputVector.x * maxSpeed;
        float targetY = inputVector.y * maxSpeed;

        // Apply acceleration/friction
        velocity.x = approach(velocity.x, targetX, (inputVector.x != 0 ? acceleration : friction) * delta);
        velocity.y = approach(velocity.y, targetY, (inputVector.y != 0 ? acceleration : friction) * delta);

        // Animation state
        if (velocity.len() > 10f) {
            isMoving = true;
            // Update direction based on velocity
            if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
                currentDirection = velocity.x > 0 ? Direction.RIGHT : Direction.LEFT;
            } else {
                currentDirection = velocity.y > 0 ? Direction.UP : Direction.DOWN;
            }
        } else {
            isMoving = false;
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

        updateTarget(mapObjects);
    }

    private float approach(float current, float target, float amount) {
        if (current < target) {
            return Math.min(current + amount, target);
        } else {
            return Math.max(current - amount, target);
        }
    }

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
        float cy = position.y + height / 2; // Approximating center, char is 32 high but visual center roughly 16 up?

        float arrowX = cx + MathUtils.cosDeg(angle) * (radius);
        float arrowY = cy + MathUtils.sinDeg(angle) * (radius);

        // Draw centered on arrowX, arrowY
        // Subtract half width/height of arrow to center texture
        float w = 16;
        float h = 16;

        batch.draw(
                arrowRegion,
                arrowX - w/2, arrowY - h/2,
                w/2, h/2,
                w, h,
                1, 1,
                angle - 90 // Adjust rotation so arrow points correctly (assuming texture points UP or RIGHT)
                // If texture points UP (standard), and angle 0 is Right, then rotate -90?
                // If texture points RIGHT, then angle.
                // Let's assume UP. 0 deg in atan2 is RIGHT. arrow UP needs -90 to become RIGHT.
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

    private void updateBounds() {
        this.bounds.setPosition(position.x+4, position.y+4);
    }

    private GameObject checkCollision(List<GameObject> mapObjects) {
        for (GameObject obj : mapObjects) {
            if (obj == this) continue;

            // Wall collision
            if (obj instanceof Wall || obj instanceof Key || obj instanceof Exit || obj instanceof Trap) {
                if (bounds.overlaps(obj.getBounds())) {
                    return obj;
                }
            }
        }
        return null;
    }

    public void takeDamage() {
        if (damageFlashTime <= 0) {
            lives--;
            damageFlashTime = DAMAGE_DURATION;
            // Play sound if possible
        }
    }

    public int getLives() {
        return lives;
    }

    public boolean isDead() {
        return lives <= 0;
    }

    public boolean isDamaged() {
        return damageFlashTime > 0;
    }

    public boolean hasKey() {
        return hasKey;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    // Debug methods
    public void setLives(int lives) {
        this.lives = lives;
        if (this.lives > 4) this.lives = 4;
        if (this.lives < 0) this.lives = 0;
    }

    public void addLives(int amount) {
        this.lives += amount;
        if (this.lives > 4) this.lives = 4;
        if (this.lives < 0) this.lives = 0;
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


