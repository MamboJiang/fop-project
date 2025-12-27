package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.ArrayList;

public class GameOverMenu extends Table{
    private final MazeRunnerGame game;
    private int wavesCleared = -1; // -1 means standard mode
    private Runnable onRetry;
    private Runnable onExit;
    private boolean isWin;
    private Runnable onNextLevel;
    private int finalScore;
    private Table leaderboardTable;

    public GameOverMenu(MazeRunnerGame game, Runnable onRetry, Runnable onExit, Runnable onNextLevel, boolean isWin, int finalScore) {
        this(game, onRetry, onExit, onNextLevel, isWin, -1, finalScore);
    }

    public GameOverMenu(MazeRunnerGame game, Runnable onRetry, Runnable onExit, Runnable onNextLevel, boolean isWin, int wavesCleared, int finalScore) {
        this.game = game;
        this.onRetry = onRetry;
        this.onExit = onExit;
        this.isWin  = isWin;
        this.onNextLevel = onNextLevel;
        this.wavesCleared = wavesCleared;
        this.finalScore = finalScore;

        setFillParent(true);
        setVisible(false);

        setupUI();
    }

    private void setupUI(){
        Skin skin = game.getSkin();
        Drawable bg = skin.newDrawable("white", 0, 0, 0, 0.8f);
        setBackground(bg);

        Table content = new Table();
        content.setBackground(skin.getDrawable("window"));

        Label titleLabelLose = new Label("GAME OVER", skin, "title");
        Label titleLabelWin = new Label("LEVEL CLEARED!", skin, "title");

        Label scoreLabel = new Label("Score: " + finalScore, skin);

        leaderboardTable = new Table();
        content.add(leaderboardTable).pad(10).row();

        // Endless Mode Label
        Label wavesLabel = null;
        if (wavesCleared >= 0) {
            wavesLabel = new Label("Waves Cleared: " + wavesCleared, skin);
            titleLabelLose.setText("run ended"); // Stylish lower case or CAPS
        }

        TextButton retryBtn = new TextButton(wavesCleared >= 0 ? "Restart Run" : "Retry Level", skin);
        retryBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onRetry != null) onRetry.run();
                setVisible(false);
            }
        });

        TextButton exitBtn = new TextButton("Main Menu", skin);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onExit != null) onExit.run();
                game.goToMenu();
            }
        });

        TextButton nextLevelBtn = new TextButton("Next Level", skin);
        if (wavesCleared >= 0) {
            nextLevelBtn.setText("Next Wave");
        }
        nextLevelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onNextLevel != null) {
                    onNextLevel.run();
                }
                setVisible(false);
            }
        });

        if (!isWin) {
            content.add(titleLabelLose).pad(20).row();
            
            if (wavesCleared >= 0) {
                // Endless Mode Loss
                if (wavesLabel != null) content.add(wavesLabel).pad(10).row();
                // User explicitly said: NO RETRY.
                // So only Main Menu?
                // Or maybe "Restart Run" is okay?
                // "Endless mode end after showing cleared waves" -> "don't show RETRY".
                // I will hide Retry button completely for Endless Mode Loss.
                content.add(exitBtn).width(300).pad(10).row();
            } else {
                // Standard Loss
                content.add(retryBtn).width(300).pad(10).row();
                content.add(exitBtn).width(300).pad(10).row();
            }
        } else {
            // Win
            // 胜利时显示分数
            content.add(titleLabelWin).pad(20).row();
            // --- 将分数显示在按钮之前 ---
            content.add(scoreLabel).pad(10).row();

            content.add(nextLevelBtn).pad(20).row();
            if (wavesCleared == -1) {
                // Standard Win - Allow replaying/retrying this level?
                content.add(retryBtn).width(300).pad(10).row();
            }
            content.add(exitBtn).width(300).pad(10).row();
        }

        add(content);
    }
    
    public void show() {
        setVisible(true);
        toFront();
    }

    public void hide() {
        setVisible(false);
    }

    public void loadLeaderboard() {
        leaderboardTable.clear(); // 清空旧数据

        Label title = new Label("Leaderboard", game.getSkin(), "title");
        leaderboardTable.add(title).padBottom(10).colspan(2).row();

        ArrayList<LeaderboardManager.ScoreEntry> scores = LeaderboardManager.loadScores();

        if (scores.isEmpty()) {
            leaderboardTable.add(new Label("No records yet!", game.getSkin())).colspan(2);
        } else {
            for (int i = 0; i < scores.size(); i++) {
                LeaderboardManager.ScoreEntry entry = scores.get(i);

                // 排名 #1, #2...
                Label rankLabel = new Label("#" + (i + 1), game.getSkin());
                // 名字: 分数
                Label entryLabel = new Label(entry.name + ": " + entry.score, game.getSkin());

                leaderboardTable.add(rankLabel).left().padRight(10);
                leaderboardTable.add(entryLabel).left().row();
            }
        }
    }
}
