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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
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
    private Table bossTable;      // Layer 1: Boss Image
    private Table textLayerTable; // Layer 3: Dialogue Text
    private Table menuTable;      // Layer 4: Buttons
    private Image gradientBg;     // Layer 2: Static Gradient
    
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

    private Texture backgroundTexture;
    private Image backgroundImage1;
    private Image backgroundImage2;
    private float scrollSpeed = 25f;

    private Image cinematicBarTop;
    private Image cinematicBarBottom;
    private Texture blackTexture;
    private static final float CINEMATIC_RATIO = 0.125f;
    
    private boolean isGameMenu = false;

    public StoryMenu(MazeRunnerGame game) {
        this(game, false);
    }

    public StoryMenu(MazeRunnerGame game, boolean isGameMenu) {
        this.game = game;
        this.isGameMenu = isGameMenu;
        this.stage = new Stage(new ExtendViewport(1920, 1080)); // 1080p Target
        
        setupUI();
    }
    
    private void setupUI() {
        // Gradient (Static at bottom)
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
        gradientBg.setSize(stage.getWidth(), 500); // Fixed height

        backgroundTexture = new Texture(Gdx.files.internal("selfmade/background.png"));

        backgroundImage1 = new Image(backgroundTexture);
        backgroundImage2 = new Image(backgroundTexture);

        backgroundImage1.setScaling(Scaling.stretch);
        backgroundImage1.setSize(stage.getWidth(), stage.getHeight());

        backgroundImage2.setScaling(Scaling.stretch);
        backgroundImage2.setSize(stage.getWidth(), stage.getHeight());

        backgroundImage1.setPosition(0, 0);
        backgroundImage2.setPosition(stage.getWidth(), 0);


        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        blackTexture = new Texture(pixmap);
        pixmap.dispose();

        cinematicBarTop = new Image(blackTexture);
        cinematicBarBottom = new Image(blackTexture);
        // ---------------------------------------------------------
        // Layer 1: Boss Table (Behind Gradient)
        // ---------------------------------------------------------
        bossTable = new Table();
        bossTable.setFillParent(false);
        bossTable.bottom().left(); // Align content?
        
        // Layer 3: Text Layer (In front of Gradient)
        textLayerTable = new Table();
        textLayerTable.setFillParent(false);
        textLayerTable.bottom().left();
        
        float initialWidth = isGameMenu ? stage.getWidth() * 0.5f : stage.getWidth();
        bossTable.setSize(initialWidth, stage.getHeight());
        textLayerTable.setSize(initialWidth, stage.getHeight());

        // Add to stage in correct order
        stage.addActor(backgroundImage1);
        stage.addActor(backgroundImage2);
        stage.addActor(bossTable);
        stage.addActor(cinematicBarBottom);
        stage.addActor(cinematicBarTop);// Bottom
        stage.addActor(gradientBg);     // Middle
        stage.addActor(textLayerTable); // Top of Left Section
        
        
        // --- Boss Content ---
        bossTexture = new Texture(Gdx.files.internal("player/lihui/bosssit.PNG"));
        bossImage = new Image(bossTexture);
        bossImage.setScaling(Scaling.fit);
        
        if (isGameMenu) {
            bossImage.setColor(1, 1, 1, 1);
            state = 3; 
        } else {
            bossImage.setColor(1, 1, 1, 0);
            state = 0;
        }
        
        // Boss positioned in bossTable
        bossTable.add(bossImage).grow().padTop(com.badlogic.gdx.scenes.scene2d.ui.Value.percentHeight(0.2f, bossTable));
        
        // Defer scaling (Origin fix)
        Gdx.app.postRunnable(() -> {
            bossTable.layout(); 
            bossImage.setOrigin(Align.center);
            bossImage.setScale(1.3f); 
        });
        
        // --- Text Content ---
        dialogueContainer = new Table();
        Table textTable = new Table();
        
        Label.LabelStyle nameStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);
        Label.LabelStyle textStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);
        
        speakerLabel = new Label("- ??? -", nameStyle);
        speakerLabel.setColor(0, 1, 1, isGameMenu ? 1 : 0);
        speakerLabel.setFontScale(1.5f);
        speakerLabel.setAlignment(Align.center);
        
        textLabel = new Label("", textStyle);
        textLabel.setWrap(true);
        textLabel.setFontScale(1.2f);
        textLabel.setAlignment(Align.center);
        
        textAnimContainer = new com.badlogic.gdx.scenes.scene2d.ui.Container<>(textLabel);
        textAnimContainer.setTransform(true);
        textAnimContainer.fillX();
        
        textTable.add(speakerLabel).expandX().padBottom(20).row();
        textTable.add(textAnimContainer).growX().padBottom(20).row();
        
        // Initial Text
        if (isGameMenu) {
             animateText("Welcome back to the Maze.");
        } else {
             animateText("You are in a chaos\nPress [space] to continue");
        }
        
        // Arrow
        createArrowTexture();
        arrowImage = new Image(arrowTexture);
        arrowImage.setOrigin(Align.center);
        arrowImage.setSize(32, 24);
        arrowImage.setPosition(0, 0);
        
        Group arrowGroup = new Group();
        arrowGroup.setSize(32, 24);
        arrowGroup.addActor(arrowImage);
        
        if (!isGameMenu) {
            startBobbing();
        }
        
        Stack stack = new Stack();
        stack.add(textTable);
        
        if (!isGameMenu) {
            Table arrowTable = new Table();
            arrowTable.add(arrowGroup).size(32, 24).expand().bottom().padBottom(10);
            stack.add(arrowTable);
        }
        
        dialogueContainer.add(stack).grow().pad(50);
        
        // Add dialogue container to Text Layer
        textLayerTable.add(dialogueContainer).growX().height(400).bottom();
        
        
        // ---------------------------------------------------------
        // Layer 4: Menu Table (Right Side / Offscreen)
        // ---------------------------------------------------------
        menuTable = new Table();
        if (isGameMenu) {
            menuTable.setSize(stage.getWidth() * 0.5f, stage.getHeight());
            menuTable.setPosition(stage.getWidth() * 0.5f, 0);
            addGameMenuButtons();
        } else {
            menuTable.setSize(stage.getWidth() * 0.5f, stage.getHeight());
            menuTable.setPosition(stage.getWidth(), 0); 
            addMenuButtons();
        }
        stage.addActor(menuTable);
        
        // Input Logic
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleInput();
            }
        });
    }
    
    private TextButton createHoverButton(String text, Skin skin, String styleName) {
        final TextButton button = new TextButton(text, skin, styleName);
        button.setTransform(true);
        button.setOrigin(Align.center);
        
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
    
    private void animateText(String text) {
        textLabel.setText(text);
        
        textAnimContainer.clearActions();
        textAnimContainer.setScale(0f);
        
        Gdx.app.postRunnable(() -> {
             if (bossTable != null) bossTable.layout();
             if (textLayerTable != null) textLayerTable.layout();
             if (dialogueContainer != null) dialogueContainer.layout();
             
             textAnimContainer.setOrigin(Align.center);
             textAnimContainer.addAction(Actions.scaleTo(1.0f, 1.0f, 0.25f, Interpolation.swingOut));
        });
    }
    
    private void handleInput() {
        if (isTransitioning) return;
        if (state == 3) return;
        
        if (state == 0) {
             playArrowFeedback();
             state = 1;
             bossImage.addAction(Actions.fadeIn(1.0f));
             animateText("Welcome... to the Maze.");
             speakerLabel.setColor(0, 1, 1, 1);
             speakerLabel.setVisible(true);
             
        } else if (state == 1) {
            playArrowFeedback();
            state = 2;
            transitionToMenu();
        } else if (state == STATE_PRE_CINEMATIC) {
            game.setScreen(new CinematicScreen(game));
        }
    }
    
    private void addGameMenuButtons() {
        Table btnContainer = new Table();
        Skin skin = game.getSkin();

        TextButton selectLevelBtn = createHoverButton("Select Level", skin, "middle");
        selectLevelBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToLevelSelect();
            }
        });
        
        TextButton endlessBtn = createHoverButton("Endless Mode", skin, "middle");
        endlessBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean unlocked = !game.getPlayerState().getCompletedLevels().isEmpty();
                if (unlocked) {
                    de.tum.cit.fop.maze.GameObj.PlayerState state = game.getPlayerState();
                    boolean hasRun = state.getEndlessWave() > 1 || state.getCurrentRunScore() > 0;
                    if (hasRun) {
                        Dialog dialog = new Dialog("Resume Run?", skin) {
                            @Override
                            protected void result(Object object) {
                                int choice = (Integer) object;
                                if (choice == 1) game.goToEndlessMode(state.getUsername());
                                else if (choice == 2) {
                                    state.resetEndlessWave();
                                    state.resetRunState();
                                    game.goToEndlessMode(state.getUsername());
                                }
                            }
                        };
                        dialog.text("Continue Wave " + state.getEndlessWave() + "?").button("Yes", 1).button("New", 2).show(stage);
                    } else {
                        game.goToEndlessMode(game.getPlayerState().getUsername());
                    }
                } else {
                    new Dialog("Locked", skin).text("Complete a level first!").button("OK").show(stage);
                }
            }
        });

        TextButton skillsBtn = createHoverButton("Skills & Upgrades", skin, "middle");
        skillsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) { game.goToSkillTree(); }
        });

        TextButton achBtn = createHoverButton("Achievements", skin, "middle");
        achBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) { game.setScreen(new de.tum.cit.fop.maze.GameControl.AchievementsScreen(game)); }
        });

        TextButton settingsBtn = createHoverButton("Settings", skin, "middle");
        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) { game.goToSettings(); }
        });
        
        TextButton backBtn = createHoverButton("Return to Title", skin, "short");
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.unloadGame();
                game.setScreen(new StoryMenu(game, false));
            }
        });
        
        // Consistent spacing
        btnContainer.add(selectLevelBtn).padBottom(15).row();
        btnContainer.add(endlessBtn).padBottom(15).row();
        btnContainer.add(skillsBtn).padBottom(15).row();
        btnContainer.add(achBtn).padBottom(15).row();
        btnContainer.add(settingsBtn).padBottom(15).row();
        btnContainer.add(backBtn).padTop(20);
        
        menuTable.add(btnContainer).center();
    }
    
    // State Constants
    private static final int STATE_CHAOS = 0;
    private static final int STATE_BOSS = 1;
    private static final int STATE_MENU = 2; // Start Menu (New/Continue/Exit)
    private static final int STATE_GAMEHUB = 3; // Game Menu (Select Level etc)
    private static final int STATE_INPUT = 4; // Name Input
    private static final int STATE_OVERWRITE = 5; // Confirm Overwrite
    private static final int STATE_PRE_CINEMATIC = 6; // Transition to Cinematic
    
    // ... Existing fields ...
    private Table inputTable;
    private Table overwriteTable;
    private com.badlogic.gdx.scenes.scene2d.ui.TextField nameInput;
    
    // ...

    // Update addMenuButtons to handle Single Save Logic
    private void addMenuButtons() {
        Table btnContainer = new Table();
        Skin skin = game.getSkin();
        boolean hasSave = de.tum.cit.fop.maze.GameControl.GameSaveManager.hasSave(0);
        
        // Create Buttons
        TextButton continueBtn = createHoverButton("Continue Game", skin, "middle");
        TextButton newGameBtn = createHoverButton("New Game", skin, "middle");
        TextButton exitBtn = createHoverButton("Exit", skin, "short");
        
        // Listeners
        if (hasSave) {
            continueBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (game.loadGame(0)) {
                         game.setScreen(new StoryMenu(game, true));
                    }
                }
            });
        } else {
            // Disable Continue
            continueBtn.setDisabled(true);
            continueBtn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
            continueBtn.setColor(1, 1, 1, 0.5f); // Fade it out
        }
        
        newGameBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (hasSave) {
                    setState(STATE_OVERWRITE);
                } else {
                    setState(STATE_INPUT);
                }
            }
        });
        
        exitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) { Gdx.app.exit(); }
        });
        
        // Layout Order
        if (hasSave) {
            btnContainer.add(continueBtn).padBottom(20).row();
            btnContainer.add(newGameBtn).padBottom(20).row();
            btnContainer.add(exitBtn).padTop(20);
        } else {
            btnContainer.add(newGameBtn).padBottom(20).row();
            btnContainer.add(continueBtn).padBottom(20).row();
            btnContainer.add(exitBtn).padTop(20);
        }
        
        menuTable.add(btnContainer).center();
        
        // Setup other UI tables
        setupInputUI();
        setupOverwriteUI();
    }
    
    private void setupInputUI() {
        inputTable = new Table();
        // Initially hidden
        inputTable.setVisible(false);
        menuTable.addActor(inputTable);
        // Position it? menuTable uses center layout for btnContainer. 
        // We can add it to menuTable but manage visibility.
        // Better: Clear menuTable children or use Stack? 
        // Simple: Just hide btnContainer (need reference) and show inputTable. 
        // For now, let's just create it and switch visibility in setState.
        
        // Actually, let's keep it simple. addMenuButtons created a btnContainer. 
        // We need to access it to hide it.
        // Refactor: Make btnContainer class field or named actor? 
        // Approach: menuTable.clear(); menuTable.add(inputTable); when switching.
    }
    
    private void setupOverwriteUI() {
        overwriteTable = new Table();
        overwriteTable.setVisible(false);
    }
    
    private void setState(int newState) {
        this.state = newState;
        
        if (state == STATE_INPUT) {
            // Hide Standard Buttons
            menuTable.clearChildren();
            
            // Show Input UI
            Table container = new Table();
            nameInput = new com.badlogic.gdx.scenes.scene2d.ui.TextField("", game.getSkin());
            nameInput.setMessageText("Enter Name...");
            
            TextButton confirmBtn = createHoverButton("Confirm", game.getSkin(), "short");
            confirmBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                     String name = nameInput.getText();
                     if (name.trim().isEmpty()) name = "Player";
                     // Start New Game
                     game.startNewGame(name, 0);
                     setState(STATE_PRE_CINEMATIC);
                }
            });
            
            container.add(nameInput).width(300).padBottom(20).row();
            container.add(confirmBtn);
            
            menuTable.add(container).center();
            
            animateText("Do you remember your name?");
            
        } else if (state == STATE_OVERWRITE) {
            menuTable.clearChildren();
            
            Table container = new Table();
            TextButton yesBtn = createHoverButton("Yes", game.getSkin(), "short");
            yesBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    setState(STATE_INPUT);
                }
            });
            
            TextButton noBtn = createHoverButton("No", game.getSkin(), "short");
            noBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // Reset to Main Menu
                    menuTable.clearChildren();
                    addMenuButtons();
                    animateText("Welcome back to the Maze.");
                }
            });
            
            container.add(yesBtn).padRight(20);
            container.add(noBtn);
            
            menuTable.add(container).center();
            
            animateText("Do you want to start over?\nI respect that.");
            
        } else if (state == STATE_PRE_CINEMATIC) {
            isTransitioning = true;
            
            // Hide Right Menu
            menuTable.clearChildren();
            menuTable.setVisible(false);
            
            // Reverse Split Animation (Boss back to Full Width)
            float targetWidth = stage.getWidth();
            bossTable.clearActions();
            bossTable.addAction(Actions.sizeTo(targetWidth, stage.getHeight(), 0.8f, Interpolation.pow2Out));
            
            textLayerTable.clearActions();
            textLayerTable.addAction(Actions.sizeTo(targetWidth, stage.getHeight(), 0.8f, Interpolation.pow2Out));
            
            animateText("Let me tell you the background...");
            
            // Show Arrow for manual input
            startBobbing();
            arrowImage.addAction(Actions.fadeIn(0.5f));
            
            // Unlock input after animation
            stage.addAction(Actions.delay(1.0f, Actions.run(() -> isTransitioning = false)));
        }
    }
    
    private void transitionToMenu() {
        isTransitioning = true;
        arrowImage.clearActions();
        arrowImage.addAction(Actions.fadeOut(0.2f));
        
        // Animate BOTH layers
        float targetWidth = stage.getWidth() * 0.5f;
        bossTable.addAction(Actions.sizeTo(targetWidth, stage.getHeight(), 0.8f, Interpolation.pow2Out));
        textLayerTable.addAction(Actions.sizeTo(targetWidth, stage.getHeight(), 0.8f, Interpolation.pow2Out));
        
        menuTable.setVisible(true);
        menuTable.setPosition(stage.getWidth(), 0);
        menuTable.setSize(targetWidth, stage.getHeight());
        
        menuTable.addAction(Actions.moveTo(targetWidth, 0, 0.8f, Interpolation.pow2Out));
        
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
        arrowImage.setY(0);
        arrowImage.addAction(Actions.forever(
            Actions.sequence(
                Actions.moveBy(0, -5, 0.5f, Interpolation.sine),
                Actions.moveBy(0, 5, 0.5f, Interpolation.sine)
            )
        ));
    }
    
    private void playArrowFeedback() {
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

        updateBackground(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
             handleInput();
        }
        
        stage.act(delta);
        
        // Update Boss Origin continuously
        if (bossImage != null && bossTable != null) {
            bossTable.validate();
            bossImage.setOrigin(Align.center);
        }
        
        stage.draw();
    }


    private void updateBackground(float delta) {
        if (backgroundImage1 == null || backgroundImage2 == null) return;

        backgroundImage1.setX(backgroundImage1.getX() - scrollSpeed * delta);
        backgroundImage2.setX(backgroundImage2.getX() - scrollSpeed * delta);

        float width = backgroundImage1.getWidth();


        if (backgroundImage1.getX() + width <= 0) {

            backgroundImage1.setX(backgroundImage2.getX() + width);
        }


        if (backgroundImage2.getX() + width <= 0) {
            // 把它放到第一张图的屁股后面
            backgroundImage2.setX(backgroundImage1.getX() + width);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        if (backgroundImage1 != null && backgroundImage2 != null) {
            float stageW = stage.getWidth();
            float stageH = stage.getHeight();

            backgroundImage1.setSize(stageW, stageH);
            backgroundImage2.setSize(stageW, stageH);
        }

        if (cinematicBarTop != null && cinematicBarBottom != null) {
            float stageW = stage.getWidth();
            float stageH = stage.getHeight();

            float barHeight = stageH * CINEMATIC_RATIO;

            cinematicBarBottom.setSize(stageW, barHeight);
            cinematicBarBottom.setPosition(0, 0);


            cinematicBarTop.setSize(stageW, barHeight);
            cinematicBarTop.setPosition(0, stageH - barHeight); // 放在最顶端
        }

        if (gradientBg != null) {
            gradientBg.setSize(stage.getWidth(), 500);
        }
        
        if (bossImage != null && bossTable != null) {
             bossTable.invalidate();
             bossTable.validate();
             bossImage.setOrigin(Align.center);
        }
        
        if (!isTransitioning) {
            if (state < 2) {
                // Intro Full Width
                bossTable.setSize(stage.getWidth(), stage.getHeight());
                textLayerTable.setSize(stage.getWidth(), stage.getHeight());
                if (menuTable != null) {
                    menuTable.setSize(stage.getWidth() * 0.5f, stage.getHeight());
                    menuTable.setPosition(stage.getWidth(), 0);
                }
            } else {
                 // Splt 50/50
                 float splitW = stage.getWidth() * 0.5f;
                 bossTable.setSize(splitW, stage.getHeight());
                 textLayerTable.setSize(splitW, stage.getHeight());
                 
                 if (menuTable != null) {
                    menuTable.setSize(splitW, stage.getHeight());
                    menuTable.setPosition(splitW, 0);
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
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (blackTexture != null) blackTexture.dispose();
    }
}
