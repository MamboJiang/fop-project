package de.tum.cit.fop.maze.AI;

import com.badlogic.gdx.math.Vector2;
import java.util.*;

/**
 * Utility class for A* pathfinding.
 */
public class PathFinder {
    
    /**
     * Represents a node in the pathfinding grid.
     */
    private static class Node implements Comparable<Node> {
        int x, y;
        Node parent;
        float gCost;
        float hCost;
        
        /**
         * Constructor for Node.
         * @param x X coordinate.
         * @param y Y coordinate.
         * @param parent Parent node.
         * @param g G cost.
         * @param h H cost.
         */
        public Node(int x, int y, Node parent, float g, float h) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.gCost = g;
            this.hCost = h;
        }
        
        /**
         * Calculates F cost.
         * @return F cost.
         */
        public float fCost() { return gCost + hCost; }
        
        /**
         * Compares nodes based on F cost.
         * @param o Other node.
         * @return Comparison result.
         */
        @Override
        public int compareTo(Node o) {
            return Float.compare(this.fCost(), o.fCost());
        }
        
        /**
         * Checks for equality based on coordinates.
         * @param o Other object.
         * @return True if coordinates match, false otherwise.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return x == node.x && y == node.y;
        }
        
        /**
         * Generates hash code based on coordinates.
         * @return Hash code.
         */
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
    
    /**
     * Finds a path from start to end using A* algorithm.
     * @param grid The navigation grid.
     * @param startWorld Start position in world coordinates.
     * @param endWorld End position in world coordinates.
     * @return List of waypoints (Vector2) representing the path, or null if no path found.
     */
    public static List<Vector2> findPath(Grid grid, Vector2 startWorld, Vector2 endWorld) {
        int startX = (int)(startWorld.x / 16);
        int startY = (int)(startWorld.y / 16);
        int endX = (int)(endWorld.x / 16);
        int endY = (int)(endWorld.y / 16);
        
        if (!grid.isWalkable(endX, endY)) return null;
        
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();
        Map<String, Node> nodeMap = new HashMap<>();
        
        Node startNode = new Node(startX, startY, null, 0, heuristic(startX, startY, endX, endY));
        openSet.add(startNode);
        nodeMap.put(key(startX, startY), startNode);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            closedSet.add(current);
            
            if (current.x == endX && current.y == endY) {
                return reconstructPath(current);
            }
            
            for (int[] offset : new int[][]{{0,1}, {0,-1}, {1,0}, {-1,0}}) {
                int nx = current.x + offset[0];
                int ny = current.y + offset[1];
                
                if (!grid.isWalkable(nx, ny)) continue;
                
                float newGCost = current.gCost + 1;
                Node neighbor = new Node(nx, ny, current, newGCost, heuristic(nx, ny, endX, endY));
                
                if (closedSet.contains(neighbor)) continue;
                
                Node existing = nodeMap.get(key(nx, ny));
                if (existing != null && newGCost >= existing.gCost) continue;
                
                if (existing != null) {
                    openSet.remove(existing);
                    nodeMap.remove(key(nx, ny));
                }
                
                openSet.add(neighbor);
                nodeMap.put(key(nx, ny), neighbor);
            }
        }
        
        return null;
    }
    
    /**
     * Calculates heuristic (Manhattan distance) between two points.
     * @param x1 Start X.
     * @param y1 Start Y.
     * @param x2 End X.
     * @param y2 End Y.
     * @return Heuristic value.
     */
    private static float heuristic(int x1, int y1, int x2, int y2) {

        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    
    /**
     * Generates a unique key for the node coordinates.
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return Key string.
     */
    private static String key(int x, int y) {
        return x + "," + y;
    }
    
    /**
     * Reconstructs the path from the end node to the start.
     * @param endNode The end node.
     * @return List of waypoints.
     */
    private static List<Vector2> reconstructPath(Node endNode) {
        List<Vector2> path = new ArrayList<>();
        Node current = endNode;
        while (current != null) {

            path.add(new Vector2(current.x * 16 + 8, current.y * 16 + 8));
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }
}
