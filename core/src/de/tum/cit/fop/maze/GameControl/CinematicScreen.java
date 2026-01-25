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

    // Data
    private CinematicData cinematicData;
    private int frameIndex = -1;
    private String storyPath;
    private String currentImagePath = null;
    private Runnable onFinish;
    private boolean finished = false;

    // UI
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

    // Dialogue Integration
    private DialogueManager dialogueManager;

    public CinematicScreen(MazeRunnerGame game) {
        this(game, "story/data/opening.json", () -> {
            game.setScreen(new de.tum.cit.fop.maze.GameScreen(game, Gdx.files.internal("maps/level-0.properties")));
        });
    }

    public CinematicScreen(MazeRunnerGame game, String storyPath, Runnable onFinish) {
        this.game = game;
        this.storyPath = storyPath;
        this.onFinish = onFinish;
        this.stage = new Stage(new ExtendViewport(1920, 1080), game.getSpriteBatch());

        // Initialize DialogueManager
        this.dialogueManager = new DialogueManager(game.getSkin(), game.getPlayerState());
        this.dialogueManager.setBackgroundScrimVisible(false); // No dark overlay for cinematic

        loadData();
        setupUI();
    }

    private void loadData() {
        Json json = new Json();
        FileHandle file = Gdx.files.internal(storyPath);
        if (file.exists()) {
            cinematicData = json.fromJson(CinematicData.class, file);
        } else {
            Gdx.app.error("CinematicScreen", storyPath + " not found!");
            cinematicData = new CinematicData(); // Empty
        }
    }

    private void setupUI() {
        // 1. Center Content (Stack)
        Table centerTable = new Table();
        centerTable.setFillParent(true);
        stage.addActor(centerTable);

        // Blue Rect
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(0.0f, 0.4f, 0.8f, 1f);
        p.fill();
        blueTexture = new Texture(p);
        p.dispose();

        blueRect = new Image(blueTexture);
        blueRect.setSize(1500, 900); // Set size for origin calculation
        blueRect.setOrigin(Align.center);
        blueRect.setScale(0); // Start hidden
        blueRect.setVisible(false);

        // Story Image
        storyImage = new Image();
        storyImage.setSize(1500, 900);
        storyImage.setScaling(Scaling.fit); // Maintain aspect ratio
        storyImage.setOrigin(Align.center);
        storyImage.setColor(1, 1, 1, 0); // Transparent
        storyImage.setVisible(false);

        // Stack them
        Stack stack = new Stack();

        Group blueRectGroup = new Group();
        blueRectGroup.addActor(blueRect);

        stack.add(blueRectGroup);
        stack.add(storyImage);

        // Center the stack
        centerTable.add(stack).size(1500, 900).center().padBottom(250);

        // 2. Bottom Bar
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        stage.addActor(uiTable);
        uiTable.bottom();

        // Gradient
        Pixmap pix = new Pixmap(1, 500, Pixmap.Format.RGBA8888);
        for (int y = 0; y < 500; y++) {
            float alpha = 1.0f - ((float) y / 500f);
            pix.setColor(0f, 0f, 0.4f, alpha * 0.9f); // Dark Blue
            pix.drawPixel(0, 499 - y);
        }
        gradientTexture = new Texture(pix);
        pix.dispose();

        Table bottomContainer = new Table();
        this.bottomContainer = bottomContainer; // Assign to field
        bottomContainer.setBackground(new TextureRegionDrawable(gradientTexture));
        uiTable.add(bottomContainer).growX().height(450).bottom();

        // Text
        Label.LabelStyle labelStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"),
                com.badlogic.gdx.graphics.Color.WHITE);
        dialogueText = new Label("", labelStyle);
        dialogueText.setAlignment(Align.center);
        dialogueText.setWrap(true);
        dialogueText.setFontScale(1.2f);

        // Wrap in Container to enable actor-based scaling animation
        textContainer = new Container<>(dialogueText);
        textContainer.setTransform(true);
        textContainer.setOrigin(Align.center);
        textContainer.fillX();

        bottomContainer.add(textContainer).growX().padLeft(100).padRight(100).padTop(50).row();

        // Arrow
        createArrowTexture();
        arrowImage = new Image(arrowTexture);
        arrowImage.setOrigin(Align.center);

        // Initial bobbing
        startBobbing();

        bottomContainer.add(arrowImage).size(32, 24).padBottom(30).padTop(10);

        // Input
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // If Dialogue is active, do NOT advance frame from here.
                // DialogueManager handles its own input.
                if (dialogueManager.isActive())
                    return;

                playArrowFeedback();
                nextFrame();
            }
        });
    }

    private void startBobbing() {
        arrowImage.clearActions();
        arrowImage.addAction(Actions.forever(
                Actions.sequence(
                        Actions.moveBy(0, -10, 0.5f, Interpolation.sine),
                        Actions.moveBy(0, 10, 0.5f, Interpolation.sine))));
    }

    private void playArrowFeedback() {
        arrowImage.clearActions();
        arrowImage.addAction(Actions.sequence(
                Actions.scaleTo(1.5f, 1.5f, 0.05f),
                Actions.scaleTo(1f, 1f, 0.05f),
                Actions.run(this::startBobbing)));
    }

    private void createArrowTexture() {
        // Draw a white triangle V
        Pixmap p = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        p.setColor(1, 1, 1, 1);
        for (int i = 0; i < 4; i++) {
            p.drawLine(4 + i, 4, 16, 28 - i);
            p.drawLine(28 - i, 4, 16, 28 - i);
        }
        arrowTexture = new Texture(p);
        p.dispose();
    }

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

    private void updateFrame(CinematicData.CinematicFrame frame) {
        // Text Animation: Pop big then back (Overshoot)
        dialogueText.setText(frame.getText());
        textContainer.clearActions();
        textContainer.setScale(0f); // Start hidden immediately

        // Defer layout calculation to ensure Label wrapping is processed by Stage
        Gdx.app.postRunnable(() -> {
            // Force layout update so the container has the correct size/pos before
            // animation
            bottomContainer.layout();
            textContainer.setOrigin(Align.center);

            // Use single SwingOut for smooth overshoot (pop and settle)
            textContainer.addAction(Actions.scaleTo(1.0f, 1.0f, 0.15f, Interpolation.swingOut));
        });

        // Image Logic
        if (frame.isShowImage()) {
            String newImagePath = frame.getImage();

            // Check if image is same as previous
            if (newImagePath != null && newImagePath.equals(currentImagePath) && storyTexture != null) {
                // SKIP Animation - Keep everything visible
                blueRect.setVisible(true);
                blueRect.setScale(1.0f); // Ensure scale is 1
                blueRect.clearActions();

                storyImage.setVisible(true);
                storyImage.setColor(1, 1, 1, 1); // Ensure full alpha
                storyImage.clearActions();

                // Still check for Dialogue Triggers (in case same image triggers different
                // dialogue? Unlikely but safe)
                if (newImagePath.endsWith("endstory.png")) {
                    // Check if not active? Actually better to not re-trigger if already running?
                    // No, let updateFrame trigger checks logic.
                    // But if it's the SAME image, usually we don't want to re-trigger START
                    // dialogue if it's the same sequence.
                    // User said: "If same image, don't pop up".
                    // Implies we probably shouldn't restart dialogue either if it's the exact same
                    // file.
                    // But user specifically asked about IMAGE pop up.
                    // Let's stick to IMAGE logic.
                }

                // Wait, if next frame has SAME image but DIFFERENT text, updateFrame is called.
                // Check for Ending 5 Trigger (Specific Text)
                if (frame.getText() != null && frame.getText().contains("###")) {
                    dialogueManager.loadDialogue("ending5");
                    dialogueManager.startDialogue();
                }

            } else {
                // NEW Image or First Image
                currentImagePath = newImagePath;

                // Always replay animation
                blueRect.setVisible(true);

                // Random rotation: +/- [3, 10] degrees
                float randomAngle = MathUtils.randomSign() * MathUtils.random(3f, 5f);
                blueRect.setRotation(randomAngle);

                blueRect.setScale(0f);

                // NO Overshoot for Rect (One time in place) -> exp5Out
                blueRect.clearActions();
                blueRect.addAction(Actions.scaleTo(1.0f, 1.0f, 0.2f, Interpolation.swingOut)); // 0.2s Fast

                // Load Image
                if (frame.getImage() != null) {
                    if (storyTexture != null)
                        storyTexture.dispose();
                    try {
                        storyTexture = new Texture(Gdx.files.internal(frame.getImage()));
                        storyImage.setDrawable(new TextureRegionDrawable(storyTexture));

                        // Match blueRect size to the fitted image size
                        Vector2 fittedSize = Scaling.fit.apply(storyTexture.getWidth(), storyTexture.getHeight(), 1500,
                                900);
                        float w = fittedSize.x;
                        float h = fittedSize.y;

                        blueRect.setSize(w, h);

                        // Manual centering in the 1500x900 group
                        blueRect.setPosition((1500 - w) / 2f, (900 - h) / 2f);

                        blueRect.setOrigin(Align.center);

                        // Trigger Dialogue based on Image Name (User Request)
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
                                // Only start ending4 if valid and NOT ending5
                                dialogueManager.loadDialogue("ending4");
                                dialogueManager.startDialogue();
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                storyImage.setVisible(true);
                storyImage.setColor(1, 1, 1, 0); // Transparent
                storyImage.clearActions();
                // Wait for Rect (0.2s) then fade in fast (0.1s)
                storyImage.addAction(Actions.sequence(
                        Actions.delay(0.1f),
                        Actions.fadeIn(0.1f)));
            }
        } else {
            blueRect.setVisible(false);
            storyImage.setVisible(false);
            currentImagePath = null; // Reset if image hidden
        }
    }

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

        // 1. Draw Cinematic Stage (Background) FIRST
        stage.act(delta);
        stage.draw();

        // 2. Handle Input & Draw Dialogue (Foreground)
        if (dialogueManager.isActive()) {
            if (Gdx.input.getInputProcessor() != dialogueManager.getStage()) {
                Gdx.input.setInputProcessor(dialogueManager.getStage());
            }
            // Update Dialogue
            dialogueManager.render(delta);
        } else {
            if (Gdx.input.getInputProcessor() != stage) {
                Gdx.input.setInputProcessor(stage);
            }
            // Handle Cinematic Input (Space)
            if (inputDelayTimer <= 0 && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                // Simulate click on arrow for feedback
                playArrowFeedback();
                nextFrame();
            }
        }

        // Draw Dialogue on top if active (Called by dialogueManager.render usually, but
        // render calls stage.draw inside)
        // DialogueManager.render() calls stage.draw(). So we don't need to call it
        // again here.
        // Wait, DialogueManager.render() clears screen?
        // Checking DialogueManager.java: No, it just calls stage.act and stage.draw.
        // Screen clear is done here in CinematicScreen.render.
    }

    @Override
    public void show() {
        // Initial input processor
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (dialogueManager != null) {
            dialogueManager.resize(width, height);
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

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
