package de.tum.cit.fop.maze.AI;

import com.badlogic.gdx.math.Vector2;
import java.util.*;

/**
 * simple A* PathFinder for Grid-based maps.
 */
public class PathFinder {
    
    public static class Node implements Comparable<Node> {
        public int x, y;
        public Node parent;
        public float gCost; // Cost from start
        public float hCost; // Heuristic to end
        
        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public float fCost() { return gCost + hCost; }

        @Override
        public int compareTo(Node o) {
            return Float.compare(this.fCost(), o.fCost());
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    /**
     * Finds a path from start to end using A* algorithm.
     * @param grid boolean 2D array where true = walkable, false = wall.
     * @param start World position start (will be converted to grid coords).
     * @param end World position end (will be converted to grid coords).
     * @param tileSize Size of each tile (e.g., 16).
     * @return List of World Positions (centers of tiles) for the path, or empty if no path.
     */
    public static List<Vector2> findPath(boolean[][] grid, Vector2 start, Vector2 end, float tileSize) {
        int startX = (int) (start.x / tileSize);
        int startY = (int) (start.y / tileSize);
        int endX = (int) (end.x / tileSize);
        int endY = (int) (end.y / tileSize);
        
        int width = grid.length;
        int height = grid[0].length;
        
        // Bounds check
        if (startX < 0 || startX >= width || startY < 0 || startY >= height || 
            endX < 0 || endX >= width || endY < 0 || endY >= height) {
            return new ArrayList<>();
        }
        
        // Open and Closed sets
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();
        Map<String, Node> allNodes = new HashMap<>(); // Cache nodes to update costs
        
        Node startNode = new Node(startX, startY);
        startNode.gCost = 0;
        startNode.hCost = heuristic(startX, startY, endX, endY);
        
        openSet.add(startNode);
        allNodes.put(key(startX, startY), startNode);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            if (current.x == endX && current.y == endY) {
                return retracePath(current, tileSize);
            }
            
            closedSet.add(current);
            
            // Neighbors (4 directions)
            int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1,0}};
            
            for (int[] dir : dirs) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];
                
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                if (!grid[nx][ny]) continue; // Wall
                
                Node neighbor = allNodes.getOrDefault(key(nx, ny), new Node(nx, ny));
                if (closedSet.contains(neighbor)) continue;
                
                float newGCost = current.gCost + 1; // Distance 1
                
                boolean inOpenSet = openSet.contains(neighbor);
                
                if (!inOpenSet || newGCost < neighbor.gCost) {
                    neighbor.gCost = newGCost;
                    neighbor.hCost = heuristic(nx, ny, endX, endY);
                    neighbor.parent = current;
                    
                    if (!inOpenSet) {
                        openSet.add(neighbor);
                        allNodes.put(key(nx, ny), neighbor);
                    } else {
                        // Re-sort priority queue (inefficient but safe way is remove/add)
                        openSet.remove(neighbor);
                        openSet.add(neighbor);
                    }
                }
            }
        }
        
        return new ArrayList<>(); // No path found
    }
    
    private static String key(int x, int y) {
        return x + "," + y;
    }
    
    private static float heuristic(int x1, int y1, int x2, int y2) {
        // Manhattan distance usually better for 4-way grid
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    
    private static List<Vector2> retracePath(Node endNode, float tileSize) {
        List<Vector2> path = new ArrayList<>();
        Node current = endNode;
        
        while (current != null) {
            // Convert grid coord back to world center coord
            float worldX = current.x * tileSize + tileSize/2;
            float worldY = current.y * tileSize + tileSize/2;
            path.add(new Vector2(worldX, worldY));
            current = current.parent;
        }
        
        Collections.reverse(path);
        return path;
    }
}
