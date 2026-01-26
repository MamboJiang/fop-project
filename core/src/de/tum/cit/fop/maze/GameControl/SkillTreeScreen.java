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
import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.GameObj.PlayerState;
import de.tum.cit.fop.maze.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

/**
 * Screen where players can spend XP to upgrade skills.
 * Features a scrolling background and achievement popup notifications.
 */
public class SkillTreeScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    /** Root table for holding all UI content. */
    private Table rootTable;

    private Label xpLabel;


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

    /** Achievement popup resources. */
    private Texture objectsTexture;
    /** Nine-patch drawable for achievement popup background. */
    private NinePatch achievementNinePatch;

    /**
     * Constructor for SkillTreeScreen.
     *
     * @param game Main game instance.
     */
    public SkillTreeScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1920, 1080));

        loadAssets();

        backgroundTexture = new Texture(Gdx.files.internal("selfmade/background.png"));
        backgroundImage1 = new Image(backgroundTexture);
        backgroundImage2 = new Image(backgroundTexture);

        backgroundImage1.setScaling(Scaling.stretch);
        backgroundImage2.setScaling(Scaling.stretch);

        backgroundImage1.setSize(stage.getWidth(), stage.getHeight());
        backgroundImage2.setSize(stage.getWidth(), stage.getHeight());
        backgroundImage1.setPosition(StoryMenu.savedBackgroundX, 0);
        backgroundImage2.setPosition(StoryMenu.savedBackgroundX + stage.getWidth(), 0);

        stage.addActor(backgroundImage1);
        stage.addActor(backgroundImage2);

        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0.6f);
        p.fill();
        overlayTexture = new Texture(p);
        p.dispose();
        overlayImage = new Image(overlayTexture);
        overlayImage.setSize(stage.getWidth(), stage.getHeight());
        stage.addActor(overlayImage);

        Pixmap p2 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p2.setColor(Color.BLACK);
        p2.fill();
        blackTexture = new Texture(p2);
        p2.dispose();

        cinematicBarTop = new Image(blackTexture);
        cinematicBarBottom = new Image(blackTexture);
        stage.addActor(cinematicBarTop);
        stage.addActor(cinematicBarBottom);

        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        rebuildUI();
    }

    /**
     * Loads assets for achievement popup display.
     * Extracts the popup background region from objects.png texture.
     */
    private void loadAssets() {
        try {
            objectsTexture = new Texture(Gdx.files.internal("objects.png"));
            com.badlogic.gdx.graphics.g2d.TextureRegion bgRegion = new com.badlogic.gdx.graphics.g2d.TextureRegion(
                    objectsTexture, 4 * 16, 18 * 16, 4 * 16, 2 * 16);
            achievementNinePatch = new NinePatch(bgRegion, 16, 16, 0, 0);
            achievementNinePatch.scale(4, 4);
        } catch (Exception e) {
            Gdx.app.error("SkillTree", "Failed to load popup assets: " + e.getMessage());
        }
    }

    /**
     * Clears and rebuilds the entire UI in-place without destroying the screen.
     * This allows for live updates when skills are upgraded.
     */
    private void rebuildUI() {
        rootTable.clear();

        Label.LabelStyle titleStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);
        Label.LabelStyle bodyStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);

        Label titleLabel = new Label("Skill Tree", titleStyle);
        titleLabel.setFontScale(1.5f);
        rootTable.add(titleLabel).padBottom(50).colspan(2).row();

        xpLabel = new Label("Available XP: " + game.getPlayerState().getCurrentXP(), bodyStyle);
        rootTable.add(xpLabel).padBottom(30).colspan(2).row();

        String attackStatus = game.getPlayerState().isAttackUnlocked() ? "Unlocked" : "Locked";
        Label attackLabel = new Label("Attack Ability: " + attackStatus, bodyStyle);
        rootTable.add(attackLabel).padBottom(30).colspan(2).row();

        createUpgradeRow(rootTable, "Health (+1 Max HP)", "HEALTH");
        createUpgradeRow(rootTable, "Speed (+10% Movement)", "SPEED");
        createUpgradeRow(rootTable, "Defense (+10% Block Chance)", "DEFENSE");

        TextButton backButton = new TextButton("Back to Menu", game.getSkin(), "short");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu(false);
            }
        });
        rootTable.add(backButton).colspan(2).padTop(50);
    }

    /**
     * Creates a row in the upgrade table for a specific skill.
     *
     * @param table The UI table.
     * @param name  Display name of the skill.
     * @param type  Internal skill type identifier.
     */
    private void createUpgradeRow(Table table, String name, String type) {
        PlayerState state = game.getPlayerState();

        int currentLvl = (type.equals("HEALTH") ? state.getHealthLevel()
                : type.equals("SPEED") ? state.getSpeedLevel() : state.getDefenseLevel());

        Label.LabelStyle bodyStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);
        table.add(new Label(name + " (Lvl " + currentLvl + ")", bodyStyle)).left().pad(10);

        int cost = state.getUpgradeCost(type);
        TextButton btn = new TextButton("Upgrade (" + cost + " XP)", game.getSkin(), "short");

        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean wasPowerUpUnlocked = isAchievementUnlocked("power_up");

                if (game.getPlayerState().upgradeSkill(type)) {
                    AchievementManager.getInstance().onEvent(EventType.UPGRADE_SKILL, 1);
                    game.saveGame();

                    boolean isPowerUpUnlockedNow = isAchievementUnlocked("power_up");

                    if (!wasPowerUpUnlocked && isPowerUpUnlockedNow) {
                        showLocalAchievementPopup(AchievementManager.getInstance().getAchievements()
                                .stream().filter(a -> a.getId().equals("power_up")).findFirst().orElse(null));
                    }

                    rebuildUI();
                }
            }
        });

        if (state.getCurrentXP() < cost) {
            btn.setDisabled(true);
            btn.setColor(0.5f, 0.5f, 0.5f, 1f);
        }

        table.add(btn).pad(10).row();
    }

    /**
     * Checks if an achievement with the given ID is unlocked.
     *
     * @param id Achievement ID to check.
     * @return True if the achievement is unlocked, false otherwise.
     */
    private boolean isAchievementUnlocked(String id) {
        for (Achievement a : AchievementManager.getInstance().getAchievements()) {
            if (a.getId().equals(id)) {
                return a.isUnlocked();
            }
        }
        return false;
    }

    /**
     * Displays an achievement popup on the current stage.
     *
     * @param achievement The achievement to display.
     */
    private void showLocalAchievementPopup(Achievement achievement) {
        if (achievement == null || achievementNinePatch == null)
            return;

        AchievementPopup popup = new AchievementPopup(achievement, game.getSkin(), achievementNinePatch);
        stage.addActor(popup);
        popup.toFront();
        popup.animate();
    }

    /**
     * Inner class for displaying achievement unlock notifications.
     * Adapted from HUD implementation.
     */
    private static class AchievementPopup extends Table {
        /**
         * Constructs an achievement popup.
         *
         * @param achievement The achievement to display.
         * @param skin        The UI skin.
         * @param bgPatch     The nine-patch background.
         */
        public AchievementPopup(Achievement achievement, com.badlogic.gdx.scenes.scene2d.ui.Skin skin,
                NinePatch bgPatch) {
            this.setBackground(new NinePatchDrawable(bgPatch));
            this.setSize(340, 128);
            this.setPosition((1920 - 340) / 2f, 1080 + 10);

            Label titleLabel = new Label("Achievement!", skin);
            titleLabel.setFontScale(0.8f);
            this.add(titleLabel).padTop(0).padLeft(100).row();

            Label nameLabel = new Label(achievement.getName(), skin);
            nameLabel.setFontScale(0.8f);
            this.add(nameLabel).padTop(-5).padLeft(100);
        }

        /**
         * Animates the popup with slide-in, delay, and slide-out effects.
         */
        public void animate() {
            this.addAction(Actions.sequence(
                    Actions.moveTo(this.getX(), 1080 - 150, 0.5f, Interpolation.swingOut),
                    Actions.delay(3f),
                    Actions.moveTo(this.getX(), 1080 + 10, 0.5f, Interpolation.swingIn),
                    Actions.removeActor()));
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateBackground(delta);

        stage.act(delta);
        stage.draw();
    }

    /**
     * Updates the scrolling background animation.
     *
     * @param delta Time elapsed since last frame.
     */
    private void updateBackground(float delta) {
        if (backgroundImage1 == null || backgroundImage2 == null)
            return;

        backgroundImage1.setX(backgroundImage1.getX() - scrollSpeed * delta);
        backgroundImage2.setX(backgroundImage2.getX() - scrollSpeed * delta);

        float w = backgroundImage1.getWidth();
        float currentX = backgroundImage1.getX() % w;
        if (currentX > 0)
            currentX -= w;
        StoryMenu.savedBackgroundX = currentX;

        float width = backgroundImage1.getWidth();
        if (backgroundImage1.getX() + width <= 0)
            backgroundImage1.setX(backgroundImage2.getX() + width);
        if (backgroundImage2.getX() + width <= 0)
            backgroundImage2.setX(backgroundImage1.getX() + width);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        float stageW = stage.getWidth();
        float stageH = stage.getHeight();
        if (backgroundImage1 != null)
            backgroundImage1.setSize(stageW, stageH);
        if (backgroundImage2 != null)
            backgroundImage2.setSize(stageW, stageH);
        if (overlayImage != null)
            overlayImage.setSize(stageW, stageH);
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
        if (backgroundTexture != null)
            backgroundTexture.dispose();
        if (overlayTexture != null)
            overlayTexture.dispose();
        if (blackTexture != null)
            blackTexture.dispose();
        if (objectsTexture != null)
            objectsTexture.dispose();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}