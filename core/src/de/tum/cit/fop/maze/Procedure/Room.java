package de.tum.cit.fop.maze.Procedure;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;

public class Room {
    public int x, y, width, height;
    
    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public boolean intersects(Room other) {
        return (x < other.x + other.width && x + width > other.x &&
                y < other.y + other.height && y + height > other.y);
    }
    
    public Vector2 getCenter() {
        return new Vector2(x + width / 2, y + height / 2);
    }
    
    public Vector2 getRandomPoint() {
        return new Vector2(
            MathUtils.random(x + 1, x + width - 2),
            MathUtils.random(y + 1, y + height - 2)
        );
    }
}
