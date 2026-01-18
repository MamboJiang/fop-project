package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.GameObj.Character;
import de.tum.cit.fop.maze.GameScreen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.GameObj.Boss;

/**
 * Heads-Up Display (HUD) for the game.
 * Shows lives (hearts), key status, timer, score, and debug console.
 */
public class HUD {
    private final Stage stage;
    private final Texture objectsTexture;
    private final TextureRegion[] heartRegions;
    private final TextureRegion keyRegion;
    private final com.badlogic.gdx.graphics.g2d.NinePatch achievementNinePatch;

    private Image keyImage;
    private Table table;
    private Table heartsTable; // Container for heart images
    private com.badlogic.gdx.utils.Array<Image> heartImages; // List of active heart actors
    private Table debugTable;
    private Label debugInfoLabel;
    private Label timeLabel;
    private Label promptLabel;
    private Label floorLabel;

    // Tutorial hints
    private Label moveHintLabel;
    private Label sprintHintLabel;
    private Label attackHintLabel;
    private boolean moveHintDismissed = false;
    private boolean sprintHintDismissed = false;
    private boolean attackHintDismissed = false;

    private Table contentTable;
    private com.badlogic.gdx.scenes.scene2d.ui.TextField consoleInput;

    private final GameScreen gameScreen;
    private final Skin skin;
    private Character character;


    private Table bossTable;
    private Image bossHealthBar;
    private Label bossNameLabel;
    private Texture blankTexture; // 用代码生成的纯白图片
    private float bossBarMaxWidth = 400f; // 血条最大宽度

    /**
     * Constructor for HUD.
     * 
     * @param spriteBatch SpriteBatch for rendering.
     * @param gameScreen  Reference to game screen.
     * @param skin        UI skin.
     */
    public HUD(SpriteBatch spriteBatch, GameScreen gameScreen, Skin skin) {
        this.gameScreen = gameScreen;
        this.skin = skin;
        stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1920, 1080), spriteBatch);

        // Load Mask Icons for Lives
        // We reused objectsTexture variable name, rename it to maskTexture for clarity or just use new var
        objectsTexture = new Texture(Gdx.files.internal("assets/selfmade/maskicon.png")); // This is now maskicon
        
        Texture maskTexture = objectsTexture; // Alias
        heartRegions = new TextureRegion[5];
        for(int i=0; i<5; i++) {
            heartRegions[i] = new TextureRegion(maskTexture);
        }

        // Load Key (Card) Icon
        Texture basicTile = new Texture(Gdx.files.internal("assets/selfmade/basictile.png"));
        TextureRegion[][] tiles = TextureRegion.split(basicTile, 32, 32); 
        keyRegion = tiles[1][1]; 

        // Load Atlas for Achievement Background (original objects.png)
        Texture atlasTexture = new Texture(Gdx.files.internal("objects.png"));
        TextureRegion bgRegion = new TextureRegion(atlasTexture, 4 * 16, 18 * 16, 4 * 16, 2 * 16);
        achievementNinePatch = new com.badlogic.gdx.graphics.g2d.NinePatch(bgRegion, 16, 16, 0, 0);
        achievementNinePatch.scale(4, 4);

        heartImages = new com.badlogic.gdx.utils.Array<>();

        setupUI();
        setupBossHUD();
        setupDebugMenu();

        AchievementManager.getInstance().setHUD(this);
    }

    private void setupBossHUD() {
        // 1. 动态生成一个 1x1 的纯白纹理，用于做血条
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        blankTexture = new Texture(pixmap);
        pixmap.dispose();

        // 2. 创建 Boss 血条的容器 Table
        bossTable = new Table();
        bossTable.top(); // 靠上对齐
        bossTable.setFillParent(true);
        bossTable.setVisible(false); // 默认隐藏，只有Boss出现时才显示

        // 3. 创建 Boss 名字标签
        Label.LabelStyle nameStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.RED);
        bossNameLabel = new Label("BIG BOSS", nameStyle);
        bossNameLabel.setFontScale(0.8f);

        // 4. 创建血条组合 (背景黑条 + 前景红条)
        // 使用 Stack 让红条覆盖在黑条上面
        com.badlogic.gdx.scenes.scene2d.ui.Stack barStack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();

        // 背景 (黑色)
        Image bgBar = new Image(blankTexture);
        bgBar.setColor(Color.BLACK);

        // 前景 (红色)
        bossHealthBar = new Image(blankTexture);
        bossHealthBar.setColor(Color.RED);

        // 注意：这里需要把 Image 包装一下或者直接操作，为了简单，我们让 Stack 决定大小
        barStack.add(bgBar);

        // 为了让红条能缩短，我们需要把它放在一个左对齐的容器里，否则 Stack 会强制拉伸它
        Table innerTable = new Table();
        innerTable.left(); // 关键：左对齐
        innerTable.add(bossHealthBar).width(bossBarMaxWidth).height(20);
        barStack.add(innerTable);

        // 5. 布局到 bossTable
        // 位置：使用 padTop 将整个血条往下推，避免与时间重叠
        bossTable.add(bossNameLabel).padTop(70).padBottom(10).row();
        bossTable.add(barStack).width(bossBarMaxWidth).height(20);

        stage.addActor(bossTable);
    }


    private void setupUI() {
        if (table != null) table.remove();
        
        com.badlogic.gdx.scenes.scene2d.ui.Stack stack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        stack.setFillParent(true);
        
        // Layer 1: Time/Score (Centered)
        Table centerLayer = new Table();
        centerLayer.top(); 
        Label.LabelStyle fontStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.WHITE);
        timeLabel = new Label("Time: 00:00\nScore: 1000", fontStyle);
        timeLabel.setAlignment(Align.center);
        
        // Ensure perfect centering by adding to a container that spans width but aligns top
        centerLayer.add(timeLabel).pad(10); 
        stack.add(centerLayer);
        
        // Layer 2: Hearts (Left) and Key (Right)
        Table sidesLayer = new Table();
        sidesLayer.top().left();
        sidesLayer.setFillParent(true);
        
        heartsTable = new Table();
        heartsTable.top().left();
        
        keyImage = new Image(keyRegion);
        
        // Left: Hearts
        sidesLayer.add(heartsTable).top().left().pad(10);
        
        // Spacer to push Key to right
        sidesLayer.add().expandX();
        
        // Right: Key and Level Name
        Table rightTable = new Table();
        rightTable.top().right();
        rightTable.add(keyImage).size(64, 64).row();
        
        Label.LabelStyle smallStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.WHITE);
        floorLabel = new Label("", smallStyle);
        floorLabel.setFontScale(0.8f);
        rightTable.add(floorLabel).padTop(5);
        
        sidesLayer.add(rightTable).top().right().pad(10);
        
        stack.add(sidesLayer);
        
        // Add Hint Labels (add to stage directly or another layer)
        // Hints are generally centered bottom or somewhere else. 
        // Existing code added them to 'stage' but they were not in the main table.
        // I'll re-add them to stage.
        
    Label.LabelStyle blueStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.GREEN);
    promptLabel = new Label("Press [F] to Interact", blueStyle);
    promptLabel.setFontScale(1f);
    promptLabel.setVisible(false);
    
    // Position hints manually or add to a layer?
    // Hints appear dynamically. Code sets position/visibility elsewhere?
    // Let's keep them added to stage.
        
    stage.addActor(promptLabel);

    // Tutorial hints
    moveHintLabel = new Label("Press [WASD] to Move", blueStyle);
    moveHintLabel.setFontScale(1f);
    moveHintLabel.setVisible(false);
    stage.addActor(moveHintLabel);

    sprintHintLabel = new Label("Hold [Shift] to Run", blueStyle);
    sprintHintLabel.setFontScale(1f);
    sprintHintLabel.setVisible(false);
    stage.addActor(sprintHintLabel);

    attackHintLabel = new Label("Press [J] to Attack", blueStyle);
    attackHintLabel.setFontScale(1f);
    attackHintLabel.setVisible(false);
    stage.addActor(attackHintLabel);

        stage.addActor(stack);
        this.table = sidesLayer; // Keep reference to one of them if needed for debug? 
        // Actually 'table' variable is used? Assuming 'table' field exists.
        // Yes, 'private Table table'.
        // I will point 'table' to the stack or sidesLayer? Not critical if no external access.
    }

    private void setupDebugMenu() {
        debugTable = new Table();
        debugTable.bottom().left();
        debugTable.setFillParent(true);

        contentTable = new Table();
        contentTable.setVisible(false);

        debugInfoLabel = new Label("Speed: 0\nHP: 4\nKey: false", skin);
        contentTable.add(debugInfoLabel).left().pad(5).row();

        final Label consoleLog = new Label("Console ready. Type 'help' for commands.", skin);
        consoleLog.setWrap(true);

        contentTable.add(consoleLog).width(300).left().pad(5).row();

        consoleInput = new com.badlogic.gdx.scenes.scene2d.ui.TextField("", skin);
        consoleInput.setMessageText("Enter command...");
        consoleInput.setTextFieldListener(new com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener() {
            @Override
            public void keyTyped(com.badlogic.gdx.scenes.scene2d.ui.TextField textField, char c) {
                if ((c == '\r' || c == '\n') && !textField.getText().trim().isEmpty()) {
                    String cmd = textField.getText().trim();
                    textField.setText("");
                    String output = handleCommand(cmd);
                    consoleLog.setText(output);
                }
            }
        });
        consoleInput.setTextFieldFilter(new com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(com.badlogic.gdx.scenes.scene2d.ui.TextField textField, char c) {
                if (c == '`' || c == '~')
                    return false;
                return true;
            }
        });
        contentTable.add(consoleInput).width(300).left().pad(5).row();

        TextButton toggleMenuBtn = new TextButton("Debug", skin);
        toggleMenuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean isVisible = !contentTable.isVisible();
                contentTable.setVisible(isVisible);
                if (isVisible) {
                    stage.setKeyboardFocus(consoleInput);
                } else {
                    stage.setKeyboardFocus(null);
                    Gdx.input.setOnscreenKeyboardVisible(false);
                }
            }
        });

        debugTable.add(contentTable).left().pad(5).row();
        debugTable.add(toggleMenuBtn).left().pad(5);

        stage.addActor(debugTable);
    }

    private String handleCommand(String commandLine) {
        String[] parts = commandLine.split("\\s+");
        if (parts.length == 0)
            return "";

        String command = parts[0].toLowerCase();

        try {
            switch (command) {
                case "help":
                    return "Commands: hp <add/set>, key <true/false>, god, shield, zoom <in/out>, debug, map, leaderboard <clear/add>";

                case "hp":
                    if (parts.length < 3)
                        return "Usage: hp <add/set> <value>";
                    int val = Integer.parseInt(parts[2]);
                    if (character != null) {
                        if (parts[1].equalsIgnoreCase("add")) {
                            character.addLives(val);
                            return "Added " + val + " lives.";
                        } else if (parts[1].equalsIgnoreCase("set")) {
                            character.setLives(val);
                            return "Set lives to " + val + ".";
                        }
                    }
                    return "Character not found or invalid sub-command.";

                case "key":
                    if (parts.length < 2)
                        return "Usage: key <true/false>";
                    boolean hasKey = Boolean.parseBoolean(parts[1]);
                    if (character != null) {
                        character.setHasKey(hasKey);
                        return "Key set to " + hasKey;
                    }
                    return "Character not found.";

                case "god":
                    if (character != null) {
                        boolean newState = !character.isInfiniteHP();
                        if (parts.length > 1) {
                            newState = Boolean.parseBoolean(parts[1]);
                        }
                        character.setInfiniteHP(newState);
                        return "Infinite HP: " + newState;
                    }
                    return "Character not found.";

                case "shield":
                    if (character != null) {
                        if (character.isShielded()) {
                            character.activateShield(0);
                            return "Shield Deactivated";
                        } else {
                            character.activateShield(9999f);
                            return "Infinite Shield Activated";
                        }
                    }
                    return "Character not found.";

                case "zoom":
                    if (parts.length < 2)
                        return "Usage: zoom <in/out>";
                    if (parts[1].equalsIgnoreCase("in")) {
                        gameScreen.zoomIn();
                        return "Zoomed In";
                    } else if (parts[1].equalsIgnoreCase("out")) {
                        gameScreen.zoomOut();
                        return "Zoomed Out";
                    }
                    return "Invalid zoom argument.";

                case "debug":
                    gameScreen.toggleDebug();
                    return "Debug Mode Toggled";

                case "map":
                    gameScreen.toggleMapDebug();
                    return "Map View Toggled";

                case "achievement":
                    if (parts.length < 2)
                        return "Usage: achievement <unlock/list> [id]";
                    if (parts[1].equalsIgnoreCase("unlock")) {
                        if (parts.length < 3)
                            return "Specify achievement ID.";

                        AchievementManager.getInstance().debugUnlock(parts[2]);
                        return "Attempting unlock: " + parts[2];
                    }
                    return "Unknown achievement command.";

                case "leaderboard":
                    if (parts.length < 2)
                        return "Usage: leaderboard <clear/add>";
                    if (parts[1].equalsIgnoreCase("clear")) {
                        LeaderboardManager.clearOnlineLeaderboard(() -> Gdx.app.log("Console", "Leaderboard Cleared"));
                        return "Clearing Leaderboard...";
                    } else if (parts[1].equalsIgnoreCase("add")) {
                        LeaderboardManager.addDebugEntry();
                        return "Added dummy entry.";
                    }
                    return "Unknown leaderboard command.";

                default:
                    return "Unknown command: " + command;
            }
        } catch (NumberFormatException e) {
            return "Invalid number format.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Updates the HUD elements based on character state.
     * 
     * @param character The player character.
     */
    public void update(Character character) {
        this.character = character;

        String timeStr = gameScreen.getFormattedTime();
        timeLabel.setText(timeStr);
        
        if (floorLabel != null && gameScreen.getCurrentLevelName().startsWith("Floor")) {
            floorLabel.setVisible(true);
            floorLabel.setText(gameScreen.getCurrentLevelName());
        } else if (floorLabel != null) {
            floorLabel.setVisible(false);
        }

        int currentLives = character.getLives();
        int maxLives = character.getMaxLives();

        if (heartImages.size != maxLives) {
            heartsTable.clearChildren();
            heartImages.clear();
            for (int i = 0; i < maxLives; i++) {
                Image img = new Image(heartRegions[0]); // Default full
                heartsTable.add(img).size(64, 64).padRight(5).padBottom(5); // Increased padBottom, smaller size? User asked for grid.
                // Assuming mask icons are roughly 32x32 or scaled. Previous code used 64x64. Let's stick to user request or reasonable size. 
                // User didn't specify size, but 64x64 is big for many masks. I'll keep 64 if possible or 48.
                // Let's use 48x48 to fit 5.
                // Row break every 5 items
                if ((i + 1) % 5 == 0) {
                    heartsTable.row();
                }
                heartImages.add(img);
            }
        }

        // Logic to update drawable based on health
        // Since we now use a single mask icon, this part is simplified:
        // We either show it or don't? OR we assume full health logic applies?
        // Wait, maskicon is likely just "one mask = 4 HP" or "one mask = 1 HP"?
        // Original logic: 1 heart = 4 HP. 
        // If we switch to mask icons, does 1 mask = 1 HP? 
        // User said: "左上角HUD的爱心换成...maskicon.png...12345个...满5个换行".
        // This implies count of masks = count of lives/health?
        // "Lives" in this game seem to be HP chunks. 
        // Let's assume 1 Mask = 1 Life Unit (1 HP).
        // Original: numHearts = ceil(maxLives / 4.0).
        // If user wants "1, 2, 3, 4, 5个", maybe they want 1 icon per 1 HP?
        // If so, I should change numHearts calculation to just 'maxLives'.
        
        // Let's Assume 1 Mask = 1 HP based on "12345 ge".
        // Re-calculating numHearts
        int numIcons = maxLives; 
        
        if (heartImages.size != numIcons) {
             heartsTable.clearChildren();
             heartImages.clear();
             for (int i = 0; i < numIcons; i++) {
                 Image img = new Image(heartRegions[0]); 
                 heartsTable.add(img).size(48, 48).padRight(5).padBottom(5);
                 if ((i + 1) % 5 == 0) {
                     heartsTable.row();
                 }
                 heartImages.add(img);
             }
        }
        
        // Update visibility/texture
        for (int i = 0; i < heartImages.size; i++) {
            Image img = heartImages.get(i);
            // If currentLives > i, this mask is active (full).
            // If currentLives <= i, this mask is empty/lost? 
            // Or just hide it? 
            // Usually we keep empty containers. 
            // Since we don't have an "empty mask" texture, we might tint it black or reduce alpha.
            
            if (i < currentLives) {
                img.setColor(Color.WHITE);
            } else {
                img.setColor(Color.DARK_GRAY); // Dimmed for lost health
            }
        }

        if (character.hasKey()) {
            keyImage.setColor(Color.WHITE);
        } else {
            keyImage.setColor(Color.DARK_GRAY);
        }

        Boss boss = gameScreen.getActiveBoss();
        if (boss != null && !boss.isDead()) {
            bossTable.setVisible(true); // 显示血条
            bossNameLabel.setVisible(true);


            float percent = boss.getHealthPercentage();


            bossHealthBar.setWidth(bossBarMaxWidth * percent);


            bossHealthBar.invalidate();
        } else {
            // 如果没有 Boss 或 Boss 死了，隐藏血条
            bossTable.setVisible(false);
        }


        if (debugInfoLabel != null) {
            float speed = character.getVelocity().len();
            debugInfoLabel.setText(String.format("Speed: %.2f\nHP: %d/%d\nKey: %b", speed, currentLives, maxLives,
                    character.hasKey()));
        }

        // Update Floating Prompt Position
        if (promptLabel.isVisible() && character != null) {
            // World position: Below feet. Character is ~16x16 or 20x20.
            Vector3 worldPos = new Vector3(character.getPosition().x + character.getWidth() / 2f, character.getPosition().y - 12f, 0);
            Vector3 screenPos = gameScreen.getCamera().project(worldPos);
            Vector2 stagePos = stage.screenToStageCoordinates(new Vector2(screenPos.x, screenPos.y));
            
            promptLabel.setPosition(stagePos.x, stagePos.y, Align.center | Align.top);
        }

        // Update Tutorial Hints Position
        Vector3 worldPos2 = new Vector3(character.getPosition().x + character.getWidth() / 2f, character.getPosition().y - 12f, 0);
        Vector3 screenPos2 = gameScreen.getCamera().project(worldPos2);
        Vector2 stagePos2 = stage.screenToStageCoordinates(new Vector2(screenPos2.x, screenPos2.y));

        // Stack hints vertically below character
        float yOffset = 0;
        if (moveHintLabel.isVisible()) {
            moveHintLabel.setPosition(stagePos2.x, stagePos2.y + yOffset, Align.center | Align.top);
            yOffset -= 30; // Space between hints
        }
        if (sprintHintLabel.isVisible()) {
            sprintHintLabel.setPosition(stagePos2.x, stagePos2.y + yOffset, Align.center | Align.top);
            yOffset -= 30;
        }
        if (attackHintLabel.isVisible()) {
            attackHintLabel.setPosition(stagePos2.x, stagePos2.y + yOffset, Align.center | Align.top);
        }
    }

    public void setPromptVisible(boolean visible) {
        if (promptLabel != null) {
            promptLabel.setVisible(visible);
        }
    }

    // Tutorial hint control methods
    public void showMoveHint() {
        if (!moveHintDismissed && moveHintLabel != null) {
            moveHintLabel.setVisible(true);
        }
    }

    public void dismissMoveHint() {
        moveHintDismissed = true;
        if (moveHintLabel != null) {
            moveHintLabel.setVisible(false);
        }
    }

    public void showSprintHint() {
        if (!sprintHintDismissed && sprintHintLabel != null) {
            sprintHintLabel.setVisible(true);
        }
    }

    public void dismissSprintHint() {
        sprintHintDismissed = true;
        if (sprintHintLabel != null) {
            sprintHintLabel.setVisible(false);
        }
    }

    public void showAttackHint() {
        if (!attackHintDismissed && attackHintLabel != null) {
            attackHintLabel.setVisible(true);
        }
    }

    public void dismissAttackHint() {
        attackHintDismissed = true;
        if (attackHintLabel != null) {
            attackHintLabel.setVisible(false);
        }
    }

    public Stage getStage() {
        return stage;
    }

    public void render(float delta) {

        int consoleKey = gameScreen.getGame().getConfigManager().getKey("CONSOLE");
        if (Gdx.input.isKeyJustPressed(consoleKey)) {
            boolean isVisible = !contentTable.isVisible();
            contentTable.setVisible(isVisible);
            if (isVisible) {
                stage.setKeyboardFocus(consoleInput);
            } else {
                stage.setKeyboardFocus(null);
                Gdx.input.setOnscreenKeyboardVisible(false);
            }
        }

        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        objectsTexture.dispose();
    }

    /**
     * Displays an achievement unlocked popup.
     * 
     * @param achievement The achievement to display.
     */
    public void showAchievementPopup(Achievement achievement) {
        Gdx.app.postRunnable(() -> {
            AchievementPopup popup = new AchievementPopup(achievement, skin, achievementNinePatch);
            stage.addActor(popup);
            popup.toFront(); // Ensure it renders on top of everything
            popup.animate();
        });
    }

    private static class AchievementPopup extends Table {
        public AchievementPopup(Achievement achievement, Skin skin, com.badlogic.gdx.graphics.g2d.NinePatch bgPatch) {
            this.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(bgPatch));

            this.setSize(340, 128);

            this.setPosition((1920 - 340) / 2f, 1080 + 10);

            Label titleLabel = new Label("Achievement!", skin);
            titleLabel.setFontScale(0.8f);
            this.add(titleLabel).padTop(0).padLeft(100).row();
            Label nameLabel = new Label(achievement.getName(), skin);
            nameLabel.setFontScale(0.8f);
            this.add(nameLabel).padTop(-5).padLeft(100);
        }

        public void animate() {
            this.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo(this.getX(), 1080 - 150, 0.5f,
                            com.badlogic.gdx.math.Interpolation.swingOut),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(3f),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo(this.getX(), 1080 + 10, 0.5f,
                            com.badlogic.gdx.math.Interpolation.swingIn),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor()));
        }
    }
}
