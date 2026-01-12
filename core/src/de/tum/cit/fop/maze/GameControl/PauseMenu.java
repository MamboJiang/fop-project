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
    
    private Label scoreLabel;
    private Label difficultyLabel;
    private Label xpLabel;

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
        

        Drawable bg = skin.newDrawable("white", 0, 0, 0, 0.8f);
        setBackground(bg);
        

        Table content = new Table();
        content.setBackground(skin.getDrawable("window"));
        

        content.add(new Label("PAUSED", skin, "title")).pad(20).row();
        

        scoreLabel = new Label("Score: 0", skin);
        difficultyLabel = new Label("Difficulty: 1", skin);
        xpLabel = new Label("XP: 0", skin);
        
        content.add(scoreLabel).pad(5).row();
        content.add(difficultyLabel).pad(5).row();
        content.add(xpLabel).pad(5).padBottom(20).row();
        

        TextButton resumeBtn = new TextButton("Resume", skin);
        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onResume != null) onResume.run();
                setVisible(false);
            }
        });
        content.add(resumeBtn).width(300).pad(10).row();
        

        TextButton exitBtn = new TextButton("Exit to Menu", skin);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onExit != null) {
                    onExit.run();
                } else {
                    game.goToMenu();
                }
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
    
    public void updateStats(int score, int difficulty, int xp) {
        if (scoreLabel != null) scoreLabel.setText("Score: " + score);
        if (difficultyLabel != null) difficultyLabel.setText("Difficulty: " + difficulty);
        if (xpLabel != null) xpLabel.setText("Total XP: " + xp);
    }

    public void setStatsVisible(boolean visible) {
        if (scoreLabel != null) scoreLabel.setVisible(visible);
        if (difficultyLabel != null) difficultyLabel.setVisible(visible);
        if (xpLabel != null) xpLabel.setVisible(visible);
    }
}
