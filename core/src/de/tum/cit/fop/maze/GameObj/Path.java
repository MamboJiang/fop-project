package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A decorative floor tile that entities can walk on.
 */
public class Path extends GameObject {
    /**
     * Constructor for Path.
     * @param x X pos.
     * @param y Y pos.
     * @param width Width.
     * @param height Height.
     * @param textureRegion Texture.
     */
    public Path(float x, float y, float width, float height, TextureRegion textureRegion) {
        super(x, y, width, height, textureRegion);
    }
}
