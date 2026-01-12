package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.Actor;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.ArrayList;

public class GameOverMenu extends Table{
    private final MazeRunnerGame game;
    private int wavesCleared = -1;
    private Runnable onRetry;
    private Runnable onExit;
    private boolean isWin;
    private Runnable onNextLevel;
    private int finalScore;
    private Table leaderboardTable;
    private Table bottomRankTable;
    private int xp;

    public GameOverMenu(MazeRunnerGame game, Runnable onRetry, Runnable onExit, Runnable onNextLevel, boolean isWin, int finalScore, int xp) {
        this(game, onRetry, onExit, onNextLevel, isWin, -1, finalScore, xp);
    }

    public GameOverMenu(MazeRunnerGame game, Runnable onRetry, Runnable onExit, Runnable onNextLevel, boolean isWin, int wavesCleared, int finalScore, int xp) {
        this.game = game;
        this.onRetry = onRetry;
        this.onExit = onExit;
        this.isWin  = isWin;
        this.onNextLevel = onNextLevel;
        this.wavesCleared = wavesCleared;
        this.finalScore = finalScore;
        this.xp = xp;

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
        Label xpLabel = new Label("XP gained: " + xp, skin);
        

        bottomRankTable = new Table();

        if (wavesCleared >= 0 && !isWin) {
            Label lbTitle = new Label("Leaderboard", skin, "title");
            content.add(lbTitle).padTop(10).row();

            leaderboardTable = new Table();
            leaderboardTable.top();
            
            com.badlogic.gdx.scenes.scene2d.ui.ScrollPane scrollPane = new com.badlogic.gdx.scenes.scene2d.ui.ScrollPane(leaderboardTable, skin);
            scrollPane.setFadeScrollBars(false);
            scrollPane.setScrollingDisabled(true, false);
            

            scrollPane.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
               @Override
               public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                   getStage().setScrollFocus(scrollPane);
               }
               @Override
               public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                   getStage().setScrollFocus(null);
               }
            });


            content.add(scrollPane).width(500).height(200).pad(10).row();
            

            content.add(bottomRankTable).growX().pad(5).row();
        }


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

        TextButton exitBtn = new TextButton(wavesCleared >= 0 ? "Quit" : "Main Menu", skin);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onExit != null) onExit.run();
                else game.goToMenu();
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

                if (wavesLabel != null) content.add(wavesLabel).pad(10).row();
                

                content.add(scoreLabel).pad(5).row();
                content.add(xpLabel).pad(5).row();
                

                content.add(exitBtn).width(300).pad(10).row();
            } else {

                content.add(retryBtn).width(300).pad(10).row();
                content.add(exitBtn).width(300).pad(10).row();
            }
        } else {

            content.add(titleLabelWin).pad(20).row();

            content.add(scoreLabel).pad(10).row();
            content.add(xpLabel).pad(10).row();

            content.add(nextLevelBtn).pad(20).row();
            if (wavesCleared == -1) {

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
        if (leaderboardTable == null) return;
        
        leaderboardTable.clear();
        leaderboardTable.add(new Label("Loading...", game.getSkin())).expandX().center().row();

        LeaderboardManager.fetchScores(new LeaderboardManager.LeaderboardCallback() {
            @Override
            public void onScoresLoaded(ArrayList<LeaderboardManager.ScoreEntry> scores) {
                leaderboardTable.clear();

                if (bottomRankTable != null) bottomRankTable.clear();

                if (scores.isEmpty()) {
                    leaderboardTable.add(new Label("No records yet!", game.getSkin())).expandX().center();
                    if (bottomRankTable != null) bottomRankTable.add(new Label("You: " + finalScore, game.getSkin())).center();
                } else {
                    boolean userFound = false;
                    String currentUserName = game.getPlayerState().getUsername();
                    int myRank = -1;
                    
                     for (int i = 0; i < scores.size(); i++) {
                        LeaderboardManager.ScoreEntry entry = scores.get(i);

                        Label rankLabel = new Label("#" + (i + 1), game.getSkin());
                        Label entryLabel = new Label(entry.name + ": " + entry.score, game.getSkin());
                        

                        boolean isHighlight = (entry.score == finalScore && entry.name.equals(currentUserName));
                        if (isHighlight) {
                            userFound = true;
                            myRank = i + 1;
                            rankLabel.setColor(com.badlogic.gdx.graphics.Color.GOLD);
                            entryLabel.setColor(com.badlogic.gdx.graphics.Color.GOLD);
                        }
                        
                        Table rowTable = new Table();
                        rowTable.add(rankLabel).padRight(15).right().width(40);
                        rowTable.add(entryLabel).left();


                        leaderboardTable.add(rowTable).expandX().center().padRight(20).padBottom(5).row();
                    }
                    

                    if (bottomRankTable != null) {
                         Label footerRank;
                         if (userFound) {
                             footerRank = new Label("Your Rank: #" + myRank, game.getSkin());
                         } else {
                             footerRank = new Label("Your Rank: Not in Top " + scores.size(), game.getSkin());
                         }
                         Label footerScore = new Label("Score: " + finalScore, game.getSkin());
                         footerRank.setColor(com.badlogic.gdx.graphics.Color.GOLD);
                         footerScore.setColor(com.badlogic.gdx.graphics.Color.GOLD);
                         
                         bottomRankTable.add(footerRank).padRight(20);
                         bottomRankTable.add(footerScore);
                    }
                }
            }

            @Override
            public void onError(String message) {
                 leaderboardTable.clear();
                 leaderboardTable.add(new Label("Error: " + message, game.getSkin())).expandX().center();
            }
        });
    }
}
