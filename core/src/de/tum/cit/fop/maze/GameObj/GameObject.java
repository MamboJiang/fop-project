package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class GameObject {

    //坐标
    protected Vector2 position;

    //长，宽度
    protected float width;
    protected float height;

    //碰撞检测
    protected Rectangle bounds;

    //贴图
    protected TextureRegion textureRegion;

    //是否可以被移除
    protected boolean markedForRemoval = false;

    //构造函数 Constructor
    public GameObject(float x, float y, float width, float height, TextureRegion textureRegion) {
        this.position = new Vector2(x, y);
        this.width = width;
        this.height = height;
        this.textureRegion = textureRegion;
        // 初始化碰撞箱
        this.bounds = new Rectangle(x, y, width, height);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public void setTextureRegion(TextureRegion textureRegion) {
        this.textureRegion = textureRegion;
    }

    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    public void setMarkedForRemoval(boolean markedForRemoval) {
        this.markedForRemoval = markedForRemoval;
    }
}
