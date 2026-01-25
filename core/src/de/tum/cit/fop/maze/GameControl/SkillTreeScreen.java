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
 */
public class SkillTreeScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private Table rootTable; // 用于放置 UI 内容的根表格

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

    // Achievement Popup Resources
    private Texture objectsTexture; // 加载 objects.png
    private NinePatch achievementNinePatch; // 弹窗背景

    /**
     * Constructor for SkillTreeScreen.
     * @param game Main game instance.
     */
    public SkillTreeScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1920, 1080));

        loadAssets(); // 加载资源

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

        // 初始化根表格
        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // 构建 UI
        rebuildUI();
    }

    private void loadAssets() {
        // 加载用于弹窗的资源（复用 HUD 中的逻辑）
        try {
            objectsTexture = new Texture(Gdx.files.internal("objects.png"));
            // 提取弹窗背景区域
            com.badlogic.gdx.graphics.g2d.TextureRegion bgRegion = new com.badlogic.gdx.graphics.g2d.TextureRegion(objectsTexture, 4 * 16, 18 * 16, 4 * 16, 2 * 16);
            achievementNinePatch = new NinePatch(bgRegion, 16, 16, 0, 0);
            achievementNinePatch.scale(4, 4); // 保持和 HUD 一致的缩放
        } catch (Exception e) {
            Gdx.app.error("SkillTree", "Failed to load popup assets: " + e.getMessage());
        }
    }

    /**
     * 核心方法：清空并重新构建 UI，实现“原地刷新”而不销毁屏幕
     */
    private void rebuildUI() {
        rootTable.clear(); // 清空旧内容

        Label.LabelStyle titleStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);
        Label.LabelStyle bodyStyle = new Label.LabelStyle(game.getSkin().getFont("hoefler"), Color.WHITE);

        Label titleLabel = new Label("Skill Tree", titleStyle);
        titleLabel.setFontScale(1.5f);
        rootTable.add(titleLabel).padBottom(50).colspan(2).row();

        Label xpLabel = new Label("Available XP: " + game.getPlayerState().getCurrentXP(), bodyStyle);
        rootTable.add(xpLabel).padBottom(30).colspan(2).row();

        String attackStatus = game.getPlayerState().isAttackUnlocked() ? "Unlocked" : "Locked";
        Label attackLabel = new Label("Attack Ability: " + attackStatus, bodyStyle);
        rootTable.add(attackLabel).padBottom(30).colspan(2).row();

        // 重新添加行
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
                // 1. 检查升级前 "Power Up" 是否已解锁
                boolean wasPowerUpUnlocked = isAchievementUnlocked("power_up");

                if (game.getPlayerState().upgradeSkill(type)) {
                    // 2. 触发事件 (逻辑层面解锁)
                    AchievementManager.getInstance().onEvent(EventType.UPGRADE_SKILL, 1);
                    game.saveGame();

                    // 3. 检查升级后是否刚解锁
                    boolean isPowerUpUnlockedNow = isAchievementUnlocked("power_up");

                    if (!wasPowerUpUnlocked && isPowerUpUnlockedNow) {
                        // 刚解锁 -> 手动显示弹窗
                        showLocalAchievementPopup(AchievementManager.getInstance().getAchievements()
                                .stream().filter(a -> a.getId().equals("power_up")).findFirst().orElse(null));
                    }

                    // 4. 原地刷新 UI，而不是 setScreen(new ...)
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

    // 辅助方法：检查成就状态
    private boolean isAchievementUnlocked(String id) {
        for (Achievement a : AchievementManager.getInstance().getAchievements()) {
            if (a.getId().equals(id)) {
                return a.isUnlocked();
            }
        }
        return false;
    }

    // 在当前 Stage 显示弹窗
    private void showLocalAchievementPopup(Achievement achievement) {
        if (achievement == null || achievementNinePatch == null) return;

        AchievementPopup popup = new AchievementPopup(achievement, game.getSkin(), achievementNinePatch);
        stage.addActor(popup);
        popup.toFront();
        popup.animate();
    }

    // 内部类：AchievementPopup (从 HUD 复制并适配)
    private static class AchievementPopup extends Table {
        public AchievementPopup(Achievement achievement, com.badlogic.gdx.scenes.scene2d.ui.Skin skin, NinePatch bgPatch) {
            this.setBackground(new NinePatchDrawable(bgPatch));
            this.setSize(340, 128);
            this.setPosition((1920 - 340) / 2f, 1080 + 10); // 初始位置在屏幕上方

            Label titleLabel = new Label("Achievement!", skin);
            titleLabel.setFontScale(0.8f);
            this.add(titleLabel).padTop(0).padLeft(100).row();

            Label nameLabel = new Label(achievement.getName(), skin);
            nameLabel.setFontScale(0.8f);
            this.add(nameLabel).padTop(-5).padLeft(100);
        }

        public void animate() {
            this.addAction(Actions.sequence(
                    Actions.moveTo(this.getX(), 1080 - 150, 0.5f, Interpolation.swingOut), // 滑入
                    Actions.delay(3f), // 停留
                    Actions.moveTo(this.getX(), 1080 + 10, 0.5f, Interpolation.swingIn), // 滑出
                    Actions.removeActor() // 移除
            ));
        }
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

        float w = backgroundImage1.getWidth();
        float currentX = backgroundImage1.getX() % w;
        if (currentX > 0) currentX -= w;
        StoryMenu.savedBackgroundX = currentX;

        float width = backgroundImage1.getWidth();
        if (backgroundImage1.getX() + width <= 0) backgroundImage1.setX(backgroundImage2.getX() + width);
        if (backgroundImage2.getX() + width <= 0) backgroundImage2.setX(backgroundImage1.getX() + width);
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }

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
        if (objectsTexture != null) objectsTexture.dispose(); // 记得释放新加载的资源
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}