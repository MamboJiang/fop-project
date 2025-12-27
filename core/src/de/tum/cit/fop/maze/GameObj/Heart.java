package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Heart extends GameObject implements Collectable {
    private Animation<TextureRegion> floatAnimation;
    private float stateTime;

    public Heart(float x, float y) {
        super(x, y, 16, 16, null); // Texture set later
        loadAnimation();
    }
    
    private void loadAnimation() {
        Texture texture = new Texture(Gdx.files.internal("objects.png"));
        TextureRegion[][] tmp = TextureRegion.split(texture, 16, 16);
        
        // Row 3, Cols 0-3
        TextureRegion[] frames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            frames[i] = tmp[3][i];
        }
        
        floatAnimation = new Animation<>(0.15f, frames);
        floatAnimation.setPlayMode(Animation.PlayMode.LOOP);
        
        this.textureRegion = frames[0];
    }
    
    public void update(float delta) {
        stateTime += delta;
        this.textureRegion = floatAnimation.getKeyFrame(stateTime, true);
    }

    @Override
    public void collect(Character character) {
        if (character.getLives() < 4) {
            character.addLives(1);
            setMarkedForRemoval(true);
            System.out.println("Heart Collected! Lives: " + character.getLives());
        }
    }
}
