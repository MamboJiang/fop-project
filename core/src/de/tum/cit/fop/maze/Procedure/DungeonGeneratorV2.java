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

/**
 * Procedural generator for Endless Mode Ver2.
 * Features:
 * - Linear chain of rooms.
 * - Odd-sized rectangular rooms for centered doors.
 * - Centered edge-to-edge path connections.
 * - Boss room logic at level exits.
 */
public class DungeonGeneratorV2 {

    private static final int WALL = 0;
    private static final int FLOOR = 1;
    
    private int width, height;
    private int[][] map;
    private List<Room> rooms;

    private TextureRegion wallRegion;
    private TextureRegion floorRegion;
    private TextureRegion entryRegion;
    private TextureRegion exitRegion;
    private TextureRegion trapRegion;
    private TextureRegion chestRegion;

    public DungeonGeneratorV2(int width, int height) {
        this.width = width;
        this.height = height;
        this.map = new int[width][height];
        this.rooms = new ArrayList<>();
        loadResources();
    }
    
    private void loadResources() {
        Texture texture = new Texture(Gdx.files.internal("assets/selfmade/basictile.png"));
        TextureRegion[][] regions = TextureRegion.split(texture, 32, 32);
        
        wallRegion = regions[0][0];
        floorRegion = regions[1][0]; 
        entryRegion = regions[0][2];
        exitRegion = regions[0][1];
        trapRegion = regions[2][2];
        chestRegion = regions[1][1];
    }
    
    /**
     * Generates a Ver2 dungeon.
     * @param difficulty Floor/Difficulty index.
     * @param isBossLevel If true, the last room will be a Boss room.
     * @return List of GameObjects.
     */
    public List<GameObject> generate(int difficulty, boolean isBossLevel) {
        // Reset Map
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = WALL;
            }
        }
        rooms.clear();

        // 1. Place Entry Room (Center of Map roughly)
        int cx = width / 2;
        int cy = height / 2;
        // Odd size rooms: 9 to 15
        int startW = MathUtils.random(4, 7) * 2 + 1; 
        int startH = MathUtils.random(4, 7) * 2 + 1;
        
        Room startRoom = new Room(cx - startW/2, cy - startH/2, startW, startH);
        rooms.add(startRoom);
        carveRoom(startRoom);
        
        Room currentRoom = startRoom;
        
        // 2. Chain Generation
        int targetRooms = 6 + MathUtils.random(2); // 6 to 8 rooms
        
        // For boss levels, ensure at least 4 rooms to prevent entry-exit direct connection
        if (isBossLevel && targetRooms < 4) {
            targetRooms = 4;
        }
        
        int attempts = 0;
        int maxAttempts = 100;
        
        while (rooms.size() < targetRooms && attempts < maxAttempts) {
            attempts++;
            
            // Pick a direction: 0=Up, 1=Right, 2=Down, 3=Left
            int dir = MathUtils.random(0, 3);
            
            // Corridor Length: 10 to 15 (Similar to room sizes 9-15)
            int corridorLen = MathUtils.random(10, 15);
            
            // New Room Size
            int newW = MathUtils.random(4, 7) * 2 + 1;
            int newH = MathUtils.random(4, 7) * 2 + 1;
            
            // Calculate Position
            int nx = 0, ny = 0;
            // Center alignment
            int prevCenterX = currentRoom.x + currentRoom.width / 2;
            int prevCenterY = currentRoom.y + currentRoom.height / 2;
            
            if (dir == 0) { // UP
                // New room bottom edge aligns with prev top edge + len
                // X center aligned
                nx = prevCenterX - newW / 2;
                ny = currentRoom.y + currentRoom.height + corridorLen;
                
            } else if (dir == 1) { // RIGHT
                nx = currentRoom.x + currentRoom.width + corridorLen;
                ny = prevCenterY - newH / 2;
                
            } else if (dir == 2) { // DOWN
                nx = prevCenterX - newW / 2;
                ny = currentRoom.y - corridorLen - newH;
                
            } else if (dir == 3) { // LEFT
                nx = currentRoom.x - corridorLen - newW;
                ny = prevCenterY - newH / 2;
            }
            
            // Validation
            if (nx < 2 || ny < 2 || nx + newW > width - 2 || ny + newH > height - 2) {
                continue; // Out of bounds
            }
            
            Room newRoom = new Room(nx, ny, newW, newH);
            
            // Overlap Check (with padding)
            boolean overlaps = false;
            for (Room r : rooms) {
                // Determine collision with 2-tile padding to ensure walls
                if (rectsIntersect(newRoom.x - 2, newRoom.y - 2, newRoom.width + 4, newRoom.height + 4,
                                   r.x, r.y, r.width, r.height)) {
                    overlaps = true;
                    break;
                }
            }
            
            if (!overlaps) {
                rooms.add(newRoom);
                carveRoom(newRoom);
                connectRooms(currentRoom, newRoom, dir, corridorLen);
                currentRoom = newRoom;
                attempts = 0; // Reset attempts after success
            }
        }
        
        // 2.5 Branching Generation
        // Try to branch out from existing rooms to add complexity
        int branchTarget = 10; // Increased target
        int branchCount = 0;
        
        // Create a copy of rooms to iterate safely while adding new rooms
        List<Room> mainPathRooms = new ArrayList<>(rooms);
        
        // Skip Start (0) and End (last)
        for (int i = 1; i < mainPathRooms.size() - 1 && branchCount < branchTarget; i++) {
             if (MathUtils.randomBoolean(0.9f)) { // 90% chance to TRY branching
                 Room baseRoom = mainPathRooms.get(i);
                 // Try multiple times to find a valid branch
                 boolean success = false;
                 for(int k=0; k<4; k++) {
                     if(attemptBranch(baseRoom)) {
                         success = true;
                         // 50% chance to add ANOTHER branch to the same room (Hub room)
                         if (!MathUtils.randomBoolean(0.5f)) break; 
                     }
                 }
                 if (success) branchCount++;
             }
        }

        // 3. Object Placement
        return placeObjects(difficulty, isBossLevel);
    }
    
    private boolean attemptBranch(Room r) {
        int dir = MathUtils.random(0, 3);
        int corridorLen = MathUtils.random(5, 10);
        int newW = MathUtils.random(4, 6) * 2 + 1;
        int newH = MathUtils.random(4, 6) * 2 + 1;
        
        int nx = 0, ny = 0;
        int cX = r.x + r.width / 2;
        int cY = r.y + r.height / 2;
        
        if (dir == 0) { // UP
            nx = cX - newW / 2;
            ny = r.y + r.height + corridorLen;
        } else if (dir == 1) { // RIGHT
            nx = r.x + r.width + corridorLen;
            ny = cY - newH / 2;
        } else if (dir == 2) { // DOWN
            nx = cX - newW / 2;
            ny = r.y - corridorLen - newH;
        } else if (dir == 3) { // LEFT
            nx = r.x - corridorLen - newW;
            ny = cY - newH / 2;
        }
        
        if (nx < 2 || ny < 2 || nx + newW > width - 2 || ny + newH > height - 2) return false;
        
        Room branch = new Room(nx, ny, newW, newH);
        boolean overlaps = false;
        for (Room existing : rooms) {
             if (rectsIntersect(branch.x - 2, branch.y - 2, branch.width + 4, branch.height + 4,
                                existing.x, existing.y, existing.width, existing.height)) {
                 overlaps = true;
                 break;
             }
        }
        
        if (!overlaps) {
            rooms.add(branch);
            carveRoom(branch);
            connectRooms(r, branch, dir, corridorLen);
            return true;
        }
        return false;
    }
    
    private boolean rectsIntersect(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }
    
    private void carveRoom(Room r) {
        for (int x = r.x; x < r.x + r.width; x++) {
            for (int y = r.y; y < r.y + r.height; y++) {
                map[x][y] = FLOOR;
            }
        }
    }
    
    private void connectRooms(Room r1, Room r2, int dir, int len) {
        int c1x = r1.x + r1.width/2;
        int c1y = r1.y + r1.height/2;
        
        // Carve straight line from edge to edge with Width 3
        if (dir == 0) { // UP from R1 to R2
             for (int y = r1.y + r1.height; y < r2.y; y++) {
                 map[c1x][y] = FLOOR;
                 map[c1x-1][y] = FLOOR;
                 map[c1x+1][y] = FLOOR;
             }
        } else if (dir == 1) { // RIGHT
             for (int x = r1.x + r1.width; x < r2.x; x++) {
                 map[x][c1y] = FLOOR;
                 map[x][c1y-1] = FLOOR;
                 map[x][c1y+1] = FLOOR;
             }
        } else if (dir == 2) { // DOWN (R2 is below R1)
             for (int y = r2.y + r2.height; y < r1.y; y++) {
                 map[c1x][y] = FLOOR;
                 map[c1x-1][y] = FLOOR;
                 map[c1x+1][y] = FLOOR;
             }
        } else if (dir == 3) { // LEFT
             for (int x = r2.x + r2.width; x < r1.x; x++) {
                 map[x][c1y] = FLOOR;
                 map[x][c1y-1] = FLOOR;
                 map[x][c1y+1] = FLOOR;
             }
        }
    }
    
    private List<GameObject> placeObjects(int difficulty, boolean isBossLevel) {
        List<GameObject> objects = new ArrayList<>();
        boolean[][] occupied = new boolean[width][height];

        // Tiles
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float wx = x * 16;
                float wy = y * 16;
                if (map[x][y] == FLOOR) {
                    objects.add(new Path(wx, wy, 16, 16, floorRegion));
                } else {
                    // VOID LOGIC: Only place wall if it is a boundary of a floor
                    boolean isPerimeter = false;
                    // Check orthogonal neighbors
                    int[][] offsets = {{-1,0}, {1,0}, {0,-1}, {0,1}};
                    for (int[] off : offsets) {
                        int nx = x + off[0];
                        int ny = y + off[1];
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                            if (map[nx][ny] == FLOOR) {
                                isPerimeter = true;
                                break;
                            }
                        }
                    }
                    
                    if (isPerimeter) {
                        objects.add(new Wall(wx, wy, 16, 16, wallRegion));
                    }
                }
            }
        }
        
        if (rooms.isEmpty()) return objects;
        
        // Entry
        Room startRoom = rooms.get(0);
        Vector2 entryPos = new Vector2(startRoom.x + startRoom.width/2, startRoom.y + startRoom.height/2);
        objects.add(new EntryPoint(entryPos.x * 16, entryPos.y * 16, 16, 16, entryRegion));
        occupied[(int)entryPos.x][(int)entryPos.y] = true;
        
        // Exit (Last Room)
        Room endRoom = rooms.get(rooms.size() - 1);
        Vector2 exitPos = new Vector2(endRoom.x + endRoom.width/2, endRoom.y + endRoom.height/2);
        
        if (!isBossLevel) {
            objects.add(new Exit(exitPos.x * 16, exitPos.y * 16, 16, 16, exitRegion));
             occupied[(int)exitPos.x][(int)exitPos.y] = true;
        } else {
            // Boss Level:
            // Boss will be spawned by DungeonController when player enters this room.
             occupied[(int)exitPos.x][(int)exitPos.y] = true; // Reserve spot
        }

        // Enemies & Traps (Skip Start and End Rooms for safety in Boss level? Maybe End room has minions?)
        // Standard: Skip Start. End room only has Boss if BossLevel.
        int enemyCountPerRoom = 2 + (difficulty / 2);
        int trapCountPerRoom = 1 + (difficulty / 3);
        
        for (int i = 1; i < rooms.size() - 1; i++) {
             Room r = rooms.get(i);
             fillRoom(r, objects, occupied, enemyCountPerRoom, trapCountPerRoom);
        }
        
        // If NOT Boss level, populate the last room with some guards?
        if (!isBossLevel) {
             fillRoom(endRoom, objects, occupied, enemyCountPerRoom + 1, trapCountPerRoom);
        }
        
        return objects;
    }
    
    private void fillRoom(Room r, List<GameObject> objects, boolean[][] occupied, int enemies, int traps) {
         for (int j = 0; j < enemies; j++) {
             Vector2 pos = getFreeRandomPoint(r, occupied);
             if (pos == null) continue;
             occupied[(int)pos.x][(int)pos.y] = true;
             
             boolean isGhost = MathUtils.randomBoolean();
             if (isGhost) {
                 objects.add(new GhostSpawnPoint(pos.x * 16, pos.y * 16, 16, 16, MapLoader.getMobAnimations(2, 1)[0].getKeyFrame(0))); 
             } else {
                 objects.add(new EnemySpawnPoint(pos.x * 16, pos.y * 16, 16, 16, MapLoader.getMobAnimations(0, 0)[0].getKeyFrame(0)));
             }
         }
         
         for (int j = 0; j < traps; j++) {
             Vector2 pos = getFreeRandomPoint(r, occupied);
             if (pos == null) continue;
             occupied[(int)pos.x][(int)pos.y] = true;
             objects.add(new Trap(pos.x * 16, pos.y * 16, 16, 16, trapRegion));
         }
    }

    public List<Room> getRooms() {
        return rooms;
    }

    private Vector2 getFreeRandomPoint(Room r, boolean[][] occupied) {
        for(int k=0; k<10; k++) {
            Vector2 p = r.getRandomPoint();
            int ix = (int)p.x;
            int iy = (int)p.y;
            if (!occupied[ix][iy]) {
                return p;
            }
        }
        return null;
    }
}
