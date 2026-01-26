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

    /**
     * Constructor for Grid.
     * @param mapWidth Width of the map in pixels.
     * @param mapHeight Height of the map in pixels.
     * @param objects List of game objects to check for collisions (Walls).
     */
    public Grid(int mapWidth, int mapHeight, List<GameObject> objects) {

        

        float maxX = 0;
        float maxY = 0;
        for (GameObject obj : objects) {
            maxX = Math.max(maxX, obj.getPosition().x);
            maxY = Math.max(maxY, obj.getPosition().y);
        }
        
        this.width = (int)(maxX / tileSize) + 2;
        this.height = (int)(maxY / tileSize) + 2;
        
        walkable = new boolean[width][height];
        

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                walkable[x][y] = true;
            }
        }
        

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
    
    /**
     * Checks if a specific tile coordinate is walkable.
     * @param x Tile X coordinate.
     * @param y Tile Y coordinate.
     * @return True if walkable, false otherwise.
     */
    public boolean isWalkable(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        return walkable[x][y];
    }
    
    /**
     * Gets the width of the grid.
     * @return Grid width.
     */
    public int getWidth() { return width; }

    /**
     * Gets the height of the grid.
     * @return Grid height.
     */
    public int getHeight() { return height; }
}
