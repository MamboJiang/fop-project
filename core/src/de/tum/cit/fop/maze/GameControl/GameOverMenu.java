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

/**
 * UI Element (Table) displayed when the game ends (Win or Lose).
 * Shows score, XP, leaderboard, and navigation buttons.
 */
public class GameOverMenu extends Table implements com.badlogic.gdx.utils.Disposable {
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

    private com.badlogic.gdx.graphics.Texture menuBgTex;
    private com.badlogic.gdx.graphics.Texture titleBgTex;
    private com.badlogic.gdx.graphics.Texture btnUpTex;
    private com.badlogic.gdx.graphics.Texture btnDownTex;
    private com.badlogic.gdx.graphics.Texture btnOverTex;

    private com.badlogic.gdx.graphics.g2d.BitmapFont titleFont;
    private com.badlogic.gdx.graphics.g2d.BitmapFont regularFont;
    private com.badlogic.gdx.graphics.g2d.BitmapFont leaderboardFont;

    public GameOverMenu(MazeRunnerGame game, Runnable onRetry, Runnable onExit, Runnable onNextLevel, boolean isWin,
            int finalScore, int xp) {
        this(game, onRetry, onExit, onNextLevel, isWin, -1, finalScore, xp);
    }

    /**
     * Constructor for GameOverMenu.
     * 
     * @param game         Main game instance.
     * @param onRetry      Callback for retry.
     * @param onExit       Callback for exit.
     * @param onNextLevel  Callback for next level.
     * @param isWin        True if level was completed successfully.
     * @param wavesCleared Number of waves cleared (if endless mode), or -1.
     * @param finalScore   The final score achieved.
     * @param xp           The XP gained.
     */
    public GameOverMenu(MazeRunnerGame game, Runnable onRetry, Runnable onExit, Runnable onNextLevel, boolean isWin,
            int wavesCleared, int finalScore, int xp) {
        this.game = game;
        this.onRetry = onRetry;
        this.onExit = onExit;
        this.isWin = isWin;
        this.onNextLevel = onNextLevel;
        this.wavesCleared = wavesCleared;
        this.finalScore = finalScore;
        this.xp = xp;

        setFillParent(true);
        setVisible(false);

        setupUI();
    }

    /**
     * Initializes the UI components.
     */
    private void setupUI() {
        Skin skin = game.getSkin();

        menuBgTex = new com.badlogic.gdx.graphics.Texture(
                com.badlogic.gdx.Gdx.files.internal("selfmade/uielements/menuscreenxxxx.png"));
        titleBgTex = new com.badlogic.gdx.graphics.Texture(
                com.badlogic.gdx.Gdx.files.internal("selfmade/uielements/buttontype2.png"));

        btnUpTex = new com.badlogic.gdx.graphics.Texture(
                com.badlogic.gdx.Gdx.files.internal("selfmade/uielements/buttonbasemiddle.png"));
        btnDownTex = new com.badlogic.gdx.graphics.Texture(
                com.badlogic.gdx.Gdx.files.internal("selfmade/uielements/buttonpressedmiddle.png"));
        btnOverTex = new com.badlogic.gdx.graphics.Texture(
                com.badlogic.gdx.Gdx.files.internal("selfmade/uielements/buttononmiddle.png"));

        com.badlogic.gdx.graphics.Color textColor = com.badlogic.gdx.graphics.Color.valueOf("6699CC");

        com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator generator = new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator(
                com.badlogic.gdx.Gdx.files.internal("other/Hoefler Text Regular.ttf"));
        com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter parameter = new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 36;
        parameter.color = textColor;
        titleFont = generator.generateFont(parameter);

        parameter.size = 34;
        parameter.color = textColor;
        regularFont = generator.generateFont(parameter);

        parameter.size = 24;
        leaderboardFont = generator.generateFont(parameter);

        generator.dispose();

        Drawable dimBg = skin.newDrawable("white", 0.01f, 0.02f, 0.1f, 0.95f);
        setBackground(dimBg);

        Table content = new Table();
        content.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(menuBgTex)));

        Table titleTable = new Table();
        titleTable.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(titleBgTex)));

        String titleText = isWin ? "LEVEL CLEARED!" : "GAME OVER";
        if (wavesCleared >= 0 && !isWin) {
            titleText = "RUN ENDED";
        }

        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, textColor);
        titleTable.add(new Label(titleText, titleStyle)).padBottom(10);

        content.add(titleTable).padTop(-200).padBottom(-20).row();

        Label.LabelStyle infoStyle = new Label.LabelStyle(regularFont, textColor);

        Label scoreLabel = new Label("Score: " + finalScore, infoStyle);
        Label xpLabel = new Label("XP gained: " + xp, infoStyle);

        bottomRankTable = new Table();

        if (wavesCleared >= 0 && !isWin) {

            Label wavesLabel = new Label("Waves Cleared: " + wavesCleared, infoStyle);
            content.add(wavesLabel).pad(1).row();
            content.add(scoreLabel).pad(1).row();

            leaderboardTable = new Table();
            leaderboardTable.top();

            com.badlogic.gdx.scenes.scene2d.ui.ScrollPane scrollPane = new com.badlogic.gdx.scenes.scene2d.ui.ScrollPane(
                    leaderboardTable, skin);
            scrollPane.setFadeScrollBars(false);


            content.add(scrollPane).width(450).height(200).pad(10).row();
            content.add(bottomRankTable).growX().pad(5).row();

        } else {
            content.add(scoreLabel).pad(1).row();
            content.add(xpLabel).pad(1).padBottom(5).row();
        }

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(btnUpTex));
        btnStyle.down = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(btnDownTex));
        btnStyle.over = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(btnOverTex));
        btnStyle.font = regularFont;
        btnStyle.fontColor = textColor;

        TextButton retryBtn = new TextButton(wavesCleared >= 0 ? "Restart Run" : "Retry Level", btnStyle);
        retryBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onRetry != null)
                    onRetry.run();
                setVisible(false);
            }
        });

        TextButton exitBtn = new TextButton(wavesCleared >= 0 ? "Quit" : "Main Menu", btnStyle);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onExit != null)
                    onExit.run();
                else
                    game.goToMenu();
            }
        });

        TextButton nextLevelBtn = new TextButton(wavesCleared >= 0 ? "Next Wave" : "Next Level", btnStyle);
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
            if (wavesCleared >= 0) {
                content.add(retryBtn).width(300).height(80).pad(5).row();
                content.add(exitBtn).width(300).height(80).pad(5).row();
            } else {
                content.add(retryBtn).width(300).height(80).pad(5).row();
                content.add(exitBtn).width(300).height(80).pad(5).row();
            }
        } else {
            content.add(nextLevelBtn).width(300).height(80).pad(5).row();
            if (wavesCleared == -1) {
                content.add(retryBtn).width(300).height(80).pad(5).row();
            }
            content.add(exitBtn).width(300).height(80).pad(5).row();
        }

        com.badlogic.gdx.scenes.scene2d.ui.Cell cell = add(content)
                .size(menuBgTex.getWidth() * 0.55f, menuBgTex.getHeight() * 0.55f).center();

        if (wavesCleared >= 0) {
            cell.padTop(200);
            content.padBottom(200);
        }
    }

    public void show() {
        setVisible(true);
        toFront();
    }

    public void hide() {
        setVisible(false);
    }

    /**
     * Fetches and displays the leaderboard scores.
     */
    public void loadLeaderboard() {
        if (leaderboardTable == null)
            return;

        leaderboardTable.clear();
        Label loading = new Label("Loading...", game.getSkin());
        loading.setStyle(new Label.LabelStyle(leaderboardFont, com.badlogic.gdx.graphics.Color.valueOf("6699CC")));
        leaderboardTable.add(loading).expandX().center().row();

        LeaderboardManager.fetchScores(new LeaderboardManager.LeaderboardCallback() {
            @Override
            public void onScoresLoaded(ArrayList<LeaderboardManager.ScoreEntry> scores) {
                leaderboardTable.clear();

                if (bottomRankTable != null)
                    bottomRankTable.clear();

                com.badlogic.gdx.graphics.Color textColor = com.badlogic.gdx.graphics.Color.valueOf("6699CC");
                Label.LabelStyle lbStyle = new Label.LabelStyle(leaderboardFont, textColor);

                if (scores.isEmpty()) {
                    leaderboardTable.add(new Label("No records yet!", lbStyle)).expandX().center();
                    if (bottomRankTable != null)
                        bottomRankTable.add(new Label("You: " + finalScore, lbStyle)).center();
                } else {
                    boolean userFound = false;
                    String currentUserName = game.getPlayerState().getUsername();
                    int myRank = -1;

                    for (int i = 0; i < scores.size(); i++) {
                        LeaderboardManager.ScoreEntry entry = scores.get(i);

                        Label rankLabel = new Label("#" + (i + 1), lbStyle);
                        Label entryLabel = new Label(entry.name + ": " + entry.score, lbStyle);

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
                            footerRank = new Label("Your Rank: #" + myRank, lbStyle);
                        } else {
                            footerRank = new Label("Your Rank: Not in Top " + scores.size(), lbStyle);
                        }
                        Label footerScore = new Label("Score: " + finalScore, lbStyle);
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
                Label error = new Label("Error: " + message, game.getSkin());
                error.setStyle(new Label.LabelStyle(leaderboardFont, com.badlogic.gdx.graphics.Color.RED));
                leaderboardTable.add(error).expandX().center();
            }
        });
    }

    @Override
    public void dispose() {
        if (menuBgTex != null)
            menuBgTex.dispose();
        if (titleBgTex != null)
            titleBgTex.dispose();
        if (btnUpTex != null)
            btnUpTex.dispose();
        if (btnDownTex != null)
            btnDownTex.dispose();
        if (btnOverTex != null)
            btnOverTex.dispose();
        if (titleFont != null)
            titleFont.dispose();
        if (regularFont != null)
            regularFont.dispose();
        if (leaderboardFont != null)
            leaderboardFont.dispose();
    }
}
