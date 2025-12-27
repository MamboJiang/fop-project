package de.tum.cit.fop.maze;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.GameObj.Character;
import de.tum.cit.fop.maze.GameObj.EntryPoint;
import de.tum.cit.fop.maze.GameObj.GameObject;
import de.tum.cit.fop.maze.GameControl.HUD;
import de.tum.cit.fop.maze.GameControl.PauseMenu;
import de.tum.cit.fop.maze.GameControl.GameOverMenu;

import java.util.List;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private OrthographicCamera camera;
    private Viewport viewport; // Added Viewport
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private HUD hud;
    
    // Debug
    private boolean debugEnabled = false;

    // Pause State and UI
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private Stage pauseStage;
    private PauseMenu pauseMenu;
    private GameOverMenu GameOverMenu;

    // Game Objects
    private de.tum.cit.fop.maze.GameObj.Character character;
    private List<GameObject> mapObjects;
    private List<de.tum.cit.fop.maze.GameObj.Enemy> enemies;
    private FileHandle mapFile;
    private de.tum.cit.fop.maze.AI.Grid grid;
    private List<de.tum.cit.fop.maze.VFX.DamageNumber> damageNumbers;
    
    // Procedural Generation
    private boolean isProcedural = false;
    private int currentDifficulty = 1;

    /**
     * Constructor for GameScreen (File Mode).
     */
    public GameScreen(MazeRunnerGame game, FileHandle mapFile) {
        this.game = game;
        this.mapFile = mapFile;
        this.isProcedural = false;

        initCommon();
        setupLevel();
    }
    
    /**
     * Constructor for GameScreen (Procedural Mode).
     */
    public GameScreen(MazeRunnerGame game, boolean isProcedural) {
        this.game = game;
        this.isProcedural = isProcedural;
        this.currentDifficulty = 1;
        
        initCommon();
        setupLevel();
    }
    
    private void initCommon() {
        // Create and configure the camera for the game view
        camera = new OrthographicCamera();
        camera.zoom = 0.7f; 
        
        viewport = new ExtendViewport(640, 360, camera);

        // Get the font from the game's skin
        font = game.getSkin().getFont("font");
        
        shapeRenderer = new ShapeRenderer();
        hud = new HUD(game.getSpriteBatch(), this, game.getSkin());
        screenShake = new de.tum.cit.fop.maze.VFX.ScreenShake();

        setupPauseMenu();
    }

    private void setupLevel() {
        if (isProcedural) {
             generateProceduralLevel();
             return;
        }

        // Load map
        // Ideally we should select levels, for now load level 1
        //FileHandle mapHandle = Gdx.files.internal("maps/level-1.properties");
        //if (!mapHandle.exists()) {
            // Try absolute path if internal fails (e.g. running from IDE root vs assets root)
            // However, map loader tries internal. Let's assume standard GDX internal structure.
            // If this fails, we might need to look in "assets/maps" or similar depending on working dir
        if(this.mapFile ==null || !this.mapFile.exists()){
            Gdx.app.error("GameScreen", "Map file is null or does not exist!");
            this.mapFile = Gdx.files.internal("maps/level-6.properties");
        }

        mapObjects = MapLoader.loadMap(this.mapFile);

        initMapObjects();
    }

    private void generateProceduralLevel() {
        // Size scales slightly with difficulty? Or static 50x50
        int size = 40 + (currentDifficulty * 2);
        if (size > 100) size = 100;
        
        de.tum.cit.fop.maze.Procedure.DungeonGenerator generator = new de.tum.cit.fop.maze.Procedure.DungeonGenerator(size, size);
        mapObjects = generator.generate(currentDifficulty);
        
        initMapObjects();
    }
    
    private void initMapObjects() {
        // Initialize AI Grid
        grid = new de.tum.cit.fop.maze.AI.Grid(0, 0, mapObjects);

        // Find entry point to spawn character
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
            character = new Character(spawnX+16, spawnY);
        } else {
             // Reset character position for new level
             character.setPosition(spawnX+16, spawnY);
             // Maybe retain health/key in procedural mode?
        }
        
        // Create Enemy List
        enemies = new java.util.ArrayList<>();
        
        // Find Enemy Spawn Points and Convert to Real Enemies
        List<GameObject> toRemove = new java.util.ArrayList<>();
        for (GameObject obj : mapObjects) {
             if (obj instanceof de.tum.cit.fop.maze.GameObj.EnemySpawnPoint) {
                 enemies.add(new de.tum.cit.fop.maze.GameObj.Enemy(
                     obj.getPosition().x, 
                     obj.getPosition().y, 
                     de.tum.cit.fop.maze.MapLoader.getMobAnimations(0, 0), // Base Enemy: Col 0, Row 0
                     grid, 
                     character
                 ));
                 toRemove.add(obj);
             } else if (obj instanceof de.tum.cit.fop.maze.GameObj.GhostSpawnPoint) {
                 enemies.add(new de.tum.cit.fop.maze.GameObj.Ghost(
                     obj.getPosition().x, 
                     obj.getPosition().y, 
                     grid, 
                     character
                 ));
                 toRemove.add(obj);
             }
        }
        
        // Remove spawn points from mapObjects so they don't render twice or collide
        mapObjects.removeAll(toRemove);
        
        damageNumbers = new java.util.ArrayList<>();
    }

    private void setupPauseMenu() {
        pauseStage = new Stage(new FitViewport(1920, 1080), game.getSpriteBatch());
        
        pauseMenu = new PauseMenu(game, 
            () -> togglePause(), // Resume action
            null // Exit action (default)
        );
        pauseStage.addActor(pauseMenu);
    }

    private void loadNextLevel() {
        if (isProcedural) {
            // Increase Difficulty
            currentDifficulty++;
            
            // Reset Game State
            isGameOver = false;
            isPaused = false;
            if (character != null) {
                character.resetForNewLevel();
            }
            
            // Hide Menu if open
            if (GameOverMenu != null) {
                GameOverMenu.remove();
            }
            if (pauseMenu != null) {
                pauseMenu.hide();
            }
            
            // Reset Input
            updateInputProcessor();
            
            // Generate next level
            generateProceduralLevel();
            return;
        }

        // 1. 获取所有地图文件
        List<FileHandle> maps = MapLoader.getMapFiles(); //
        int currentIndex = -1;

        // 2. 找到当前地图在列表中的位置
        for (int i = 0; i < maps.size(); i++) {
            // mapFile 是 GameScreen 的成员变量
            if (maps.get(i).name().equals(this.mapFile.name())) {
                currentIndex = i;
                break;
            }
        }

        // 3. 判断是否有下一张图
        if (currentIndex != -1 && currentIndex + 1 < maps.size()) {
            // 有下一关：告诉主游戏类切换到下一张地图
            FileHandle nextMap = maps.get(currentIndex + 1);
            game.goToGame(nextMap); //
        } else {
            // 没有下一关了（全通关）：回到主菜单
            game.goToMenu();
        }
    }

    private void showGameOverMenu(boolean win) {
        if (isGameOver) return; // 防止重复触发
        isGameOver = true;

        // 创建结果菜单
        int waves = isProcedural ? currentDifficulty - 1 : -1;
        GameOverMenu = new GameOverMenu(game,
                () -> {
                    // Retry 逻辑: 重新加载当前地图
                    // If procedural, this restarts the run? Or restarts the level?
                    // Currently we hid retry for procedural lose.
                    // But if we passed null as retry action, that would be safer.
                    if (isProcedural) {
                        game.goToEndlessMode(); // Restart run
                    } else {
                        game.goToGame(this.mapFile);
                    }
                },
                () -> {
                    // Exit 逻辑
                },
                () -> {
                    loadNextLevel();
                },
                win,
                waves
        );

        pauseStage.addActor(GameOverMenu);
        GameOverMenu.show();

        // 切换输入处理器到 UI
        Gdx.input.setInputProcessor(pauseStage);
    }

    /**
     * Toggles the pause state and handles input processor switching.
     */
    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            pauseMenu.show();
        } else {
            pauseMenu.hide(); // Should already be hidden by Resume button, but safe to call
        }
        updateInputProcessor();
    }
    
    public void updateInputProcessor() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        if (isPaused) {
            multiplexer.addProcessor(pauseStage);
        } else {
            // Process HUD input first, then game input (if any)
            multiplexer.addProcessor(hud.getStage());
        }
        Gdx.input.setInputProcessor(multiplexer);
    }
    
    public void toggleDebug() {
        debugEnabled = !debugEnabled;
    }
    
    public void zoomIn() {
        // With viewport, zoom modifies camera.zoom directly
        camera.zoom = Math.max(0.1f, camera.zoom - 0.1f);
        camera.update();
    }
    
    public void zoomOut() {
        camera.zoom = Math.min(2.0f, camera.zoom + 0.1f);
        camera.update();
    }


        // Camera Shake
    private de.tum.cit.fop.maze.VFX.ScreenShake screenShake;

    // Screen interface methods with necessary functionality
    @Override
    public void render(float delta) {
        // Toggle Debug
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F3)) {
            toggleDebug();
        }

        // Check for pause key press to go back to the menu
        if (Gdx.input.isKeyJustPressed(game.getConfigManager().getKey("PAUSE"))) {
            togglePause();
        }

        ScreenUtils.clear(0, 0, 0, 1); // Clear the screen
        boolean isLevelCompleted = character.isLevelCompleted();
        // Logic update
        if (!isPaused && !isGameOver && !isLevelCompleted) {
            if (character != null) {
                // Check if shake is requested
                if (character.isScreenShakeRequested()) {
                    if (screenShake != null) screenShake.start(0.3f, 0.8f);
                    character.clearScreenShakeRequest();
                }
                
                // Check if damage number requested
                if (character.isDamageNumberRequested()) {
                     damageNumbers.add(new de.tum.cit.fop.maze.VFX.DamageNumber(character, 1));
                     character.clearDamageNumberRequest();
                }

                character.update(delta, mapObjects, game.getConfigManager());

                // Camera follow character with smooth lerp
                float targetX = character.getPosition().x + 8; // Center of 16 width
                float targetY = character.getPosition().y + 16; // Center of 32 height
                
                // Lerp factor (adjust for smoothness, 5f is typical)
                float lerpSpeed = 5f;
                camera.position.x += (targetX - camera.position.x) * lerpSpeed * delta;
                camera.position.y += (targetY - camera.position.y) * lerpSpeed * delta;
                
                // Apply Shake (Post-Lerp)
                if (screenShake != null) {
                    screenShake.update(delta, camera);
                } else {
                    camera.update();
                }
            }
            if(mapObjects != null){
                mapObjects.removeIf(GameObject::isMarkedForRemoval);
            }
            if (character.isLevelCompleted()) {
                showGameOverMenu(true);

            }
            if(character.isDead()){
                showGameOverMenu(false);
            }
        }

        // Render
        viewport.apply(); // Update camera viewport
        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        game.getSpriteBatch().begin();

        // Draw map objects
        if (mapObjects != null) {
            for (GameObject obj : mapObjects) {
                if (obj.getTextureRegion() != null) {
                    game.getSpriteBatch().draw(obj.getTextureRegion(), obj.getPosition().x, obj.getPosition().y, obj.getWidth(), obj.getHeight());
                }
            }
        }

        // Draw character
        if (character != null) {
            character.draw(game.getSpriteBatch());
        }
        
        // Draw Enemies
        for (de.tum.cit.fop.maze.GameObj.Enemy enemy : enemies) {
            game.getSpriteBatch().draw(enemy.getTextureRegion(), enemy.getPosition().x, enemy.getPosition().y, 16, 16);
            // Draw Status Icon
            enemy.drawStatus(game.getSpriteBatch(), font);
        }
        
        // Draw Damage Numbers
        if (damageNumbers != null) {
            java.util.Iterator<de.tum.cit.fop.maze.VFX.DamageNumber> iter = damageNumbers.iterator();
            while (iter.hasNext()) {
                de.tum.cit.fop.maze.VFX.DamageNumber dn = iter.next();
                dn.render(game.getSpriteBatch(), font);
                
                if (!isPaused && !isGameOver && !character.isLevelCompleted()) {
                    dn.update(delta);
                }
                
                if (dn.isFinished()) {
                    iter.remove();
                }
            }
        }

        game.getSpriteBatch().end(); // Important to call this after drawing everything
        
        // Update Enemies
        if (!isPaused && !isGameOver && !character.isLevelCompleted()) {
            for (de.tum.cit.fop.maze.GameObj.Enemy enemy : enemies) {
                enemy.update(delta);
            }
        }
        
        // Draw HUD
        if (character != null) {
            hud.update(character);
            hud.render(delta);
        }
        
        
        // Debug Rendering for Collision Boxes
        if (debugEnabled && character != null) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            
            // Draw Character Bounds (Red)
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(
                character.getBounds().x, 
                character.getBounds().y, 
                character.getBounds().width, 
                character.getBounds().height
            );
            
            // Draw Wall Bounds (Green)
            shapeRenderer.setColor(Color.GREEN);
            if (mapObjects != null) {
                for (GameObject obj : mapObjects) {
                    if (obj instanceof de.tum.cit.fop.maze.GameObj.Wall) {
                        shapeRenderer.rect(
                            obj.getBounds().x, 
                            obj.getBounds().y, 
                            obj.getBounds().width, 
                            obj.getBounds().height
                        );
                    }
                }
            }
            
            // Draw Enemy Path Debug
            for (de.tum.cit.fop.maze.GameObj.Enemy enemy : enemies) {
                enemy.drawDebug(shapeRenderer);
            }
            
            shapeRenderer.end();
        }

        // Draw pause menu if paused
        if (isPaused || isGameOver || isLevelCompleted) {
            pauseStage.act(delta);
            pauseStage.draw();
        }
    }


    public void resize(int width, int height) {
        viewport.update(width, height, false); // Update Viewport
        pauseStage.getViewport().update(width, height, true);
        hud.resize(width, height);
    }


    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {
        updateInputProcessor();
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (pauseStage != null) pauseStage.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (hud != null) hud.dispose();
    }
}
