package de.tum.cit.fop.maze.GameObj;


import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A collidable obstacle that blocks movement.
 */
public class Wall extends GameObject{
    /**
     * Constructor for Wall.
     * @param x X pos.
     * @param y Y pos.
     * @param width Width.
     * @param height Height.
     * @param textureRegion Texture.
     */
    public Wall(float x, float y, float width, float height, TextureRegion textureRegion){
        super(x, y, width, height, textureRegion);
    }
}
