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
import de.tum.cit.fop.maze.GameObj.Character;
import de.tum.cit.fop.maze.GameObj.EntryPoint;
import de.tum.cit.fop.maze.GameObj.GameObject;
import de.tum.cit.fop.maze.GameControl.HUD;

import java.util.List;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private final BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private HUD hud;
    
    // Debug
    private boolean debugEnabled = false;

    // Pause State and UI
    private boolean isPaused = false;
    private Stage pauseStage;
    private Texture pauseBackground;

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
        camera.setToOrtho(false);
        camera.zoom = 0.5f;

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
        pauseStage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        // Create semi-transparent background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.7f); // 70% transparent black
        pixmap.fill();
        pauseBackground = new Texture(pixmap);
        pixmap.dispose();

        Image backgroundImage = new Image(pauseBackground);
        backgroundImage.setFillParent(true);
        pauseStage.addActor(backgroundImage);

        // Table for buttons
        Table table = new Table();
        table.setFillParent(true);
        pauseStage.addActor(table);

        // Resume Button
        TextButton resumeButton = new TextButton("Resume", game.getSkin());
        table.add(resumeButton).width(300).padBottom(20).row();
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                togglePause();
            }
        });

        // Menu Button
        TextButton menuButton = new TextButton("Exit to Menu", game.getSkin());
        table.add(menuButton).width(300).row();
        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
    }

    /**
     * Toggles the pause state and handles input processor switching.
     */
    private void togglePause() {
        isPaused = !isPaused;
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

                // Camera follow character
                camera.position.set(character.getPosition().x, character.getPosition().y, 0);
                camera.update();
            }
        }

        // Render
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
        camera.setToOrtho(false);
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
        if (pauseBackground != null) pauseBackground.dispose();
        if (pauseStage != null) pauseStage.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (hud != null) hud.dispose();
    }
}
