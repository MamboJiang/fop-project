package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MapLoader;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.List;

/**
 * Screen for selecting a level to play.
 */
public class LevelSelectionScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;

    private ScrollPane scrollPane;

    public LevelSelectionScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1920, 1080), game.getSpriteBatch());

        Table table = new Table();
        // table.setFillParent(true); // Don't fill parent if using ScrollPane, or use container
        
        // Title
        Table container = new Table();
        container.setFillParent(true);
        stage.addActor(container);
        
        container.add(new Label("Select Level", game.getSkin(), "title")).padBottom(20).row();

        // Level Buttons
        Table levelsTable = new Table();
        List<FileHandle> mapFiles = MapLoader.getMapFiles();
        
        if (mapFiles.isEmpty()) {
            levelsTable.add(new Label("No maps found!", game.getSkin())).row();
        } else {
            for (final FileHandle mapFile : mapFiles) {
                String mapName = mapFile.nameWithoutExtension();
                TextButton levelButton = new TextButton(mapName, game.getSkin());
                levelButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        game.goToGame(mapFile);
                    }
                });
                levelsTable.add(levelButton).width(300).padBottom(10).row();
            }
        }

        // ScrollPane for levels
        scrollPane = new ScrollPane(levelsTable, game.getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // Disable horizontal scrolling
        container.add(scrollPane).width(400).height(400).padBottom(20).row();

        // Back Button
        TextButton backButton = new TextButton("Back", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        container.add(backButton).width(200);
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
        // Enable scroll focus
        if (scrollPane != null) {
            stage.setScrollFocus(scrollPane);
        }
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
