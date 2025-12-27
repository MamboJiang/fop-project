package de.tum.cit.fop.maze.Procedure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.GameObj.*;
import de.tum.cit.fop.maze.MapLoader;

import java.util.ArrayList;
import java.util.List;

public class DungeonGenerator {
    
    // Tile Types
    private static final int WALL = 0;
    private static final int FLOOR = 1;
    
    private int width, height;
    private int[][] map;
    private List<Room> rooms;
    
    // Textures (Loaded once or passed in?)
    // Basic Tiles
    private TextureRegion wallRegion;
    private TextureRegion floorRegion;
    private TextureRegion entryRegion;
    private TextureRegion exitRegion;
    private TextureRegion trapRegion;
    private TextureRegion chestRegion; // Key/Chest
    
    public DungeonGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        this.map = new int[width][height];
        this.rooms = new ArrayList<>();
        
        loadResources();
    }
    
    private void loadResources() {
        Texture texture = new Texture(Gdx.files.internal("basictiles.png"));
        TextureRegion[][] regions = TextureRegion.split(texture, 16, 16);
        
        wallRegion = regions[0][0];      // Wall
        floorRegion = regions[1][1];     // Floor
        entryRegion = regions[6][0];     // Entry
        exitRegion = regions[6][2];      // Exit
        trapRegion = regions[9][2];      // Trap
        chestRegion = regions[4][4];     // Key
    }
    
    public List<GameObject> generate(int difficultyLevel) {
        // Reset
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = WALL;
            }
        }
        rooms.clear();
        
        // 1. Place Rooms
        int maxRooms = 10 + difficultyLevel; // More rooms as we go deeper?
        int attempts = 50;
        
        for (int i = 0; i < attempts && rooms.size() < maxRooms; i++) {
            int w = MathUtils.random(6, 12); // Room width
            int h = MathUtils.random(6, 12); // Room height
            int x = MathUtils.random(1, width - w - 1);
            int y = MathUtils.random(1, height - h - 1);
            
            Room newRoom = new Room(x, y, w, h);
            
            boolean overlaps = false;
            for (Room r : rooms) {
                if (newRoom.intersects(r)) {
                    overlaps = true;
                    break;
                }
            }
            
            if (!overlaps) {
                rooms.add(newRoom);
                carveRoom(newRoom);
            }
        }
        
        // 2. Connect Rooms
        for (int i = 0; i < rooms.size() - 1; i++) {
            Room r1 = rooms.get(i);
            Room r2 = rooms.get(i + 1);
            
            connectRooms(r1, r2);
        }
        
        // 3. Generate Objects
        List<GameObject> objects = new ArrayList<>();
        
        // Create Floor & Walls
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float wx = x * 16;
                float wy = y * 16;
                
                if (map[x][y] == FLOOR) {
                    objects.add(new Path(wx, wy, 16, 16, floorRegion));
                } else {
                    objects.add(new Wall(wx, wy, 16, 16, wallRegion));
                }
            }
        }
        
        // Helper to track occupied tiles (to prevent stacking traps on keys/exits)
        boolean[][] occupied = new boolean[width][height];
        
        // 4. Place Special Items
        if (!rooms.isEmpty()) {
            // First Room: Entry
            Room startRoom = rooms.get(0);
            Vector2 entryPos = startRoom.getCenter();
            objects.add(new EntryPoint(entryPos.x * 16, entryPos.y * 16, 16, 16, entryRegion));
            occupied[(int)entryPos.x][(int)entryPos.y] = true;
            
            // Last Room: Exit
            Room endRoom = rooms.get(rooms.size() - 1);
            Vector2 exitPos = endRoom.getCenter();
            objects.add(new Exit(exitPos.x * 16, exitPos.y * 16, 16, 16, exitRegion));
            occupied[(int)exitPos.x][(int)exitPos.y] = true;
            
            // Key (Random Room except Start/End)
            if (rooms.size() > 2) {
                Room keyRoom = rooms.get(MathUtils.random(1, rooms.size() - 2));
                Vector2 keyPos = keyRoom.getCenter();
                objects.add(new Key(keyPos.x * 16, keyPos.y * 16, 16, 16, chestRegion));
                occupied[(int)keyPos.x][(int)keyPos.y] = true;
            } else {
                // Determine fallback if few rooms
                // Try to find a spot near exit but not ON exit
                int ex = (int)exitPos.x;
                int ey = (int)exitPos.y;
                // Just shifted by 2 tiles
                int kx = ex + 2; 
                int ky = ey;
                if (kx >= width) kx = ex - 2;
                
                objects.add(new Key(kx * 16, ky * 16, 16, 16, chestRegion));
                if (kx > 0 && kx < width) occupied[kx][ky] = true;
            }
            
            // 5. Populate Enemies & Traps
            // Difficulty Logic
            int enemyCountPerRoom = 1 + (difficultyLevel / 2);
            int trapCountPerRoom = 1 + (difficultyLevel / 3);
            
            for (int i = 1; i < rooms.size(); i++) { // Skip start room
                Room r = rooms.get(i);
                
                // Enemies
                for (int j = 0; j < enemyCountPerRoom; j++) {
                     // Try to find free spot
                     Vector2 pos = getFreeRandomPoint(r, occupied);
                     if (pos == null) continue; // Room too full
                     
                     occupied[(int)pos.x][(int)pos.y] = true; 
                     
                     boolean isGhost = MathUtils.randomBoolean();
                     if (isGhost) {
                          objects.add(new GhostSpawnPoint(pos.x * 16, pos.y * 16, 16, 16, MapLoader.getMobAnimations(2, 1)[0].getKeyFrame(0))); 
                     } else {
                          objects.add(new EnemySpawnPoint(pos.x * 16, pos.y * 16, 16, 16, MapLoader.getMobAnimations(0, 0)[0].getKeyFrame(0)));
                     }
                }
                
                // Traps
                for (int j = 0; j < trapCountPerRoom; j++) {
                     Vector2 pos = getFreeRandomPoint(r, occupied);
                     if (pos == null) continue;
                     
                     occupied[(int)pos.x][(int)pos.y] = true;
                     objects.add(new Trap(pos.x * 16, pos.y * 16, 16, 16, trapRegion));
                }
            }
        }
        
        return objects;
    }
    
    // Helper to find valid point in room that isn't occupied
    private Vector2 getFreeRandomPoint(Room r, boolean[][] occupied) {
        for(int k=0; k<10; k++) {
            Vector2 p = r.getRandomPoint();
            int ix = (int)p.x;
            int iy = (int)p.y;
            if (!occupied[ix][iy]) {
                return p;
            }
        }
        return null; // Describe failure to find spot
    }
    
    private void carveRoom(Room room) {
        for (int x = room.x; x < room.x + room.width; x++) {
            for (int y = room.y; y < room.y + room.height; y++) {
                map[x][y] = FLOOR;
            }
        }
    }
    
    private void connectRooms(Room r1, Room r2) {
        Vector2 c1 = r1.getCenter();
        Vector2 c2 = r2.getCenter();
        
        int x1 = (int)c1.x;
        int y1 = (int)c1.y;
        int x2 = (int)c2.x;
        int y2 = (int)c2.y;
        
        // Randomly choose X-then-Y or Y-then-X
        if (MathUtils.randomBoolean()) {
            carveHCorridor(x1, x2, y1);
            carveVCorridor(y1, y2, x2);
        } else {
            carveVCorridor(y1, y2, x1);
            carveHCorridor(x1, x2, y2);
        }
    }
    
    private void carveHCorridor(int x1, int x2, int y) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            carvePoint(x, y);
            carvePoint(x, y+1); // Wide corridors (2 tiles) for easy movement
        }
    }
    
    private void carveVCorridor(int y1, int y2, int x) {
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            carvePoint(x, y);
            carvePoint(x+1, y);
        }
    }
    
    private void carvePoint(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1) {
             map[x][y] = FLOOR;
        }
    }
}
