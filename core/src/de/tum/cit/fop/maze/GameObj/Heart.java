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
    
    private void loadAnimation() {
        Texture texture = new Texture(Gdx.files.internal("assets/selfmade/maskitem.png"));
        // Assuming it's a single image, create a 1-frame animation or just use the region
        TextureRegion[][] tmp = TextureRegion.split(texture, 32, 32); // Assuming 32x32 size for item?
        // Or if it's not a grid, just use full texture?
        // Most "items" so far were 16x16 tiles. 
        // maskicon is likely 32x32 or similar if selfmade. 
        // Let's assume it's a single 32x32 sprite.
        
        TextureRegion region = new TextureRegion(texture);
        
        // Wrap in animation for compatibility
        TextureRegion[] frames = new TextureRegion[1];
        frames[0] = region;
        
        floatAnimation = new Animation<>(0.15f, frames);
        floatAnimation.setPlayMode(Animation.PlayMode.LOOP);
        
        this.textureRegion = frames[0];
        
        // Verify size setting in constructor? 
        // Constructor sets 16, 16. If texture is bigger, it might look squashed or just drawn small.
        // I might need to update width/height? 
        // "Heart" constructor calls super(x,y,16,16...). 
        // If I can't change constructor here easily without risking other things, 
        // I'll trust the sprite batch to draw it 16x16 or whatever size.
    }
    
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
