package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MapLoader;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.List;

/**
 * Screen for selecting a level to play.
 * Redesigned for horizontal layout with custom frame.
 */
public class LevelSelectionScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private Label levelNameLabel;
    private Texture frameTexture;

    /**
     * Constructor for LevelSelectionScreen.
     * @param game Main game instance.
     */
    public LevelSelectionScreen(MazeRunnerGame game) {
        this.game = game;
        // Use FitViewport to maintain aspect ratio
        this.stage = new Stage(new FitViewport(1920, 1080), game.getSpriteBatch());

        // Load Frame Texture
        frameTexture = new Texture(Gdx.files.internal("selfmade/uielements/levelselect.png"));
        Image frameImage = new Image(frameTexture);

        // Root Table
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // Title
        rootTable.add(new Label("Select Level", game.getSkin(), "title")).padBottom(50).row();

        // Container implementation: Stack (Frame + ScrollPane) doesn't work well with Tables naturally, 
        // but we can use a Table with a background or just layer them if we use a Stack.
        // However, precise positioning is easier if we just center the frame and put buttons inside/over it.
        // The user says "Frame's image is levelselect.png".
        
        // Let's use a Stack or just a Container Table with background
        com.badlogic.gdx.scenes.scene2d.ui.Stack stack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        
        // Layer 1: Frame centered
        Table frameTable = new Table();
        frameTable.add(frameImage);
        stack.add(frameTable);
        
        // Layer 2: Buttons
        // We need the buttons to be aligned with the "holes" in the frame if it has any, 
        // but user just said "horizontal layout".
        // I will create a ScrollPane that sits centrally.
        
        Table levelsTable = new Table();
        List<FileHandle> mapFiles = MapLoader.getMapFiles();
        
        if (mapFiles.isEmpty()) {
            levelsTable.add(new Label("No maps found!", game.getSkin()));
        } else {
            for (int i = 0; i < mapFiles.size(); i++) {
                final FileHandle mapFile = mapFiles.get(i);
                // Level Number (Roman numerals in image, but standard is fine unless requested)
                // Use the Roman numeral logic if I can, but simple numbers are safer.
                String displayName = convertToRoman(i + 1);
                
                TextButton levelButton = new TextButton(displayName, game.getSkin(), "level");
                
                // Button Logic
                levelButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (mapFile.nameWithoutExtension().equalsIgnoreCase("level-1")) {
                            game.goToStory(mapFile);
                        } else {
                            game.goToGame(mapFile);
                        }
                    }
                });
                
                // Hover Logic for Label
                final int levelIndex = i + 1;
                final String mapName = mapFile.nameWithoutExtension(); 
                
                levelButton.addListener(new ClickListener() {
                   @Override
                   public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                       super.enter(event, x, y, pointer, fromActor);
                       updateLabel(levelIndex, mapName);
                   }
                   
                   @Override
                   public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                       super.exit(event, x, y, pointer, toActor);
                       // Optional: Clear label or keep last selected?
                       // "Keep last" is usually better UX, or clear.
                       // User says "放在哪一关...显示LevelX...". Implies dynamic show.
                       // I'll leave it as is or clear if desired. 
                       // Let's clear it to "Select a Level" or empty?
                       // User said "without name it shows - blank".
                       // I will strictly follow "hover updates it".
                   }
                });
                
                levelsTable.add(levelButton).pad(15); // Horizontal spacing
            }
        }

        ScrollPane scrollPane = new ScrollPane(levelsTable, game.getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, true); // Allow horizontal, disable vertical
        
        // Add ScrollPane to Stack (Layer 2)
        // We might need to pad it to fit inside the frame visuals.
        // Assuming frame is a border around the buttons.
        Table scrollContainer = new Table();
        scrollContainer.add(scrollPane).width(1200).height(200); // Adjust width/height to fit frame
        stack.add(scrollContainer);
        
        rootTable.add(stack).padBottom(20).row();
        
        // Level Name Label
        levelNameLabel = new Label("", game.getSkin());
        levelNameLabel.setAlignment(Align.center);
        rootTable.add(levelNameLabel).padBottom(30).minHeight(40).row();
        
        // Back Button
        TextButton backButton = new TextButton("Back", game.getSkin(), "short");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        rootTable.add(backButton);
    }
    
    private void updateLabel(int index, String filename) {
        // "Level X - [Custom Name]"
        String customName = getLevelName(index);
        String display = "Level " + index + " - " + customName;
        levelNameLabel.setText(display);
    }

    private String getLevelName(int index) {
        switch (index) {
            case 1: return "The Awakening";
            case 2: return "Ancient Ruins";
            case 3: return "The Dark Forest";
            case 4: return "Crystal Caves";
            case 5: return "Volcanic Depths";
            default: return "Unknown Territory";
        }
    }

    private String convertToRoman(int n) {
        // Simple 1-10 converter
        String[] roman = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        if (n > 0 && n <= 10) return roman[n-1];
        return String.valueOf(n);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override 
    public void dispose() {
        stage.dispose();
        if (frameTexture != null) frameTexture.dispose();
    }
}
