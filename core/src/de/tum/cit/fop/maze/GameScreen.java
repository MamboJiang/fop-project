package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.GameControl.*;
import de.tum.cit.fop.maze.GameObj.*;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.tum.cit.fop.maze.GameObj.Character;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.List;
import java.util.ArrayList;

/**
 * The GameScreen class is responsible for the main gameplay loop.
 * It handles rendering, updates, input, and game logic like level generation
 * and game over states.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private HUD hud;

    private boolean debugEnabled = false;
    private boolean debugMapMode = false;

    private boolean isPaused = false;
    private boolean isGameOver = false;
    private Stage pauseStage;
    private PauseMenu pauseMenu;
    private GameOverMenu GameOverMenu;

    private de.tum.cit.fop.maze.GameObj.Character character;
    private List<GameObject> mapObjects;
    private List<de.tum.cit.fop.maze.GameObj.Enemy> enemies;
    private FileHandle mapFile;
    private de.tum.cit.fop.maze.AI.Grid grid;
    private List<de.tum.cit.fop.maze.VFX.DamageNumber> damageNumbers;
    private boolean isProcedural = false;
    private int currentDifficulty = 1;
    private float levelTimer = 0f;
    private de.tum.cit.fop.maze.GameObj.Nono nono;
    private boolean pendingNonoUnlock = false;
    private int score = 0;
    private static final int BASE_SCORE_PER_LEVEL = 1000;
    private static final int PENALTY_PER_SECOND = 10;
    private static final int SCORE_PER_LIFE = 500;

    private String playerName = "Player";
    private int totalRunScore = 0;

    private String currentLevelName = "Unknown";
    private boolean levelStartDialoguePlayed = false;
    private float levelStartTimer = 0f;
    private boolean levelAfterDialoguePlayed = false;
    private List<Projectile> projectiles;
    private TextureRegion bulletTex;
    private Boss activeBoss;
    private float bossItemSpawnTimer = 0f;
    private static final float BOSS_ITEM_SPAWN_INTERVAL = 15f; // 15 seconds
    private float bossDeathTimer = 0f;

    // Boss Room Locking
    private boolean isBossRoomLocked = false;
    private Texture wallTextureForLock;
    private List<de.tum.cit.fop.maze.GameObj.Wall> lockedWalls = new ArrayList<>();

    /**
     * Constructor for loading a specific map file.
     * 
     * @param game    The main game class.
     * @param mapFile The map file to load.
     */
    public GameScreen(MazeRunnerGame game, FileHandle mapFile) {
        this.game = game;
        this.mapFile = mapFile;
        this.isProcedural = false;
        if (mapFile != null) {
            this.currentLevelName = mapFile.nameWithoutExtension();
        }

        initCommon();
        setupLevel();
    }

    /**
     * Constructor for Procedural / Endless modes.
     * 
     * @param game         The main game class.
     * @param isProcedural Whether the level should be generated procedurally.
     * @param playerName   The name of the player.
     */
    public GameScreen(MazeRunnerGame game, boolean isProcedural, String playerName) {
        this.game = game;
        this.isProcedural = isProcedural;
        this.currentDifficulty = 1;

        this.playerName = playerName;
        this.totalRunScore = 0;

        initCommon();
        setupLevel();
    }

    /**
     * Sets the difficulty for procedural generation.
     * 
     * @param difficulty The difficulty level.
     */
    private boolean isEndlessVer2 = false;
    private de.tum.cit.fop.maze.Procedure.DungeonController dungeonController;

    public void addGameObject(GameObject obj) {
        if (mapObjects != null) {
            mapObjects.add(obj);
        }
    }

    public List<GameObject> getGameObjects() {
        return mapObjects;
    }

    public void showPopupMessage(String message) {
        // Simple log for now, or use HUD if available
        Gdx.app.log("GameScreen", "Popup: " + message);
        // If HUD has a notification method, call it here
        // if (hud != null) hud.showNotification(message);
    }

    public void spawnBoss(float x, float y, int floor) {
        if (bulletTex == null) {
            // Load if not loaded? Or assume loaded common
            // bulletTex is field?
        }
        de.tum.cit.fop.maze.GameObj.Boss boss = new de.tum.cit.fop.maze.GameObj.Boss(
                x, y,
                de.tum.cit.fop.maze.MapLoader.getBossAnimations(),
                grid,
                character,
                projectiles,
                bulletTex // Assuming this field is accessible
        );

        // Scale boss health for Endless Ver2: 200 + (floor - 1) * 200
        if (isEndlessVer2) {
            int bossHealth = 200 + (floor - 1) * 200;
            boss.setHealth(bossHealth);
            boss.setMaxHealth(bossHealth);
        }

        this.activeBoss = boss;
        this.bossItemSpawnTimer = 0f;
        addGameObject(boss);
        enemies.add(boss); // Keep track as enemy too
    }

    public de.tum.cit.fop.maze.GameObj.Character getCharacter() {
        return character;
    }

    public List<de.tum.cit.fop.maze.GameObj.Enemy> getEnemies() {
        return enemies;
    }

    public boolean isWalkable(int x, int y) {
        if (grid != null) {
            return grid.isWalkable(x, y);
        }
        return false;
    }

    /**
     * Sets the difficulty for procedural generation.
     * 
     * @param difficulty The difficulty level.
     */
    public void setDifficulty(int difficulty) {
        this.currentDifficulty = difficulty;

        if (isEndlessVer2) {
            generateProceduralLevelV2();
        } else {
            generateProceduralLevel();
        }
    }

    public void setEndlessVer2(boolean isEndlessVer2) {
        this.isEndlessVer2 = isEndlessVer2;
    }

    public String getCurrentLevelName() {
        return currentLevelName;
    }

    /**
     * Common initialization for camera, viewport, HUD, and other systems.
     */
    // Dialogue System
    private de.tum.cit.fop.maze.Conversation.DialogueManager dialogueManager;

    /**
     * Common initialization for camera, viewport, HUD, and other systems.
     */
    // Level 3 Dialogue Triggers
    private int lastHealth = -1;
    private boolean l3DamageTriggered = false;
    private boolean l3HealTriggered = false;
    private float damageDialogueTimer = -1; // Timer for delayed dialogue
    private boolean wasDialogueActive = false; // Track dialogue state change

    // ShapeRenderer already imported

    // ...

    // Fields
    // Level 5 Logic
    private boolean level5StartDialoguePlayed = false;
    private boolean level5BossDialoguePlayed = false;
    private boolean level5AfterDialoguePlayed = false;
    private int level5CameraState = 0; // 0: None, 1: Pan to Exit, 2: Wait, 3: Pan Back, 4: Done
    private float level5CameraTimer = 0f;
    private com.badlogic.gdx.math.Vector2 level5BossSpawnPos;

    // Level 4 Logic
    // Level 4 Logic
    private float level4StartTimer = 0;
    private boolean level4PreTriggered = false;
    private int level4IntroState = 0;
    private float level4IntroTimer = 0f;
    private int level4FlashCount = 0;
    private TextureRegion blackTex; // Still need this for initial blackout or use FBO clear color

    // Flashlight
    private de.tum.cit.fop.maze.VFX.FlashlightEffect flashlightEffect;

    private void initCommon() {
        flashlightEffect = new de.tum.cit.fop.maze.VFX.FlashlightEffect();

        camera = new OrthographicCamera();
        camera.zoom = 0.7f;

        viewport = new ExtendViewport(640, 360, camera);

        font = game.getSkin().getFont("font");

        shapeRenderer = new ShapeRenderer();
        hud = new HUD(game.getSpriteBatch(), this, game.getSkin());
        screenShake = new de.tum.cit.fop.maze.VFX.ScreenShake();

        setupPauseMenu();

        // Initialize Dialogue Manager
        dialogueManager = new de.tum.cit.fop.maze.Conversation.DialogueManager(game.getSkin());
        // Load dialogue for the current level (e.g. "level-0")
        if (currentLevelName != null) {
            dialogueManager.loadDialogue(currentLevelName);
        }
    }

    public void updateInputProcessor() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        if (isPaused) {
            multiplexer.addProcessor(pauseStage);
        } else if (dialogueManager.isActive()) {
            multiplexer.addProcessor(dialogueManager.getStage());
        } else {
            multiplexer.addProcessor(hud.getStage());
        }
        Gdx.input.setInputProcessor(multiplexer);
    }

    /**
     * Sets up the level, either loading from file or generating procedurally.
     */
    private void setupLevel() {
        if (isProcedural) {
            generateProceduralLevel();
            return;
        }

        if (this.mapFile == null || !this.mapFile.exists()) {
            Gdx.app.error("GameScreen", "Map file is null or does not exist!");
            // CHANGED: Default to level-0 for testing
            this.mapFile = Gdx.files.internal("maps/level-0.properties");
        }

        mapObjects = MapLoader.loadMap(this.mapFile);
        System.out.println("number of objects:" + mapObjects.size());
        initMapObjects();
    }

    /**
     * Generates a procedurally generated level using DungeonGenerator.
     */
    private void generateProceduralLevel() {

        int size = 40 + (currentDifficulty * 2);
        if (size > 100)
            size = 100;

        de.tum.cit.fop.maze.Procedure.DungeonGenerator generator = new de.tum.cit.fop.maze.Procedure.DungeonGenerator(
                size, size);
        mapObjects = generator.generate(currentDifficulty);

        initMapObjects();
    }

    /**
     * Generates a Ver2 procedural level.
     */
    private void generateProceduralLevelV2() {
        int floor = (currentDifficulty - 1) / 3 + 1;
        int stage = (currentDifficulty - 1) % 3 + 1;

        // Update Level Name for UI
        this.currentLevelName = "Floor " + floor + "-" + stage;

        boolean isBossLevel = (stage == 3);

        de.tum.cit.fop.maze.Procedure.DungeonGeneratorV2 gen = new de.tum.cit.fop.maze.Procedure.DungeonGeneratorV2(130,
                130);
        mapObjects = gen.generate(floor, isBossLevel);

        // Initialize Controller
        dungeonController = new de.tum.cit.fop.maze.Procedure.DungeonController(this);
        dungeonController.init(gen.getRooms(), isBossLevel, isEndlessVer2, floor);

        initMapObjects();
    }

    /**
     * Initializes game objects (Player, Enemies, Items) from the map data.
     */
    private void initMapObjects() {
        projectiles = new ArrayList<>();

        // Load custom bullet texture
        Texture bulletTexture = new Texture(com.badlogic.gdx.Gdx.files.internal("selfmade/bullet.png"));
        bulletTex = new TextureRegion(bulletTexture);

        // Create black texture for other purposes
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(16, 16,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.BLACK);
        pixmap.fill();
        Texture tempBlackTexture = new Texture(pixmap);
        blackTex = new TextureRegion(tempBlackTexture); // Add field blackTex to class

        pixmap.dispose();
        grid = new de.tum.cit.fop.maze.AI.Grid(0, 0, mapObjects);

        float spawnX = 0;
        float spawnY = 0;
        for (GameObject obj : mapObjects) {
            if (obj instanceof EntryPoint) {
                spawnX = obj.getPosition().x;
                spawnY = obj.getPosition().y;
                break;
            }
        }

        if (character == null) {

            character = new Character(spawnX + 16, spawnY, game.getPlayerState(), game);
        } else {
            character.setPosition(spawnX + 16, spawnY);
        }

        // Reset Level 3 Dialogue State
        lastHealth = (int) character.getCurrentHealth();
        l3DamageTriggered = false;
        l3HealTriggered = false;
        damageDialogueTimer = -1;

        // Level 4
        level4StartTimer = 0;
        level4PreTriggered = false;
        level4IntroState = 0;
        level4IntroTimer = 0f;
        level4FlashCount = 0;
        if (flashlightEffect != null) {
            flashlightEffect.reset();
            flashlightEffect.setEnabled(false); // Ensure initially disabled
        }

        if (flashlightEffect != null) {
            flashlightEffect.reset();
            flashlightEffect.setEnabled(false); // Ensure initially disabled
        }

        // Reset Level 5 Dialogue State
        level5StartDialoguePlayed = false;
        level5BossDialoguePlayed = false;
        level5AfterDialoguePlayed = false;
        level5CameraState = 0;
        level5CameraTimer = 0f;

        // Snap camera to player immediately to avoid "flying in"
        camera.position.set(character.getPosition().x, character.getPosition().y, 0);
        camera.update();

        // Load mask appearance for all levels except Level 0
        if (!"level-0".equals(currentLevelName)) {
            character.loadMaskAppearance();
        }

        // Reset abilities for specific levels
        if ("level-0".equals(currentLevelName)) {
            game.getPlayerState().setNonoUnlocked(false);
        }
        if ("level-2".equals(currentLevelName)) {
            character.setAttackUnlocked(false);
        }

        enemies = new java.util.ArrayList<>();

        // Spawn Suicide Monster in Level 3 near spawn
        if ("level-3".equals(currentLevelName)) {
            de.tum.cit.fop.maze.GameObj.Enemy suicideEnemy = new de.tum.cit.fop.maze.GameObj.Enemy(
                    spawnX + 48,
                    spawnY + 32,
                    de.tum.cit.fop.maze.MapLoader.getRobotAnimations(),
                    grid,
                    character);
            suicideEnemy.setSuicide(true);
            enemies.add(suicideEnemy);
        }

        if (isProcedural) {
            de.tum.cit.fop.maze.GameObj.PlayerState state = game.getPlayerState();
            if (state.getCurrentRunScore() > 0) {
                this.totalRunScore = state.getCurrentRunScore();
            }
            if (state.getCurrentRunHealth() > 0) {
                character.setCurrentHealth(state.getCurrentRunHealth());
            }
        }

        List<GameObject> toRemove = new java.util.ArrayList<>();
        for (GameObject obj : mapObjects) {
            if (obj instanceof de.tum.cit.fop.maze.GameObj.EnemySpawnPoint) {
                enemies.add(new de.tum.cit.fop.maze.GameObj.Enemy(
                        obj.getPosition().x,
                        obj.getPosition().y,
                        de.tum.cit.fop.maze.MapLoader.getRobotAnimations(), // Updated to Robot sprite
                        grid,
                        character));
                toRemove.add(obj);
            } else if (obj instanceof de.tum.cit.fop.maze.GameObj.GhostSpawnPoint) {
                enemies.add(new de.tum.cit.fop.maze.GameObj.Ghost(
                        obj.getPosition().x,
                        obj.getPosition().y,
                        de.tum.cit.fop.maze.MapLoader.getDroneAnimations(), // Updated to Drone sprite
                        grid,
                        character));
                toRemove.add(obj);
            } else if (obj instanceof de.tum.cit.fop.maze.GameObj.BossSpawnPoint) {
                if ("level-5".equals(currentLevelName)) {
                    // Defer spawning for Level 5
                    level5BossSpawnPos = new com.badlogic.gdx.math.Vector2(obj.getPosition().x, obj.getPosition().y);
                    toRemove.add(obj);
                } else {
                    Boss boss = new de.tum.cit.fop.maze.GameObj.Boss(
                            obj.getPosition().x,
                            obj.getPosition().y,
                            de.tum.cit.fop.maze.MapLoader.getBossAnimations(),
                            grid,
                            character,
                            projectiles,
                            bulletTex);
                    this.activeBoss = boss;
                    this.bossItemSpawnTimer = 0f; // Reset item spawn timer for Boss fight
                    enemies.add(boss);
                    toRemove.add(obj);
                }
            }
        }

        mapObjects.removeAll(toRemove);

        // Special Logic: In Level 2, replace the Key with AttackUnlockItem (The Knife)
        if ("level-2".equals(currentLevelName)) {
            List<GameObject> toAdd = new java.util.ArrayList<>();
            java.util.Iterator<GameObject> iter = mapObjects.iterator();
            while (iter.hasNext()) {
                GameObject obj = iter.next();
                if (obj instanceof de.tum.cit.fop.maze.GameObj.Key) {
                    iter.remove();
                    // Create AttackUnlockItem at same position with same texture (or different if
                    // we had one)
                    // Using the Key's texture region
                    toAdd.add(new de.tum.cit.fop.maze.GameObj.AttackUnlockItem(
                            obj.getPosition().x,
                            obj.getPosition().y,
                            obj.getWidth(),
                            obj.getHeight(),
                            obj.getTextureRegion()));
                }
            }
            mapObjects.addAll(toAdd);
        }

        // Special Logic: In Level 3, replace the Key with a Shield Item visual and
        // spawn a ShieldItem there too
        if ("level-3".equals(currentLevelName)) {
            List<GameObject> toAdd = new java.util.ArrayList<>();
            java.util.Iterator<GameObject> iter = mapObjects.iterator();
            while (iter.hasNext()) {
                GameObject obj = iter.next();
                if (obj instanceof de.tum.cit.fop.maze.GameObj.Key) {
                    iter.remove();
                    // Create a Key that looks like a Shield
                    // Use shield texture
                    Texture shieldTex = new Texture(Gdx.files.internal("selfmade/shielditem.png"));
                    TextureRegion shieldReg = new TextureRegion(shieldTex);

                    // Add the "Key" which is visual goal
                    de.tum.cit.fop.maze.GameObj.Key shieldKey = new de.tum.cit.fop.maze.GameObj.Key(
                            obj.getPosition().x,
                            obj.getPosition().y,
                            16,
                            16,
                            shieldReg);
                    toAdd.add(shieldKey);

                    // Add actual Shield Item for effect
                    de.tum.cit.fop.maze.GameObj.ShieldItem realShield = new de.tum.cit.fop.maze.GameObj.ShieldItem(
                            obj.getPosition().x,
                            obj.getPosition().y);
                    toAdd.add(realShield);
                }
            }
            mapObjects.addAll(toAdd);

            // Also spawn a Heart (Mask) near spawn point (right and up a bit)
            // Find entry point again or use previously found spawnX, spawnY
            float entryX = 0, entryY = 0;
            for (GameObject obj : mapObjects) {
                if (obj instanceof EntryPoint) {
                    entryX = obj.getPosition().x;
                    entryY = obj.getPosition().y;
                    break;
                }
            }
            // Spawn Heart at spawnX + 32, spawnY + 48 (example "right up")
            de.tum.cit.fop.maze.GameObj.Heart startMask = new de.tum.cit.fop.maze.GameObj.Heart(
                    entryX + 128,
                    entryY + 32);
            mapObjects.add(startMask);
        }

        // Special Logic: In Level 4, replace the Key texture with Serum (no shield
        // effect)
        if ("level-4".equals(currentLevelName)) {
            List<GameObject> toAdd = new java.util.ArrayList<>();
            java.util.Iterator<GameObject> iter = mapObjects.iterator();
            while (iter.hasNext()) {
                GameObject obj = iter.next();
                if (obj instanceof de.tum.cit.fop.maze.GameObj.Key) {
                    iter.remove();
                    // Create a Key that looks like Serum
                    Texture serumTex = new Texture(Gdx.files.internal("selfmade/serum.png"));
                    TextureRegion serumReg = new TextureRegion(serumTex);

                    de.tum.cit.fop.maze.GameObj.Key serumKey = new de.tum.cit.fop.maze.GameObj.Key(
                            obj.getPosition().x,
                            obj.getPosition().y,
                            16,
                            16,
                            serumReg);
                    toAdd.add(serumKey);
                }
            }
            mapObjects.addAll(toAdd);
        }

        // Nono Trigger Logic in Level 0
        if ("level-0".equals(currentLevelName)) {
            // Find existing trigger to remove (we spawn Nono at center regardless)
            GameObject triggerToRemove = null;

            for (GameObject obj : mapObjects) {
                if (obj instanceof de.tum.cit.fop.maze.GameObj.DialogueTrigger) {
                    triggerToRemove = obj;
                    break;
                }
            }

            if (triggerToRemove != null) {
                mapObjects.remove(triggerToRemove);
            }

            // Always spawn Nono at Center (15*16)/2 - 10 = 110
            Texture nonoTex = new Texture(Gdx.files.internal("player/sprite/nono.png"));
            TextureRegion[][] tmp = TextureRegion.split(nonoTex, 32, 32);
            TextureRegion nonoFrame = tmp[0][0];

            de.tum.cit.fop.maze.GameObj.NonoNPC npc = new de.tum.cit.fop.maze.GameObj.NonoNPC(110, 110, nonoFrame);
            mapObjects.add(npc);
        }

        // Spawn Nono if unlocked
        if (game.getPlayerState().isNonoUnlocked()) {
            nono = new de.tum.cit.fop.maze.GameObj.Nono(character.getPosition().x, character.getPosition().y,
                    character);
        } else {
            nono = null;
        }

        java.util.Map<String, java.util.List<GameObject>> chunks = new java.util.HashMap<>();
        int chunkSize = 16 * 16;

        for (GameObject obj : mapObjects) {
            if (obj instanceof de.tum.cit.fop.maze.GameObj.Path) {
                int cx = (int) (obj.getPosition().x / chunkSize);
                int cy = (int) (obj.getPosition().y / chunkSize);
                String key = cx + "," + cy;

                if (!chunks.containsKey(key)) {
                    chunks.put(key, new java.util.ArrayList<>());
                }
                chunks.get(key).add(obj);
            }
        }

        // Only spawn hearts and shields in levels 3+, but NO random shields in Level 3
        if (!"level-0".equals(currentLevelName) && !"level-1".equals(currentLevelName)
                && !"level-2".equals(currentLevelName)) {
            for (java.util.List<GameObject> chunkPaths : chunks.values()) {
                if (com.badlogic.gdx.math.MathUtils.randomBoolean(0.5f)) {
                    GameObject randomPath = chunkPaths
                            .get(com.badlogic.gdx.math.MathUtils.random(chunkPaths.size() - 1));
                    de.tum.cit.fop.maze.GameObj.Heart heart = new de.tum.cit.fop.maze.GameObj.Heart(
                            randomPath.getPosition().x, randomPath.getPosition().y);
                    mapObjects.add(heart);
                }

                // Random Shields: EXCLUDE Level 3
                if (!"level-3".equals(currentLevelName) && com.badlogic.gdx.math.MathUtils.randomBoolean(0.2f)) {
                    GameObject randomPath = chunkPaths
                            .get(com.badlogic.gdx.math.MathUtils.random(chunkPaths.size() - 1));

                    de.tum.cit.fop.maze.GameObj.ShieldItem shield = new de.tum.cit.fop.maze.GameObj.ShieldItem(
                            randomPath.getPosition().x, randomPath.getPosition().y);
                    mapObjects.add(shield);
                }
            }
        }

        damageNumbers = new java.util.ArrayList<>();
    }

    /**
     * Sets up the pause menu UI.
     */
    private void setupPauseMenu() {
        pauseStage = new Stage(new ExtendViewport(1920, 1080), game.getSpriteBatch());

        pauseMenu = new PauseMenu(game,
                () -> togglePause(),
                () -> {
                    // Commented out save progress dialog - user request
                    /*
                     * if (isProcedural) {
                     * Dialog dialog = new Dialog("Endless Mode", game.getSkin()) {
                     * 
                     * @Override
                     * protected void result(Object object) {
                     * if (object instanceof Integer) {
                     * int choice = (Integer) object;
                     * if (choice == 1) {
                     * 
                     * game.getPlayerState().setEndlessWave(currentDifficulty);
                     * 
                     * game.getPlayerState().setCurrentRunScore(totalRunScore);
                     * if (character != null) {
                     * game.getPlayerState().setCurrentRunHealth(character.getCurrentHealth());
                     * }
                     * 
                     * game.saveGame();
                     * game.goToMenu();
                     * } else if (choice == 2) {
                     * 
                     * game.getPlayerState().resetEndlessWave();
                     * game.getPlayerState().resetRunState();
                     * pauseMenu.hide();
                     * isPaused = false;
                     * showGameOverMenu(false);
                     * } else {
                     * 
                     * }
                     * }
                     * }
                     * };
                     * dialog.text(
                     * "Save Difficulty or End Run (Submit Score)?\nNew Run will be: Lv " +
                     * currentDifficulty);
                     * dialog.button(new
                     * com.badlogic.gdx.scenes.scene2d.ui.TextButton("Save & Quit", game.getSkin(),
                     * "short"), 1);
                     * dialog.button(new
                     * com.badlogic.gdx.scenes.scene2d.ui.TextButton("End Game (Submit Score)",
                     * game.getSkin(), "short"), 2);
                     * dialog.button(new com.badlogic.gdx.scenes.scene2d.ui.TextButton("Cancel",
                     * game.getSkin(), "short"), 0);
                     * dialog.show(pauseStage);
                     * } else {
                     * game.goToMenu();
                     * }
                     */
                    // Trigger settlement screen for endless mode, otherwise go to menu
                    if (isProcedural) {
                        game.getPlayerState().resetEndlessWave();
                        game.getPlayerState().resetRunState();
                        pauseMenu.hide();
                        isPaused = false;
                        showGameOverMenu(false);
                    } else {
                        game.goToMenu();
                    }
                });
        pauseStage.addActor(pauseMenu);
    }

    /**
     * Loads the next level. If procedural, generates a new harder level.
     * If story/classic, loads the next map file.
     */
    private void loadNextLevel() {
        if (isProcedural) {
            totalRunScore += calculateScore();

            currentDifficulty++;
            game.getPlayerState().setEndlessWave(currentDifficulty);
            // Update Max Floor
            int floor = (currentDifficulty - 1) / 3 + 1;
            game.getPlayerState().setMaxEndlessFloor(floor);

            de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance()
                    .onStatusUpdate(de.tum.cit.fop.maze.GameControl.EventType.REACH_DIFFICULTY, currentDifficulty);

            isGameOver = false;
            isPaused = false;
            levelTimer = 0f;
            if (character != null) {
                character.resetForNewLevel();
            }

            if (GameOverMenu != null) {
                GameOverMenu.remove();
            }
            if (pauseMenu != null) {
                pauseMenu.hide();
            }

            updateInputProcessor();

            if (isEndlessVer2) {
                generateProceduralLevelV2();
            } else {
                generateProceduralLevel();
            }
            return;
        }

        List<FileHandle> maps = MapLoader.getMapFiles();
        // Sort maps to ensure consistent order (Windows vs Mac) and match
        // LevelSelectionScreen
        java.util.Collections.sort(maps, new java.util.Comparator<FileHandle>() {
            @Override
            public int compare(FileHandle o1, FileHandle o2) {
                return o1.name().compareTo(o2.name());
            }
        });

        int currentIndex = -1;

        for (int i = 0; i < maps.size(); i++) {

            if (maps.get(i).name().equals(this.mapFile.name())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex != -1 && currentIndex + 1 < maps.size()) {

            FileHandle nextMap = maps.get(currentIndex + 1);
            game.goToGame(nextMap);
        } else {

            game.goToMenu();
        }
    }

    /**
     * Awards XP to the player.
     * 
     * @param win Whether the level was won.
     * @return The amount of XP awarded.
     */
    private int awardXP(boolean win) {
        if (win) {

            if (!isProcedural && currentLevelName != null) {
                game.getPlayerState().addCompletedLevel(currentLevelName);
            }

            int xpEarned = 100;

            if (isProcedural) {
                game.getPlayerState().addCurrentRunXP(xpEarned);
                System.out.println("Run XP: " + game.getPlayerState().getCurrentRunXP());
            } else {
                game.getPlayerState().addXP(xpEarned);
                System.out.println("Awarded " + xpEarned + " XP");
            }

            game.saveGame();

            return xpEarned;
        }
        return 0;
    }

    /**
     * Shows the Game Over menu with appropriate options (Win/Loss).
     * 
     * @param win True if the player won the level/run.
     */
    private void showGameOverMenu(boolean win) {
        if (isGameOver)
            return;
        isGameOver = true;

        int awardXP = awardXP(win);

        if (isProcedural && !win) {

            int runXP = game.getPlayerState().getCurrentRunXP();
            game.getPlayerState().addXP(runXP);
            awardXP = runXP;

            game.getPlayerState().resetEndlessWave();
            game.getPlayerState().resetRunState();
            game.saveGame();
        } else if (!isProcedural && win) {
            // Mark level as completed and save
            game.getPlayerState().addCompletedLevel(currentLevelName);
            game.saveGame();
        }

        int currentLevelScore = win ? calculateScore() : 0;
        int finalDisplayScore = isProcedural ? (totalRunScore + currentLevelScore) : currentLevelScore;

        int waves = -1;
        if (isProcedural) {
            waves = win ? currentDifficulty : currentDifficulty - 1;
        }

        GameOverMenu = new GameOverMenu(game,
                () -> {

                    if (isProcedural) {
                        if (isEndlessVer2 && activeBoss == null) {
                            game.goToEndlessModeVer2(playerName);
                        } else {
                            game.goToEndlessMode(playerName);
                        }
                    } else {
                        game.goToGame(this.mapFile);
                    }
                },
                () -> {

                    if (isProcedural && win) {
                        totalRunScore += calculateScore();
                        game.getPlayerState().resetEndlessWave();
                        game.getPlayerState().resetRunState();

                        if (GameOverMenu != null)
                            GameOverMenu.remove();
                        isGameOver = false;
                        showGameOverMenu(false);
                    } else {
                        game.goToMenu();
                    }
                },
                () -> {
                    loadNextLevel();
                },
                win,
                waves,
                finalDisplayScore,
                awardXP);

        if (isProcedural) {

            if (!win) {
                de.tum.cit.fop.maze.GameControl.LeaderboardManager.saveScore(playerName, finalDisplayScore, () -> {
                    if (GameOverMenu != null)
                        GameOverMenu.loadLeaderboard();
                });
                de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance()
                        .onEvent(de.tum.cit.fop.maze.GameControl.EventType.GAME_OVER, 1);
            }
        } else {
            if (!win) {
                de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance()
                        .onEvent(de.tum.cit.fop.maze.GameControl.EventType.GAME_OVER, 1);
            }
        }

        pauseStage.addActor(GameOverMenu);
        GameOverMenu.show();
        game.playGameOverSound();

        Gdx.input.setInputProcessor(pauseStage);
    }

    /**
     * Toggles the pause state of the game.
     */
    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            if (isProcedural) {
                pauseMenu.setStatsVisible(true);
                int xpToShow = game.getPlayerState().getCurrentRunXP();
                pauseMenu.updateStats(totalRunScore, currentDifficulty, xpToShow);
            } else {
                pauseMenu.setStatsVisible(false);
            }
            pauseMenu.show();
        } else {
            pauseMenu.hide();
        }
        updateInputProcessor();
    }

    /**
     * Toggles debug rendering mode.
     */
    public void toggleDebug() {
        debugEnabled = !debugEnabled;
    }

    public void toggleMapDebug() {
        debugMapMode = !debugMapMode;
        if (debugMapMode) {
            camera.zoom = 6.0f;
            // Center on 100x100 map (100 * 16 / 2 = 800)
            camera.position.set(800, 800, 0);
            camera.update();
        } else {
            camera.zoom = 1.0f;
            // Position update happens in render
        }
    }

    /**
     * Zooms the camera in.
     */
    public void zoomIn() {
        camera.zoom = Math.max(0.1f, camera.zoom - 0.1f);
        camera.update();
    }

    /**
     * Zooms the camera out.
     */
    public void zoomOut() {
        camera.zoom = Math.min(2.0f, camera.zoom + 0.1f);
        camera.update();
    }

    private de.tum.cit.fop.maze.VFX.ScreenShake screenShake;

    public OrthographicCamera getCamera() {
        return camera;
    }

    /**
     * Main render loop.
     * 
     * @param delta Time since last frame in seconds.
     */
    @Override
    public void render(float delta) {
        if ("level-0".equals(currentLevelName) && !levelStartDialoguePlayed) {
            levelStartTimer += delta;
            if (levelStartTimer >= 0.3f) {
                levelStartDialoguePlayed = true;
                EncyclopediaManager.getInstance().unlock(game, "main_character");
                dialogueManager.loadDialogue("level-0-pre");
                dialogueManager.startDialogue();
                updateInputProcessor();
            }
        }

        // Level 0 After Dialogue (Mask Pickup)
        if ("level-0".equals(currentLevelName) && !levelAfterDialoguePlayed && character != null
                && character.hasKey()) {
            levelAfterDialoguePlayed = true;
            EncyclopediaManager.getInstance().unlock(game, "npc_nono");
            dialogueManager.loadDialogue("level-0-after");
            dialogueManager.startDialogue();
            updateInputProcessor();
        }

        // Level 1 Start Dialogue
        if ("level-1".equals(currentLevelName) && !levelStartDialoguePlayed) {
            levelStartTimer += delta;
            if (levelStartTimer >= 0.3f) {
                levelStartDialoguePlayed = true;
                dialogueManager.loadDialogue("level-1-pre");
                dialogueManager.startDialogue();
                updateInputProcessor();
            }
        }

        // Level 1 After Dialogue (Key Pickup)
        if ("level-1".equals(currentLevelName) && !levelAfterDialoguePlayed && character != null
                && character.hasKey()) {
            levelAfterDialoguePlayed = true;
            EncyclopediaManager.getInstance().unlock(game, "enemy_trap");
            EncyclopediaManager.getInstance().unlock(game, "enemy_robot");
            dialogueManager.loadDialogue("level-1-after");
            dialogueManager.startDialogue();
            updateInputProcessor();
        }

        // Level 2 Start Dialogue
        if ("level-2".equals(currentLevelName) && !levelStartDialoguePlayed) {
            levelStartTimer += delta;
            if (levelStartTimer >= 0.3f) {
                levelStartDialoguePlayed = true;
                dialogueManager.loadDialogue("level-2-pre");
                dialogueManager.startDialogue();
                updateInputProcessor();
            }
        }

        // Level 2 After Dialogue (Key Pickup)
        if ("level-2".equals(currentLevelName) && !levelAfterDialoguePlayed && character != null
                && character.hasKey()) {
            levelAfterDialoguePlayed = true;
            EncyclopediaManager.getInstance().unlock(game, "enemy_drone");
            dialogueManager.loadDialogue("level-2-after");
            dialogueManager.startDialogue();
            updateInputProcessor();
        }

        if ("level-4".equals(currentLevelName) || (isEndlessVer2 && !currentLevelName.endsWith("-3"))) {
            // 第四关：停止其他音乐，播放迷雾音乐
            if (game.backgroundMusic.isPlaying())
                game.backgroundMusic.stop();
            if (game.bossFightMusic.isPlaying())
                game.bossFightMusic.stop();

            if (!game.warFogMusic.isPlaying()) {
                game.warFogMusic.play();
            }
        } else if ("level-5".equals(currentLevelName) || (isEndlessVer2 && currentLevelName.endsWith("-3"))) {
            EncyclopediaManager.getInstance().unlock(game, "boss_guardian");
            // 第五关：停止其他音乐，播放Boss音乐
            if (game.backgroundMusic.isPlaying())
                game.backgroundMusic.stop();
            if (game.warFogMusic.isPlaying())
                game.warFogMusic.stop();

            if (!game.bossFightMusic.isPlaying()) {
                game.bossFightMusic.play();
            }
        } else {
            // 其他关卡：停止迷雾和Boss音乐，播放背景音乐
            if (game.warFogMusic.isPlaying())
                game.warFogMusic.stop();
            if (game.bossFightMusic.isPlaying())
                game.bossFightMusic.stop();

            if (!game.backgroundMusic.isPlaying()) {
                game.backgroundMusic.play();
            }
        }

        // Level 4 After Dialogue (Key Pickup)
        if ("level-4".equals(currentLevelName) && !levelAfterDialoguePlayed && character != null
                && character.hasKey()) {
            levelAfterDialoguePlayed = true;
            dialogueManager.loadDialogue("level-4-after");
            dialogueManager.startDialogue();
            updateInputProcessor();
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F3)) {
            toggleDebug();
        }

        if (Gdx.input.isKeyJustPressed(game.getConfigManager().getKey("PAUSE"))) {
            if (dialogueManager.isActive()) {
                // Maybe allow pausing during dialogue, or just ignore
            } else {
                togglePause();
            }
        }

        // Interaction Check for Dialogue
        if (!isPaused && !dialogueManager.isActive() && character != null) {

            // Update Dungeon Controller (Room Logic)
            if (dungeonController != null) {
                dungeonController.update(delta);
            }

            if (mapObjects != null) {
                boolean nearTrigger = false;
                for (GameObject obj : mapObjects) {
                    if (obj instanceof de.tum.cit.fop.maze.GameObj.DialogueTrigger) {
                        de.tum.cit.fop.maze.GameObj.DialogueTrigger trigger = (de.tum.cit.fop.maze.GameObj.DialogueTrigger) obj;
                        if (trigger.checkProximity(character.getPosition())) {
                            nearTrigger = true;
                            if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                                dialogueManager.startDialogue();
                                updateInputProcessor();

                                // Check if this is the Nono unlock trigger
                                if ("nono-unlock".equals(trigger.getDialogueId())) {
                                    pendingNonoUnlock = true;
                                }
                            }
                        }
                    } else if (obj instanceof de.tum.cit.fop.maze.GameObj.NonoNPC) {
                        de.tum.cit.fop.maze.GameObj.NonoNPC npc = (de.tum.cit.fop.maze.GameObj.NonoNPC) obj;
                        if (npc.checkProximity(character.getPosition())) {
                            nearTrigger = true;
                            if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                                if ("level-0".equals(currentLevelName)) {
                                    dialogueManager.loadDialogue("level-0");
                                }
                                dialogueManager.startDialogue();
                                updateInputProcessor();
                                if ("nono-unlock".equals(npc.getDialogueId())) {
                                    pendingNonoUnlock = true;
                                }
                            }
                        }
                    }
                }
                if (hud != null) {
                    hud.setPromptVisible(nearTrigger && !dialogueManager.isActive());
                }
            }

            // Check for completed dialogue to unlock Nono
            if (pendingNonoUnlock && !dialogueManager.isActive()) {
                pendingNonoUnlock = false;
                game.getPlayerState().setNonoUnlocked(true);
                game.saveGame();
                // Spawn Nono immediately
                nono = new de.tum.cit.fop.maze.GameObj.Nono(character.getPosition().x, character.getPosition().y,
                        character);

                // Spawn MaskItem above Nono
                Texture maskTex = new Texture(Gdx.files.internal("selfmade/maskitem.png"));
                TextureRegion maskRegion = new TextureRegion(maskTex);
                de.tum.cit.fop.maze.GameObj.MaskItem maskItem = new de.tum.cit.fop.maze.GameObj.MaskItem(
                        nono.getPosition().x,
                        nono.getPosition().y + 32,
                        16, 16,
                        maskRegion);
                mapObjects.add(maskItem);

                // Remove the NonoNPC
                mapObjects.removeIf(obj -> obj instanceof de.tum.cit.fop.maze.GameObj.NonoNPC &&
                        "nono-unlock".equals(((de.tum.cit.fop.maze.GameObj.NonoNPC) obj).getDialogueId()));
            }

            // Attack Logic (Moved to Character.java)

        }

        ScreenUtils.clear(0, 0, 0, 1);
        boolean isLevelCompleted = character.isLevelCompleted();
        if (!dialogueManager.isActive()) {
            levelTimer += delta;
        }

        if (!isPaused && !isGameOver && !isLevelCompleted && !dialogueManager.isActive()) {
            if (character != null) {

                if (character.isScreenShakeRequested()) {
                    if (screenShake != null)
                        screenShake.start(0.3f, 0.8f);
                    character.clearScreenShakeRequest();
                }

                if (character.isBlockEffectRequested()) {
                    System.out.println("Spawning BLOCK effect!");
                    damageNumbers.add(new de.tum.cit.fop.maze.VFX.DamageNumber(
                            character,
                            "BLOCK",
                            com.badlogic.gdx.graphics.Color.CYAN));
                    character.clearBlockEffectRequest();
                }

                if (character.isDamageNumberRequested()) {
                    damageNumbers.add(new de.tum.cit.fop.maze.VFX.DamageNumber(character, 1));
                    character.clearDamageNumberRequest();
                }

                if ("level-5".equals(currentLevelName) && !isBossRoomLocked) {
                    // Check if player enters Boss Room (Threshold X > 20 tiles [Corridor ends at
                    // 20])
                    if (character.getPosition().x > 22 * 16) {
                        isBossRoomLocked = true;

                        if (wallTextureForLock == null) {
                            wallTextureForLock = new Texture(Gdx.files.internal("selfmade/basictile.png"));
                        }
                        TextureRegion[][] tmp = TextureRegion.split(wallTextureForLock, 32, 32);
                        TextureRegion wallRegion = tmp[0][0];

                        // Spawn Walls at ENTRANCE X=21, Y=14, 15, 16
                        for (int y = 14; y <= 16; y++) {
                            de.tum.cit.fop.maze.GameObj.Wall w = new de.tum.cit.fop.maze.GameObj.Wall(21 * 16, y * 16,
                                    16, 16, wallRegion);
                            mapObjects.add(w);
                            lockedWalls.add(w);
                        }

                        // Spawn Walls at EXIT X=37, Y=14, 15, 16
                        for (int y = 14; y <= 16; y++) {
                            de.tum.cit.fop.maze.GameObj.Wall w = new de.tum.cit.fop.maze.GameObj.Wall(37 * 16, y * 16,
                                    16, 16, wallRegion);
                            mapObjects.add(w);
                            lockedWalls.add(w);
                        }

                        showPopupMessage("TRAPPED! DEFEAT THE BOSS!");

                        // Activate Boss (Spawn if not exists)
                        if (activeBoss == null && level5BossSpawnPos != null) {
                            Boss boss = new de.tum.cit.fop.maze.GameObj.Boss(
                                    level5BossSpawnPos.x,
                                    level5BossSpawnPos.y,
                                    de.tum.cit.fop.maze.MapLoader.getBossAnimations(),
                                    grid,
                                    character,
                                    projectiles,
                                    bulletTex);
                            this.activeBoss = boss;
                            this.bossItemSpawnTimer = 0f;
                            enemies.add(boss);
                        }

                        if (activeBoss != null) {
                            activeBoss.setActive(true);
                            // Trigger Level 5 Boss Dialogue
                            if (!level5BossDialoguePlayed) {
                                dialogueManager.loadDialogue("level-5");
                                dialogueManager.startDialogue();
                                level5BossDialoguePlayed = true;
                            }
                        }
                    }
                } else if (activeBoss != null && !activeBoss.isActive()) {
                    // Generic Boss Activation (Endless Mode / Other Levels)
                    // Trigger when Player is close (simulating "Entering Boss Room")
                    // Distance < 12 tiles (192 pixels)
                    if (character.getPosition().dst(activeBoss.getPosition()) < 192) {
                        activeBoss.setActive(true);
                    }
                }

                // Level 5 Logic: Start Dialogue
                if ("level-5".equals(currentLevelName) && !level5StartDialoguePlayed) {
                    levelStartTimer += delta;
                    if (levelStartTimer >= 1.0f) {
                        dialogueManager.loadDialogue("level-5-pre");
                        dialogueManager.startDialogue();
                        level5StartDialoguePlayed = true;
                    }
                }

                // Trigger Camera Flyby after Dialogue finishes
                if ("level-5".equals(currentLevelName) && level5StartDialoguePlayed && !dialogueManager.isActive()) {
                    if (level5CameraState == 0) {
                        level5CameraState = 1; // Start Sequence
                    }
                }

                // Level 5 Logic: Near Exit Logic (Trigger Ending when X > 51)
                // Exit is at X=53.
                if ("level-5".equals(currentLevelName) && character.getPosition().x > 51 * 16 && character.hasKey()) {
                    // Check if not already transitioning?
                    // Just trigger ending directly

                    awardXP(true);
                    game.setScreen(
                            new de.tum.cit.fop.maze.GameControl.CinematicScreen(game, "story/data/ending.json", () -> {
                                game.goToMenu(false);
                            }));
                    return;
                }

                character.update(delta, mapObjects, enemies, game.getConfigManager());

                // Tutorial Hints Logic
                if ("level-0".equals(currentLevelName)) {
                    // Show movement hint at start
                    hud.showMoveHint();
                    hud.showSprintHint();

                    // Dismiss move hint if WASD pressed
                    if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.A) ||
                            Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                        hud.dismissMoveHint();
                    }

                    // Dismiss sprint hint if Shift pressed
                    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                            || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                        hud.dismissSprintHint();
                    }
                }

                // Level 2: Show attack hint after getting weapon
                if ("level-2".equals(currentLevelName) && character.isAttackUnlocked()) {
                    hud.showAttackHint();

                    // Dismiss attack hint if J pressed
                    if (Gdx.input.isKeyPressed(Input.Keys.J)) {
                        hud.dismissAttackHint();
                    }
                }

                if (!debugMapMode) {
                    if (level5CameraState == 1) {
                        // Pan to Exit (X=53 * 16 = 848)
                        float targetX = 53 * 16 + 8;
                        float targetY = 15 * 16 + 8;
                        float dist = com.badlogic.gdx.math.Vector2.dst(camera.position.x, camera.position.y, targetX,
                                targetY);

                        float speed = 300f * delta; // Constant speed pan? Or Lerp? Lerp is smoother.
                        float lerpSpeed = 2f;
                        camera.position.x += (targetX - camera.position.x) * lerpSpeed * delta;
                        camera.position.y += (targetY - camera.position.y) * lerpSpeed * delta;

                        if (dist < 10) {
                            level5CameraState = 2; // Arrived
                            level5CameraTimer = 0f;
                        }
                    } else if (level5CameraState == 2) {
                        // Wait 1s
                        level5CameraTimer += delta;
                        if (level5CameraTimer >= 1.0f) {
                            level5CameraState = 3;
                        }
                    } else if (level5CameraState == 3) {
                        // Pan back to Player
                        float targetX = character.getPosition().x + 8;
                        float targetY = character.getPosition().y + 16;

                        float lerpSpeed = 3f;
                        camera.position.x += (targetX - camera.position.x) * lerpSpeed * delta;
                        camera.position.y += (targetY - camera.position.y) * lerpSpeed * delta;

                        float dist = com.badlogic.gdx.math.Vector2.dst(camera.position.x, camera.position.y, targetX,
                                targetY);
                        if (dist < 10) {
                            level5CameraState = 4; // Finished
                        }
                    } else {
                        // Normal Following
                        float targetX = character.getPosition().x + 8;
                        float targetY = character.getPosition().y + 16;

                        float lerpSpeed = 5f;
                        camera.position.x += (targetX - camera.position.x) * lerpSpeed * delta;
                        camera.position.y += (targetY - camera.position.y) * lerpSpeed * delta;
                    }
                }

                if (screenShake != null) {
                    screenShake.update(delta, camera);
                } else {
                    camera.update();
                }

                if (projectiles != null) {
                    java.util.Iterator<de.tum.cit.fop.maze.GameObj.Projectile> pIter = projectiles.iterator();
                    while (pIter.hasNext()) {
                        de.tum.cit.fop.maze.GameObj.Projectile p = pIter.next();
                        p.update(delta);

                        // 1. 简单的撞墙检测 (如果子弹中心在不可行走的格子上)
                        int gx = (int) (p.getPosition().x / 16);
                        int gy = (int) (p.getPosition().y / 16);
                        // grid 必须在 GameScreen 中可访问，确保你在 setupLevel 中初始化了 grid
                        if (grid != null && !grid.isWalkable(gx, gy)) {
                            p.setMarkedForRemoval(true);
                        }

                        // 2. 击中玩家检测 (Boss打玩家)
                        if (p.isEnemyProjectile() && character != null) {
                            if (p.getBounds().overlaps(character.getBounds())) {
                                if (character.isShielded()) {
                                    game.playBlockSound();
                                } else {
                                    character.takeDamage(1);
                                }
                                p.setMarkedForRemoval(true);
                            }
                        }

                        if (p.isMarkedForRemoval()) {
                            pIter.remove();
                        }
                    }
                }

            }
            if (mapObjects != null) {
                mapObjects.removeIf(GameObject::isMarkedForRemoval);
            }
            if (character.isLevelCompleted()) {
                de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance()
                        .onEvent(de.tum.cit.fop.maze.GameControl.EventType.LEVEL_COMPLETE, 1);
                showGameOverMenu(true);

            }
            if (character.isDead()) {
                showGameOverMenu(false);
            }
        }

        viewport.apply();
        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        game.getSpriteBatch().begin();

        if (mapObjects != null) {
            for (GameObject obj : mapObjects) {
                // Skip rendering Enemies/Boss here as they are rendered separately
                if (obj instanceof de.tum.cit.fop.maze.GameObj.Enemy) {
                    continue;
                }

                if (obj instanceof de.tum.cit.fop.maze.GameObj.Heart) {
                    ((de.tum.cit.fop.maze.GameObj.Heart) obj).update(delta);
                } else if (obj instanceof de.tum.cit.fop.maze.GameObj.ShieldItem) {
                    ((de.tum.cit.fop.maze.GameObj.ShieldItem) obj).update(delta);
                } else if (obj instanceof de.tum.cit.fop.maze.GameObj.NonoNPC) {
                    ((de.tum.cit.fop.maze.GameObj.NonoNPC) obj).update(delta);
                }

                if (obj.getTextureRegion() != null) {
                    game.getSpriteBatch().draw(obj.getTextureRegion(), obj.getPosition().x, obj.getPosition().y,
                            obj.getWidth(), obj.getHeight());
                }
            }
        }

        if (character != null) {
            character.draw(game.getSpriteBatch());
        }

        if (nono != null) {
            nono.draw(game.getSpriteBatch());
        }
        if (projectiles != null) {
            for (de.tum.cit.fop.maze.GameObj.Projectile p : projectiles) {

                game.getSpriteBatch().draw(p.getTextureRegion(), p.getPosition().x, p.getPosition().y, 8, 8);
            }
        }
        for (de.tum.cit.fop.maze.GameObj.Enemy enemy : enemies) {
            enemy.draw(game.getSpriteBatch());

            enemy.drawStatus(game.getSpriteBatch(), font, debugEnabled);
        }

        // Level 4 Lighting (Draws Multiply layer over map AND characters/enemies)
        if ("level-4".equals(currentLevelName) && flashlightEffect != null && level4IntroState >= 8) {
            game.getSpriteBatch().end(); // End batch from map/entity rendering

            Vector2 targetPos = null;
            if (character != null) {
                targetPos = new Vector2(character.getPosition().x + character.getWidth() / 2,
                        character.getPosition().y + character.getHeight() / 2);
            }
            Vector2 nonoPos = null;
            if (nono != null) {
                nonoPos = new Vector2(nono.getPosition().x + nono.getWidth() / 2,
                        nono.getPosition().y + nono.getHeight() / 2);
            }

            flashlightEffect.render(delta, camera, viewport, game.getSpriteBatch(), shapeRenderer, nonoPos, targetPos);

            game.getSpriteBatch().begin(); // Restart batch for damage numbers
        }

        if (damageNumbers != null) {
            java.util.Iterator<de.tum.cit.fop.maze.VFX.DamageNumber> iter = damageNumbers.iterator();
            while (iter.hasNext()) {
                de.tum.cit.fop.maze.VFX.DamageNumber dn = iter.next();
                dn.render(game.getSpriteBatch(), font);

                if (!isPaused && !isGameOver && !character.isLevelCompleted() && !dialogueManager.isActive()) {
                    dn.update(delta);
                }

                if (dn.isFinished()) {
                    iter.remove();
                }
            }
        }

        game.getSpriteBatch().end();

        if (!isPaused && !isGameOver && !character.isLevelCompleted() && !dialogueManager.isActive()) {
            java.util.Iterator<de.tum.cit.fop.maze.GameObj.Enemy> enemyIter = enemies.iterator();
            while (enemyIter.hasNext()) {
                de.tum.cit.fop.maze.GameObj.Enemy enemy = enemyIter.next();

                // User Request: Boss stops moving when dead (skip update)
                if (!(enemy instanceof de.tum.cit.fop.maze.GameObj.Boss && enemy.isMarkedForRemoval())) {
                    enemy.update(delta);
                }

                if (enemy.isMarkedForRemoval()) {

                    // Trigger Cinematic Screen if Boss dies (with 2s delay)
                    if (enemy instanceof de.tum.cit.fop.maze.GameObj.Boss) {
                        bossDeathTimer += delta;
                        if (bossDeathTimer < 2.0f) {
                            continue; // Wait until timer reaches 2.0s
                        }
                        if (isEndlessVer2) {
                            // Achievement Trigger
                            de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance()
                                    .onEvent(de.tum.cit.fop.maze.GameControl.EventType.KILL_BOSS_ENDLESS, 1);

                            // Restore health to full when boss is defeated
                            if (character != null) {
                                character.setLives(character.getMaxLives());
                            }

                            // Spawn Exit
                            Texture texture = new Texture(Gdx.files.internal("selfmade/basictile.png"));
                            TextureRegion[][] regions = TextureRegion.split(texture, 32, 32);
                            TextureRegion exitRegion = regions[0][1];

                            mapObjects.add(new Exit(enemy.getPosition().x, enemy.getPosition().y, 16, 16, exitRegion));

                            enemyIter.remove();
                            continue;
                        }

                        // Level 5 Boss Defeat Logic:
                        // 1. Drop Key
                        // 2. Remove Lock Walls
                        // 3. No Cinematic (User Request)
                        if ("level-5".equals(currentLevelName)) {
                            // Spawn Key at Boss location
                            Texture keyTex = new Texture(Gdx.files.internal("selfmade/basictile.png")); // Or chest
                                                                                                        // texture
                            TextureRegion[][] regs = TextureRegion.split(keyTex, 32, 32);
                            TextureRegion keyRegion = regs[1][1];

                            mapObjects.add(new de.tum.cit.fop.maze.GameObj.Key(enemy.getPosition().x,
                                    enemy.getPosition().y, 16, 16, keyRegion));

                            // Unlock Walls
                            if (lockedWalls != null && !lockedWalls.isEmpty()) {
                                for (de.tum.cit.fop.maze.GameObj.Wall w : lockedWalls) {
                                    w.setMarkedForRemoval(true);
                                }
                                lockedWalls.clear();
                                showPopupMessage("The barriers disappear!");
                            }

                            // Trigger "After Boss" Dialogue
                            if (!level5AfterDialoguePlayed) {
                                dialogueManager.loadDialogue("level-5-after");
                                dialogueManager.startDialogue();
                                level5AfterDialoguePlayed = true;
                            }

                            enemyIter.remove();
                            return;
                        }

                        game.setScreen(new de.tum.cit.fop.maze.GameControl.CinematicScreen(game,
                                "story/data/ending.json", () -> {
                                    game.goToMenu(false);
                                }));
                        return;
                    }

                    de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance()
                            .onEvent(de.tum.cit.fop.maze.GameControl.EventType.KILL_ENEMY, 1);

                    enemyIter.remove();
                }
            }

            // Boss level item spawning
            if (activeBoss != null && !activeBoss.isMarkedForRemoval()) {
                bossItemSpawnTimer += delta;
                if (bossItemSpawnTimer >= BOSS_ITEM_SPAWN_INTERVAL) {
                    bossItemSpawnTimer = 0f;
                    spawnBossLevelItem();
                }
            }
        }

        if (character != null) {

            // Level 3 Dynamic Dialogue Logic
            if ("level-3".equals(currentLevelName)) {
                int currentHp = (int) character.getCurrentHealth();
                if (lastHealth != -1) {
                    // Trigger 1: Damage Taken (Delayed 0.5s)
                    if (currentHp < lastHealth && !l3DamageTriggered && damageDialogueTimer == -1) {
                        damageDialogueTimer = 0;
                    }

                    if (damageDialogueTimer >= 0) {
                        damageDialogueTimer += delta;
                        if (damageDialogueTimer >= 0.5f) {
                            dialogueManager.loadDialogue("level-3-pre");
                            dialogueManager.startDialogue();
                            l3DamageTriggered = true;
                            damageDialogueTimer = -1;
                        }
                    }

                    // Trigger 2: Healed (picked up blood)
                    if (currentHp > lastHealth && !l3HealTriggered) {
                        dialogueManager.loadDialogue("level-3-after");
                        dialogueManager.startDialogue();
                        l3HealTriggered = true;
                    }
                }
                lastHealth = currentHp;
            }

            // Level 4 Logic: Intro Sequence
            if ("level-4".equals(currentLevelName)) {
                switch (level4IntroState) {
                    case 0:
                        // Start Dialogue 1
                        dialogueManager.loadDialogue("level-4");
                        dialogueManager.startDialogue();
                        level4IntroState = 1;
                        break;
                    case 1:
                        // Wait for Dialog 1
                        if (!dialogueManager.isActive()) {
                            dialogueManager.loadDialogue("level-4-1");
                            dialogueManager.startDialogue();
                            level4IntroState = 2;
                        }
                        break;
                    case 2:
                        // Wait for Dialog 2
                        if (!dialogueManager.isActive()) {
                            dialogueManager.loadDialogue("level-4-2");
                            dialogueManager.startDialogue();
                            level4IntroState = 3;
                        }
                        break;
                    case 3:
                        // Wait for Dialog 3
                        if (!dialogueManager.isActive()) {
                            level4IntroTimer = 0;
                            level4IntroState = 4;
                        }
                        break;
                    case 4:
                        // 2s Delay
                        level4IntroTimer += delta;
                        if (level4IntroTimer >= 2.0f) {
                            level4IntroTimer = 0;
                            level4IntroState = 5;
                            level4FlashCount = 0;
                        }
                        break;
                    case 5:
                        // Flashes: 2 flashes. (White -> Normal -> White -> Normal -> Black)
                        // Flash duration 0.1s
                        level4IntroTimer += delta;
                        if (level4IntroTimer >= 0.1f) {
                            level4IntroTimer = 0;
                            level4FlashCount++;
                            if (level4FlashCount >= 4) { // 2 full cycles (0,1,2,3) -> 4
                                level4IntroState = 6;
                                level4IntroTimer = 0f; // Reset for black screen wait
                            }
                        }
                        // Draw White Overlay if FlashCount is EVEN (0, 2)
                        if (level4FlashCount % 2 == 0) {
                            Gdx.gl.glEnable(GL20.GL_BLEND);
                            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                            shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
                            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                            shapeRenderer.setColor(1, 1, 1, 1); // White
                            // Use huge rect to be safe
                            shapeRenderer.rect(camera.position.x - 1000, camera.position.y - 1000, 2000, 2000);
                            shapeRenderer.end();
                            Gdx.gl.glDisable(GL20.GL_BLEND);
                        }
                        break;
                    case 6:
                        // Black Screen Wait (1s)

                        // Draw Black Overlay
                        Gdx.gl.glEnable(GL20.GL_BLEND);
                        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
                        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                        shapeRenderer.setColor(0, 0, 0, 1); // Black
                        shapeRenderer.rect(camera.position.x - 1000, camera.position.y - 1000, 2000, 2000);
                        shapeRenderer.end();
                        Gdx.gl.glDisable(GL20.GL_BLEND);

                        level4IntroTimer += delta;
                        if (level4IntroTimer >= 1.0f) {
                            // Start Pre-Dialogue
                            dialogueManager.loadDialogue("level-4-pre");
                            dialogueManager.startDialogue();
                            level4IntroState = 7;
                        }
                        break;
                    case 7:
                        // Waiting for Pre-Dialogue
                        // Draw Black Overlay
                        Gdx.gl.glEnable(GL20.GL_BLEND);
                        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
                        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                        shapeRenderer.setColor(0, 0, 0, 1); // Black
                        // Use huge rect to be safe
                        shapeRenderer.rect(camera.position.x - 1000, camera.position.y - 1000, 2000, 2000);
                        shapeRenderer.end();
                        Gdx.gl.glDisable(GL20.GL_BLEND);

                        if (!dialogueManager.isActive()) {
                            level4IntroState = 8;
                        }
                        break;
                    case 8:
                        // Game Start: Enable Flashlight
                        if (flashlightEffect != null && !flashlightEffect.isEnabled()) {
                            flashlightEffect.setEnabled(true);
                        }
                        break;
                }
            }

            hud.update(character);
            hud.render(delta);
        }

        if (debugEnabled && character != null) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(
                    character.getBounds().x,
                    character.getBounds().y,
                    character.getBounds().width,
                    character.getBounds().height);

            shapeRenderer.setColor(Color.GREEN);
            if (mapObjects != null) {
                for (GameObject obj : mapObjects) {
                    if (obj instanceof de.tum.cit.fop.maze.GameObj.Wall) {
                        shapeRenderer.rect(
                                obj.getBounds().x,
                                obj.getBounds().y,
                                obj.getBounds().width,
                                obj.getBounds().height);
                    }
                }
            }

            for (de.tum.cit.fop.maze.GameObj.Enemy enemy : enemies) {
                enemy.drawDebug(shapeRenderer);
            }

            if (character.isAttacking()) {
                shapeRenderer.setColor(Color.RED);
                com.badlogic.gdx.math.Rectangle attackBox = character.getAttackRect();
                shapeRenderer.rect(attackBox.x, attackBox.y, attackBox.width, attackBox.height);
            }

            if (character.isAttacking()) {
                shapeRenderer.setColor(Color.RED);
                com.badlogic.gdx.math.Rectangle attackBox = character.getAttackRect();
                shapeRenderer.rect(attackBox.x, attackBox.y, attackBox.width, attackBox.height);
            }

            shapeRenderer.end();
        }

        if (nono != null && !isPaused && !isGameOver && !dialogueManager.isActive()) {
            nono.setMapObjects(mapObjects);
            nono.update(delta);
        }

        // Handle Dialogue Unlock for Nono
        // If we just finished a dialogue with "nono-unlock" ID?
        // DialogueManager handles conversation. We need to know if it finished.
        // We can check if dialogue WAS active and NOW is not, and we have a flag set.

        // Or simpler: Check proximity to the trigger and if dialogue just closed.
        // GameScreen doesn't easily track WHICH dialogue just finished.
        // But for Level 0, if we interact with the trigger, we can assume it's the
        // unlock.
        // Let's refine the trigger logic above.

        if (isPaused || isGameOver || isLevelCompleted) {
            pauseStage.act(delta);
            pauseStage.draw();
        }

        if (dialogueManager.isActive()) {
            dialogueManager.render(delta);
        }

        // Auto-switch Input Processor when dialogue starts/ends
        if (dialogueManager.isActive() != wasDialogueActive) {
            updateInputProcessor();
            wasDialogueActive = dialogueManager.isActive();
        }

    }

    /**
     * Resizes the viewport and UI stages.
     * 
     * @param width  New width.
     * @param height New height.
     */
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        pauseStage.getViewport().update(width, height, true);
        if (dialogueManager != null) {
            dialogueManager.resize(width, height);
        }
        hud.resize(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    /**
     * Called when this screen becomes the current screen.
     */
    @Override
    public void show() {
        updateInputProcessor();
    }

    @Override
    public void hide() {
    }

    /**
     * Disposes of the screen resources.
     */
    @Override
    public void dispose() {
        if (pauseStage != null)
            pauseStage.dispose();
        if (pauseMenu != null)
            pauseMenu.dispose();
        if (GameOverMenu != null)
            GameOverMenu.dispose();
        if (shapeRenderer != null)
            shapeRenderer.dispose();
        if (hud != null)
            hud.dispose();
        if (dialogueManager != null)
            dialogueManager.dispose();
        if (flashlightEffect != null)
            flashlightEffect.dispose();
        if (wallTextureForLock != null)
            wallTextureForLock.dispose();
    }

    /**
     * Calculates the score for the current level.
     * 
     * @return The calculated score.
     */
    public int calculateScore() {
        int lifeScore = 0;
        if (character != null) {
            lifeScore = character.getLives() * SCORE_PER_LIFE;
        }

        int timePenalty = (int) (levelTimer * PENALTY_PER_SECOND);
        int totalScore = BASE_SCORE_PER_LEVEL - timePenalty;

        if (isProcedural) {
            totalScore *= currentDifficulty;
        }

        totalScore += lifeScore;

        return Math.max(0, totalScore);
    }

    /**
     * Gets the formatted level timer.
     * 
     * @return String in MM:SS format.
     */
    public String getFormattedTime() {
        int minutes = (int) levelTimer / 60;
        int seconds = (int) levelTimer % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Returns the main game instance.
     * 
     * @return MazeRunnerGame instance.
     */
    public MazeRunnerGame getGame() {
        return game;
    }

    public Boss getActiveBoss() {
        return activeBoss;
    }

    /**
     * Spawns a health or shield item at a random walkable location during Boss
     * fight.
     */
    private void spawnBossLevelItem() {
        if (grid == null || mapObjects == null)
            return;

        // Randomly choose between health (70%) and shield (30%)
        boolean spawnHealth = com.badlogic.gdx.math.MathUtils.randomBoolean(0.7f);

        // Find a random walkable position
        int maxAttempts = 50;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int minX = 5;
            int maxX = grid.getWidth() - 5;
            int minY = 5;
            int maxY = grid.getHeight() - 5;

            // Level 5 Boss Room Constraint (X: 22-36, Y: 8-22)
            if ("level-5".equals(currentLevelName)) {
                minX = 22;
                maxX = 36;
                minY = 8;
                maxY = 22;
            }

            int randomX = com.badlogic.gdx.math.MathUtils.random(minX, maxX);
            int randomY = com.badlogic.gdx.math.MathUtils.random(minY, maxY);

            if (grid.isWalkable(randomX, randomY)) {
                float worldX = randomX * 16;
                float worldY = randomY * 16;

                // Check if position is not too close to player or Boss
                if (character != null) {
                    float distToPlayer = com.badlogic.gdx.math.Vector2.dst(
                            worldX, worldY,
                            character.getPosition().x, character.getPosition().y);
                    if (distToPlayer < 64)
                        continue; // Too close to player
                }

                if (activeBoss != null) {
                    float distToBoss = com.badlogic.gdx.math.Vector2.dst(
                            worldX, worldY,
                            activeBoss.getPosition().x, activeBoss.getPosition().y);
                    if (distToBoss < 80)
                        continue; // Too close to Boss
                }

                // Spawn the item
                if (spawnHealth) {
                    de.tum.cit.fop.maze.GameObj.Heart heart = new de.tum.cit.fop.maze.GameObj.Heart(worldX, worldY);
                    mapObjects.add(heart);
                } else {
                    de.tum.cit.fop.maze.GameObj.ShieldItem shield = new de.tum.cit.fop.maze.GameObj.ShieldItem(worldX,
                            worldY);
                    mapObjects.add(shield);
                }

                break; // Successfully spawned, exit loop
            }
        }
    }
}
