package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A key item required to unlock the level exit.
 */
public class Key extends GameObject{
    /**
     * Constructor for Key.
     * @param x X pos.
     * @param y Y pos.
     * @param width Width.
     * @param height Height.
     * @param textureRegion Texture.
     */
    public Key(float x, float y, float width, float height, TextureRegion textureRegion) {
        super(x, y, width, height, textureRegion);
    }
}
