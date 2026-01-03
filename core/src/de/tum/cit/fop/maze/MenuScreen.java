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
 * The MenuScreen class is responsible for displaying the main menu of the game.
 * It extends the LibGDX Screen class and sets up the UI components for the menu.
 */
public class MenuScreen implements Screen {

    private final Stage stage;

    /**
     * Constructor for MenuScreen. Sets up the camera, viewport, stage, and UI elements.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public MenuScreen(MazeRunnerGame game) {
        Viewport viewport = new FitViewport(2560, 1440);
        stage = new Stage(viewport, game.getSpriteBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Check if game is loaded
        boolean isLoaded = (game.getPlayerState() != null && game.getPlayerState().getUsername() != null);

        if (!isLoaded) {
            // --- STATE 1: TITLE SCREEN ---
            table.add(new Label("Maze Runner", game.getSkin(), "title")).padBottom(80).row();

            // 1. Continue Game (if any save exists)
            int latestSlot = de.tum.cit.fop.maze.GameControl.GameSaveManager.getLatestSaveSlot();
            TextButton continueButton = new TextButton("Continue Game", game.getSkin());
            if (latestSlot != -1) {
                continueButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (game.loadGame(latestSlot)) {
                             // Reload Menu to switch to Hub State
                             game.goToMenu(); 
                        }
                    }
                });
            } else {
                continueButton.setDisabled(true);
                continueButton.setColor(0.5f, 0.5f, 0.5f, 1f);
            }
            table.add(continueButton).width(300).padBottom(15).row();

            // 2. New Game
            TextButton newGameButton = new TextButton("New Game", game.getSkin());
            table.add(newGameButton).width(300).padBottom(15).row();
            newGameButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new de.tum.cit.fop.maze.GameControl.SaveSlotScreen(game, false));
                }
            });

            // 3. Load Game
            TextButton loadGameButton = new TextButton("Load Game", game.getSkin());
            table.add(loadGameButton).width(300).padBottom(15).row();
            loadGameButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new de.tum.cit.fop.maze.GameControl.SaveSlotScreen(game, true));
                }
            });
            
            // 4. Settings
            TextButton settingsButton = new TextButton("Settings", game.getSkin());
            table.add(settingsButton).width(300).padBottom(15).row();
            settingsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToSettings();
                }
            });

            // 5. Exit
            TextButton exitButton = new TextButton("Exit", game.getSkin());
            table.add(exitButton).width(300).row();
            exitButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.app.exit();
                }
            });

        } else {
            // --- STATE 2: GAME HUB (Loaded) ---
            String title = "Welcome, " + game.getPlayerState().getUsername();
            table.add(new Label(title, game.getSkin(), "title")).padBottom(50).row();
            
            // 1. Select Level (was Continue/Play)
            TextButton playButton = new TextButton("Select Level", game.getSkin());
            table.add(playButton).width(300).padBottom(15).row();
            playButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToLevelSelect();
                }
            });

            // 2. Endless Mode
            TextButton endlessButton = new TextButton("Endless Mode", game.getSkin());
            table.add(endlessButton).width(300).padBottom(15).row();
            endlessButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    boolean unlocked = !game.getPlayerState().getCompletedLevels().isEmpty();
                    if (unlocked) {
                        // Check for existing run
                        de.tum.cit.fop.maze.GameObj.PlayerState state = game.getPlayerState();
                        boolean hasRun = state.getEndlessWave() > 1 || state.getCurrentRunScore() > 0;
                        
                        if (hasRun) {
                            Dialog dialog = new Dialog("Resume Run?", game.getSkin()) {
                                @Override
                                protected void result(Object object) {
                                    int choice = (Integer) object;
                                    if (choice == 1) { // Resume
                                        game.goToEndlessMode(state.getUsername());
                                    } else if (choice == 2) { // New Run
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

            // 3. Skills
            TextButton skillsButton = new TextButton("Skills & Upgrades", game.getSkin());
            table.add(skillsButton).width(300).padBottom(15).row();
            skillsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToSkillTree();
                }
            });

            // 4. Achievements
            TextButton achButton = new TextButton("Achievements", game.getSkin());
            table.add(achButton).width(300).padBottom(15).row();
            achButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new de.tum.cit.fop.maze.GameControl.AchievementsScreen(game));
                }
            });
            
             // 5. Settings
            TextButton settingsButton = new TextButton("Settings", game.getSkin());
            table.add(settingsButton).width(300).padBottom(15).row();
            settingsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToSettings();
                }
            });

            // 6. Return to Title
            TextButton backButton = new TextButton("Return to Title", game.getSkin());
            table.add(backButton).width(300).row();
            backButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // Unload game state
                    game.unloadGame(); // Need to implement this method or manually nullify
                    game.goToMenu();
                }
            });
        }
    }

    // ... render, resize, dispose ...

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
        
        // Auto-load if we prefer? No, user choice is better.
        // But we might want to refresh 'Continue' button if we returned from game?
        // Note: New MenuScreen is created every time goToMenu is called, so constructor logic runs again.
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}
}
