package de.tum.cit.fop.maze.VFX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

/**
 * Handles the screen transition effect (glass shatter style).
 */
public class TransitionEffect implements Disposable {

    private Texture[] shards;
    private Texture underMask;

    public enum State {
        IDLE,
        SLAM_IN,
        UNDERMASK_IN,
        COVERED,
        SHATTER_OUT
    }
    
    private State currentState = State.IDLE;
    private float stateTime = 0f;
    private Runnable onCoveredAction;
    private boolean actionExecuted = false;

    private static final float SLAM_DURATION = 0.5f;
    private static final float SHATTER_DURATION = 2.0f;
    private static final float MASK_DELAY = 0.05f;

    private Vector2[] shatterDirs;

    /**
     * Constructor for TransitionEffect.
     */
    public TransitionEffect() {
        shards = new Texture[9];
        for (int i = 0; i < 9; i++) {
            shards[i] = new Texture(Gdx.files.internal("selfmade/breakeffect/back" + (i + 1) + ".png"));
        }
        underMask = new Texture(Gdx.files.internal("selfmade/undermask.png"));

        
        shatterDirs = new Vector2[9];
        shatterDirs[0] = new Vector2(-1, 1).nor();
        shatterDirs[1] = new Vector2(0, 1).nor();
        shatterDirs[2] = new Vector2(1, 1).nor();
        shatterDirs[3] = new Vector2(1, 0).nor();
        shatterDirs[4] = new Vector2(1, -1).nor();
        shatterDirs[5] = new Vector2(0, -1).nor();
        shatterDirs[6] = new Vector2(-1, -1).nor();
        shatterDirs[7] = new Vector2(-1, 0).nor();

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

    /**
     * Updates the transition state.
     * @param delta Time delta.
     */
    public void update(float delta) {
        if (currentState == State.IDLE) return;
        
        stateTime += delta;

        switch (currentState) {
            case SLAM_IN:

                if (stateTime >= SLAM_DURATION + 0.2f) {
                    currentState = State.COVERED;
                    stateTime = 0;
                }
                break;
                
            case COVERED:
                if (!actionExecuted && onCoveredAction != null) {
                    onCoveredAction.run();
                    actionExecuted = true;
                }

                if (stateTime >= 0.5f) {
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

    /**
     * Renders the transition effect.
     * @param batch The SpriteBatch.
     */
    public void render(SpriteBatch batch) {
        if (currentState == State.IDLE) return;

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        

        batch.setProjectionMatrix(batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height));
        

        float centerX = width / 2f;
        float centerY = height / 2f;
        

        float normalScale = Math.max(width / 2720f, height / 1568f);
        
        batch.begin();
        

        if (currentState == State.SLAM_IN || currentState == State.COVERED) {

            float effectiveTime = (currentState == State.COVERED) ? SLAM_DURATION : stateTime;
            float progress = MathUtils.clamp(effectiveTime / SLAM_DURATION, 0, 1);

            float scale = Interpolation.bounceOut.apply(3.0f * normalScale, normalScale, progress);
            

            for (int i = 0; i < 9; i++) {
                drawCentered(batch, shards[i], centerX, centerY, scale, 0);
            }
            

            float maskTime = effectiveTime - MASK_DELAY;
            if (maskTime > 0) {
                 float maskProgress = MathUtils.clamp(maskTime / SLAM_DURATION, 0, 1);

                 float finalMaskScale = normalScale * 0.5f; 
                 float maskScale = Interpolation.bounceOut.apply(3.0f * finalMaskScale, finalMaskScale, maskProgress);
                 

                 drawCentered(batch, underMask, centerX, centerY, maskScale, 0);
            }
        }

        else if (currentState == State.SHATTER_OUT) {
             float progress = MathUtils.clamp(stateTime / SHATTER_DURATION, 0, 1);
             float finalMaskScale = normalScale * 0.5f;


             float flyDistance = width * 1.5f; 
             float rotDegrees = 240f;
             

             for (int i = 0; i < 9; i++) {
                 Vector2 dir = shatterDirs[i];
                 float shardProg = Interpolation.pow2Out.apply(0, 1, progress);

                 float offX = dir.x * flyDistance * shardProg;
                 float offY = dir.y * flyDistance * shardProg;
                 float rotation = shardProg * rotDegrees * (i % 2 == 0 ? 1 : -1); 
                 
                 drawCentered(batch, shards[i], centerX + offX, centerY + offY, normalScale, rotation);
             }


             float maskMoveProgress = MathUtils.clamp(progress * 3.0f, 0, 1);
             float moveY = Interpolation.pow2In.apply(0, -height * 1.5f, maskMoveProgress);
             drawCentered(batch, underMask, centerX, centerY + moveY, finalMaskScale, 0);
        }
        
        batch.end();
    }
    
    /**
     * Helper method to draw a texture centered at a specific position with rotation.
     * @param batch The SpriteBatch.
     * @param tex The texture to draw.
     * @param x Center X.
     * @param y Center Y.
     * @param scale Scale factor.
     * @param rotation Rotation in degrees.
     */
    private void drawCentered(SpriteBatch batch, Texture tex, float x, float y, float scale, float rotation) {
        float w = tex.getWidth();
        float h = tex.getHeight();
        float originX = w / 2;
        float originY = h / 2;
        
        batch.draw(tex,
            x - originX, y - originY, 
            originX, originY, 
            w, h, 
            scale, scale, 
            rotation, 
            0, 0, (int)w, (int)h, 
            false, false);
    }

    /**
     * Disposes of the textures used by the transition.
     */
    @Override
    public void dispose() {
        for(Texture t : shards) {
            if (t != null) t.dispose();
        }
        if (underMask != null) underMask.dispose();
    }
}
