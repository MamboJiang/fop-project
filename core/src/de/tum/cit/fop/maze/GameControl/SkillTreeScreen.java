package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.GameObj.PlayerState;
import de.tum.cit.fop.maze.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.graphics.Pixmap;


/**
 * Screen where players can spend XP to upgrade skills.
 */
public class SkillTreeScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private Label xpLabel;
    
    // Background Fields
    private Texture backgroundTexture;
    private Image backgroundImage1;
    private Image backgroundImage2;
    private Texture overlayTexture;
    private Image overlayImage;
    private Image cinematicBarTop;
    private Image cinematicBarBottom;
    private Texture blackTexture;
    private float scrollSpeed = 25f;
    private static final float CINEMATIC_RATIO = 0.125f;

    /**
     * Constructor for SkillTreeScreen.
     * @param game Main game instance.
     */
    public SkillTreeScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1920, 1080));

        // Background Setup
        backgroundTexture = new Texture(Gdx.files.internal("selfmade/background.png"));
        backgroundImage1 = new Image(backgroundTexture);
        backgroundImage2 = new Image(backgroundTexture);
        
        backgroundImage1.setScaling(Scaling.stretch);
        backgroundImage2.setScaling(Scaling.stretch);
        
        // Use saved position from StoryMenu
        backgroundImage1.setSize(stage.getWidth(), stage.getHeight()); 
        backgroundImage2.setSize(stage.getWidth(), stage.getHeight());
        backgroundImage1.setPosition(StoryMenu.savedBackgroundX, 0);
        backgroundImage2.setPosition(StoryMenu.savedBackgroundX + stage.getWidth(), 0);
        
        stage.addActor(backgroundImage1);
        stage.addActor(backgroundImage2);
        
        // Overlay Setup (Black)
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0.6f); 
        p.fill();
        overlayTexture = new Texture(p);
        p.dispose();
        overlayImage = new Image(overlayTexture);
        overlayImage.setSize(stage.getWidth(), stage.getHeight());
        stage.addActor(overlayImage);
        
        // Cinematic Bars
        Pixmap p2 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p2.setColor(Color.BLACK);
        p2.fill();
        blackTexture = new Texture(p2);
        p2.dispose();
        
        cinematicBarTop = new Image(blackTexture);
        cinematicBarBottom = new Image(blackTexture);
        stage.addActor(cinematicBarTop);
        stage.addActor(cinematicBarBottom);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label.LabelStyle titleStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);
        Label.LabelStyle bodyStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);
        
        Label titleLabel = new Label("Skill Tree", titleStyle);
        titleLabel.setFontScale(1.5f); // Make title larger
        table.add(titleLabel).padBottom(50).colspan(2).row();


        xpLabel = new Label("Available XP: " + game.getPlayerState().getCurrentXP(), bodyStyle);
        table.add(xpLabel).padBottom(30).colspan(2).row();

        String attackStatus = game.getPlayerState().isAttackUnlocked() ? "Unlocked" : "Locked";
        Label attackLabel = new Label("Attack Ability: " + attackStatus, bodyStyle);
        table.add(attackLabel).padBottom(30).colspan(2).row();



        createUpgradeRow(table, "Health (+1 Max HP)", "HEALTH");
        createUpgradeRow(table, "Speed (+10% Movement)", "SPEED");
        createUpgradeRow(table, "Defense (+10% Block Chance)", "DEFENSE");


        TextButton backButton = new TextButton("Back to Menu", game.getSkin(), "short");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu(false);
            }
        });
        table.add(backButton).colspan(2).padTop(50);
    }

    private void createUpgradeRow(Table table, String name, String type) {
        PlayerState state = game.getPlayerState();


        int currentLvl = (type.equals("HEALTH") ? state.getHealthLevel() :
                type.equals("SPEED") ? state.getSpeedLevel() : state.getDefenseLevel());

        Label.LabelStyle bodyStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);
        table.add(new Label(name + " (Lvl " + currentLvl + ")", bodyStyle)).left().pad(10);


        int cost = state.getUpgradeCost(type);
        TextButton btn = new TextButton("Upgrade (" + cost + " XP)", game.getSkin(), "short");

        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getPlayerState().upgradeSkill(type)) {

                    game.saveGame();
                    

                    game.setScreen(new SkillTreeScreen(game));
                }
            }
        });


        if (state.getCurrentXP() < cost) {
            btn.setDisabled(true);
            btn.setColor(0.5f, 0.5f, 0.5f, 1f);
        }

        table.add(btn).pad(10).row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        updateBackground(delta);
        
        stage.act(delta);
        stage.draw();
    }
    
    private void updateBackground(float delta) {
        if (backgroundImage1 == null || backgroundImage2 == null) return;
        
        backgroundImage1.setX(backgroundImage1.getX() - scrollSpeed * delta);
        backgroundImage2.setX(backgroundImage2.getX() - scrollSpeed * delta);
        
        // Normalize saved position to ensure consistency across screens
        float w = backgroundImage1.getWidth();
        float currentX = backgroundImage1.getX() % w;
        if (currentX > 0) currentX -= w;
        StoryMenu.savedBackgroundX = currentX;
        
        float width = backgroundImage1.getWidth();
        if (backgroundImage1.getX() + width <= 0) backgroundImage1.setX(backgroundImage2.getX() + width);
        if (backgroundImage2.getX() + width <= 0) backgroundImage2.setX(backgroundImage1.getX() + width);
    }

    @Override
    public void show() { Gdx.input.setInputProcessor(stage); }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        
        float stageW = stage.getWidth();
        float stageH = stage.getHeight();
        
        if (backgroundImage1 != null) backgroundImage1.setSize(stageW, stageH);
        if (backgroundImage2 != null) backgroundImage2.setSize(stageW, stageH);
        if (overlayImage != null) overlayImage.setSize(stageW, stageH);
        
        if (cinematicBarTop != null && cinematicBarBottom != null) {
            float barHeight = stageH * CINEMATIC_RATIO;
            cinematicBarBottom.setSize(stageW, barHeight);
            cinematicBarBottom.setPosition(0, 0);
            cinematicBarTop.setSize(stageW, barHeight);
            cinematicBarTop.setPosition(0, stageH - barHeight);
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (overlayTexture != null) overlayTexture.dispose();
        if (blackTexture != null) blackTexture.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
