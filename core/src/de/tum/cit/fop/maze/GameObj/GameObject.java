package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * The abstract base class for all objects in the game world.
 * Holds position, dimensions, bounds, and texture data.
 */
public abstract class GameObject {

    protected Vector2 position;

    protected float width;
    protected float height;

    protected Rectangle bounds;

    protected TextureRegion textureRegion;

    protected boolean markedForRemoval = false;

    /**
     * Constructor for GameObject.
     * 
     * @param x             The x-coordinate.
     * @param y             The y-coordinate.
     * @param width         The width of the object.
     * @param height        The height of the object.
     * @param textureRegion The visual texture of the object.
     */
    public GameObject(float x, float y, float width, float height, TextureRegion textureRegion) {
        this.position = new Vector2(x, y);
        this.width = width;
        this.height = height;
        this.textureRegion = textureRegion;

        this.bounds = new Rectangle(x, y, width, height);
    }

    /**
     * @return The position vector.
     */
    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    /**
     * @return Object width.
     */
    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * @return Object height.
     */
    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * @return The collision boundary rectangle.
     */
    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    /**
     * @return The texture region for rendering.
     */
    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public void setTextureRegion(TextureRegion textureRegion) {
        this.textureRegion = textureRegion;
    }

    /**
     * @return True if this object needs to be removed from the game.
     */
    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    public void setMarkedForRemoval(boolean markedForRemoval) {
        this.markedForRemoval = markedForRemoval;
    }
}
