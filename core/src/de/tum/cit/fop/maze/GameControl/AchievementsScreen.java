package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * Screen that displays the list of achievements and their status.
 */
public class AchievementsScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final Skin skin;

    /**
     * Constructor for AchievementsScreen.
     * @param game Main game instance.
     */
    public AchievementsScreen(MazeRunnerGame game) {
        this.game = game;
        this.skin = game.getSkin();
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        Label title = new Label("Achievements", skin, "title");
        rootTable.add(title).padBottom(20).row();


        Table listTable = new Table();
        listTable.top();

        java.util.Collection<Achievement> achievements = AchievementManager.getInstance().getAchievements();
        if (achievements.isEmpty()) {
             listTable.add(new Label("No achievements found or loaded.", skin)).pad(20).row();
        } else {
            for (Achievement a : achievements) {
                addAchievementRow(listTable, a);
            }
        }

        ScrollPane scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
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

        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        rootTable.add(backButton).width(200);
    }

    private void addAchievementRow(Table table, Achievement a) {
        Table row = new Table();
        row.setBackground(skin.getDrawable("button"));
        row.pad(10);

        String statusText = a.isUnlocked() ? " [UNLOCKED]" : " [LOCKED]";
        Label nameLabel = new Label(a.getName() + statusText, skin);
        if (a.isUnlocked()) {
            nameLabel.setColor(Color.GOLD);
        } else {
            nameLabel.setColor(Color.GRAY);
        }
        row.add(nameLabel).expandX().left().row();
        

        Label descLabel = new Label(a.getDescription(), skin);
        descLabel.setFontScale(0.8f);
        descLabel.setWrap(true);
        row.add(descLabel).expandX().left().width(700).padTop(5).row();
        

        int current = a.getProgress();
        int target = a.getTarget();
        if (current > target) current = target;
        
        String progressText = "Progress: " + current + " / " + target;
        Label progressLabel = new Label(progressText, skin);
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
