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
    private Table heartsTable;
    private com.badlogic.gdx.utils.Array<Image> heartImages;
    private Table debugTable;
    private Label debugInfoLabel;
    private Label timeLabel;
    private Label promptLabel;
    private Label floorLabel;

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
    private Texture blankTexture;
    private float bossBarMaxWidth = 1000f;

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
        stage = new Stage(new com.badlogic.gdx.utils.viewport.ExtendViewport(1920, 1080), spriteBatch);

        objectsTexture = new Texture(Gdx.files.internal("selfmade/maskicon.png"));

        Texture maskTexture = objectsTexture;
        heartRegions = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            heartRegions[i] = new TextureRegion(maskTexture);
        }

        Texture basicTile = new Texture(Gdx.files.internal("selfmade/basictile.png"));
        TextureRegion[][] tiles = TextureRegion.split(basicTile, 32, 32);
        keyRegion = tiles[1][1];


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
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        blankTexture = new Texture(pixmap);
        pixmap.dispose();

        bossTable = new Table();
        bossTable.top();
        bossTable.setFillParent(true);
        bossTable.setVisible(false);

        Label.LabelStyle nameStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.RED);
        bossNameLabel = new Label("The Guardian", nameStyle);
        bossNameLabel.setFontScale(1.5f);

        com.badlogic.gdx.scenes.scene2d.ui.Stack barStack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();

        Image bgBar = new Image(blankTexture);
        bgBar.setColor(Color.BLACK);

        bossHealthBar = new Image(blankTexture);
        bossHealthBar.setColor(Color.RED);

        barStack.add(bgBar);

        Table innerTable = new Table();
        innerTable.left();
        innerTable.add(bossHealthBar).width(bossBarMaxWidth).height(30);
        barStack.add(innerTable);

        bossTable.add(bossNameLabel).padTop(70).padBottom(10).row();
        bossTable.add(barStack).width(bossBarMaxWidth).height(30);

        stage.addActor(bossTable);
    }

    private void setupUI() {
        if (table != null)
            table.remove();

        com.badlogic.gdx.scenes.scene2d.ui.Stack stack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        stack.setFillParent(true);

        Table centerLayer = new Table();
        centerLayer.top();
        Label.LabelStyle fontStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.WHITE);
        timeLabel = new Label("Time: 00:00\nScore: 1000", fontStyle);
        timeLabel.setAlignment(Align.center);

        centerLayer.add(timeLabel).pad(10);
        stack.add(centerLayer);

        Table sidesLayer = new Table();
        sidesLayer.top().left();
        sidesLayer.setFillParent(true);

        heartsTable = new Table();
        heartsTable.top().left();

        keyImage = new Image(keyRegion);

        sidesLayer.add(heartsTable).top().left().pad(10);

        sidesLayer.add().expandX();

        Table rightTable = new Table();
        rightTable.top().right();
        rightTable.add(keyImage).size(64, 64).row();

        Label.LabelStyle smallStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.WHITE);
        floorLabel = new Label("", smallStyle);
        floorLabel.setFontScale(0.8f);
        rightTable.add(floorLabel).padTop(5);

        sidesLayer.add(rightTable).top().right().pad(10);

        stack.add(sidesLayer);



        Label.LabelStyle blueStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.GREEN);
        promptLabel = new Label("Press [F] to Interact", blueStyle);
        promptLabel.setFontScale(1f);
        promptLabel.setVisible(false);



        stage.addActor(promptLabel);

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
        this.table = sidesLayer;
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
                Image img = new Image(heartRegions[0]);
                heartsTable.add(img).size(64, 64).padRight(5).padBottom(5);
                if ((i + 1) % 5 == 0) {
                    heartsTable.row();
                }
                heartImages.add(img);
            }
        }

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


        for (int i = 0; i < heartImages.size; i++) {
            Image img = heartImages.get(i);

            if (i < currentLives) {
                img.setColor(Color.WHITE);
            } else {
                img.setColor(Color.DARK_GRAY);
            }
        }

        if (character.hasKey()) {
            keyImage.setColor(Color.WHITE);
        } else {
            keyImage.setColor(Color.DARK_GRAY);
        }

        Boss boss = gameScreen.getActiveBoss();
        if (boss != null && !boss.isDead()) {
            bossTable.setVisible(true);
            bossNameLabel.setVisible(true);

            float percent = boss.getHealthPercentage();

            bossHealthBar.setWidth(bossBarMaxWidth * percent);

            bossHealthBar.invalidate();
        } else {

            bossTable.setVisible(false);
        }

        if (debugInfoLabel != null) {
            float speed = character.getVelocity().len();
            debugInfoLabel.setText(String.format("Speed: %.2f\nHP: %d/%d\nKey: %b", speed, currentLives, maxLives,
                    character.hasKey()));
        }

        if (promptLabel.isVisible() && character != null) {
            Vector3 worldPos = new Vector3(character.getPosition().x + character.getWidth() / 2f,
                    character.getPosition().y - 12f, 0);
            Vector3 screenPos = gameScreen.getCamera().project(worldPos);
            Vector2 stagePos = stage.screenToStageCoordinates(new Vector2(screenPos.x, screenPos.y));

            promptLabel.setPosition(stagePos.x, stagePos.y, Align.center | Align.top);
        }

        Vector3 worldPos2 = new Vector3(character.getPosition().x + character.getWidth() / 2f,
                character.getPosition().y - 12f, 0);
        Vector3 screenPos2 = gameScreen.getCamera().project(worldPos2);
        Vector2 stagePos2 = stage.screenToStageCoordinates(new Vector2(screenPos2.x, screenPos2.y));

        float yOffset = 0;
        if (moveHintLabel.isVisible()) {
            moveHintLabel.setPosition(stagePos2.x, stagePos2.y + yOffset, Align.center | Align.top);
            yOffset -= 30;
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
            popup.toFront();
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
