package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.MazeRunnerGame;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.graphics.Pixmap;

/**
 * Screen for game settings including volume and key bindings.
 */
public class SettingsScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private boolean isWaitingForKey = false;
    private String actionToRebind = null;
    private TextButton activeRebindButton = null;

    // Background Fields
    private Texture backgroundTexture;
    private Image backgroundImage1;
    private Image backgroundImage2;
    private Texture overlayTexture;
    private Image overlayImage;
    private Image cinematicBarTop;
    private Image cinematicBarBottom;
    private Texture blackTexture;
    private float scrollSpeed = 25f;
    private static final float CINEMATIC_RATIO = 0.125f;

    /**
     * Constructor for SettingsScreen.
     * 
     * @param game Main game instance.
     */
    public SettingsScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1920, 1080), game.getSpriteBatch());

        // Background Setup
        backgroundTexture = new Texture(Gdx.files.internal("selfmade/background.png"));
        backgroundImage1 = new Image(backgroundTexture);
        backgroundImage2 = new Image(backgroundTexture);

        backgroundImage1.setScaling(Scaling.stretch);
        backgroundImage2.setScaling(Scaling.stretch);

        // Use saved position from StoryMenu
        backgroundImage1.setSize(stage.getWidth(), stage.getHeight());
        backgroundImage2.setSize(stage.getWidth(), stage.getHeight());
        backgroundImage1.setPosition(StoryMenu.savedBackgroundX, 0);
        backgroundImage2.setPosition(StoryMenu.savedBackgroundX + stage.getWidth(), 0);

        stage.addActor(backgroundImage1);
        stage.addActor(backgroundImage2);

        // Overlay Setup (Black)
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0.6f);
        p.fill();
        overlayTexture = new Texture(p);
        p.dispose();
        overlayImage = new Image(overlayTexture);
        overlayImage.setSize(stage.getWidth(), stage.getHeight());
        stage.addActor(overlayImage);

        // Cinematic Bars
        Pixmap p2 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p2.setColor(Color.BLACK);
        p2.fill();
        blackTexture = new Texture(p2);
        p2.dispose();

        cinematicBarTop = new Image(blackTexture);
        cinematicBarBottom = new Image(blackTexture);
        stage.addActor(cinematicBarTop);
        stage.addActor(cinematicBarBottom);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        Skin skin = game.getSkin();

        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.WHITE);
        Label.LabelStyle bodyStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.WHITE);

        Label titleLabel = new Label("Settings", titleStyle);
        titleLabel.setFontScale(1.5f); // Make title larger
        table.add(titleLabel).padBottom(20).colspan(2).row();

        table.add(new Label("Music Volume:", bodyStyle)).right().padRight(10);
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

        table.add(new Label("Sound Volume:", bodyStyle)).right().padRight(10);
        final Slider soundSlider = new Slider(0, 1, 0.1f, false, skin);
        soundSlider.setValue(game.getConfigManager().getSoundVolume());
        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getConfigManager().setSoundVolume(soundSlider.getValue());

            }
        });
        table.add(soundSlider).width(200).padBottom(20).row();

        table.add(new Label("Key Bindings", bodyStyle)).colspan(2).padBottom(10).row();

        addKeyBindingRow(table, skin, "Move Up", "UP");
        addKeyBindingRow(table, skin, "Move Down", "DOWN");
        addKeyBindingRow(table, skin, "Move Left", "LEFT");
        addKeyBindingRow(table, skin, "Move Right", "RIGHT");
        addKeyBindingRow(table, skin, "Pause/Menu", "PAUSE");
        addKeyBindingRow(table, skin, "Open Console", "CONSOLE");

        TextButton backButton = new TextButton("Back", skin, "short");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu(false);
            }
        });
        table.add(backButton).colspan(2).padTop(20);

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

    /**
     * Helper to add a key binding row.
     * 
     * @param table      Target table.
     * @param skin       UI Skin.
     * @param labelText  Label for action.
     * @param actionName Internal action name.
     */
    private void addKeyBindingRow(Table table, Skin skin, String labelText, final String actionName) {
        Label.LabelStyle bodyStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.WHITE);
        table.add(new Label(labelText + ":", bodyStyle)).right().padRight(10);

        String currentKey = Input.Keys.toString(game.getConfigManager().getKey(actionName));
        final TextButton keyButton = new TextButton(currentKey, skin, "keybinding");

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

        table.add(keyButton).padBottom(5).row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateBackground(delta);

        stage.act(delta);
        stage.draw();
    }

    private void updateBackground(float delta) {
        if (backgroundImage1 == null || backgroundImage2 == null)
            return;

        backgroundImage1.setX(backgroundImage1.getX() - scrollSpeed * delta);
        backgroundImage2.setX(backgroundImage2.getX() - scrollSpeed * delta);

        // Normalize saved position to ensure consistency across screens
        float w = backgroundImage1.getWidth();
        float currentX = backgroundImage1.getX() % w;
        if (currentX > 0)
            currentX -= w;
        StoryMenu.savedBackgroundX = currentX;

        float width = backgroundImage1.getWidth();
        if (backgroundImage1.getX() + width <= 0)
            backgroundImage1.setX(backgroundImage2.getX() + width);
        if (backgroundImage2.getX() + width <= 0)
            backgroundImage2.setX(backgroundImage1.getX() + width);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        float stageW = stage.getWidth();
        float stageH = stage.getHeight();

        if (backgroundImage1 != null)
            backgroundImage1.setSize(stageW, stageH);
        if (backgroundImage2 != null)
            backgroundImage2.setSize(stageW, stageH);
        if (overlayImage != null)
            overlayImage.setSize(stageW, stageH);

        if (cinematicBarTop != null && cinematicBarBottom != null) {
            float barHeight = stageH * CINEMATIC_RATIO;
            cinematicBarBottom.setSize(stageW, barHeight);
            cinematicBarBottom.setPosition(0, 0);
            cinematicBarTop.setSize(stageW, barHeight);
            cinematicBarTop.setPosition(0, stageH - barHeight);
        }
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
        if (backgroundTexture != null)
            backgroundTexture.dispose();
        if (overlayTexture != null)
            overlayTexture.dispose();
        if (blackTexture != null)
            blackTexture.dispose();
    }
}
