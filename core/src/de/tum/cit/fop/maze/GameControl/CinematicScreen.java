package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import de.tum.cit.fop.maze.Conversation.DialogueManager;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class CinematicScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;


    private CinematicData cinematicData;
    private int frameIndex = -1;
    private String storyPath;
    private String currentImagePath = null;
    private Runnable onFinish;
    private boolean finished = false;


    private Container<Label> textContainer;
    private Label dialogueText;
    private Image blueRect;
    private Image storyImage;
    private Image arrowImage;

    private Texture gradientTexture;
    private Texture storyTexture;
    private Texture arrowTexture;
    private Texture blueTexture;

    private Table bottomContainer;


    private DialogueManager dialogueManager;

    /**
     * Default Constructor.
     * 
     * @param game The main game class.
     */
    public CinematicScreen(MazeRunnerGame game) {
        this(game, "story/data/opening.json", () -> {
            game.setScreen(new de.tum.cit.fop.maze.GameScreen(game, Gdx.files.internal("maps/level-0.properties")));
        });
    }

    /**
     * Constructor with custom story path and callback.
     * 
     * @param game      The main game class.
     * @param storyPath Path to the JSON story file.
     * @param onFinish  Runnable to execute when cinematic finishes.
     */
    public CinematicScreen(MazeRunnerGame game, String storyPath, Runnable onFinish) {
        this.game = game;
        this.storyPath = storyPath;
        this.onFinish = onFinish;
        this.stage = new Stage(new ExtendViewport(1920, 1080), game.getSpriteBatch());


        this.dialogueManager = new DialogueManager(game.getSkin(), game.getPlayerState());
        this.dialogueManager.setBackgroundScrimVisible(false);

        loadData();
        setupUI();
    }

    /**
     * Loads the cinematic data from the JSON file.
     */
    private void loadData() {
        Json json = new Json();
        FileHandle file = Gdx.files.internal(storyPath);
        if (file.exists()) {
            cinematicData = json.fromJson(CinematicData.class, file);
        } else {
            Gdx.app.error("CinematicScreen", storyPath + " not found!");
            cinematicData = new CinematicData();
        }
    }

    /**
     * Sets up the UI elements including images and dialogues.
     */
    private void setupUI() {

        Table centerTable = new Table();
        centerTable.setFillParent(true);
        stage.addActor(centerTable);


        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(0.0f, 0.4f, 0.8f, 1f);
        p.fill();
        blueTexture = new Texture(p);
        p.dispose();

        blueRect = new Image(blueTexture);
        blueRect.setSize(1500, 900);
        blueRect.setOrigin(Align.center);
        blueRect.setScale(0);
        blueRect.setVisible(false);


        storyImage = new Image();
        storyImage.setSize(1500, 900);
        storyImage.setScaling(Scaling.fit);
        storyImage.setOrigin(Align.center);
        storyImage.setColor(1, 1, 1, 0);
        storyImage.setVisible(false);


        Stack stack = new Stack();

        Group blueRectGroup = new Group();
        blueRectGroup.addActor(blueRect);

        stack.add(blueRectGroup);
        stack.add(storyImage);


        centerTable.add(stack).size(1500, 900).center().padBottom(250);


        Table uiTable = new Table();
        uiTable.setFillParent(true);
        stage.addActor(uiTable);
        uiTable.bottom();


        Pixmap pix = new Pixmap(1, 500, Pixmap.Format.RGBA8888);
        for (int y = 0; y < 500; y++) {
            float alpha = 1.0f - ((float) y / 500f);
            pix.setColor(0f, 0f, 0.4f, alpha * 0.9f);
            pix.drawPixel(0, 499 - y);
        }
        gradientTexture = new Texture(pix);
        pix.dispose();

        Table bottomContainer = new Table();
        this.bottomContainer = bottomContainer;
        bottomContainer.setBackground(new TextureRegionDrawable(gradientTexture));
        uiTable.add(bottomContainer).growX().height(450).bottom();


        Label.LabelStyle labelStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"),
                com.badlogic.gdx.graphics.Color.WHITE);
        dialogueText = new Label("", labelStyle);
        dialogueText.setAlignment(Align.center);
        dialogueText.setWrap(true);
        dialogueText.setFontScale(1.2f);


        textContainer = new Container<>(dialogueText);
        textContainer.setTransform(true);
        textContainer.setOrigin(Align.center);
        textContainer.fillX();

        bottomContainer.add(textContainer).growX().padLeft(100).padRight(100).padTop(50).row();


        createArrowTexture();
        arrowImage = new Image(arrowTexture);
        arrowImage.setOrigin(Align.center);

        startBobbing();

        bottomContainer.add(arrowImage).size(32, 24).padBottom(30).padTop(10);


        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                if (dialogueManager.isActive())
                    return;

                playArrowFeedback();
                nextFrame();
            }
        });
    }

    /**
     * Starts the bobbing animation for the arrow.
     */
    private void startBobbing() {
        arrowImage.clearActions();
        arrowImage.addAction(Actions.forever(
                Actions.sequence(
                        Actions.moveBy(0, -10, 0.5f, Interpolation.sine),
                        Actions.moveBy(0, 10, 0.5f, Interpolation.sine))));
    }

    /**
     * Plays a feedback animation when the arrow is clicked or activated.
     */
    private void playArrowFeedback() {
        arrowImage.clearActions();
        arrowImage.addAction(Actions.sequence(
                Actions.scaleTo(1.5f, 1.5f, 0.05f),
                Actions.scaleTo(1f, 1f, 0.05f),
                Actions.run(this::startBobbing)));
    }

    /**
     * Creates the arrow texture procedurally.
     */
    private void createArrowTexture() {

        Pixmap p = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        p.setColor(1, 1, 1, 1);
        for (int i = 0; i < 4; i++) {
            p.drawLine(4 + i, 4, 16, 28 - i);
            p.drawLine(28 - i, 4, 16, 28 - i);
        }
        arrowTexture = new Texture(p);
        p.dispose();
    }

    /**
     * Advances to the next frame in the cinematic.
     */
    private void nextFrame() {
        if (cinematicData == null || cinematicData.getFrames() == null) {
            finish();
            return;
        }

        frameIndex++;
        if (frameIndex >= cinematicData.getFrames().size()) {
            finish();
            return;
        }

        updateFrame(cinematicData.getFrames().get(frameIndex));
    }

    /**
     * Updates the UI to show the specified frame data.
     * @param frame The frame data to display.
     */
    private void updateFrame(CinematicData.CinematicFrame frame) {

        dialogueText.setText(frame.getText());
        textContainer.clearActions();
        textContainer.setScale(0f);


        Gdx.app.postRunnable(() -> {

            bottomContainer.layout();
            textContainer.setOrigin(Align.center);


            textContainer.addAction(Actions.scaleTo(1.0f, 1.0f, 0.15f, Interpolation.swingOut));
        });

        if (frame.isShowImage()) {
            String newImagePath = frame.getImage();


            if (newImagePath != null && newImagePath.equals(currentImagePath) && storyTexture != null) {

                blueRect.setVisible(true);
                blueRect.setScale(1.0f);
                blueRect.clearActions();

                storyImage.setVisible(true);
                storyImage.setColor(1, 1, 1, 1);
                storyImage.clearActions();


                if (newImagePath.endsWith("endstory.png")) {

                }


                if (frame.getText() != null && frame.getText().contains("###")) {
                    dialogueManager.loadDialogue("ending5");
                    dialogueManager.startDialogue();
                }

            } else {

                currentImagePath = newImagePath;


                blueRect.setVisible(true);

                float randomAngle = MathUtils.randomSign() * MathUtils.random(3f, 5f);
                blueRect.setRotation(randomAngle);

                blueRect.setScale(0f);

                blueRect.clearActions();
                blueRect.addAction(Actions.scaleTo(1.0f, 1.0f, 0.2f, Interpolation.swingOut));

                if (frame.getImage() != null) {
                    if (storyTexture != null)
                        storyTexture.dispose();
                    try {
                        storyTexture = new Texture(Gdx.files.internal(frame.getImage()));
                        storyImage.setDrawable(new TextureRegionDrawable(storyTexture));

                        Vector2 fittedSize = Scaling.fit.apply(storyTexture.getWidth(), storyTexture.getHeight(), 1500,
                                900);
                        float w = fittedSize.x;
                        float h = fittedSize.y;

                        blueRect.setSize(w, h);

                        blueRect.setPosition((1500 - w) / 2f, (900 - h) / 2f);

                        blueRect.setOrigin(Align.center);

                        String imgPath = frame.getImage();
                        if (imgPath.endsWith("endstory.png")) {
                            dialogueManager.loadDialogue("ending1");
                            dialogueManager.startDialogue();
                        } else if (imgPath.endsWith("endstorynono.png")) {
                            dialogueManager.loadDialogue("ending2");
                            dialogueManager.startDialogue();
                        } else if (imgPath.endsWith("endstory1.png")) {
                            dialogueManager.loadDialogue("ending3");
                            dialogueManager.startDialogue();
                        } else if (imgPath.endsWith("endstory3.png")) {
                            if (frame.getText() != null && frame.getText().contains("who took off the mask to atone")) {
                                dialogueManager.loadDialogue("ending5");
                                dialogueManager.startDialogue();
                            } else {
                                dialogueManager.loadDialogue("ending4");
                                dialogueManager.startDialogue();
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                storyImage.setVisible(true);
                storyImage.setColor(1, 1, 1, 0);
                storyImage.clearActions();
                storyImage.addAction(Actions.sequence(
                        Actions.delay(0.1f),
                        Actions.fadeIn(0.1f)));
            }
        } else {
            blueRect.setVisible(false);
            storyImage.setVisible(false);
            currentImagePath = null;
        }
    }

    /**
     * Finishes the cinematic and triggers the callback.
     */
    private void finish() {
        if (finished)
            return;
        finished = true;
        game.playTransition(() -> {
            if (onFinish != null) {
                onFinish.run();
            }
            dispose();
        });
    }

    private float inputDelayTimer = 0.5f;
    private boolean started = false;

    /**
     * Renders the cinematic screen.
     * @param delta Time delta.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!started) {
            started = true;
            nextFrame();
        }

        if (inputDelayTimer > 0) {
            inputDelayTimer -= delta;
        }

        stage.act(delta);
        stage.draw();

        if (dialogueManager.isActive()) {
            if (Gdx.input.getInputProcessor() != dialogueManager.getStage()) {
                Gdx.input.setInputProcessor(dialogueManager.getStage());
            }
            dialogueManager.render(delta);
        } else {
            if (Gdx.input.getInputProcessor() != stage) {
                Gdx.input.setInputProcessor(stage);
            }
            if (inputDelayTimer <= 0 && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                playArrowFeedback();
                nextFrame();
            }
        }

    }

    /**
     * Called when this screen becomes the current screen.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Resizes the viewport.
     * @param width New width.
     * @param height New height.
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (dialogueManager != null) {
            dialogueManager.resize(width, height);
        }
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
     * Called when screen is hidden.
     */
    @Override
    public void hide() {
    }

    /**
     * Disposes of assets.
     */
    @Override
    public void dispose() {
        stage.dispose();
        if (gradientTexture != null)
            gradientTexture.dispose();
        if (storyTexture != null)
            storyTexture.dispose();
        if (arrowTexture != null)
            arrowTexture.dispose();
        if (blueTexture != null)
            blueTexture.dispose();
        if (dialogueManager != null)
            dialogueManager.dispose();
    }
}
