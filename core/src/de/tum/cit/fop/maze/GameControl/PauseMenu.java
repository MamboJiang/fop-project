package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.tum.cit.fop.maze.MazeRunnerGame;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;

/**
 * UI Overlay for the Pause menu.
 */
public class PauseMenu extends Table implements com.badlogic.gdx.utils.Disposable {
    
    private final MazeRunnerGame game;
    private Runnable onResume;
    private Runnable onExit;
    
    private Label scoreLabel;
    private Label difficultyLabel;
    private Label xpLabel;
    
    // Assets
    private com.badlogic.gdx.graphics.Texture menuBgTex;
    private com.badlogic.gdx.graphics.Texture titleBgTex; // buttontype2
    private com.badlogic.gdx.graphics.Texture btnUpTex; // buttonbaseshort
    private com.badlogic.gdx.graphics.Texture btnDownTex; // buttonpressedshort
    private com.badlogic.gdx.graphics.Texture btnOverTex; // buttononshort
    
    private BitmapFont titleFont;
    private BitmapFont regularFont;

    /**
     * Constructor for PauseMenu.
     * @param game Game instance.
     * @param onResume Callback for resume.
     * @param onExit Callback for exit.
     */
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
        
        // Load Custom Assets
        menuBgTex = new com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal("assets/selfmade/uielements/menuscreenxxxx.png"));
        titleBgTex = new com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal("assets/selfmade/uielements/buttontype2.png"));
        
        // Switched to 'middle' button style
        btnUpTex = new com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal("assets/selfmade/uielements/buttonbasemiddle.png"));
        btnDownTex = new com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal("assets/selfmade/uielements/buttonpressedmiddle.png"));
        btnOverTex = new com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal("assets/selfmade/uielements/buttononmiddle.png"));
        
        // Lighter Blue color
        com.badlogic.gdx.graphics.Color textColor = com.badlogic.gdx.graphics.Color.valueOf("6699CC");

        // Load Font (Hoefler)
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(com.badlogic.gdx.Gdx.files.internal("assets/other/Hoefler Text Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        
        // Title Font
        parameter.size = 36;
        parameter.color = textColor;
        titleFont = generator.generateFont(parameter);
        
        // Regular Text Font
        parameter.size = 34;
        parameter.color = textColor;
        regularFont = generator.generateFont(parameter);
        
        generator.dispose(); 

        // Main Container (Darkened background for full screen - Even Deeper Blue)
        Drawable dimBg = skin.newDrawable("white", 0.01f, 0.02f, 0.1f, 0.95f);
        setBackground(dimBg);

        // Content Table (The actual menu image)
        Table content = new Table();
        content.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(menuBgTex)));
        
        // Title Section
        Table titleTable = new Table();
        titleTable.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(titleBgTex)));
        
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, textColor);
        titleTable.add(new Label("- Pause Menu -", titleStyle)).padBottom(10); 
        
        // Moved title further up (more negative padding)
        content.add(titleTable).padTop(-250).padBottom(-40).row();
        
        // Stats
        Label.LabelStyle infoStyle = new Label.LabelStyle(regularFont, textColor);
        scoreLabel = new Label("Score: 0", infoStyle);
        difficultyLabel = new Label("Difficulty: 1", infoStyle);
        xpLabel = new Label("XP: 0", infoStyle);
        
        content.add(scoreLabel).pad(1).padTop(-15).row(); // Moved up
        content.add(difficultyLabel).pad(1).row();
        content.add(xpLabel).pad(1).padBottom(5).row(); // Reduced bottom padding to move buttons up
        
        // Buttons Style
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(btnUpTex));
        btnStyle.down = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(btnDownTex));
        btnStyle.over = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(btnOverTex));
        btnStyle.font = regularFont;
        btnStyle.fontColor = textColor;
        
        TextButton resumeBtn = new TextButton("Resume", btnStyle);
        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onResume != null) onResume.run();
                setVisible(false);
            }
        });
        // Middle buttons are wider, adjusted size to 300x80 (approx ratio)
        content.add(resumeBtn).pad(5).width(300).height(80).row();
        
        TextButton exitBtn = new TextButton("Exit to Menu", btnStyle);
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
        content.add(exitBtn).pad(5).width(300).height(80).row();
        
        // Add content table, scale down size to 55%
        add(content).size(menuBgTex.getWidth() * 0.55f, menuBgTex.getHeight() * 0.55f).center();
    }
    
    public void show() {
        setVisible(true);
        toFront();
    }
    
    public void hide() {
        setVisible(false);
    }
    
    /**
     * Updates stats displayed in the pause menu.
     * @param score Current score.
     * @param difficulty Current difficulty.
     * @param xp Total XP.
     */
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

    @Override
    public void dispose() {
        if (menuBgTex != null) menuBgTex.dispose();
        if (titleBgTex != null) titleBgTex.dispose();
        if (btnUpTex != null) btnUpTex.dispose();
        if (btnDownTex != null) btnDownTex.dispose();
        if (btnOverTex != null) btnOverTex.dispose();
        if (titleFont != null) titleFont.dispose();
        if (regularFont != null) regularFont.dispose();
    }
}
