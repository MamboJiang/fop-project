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
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class CinematicScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    
    // Data
    private CinematicData cinematicData;
    private int frameIndex = -1;

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

    public CinematicScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new FitViewport(1920, 1080), game.getSpriteBatch());

        loadData();
        setupUI();
        nextFrame(); // Start first frame
    }
    
    private void loadData() {
        Json json = new Json();
        FileHandle file = Gdx.files.internal("story/data/opening.json");
        if (file.exists()) {
            cinematicData = json.fromJson(CinematicData.class, file);
        } else {
            Gdx.app.error("CinematicScreen", "opening.json not found!");
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
        blueRect.setSize(800, 500); // Set size for origin calculation
        blueRect.setOrigin(Align.center);
        blueRect.setScale(0); // Start hidden
        blueRect.setVisible(false);

        // Story Image
        storyImage = new Image();
        storyImage.setSize(800, 500);
        storyImage.setScaling(Scaling.fit); // Maintain aspect ratio
        storyImage.setOrigin(Align.center);
        storyImage.setColor(1, 1, 1, 0); // Transparent
        storyImage.setVisible(false);

        // Stack them
        // Fixed size for the frame: 800x500 roughly
        Stack stack = new Stack();
        
        // Use a simple Group. Stack will size Group to 800x500.
        // Group will NOT resize blueRect. We control blueRect manually.
        Group blueRectGroup = new Group(); 
        blueRectGroup.addActor(blueRect);
        
        stack.add(blueRectGroup);
        stack.add(storyImage);

        // Center the stack
        centerTable.add(stack).size(800, 500).center();


        // 2. Bottom Bar
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        stage.addActor(uiTable);
        uiTable.bottom();

        // Gradient
        Pixmap pix = new Pixmap(1, 500, Pixmap.Format.RGBA8888);
        for (int y = 0; y < 500; y++) {
            float alpha = 1.0f - ((float)y / 500f);
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
        dialogueText = new Label("", game.getSkin());
        dialogueText.setAlignment(Align.center);
        dialogueText.setWrap(true);
        dialogueText.setFontScale(1.2f);
        
        // Text
        Label.LabelStyle labelStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), com.badlogic.gdx.graphics.Color.WHITE);
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
                Actions.moveBy(0, 10, 0.5f, Interpolation.sine)
            )
        ));
    }

    private void playArrowFeedback() {
        arrowImage.clearActions();
        arrowImage.addAction(Actions.sequence(
            Actions.scaleTo(1.5f, 1.5f, 0.05f),
            Actions.scaleTo(1f, 1f, 0.05f),
            Actions.run(this::startBobbing)
        ));
    }
    
    private void createArrowTexture() {
        // Draw a white triangle V
        Pixmap p = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        p.setColor(1, 1, 1, 1);
        // Line 1: 0,0 to 16,32
        // Line 2: 32,0 to 16,32
        for (int i = 0; i < 4; i++) {
             p.drawLine(4+i, 4, 16, 28-i);
             p.drawLine(28-i, 4, 16, 28-i);
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
            // Force layout update so the container has the correct size/pos before animation
            bottomContainer.layout(); 
            textContainer.setOrigin(Align.center);
            
            // Use single SwingOut for smooth overshoot (pop and settle)
            textContainer.addAction(Actions.scaleTo(1.0f, 1.0f, 0.15f, Interpolation.swingOut));
        });

        // Image Logic
        if (frame.isShowImage()) {
            // Always replay animation
            blueRect.setVisible(true);
            
            // Random rotation: +/- [3, 10] degrees
            float randomAngle = MathUtils.randomSign() * MathUtils.random(3f, 5f);
            blueRect.setRotation(randomAngle); 
            
            blueRect.setScale(0f);
            
            // NO Overshoot for Rect (One time in place) -> exp5Out
            blueRect.clearActions();
            blueRect.addAction(Actions.scaleTo(1.0f, 1.0f, 0.2f, Interpolation.exp5Out)); // 0.2s Fast
            
            // Load Image
            if (frame.getImage() != null) {
                if (storyTexture != null) storyTexture.dispose();
                try {
                    storyTexture = new Texture(Gdx.files.internal(frame.getImage()));
                    storyImage.setDrawable(new TextureRegionDrawable(storyTexture));
                    
                    // Match blueRect size to the fitted image size
                    Vector2 fittedSize = Scaling.fit.apply(storyTexture.getWidth(), storyTexture.getHeight(), 800, 500);
                    float w = fittedSize.x;
                    float h = fittedSize.y;
                    
                    blueRect.setSize(w, h);
                    
                    // Manual centering in the 800x500 group
                    blueRect.setPosition((800 - w) / 2f, (500 - h) / 2f);
                    
                    blueRect.setOrigin(Align.center);
                    
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
                Actions.fadeIn(0.1f)
            ));
        } else {
            blueRect.setVisible(false);
            storyImage.setVisible(false);
        }
    }

    private void finish() {
        game.setScreen(new de.tum.cit.fop.maze.GameScreen(game, Gdx.files.internal("maps/level-0.properties")));
        dispose();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            // Simulate click on arrow for feedback
            playArrowFeedback();
            nextFrame();
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    
    @Override
    public void dispose() {
        stage.dispose();
        if (gradientTexture != null) gradientTexture.dispose();
        if (storyTexture != null) storyTexture.dispose();
        if (arrowTexture != null) arrowTexture.dispose();
        if (blueTexture != null) blueTexture.dispose();
    }
}
