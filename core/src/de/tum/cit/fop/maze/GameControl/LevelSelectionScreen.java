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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.graphics.Pixmap;

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
    private Table contentTable;

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
     * Constructor for LevelSelectionScreen.
     * 
     * @param game Main game instance.
     */
    public LevelSelectionScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new FitViewport(1920, 1080), game.getSpriteBatch());

        frameTexture = new Texture(Gdx.files.internal("selfmade/uielements/levelselect.png"));
        Image frameImage = new Image(frameTexture);
        backgroundTexture = new Texture(Gdx.files.internal("selfmade/background.png"));
        backgroundImage1 = new Image(backgroundTexture);
        backgroundImage2 = new Image(backgroundTexture);

        backgroundImage1.setScaling(Scaling.stretch);
        backgroundImage2.setScaling(Scaling.stretch);

        backgroundImage1.setSize(stage.getWidth(), stage.getHeight());
        backgroundImage2.setSize(stage.getWidth(), stage.getHeight());
        backgroundImage1.setPosition(StoryMenu.savedBackgroundX, 0);
        backgroundImage2.setPosition(StoryMenu.savedBackgroundX + stage.getWidth(), 0);

        stage.addActor(backgroundImage1);
        stage.addActor(backgroundImage2);


        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0.6f);
        p.fill();
        overlayTexture = new Texture(p);
        p.dispose();
        overlayImage = new Image(overlayTexture);
        overlayImage.setSize(stage.getWidth(), stage.getHeight());
        stage.addActor(overlayImage);

        Pixmap p2 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p2.setColor(Color.BLACK);
        p2.fill();
        blackTexture = new Texture(p2);
        p2.dispose();

        cinematicBarTop = new Image(blackTexture);
        cinematicBarBottom = new Image(blackTexture);

        stage.addActor(cinematicBarTop);
        stage.addActor(cinematicBarBottom);

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        Label.LabelStyle titleStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);
        Label titleLabel = new Label("Select Level", titleStyle);
        titleLabel.setFontScale(1.5f);
        rootTable.add(titleLabel).padBottom(50).row();

        contentTable = new Table();
        rootTable.add(contentTable);

        com.badlogic.gdx.scenes.scene2d.ui.Stack stack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();

        Table frameTable = new Table();
        frameTable.add(frameImage);
        stack.add(frameTable);



        Table levelsTable = new Table();
        List<FileHandle> mapFiles = MapLoader.getMapFiles();
        java.util.Collections.sort(mapFiles, new java.util.Comparator<FileHandle>() {
            @Override
            public int compare(FileHandle o1, FileHandle o2) {
                return o1.name().compareTo(o2.name());
            }
        });

        Label.LabelStyle bodyStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);

        if (mapFiles.isEmpty()) {
            levelsTable.add(new Label("No maps found!", bodyStyle));
        } else {
            for (int i = 0; i < mapFiles.size(); i++) {
                final FileHandle mapFile = mapFiles.get(i);

                boolean isUnlocked = false;
                if (i == 0) {
                    isUnlocked = true;
                } else {
                    FileHandle prevMap = mapFiles.get(i - 1);
                    String prevLevelName = prevMap.nameWithoutExtension();
                    if (game.getPlayerState().isLevelCompleted(prevLevelName)) {
                        isUnlocked = true;
                    }
                }

                String displayName;
                if (isUnlocked) {
                    displayName = convertToRoman(i + 1);
                } else {
                    displayName = "???";
                }

                TextButton levelButton = new TextButton(displayName, game.getSkin(), "level");

                if (!isUnlocked) {
                    levelButton.setDisabled(true);
                    levelButton.setColor(1, 1, 1, 0.5f);
                    levelButton.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
                }

                if (isUnlocked) {
                    levelButton.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            game.goToGame(mapFile);
                        }
                    });

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
                        }
                    });
                } else {

                    levelButton.addListener(new ClickListener() {
                        @Override
                        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                            super.enter(event, x, y, pointer, fromActor);
                            levelNameLabel.setText("Locked");
                        }

                        @Override
                        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                            super.exit(event, x, y, pointer, toActor);
                        }
                    });
                }

                levelsTable.add(levelButton).pad(15);
            }
        }

        ScrollPane scrollPane = new ScrollPane(levelsTable);

        scrollPane.setScrollingDisabled(false, true);


        Table scrollContainer = new Table();
        scrollContainer.add(scrollPane).width(1200).height(200);
        stack.add(scrollContainer);

        contentTable.add(stack).padBottom(20).row();


        levelNameLabel = new Label("", bodyStyle);
        levelNameLabel.setAlignment(Align.center);
        contentTable.add(levelNameLabel).padBottom(30).minHeight(40).row();


        TextButton backButton = new TextButton("Back", game.getSkin(), "short");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        contentTable.add(backButton);
    }

    /**
     * Updates the level name label.
     * 
     * @param index    Level index (1-based).
     * @param filename Filename of the map.
     */
    private void updateLabel(int index, String filename) {
        String customName = getLevelName(index);
        String display = "Level " + index + " - " + customName;
        levelNameLabel.setText(display);
    }

    /**
     * Gets a display name for a level index.
     * 
     * @param index Level index.
     * @return Name of the level.
     */
    private String getLevelName(int index) {
        switch (index) {
            case 1:
                return "The Awakening";
            case 2:
                return "Ancient Ruins";
            case 3:
                return "The Dark Forest";
            case 4:
                return "Crystal Caves";
            case 5:
                return "Volcanic Depths";
            default:
                return "Unknown Territory";
        }
    }

    /**
     * Converts integer to Roman numeral.
     * 
     * @param n Number (1-10).
     * @return Roman numeral string.
     */
    private String convertToRoman(int n) {
        String[] roman = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X" };
        if (n > 0 && n <= 10)
            return roman[n - 1];
        return String.valueOf(n);
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

        float w = backgroundImage1.getWidth();
        float currentX = backgroundImage1.getX() % w;
        if (currentX > 0)
            currentX -= w;
        StoryMenu.savedBackgroundX = currentX;

        float width = backgroundImage1.getWidth();

        if (backgroundImage1.getX() + width <= 0) {
            backgroundImage1.setX(backgroundImage2.getX() + width);
        }

        if (backgroundImage2.getX() + width <= 0) {
            backgroundImage2.setX(backgroundImage1.getX() + width);
        }
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

        if (contentTable != null) {
            contentTable.clearActions();
            contentTable.addAction(Actions.sequence(
                    Actions.moveBy(0, -stage.getHeight()),
                    Actions.moveBy(0, stage.getHeight(), 0.3f, Interpolation.exp5Out)));
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
        if (frameTexture != null)
            frameTexture.dispose();
        if (frameTexture != null)
            frameTexture.dispose();
        if (backgroundTexture != null)
            backgroundTexture.dispose();
        if (overlayTexture != null)
            overlayTexture.dispose();
        if (blackTexture != null)
            blackTexture.dispose();
    }
}
