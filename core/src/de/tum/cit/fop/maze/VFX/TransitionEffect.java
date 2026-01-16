package de.tum.cit.fop.maze.VFX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public class TransitionEffect implements Disposable {

    private Texture[] shards;
    private Texture underMask;
    
    // State management
    public enum State {
        IDLE,
        SLAM_IN,        // Shards slam in
        UNDERMASK_IN,   // Mask slams in shortly after
        COVERED,        // Fully covered, ready to switch screen
        SHATTER_OUT     // Shards fly out, mask slides down
    }
    
    private State currentState = State.IDLE;
    private float stateTime = 0f;
    private Runnable onCoveredAction;
    private boolean actionExecuted = false;

    // Animation Config
    private static final float SLAM_DURATION = 0.5f;
    private static final float SHATTER_DURATION = 2.0f;
    private static final float MASK_DELAY = 0.05f; // Delay for mask after shards start

    // Shatter vectors (Direction for back1..9)
    // Assuming 1 is TL, going clockwise
    private Vector2[] shatterDirs;

    public TransitionEffect() {
        // Load assets
        shards = new Texture[9];
        for (int i = 0; i < 9; i++) {
            // back1.png to back9.png
            shards[i] = new Texture(Gdx.files.internal("selfmade/breakeffect/back" + (i + 1) + ".png"));
        }
        underMask = new Texture(Gdx.files.internal("selfmade/undermask.png"));

        // Initialize shatter directions
        // 1:TL, 2:T, 3:TR, 4:R, 5:BR, 6:B, 7:BL, 8:L, 9:C (or spiral?)
        // User said: "First is top-left, clockwise rotation".
        // Let's assume grid 3x3:
        // 1 2 3
        // 8 9 4   <-- somewhat clockwise spiral? Or 1 2 3 4 5 6 7 8 9 (TL->TR, etc)?
        // "顺时针旋转" (Rotate clockwise) usually implies the ORDER around the center.
        // Let's guess vectors based on corners and sides to make it look like an explosion.
        
        shatterDirs = new Vector2[9];
        shatterDirs[0] = new Vector2(-1, 1).nor();  // back1 (TL)
        shatterDirs[1] = new Vector2(0, 1).nor();   // back2 (T)
        shatterDirs[2] = new Vector2(1, 1).nor();   // back3 (TR)
        shatterDirs[3] = new Vector2(1, 0).nor();   // back4 (R)
        shatterDirs[4] = new Vector2(1, -1).nor();  // back5 (BR)
        shatterDirs[5] = new Vector2(0, -1).nor();  // back6 (B)
        shatterDirs[6] = new Vector2(-1, -1).nor(); // back7 (BL)
        shatterDirs[7] = new Vector2(-1, 0).nor();  // back8 (L)
        
        // back9 (Center) - User reported random behavior. Fixing to consistent direction.
        // Sending it UP to clear the center (opposing Undermask which slides DOWN)
        shatterDirs[8] = new Vector2(-1, 0).nor();
    }

    /**
     * Starts the transition.
     * @param onCovered The code to run when the screen is fully covered (switch screens here).
     */
    public void start(Runnable onCovered) {
        this.onCoveredAction = onCovered;
        this.currentState = State.SLAM_IN;
        this.stateTime = 0f;
        this.actionExecuted = false;
    }

    public void update(float delta) {
        if (currentState == State.IDLE) return;
        
        stateTime += delta;

        switch (currentState) {
            case SLAM_IN:
                // Check if it's time to trigger undermask (visual only, handled in render logic)
                if (stateTime >= SLAM_DURATION + 0.2f) { // Give it a moment to settle
                    currentState = State.COVERED;
                    stateTime = 0;
                }
                break;
                
            case COVERED:
                if (!actionExecuted && onCoveredAction != null) {
                    onCoveredAction.run();
                    actionExecuted = true;
                }
                
                // Wait 0.4s before shattering
                if (stateTime >= 0.4f) {
                    currentState = State.SHATTER_OUT;
                    stateTime = 0;
                }
                break;
                
            case SHATTER_OUT:
                if (stateTime >= SHATTER_DURATION) {
                    currentState = State.IDLE;
                    onCoveredAction = null;
                }
                break;
                
            default:
                break;
        }
    }

    public void render(SpriteBatch batch) {
        if (currentState == State.IDLE) return;

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        
        // Ensure we are drawing in screen coordinates (UI space)
        // Save previous matrix if needed? Usually for a transition overlay we just want to overwrite.
        // We need a matrix that maps (0,0) to bottom-left and (width,height) to top-right.
        batch.setProjectionMatrix(batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height));
        
        // Center of screen
        float centerX = width / 2f;
        float centerY = height / 2f;
        
        // We draw textures centered.
        // Screen Space Calculation
        float normalScale = Math.max(width / 2720f, height / 1568f); // Cover
        
        batch.begin();
        
        // --- LOGIC FOR SLAM IN ---
        if (currentState == State.SLAM_IN || currentState == State.COVERED) {
            // If COVERED, force progress to 1 (End of Slam) to avoid reset glitch
            float effectiveTime = (currentState == State.COVERED) ? SLAM_DURATION : stateTime;
            float progress = MathUtils.clamp(effectiveTime / SLAM_DURATION, 0, 1);
            
            // Shards Slam: Scale from 3.0 -> normalScale
            float scale = Interpolation.bounceOut.apply(3.0f * normalScale, normalScale, progress);
            
            // Draw Shards
            for (int i = 0; i < 9; i++) {
                drawCentered(batch, shards[i], centerX, centerY, scale, 0);
            }
            
            // Undermask Slam: Starts after delay
            // If COVERED, effectively (SLAM_DURATION - MASK_DELAY) which is > 0
            float maskTime = effectiveTime - MASK_DELAY;
            if (maskTime > 0) {
                 float maskProgress = MathUtils.clamp(maskTime / SLAM_DURATION, 0, 1);
                 // User requested undermask smaller
                 float finalMaskScale = normalScale * 0.5f; 
                 float maskScale = Interpolation.bounceOut.apply(3.0f * finalMaskScale, finalMaskScale, maskProgress);
                 
                 // Draw Mask SECOND (On Top)
                 drawCentered(batch, underMask, centerX, centerY, maskScale, 0);
            }
        }
        
        // --- LOGIC FOR SHATTER OUT ---
        else if (currentState == State.SHATTER_OUT) {
             float progress = MathUtils.clamp(stateTime / SHATTER_DURATION, 0, 1);
             float finalMaskScale = normalScale * 0.5f;

             // Shards fly out
             float flyDistance = width * 1.5f; 
             float rotDegrees = 240f;
             
             // Draw Shards FIRST
             for (int i = 0; i < 9; i++) {
                 Vector2 dir = shatterDirs[i];
                 float shardProg = Interpolation.pow2Out.apply(0, 1, progress);

                 float offX = dir.x * flyDistance * shardProg;
                 float offY = dir.y * flyDistance * shardProg;
                 float rotation = shardProg * rotDegrees * (i % 2 == 0 ? 1 : -1); 
                 
                 drawCentered(batch, shards[i], centerX + offX, centerY + offY, normalScale, rotation);
             }

             // Undermask slides down (Draw LAST / ON TOP)
             // Make it move faster than the shards (e.g. 3x speed of the 2.0s duration)
             float maskMoveProgress = MathUtils.clamp(progress * 3.0f, 0, 1);
             float moveY = Interpolation.pow2In.apply(0, -height * 1.5f, maskMoveProgress);
             drawCentered(batch, underMask, centerX, centerY + moveY, finalMaskScale, 0);
        }
        
        batch.end();
    }
    
    private void drawCentered(SpriteBatch batch, Texture tex, float x, float y, float scale, float rotation) {
        float w = tex.getWidth();
        float h = tex.getHeight();
        float originX = w / 2;
        float originY = h / 2;
        
        // batch.draw(texture, x - originX, y - originY, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth, srcHeight, flipX, flipY)
        batch.draw(tex, 
            x - originX, y - originY, 
            originX, originY, 
            w, h, 
            scale, scale, 
            rotation, 
            0, 0, (int)w, (int)h, 
            false, false);
    }

    @Override
    public void dispose() {
        for(Texture t : shards) {
            if (t != null) t.dispose();
        }
        if (underMask != null) underMask.dispose();
    }
}
