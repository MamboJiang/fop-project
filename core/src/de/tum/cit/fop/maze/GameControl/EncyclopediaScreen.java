package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport; // 推荐使用ExtendViewport适配不同屏幕
import com.badlogic.gdx.math.Interpolation;
import de.tum.cit.fop.maze.MazeRunnerGame;
import java.util.Map;

public class EncyclopediaScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final Skin skin;
    private final EncyclopediaManager encyclopediaManager;

    // --- UI Assets (与 StoryMenu/PauseMenu 保持一致) ---
    private Texture backgroundTexture;
    private Image backgroundImage1;
    private Image backgroundImage2;
    private float scrollSpeed = 25f;

    private Texture blackTexture;
    private Image cinematicBarTop;
    private Image cinematicBarBottom;
    private static final float CINEMATIC_RATIO = 0.125f;

    private Texture titleBgTexture; // buttontype2.png
    private Texture cardBgTexture;  // 半透明背景用于卡片

    private Texture detailPanelTexture;

    // 字体样式
    private Label.LabelStyle titleStyle;
    private Label.LabelStyle bodyStyle;
    private Color themeColor = Color.valueOf("6699CC"); // 统一的淡蓝色

    public EncyclopediaScreen(MazeRunnerGame game) {
        this.game = game;
        this.skin = game.getSkin();
        this.encyclopediaManager = EncyclopediaManager.getInstance();

        // 使用 ExtendViewport 1920x1080 (与 StoryMenu 保持一致)
        this.stage = new Stage(new ExtendViewport(1920, 1080), game.getSpriteBatch());

        loadAssets();
        setupStyles();
        buildUI();

        // 更新主角名字
        if (this.encyclopediaManager.getAllEntries().containsKey("main_character")) {
            this.encyclopediaManager.getAllEntries().get("main_character").setName(game.getPlayerState().getUsername());
        }
    }

    private void loadAssets() {
        // 1. 背景 (复用 StoryMenu 的逻辑)
        backgroundTexture = new Texture(Gdx.files.internal("selfmade/background.png"));
        backgroundImage1 = new Image(backgroundTexture);
        backgroundImage2 = new Image(backgroundTexture);
        backgroundImage1.setScaling(Scaling.stretch);
        backgroundImage2.setScaling(Scaling.stretch);
        detailPanelTexture = new Texture(Gdx.files.internal("selfmade/uielements/levelbuttonbase.png"));

        // 保持背景位置同步
        backgroundImage1.setPosition(StoryMenu.savedBackgroundX, 0);
        backgroundImage2.setPosition(StoryMenu.savedBackgroundX + stage.getWidth(), 0); // 初始宽度可能不准，resize会修正

        stage.addActor(backgroundImage1);
        stage.addActor(backgroundImage2);

        // 2. 电影黑边
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        blackTexture = new Texture(pixmap);

        cinematicBarTop = new Image(blackTexture);
        cinematicBarBottom = new Image(blackTexture);
        stage.addActor(cinematicBarTop);
        stage.addActor(cinematicBarBottom);

        // 3. UI 元素纹理
        titleBgTexture = new Texture(Gdx.files.internal("selfmade/uielements/buttontype2.png"));

        // 创建一个半透明的黑色背景用于卡片
        Pixmap p2 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p2.setColor(0, 0, 0, 0.5f);
        p2.fill();
        cardBgTexture = new Texture(p2);
        p2.dispose();
        pixmap.dispose();
    }

    private void setupStyles() {
        // 使用 Hoefler Text 字体
        titleStyle = new Label.LabelStyle(skin.getFont("hoefler"), themeColor);
        bodyStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.WHITE);
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // --- 1. 标题部分 (模仿 PauseMenu) ---
        Table titleTable = new Table();
        titleTable.setBackground(new TextureRegionDrawable(titleBgTexture));

        Label titleLabel = new Label("Encyclopedia", titleStyle);
        titleLabel.setFontScale(1.2f);
        titleTable.add(titleLabel).padBottom(10); // 微调文字在背景中的位置

        // 放在页面顶部，留出黑边位置
        root.add(titleTable).padTop(80).padBottom(20).row();

        // --- 2. 内容区域 (Grid) ---
        Table contentTable = new Table();
        contentTable.top();

        Map<String, EncyclopediaEntry> entries = encyclopediaManager.getAllEntries();
        int columns = 0;

        for (EncyclopediaEntry entry : entries.values()) {
            boolean isUnlocked = game.getPlayerState().getDiscoveredEncyclopediaIds().contains(entry.getId());

            // 创建卡片
            Table card = createEntryCard(entry, isUnlocked);

            // 调整卡片大小和间距
            contentTable.add(card).width(320).height(380).pad(20);

            columns++;
            if (columns >= 3) { // 每行3个
                contentTable.row();
                columns = 0;
            }
        }

        // --- 3. 滚动面板 ---
        ScrollPane scrollPane = new ScrollPane(contentTable); // 默认皮肤样式可能带背景，这里去掉
        // scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // 禁止水平滚动
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, true);
        // 放在中间，稍微缩进
        root.add(scrollPane).expand().fill().pad(20).padBottom(40).row();

        // --- 4. 返回按钮 ---
        TextButton backButton = createHoverButton("Back to Menu", "short");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        root.add(backButton).bottom().padBottom(100); // 抬高一点避开黑边
    }

    /**
     * 创建统一风格的悬停按钮
     */
    private TextButton createHoverButton(String text, String styleName) {
        final TextButton button = new TextButton(text, skin, styleName); // 使用 skin 里定义的样式 (short/middle)
        button.setTransform(true);
        button.setOrigin(Align.center);

        // 覆盖字体颜色为统一主题色 (如果 style 里不是的话)
        button.getLabel().setStyle(new Label.LabelStyle(skin.getFont("hoefler"), themeColor));

        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (pointer == -1) {
                    button.clearActions();
                    button.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f, Interpolation.smooth));
                }
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer == -1) {
                    button.clearActions();
                    button.addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f, Interpolation.smooth));
                }
            }
        });
        return button;
    }

    private Table createEntryCard(EncyclopediaEntry entry, boolean isUnlocked) {
        Table card = new Table();
        card.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);

        // 使用半透明黑色背景，带一点边框感
        card.setBackground(new TextureRegionDrawable(cardBgTexture));

        // 图片逻辑
        Image icon;
        Texture texture;
        try {
            texture = new Texture(Gdx.files.internal(entry.getTexturePath()));
        } catch (Exception e) {
            Pixmap p = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
            p.setColor(Color.RED); p.fill();
            texture = new Texture(p);
            p.dispose();
        }
        icon = new Image(texture);
        icon.setScaling(Scaling.fit);

        if (!isUnlocked) {
            icon.setColor(Color.BLACK);
        }

        // 文字逻辑
        String nameText = isUnlocked ? entry.getName() : "???";
        Label nameLabel = new Label(nameText, bodyStyle);
        nameLabel.setAlignment(Align.center);
        // 如果未解锁，文字变灰
        if (!isUnlocked) nameLabel.setColor(Color.GRAY);

        // 组装
        card.add(icon).size(128, 128).pad(20).row();
        card.add(nameLabel).padBottom(20).row();

        // 交互效果
        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isUnlocked) {
                    showDetailDialog(entry);
                }
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (isUnlocked) card.addAction(Actions.scaleTo(1.05f, 1.05f, 0.1f));
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (isUnlocked) card.addAction(Actions.scaleTo(1f, 1f, 0.1f));
            }
        });

        card.setTransform(true);
        card.setOrigin(Align.center);
        return card;
    }

    private void showDetailDialog(EncyclopediaEntry entry) {
        Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {}
        };

        // 设置 Dialog 背景为深色半透明，统一风格
        dialog.setBackground(new TextureRegionDrawable(detailPanelTexture));


        dialog.setMovable(false);
        dialog.setModal(true);

        Table content = dialog.getContentTable();
        content.pad(50);

        // 标题
        Label titleLabel = new Label(entry.getName(), titleStyle); // 使用统一的主题色字体
        titleLabel.setFontScale(1.5f);
        content.add(titleLabel).padBottom(30).row();

        // 图片
        try {
            Texture bigTex = new Texture(Gdx.files.internal(entry.getTexturePath()));
            Image bigImage = new Image(bigTex);
            bigImage.setScaling(Scaling.fit);
            content.add(bigImage).size(400, 400).padBottom(30).row();
        } catch (Exception e) {}

        // 描述
        Label descLabel = new Label(entry.getDescription(), bodyStyle); // 使用白色字体
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.center);
        descLabel.setFontScale(1.1f);
        content.add(descLabel).width(700).padBottom(40).row();

        // 关闭按钮
        TextButton closeBtn = createHoverButton("Close", "short");
        dialog.button(closeBtn, true);
        dialog.getButtonTable().getCell(closeBtn).width(200).height(60).padBottom(20);

        dialog.show(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- 核心：更新背景 ---
        updateBackground(delta);

        stage.act(delta);
        stage.draw();
    }

    private void updateBackground(float delta) {
        if (backgroundImage1 == null || backgroundImage2 == null) return;

        // 滚动逻辑
        backgroundImage1.setX(backgroundImage1.getX() - scrollSpeed * delta);
        backgroundImage2.setX(backgroundImage2.getX() - scrollSpeed * delta);

        // 更新全局状态，保证切换场景时背景连贯
        float w = backgroundImage1.getWidth();
        // 修正：处理负数取模
        float currentX = backgroundImage1.getX() % w;
        if (currentX > 0) currentX -= w;
        StoryMenu.savedBackgroundX = currentX;

        // 循环衔接
        if (backgroundImage1.getX() + w <= 0) {
            backgroundImage1.setX(backgroundImage2.getX() + w);
        }
        if (backgroundImage2.getX() + w <= 0) {
            backgroundImage2.setX(backgroundImage1.getX() + w);
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        float stageW = stage.getWidth();
        float stageH = stage.getHeight();

        // 调整背景尺寸
        if (backgroundImage1 != null) backgroundImage1.setSize(stageW, stageH);
        if (backgroundImage2 != null) backgroundImage2.setSize(stageW, stageH);

        // 调整黑边尺寸
        if (cinematicBarTop != null && cinematicBarBottom != null) {
            float barHeight = stageH * CINEMATIC_RATIO;
            cinematicBarBottom.setSize(stageW, barHeight);
            cinematicBarBottom.setPosition(0, 0);

            cinematicBarTop.setSize(stageW, barHeight);
            cinematicBarTop.setPosition(0, stageH - barHeight);
        }
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (blackTexture != null) blackTexture.dispose();
        if (titleBgTexture != null) titleBgTexture.dispose();
        if (cardBgTexture != null) cardBgTexture.dispose();
    }
}