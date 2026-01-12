package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Represents the player's spawning point in a level.
 */
public class EntryPoint extends GameObject{
    /**
     * Constructor for EntryPoint.
     * @param x X pos.
     * @param y Y pos.
     * @param width Width.
     * @param height Height.
     * @param textureRegion Texture.
     */
    public EntryPoint(float x, float y, float width, float height, TextureRegion textureRegion){
        super(x, y, width, height, textureRegion);
    }
}
