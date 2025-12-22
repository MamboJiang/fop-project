package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Map;

/**
 * Screen for game settings including volume and key bindings.
 */
public class SettingsScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private boolean isWaitingForKey = false;
    private String actionToRebind = null;
    private TextButton activeRebindButton = null;

    public SettingsScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        Skin skin = game.getSkin();

        // Title
        table.add(new Label("Settings", skin, "title")).padBottom(20).colspan(2).row();

        // Music Volume
        table.add(new Label("Music Volume:", skin)).right().padRight(10);
        final Slider musicSlider = new Slider(0, 1, 0.1f, false, skin);
        musicSlider.setValue(game.getConfigManager().getMusicVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getConfigManager().setMusicVolume(musicSlider.getValue());
                game.updateMusicVolume();
            }
        });
        table.add(musicSlider).width(200).row();

        // Sound Volume
        table.add(new Label("Sound Volume:", skin)).right().padRight(10);
        final Slider soundSlider = new Slider(0, 1, 0.1f, false, skin);
        soundSlider.setValue(game.getConfigManager().getSoundVolume());
        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getConfigManager().setSoundVolume(soundSlider.getValue());
                // Test sound could be played here
            }
        });
        table.add(soundSlider).width(200).padBottom(20).row();

        // Key Bindings
        table.add(new Label("Key Bindings", skin)).colspan(2).padBottom(10).row();
        
        addKeyBindingRow(table, skin, "Move Up", "UP");
        addKeyBindingRow(table, skin, "Move Down", "DOWN");
        addKeyBindingRow(table, skin, "Move Left", "LEFT");
        addKeyBindingRow(table, skin, "Move Right", "RIGHT");
        addKeyBindingRow(table, skin, "Pause/Menu", "PAUSE");

        // Back Button
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        table.add(backButton).colspan(2).padTop(20).width(200);
        
        // Input processor for capturing keys
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (isWaitingForKey && actionToRebind != null) {
                    game.getConfigManager().setKey(actionToRebind, keycode);
                    activeRebindButton.setText(Input.Keys.toString(keycode));
                    isWaitingForKey = false;
                    actionToRebind = null;
                    activeRebindButton = null;
                    return true;
                }
                return false;
            }
        });
    }
    
    private void addKeyBindingRow(Table table, Skin skin, String labelText, final String actionName) {
        table.add(new Label(labelText + ":", skin)).right().padRight(10);
        
        String currentKey = Input.Keys.toString(game.getConfigManager().getKey(actionName));
        final TextButton keyButton = new TextButton(currentKey, skin);
        
        keyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!isWaitingForKey) {
                    isWaitingForKey = true;
                    actionToRebind = actionName;
                    activeRebindButton = keyButton;
                    keyButton.setText("Press any key...");
                }
            }
        });
        
        table.add(keyButton).width(150).padBottom(5).row();
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
