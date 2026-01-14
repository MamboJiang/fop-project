package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.AI.Grid;

/**
 * A Ghost enemy that ignores walls and moves directly towards the player.
 */
public class Ghost extends Enemy {

    /**
     * Constructor for Ghost.
     * @param x X pos.
     * @param y Y pos.
     * @param grid Logic grid (unused for movement, but required by super).
     * @param target Target character.
     */
    public Ghost(float x, float y, Animation<TextureRegion>[] anims, Grid grid, Character target) {
        super(x, y, anims, grid, target);
    }
        
    /**
     * Updates the Ghost's movement logic (flying through walls).
     */
    @Override
    public void update(float delta) {
        stateTime += delta;
        inputVector.set(0, 0);


        float dist = com.badlogic.gdx.math.Vector2.dst(getCenter().x, getCenter().y, getTargetCenter().x, getTargetCenter().y);


        if (currentState == State.RETREAT) {

             inputVector.set(getCenter()).sub(getTargetCenter()).nor();
             this.maxSpeed = 40f;

             if (dist > 160f) {
                 currentState = State.PATROL;
                 this.maxSpeed = 0f;
             }
             
        } else if (currentState == State.CHASE) {

            if (dist > 160f) {
                currentState = State.PATROL;
                this.maxSpeed = 0f;
            } else {

                inputVector.set(getTargetCenter()).sub(getCenter()).nor();
                this.maxSpeed = 30f;
            }
        } else {

            this.maxSpeed = 0f;

            if (dist < 80f) {

                currentState = State.CHASE;

            }
        }

        updatePhysics(delta);
        
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        

        bounds.setPosition(position.x+4, position.y+4);

        updateCombat(delta);

        com.badlogic.gdx.graphics.g2d.Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> currentAnim = walkDown;
        if (velocity.len() > 1f) {
            if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
                if (velocity.x > 0) currentAnim = walkRight;
                else currentAnim = walkLeft;
            } else {
                if (velocity.y > 0) currentAnim = walkUp;
                else currentAnim = walkDown;
            }
            this.textureRegion = currentAnim.getKeyFrame(stateTime, true);
        } else {
            this.textureRegion = walkDown.getKeyFrame(stateTime, true);
        }
    }
    
    /**
     * Draws debug info for Ghost.
     */
    @Override
    public void drawDebug(com.badlogic.gdx.graphics.glutils.ShapeRenderer sr) {

        sr.setColor(com.badlogic.gdx.graphics.Color.YELLOW);
        sr.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        sr.setColor(com.badlogic.gdx.graphics.Color.GREEN);
        sr.circle(getCenter().x, getCenter().y, 80f);

        sr.setColor(com.badlogic.gdx.graphics.Color.RED);
        sr.circle(getCenter().x, getCenter().y, 160f);

        if (currentState == State.CHASE) {
            sr.setColor(com.badlogic.gdx.graphics.Color.CYAN);
            sr.line(getCenter(), getTargetCenter());
        }
    }
}
