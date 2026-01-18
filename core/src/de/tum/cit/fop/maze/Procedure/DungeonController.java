package de.tum.cit.fop.maze.Procedure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.GameObj.Boss;
import de.tum.cit.fop.maze.GameObj.Enemy;
import de.tum.cit.fop.maze.GameObj.GameObject;
import de.tum.cit.fop.maze.GameObj.Wall;
import de.tum.cit.fop.maze.GameObj.Key;
import de.tum.cit.fop.maze.GameScreen;
import de.tum.cit.fop.maze.MapLoader;

import java.util.ArrayList;
import java.util.List;

public class DungeonController {
    
    private GameScreen gameScreen;
    private List<Room> rooms;
    
    private Room bossRoom;
    private Room trapRoom;
    
    private boolean bossTriggered = false;
    private boolean bossCleared = false;
    
    private boolean trapTriggered = false;
    private boolean trapCleared = false;
    
    // Store walls created for trap room/boss room
    private List<Wall> trapWalls = new ArrayList<>();
    private List<Wall> bossWalls = new ArrayList<>();
    
    // Boss room item spawning (Endless Ver2 only)
    private float bossItemSpawnTimer = 0f;
    private static final float BOSS_ITEM_SPAWN_INTERVAL = 10f; // 10 seconds
    private boolean isEndlessVer2 = false;
    private int currentFloor = 1; // Track current floor for boss health scaling
    
    private TextureRegion wallRegion;
    private TextureRegion keyRegion;
    
    public DungeonController(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        loadResources();
    }
    
    private void loadResources() {
        Texture texture = new Texture(Gdx.files.internal("assets/selfmade/basictile.png"));
        TextureRegion[][] regions = TextureRegion.split(texture, 32, 32);
        wallRegion = regions[0][0];
        keyRegion = regions[1][1];
    }
    
    public void init(List<Room> rooms, boolean isBossLevel, boolean isEndlessVer2, int floor) {
        this.rooms = rooms;
        this.bossTriggered = false;
        this.bossCleared = false;
        this.trapTriggered = false;
        this.trapCleared = false;
        this.trapWalls.clear();
        this.bossWalls.clear();
        this.bossItemSpawnTimer = 0f;
        this.isEndlessVer2 = isEndlessVer2;
        this.currentFloor = floor;
        
        // Identify Boss Room (Last one)
        if (isBossLevel && !rooms.isEmpty()) {
            this.bossRoom = rooms.get(rooms.size() - 1);
        } else {
            this.bossRoom = null;
        }
        
        // Pick Trap Room: Must be a Branch (not on Main Path) if possible
        List<Room> candidates = new ArrayList<>();
        for (Room r : rooms) {
             // Exclude Start/Exit (Start is index 0, Exit is known as bossRoom or last)
             // But we just check r != bossRoom and r != rooms.get(0)
             if (r == rooms.get(0)) continue;
             if (r == bossRoom) continue;
             
             if (!r.isMainPath) {
                 candidates.add(r);
             }
        }
        
        // Fallback: If no branches, use any non-start/end room
        if (candidates.isEmpty() && rooms.size() > 2) {
             for (Room r : rooms) {
                 if (r != rooms.get(0) && r != bossRoom) {
                     candidates.add(r);
                 }
             }
        }

        if (!candidates.isEmpty()) {
            this.trapRoom = candidates.get(MathUtils.random(candidates.size() - 1));
        } else {
            this.trapRoom = null;
        }
    }
    
    public void update(float delta) {
        if (gameScreen.getCharacter() == null) return;
        
        Rectangle playerRect = gameScreen.getCharacter().getBounds();
        
        // Boss Room Trigger
        if (bossRoom != null && !bossTriggered) {
             // Inset 2 tiles deep to prevent getting stuck in walls
             Rectangle innerRect = new Rectangle((bossRoom.x+2) * 16, (bossRoom.y+2) * 16, (bossRoom.width-4) * 16, (bossRoom.height-4) * 16);
            if (innerRect.contains(gameScreen.getCharacter().getPosition())) {
                triggerBoss();
            }
        }
        
        // Boss Room Clear Check
        if (bossTriggered && !bossCleared) {
            checkBossClear();
        }
        
        // Trap Room Trigger
        if (trapRoom != null && !trapTriggered) {
             // Inset 2 tiles deep to ensure player is well inside
             Rectangle innerRect = new Rectangle((trapRoom.x+2) * 16, (trapRoom.y+2) * 16, (trapRoom.width-4) * 16, (trapRoom.height-4) * 16);
             if (innerRect.contains(gameScreen.getCharacter().getPosition())) {
                 triggerTrap();
             }
        }
        
        // Trap Room Logic (Check for clear)
        if (trapTriggered && !trapCleared) {
            checkTrapClear();
        }
        
        // Boss Room Item Spawning (Endless Ver2 only)
        if (isEndlessVer2 && bossTriggered && !bossCleared) {
            bossItemSpawnTimer += delta;
            if (bossItemSpawnTimer >= BOSS_ITEM_SPAWN_INTERVAL) {
                spawnBossRoomItem();
                bossItemSpawnTimer = 0f;
            }
        }
    }
    
    private void triggerBoss() {
        bossTriggered = true;
        gameScreen.spawnBoss((bossRoom.x + bossRoom.width / 2f) * 16, (bossRoom.y + bossRoom.height / 2f) * 16, currentFloor);
        gameScreen.showPopupMessage("The Boss has appeared! Escape is cut off!");
        
        // Seal Boss Room
        sealRoom(bossRoom, bossWalls);
    }
    
    private void checkBossClear() {
        if (gameScreen.getActiveBoss() == null || gameScreen.getActiveBoss().isMarkedForRemoval()) {
            // Boss Defeated
             bossCleared = true;
             gameScreen.showPopupMessage("Boss Defeated! Path Unlocked!");
             
             // Remove Walls
             gameScreen.getGameObjects().removeAll(bossWalls);
             bossWalls.clear();
        }
    }
    
    private void triggerTrap() {
        trapTriggered = true;
        gameScreen.showPopupMessage("It's a Trap! Defeat all enemies!");
        
        sealRoom(trapRoom, trapWalls);
    }
    
    // Generalized sealing logic
    private void sealRoom(Room room, List<Wall> wallList) {
        // Check Bottom (y-1) and Top (y+height) neighbors
        for (int x = room.x; x < room.x + room.width; x++) {
             // Bottom Edge
            checkNeighborAndSeal(x, room.y, x, room.y - 1, wallList); 
             // Top Edge (Corrected: y + height is the tile above outer edge line)
            checkNeighborAndSeal(x, room.y + room.height - 1, x, room.y + room.height, wallList);
        }
        // Check Left (x-1) and Right (x+width) neighbors
        for (int y = room.y + 1; y < room.y + room.height - 1; y++) {
            checkNeighborAndSeal(room.x, y, room.x - 1, y, wallList);
            checkNeighborAndSeal(room.x + room.width - 1, y, room.x + room.width, y, wallList);
        }
    }
    
    private void checkNeighborAndSeal(int rimX, int rimY, int neighborX, int neighborY, List<Wall> wallList) {
        // rimX, rimY: The tile ON the room perimeter (inside/edge of room)
        // neighborX, neighborY: The tile OUTSIDE the room (corridor candidate)
        
        // If the neighbor is Walkable, it's a corridor/path.
        if (gameScreen.isWalkable(neighborX, neighborY) && gameScreen.isWalkable(rimX, rimY)) {
            // Seal the CORRIDOR tile (neighbor) to block entry/exit
            Wall w = new Wall(neighborX * 16, neighborY * 16, 16, 16, wallRegion);
            wallList.add(w);
            gameScreen.addGameObject(w);
        }
    }
    
    private void checkTrapClear() {
        boolean enemiesAlive = false;
        Rectangle roomRect = new Rectangle(trapRoom.x * 16, trapRoom.y * 16, trapRoom.width * 16, trapRoom.height * 16);
        
        if (gameScreen.getEnemies() != null) {
            for (Enemy enemy : gameScreen.getEnemies()) {
                if (!enemy.isMarkedForRemoval()) {
                     if (Intersector.overlaps(roomRect, enemy.getBounds())) {
                        enemiesAlive = true;
                        break;
                    }
                }
            }
        }
        
        if (!enemiesAlive) {
            unlockTrap();
        }
    }
    
    private void unlockTrap() {
        trapCleared = true;
        gameScreen.showPopupMessage("Room Cleared!");
        
        // Remove walls
        gameScreen.getGameObjects().removeAll(trapWalls);
        trapWalls.clear();
        
        // Drop Key - find a free position avoiding traps
        float cx = (trapRoom.x + trapRoom.width / 2f) * 16;
        float cy = (trapRoom.y + trapRoom.height / 2f) * 16;
        
        // Try to find a position not overlapping with traps
        boolean foundFreeSpot = false;
        for (int attempt = 0; attempt < 20; attempt++) {
            float testX = (trapRoom.x + MathUtils.random(2, trapRoom.width - 3)) * 16;
            float testY = (trapRoom.y + MathUtils.random(2, trapRoom.height - 3)) * 16;
            
            // Check if this position overlaps with any trap
            boolean overlaps = false;
            for (GameObject obj : gameScreen.getGameObjects()) {
                if (obj instanceof de.tum.cit.fop.maze.GameObj.Trap) {
                    float dx = obj.getPosition().x - testX;
                    float dy = obj.getPosition().y - testY;
                    if (Math.abs(dx) < 16 && Math.abs(dy) < 16) {
                        overlaps = true;
                        break;
                    }
                }
            }
            
            if (!overlaps && gameScreen.isWalkable((int)(testX / 16), (int)(testY / 16))) {
                cx = testX;
                cy = testY;
                foundFreeSpot = true;
                break;
            }
        }
        
        // If no free spot found, use center anyway (better than nothing)
        gameScreen.addGameObject(new Key(cx - 8, cy - 8, 16, 16, keyRegion));
    }
    
    private void spawnBossRoomItem() {
        if (bossRoom == null) return;
        
        // Find a random position within the boss room
        int attempts = 0;
        while (attempts < 10) {
            int rx = bossRoom.x + MathUtils.random(2, bossRoom.width - 3);
            int ry = bossRoom.y + MathUtils.random(2, bossRoom.height - 3);
            float wx = rx * 16;
            float wy = ry * 16;
            
            // Check if position is walkable
            if (gameScreen.isWalkable(rx, ry)) {
                // 50% chance for heart, 50% for shield
                if (MathUtils.randomBoolean()) {
                    // Spawn Heart
                    gameScreen.addGameObject(new de.tum.cit.fop.maze.GameObj.Heart(wx, wy));
                } else {
                    // Spawn Shield
                    gameScreen.addGameObject(new de.tum.cit.fop.maze.GameObj.ShieldItem(wx, wy));
                }
                break;
            }
            attempts++;
        }
    }
}
