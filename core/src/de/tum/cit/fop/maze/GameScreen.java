package de.tum.cit.fop.maze;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.GameObj.Character;
import de.tum.cit.fop.maze.GameObj.EntryPoint;
import de.tum.cit.fop.maze.GameObj.GameObject;
import de.tum.cit.fop.maze.GameControl.HUD;
import de.tum.cit.fop.maze.GameControl.PauseMenu;

import java.util.List;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private final Viewport viewport; // Added Viewport
    private final BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private HUD hud;
    
    // Debug
    private boolean debugEnabled = false;

    // Pause State and UI
    private boolean isPaused = false;
    private Stage pauseStage;
    private PauseMenu pauseMenu;

    // Game Objects
    private de.tum.cit.fop.maze.GameObj.Character character;
    private List<GameObject> mapObjects;
    private FileHandle mapFile;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game, FileHandle mapFile) {
        this.game = game;
        this.mapFile = mapFile;

        // Create and configure the camera for the game view
        camera = new OrthographicCamera();
        camera.zoom = 0.7f; // Removed manual zoom, let viewport handle it
        
        // Use ExtendViewport: (minWidth, minHeight, camera)
        // 640x360 guarantees a 16:9 view at least.
        // If window is bigger, it scales up.
        viewport = new ExtendViewport(640, 360, camera);

        // Get the font from the game's skin
        font = game.getSkin().getFont("font");
        
        shapeRenderer = new ShapeRenderer();
        hud = new HUD(game.getSpriteBatch(), this, game.getSkin());

        setupPauseMenu();
        setupLevel();
    }

    private void setupLevel() {
        // Load map
        // Ideally we should select levels, for now load level 1
        //FileHandle mapHandle = Gdx.files.internal("maps/level-1.properties");
        //if (!mapHandle.exists()) {
            // Try absolute path if internal fails (e.g. running from IDE root vs assets root)
            // However, map loader tries internal. Let's assume standard GDX internal structure.
            // If this fails, we might need to look in "assets/maps" or similar depending on working dir
        if(this.mapFile ==null || !this.mapFile.exists()){
            Gdx.app.error("GameScreen", "Map file is null or does not exist!");
            this.mapFile = Gdx.files.internal("maps/level-1.properties");
        }



        mapObjects = MapLoader.loadMap(this.mapFile);

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

        character = new Character(spawnX+16, spawnY);
    }

    private void setupPauseMenu() {
        pauseStage = new Stage(new FitViewport(1920, 1080), game.getSpriteBatch());
        
        pauseMenu = new PauseMenu(game, 
            () -> togglePause(), // Resume action
            null // Exit action (default)
        );
        pauseStage.addActor(pauseMenu);
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


    // Screen interface methods with necessary functionality
    @Override
    public void render(float delta) {
        // Check for pause key press to go back to the menu
        if (Gdx.input.isKeyJustPressed(game.getConfigManager().getKey("PAUSE"))) {
            togglePause();
        }

        ScreenUtils.clear(0, 0, 0, 1); // Clear the screen

        // Logic update
        if (!isPaused) {
            if (character != null) {
                character.update(delta, mapObjects, game.getConfigManager());

                // Camera follow character with smooth lerp
                float targetX = character.getPosition().x + 8; // Center of 16 width
                float targetY = character.getPosition().y + 16; // Center of 32 height
                
                // Lerp factor (adjust for smoothness, 5f is typical)
                float lerpSpeed = 5f;
                camera.position.x += (targetX - camera.position.x) * lerpSpeed * delta;
                camera.position.y += (targetY - camera.position.y) * lerpSpeed * delta;
                
                camera.update(); 
                // Note: viewport.apply() sets the projection matrix of the camera effectively
            }
            if(mapObjects != null){
                mapObjects.removeIf(GameObject::isMarkedForRemoval);
            }
            if (character.isLevelCompleted()) {
                game.goToMenu(); // 调用主类的切换屏幕方法
                return; // 直接结束当前帧，避免后续不必要的渲染
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
            if (character.isDamaged()) {
                game.getSpriteBatch().setColor(1, 0, 0, 1); // Red tint
            }

            game.getSpriteBatch().draw(
                    character.getTextureRegion(),
                    character.getPosition().x,
                    character.getPosition().y,
                    character.getWidth(),
                    character.getHeight()
            );

            game.getSpriteBatch().setColor(1, 1, 1, 1); // Reset color
            
            // Draw Navigation Arrow
            character.drawArrow(game.getSpriteBatch());
        }

        game.getSpriteBatch().end(); // Important to call this after drawing everything
        
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
            shapeRenderer.end();
        }

        // Draw pause menu if paused
        if (isPaused) {
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
