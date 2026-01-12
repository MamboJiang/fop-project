package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A static hazard that deals damage when stepped on.
 */
public class Trap extends GameObject{
    /**
     * Constructor for Trap.
     * @param x X pos.
     * @param y Y pos.
     * @param width Width.
     * @param height Height.
     * @param textureRegion Texture.
     */
    public Trap(float x, float y, float width, float height, TextureRegion textureRegion) {
        super(x, y, width, height, textureRegion);
    }
}
