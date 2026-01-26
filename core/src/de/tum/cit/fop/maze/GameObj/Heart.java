package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A collectable heart that restores health/lives to the player.
 */
public class Heart extends GameObject implements Collectable {
    private Animation<TextureRegion> floatAnimation;
    private float stateTime;

    /**
     * Constructor for Heart.
     * @param x X pos.
     * @param y Y pos.
     */
    public Heart(float x, float y) {
        super(x, y, 16, 16, null);
        loadAnimation();
    }
    
    /**
     * Loads heart animation frames.
     */
    private void loadAnimation() {
        Texture texture = new Texture(Gdx.files.internal("selfmade/maskitem.png"));

        TextureRegion[][] tmp = TextureRegion.split(texture, 32, 32);

        
        TextureRegion region = new TextureRegion(texture);
        

        TextureRegion[] frames = new TextureRegion[1];
        frames[0] = region;
        
        floatAnimation = new Animation<>(0.15f, frames);
        floatAnimation.setPlayMode(Animation.PlayMode.LOOP);
        
        this.textureRegion = frames[0];

    }
    
    /**
     * Updates float animation.
     * @param delta Time delta
     */
    public void update(float delta) {
        stateTime += delta;
        this.textureRegion = floatAnimation.getKeyFrame(stateTime, true);
    }

    /**
     * Restores 1 life to the character.
     */
    @Override
    public void collect(Character character) {
        if (character.getLives() < character.getMaxLives()) {
            character.addLives(1);
            setMarkedForRemoval(true);
            System.out.println("Heart Collected! Lives: " + character.getLives());
            de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance().onEvent(de.tum.cit.fop.maze.GameControl.EventType.COLLECT_ITEM, 1);
        }
    }
}
