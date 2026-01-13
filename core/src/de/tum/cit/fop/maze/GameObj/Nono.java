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
    private Animation<TextureRegion> animDown, animLeft, animRight, animUp;
    private Animation<TextureRegion> currentAnim;
    private int currentDirection = 0; // 0=Down, 1=Left, 2=Right, 3=Up
    private float hoverOffset = 0f;
    private Vector2 velocity = new Vector2();
    
    // Movement Parameters
    private static final float LEASH_RADIUS = 20f; 
    private static final float MAX_SPEED = 200f; // Increased from 100f
    private static final float FRICTION = 0.9f;

    public Nono(float x, float y, Character target) {
        super(x, y, 12, 12, null); 
        this.target = target;
        loadAnimation();
    }
    
    private void loadAnimation() {
        Texture texture = new Texture(Gdx.files.internal("mobs.png"));
        TextureRegion[][] tmp = TextureRegion.split(texture, 16, 16);
        
        // Rows 4-7 (Indices 4-7) for animations
        // Cols 0-2 (Standard 3 frames)
        float frameDuration = 0.2f;

        animDown = new Animation<>(frameDuration, tmp[4][0], tmp[4][1], tmp[4][2]);
        animLeft = new Animation<>(frameDuration, tmp[5][0], tmp[5][1], tmp[5][2]);
        animRight = new Animation<>(frameDuration, tmp[6][0], tmp[6][1], tmp[6][2]);
        animUp = new Animation<>(frameDuration, tmp[7][0], tmp[7][1], tmp[7][2]);

        animDown.setPlayMode(Animation.PlayMode.LOOP);
        animLeft.setPlayMode(Animation.PlayMode.LOOP);
        animRight.setPlayMode(Animation.PlayMode.LOOP);
        animUp.setPlayMode(Animation.PlayMode.LOOP);
        
        currentAnim = animDown;
        this.textureRegion = tmp[4][1];
    }

    public void update(float delta) {
        stateTime += delta;
        
        // Hovering effect
        hoverOffset = MathUtils.sin(stateTime * 5f) * 1.5f; 

        if (target != null) {
            // Center-to-center distance check
            float targetCenterX = target.getPosition().x + target.getWidth()/2;
            float targetCenterY = target.getPosition().y + target.getHeight()/2;
            float myCenterX = position.x + width/2;
            float myCenterY = position.y + height/2;
            
            float dist = Vector2.dst(myCenterX, myCenterY, targetCenterX, targetCenterY);
            
            // Leash Logic
            if (dist > LEASH_RADIUS) {
                // Calculate pull vector
                float pullX = targetCenterX - myCenterX;
                float pullY = targetCenterY - myCenterY;
                
                // Strength increases with distance beyond leash
                float excess = dist - LEASH_RADIUS;
                float force = excess * 50f; // Increased stiffness for tighter follow
                
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
            
            // Determine Direction from Velocity (if moving significantly)
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

        // Select Animation
        switch(currentDirection) {
            case 0: currentAnim = animDown; break;
            case 1: currentAnim = animLeft; break;
            case 2: currentAnim = animRight; break;
            case 3: currentAnim = animUp; break;
        }
        
        this.textureRegion = currentAnim.getKeyFrame(stateTime, true);
    }

    public void draw(SpriteBatch batch) {
        if (textureRegion != null) {
            float drawY = position.y + hoverOffset;
            batch.draw(textureRegion, position.x, drawY, width, height);
        }
    }
}
