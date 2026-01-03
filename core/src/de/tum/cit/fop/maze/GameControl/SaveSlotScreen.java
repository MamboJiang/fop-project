package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class SaveSlotScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final boolean isLoading; // true = Load Game, false = New Game

    public SaveSlotScreen(MazeRunnerGame game, boolean isLoading) {
        this.game = game;
        this.isLoading = isLoading;
        this.stage = new Stage(new FitViewport(2560, 1440), game.getSpriteBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        String titleText = isLoading ? "Load Game - Select Slot" : "New Game - Select Slot";
        table.add(new Label(titleText, game.getSkin(), "title")).padBottom(50).row();
        
        // 3 Slots
        for (int i = 0; i < 3; i++) {
            final int slotIndex = i;
            String summary = GameSaveManager.getSaveSummary(slotIndex);
            boolean exists = GameSaveManager.hasSave(slotIndex);
            
            String btnText = "Slot " + (i+1) + ": " + summary;
            TextButton slotButton = new TextButton(btnText, game.getSkin());
            
            // Logic
            slotButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (isLoading) {
                        if (exists) {
                            if (game.loadGame(slotIndex)) {
                                game.goToLevelSelect(); // Or menu, but usually game start/level select
                            }
                        } else {
                            // Can't load empty
                            // Maybe play error sound or shake
                        }
                    } else {
                        // New Game
                        if (exists) {
                            // Confirm Overwrite
                            showOverwriteDialog(slotIndex);
                        } else {
                           showNameInputDialog(slotIndex);
                        }
                    }
                }
            });
            
            // Disable Load button if empty? Or just do nothing?
            if (isLoading && !exists) {
                slotButton.setColor(0.5f, 0.5f, 0.5f, 1f); 
                slotButton.setDisabled(true);
            }
            
            table.add(slotButton).width(600).height(80).padBottom(20).row();
        }

        // Back Button
        TextButton backButton = new TextButton("Back", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        table.add(backButton).width(200).padTop(30);
    }
    
    private void showOverwriteDialog(int slotIndex) {
        Dialog dialog = new Dialog("Overwrite Save?", game.getSkin()) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    showNameInputDialog(slotIndex);
                }
            }
        };
        dialog.text("Slot " + (slotIndex + 1) + " is not empty.\nOverwrite it?");
        dialog.button("Yes", true);
        dialog.button("No", false);
        dialog.show(stage);
    }
    
    private void showNameInputDialog(int slotIndex) {
        TextField nameField = new TextField("Player", game.getSkin());
        Dialog dialog = new Dialog("Enter Player Name", game.getSkin()) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                     String name = nameField.getText();
                     if (name.trim().isEmpty()) name = "Player";
                     game.startNewGame(name, slotIndex);
                }
            }
        };
        dialog.getContentTable().add(new Label("Name:", game.getSkin())).padRight(10);
        dialog.getContentTable().add(nameField).width(200).row();
        dialog.button("Start", true);
        dialog.button("Cancel", false);
        dialog.show(stage);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        stage.dispose();
    }
}
