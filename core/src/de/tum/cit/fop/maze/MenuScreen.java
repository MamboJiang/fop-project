package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;


/**
 * The MenuScreen class represents the main menu of the game.
 * It provides options for New Game, Continue, Level Selection, Endless Mode, and Settings.
 */
public class MenuScreen implements Screen {

    private final Stage stage;


    /**
     * Constructor for MenuScreen. Initializes the UI stage and buttons.
     * @param game The main game instance.
     */
    public MenuScreen(MazeRunnerGame game) {
        Viewport viewport = new FitViewport(2560, 1440);
        stage = new Stage(viewport, game.getSpriteBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);


        boolean isLoaded = (game.getPlayerState() != null && game.getPlayerState().getUsername() != null);

        if (!isLoaded) {

            table.add(new Label("Maze Runner", game.getSkin(), "title")).padBottom(80).row();


            int latestSlot = de.tum.cit.fop.maze.GameControl.GameSaveManager.getLatestSaveSlot();
            TextButton continueButton = new TextButton("Continue Game", game.getSkin());
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
            table.add(continueButton).padBottom(15).row();


            TextButton newGameButton = new TextButton("New Game", game.getSkin());
            table.add(newGameButton).padBottom(15).row();
            newGameButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new de.tum.cit.fop.maze.GameControl.SaveSlotScreen(game, false));
                }
            });


            TextButton loadGameButton = new TextButton("Load Game", game.getSkin());
            table.add(loadGameButton).padBottom(15).row();
            loadGameButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new de.tum.cit.fop.maze.GameControl.SaveSlotScreen(game, true));
                }
            });
            

            TextButton settingsButton = new TextButton("Settings", game.getSkin());
            table.add(settingsButton).padBottom(15).row();
            settingsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToSettings();
                }
            });


            TextButton exitButton = new TextButton("Exit", game.getSkin());
            table.add(exitButton).row();
            exitButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.app.exit();
                }
            });

        } else {

            String title = "Welcome, " + game.getPlayerState().getUsername();
            table.add(new Label(title, game.getSkin(), "title")).padBottom(50).row();
            

            TextButton playButton = new TextButton("Select Level", game.getSkin());
            table.add(playButton).padBottom(15).row();
            playButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToLevelSelect();
                }
            });


            TextButton endlessButton = new TextButton("Endless Mode", game.getSkin());
            table.add(endlessButton).padBottom(15).row();
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


            TextButton skillsButton = new TextButton("Skills & Upgrades", game.getSkin());
            table.add(skillsButton).padBottom(15).row();
            skillsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToSkillTree();
                }
            });


            TextButton achButton = new TextButton("Achievements", game.getSkin());
            table.add(achButton).padBottom(15).row();
            achButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new de.tum.cit.fop.maze.GameControl.AchievementsScreen(game));
                }
            });
            

            TextButton settingsButton = new TextButton("Settings", game.getSkin());
            table.add(settingsButton).padBottom(15).row();
            settingsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToSettings();
                }
            });


            TextButton backButton = new TextButton("Return to Title", game.getSkin());
            table.add(backButton).row();
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
     * Renders the menu screen.
     * @param delta Time since last frame.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); 
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    /**
     * Resizes the stage viewport.
     * @param width New width.
     * @param height New height.
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Disposes of the stage resources.
     */
    @Override
    public void dispose() {
        stage.dispose();
    }

    /**
     * Called when this screen becomes the current screen.
     * Sets the input processor to the stage.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}
}
