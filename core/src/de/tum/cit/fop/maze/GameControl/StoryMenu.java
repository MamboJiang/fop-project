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

    private int state = 0;
    private boolean isTransitioning = false;

    private Table bossTable;
    private Table textLayerTable;
    private Table menuTable;
    private Image gradientBg;

    private Image bossImage;
    private Image arrowImage;
    private Table dialogueContainer;


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

    public static float savedBackgroundX = 0f;

    private Image cinematicBarTop;
    private Image cinematicBarBottom;
    private Texture blackTexture;
    private static final float CINEMATIC_RATIO = 0.125f;

    private boolean isGameMenu = false;

    private Table titleTable;

    public StoryMenu(MazeRunnerGame game) {
        this(game, false);
    }

    /**
     * Constructor for StoryMenu.
     * 
     * @param game       Main game instance.
     * @param isGameMenu True if this is the in-game hub, False if main menu.
     */
    public StoryMenu(MazeRunnerGame game, boolean isGameMenu) {
        this.game = game;
        this.isGameMenu = isGameMenu;
        this.stage = new Stage(new ExtendViewport(1920, 1080));

        setupUI();
    }

    /**
     * Initializes the UI components including background, layers, and interactive elements.
     */
    private void setupUI() {
        Pixmap pix = new Pixmap(1, 500, Pixmap.Format.RGBA8888);
        for (int y = 0; y < 500; y++) {
            float alpha = 1.0f - ((float) y / 500f);
            pix.setColor(0f, 0f, 0.4f, alpha * 0.9f);
            pix.drawPixel(0, 499 - y);
        }
        gradientTexture = new Texture(pix);
        pix.dispose();

        gradientBg = new Image(gradientTexture);
        gradientBg.setFillParent(false);
        gradientBg.setSize(stage.getWidth(), 500);

        backgroundTexture = new Texture(Gdx.files.internal("selfmade/background.png"));

        backgroundImage1 = new Image(backgroundTexture);
        backgroundImage2 = new Image(backgroundTexture);

        backgroundImage1.setScaling(Scaling.stretch);
        backgroundImage1.setSize(stage.getWidth(), stage.getHeight());

        backgroundImage2.setScaling(Scaling.stretch);
        backgroundImage2.setSize(stage.getWidth(), stage.getHeight());

        backgroundImage1.setPosition(savedBackgroundX, 0);
        backgroundImage2.setPosition(savedBackgroundX + stage.getWidth(), 0);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        blackTexture = new Texture(pixmap);
        pixmap.dispose();

        cinematicBarTop = new Image(blackTexture);
        cinematicBarBottom = new Image(blackTexture);

        bossTable = new Table();
        bossTable.setFillParent(false);
        bossTable.bottom().left();


        textLayerTable = new Table();
        textLayerTable.setFillParent(false);
        textLayerTable.bottom().left();

        float initialWidth = isGameMenu ? stage.getWidth() : stage.getWidth();

        bossTable.setSize(initialWidth, stage.getHeight());
        textLayerTable.setSize(initialWidth, stage.getHeight());


        titleTable = new Table();
        titleTable.setFillParent(true);
        titleTable.top();


        Label.LabelStyle titleStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);
        Label titleLabel = new Label("UNDERMASK", titleStyle);
        titleLabel.setFontScale(4.0f);
        titleLabel.setAlignment(Align.center);

        titleTable.add(titleLabel).padTop(300);
        if (isGameMenu) {
            titleTable.setVisible(false);
        }

        stage.addActor(backgroundImage1);
        stage.addActor(backgroundImage2);
        stage.addActor(bossTable);
        stage.addActor(cinematicBarBottom);
        stage.addActor(cinematicBarTop);
        stage.addActor(gradientBg);
        stage.addActor(textLayerTable);

        stage.addActor(titleTable);

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

        bossTable.add(bossImage).grow().padTop(com.badlogic.gdx.scenes.scene2d.ui.Value.percentHeight(0.2f, bossTable));


        Gdx.app.postRunnable(() -> {
            bossTable.layout();
            bossImage.setOrigin(Align.center);
            bossImage.setScale(1.3f);
        });


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

        if (!isGameMenu) {
            titleTable.getColor().a = 0;
            titleTable.addAction(Actions.fadeIn(1.0f));
        }


        if (isGameMenu) {
            animateText("Welcome back to the Undermask.");
        } else {
            animateText("You are in a chaos\nPress [space] to continue");
        }

        createArrowTexture();
        arrowImage = new Image(arrowTexture);
        arrowImage.setOrigin(Align.center);
        arrowImage.setSize(32, 24);
        arrowImage.setPosition(0, 0);

        Group arrowGroup = new Group();
        arrowGroup.setSize(32, 24);
        arrowGroup.addActor(arrowImage);

        startBobbing();

        Stack stack = new Stack();
        stack.add(textTable);

        Table arrowTable = new Table();
        arrowTable.add(arrowGroup).size(32, 24).expand().bottom().padBottom(10);
        stack.add(arrowTable);

        dialogueContainer.add(stack).grow().pad(50);
        textLayerTable.add(dialogueContainer).growX().height(400).bottom();

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

        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (event.getTarget().isDescendantOf(menuTable)) {
                    return;
                }
                handleInput();
            }
        });

        if (bossImage != null) {
            bossImage.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (state == 3 || isGameMenu) {
                        handleBossClick();
                    } else {
                        handleInput();
                    }
                }
            });
        }

        if (isGameMenu) {
            isTransitioning = true;

            bossTable.setSize(stage.getWidth(), stage.getHeight());
            textLayerTable.setSize(stage.getWidth(), stage.getHeight());

            menuTable.setSize(stage.getWidth() * 0.5f, stage.getHeight());
            menuTable.setPosition(stage.getWidth(), 0);
            menuTable.setVisible(false);


            stage.addAction(Actions.delay(0.01f, Actions.run(this::transitionToMenu)));
        }
    }

    private int bossClickCount = 0;
    private com.badlogic.gdx.utils.JsonValue bossDialogueData;

    /**
     * Loads boss dialogue data from JSON if not already loaded.
     */
    private void loadBossDialogue() {
        if (bossDialogueData == null) {
            com.badlogic.gdx.files.FileHandle file = Gdx.files.internal("data/boss_dialogue.json");
            if (file.exists()) {
                bossDialogueData = new com.badlogic.gdx.utils.JsonReader().parse(file);
            }
        }
    }



    /**
     * Handles clicks on the boss image to trigger dialogue or interactions.
     */
    private void handleBossClick() {
        if (bossDialogueData == null)
            loadBossDialogue();
        if (bossDialogueData == null)
            return;

        playArrowFeedback();

        bossClickCount++;

        String textToSay = "";


        com.badlogic.gdx.utils.JsonValue milestones = bossDialogueData.get("milestones");
        if (milestones != null && milestones.has(String.valueOf(bossClickCount))) {
            textToSay = milestones.getString(String.valueOf(bossClickCount));
        }
        else if (bossClickCount > 100) {
            textToSay = String.valueOf(bossClickCount);
        }
        else {
            java.util.List<String> pool = new java.util.ArrayList<>();

            com.badlogic.gdx.utils.JsonValue common = bossDialogueData.get("common");
            if (common != null) {
                for (com.badlogic.gdx.utils.JsonValue val : common)
                    pool.add(val.asString());
            }

            de.tum.cit.fop.maze.GameObj.PlayerState state = game.getPlayerState();
            if (state != null) {
                if (state.getCompletedLevels().size() >= 6) {
                    com.badlogic.gdx.utils.JsonValue story = bossDialogueData.get("story_complete");
                    if (story != null)
                        for (com.badlogic.gdx.utils.JsonValue val : story)
                            pool.add(val.asString());
                }
                if (state.getMaxEndlessFloor() >= 5) {
                    com.badlogic.gdx.utils.JsonValue endless = bossDialogueData.get("endless_5");
                    if (endless != null)
                        for (com.badlogic.gdx.utils.JsonValue val : endless)
                            pool.add(val.asString());
                }
            }

            if (!pool.isEmpty()) {
                textToSay = pool.get(com.badlogic.gdx.math.MathUtils.random(pool.size() - 1));
            } else {
                textToSay = "...";
            }
        }

        animateText(textToSay);

        bossImage.clearActions();
        bossImage.addAction(Actions.sequence(
                Actions.scaleTo(1.2f, 1.2f, 0.1f),
                Actions.scaleTo(1.3f, 1.3f, 0.1f)
        ));

    }

    /**
     * Creates a styled text button with hover animation.
     * @param text Button label.
     * @param skin UI skin.
     * @param styleName Style name in the skin.
     * @return The created TextButton.
     */
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

    /**
     * Animates text appearance in the dialogue box.
     * @param text The text to display.
     */
    private void animateText(String text) {
        textLabel.setText(text);

        textAnimContainer.clearActions();
        textAnimContainer.setScale(0f);

        Gdx.app.postRunnable(() -> {
            if (bossTable != null)
                bossTable.layout();
            if (textLayerTable != null)
                textLayerTable.layout();
            if (dialogueContainer != null)
                dialogueContainer.layout();

            textAnimContainer.setOrigin(Align.center);
            textAnimContainer.addAction(Actions.scaleTo(1.0f, 1.0f, 0.25f, Interpolation.swingOut));
        });
    }

    /**
     * Handles general input (clicks, key presses) based on current state.
     */
    private void handleInput() {
        if (isTransitioning)
            return;

        if (state == 3) {
            handleBossClick();
            return;
        }

        if (state == 0) {
            playArrowFeedback();
            state = 1;
            titleTable.addAction(Actions.fadeOut(0.5f));
            bossImage.addAction(Actions.fadeIn(1.0f));
            animateText("Welcome... to the Undermask.");
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

    /**
     * Adds buttons for the in-game hub menu.
     */
    private void addGameMenuButtons() {
        Table btnContainer = new Table();
        Skin skin = game.getSkin();

        TextButton selectLevelBtn = createHoverButton("Story Mode", skin, "middle");
        selectLevelBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToLevelSelect();
            }
        });


        TextButton endlessV2Btn = createHoverButton("Endless Mode", skin, "middle");
        endlessV2Btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                de.tum.cit.fop.maze.GameObj.PlayerState state = game.getPlayerState();
                if (state.isAttackUnlocked() || state.isNonoUnlocked()) {
                    game.goToEndlessModeVer2(state.getUsername());
                } else {
                    animateText(
                            "You need to be able to attack first or you are not qualified yet! Play your story first!");
                }
            }
        });

        TextButton skillsBtn = createHoverButton("Skills & Upgrades", skin, "middle");
        skillsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToSkillTree();
            }
        });

        TextButton achBtn = createHoverButton("Achievements", skin, "middle");
        achBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new de.tum.cit.fop.maze.GameControl.AchievementsScreen(game));
            }
        });

        TextButton encyclopediaBtn = createHoverButton("Encyclopedia", skin, "middle");
        encyclopediaBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new de.tum.cit.fop.maze.GameControl.EncyclopediaScreen(game));
            }
        });

        TextButton settingsBtn = createHoverButton("Settings", skin, "middle");
        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToSettings();
            }
        });

        TextButton backBtn = createHoverButton("Return to Title", skin, "short");
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.unloadGame();
                game.setScreen(new StoryMenu(game, false));
            }
        });

        btnContainer.add(selectLevelBtn).padBottom(15).row();
        btnContainer.add(endlessV2Btn).padBottom(15).row();
        btnContainer.add(skillsBtn).padBottom(15).row();
        btnContainer.add(achBtn).padBottom(15).row();
        btnContainer.add(encyclopediaBtn).padBottom(15).row();
        btnContainer.add(settingsBtn).padBottom(15).row();
        btnContainer.add(backBtn).padTop(20);

        menuTable.add(btnContainer).center();
    }

    private static final int STATE_CHAOS = 0;
    private static final int STATE_BOSS = 1;
    private static final int STATE_MENU = 2;
    private static final int STATE_GAMEHUB = 3;
    private static final int STATE_INPUT = 4;
    private static final int STATE_OVERWRITE = 5;
    private static final int STATE_PRE_CINEMATIC = 6;

    private Table inputTable;
    private Table overwriteTable;
    private com.badlogic.gdx.scenes.scene2d.ui.TextField nameInput;


    /**
     * Adds buttons for the main menu.
     */
    private void addMenuButtons() {
        Table btnContainer = new Table();
        Skin skin = game.getSkin();
        boolean hasSave = de.tum.cit.fop.maze.GameControl.GameSaveManager.hasSave(0);

        TextButton continueBtn = createHoverButton("Continue Game", skin, "middle");
        TextButton newGameBtn = createHoverButton("New Game", skin, "middle");
        TextButton exitBtn = createHoverButton("Exit", skin, "short");

        if (hasSave) {
            continueBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (game.loadGame(0)) {
                        isTransitioning = true;
                        float duration = 0.3f;
                        Interpolation interp = Interpolation.pow2Out;

                        menuTable.addAction(Actions.moveTo(stage.getWidth(), 0, duration, interp));

                        bossTable.addAction(Actions.sizeTo(stage.getWidth(), stage.getHeight(), duration, interp));
                        textLayerTable.addAction(Actions.sizeTo(stage.getWidth(), stage.getHeight(), duration, interp));

                        stage.addAction(
                                Actions.delay(duration, Actions.run(() -> game.setScreen(new StoryMenu(game, true)))));
                    }
                }
            });
        } else {
            continueBtn.setDisabled(true);
            continueBtn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
            continueBtn.setColor(1, 1, 1, 0.5f);
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
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

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

        setupInputUI();
        setupOverwriteUI();
    }

    /**
     * Sets up the placeholder UI for name input (handled dynamically).
     */
    private void setupInputUI() {
        inputTable = new Table();
        inputTable.setVisible(false);
        menuTable.addActor(inputTable);
    }

    /**
     * Sets up the placeholder UI for overwrite confirmation (handled dynamically).
     */
    private void setupOverwriteUI() {
        overwriteTable = new Table();
        overwriteTable.setVisible(false);
    }

    /**
     * Switches the menu state and updates UI accordingly.
     * @param newState The new state ID.
     */
    private void setState(int newState) {
        this.state = newState;

        if (state == STATE_INPUT) {
            menuTable.clearChildren();

            Table container = new Table();
            nameInput = new com.badlogic.gdx.scenes.scene2d.ui.TextField("", game.getSkin());
            nameInput.setMessageText("Enter Name...");

            TextButton confirmBtn = createHoverButton("Confirm", game.getSkin(), "short");
            confirmBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String name = nameInput.getText();
                    if (name.trim().isEmpty())
                        name = "Player";
                    game.startNewGame(name, 0);
                    setState(STATE_PRE_CINEMATIC);
                }
            });

            container.add(nameInput).width(300).padBottom(20).row();
            container.add(confirmBtn);

            menuTable.add(container).center();

            animateText("Do you still remember your name?");

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
                    menuTable.clearChildren();
                    addMenuButtons();
                    animateText("Welcome back to the Undermask.");
                }
            });

            container.add(yesBtn).padRight(20);
            container.add(noBtn);

            menuTable.add(container).center();

            animateText("Do you want to start over?\nI respect that.");

        } else if (state == STATE_PRE_CINEMATIC) {
            isTransitioning = true;

            menuTable.clearChildren();
            menuTable.setVisible(false);

            float targetWidth = stage.getWidth();
            bossTable.clearActions();
            bossTable.addAction(Actions.sizeTo(targetWidth, stage.getHeight(), 0.8f, Interpolation.pow2Out));

            textLayerTable.clearActions();
            textLayerTable.addAction(Actions.sizeTo(targetWidth, stage.getHeight(), 0.8f, Interpolation.pow2Out));

            animateText("Let me tell you the background...");

            startBobbing();
            arrowImage.addAction(Actions.fadeIn(0.5f));

            stage.addAction(Actions.delay(1.0f, Actions.run(() -> isTransitioning = false)));
        }
    }

    /**
     * Transitions from intro sequence to the main menu view.
     */
    private void transitionToMenu() {
        isTransitioning = true;
        arrowImage.clearActions();

        if (state == 2) {
            arrowImage.setVisible(false);
        } else {
            arrowImage.setVisible(true);
            arrowImage.setColor(1, 1, 1, 1);
            startBobbing();
        }

        float targetWidth = stage.getWidth() * 0.5f;
        bossTable.addAction(Actions.sizeTo(targetWidth, stage.getHeight(), 0.8f, Interpolation.pow2Out));
        textLayerTable.addAction(Actions.sizeTo(targetWidth, stage.getHeight(), 0.8f, Interpolation.pow2Out));

        menuTable.setVisible(true);
        menuTable.setPosition(stage.getWidth(), 0);
        menuTable.setSize(targetWidth, stage.getHeight());

        menuTable.addAction(Actions.moveTo(targetWidth, 0, 0.8f, Interpolation.pow2Out));

        stage.addAction(Actions.delay(0.8f, Actions.run(() -> isTransitioning = false)));
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
     * Starts the bobbing animation for the arrow indicator.
     */
    private void startBobbing() {
        arrowImage.clearActions();
        arrowImage.setY(0);
        arrowImage.addAction(Actions.forever(
                Actions.sequence(
                        Actions.moveBy(0, -5, 0.5f, Interpolation.sine),
                        Actions.moveBy(0, 5, 0.5f, Interpolation.sine))));
    }

    /**
     * Plays feedback animation on the arrow.
     */
    private void playArrowFeedback() {
        if (state == 2)
            return;
        arrowImage.clearActions();
        arrowImage.addAction(Actions.sequence(
                Actions.scaleTo(1.5f, 1.5f, 0.05f),
                Actions.scaleTo(1f, 1f, 0.05f),
                Actions.run(this::startBobbing)));
    }

    /**
     * Renders the menu screen.
     * @param delta Time delta.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateBackground(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            handleInput();
        }

        stage.act(delta);

        if (bossImage != null && bossTable != null) {
            bossTable.validate();
            bossImage.setOrigin(Align.center);
        }

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
        savedBackgroundX = currentX;

        float width = backgroundImage1.getWidth();

        if (backgroundImage1.getX() + width <= 0) {

            backgroundImage1.setX(backgroundImage2.getX() + width);
        }

        if (backgroundImage2.getX() + width <= 0) {
            backgroundImage2.setX(backgroundImage1.getX() + width);
        }
    }

    /**
     * Called when the screen becomes current.
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

        if (backgroundImage1 != null && backgroundImage2 != null) {
            float stageW = stage.getWidth();
            float stageH = stage.getHeight();

            backgroundImage1.setSize(stageW, stageH);
            backgroundImage2.setSize(stageW, stageH);

            backgroundImage2.setX(backgroundImage1.getX() + stageW);
        }

        if (cinematicBarTop != null && cinematicBarBottom != null) {
            float stageW = stage.getWidth();
            float stageH = stage.getHeight();

            float barHeight = stageH * CINEMATIC_RATIO;

            cinematicBarBottom.setSize(stageW, barHeight);
            cinematicBarBottom.setPosition(0, 0);

            cinematicBarTop.setSize(stageW, barHeight);
            cinematicBarTop.setPosition(0, stageH - barHeight);
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
                bossTable.setSize(stage.getWidth(), stage.getHeight());
                textLayerTable.setSize(stage.getWidth(), stage.getHeight());
                if (menuTable != null) {
                    menuTable.setSize(stage.getWidth() * 0.5f, stage.getHeight());
                    menuTable.setPosition(stage.getWidth(), 0);
                }
            } else {

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
        if (bossTexture != null)
            bossTexture.dispose();
        if (gradientTexture != null)
            gradientTexture.dispose();
        if (arrowTexture != null)
            arrowTexture.dispose();
        if (backgroundTexture != null)
            backgroundTexture.dispose();
        if (blackTexture != null)
            blackTexture.dispose();
    }
}