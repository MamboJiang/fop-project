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

    private List<Wall> trapWalls = new ArrayList<>();
    private List<Wall> bossWalls = new ArrayList<>();

    private float bossItemSpawnTimer = 0f;
    private static final float BOSS_ITEM_SPAWN_INTERVAL = 10f;
    private boolean isEndlessVer2 = false;
    private int currentFloor = 1;

    private TextureRegion wallRegion;
    private TextureRegion keyRegion;

    public DungeonController(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        loadResources();
    }

    private void loadResources() {
        Texture texture = new Texture(Gdx.files.internal("selfmade/basictile.png"));
        TextureRegion[][] regions = TextureRegion.split(texture, 32, 32);
        wallRegion = regions[0][0];
        keyRegion = regions[1][1];
    }

    /**
     * Initializes the dungeon controller with room data.
     * 
     * @param rooms         List of generated rooms.
     * @param isBossLevel   True if this is a boss level.
     * @param isEndlessVer2 True if using Endless Mode V2.
     * @param floor         Current floor number.
     */
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

        if (isBossLevel && !rooms.isEmpty()) {
            this.bossRoom = rooms.get(rooms.size() - 1);
        } else {
            this.bossRoom = null;
        }

        List<Room> candidates = new ArrayList<>();
        for (Room r : rooms) {

            if (r == rooms.get(0))
                continue;
            if (r == bossRoom)
                continue;

            if (!r.isMainPath) {
                candidates.add(r);
            }
        }


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

    /**
     * Updates dungeon logic (boss triggers, trap triggers, item spawning).
     * 
     * @param delta Time delta.
     */
    public void update(float delta) {
        if (gameScreen.getCharacter() == null)
            return;

        Rectangle playerRect = gameScreen.getCharacter().getBounds();


        if (bossRoom != null && !bossTriggered) {

            Rectangle innerRect = new Rectangle((bossRoom.x + 2) * 16, (bossRoom.y + 2) * 16, (bossRoom.width - 4) * 16,
                    (bossRoom.height - 4) * 16);
            if (innerRect.contains(gameScreen.getCharacter().getPosition())) {
                triggerBoss();
            }
        }


        if (bossTriggered && !bossCleared) {
            checkBossClear();
        }


        if (trapRoom != null && !trapTriggered) {

            Rectangle innerRect = new Rectangle((trapRoom.x + 2) * 16, (trapRoom.y + 2) * 16, (trapRoom.width - 4) * 16,
                    (trapRoom.height - 4) * 16);
            if (innerRect.contains(gameScreen.getCharacter().getPosition())) {
                triggerTrap();
            }
        }


        if (trapTriggered && !trapCleared) {
            checkTrapClear();
        }


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
        gameScreen.spawnBoss((bossRoom.x + bossRoom.width / 2f) * 16, (bossRoom.y + bossRoom.height / 2f) * 16,
                currentFloor);
        gameScreen.showPopupMessage("The Boss has appeared! Escape is cut off!");


        sealRoom(bossRoom, bossWalls);
    }

    private void checkBossClear() {
        if (gameScreen.getActiveBoss() == null || gameScreen.getActiveBoss().isMarkedForRemoval()) {

            bossCleared = true;
            gameScreen.showPopupMessage("Boss Defeated! Path Unlocked!");


            gameScreen.getGameObjects().removeAll(bossWalls);
            bossWalls.clear();
        }
    }

    private void triggerTrap() {
        trapTriggered = true;
        gameScreen.showPopupMessage("It's a Trap! Defeat all enemies!");

        sealRoom(trapRoom, trapWalls);
    }


    private void sealRoom(Room room, List<Wall> wallList) {
        for (int x = room.x; x < room.x + room.width; x++) {
            checkNeighborAndSeal(x, room.y, x, room.y - 1, wallList);
            checkNeighborAndSeal(x, room.y + room.height - 1, x, room.y + room.height, wallList);
        }
        for (int y = room.y + 1; y < room.y + room.height - 1; y++) {
            checkNeighborAndSeal(room.x, y, room.x - 1, y, wallList);
            checkNeighborAndSeal(room.x + room.width - 1, y, room.x + room.width, y, wallList);
        }
    }

    private void checkNeighborAndSeal(int rimX, int rimY, int neighborX, int neighborY, List<Wall> wallList) {

        if (gameScreen.isWalkable(neighborX, neighborY) && gameScreen.isWalkable(rimX, rimY)) {
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


        gameScreen.getGameObjects().removeAll(trapWalls);
        trapWalls.clear();

        float cx = (trapRoom.x + trapRoom.width / 2f) * 16;
        float cy = (trapRoom.y + trapRoom.height / 2f) * 16;

        boolean foundFreeSpot = false;
        for (int attempt = 0; attempt < 20; attempt++) {
            float testX = (trapRoom.x + MathUtils.random(2, trapRoom.width - 3)) * 16;
            float testY = (trapRoom.y + MathUtils.random(2, trapRoom.height - 3)) * 16;

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

            if (!overlaps && gameScreen.isWalkable((int) (testX / 16), (int) (testY / 16))) {
                cx = testX;
                cy = testY;
                foundFreeSpot = true;
                break;
            }
        }

        gameScreen.addGameObject(new Key(cx - 8, cy - 8, 16, 16, keyRegion));
    }

    private void spawnBossRoomItem() {
        if (bossRoom == null)
            return;
        int attempts = 0;
        while (attempts < 10) {
            int rx = bossRoom.x + MathUtils.random(2, bossRoom.width - 3);
            int ry = bossRoom.y + MathUtils.random(2, bossRoom.height - 3);
            float wx = rx * 16;
            float wy = ry * 16;

            if (gameScreen.isWalkable(rx, ry)) {
                if (MathUtils.randomBoolean()) {
                    gameScreen.addGameObject(new de.tum.cit.fop.maze.GameObj.Heart(wx, wy));
                } else {
                    gameScreen.addGameObject(new de.tum.cit.fop.maze.GameObj.ShieldItem(wx, wy));
                }
                break;
            }
            attempts++;
        }
    }
}
