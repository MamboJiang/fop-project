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
import de.tum.cit.fop.maze.GameObj.PlayerState;
import de.tum.cit.fop.maze.*;


public class SkillTreeScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private Label xpLabel;

    public SkillTreeScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Title
        table.add(new Label("Skill Tree", game.getSkin(), "title")).padBottom(50).colspan(2).row();

        // XP Display
        xpLabel = new Label("Available XP: " + game.getPlayerState().getCurrentXP(), game.getSkin());
        table.add(xpLabel).padBottom(30).colspan(2).row();

        // Create Upgrade Rows
        createUpgradeRow(table, "Health (+1 Max HP)", "HEALTH");
        createUpgradeRow(table, "Speed (+10% Movement)", "SPEED");
        createUpgradeRow(table, "Defense (+10% Block Chance)", "DEFENSE");

        // Back Button
        TextButton backButton = new TextButton("Back to Menu", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        table.add(backButton).width(300).colspan(2).padTop(50);
    }

    private void createUpgradeRow(Table table, String name, String type) {
        PlayerState state = game.getPlayerState();

        // Skill Name + Current Level
        int currentLvl = (type.equals("HEALTH") ? state.getHealthLevel() :
                type.equals("SPEED") ? state.getSpeedLevel() : state.getDefenseLevel());

        table.add(new Label(name + " (Lvl " + currentLvl + ")", game.getSkin())).left().pad(10);

        // Upgrade Button
        int cost = state.getUpgradeCost(type);
        TextButton btn = new TextButton("Upgrade (" + cost + " XP)", game.getSkin());

        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getPlayerState().upgradeSkill(type)) {
                    // Update persistence immediately
                    game.saveGame();
                    
                    // Refresh screen logic simply by recreating it or updating labels
                    game.setScreen(new SkillTreeScreen(game));
                }
            }
        });

        // Disable if not enough XP
        if (state.getCurrentXP() < cost) {
            btn.setDisabled(true);
            btn.setColor(0.5f, 0.5f, 0.5f, 1f); // Grey out
        }

        table.add(btn).width(200).pad(10).row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() { Gdx.input.setInputProcessor(stage); }

    @Override
    public void resize(int width, int height) { stage.getViewport().update(width, height, true); }

    @Override
    public void dispose() { stage.dispose(); }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
