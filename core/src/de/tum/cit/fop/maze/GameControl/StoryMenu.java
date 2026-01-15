package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class StoryMenu implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    
    // State
    private int state = 0; // 0=Chaos, 1=Boss, 2=Menu
    private boolean isTransitioning = false;
    
    // UI Elements
    // UI Elements
    private Table introTable; // Holds Boss + Dialogue Text (No Background)
    private Table menuTable;  // Holds Buttons
    private Image gradientBg; // Static Background
    
    private Image bossImage;
    private Image arrowImage;
    private Table dialogueContainer;
    
    // Labels
    private Label speakerLabel;
    private Label textLabel;
    private com.badlogic.gdx.scenes.scene2d.ui.Container<Label> textAnimContainer;
    
    private Texture bossTexture;
    private Texture gradientTexture;
    private Texture arrowTexture;
    
    public StoryMenu(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(1920, 1080), game.getSpriteBatch());
        
        setupUI();
    }
    
    private void setupUI() {
        // 0. Static Background Layer (Gradient)
        Pixmap pix = new Pixmap(1, 500, Pixmap.Format.RGBA8888);
        for (int y = 0; y < 500; y++) {
            float alpha = 1.0f - ((float)y / 500f);
            pix.setColor(0f, 0f, 0.4f, alpha * 0.9f); // Dark Blue
            pix.drawPixel(0, 499 - y);
        }
        gradientTexture = new Texture(pix);
        pix.dispose();
        
        gradientBg = new Image(gradientTexture);
        gradientBg.setFillParent(false);
        gradientBg.setSize(stage.getWidth(), 500); // Fixed height, width will update in resize
        stage.addActor(gradientBg); 
        
        // 1. Intro Section (Left/Center content) - Transparent container for Boss & Text
        introTable = new Table();
        introTable.setFillParent(false); 
        introTable.setSize(stage.getWidth(), stage.getHeight());
        stage.addActor(introTable);
        
        // Boss Image (Initially Hidden)
        bossTexture = new Texture(Gdx.files.internal("player/lihui/bosssit.PNG"));
        bossImage = new Image(bossTexture);
        bossImage.setScaling(Scaling.fit);
        bossImage.setColor(1, 1, 1, 0); // Alpha 0
        
        // Pad top to push lower (Relative)
        introTable.add(bossImage).grow().padTop(com.badlogic.gdx.scenes.scene2d.ui.Value.percentHeight(0.2f, introTable)).row();
        
        // Defer scaling to ensure origin is correct (after layout)
        Gdx.app.postRunnable(() -> {
            introTable.layout(); 
            bossImage.setOrigin(Align.center);
            bossImage.setScale(2f); // Magnify a bit
        });
        
        // Dialogue Container (Text Only, No BG)
        dialogueContainer = new Table();
        
        // Dialogue Content
        Table textTable = new Table();
        
        Label.LabelStyle nameStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);
        Label.LabelStyle textStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);
        
        speakerLabel = new Label("- ??? -", nameStyle);
        speakerLabel.setColor(0, 1, 1, 0); // Hidden initially
        speakerLabel.setFontScale(1.5f);
        
        textLabel = new Label("", textStyle);
        textLabel.setWrap(true);
        textLabel.setFontScale(1.2f);
        textLabel.setAlignment(Align.center);
        
        // Wrap for Animation
        textAnimContainer = new com.badlogic.gdx.scenes.scene2d.ui.Container<>(textLabel);
        textAnimContainer.setTransform(true); // Enable transform for scaling
        textAnimContainer.fillX();
        
        textTable.add(speakerLabel).padBottom(20).row();
        textTable.add(textAnimContainer).growX().padBottom(20).row();
        
        // Initial Text
        animateText("You are in a chaos\nPress [space] to continue");
        
        // Arrow
        createArrowTexture();
        arrowImage = new Image(arrowTexture);
        startBobbing();
        textTable.add(arrowImage).size(32, 24).bottom();
        
        dialogueContainer.add(textTable).grow().pad(50);
        
        introTable.add(dialogueContainer).growX().height(400).bottom();
        
        // 2. Menu Section (Hidden off-screen initially)
        menuTable = new Table();
        menuTable.setSize(stage.getWidth() * 0.5f, stage.getHeight());
        menuTable.setPosition(stage.getWidth(), 0); // Start off-screen right
        stage.addActor(menuTable);
        
        // Buttons
        addMenuButtons();
        
        // Input Logic
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleInput();
            }
        });
    }
    
    private void animateText(String text) {
        textLabel.setText(text);
        
        // Clear previous animations/scale
        textAnimContainer.clearActions();
        textAnimContainer.setScale(0f);
        
        // Defer layout/origin calc to ensure correct center fit
        Gdx.app.postRunnable(() -> {
             // Force layout update on parent containers
             if (introTable != null) introTable.layout();
             if (dialogueContainer != null) dialogueContainer.layout();
             
             // Now set origin based on actual size
             textAnimContainer.setOrigin(Align.center);
             
             // Pop animation
             textAnimContainer.addAction(Actions.scaleTo(1.0f, 1.0f, 0.25f, Interpolation.swingOut));
        });
    }
    
    private void handleInput() {
        if (isTransitioning) return;
        
        if (state == 0) {
            // Chaos -> Boss
             playArrowFeedback();
             state = 1;
             
             // Fade in Boss
             bossImage.addAction(Actions.fadeIn(1.0f));
             
             // Change Text with Animation
             animateText("Welcome... to the Maze.");
             
             speakerLabel.setColor(0, 1, 1, 1); // Show Speaker
             speakerLabel.setVisible(true);
             
        } else if (state == 1) {
            // Boss -> Menu
            playArrowFeedback();
            state = 2;
            transitionToMenu();
        }
    }
    
    private void addMenuButtons() {
        Table btnContainer = new Table();
        
        TextButton newGameBtn = new TextButton("New Game", game.getSkin());
        newGameBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new SaveSlotScreen(game, false));
            }
        });
        
        TextButton loadGameBtn = new TextButton("Load Game", game.getSkin());
        loadGameBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new SaveSlotScreen(game, true));
            }
        });
        
        TextButton backBtn = new TextButton("Back", game.getSkin(), "short");
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        
        btnContainer.add(newGameBtn).width(400).height(80).padBottom(20).row();
        btnContainer.add(loadGameBtn).width(400).height(80).padBottom(20).row();
        btnContainer.add(backBtn).padTop(20);
        
        // Center in the menu table (which will be right half)
        menuTable.add(btnContainer).center();
    }
    
    private void transitionToMenu() {
        isTransitioning = true;
        // State is already 2 from handleInput
        
        // Hide Arrow
        arrowImage.clearActions();
        arrowImage.addAction(Actions.fadeOut(0.2f));
        
        // 1. Animate IntroTable (Compress to Left 50%)
        // We act on size (width) -> Layout will update content center
        introTable.addAction(Actions.sizeTo(stage.getWidth() * 0.5f, stage.getHeight(), 0.8f, Interpolation.pow2Out));
        
        // 2. Animate MenuTable (Slide in from Right, take 50%)
        menuTable.setVisible(true);
        menuTable.setPosition(stage.getWidth(), 0);
        menuTable.setSize(stage.getWidth() * 0.5f, stage.getHeight()); // Ensure size
        
        menuTable.addAction(Actions.moveTo(stage.getWidth() * 0.5f, 0, 0.8f, Interpolation.pow2Out));
        
        // Wait for animation to finish transitioning state if needed
        stage.addAction(Actions.delay(0.8f, Actions.run(() -> isTransitioning = false)));
    }
    
    private void createArrowTexture() {
        Pixmap p = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        p.setColor(1, 1, 1, 1);
        for (int i = 0; i < 4; i++) {
             p.drawLine(4+i, 4, 16, 28-i);
             p.drawLine(28-i, 4, 16, 28-i);
        }
        arrowTexture = new Texture(p);
        p.dispose();
    }
    
    private void startBobbing() {
        arrowImage.clearActions();
        arrowImage.addAction(Actions.forever(
            Actions.sequence(
                Actions.moveBy(0, -5, 0.5f, Interpolation.sine),
                Actions.moveBy(0, 5, 0.5f, Interpolation.sine)
            )
        ));
    }
    
    private void playArrowFeedback() {
        // If in menu state (2), arrow is hidden anyway
        if (state == 2) return;
        arrowImage.clearActions();
        arrowImage.addAction(Actions.sequence(
            Actions.scaleTo(1.5f, 1.5f, 0.05f),
            Actions.scaleTo(1f, 1f, 0.05f),
            Actions.run(this::startBobbing)
        ));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
             handleInput();
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
        
        if (gradientBg != null) {
            gradientBg.setSize(stage.getWidth(), 500);
        }
        
        // Update origin for correct scaling after resize
        if (bossImage != null) {
             // Force layout to ensure correct size before setting origin
             introTable.invalidate();
             introTable.validate(); // Computes layout immediately
             bossImage.setOrigin(Align.center);
        }
        
        // Update Table sizes if not animating
        // If Intro/Chaos (State < 2): Full Width, Menu Offscreen
        // If Menu (State 2): Split 50/50
        
        if (!isTransitioning) {
            if (state < 2) {
                introTable.setSize(stage.getWidth(), stage.getHeight());
                if (menuTable != null) {
                    menuTable.setSize(stage.getWidth() * 0.5f, stage.getHeight());
                    menuTable.setPosition(stage.getWidth(), 0);
                }
            } else {
                 introTable.setSize(stage.getWidth() * 0.5f, stage.getHeight());
                 if (menuTable != null) {
                    menuTable.setSize(stage.getWidth() * 0.5f, stage.getHeight());
                    menuTable.setPosition(stage.getWidth() * 0.5f, 0);
                 }
            }
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        if (bossTexture != null) bossTexture.dispose();
        if (gradientTexture != null) gradientTexture.dispose();
        if (arrowTexture != null) arrowTexture.dispose();
    }
}
