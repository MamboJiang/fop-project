package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.MazeRunnerGame;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.graphics.Pixmap;

/**
 * Screen that displays the list of achievements and their status.
 */
public class AchievementsScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final Skin skin;
    
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
     * Constructor for AchievementsScreen.
     * @param game Main game instance.
     */
    public AchievementsScreen(MazeRunnerGame game) {
        this.game = game;
        this.skin = game.getSkin();
        this.stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1920, 1080), game.getSpriteBatch());

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

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.WHITE);
        Label title = new Label("Achievements", titleStyle);
        title.setFontScale(1.5f); // Make title larger
        rootTable.add(title).padBottom(20).row();


        Table listTable = new Table();
        listTable.top();

        java.util.Collection<Achievement> achievements = AchievementManager.getInstance().getAchievements();
        if (achievements.isEmpty()) {
             Label.LabelStyle bodyStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.WHITE);
             listTable.add(new Label("No achievements found or loaded.", bodyStyle)).pad(20).row();
        } else {
            for (Achievement a : achievements) {
                addAchievementRow(listTable, a);
            }
        }

        ScrollPane scrollPane = new ScrollPane(listTable); // No skin = transparent
        // scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        

        scrollPane.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
           @Override
           public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
               stage.setScrollFocus(scrollPane);
           }
           @Override
           public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
               stage.setScrollFocus(null);
           }
        });
        
        rootTable.add(scrollPane).width(800).height(500).padBottom(20).row();

        TextButton backButton = new TextButton("Back", skin, "short");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        rootTable.add(backButton);
    }

    private void addAchievementRow(Table table, Achievement a) {
        Table row = new Table();
        row.setBackground(skin.getDrawable("button"));
        row.pad(10);

        Label.LabelStyle bodyStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.WHITE);
        
        String statusText = a.isUnlocked() ? " [UNLOCKED]" : " [LOCKED]";
        Label nameLabel = new Label(a.getName() + statusText, bodyStyle);
        if (a.isUnlocked()) {
            nameLabel.setColor(Color.GOLD);
        } else {
            nameLabel.setColor(Color.GRAY);
        }
        row.add(nameLabel).expandX().left().row();
        

        Label descLabel = new Label(a.getDescription(), bodyStyle);
        descLabel.setFontScale(0.8f);
        descLabel.setWrap(true);
        row.add(descLabel).expandX().left().width(700).padTop(5).row();
        

        int current = a.getProgress();
        int target = a.getTarget();
        if (current > target) current = target;
        
        String progressText = "Progress: " + current + " / " + target;
        Label progressLabel = new Label(progressText, bodyStyle);
        progressLabel.setColor(Color.LIGHT_GRAY);
        row.add(progressLabel).expandX().left().padTop(5).row();

        table.add(row).width(750).padBottom(10).row();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (overlayTexture != null) overlayTexture.dispose();
        if (blackTexture != null) blackTexture.dispose();
    }
}
