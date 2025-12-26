package de.tum.cit.fop.maze.AI;

import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.GameObj.GameObject;
import de.tum.cit.fop.maze.GameObj.Wall;
import java.util.List;

/**
 * Represents the game map as a navigable grid for pathfinding.
 */
public class Grid {
    private boolean[][] walkable;
    private int width;
    private int height;
    private int tileSize = 16;

    public Grid(int mapWidth, int mapHeight, List<GameObject> objects) {
        // mapWidth/Height are in PIXELS? Or TILES?
        // Let's assume input is in PIXELS for now, or we define grid dimension directly.
        // Standard map is usually e.g. 64x64 tiles or something.
        // Let's calculate grid size based on objects if not provided.
        // For simplicity, let's assume a fixed max size or calculate bounds.
        
        // Dynamic sizing:
        float maxX = 0;
        float maxY = 0;
        for (GameObject obj : objects) {
            maxX = Math.max(maxX, obj.getPosition().x);
            maxY = Math.max(maxY, obj.getPosition().y);
        }
        
        this.width = (int)(maxX / tileSize) + 2;
        this.height = (int)(maxY / tileSize) + 2;
        
        walkable = new boolean[width][height];
        
        // Default to true
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                walkable[x][y] = true;
            }
        }
        
        // Mark walls as non-walkable
        for (GameObject obj : objects) {
            if (obj instanceof Wall) {
                int tax = (int)(obj.getPosition().x / tileSize);
                int tay = (int)(obj.getPosition().y / tileSize);
                if (tax >= 0 && tax < width && tay >= 0 && tay < height) {
                    walkable[tax][tay] = false;
                }
            }
        }
    }
    
    public boolean isWalkable(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        return walkable[x][y];
    }
    
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
