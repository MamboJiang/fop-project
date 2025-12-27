package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ShieldItem extends GameObject implements Collectable {
    private Animation<TextureRegion> animation;
    private float stateTime;

    public ShieldItem(float x, float y) {
        super(x, y, 16, 16, null);
        loadAnimation();
    }
    
    private void loadAnimation() {
        Texture texture = new Texture(Gdx.files.internal("objects.png"));
        TextureRegion[][] tmp = TextureRegion.split(texture, 16, 16);
        
        // Row 3, Cols 4-10 (4,5,6,7,8,9,10 = 7 frames)
        TextureRegion[] frames = new TextureRegion[7];
        for (int i = 0; i < 7; i++) {
            frames[i] = tmp[3][4 + i];
        }
        
        animation = new Animation<>(0.1f, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        
        this.textureRegion = frames[0];
    }
    
    public void update(float delta) {
        stateTime += delta;
        this.textureRegion = animation.getKeyFrame(stateTime, true);
    }

    @Override
    public void collect(Character character) {
        character.activateShield(10.0f);
        setMarkedForRemoval(true);
        System.out.println("Shield Activated! Duration: 10s");
    }
}
