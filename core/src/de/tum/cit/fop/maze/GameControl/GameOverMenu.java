package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class GameOverMenu extends Table{
    private final MazeRunnerGame game;
    private Runnable onRetry;
    private Runnable onExit;
    private boolean isWin;
    private Runnable onNextLevel;


    public GameOverMenu(MazeRunnerGame game, Runnable onRetry, Runnable onExit,Runnable onNextLevel, boolean isWin) {
        this.game = game;
        this.onRetry = onRetry;
        this.onExit = onExit;
        this.isWin  = isWin;
        this.onNextLevel = onNextLevel;

        setFillParent(true);
        setVisible(false); // 默认隐藏

        setupUI();
    }

    private void setupUI(){
        Skin skin = game.getSkin();
        // 1. 半透明背景 (Semi-transparent background)
        Drawable bg = skin.newDrawable("white", 0, 0, 0, 0.8f);
        setBackground(bg);

        // 2. 内容窗口 (Window container)
        Table content = new Table();
        content.setBackground(skin.getDrawable("window"));

        // 3. 标题 (You Lose / Game Over)
        Label titleLabelLose = new Label("YOU LOSE!", skin, "title");
        Label titleLabelWin = new Label("YOU WIN!", skin, "title");
        // 你可以根据需要改变颜色，例如红色
        // titleLabel.setColor(Color.RED);


        // 4. 重试按钮 (Retry Button)
        TextButton retryBtn = new TextButton("Retry", skin);
        retryBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onRetry != null) onRetry.run();
                setVisible(false);
            }
        });


        // 5. 返回主菜单按钮 (Exit to Menu Button)
        TextButton exitBtn = new TextButton("Main Menu", skin);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onExit != null) onExit.run();
                game.goToMenu();
            }
        });
        content.add(exitBtn).width(300).pad(10).row();

        TextButton nextLevelBtn = new TextButton("Next Level", skin);
        nextLevelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onNextLevel != null) {
                    onNextLevel.run();
                }
                setVisible(false);
            }
        });

        if(!isWin){
            content.add(titleLabelLose).pad(20).row();
            content.add(retryBtn).width(300).pad(10).row();
            content.add(exitBtn).width(300).pad(10).row();
        }
        else{
            content.add(titleLabelWin).pad(20).row();
            content.add(nextLevelBtn).pad(20).row();
            content.add(retryBtn).width(300).pad(10).row();
            content.add(exitBtn).width(300).pad(10).row();
        }


        add(content);
    }
    public void show() {
        setVisible(true);
        toFront(); // 确保它显示在最上层
    }

    public void hide() {
        setVisible(false);
    }


}
