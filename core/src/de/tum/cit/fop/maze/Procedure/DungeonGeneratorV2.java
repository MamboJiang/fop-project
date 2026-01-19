package de.tum.cit.fop.maze.Procedure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.GridPoint2;
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
        Texture texture = new Texture(Gdx.files.internal("selfmade/basictile.png"));
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
    /**
     * Generates a Ver2 dungeon using Grid-Slot System.
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

        // V5: 3x3 Grid with PADDING for external rooms
        // Grid uses 72x72 area (3 * 24).
        // 100x100 map. Offset 14.
        // V9: Increased for longer corridors (User Req)
        int slotSize = 24;
        int gridW = 3;
        int gridH = 3;
        int offsetX = (width - gridW * slotSize) / 2;
        int offsetY = (height - gridH * slotSize) / 2;
        
        // Track grid usage
        boolean[][] occupiedSlots = new boolean[gridW][gridH];
        
        // 1. Pick ENTRY and EXIT Nodes on the Grid (Opposite Edges of 3x3)
        // Flow Direction: Randomly Horizontal or Vertical
        boolean horizontal = MathUtils.randomBoolean();
        boolean reverse = MathUtils.randomBoolean(); // V6: Randomize direction (Up/Down or Left/Right)
        
        int entryGridX, entryGridY, exitGridX, exitGridY;
        
        if (horizontal) {
            if (!reverse) {
                // Left -> Right
                entryGridX = 0; 
                entryGridY = MathUtils.random(gridH - 1);
                
                exitGridX = gridW - 1; 
                exitGridY = MathUtils.random(gridH - 1);
            } else {
                // Right -> Left
                entryGridX = gridW - 1; 
                entryGridY = MathUtils.random(gridH - 1);
                
                exitGridX = 0; 
                exitGridY = MathUtils.random(gridH - 1);
            }
        } else {
            if (!reverse) {
                // Bottom -> Top
                entryGridX = MathUtils.random(gridW - 1); 
                entryGridY = 0;
                
                exitGridX = MathUtils.random(gridW - 1); 
                exitGridY = gridH - 1;
            } else {
                // Top -> Bottom
                entryGridX = MathUtils.random(gridW - 1); 
                entryGridY = gridH - 1;
                
                exitGridX = MathUtils.random(gridW - 1); 
                exitGridY = 0;
            }
        }
        
        // 2. Connect Grid Entry to Exit (Main Path)
        List<GridPoint2> mainPath = new ArrayList<>();
        int cx = entryGridX;
        int cy = entryGridY;
        occupiedSlots[cx][cy] = true;
        mainPath.add(new GridPoint2(cx, cy));
        
        int lastDx = 0, lastDy = 0;
        int streak = 0;

        while (cx != exitGridX || cy != exitGridY) {
            // Pathfinding Logic (Bias towards exit)
            int dx = Integer.compare(exitGridX, cx);
            int dy = Integer.compare(exitGridY, cy);
            
            // Randomly pick axis if both available
            boolean moveX = (dx != 0 && (dy == 0 || MathUtils.randomBoolean()));
            
            // V4/V5 Constraint: Straight path limit
            if (moveX) {
                 // If moving X extends a 2+ streak, force turn Y if possible
                 if (dx == lastDx && streak >= 2 && dy != 0) { moveX = false; } 
            } else {
                 // If moving Y extends a 2+ streak, force turn X if possible
                 if (dy == lastDy && streak >= 2 && dx != 0) { moveX = true; }
            }
            
            int nextX = cx + (moveX ? dx : 0);
            int nextY = cy + (!moveX ? dy : 0);
            
            // Streak update
            int actualDx = nextX - cx;
            int actualDy = nextY - cy;
            
            if (actualDx != 0) { 
                if (actualDx == lastDx) streak++; else streak = 1; 
                lastDx = actualDx; lastDy = 0; 
            } else { 
                if (actualDy == lastDy) streak++; else streak = 1; 
                lastDy = actualDy; lastDx = 0; 
            }
            
            cx = nextX;
            cy = nextY;
            
            if (!occupiedSlots[cx][cy]) {
                occupiedSlots[cx][cy] = true;
                mainPath.add(new GridPoint2(cx, cy));
            } else if (cx == exitGridX && cy == exitGridY) {
                 // Reached exit (even if occupied somehow, though shouldn't be)
                 occupiedSlots[cx][cy] = true;
                 mainPath.add(new GridPoint2(cx, cy));
            }
        }
        
        // 3. Branching
        int desiredExtraRooms = 2 + MathUtils.random(2);
        List<GridPoint2> branches = new ArrayList<>();
        List<GridPoint2> candidates = new ArrayList<>(mainPath);
        
        int attempts = 0;
        while (branches.size() < desiredExtraRooms && attempts < 50 && !candidates.isEmpty()) {
            attempts++;
            GridPoint2 base = candidates.get(MathUtils.random(candidates.size() - 1));
             
            int[][] neighbors = {{0,1}, {0,-1}, {1,0}, {-1,0}};
            int[] dir = neighbors[MathUtils.random(0, 3)];
            int nx = base.x + dir[0];
            int ny = base.y + dir[1];
            
            if (nx >= 0 && nx < gridW && ny >= 0 && ny < gridH && !occupiedSlots[nx][ny]) {
                occupiedSlots[nx][ny] = true;
                branches.add(new GridPoint2(nx, ny));
                attempts = 0;
            }
        }
        
        // 4. Create Rooms
        // A. External Start Room (Outside Grid)
        // V10: Consistent Distance. Place exactly 1 slotSize away from Entry Grid Center.
        int startDirX = 0, startDirY = 0;
        
        if (horizontal) {
            if (!reverse) { startDirX = -1; startDirY = 0; } // Left
            else { startDirX = 1; startDirY = 0; } // Right
        } else {
            if (!reverse) { startDirX = 0; startDirY = -1; } // Bottom
            else { startDirX = 0; startDirY = 1; } // Top
        }
        
        // Calculate Center of Entry Grid Slot
        int entrySlotCx = offsetX + entryGridX * slotSize + slotSize / 2;
        int entrySlotCy = offsetY + entryGridY * slotSize + slotSize / 2;
        
        // Calculate Target Center for Start Room (1 slot away)
        int startTargetCx = entrySlotCx + (startDirX * slotSize);
        int startTargetCy = entrySlotCy + (startDirY * slotSize);
        
        // Calculate Top-Left for Start Room
        // V10: Force Even Width/Height (10) for Center Alignment with Slot (24)
        int startRw = 10, startRh = 10;
        int startRx = startTargetCx - startRw / 2;
        int startRy = startTargetCy - startRh / 2;
        Room startRoom = new Room(startRx, startRy, startRw, startRh);
        startRoom.isMainPath = true;
        rooms.add(startRoom);
        carveRoom(startRoom);
        
        // B. Grid Rooms (Main Path + Branches)
        Room[][] gridRooms = new Room[gridW][gridH];
        List<GridPoint2> allSlots = new ArrayList<>(mainPath);
        allSlots.addAll(branches);
        
        for (GridPoint2 p : allSlots) {
             int slotX = offsetX + p.x * slotSize;
             int slotY = offsetY + p.y * slotSize;
             
             // V10: Force Even Sizes (10-14) for Center Alignment
             // Random 5-7 * 2 = 10, 12, 14
             int rw = MathUtils.random(5, 7) * 2;
             int rh = MathUtils.random(5, 7) * 2; 
             
             // Center in slot
             int rx = slotX + (slotSize - rw) / 2;
             int ry = slotY + (slotSize - rh) / 2;
             
             Room r = new Room(rx, ry, rw, rh);
             
             // Main Path Logic
             boolean isMain = false;
             for (GridPoint2 mp : mainPath) { if (mp.x == p.x && mp.y == p.y) isMain = true; }
             r.isMainPath = isMain;
             
             gridRooms[p.x][p.y] = r;
             rooms.add(r);
             carveRoom(r);
        }
        
        // C. External Exit Room (Outside Grid)
        // V10: Consistent Distance. Place exactly 1 slotSize away from Exit Grid Center.
        int exitDirX = 0, exitDirY = 0;
        
        if (horizontal) {
            if (!reverse) { exitDirX = 1; exitDirY = 0; } // Right
            else { exitDirX = -1; exitDirY = 0; } // Left
        } else {
            if (!reverse) { exitDirX = 0; exitDirY = 1; } // Top
            else { exitDirX = 0; exitDirY = -1; } // Bottom
        }
        
        // Calculate Center of Exit Grid Slot
        int exitSlotCx = offsetX + exitGridX * slotSize + slotSize / 2;
        int exitSlotCy = offsetY + exitGridY * slotSize + slotSize / 2;
        
        // Calculate Target Center for Exit Room (1 slot away)
        int exitTargetCx = exitSlotCx + (exitDirX * slotSize);
        int exitTargetCy = exitSlotCy + (exitDirY * slotSize);
        
        // Calculate Top-Left for Exit Room
        // V10: Fix Alignment. Boss Room 16x16 (Even).
        int exitRw = 16, exitRh = 16;
        int exitRx = exitTargetCx - exitRw / 2;
        int exitRy = exitTargetCy - exitRh / 2;
        Room exitRoom = new Room(exitRx, exitRy, exitRw, exitRh);
        exitRoom.isMainPath = true;
        rooms.add(exitRoom);
        carveRoom(exitRoom);
        
        // 5. Connections
        // A. Start -> Grid Entry
        connectGridRooms(startRoom, gridRooms[entryGridX][entryGridY]);
        
        // B. Main Path
        for (int i = 0; i < mainPath.size() - 1; i++) {
            GridPoint2 p1 = mainPath.get(i);
            GridPoint2 p2 = mainPath.get(i+1);
            connectGridRooms(gridRooms[p1.x][p1.y], gridRooms[p2.x][p2.y]);
        }
        
        // C. Grid Exit -> Exit Room
        connectGridRooms(gridRooms[exitGridX][exitGridY], exitRoom);
        
        // D. Branches
        for (GridPoint2 p : branches) {
             List<Room> neighbors = new ArrayList<>();
             int[][] offs = {{0,1}, {0,-1}, {1,0}, {-1,0}};
             for (int[] off : offs) {
                 int nx = p.x + off[0];
                 int ny = p.y + off[1];
                 if (nx >= 0 && nx < gridW && ny >= 0 && ny < gridH && gridRooms[nx][ny] != null) {
                     neighbors.add(gridRooms[nx][ny]);
                 }
             }
             if (!neighbors.isEmpty()) {
                 connectGridRooms(gridRooms[p.x][p.y], neighbors.get(MathUtils.random(neighbors.size() - 1)));
             }
        }
        
        // E. Loops (70% prob)
        for (GridPoint2 p : allSlots) {
             int[][] offs = {{0,1}, {1,0}};
             for (int[] off : offs) {
                 int nx = p.x + off[0];
                 int ny = p.y + off[1];
                 if (nx >= 0 && nx < gridW && ny >= 0 && ny < gridH && gridRooms[nx][ny] != null) {
                     if (MathUtils.randomBoolean(0.7f)) {
                         connectGridRooms(gridRooms[p.x][p.y], gridRooms[nx][ny]);
                     }
                 }
             }
        }
        
        return placeObjects(difficulty, isBossLevel);
    }
    
    private void carveRoom(Room r) {
        for (int x = r.x; x < r.x + r.width; x++) {
            for (int y = r.y; y < r.y + r.height; y++) {
                map[x][y] = FLOOR;
            }
        }
    }

    private void connectGridRooms(Room r1, Room r2) {
         // Determine direction
         int cx1 = r1.x + r1.width/2;
         int cy1 = r1.y + r1.height/2;
         int cx2 = r2.x + r2.width/2;
         int cy2 = r2.y + r2.height/2;
         
         if (cx1 == cx2) { // Vertical
             int minY = Math.min(r1.y + r1.height, r2.y + r2.height);
             int maxY = Math.max(r1.y, r2.y); 
             // Logic: Carve from Bottom of TopRoom to Top of BottomRoom
             // r1 Y is bottom? No, Y increases UP.
             Room bottom = (r1.y < r2.y) ? r1 : r2;
             Room top = (r1.y < r2.y) ? r2 : r1;
             
             for (int y = bottom.y + bottom.height; y < top.y; y++) {
                  map[cx1][y] = FLOOR;
                  map[cx1-1][y] = FLOOR;
                  map[cx1+1][y] = FLOOR;
             }
             // Also ensure connection into the room (in case padding was huge) -> Actually carveRoom handles inside.
             // But my logic for y above assumes spacing.
         } else { // Horizontal
             Room left = (r1.x < r2.x) ? r1 : r2;
             Room right = (r1.x < r2.x) ? r2 : r1;
             
             for (int x = left.x + left.width; x < right.x; x++) {
                  map[x][cy1] = FLOOR;
                  map[x][cy1-1] = FLOOR;
                  map[x][cy1+1] = FLOOR;
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
