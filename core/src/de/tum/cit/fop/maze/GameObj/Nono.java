package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Nono is a companion character that follows the player.
 */
public class Nono extends GameObject {

    private Character target;
    private float stateTime;
    private Animation<TextureRegion> idleDown, idleLeft, idleRight, idleUp;
    private Animation<TextureRegion> blinkDown, blinkLeft, blinkRight, blinkUp;
    private Animation<TextureRegion> currentAnim;
    private int currentDirection = 0; // 0=Down, 1=Left, 2=Right, 3=Up
    private float hoverOffset = 0f;
    private Vector2 velocity = new Vector2();
    
    // Blink Logic
    private float blinkTimer = 0f;
    private float nextBlinkTime;
    private boolean isBlinking = false;
    private float blinkStateTime = 0f;
    
    // Movement Parameters
    private static final float LEASH_RADIUS = 30f; 
    private static final float MAX_SPEED = 200f; // Increased from 100f
    private static final float FRICTION = 0.9f;

    public Nono(float x, float y, Character target) {
        super(x, y, 12, 12, null); 
        this.target = target;
        this.nextBlinkTime = MathUtils.random(3f, 5f);
        loadAnimation();
    }
    
    private void loadAnimation() {
        Texture texture = new Texture(Gdx.files.internal("assets/player/sprite/nono.png"));
        TextureRegion[][] tmp = TextureRegion.split(texture, 32, 32);
        
        float blinkDuration = 0.1f;

        // Down (Row 0)
        idleDown = new Animation<>(1f, tmp[0][0]);
        blinkDown = new Animation<>(blinkDuration, tmp[0][1], tmp[0][2], tmp[0][1]); // Blink: Open->Half->Shut->Half->Open? User said 2,3 are blinking. Indices 1, 2. Let's do 1->2->1? Or 1->2. Code implies 1-4-1 loop logic for blink often. 
        // User said "23 is blinking". Usually 1 is open, 2 is shut. 
        // Let's assume indices 1 and 2. 
        // Let's do 1 -> 2 -> 1 (Open -> Shut -> Open).
        // Actually simplest is 1->2. If loop mode is Normal.
        
        blinkDown = new Animation<>(blinkDuration, tmp[0][1], tmp[0][2]);
        blinkDown.setPlayMode(Animation.PlayMode.NORMAL);

        // Left (Row 1)
        idleLeft = new Animation<>(1f, tmp[1][0]);
        blinkLeft = new Animation<>(blinkDuration, tmp[1][1], tmp[1][2]);
        blinkLeft.setPlayMode(Animation.PlayMode.NORMAL);

        // Right (Row 2)
        idleRight = new Animation<>(1f, tmp[2][0]);
        blinkRight = new Animation<>(blinkDuration, tmp[2][1], tmp[2][2]);
        blinkRight.setPlayMode(Animation.PlayMode.NORMAL);

        // Up (Row 3)
        idleUp = new Animation<>(1f, tmp[3][0]);
        blinkUp = new Animation<>(blinkDuration, tmp[3][1], tmp[3][2]);
        blinkUp.setPlayMode(Animation.PlayMode.NORMAL);
        
        currentAnim = idleDown;
        this.textureRegion = tmp[0][0];
    }

    private Vector2 objectivePosition = null;
    private java.util.List<GameObject> mapObjects;
    
    public void setMapObjects(java.util.List<GameObject> mapObjects) {
        this.mapObjects = mapObjects;
    }
    
    public void update(float delta) {
        stateTime += delta;
        
        // Hovering effect
        hoverOffset = MathUtils.sin(stateTime * 5f) * 1.5f; 

        if (target != null) {
            // Update objective position
            updateObjective();
            
            // Center-to-center distance check
            float targetCenterX = target.getPosition().x + target.getWidth()/2;
            float targetCenterY = target.getPosition().y + target.getHeight()/2;
            float myCenterX = position.x + width/2;
            float myCenterY = position.y + height/2;
            
            float distToPlayer = Vector2.dst(myCenterX, myCenterY, targetCenterX, targetCenterY);
            
            // Determine where Nono should fly
            Vector2 desiredPosition = new Vector2(targetCenterX, targetCenterY);
            
            if (objectivePosition != null) {
                // Fly towards objective, but not beyond leash range from player
                float objX = objectivePosition.x;
                float objY = objectivePosition.y;
                
                // Calculate direction from player to objective
                float dirX = objX - targetCenterX;
                float dirY = objY - targetCenterY;
                float distToObj = (float) Math.sqrt(dirX * dirX + dirY * dirY);
                
                if (distToObj > 0.1f) {
                    // Normalize direction
                    dirX /= distToObj;
                    dirY /= distToObj;
                    
                    // Nono's ideal position is along the line from player to objective
                    // but clamped to LEASH_RADIUS from player
                    float maxDist = Math.min(LEASH_RADIUS, distToObj);
                    desiredPosition.x = targetCenterX + dirX * maxDist * 0.8f; // 80% of max distance
                    desiredPosition.y = targetCenterY + dirY * maxDist * 0.8f;
                }
            }
            
            // Calculate pull towards desired position
            float pullX = desiredPosition.x - myCenterX;
            float pullY = desiredPosition.y - myCenterY;
            float distToDesired = (float) Math.sqrt(pullX * pullX + pullY * pullY);
            
            // Apply force towards desired position
            if (distToDesired > 2f) { // Dead zone to prevent jitter
                float force = distToDesired * 30f; // Adjust stiffness
                float angle = MathUtils.atan2(pullY, pullX);
                velocity.x += MathUtils.cos(angle) * force * delta;
                velocity.y += MathUtils.sin(angle) * force * delta;
            }
            
            // Apply Velocity
            velocity.scl(FRICTION);
            
            // Clamp to MAX_SPEED
            if (velocity.len() > MAX_SPEED) {
                velocity.setLength(MAX_SPEED);
            }
            
            position.x += velocity.x * delta;
            position.y += velocity.y * delta;
            
        // Determine Direction
        if (velocity.len() > 10f) {
             if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
                 if (velocity.x > 0) currentDirection = 2; // Right
                 else currentDirection = 1; // Left
             } else {
                 if (velocity.y > 0) currentDirection = 3; // Up
                 else currentDirection = 0; // Down
             }
        }
        
        }
        
        // Blink Logic
        if (!isBlinking) {
            blinkTimer += delta;
            if (blinkTimer >= nextBlinkTime) {
                isBlinking = true;
                blinkStateTime = 0f;
                blinkTimer = 0f;
                nextBlinkTime = MathUtils.random(3f, 5f);
            }
        } else {
            blinkStateTime += delta;
            // Check if animation finished
            Animation<TextureRegion> checkAnim = null;
            switch(currentDirection) {
                case 0: checkAnim = blinkDown; break;
                case 1: checkAnim = blinkLeft; break;
                case 2: checkAnim = blinkRight; break;
                case 3: checkAnim = blinkUp; break;
            }
            if (checkAnim != null && checkAnim.isAnimationFinished(blinkStateTime)) {
                isBlinking = false;
            }
        }
        
        // Select Animation
        if (isBlinking) {
            switch(currentDirection) {
                case 0: currentAnim = blinkDown; break;
                case 1: currentAnim = blinkLeft; break;
                case 2: currentAnim = blinkRight; break;
                case 3: currentAnim = blinkUp; break;
            }
            this.textureRegion = currentAnim.getKeyFrame(blinkStateTime, false);
        } else {
            switch(currentDirection) {
                case 0: currentAnim = idleDown; break;
                case 1: currentAnim = idleLeft; break;
                case 2: currentAnim = idleRight; break;
                case 3: currentAnim = idleUp; break;
            }
            this.textureRegion = currentAnim.getKeyFrame(stateTime, true);
        }
    }
    
    private void updateObjective() {
        objectivePosition = null;
        if (mapObjects == null || target == null) return;
        
        float minDst = Float.MAX_VALUE;
        boolean hasKey = target.hasKey();
        
        // Priority order: MaskItem > Key > AttackUnlockItem (if no key) > Exit (if has key)
        for (GameObject obj : mapObjects) {
            if (obj instanceof MaskItem) {
                float dst = Vector2.dst2(target.getPosition().x, target.getPosition().y, 
                                        obj.getPosition().x, obj.getPosition().y);
                if (dst < minDst) {
                    minDst = dst;
                    objectivePosition = new Vector2(obj.getPosition().x + obj.getWidth()/2, 
                                                   obj.getPosition().y + obj.getHeight()/2);
                }
            }
        }
        
        // If no MaskItem, look for Key
        if (objectivePosition == null && !hasKey) {
            for (GameObject obj : mapObjects) {
                if (obj instanceof Key) {
                    float dst = Vector2.dst2(target.getPosition().x, target.getPosition().y, 
                                            obj.getPosition().x, obj.getPosition().y);
                    if (dst < minDst) {
                        minDst = dst;
                        objectivePosition = new Vector2(obj.getPosition().x + obj.getWidth()/2, 
                                                       obj.getPosition().y + obj.getHeight()/2);
                    }
                }
            }
        }
        
        // If no Key and no key held, look for AttackUnlockItem
        if (objectivePosition == null && !hasKey) {
            for (GameObject obj : mapObjects) {
                if (obj instanceof AttackUnlockItem) {
                    float dst = Vector2.dst2(target.getPosition().x, target.getPosition().y, 
                                            obj.getPosition().x, obj.getPosition().y);
                    if (dst < minDst) {
                        minDst = dst;
                        objectivePosition = new Vector2(obj.getPosition().x + obj.getWidth()/2, 
                                                       obj.getPosition().y + obj.getHeight()/2);
                    }
                }
            }
        }
        
        // If has key or no items found, look for Exit
        if (objectivePosition == null || hasKey) {
            for (GameObject obj : mapObjects) {
                if (obj instanceof Exit) {
                    float dst = Vector2.dst2(target.getPosition().x, target.getPosition().y, 
                                            obj.getPosition().x, obj.getPosition().y);
                    if (dst < minDst) {
                        minDst = dst;
                        objectivePosition = new Vector2(obj.getPosition().x + obj.getWidth()/2, 
                                                       obj.getPosition().y + obj.getHeight()/2);
                    }
                }
            }
        }
    }

    public void draw(SpriteBatch batch) {
        if (textureRegion != null) {
            float drawY = position.y + hoverOffset;
            batch.draw(textureRegion, position.x, drawY, width, height);
        }
    }
}
