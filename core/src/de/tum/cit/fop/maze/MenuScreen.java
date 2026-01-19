package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

// Animation imports
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;

/**
 * The MenuScreen class represents the main menu of the game.
 * It provides options for New Game, Continue, Level Selection, Endless Mode, and Settings.
 */
public class MenuScreen implements Screen {

    private final Stage stage;
    private Table contentTable; 
    private Table animatedTable; 

    /**
     * Constructor for MenuScreen. Initializes the UI stage and buttons.
     * @param game The main game instance.
     */
    public MenuScreen(MazeRunnerGame game) {
        Viewport viewport = new FitViewport(2560, 1440);
        stage = new Stage(viewport, game.getSpriteBatch());

        contentTable = new Table();
        contentTable.setFillParent(true);
        stage.addActor(contentTable);

        boolean isLoaded = (game.getPlayerState() != null && game.getPlayerState().getUsername() != null);

        if (!isLoaded) {

            contentTable.add(new Label("Maze Runner", game.getSkin(), "title")).padBottom(80).row();
            
            // Add animated table for buttons
            animatedTable = new Table();
            contentTable.add(animatedTable).row();

            int latestSlot = de.tum.cit.fop.maze.GameControl.GameSaveManager.getLatestSaveSlot();
            
            // Story Mode Button
            TextButton storyButton = createHoverButton("Story Mode", game.getSkin());
            animatedTable.add(storyButton).padBottom(15).row();
            storyButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new de.tum.cit.fop.maze.GameControl.StoryMenu(game));
                }
            });

            TextButton continueButton = createHoverButton("Continue Game", game.getSkin());
            if (latestSlot != -1) {
                continueButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (game.loadGame(latestSlot)) {
                             game.goToMenu(); 
                        }
                    }
                });
            } else {
                continueButton.setDisabled(true);
                continueButton.setColor(0.5f, 0.5f, 0.5f, 1f);
            }
            animatedTable.add(continueButton).padBottom(15).row();


            TextButton newGameButton = createHoverButton("New Game", game.getSkin());
            animatedTable.add(newGameButton).padBottom(15).row();
            newGameButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new de.tum.cit.fop.maze.GameControl.SaveSlotScreen(game, false));
                }
            });


            TextButton loadGameButton = createHoverButton("Load Game", game.getSkin());
            animatedTable.add(loadGameButton).padBottom(15).row();
            loadGameButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new de.tum.cit.fop.maze.GameControl.SaveSlotScreen(game, true));
                }
            });
            

            TextButton settingsButton = createHoverButton("Settings", game.getSkin());
            animatedTable.add(settingsButton).padBottom(15).row();
            settingsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToSettings();
                }
            });


            TextButton exitButton = createHoverButton("Exit", game.getSkin());
            animatedTable.add(exitButton).row();
            exitButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.app.exit();
                }
            });

        } else {

            String title = "Welcome, " + game.getPlayerState().getUsername();
            contentTable.add(new Label(title, game.getSkin(), "title")).padBottom(50).row();
            
            // Add animated table for buttons
            animatedTable = new Table();
            contentTable.add(animatedTable).row();

            TextButton playButton = createHoverButton("Select Level", game.getSkin());
            animatedTable.add(playButton).padBottom(15).row();
            playButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToLevelSelect();
                }
            });


            TextButton endlessButton = createHoverButton("Endless Mode", game.getSkin());
            animatedTable.add(endlessButton).padBottom(15).row();
            endlessButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    boolean unlocked = !game.getPlayerState().getCompletedLevels().isEmpty();
                    if (unlocked) {

                        de.tum.cit.fop.maze.GameObj.PlayerState state = game.getPlayerState();
                        boolean hasRun = state.getEndlessWave() > 1 || state.getCurrentRunScore() > 0;
                        
                        if (hasRun) {
                            Dialog dialog = new Dialog("Resume Run?", game.getSkin()) {
                                @Override
                                protected void result(Object object) {
                                    int choice = (Integer) object;
                                    if (choice == 1) {
                                        game.goToEndlessMode(state.getUsername());
                                    } else if (choice == 2) {
                                        state.resetEndlessWave();
                                        state.resetRunState();
                                        game.goToEndlessMode(state.getUsername());
                                    }
                                }
                            };
                            dialog.text("Continue from Wave " + state.getEndlessWave() + "?");
                            dialog.button("Continue", 1);
                            dialog.button("New Run", 2);
                            dialog.button("Cancel", 0);
                            dialog.show(stage);
                        } else {
                            game.goToEndlessMode(game.getPlayerState().getUsername());
                        }
                    } else {
                        Dialog dialog = new Dialog("Locked", game.getSkin());
                        dialog.text("Complete a level first!");
                        dialog.button("OK");
                        dialog.show(stage);
                    }
                }
            });


            TextButton skillsButton = createHoverButton("Skills & Upgrades", game.getSkin());
            animatedTable.add(skillsButton).padBottom(15).row();
            skillsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToSkillTree();
                }
            });


            TextButton achButton = createHoverButton("Achievements", game.getSkin());
            animatedTable.add(achButton).padBottom(15).row();
            achButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new de.tum.cit.fop.maze.GameControl.AchievementsScreen(game));
                }
            });

            TextButton encyclopediaButton = createHoverButton("Encyclopedia", game.getSkin());
            animatedTable.add(encyclopediaButton).padBottom(15).row();

            encyclopediaButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // 切换到刚才创建的屏幕
                    game.setScreen(new de.tum.cit.fop.maze.GameControl.EncyclopediaScreen(game));
                }
            });
            

            TextButton settingsButton = createHoverButton("Settings", game.getSkin());
            animatedTable.add(settingsButton).padBottom(15).row();
            settingsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToSettings();
                }
            });


            TextButton backButton = createHoverButton("Return to Title", game.getSkin());
            animatedTable.add(backButton).row();
            backButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.unloadGame();
                    game.goToMenu();
                }
            });
        }
    }

    /**
     * Creates a button with hover scaling effects.
     */
    private TextButton createHoverButton(String text, Skin skin) {
        final TextButton button = new TextButton(text, skin);
        button.setTransform(true); // Enable transform for scaling
        button.setOrigin(Align.center); // Scale from center
        
        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (pointer == -1) { // Mouse move
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

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); 
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        
        // Entrance Animation: Slide up from bottom
        // We use setTransform(true) on table if needed, but actions on position usually work fine.
        // We set initial position below screen (-height)
        // Note: setFillParent(true) layout might interfere if not handled, but Actions often override.
        // If it snaps back, we might need to disable fillParent, but let's try this standard approach first.
        
        if (animatedTable != null) {
            // Force layout to determine correct center position first
            contentTable.pack(); // Force layout on the parent table
            
            animatedTable.clearActions();
            animatedTable.addAction(Actions.sequence(
                Actions.moveBy(0, -stage.getHeight()),
                Actions.moveBy(0, stage.getHeight(), 0.3f, Interpolation.exp5Out)
            ));
        }
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
}
