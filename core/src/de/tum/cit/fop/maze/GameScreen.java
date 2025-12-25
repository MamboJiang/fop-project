package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.files.FileHandle;
import de.tum.cit.fop.maze.GameObj.GameObject;
import de.tum.cit.fop.maze.MapLoader;
import java.util.List;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private final BitmapFont font;
    
    // Game Objects
    private List<GameObject> gameObjects;

    private float sinusInput = 0f;
    
    // Pause State and UI
    private boolean isPaused = false;
    private Stage pauseStage;
    private Texture pauseBackground;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     * @param mapFile The map file to load.
     */
    public GameScreen(MazeRunnerGame game, FileHandle mapFile) {
        this.game = game;
        
        // Load map
        this.gameObjects = MapLoader.loadMap(mapFile);
        
        // Create and configure the camera for the game view
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = 0.5f; // Zoom in a bit more for 16px tiles

        // Get the font from the game's skin
        font = game.getSkin().getFont("font");
        
        setupPauseMenu();
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
        if (isPaused) {
            Gdx.input.setInputProcessor(pauseStage);
        } else {
            Gdx.input.setInputProcessor(null); // Or the game input processor if one exists
        }
    }


    // Screen interface methods with necessary functionality
    @Override
    public void render(float delta) {
        // Check for pause key press to go back to the menu
        if (Gdx.input.isKeyJustPressed(game.getConfigManager().getKey("PAUSE"))) {
            togglePause();
        }

        ScreenUtils.clear(0, 0, 0, 1); // Clear the screen

        camera.update(); // Update the camera
        
        // Set up and begin drawing with the sprite batch
        game.getSpriteBatch().setProjectionMatrix(camera.combined);

        // Render game world
        game.getSpriteBatch().begin(); // Important to call this before drawing anything
        
        for (GameObject obj : gameObjects) {
             game.getSpriteBatch().draw(
                 obj.getTextureRegion(),
                 obj.getPosition().x,
                 obj.getPosition().y,
                 obj.getWidth(),
                 obj.getHeight()
             );
        }

        game.getSpriteBatch().end(); // Important to call this after drawing everything
        
        // Draw pause menu if paused
        if (isPaused) {
            pauseStage.act(delta);
            pauseStage.draw();
        }
    }


    public void resize(int width, int height) {
        camera.setToOrtho(false);
        pauseStage.getViewport().update(width, height, true);
    }


    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null); // Clear any input processor from previous screen
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (pauseBackground != null) pauseBackground.dispose();
        if (pauseStage != null) pauseStage.dispose();
    }

    // Additional methods and logic can be added as needed for the game screen
}
