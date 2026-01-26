package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A collectable shield that grants temporary invulnerability.
 */
public class ShieldItem extends GameObject implements Collectable {
    private Animation<TextureRegion> animation;
    private float stateTime;

    /**
     * Constructor for ShieldItem.
     * @param x X pos.
     * @param y Y pos.
     */
    public ShieldItem(float x, float y) {

        super(x + 1.5f, y - 3.5f, 13, 23, null);
        loadAnimation();
    }
    
    private void loadAnimation() {
        Texture texture = new Texture(Gdx.files.internal("selfmade/shielditem.png"));

        TextureRegion region = new TextureRegion(texture);
        TextureRegion[] frames = new TextureRegion[] { region };
        
        animation = new Animation<>(0.1f, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        
        this.textureRegion = frames[0];
    }
    
    public void update(float delta) {
        stateTime += delta;
        this.textureRegion = animation.getKeyFrame(stateTime, true);
    }

    /**
     * Activates the character's shield for 10 seconds.
     */
    @Override
    public void collect(Character character) {
        character.activateShield(10.0f);
        setMarkedForRemoval(true);
        System.out.println("Shield Activated! Duration: 10s");
        de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance().onEvent(de.tum.cit.fop.maze.GameControl.EventType.COLLECT_ITEM, 1);
    }
}
