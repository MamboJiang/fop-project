package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.math.Interpolation;
import de.tum.cit.fop.maze.MazeRunnerGame;
import java.util.Map;

/**
 * Screen for viewing encyclopedia entries.
 * Displays unlocked characters, enemies, and lore.
 */
public class EncyclopediaScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final Skin skin;
    private final EncyclopediaManager encyclopediaManager;


    private Texture backgroundTexture;
    private Image backgroundImage1;
    private Image backgroundImage2;
    private float scrollSpeed = 25f;

    private Texture blackTexture;
    private Image cinematicBarTop;
    private Image cinematicBarBottom;
    private static final float CINEMATIC_RATIO = 0.125f;

    private Texture titleBgTexture;
    private Texture cardBgTexture;

    private Texture detailPanelTexture;


    private Label.LabelStyle titleStyle;
    private Label.LabelStyle bodyStyle;
    private Color themeColor = Color.valueOf("6699CC");

    /**
     * Constructor for EncyclopediaScreen.
     * @param game The main game instance.
     */
    public EncyclopediaScreen(MazeRunnerGame game) {
        this.game = game;
        this.skin = game.getSkin();
        this.encyclopediaManager = EncyclopediaManager.getInstance();

        this.stage = new Stage(new ExtendViewport(1920, 1080), game.getSpriteBatch());

        loadAssets();
        setupStyles();
        buildUI();


        if (this.encyclopediaManager.getAllEntries().containsKey("main_character")) {
            this.encyclopediaManager.getAllEntries().get("main_character").setName(game.getPlayerState().getUsername());
        }
        this.encyclopediaManager.updateEntriesContent(game.getPlayerState());
    }

    /**
     * Loads textures and other assets required for the screen.
     */
    private void loadAssets() {
        backgroundTexture = new Texture(Gdx.files.internal("selfmade/background.png"));
        backgroundImage1 = new Image(backgroundTexture);
        backgroundImage2 = new Image(backgroundTexture);
        backgroundImage1.setScaling(Scaling.stretch);
        backgroundImage2.setScaling(Scaling.stretch);
        detailPanelTexture = new Texture(Gdx.files.internal("selfmade/uielements/levelbuttonbase.png"));

        backgroundImage1.setPosition(StoryMenu.savedBackgroundX, 0);
        backgroundImage2.setPosition(StoryMenu.savedBackgroundX + stage.getWidth(), 0);

        stage.addActor(backgroundImage1);
        stage.addActor(backgroundImage2);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        blackTexture = new Texture(pixmap);

        cinematicBarTop = new Image(blackTexture);
        cinematicBarBottom = new Image(blackTexture);
        stage.addActor(cinematicBarTop);
        stage.addActor(cinematicBarBottom);

        titleBgTexture = new Texture(Gdx.files.internal("selfmade/uielements/buttontype2.png"));

        Pixmap p2 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p2.setColor(0, 0, 0, 0.5f);
        p2.fill();
        cardBgTexture = new Texture(p2);
        p2.dispose();
        pixmap.dispose();
    }

    /**
     * Initializes label styles and fonts.
     */
    private void setupStyles() {
        titleStyle = new Label.LabelStyle(skin.getFont("hoefler"), themeColor);
        bodyStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.WHITE);
    }

    /**
     * Constructs the main UI layout, including the title, card grid, and back button.
     */
    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table titleTable = new Table();
        titleTable.setBackground(new TextureRegionDrawable(titleBgTexture));

        Label titleLabel = new Label("Encyclopedia", titleStyle);
        titleLabel.setFontScale(1.2f);
        titleTable.add(titleLabel).padBottom(10);

        root.add(titleTable).padTop(80).padBottom(20).row();

        Table contentTable = new Table();
        contentTable.top();

        Map<String, EncyclopediaEntry> entries = encyclopediaManager.getAllEntries();
        int columns = 0;

        for (EncyclopediaEntry entry : entries.values()) {
            boolean isUnlocked = game.getPlayerState().getDiscoveredEncyclopediaIds().contains(entry.getId());

            Table card = createEntryCard(entry, isUnlocked);

            contentTable.add(card).width(320).height(380).pad(20);

            columns++;
            if (columns >= 3) {
                contentTable.row();
                columns = 0;
            }
        }

        ScrollPane scrollPane = new ScrollPane(contentTable);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, true);

        root.add(scrollPane).expand().fill().pad(20).padBottom(40).row();

        TextButton backButton = createHoverButton("Back to Menu", "short");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        root.add(backButton).bottom().padBottom(100);
    }

    /**
     * Creates a hoverable button with consistent style.
     */
    private TextButton createHoverButton(String text, String styleName) {
        final TextButton button = new TextButton(text, skin, styleName);
        button.setTransform(true);
        button.setOrigin(Align.center);

        button.getLabel().setStyle(new Label.LabelStyle(skin.getFont("hoefler"), themeColor));

        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (pointer == -1) {
                    button.clearActions();
                    button.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f, Interpolation.smooth));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer == -1) {
                    button.clearActions();
                    button.addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f, Interpolation.smooth));
                }
            }
        });
        return button;
    }

    /**
     * Creates a card for an encyclopedia entry.
     * @param entry The entry to display.
     * @param isUnlocked Whether the entry is unlocked.
     * @return The table representing the card.
     */
    private Table createEntryCard(EncyclopediaEntry entry, boolean isUnlocked) {
        Table card = new Table();
        card.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);

        card.setBackground(new TextureRegionDrawable(cardBgTexture));

        Image icon;
        Texture texture;
        try {
            texture = new Texture(Gdx.files.internal(entry.getTexturePath()));
        } catch (Exception e) {
            Pixmap p = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
            p.setColor(Color.RED);
            p.fill();
            texture = new Texture(p);
            p.dispose();
        }
        icon = new Image(texture);
        icon.setScaling(Scaling.fit);

        if (!isUnlocked) {
            icon.setColor(Color.BLACK);
        }

        String nameText = isUnlocked ? entry.getName() : "???";
        Label nameLabel = new Label(nameText, bodyStyle);
        nameLabel.setAlignment(Align.center);
        if (!isUnlocked)
            nameLabel.setColor(Color.GRAY);

        card.add(icon).size(128, 128).pad(20).row();
        card.add(nameLabel).padBottom(20).row();

        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isUnlocked) {
                    showDetailDialog(entry);
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (isUnlocked)
                    card.addAction(Actions.scaleTo(1.05f, 1.05f, 0.1f));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (isUnlocked)
                    card.addAction(Actions.scaleTo(1f, 1f, 0.1f));
            }
        });

        card.setTransform(true);
        card.setOrigin(Align.center);
        return card;
    }

    /**
     * Shows a modal dialog with details about the encyclopedia entry.
     * @param entry The entry to show details for.
     */
    private void showDetailDialog(EncyclopediaEntry entry) {
        Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
            }
        };

        dialog.setBackground(new TextureRegionDrawable(detailPanelTexture));

        dialog.setMovable(false);
        dialog.setModal(true);

        Table content = dialog.getContentTable();
        content.pad(50);

        Label titleLabel = new Label(entry.getName(), titleStyle);
        titleLabel.setFontScale(1.5f);
        content.add(titleLabel).padBottom(30).row();

        try {
            Texture bigTex = new Texture(Gdx.files.internal(entry.getTexturePath()));
            Image bigImage = new Image(bigTex);
            bigImage.setScaling(Scaling.fit);
            content.add(bigImage).size(400, 400).padBottom(30).row();
        } catch (Exception e) {
        }

        Label descLabel = new Label(entry.getDescription(), bodyStyle);
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.center);
        descLabel.setFontScale(1.1f);
        content.add(descLabel).width(700).padBottom(40).row();

        TextButton closeBtn = createHoverButton("Close", "short");
        dialog.button(closeBtn, true);
        dialog.getButtonTable().getCell(closeBtn).width(200).height(60).padBottom(20);

        dialog.show(stage);
    }

    /**
     * Renders the screen.
     * @param delta Time delta.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateBackground(delta);

        stage.act(delta);
        stage.draw();
    }

    /**
     * Updates the scrolling background position.
     * @param delta Time delta.
     */
    private void updateBackground(float delta) {
        if (backgroundImage1 == null || backgroundImage2 == null)
            return;


        backgroundImage1.setX(backgroundImage1.getX() - scrollSpeed * delta);
        backgroundImage2.setX(backgroundImage2.getX() - scrollSpeed * delta);

        float w = backgroundImage1.getWidth();

        float currentX = backgroundImage1.getX() % w;
        if (currentX > 0)
            currentX -= w;
        StoryMenu.savedBackgroundX = currentX;


        if (backgroundImage1.getX() + w <= 0) {
            backgroundImage1.setX(backgroundImage2.getX() + w);
        }
        if (backgroundImage2.getX() + w <= 0) {
            backgroundImage2.setX(backgroundImage1.getX() + w);
        }
    }

    /**
     * Resizes the viewport.
     * @param width New width.
     * @param height New height.
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        float stageW = stage.getWidth();
        float stageH = stage.getHeight();


        if (backgroundImage1 != null)
            backgroundImage1.setSize(stageW, stageH);
        if (backgroundImage2 != null)
            backgroundImage2.setSize(stageW, stageH);


        if (cinematicBarTop != null && cinematicBarBottom != null) {
            float barHeight = stageH * CINEMATIC_RATIO;
            cinematicBarBottom.setSize(stageW, barHeight);
            cinematicBarBottom.setPosition(0, 0);

            cinematicBarTop.setSize(stageW, barHeight);
            cinematicBarTop.setPosition(0, stageH - barHeight);
        }
    }

    /**
     * Called when the screen becomes the current screen.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Called when the screen is hidden.
     */
    @Override
    public void hide() {
    }

    /**
     * Called when application is paused.
     */
    @Override
    public void pause() {
    }

    /**
     * Called when application is resumed.
     */
    @Override
    public void resume() {
    }

    /**
     * Disposes of the screen assets.
     */
    @Override
    public void dispose() {
        stage.dispose();
        if (backgroundTexture != null)
            backgroundTexture.dispose();
        if (blackTexture != null)
            blackTexture.dispose();
        if (titleBgTexture != null)
            titleBgTexture.dispose();
        if (cardBgTexture != null)
            cardBgTexture.dispose();
    }
}