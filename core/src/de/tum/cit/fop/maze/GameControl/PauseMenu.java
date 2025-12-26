package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class PauseMenu extends Table {
    
    private final MazeRunnerGame game;
    private Runnable onResume;
    private Runnable onExit;

    public PauseMenu(MazeRunnerGame game, Runnable onResume, Runnable onExit) {
        this.game = game;
        this.onResume = onResume;
        this.onExit = onExit;
        
        setFillParent(true);
        setVisible(false);
        
        setupUI();
    }
    
    private void setupUI() {
        Skin skin = game.getSkin();
        
        // Semi-transparent background
        // Assumes "white" exists in skin for tinting, otherwise we might need a Pixmap or similar.
        // StoryDialogueScreen used: game.getSkin().newDrawable("white", 0, 0, 0, 0.8f);
        // We will trust "white" exists as it worked in StoryDialogueScreen
        Drawable bg = skin.newDrawable("white", 0, 0, 0, 0.8f);
        setBackground(bg);
        
        // Content Window
        Table content = new Table();
        content.setBackground(skin.getDrawable("window"));
        
        // Title
        content.add(new Label("PAUSED", skin, "title")).pad(20).row();
        
        // Resume Button
        TextButton resumeBtn = new TextButton("Resume", skin);
        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onResume != null) onResume.run();
                setVisible(false);
            }
        });
        content.add(resumeBtn).width(300).pad(10).row();
        
        // Exit to Menu Button
        TextButton exitBtn = new TextButton("Exit to Menu", skin);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onExit != null) onExit.run();
                game.goToMenu();
            }
        });
        content.add(exitBtn).width(300).pad(10).row();
        
        add(content);
    }
    
    public void show() {
        setVisible(true);
        toFront();
    }
    
    public void hide() {
        setVisible(false);
    }
}
