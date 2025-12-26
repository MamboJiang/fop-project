package de.tum.cit.fop.maze.AI;

import com.badlogic.gdx.math.Vector2;
import java.util.*;

public class PathFinder {
    
    private static class Node implements Comparable<Node> {
        int x, y;
        Node parent;
        float gCost; // Cost from start
        float hCost; // Heuristic to end
        
        public Node(int x, int y, Node parent, float g, float h) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.gCost = g;
            this.hCost = h;
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
    
    public static List<Vector2> findPath(Grid grid, Vector2 startWorld, Vector2 endWorld) {
        int startX = (int)(startWorld.x / 16);
        int startY = (int)(startWorld.y / 16);
        int endX = (int)(endWorld.x / 16);
        int endY = (int)(endWorld.y / 16);
        
        if (!grid.isWalkable(endX, endY)) return null; // Target unreachable
        
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();
        Map<String, Node> nodeMap = new HashMap<>(); // To check if node exists with better path
        
        Node startNode = new Node(startX, startY, null, 0, heuristic(startX, startY, endX, endY));
        openSet.add(startNode);
        nodeMap.put(key(startX, startY), startNode);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            closedSet.add(current);
            
            if (current.x == endX && current.y == endY) {
                return reconstructPath(current);
            }
            
            for (int[] offset : new int[][]{{0,1}, {0,-1}, {1,0}, {-1,0}}) { // 4-Directional
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
        
        return null; // No path found
    }
    
    private static float heuristic(int x1, int y1, int x2, int y2) {
        // Manhattan Distance for 4-way movement
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    
    private static String key(int x, int y) {
        return x + "," + y;
    }
    
    private static List<Vector2> reconstructPath(Node endNode) {
        List<Vector2> path = new ArrayList<>();
        Node current = endNode;
        while (current != null) {
            // Convert back to world coordinates (Center of tile)
            path.add(new Vector2(current.x * 16 + 8, current.y * 16 + 8));
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }
}
