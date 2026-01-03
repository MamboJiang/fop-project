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
import de.tum.cit.fop.maze.GameObj.Character;
import de.tum.cit.fop.maze.GameScreen;

public class HUD {
    private final Stage stage;
    private final Texture objectsTexture;
    private final TextureRegion[] heartRegions;
    private final TextureRegion keyRegion;
    private final com.badlogic.gdx.graphics.g2d.NinePatch achievementNinePatch;
    
    private Image heartImage;
    private Image keyImage;
    private Table table;
    private Table debugTable;
    private Label debugInfoLabel;
    private Label timeLabel;
    
    // Console UI
    private Table contentTable;
    private com.badlogic.gdx.scenes.scene2d.ui.TextField consoleInput;
    
    // Dependencies
    private final GameScreen gameScreen;
    private final Skin skin;
    private Character character; // Reference to character for debug actions

    public HUD(SpriteBatch spriteBatch, GameScreen gameScreen, Skin skin) {
        this.gameScreen = gameScreen;
        this.skin = skin;
        stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1920, 1080), spriteBatch);
        
        // Load textures
        objectsTexture = new Texture(Gdx.files.internal("objects.png"));
        TextureRegion[][] tmp = TextureRegion.split(objectsTexture, 16, 16);
        
        // Hearts logic omitted for brevity in diff...
        heartRegions = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            heartRegions[i] = tmp[0][4 + i];
        }
        
        keyRegion = tmp[4][0];

        // Achievement Background: "Rows 19 and 20, Columns 5-8" (Indices 18-19, 4-7)
        // We use a NinePatch to stretch it properly.
        // The region is 64x32 (4 tiles wide, 2 tiles high).
        // 16px Left Cap, 16px Right Cap, 32px Center Body.
        TextureRegion bgRegion = new TextureRegion(objectsTexture, 4 * 16, 18 * 16, 4 * 16, 2 * 16);
        // NinePatch splits: Left, Right, Top, Bottom
        achievementNinePatch = new com.badlogic.gdx.graphics.g2d.NinePatch(bgRegion, 16, 16, 0, 0);
        achievementNinePatch.scale(4, 4); // Scale up to match the UI scale (4x)
        
        setupUI();
        setupDebugMenu();
        
        // Register HUD with AchievementManager
        AchievementManager.getInstance().setHUD(this);
    }

    private void setupUI() {
        table = new Table();
        table.top();
        table.setFillParent(true);
        
        // Left: Heart
        // Initial heart image (4 lives -> index 0)
        heartImage = new Image(heartRegions[0]);
        
        // Right: Key
        // Initially invisible or specific icon? User said "show if key is there". 
        // Let's assume we show the key icon if collected, or maybe a grayed out version?
        // Requirement: "Top Right display if key is still there" (meaning present in map? or collected?)
        // "右上角显示钥匙还在不在" -> "Display in top right whether key is still there (not collected yet?)"
        // Or maybe "Display key if collected".
        // Let's interpret "Key still there" as: Show Key icon if player DOES NOT have key yet? 
        // Or logic: "If key is collected, show key".
        // Let's stick to standard: Show empty slot or key when collected.
        // Wait, "Display whether key is still there" sounds like "Key is on map".
        // Let's try: Always show key, maybe dim it if not collected?
        // Re-reading: "Show whether key is NOT there" or "Show key status".
        // Let's implement: Show Key Image always for now.
        keyImage = new Image(keyRegion);
        timeLabel = new Label("Time: 00:00\nScore: 1000", skin);
        timeLabel.setAlignment(Align.center); // 让文字居中对齐

        // 添加到表格中间

        
        table.add(heartImage).expandX().left().pad(10).size(64, 64);
        table.add(timeLabel).expandX().center().padTop(10);
        table.add(keyImage).expandX().right().pad(10).size(64, 64);
        
        stage.addActor(table);
        stage.addActor(table);
    }
    
    private void setupDebugMenu() {
        debugTable = new Table();
        debugTable.bottom().left();
        debugTable.setFillParent(true);
        
        // Container for content buttons
        contentTable = new Table();
        contentTable.setVisible(false); // Initially hidden
        
        // Debug Info Label
        debugInfoLabel = new Label("Speed: 0\nHP: 4\nKey: false", skin);
        contentTable.add(debugInfoLabel).left().pad(5).row();

        // --- Console UI ---
        // Output Log
        final Label consoleLog = new Label("Console ready. Type 'help' for commands.", skin);
        consoleLog.setWrap(true);
        // We might want scroll pane but for simplicity just a label for last few lines or strictly current feedback
        contentTable.add(consoleLog).width(300).left().pad(5).row();

        // Input Field
        consoleInput = new com.badlogic.gdx.scenes.scene2d.ui.TextField("", skin);
        consoleInput.setMessageText("Enter command...");
        consoleInput.setTextFieldListener(new com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener() {
            @Override
            public void keyTyped(com.badlogic.gdx.scenes.scene2d.ui.TextField textField, char c) {
                if ((c == '\r' || c == '\n') && !textField.getText().trim().isEmpty()) {
                    String cmd = textField.getText().trim();
                    textField.setText(""); // Clear input
                    String output = handleCommand(cmd);
                    consoleLog.setText(output);
                }
            }
        });
        consoleInput.setTextFieldFilter(new com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(com.badlogic.gdx.scenes.scene2d.ui.TextField textField, char c) {
                // Reject the console toggle key character (if it matches default or common toggle keys)
                if (c == '`' || c == '~') return false;
                
                // Also check configured key if possible mapping exists (hard to map int->char robustly without more logic)
                // For now, hardcoding rejection of backtick/tilde is what the user asked for.
                return true;
            }
        });
        contentTable.add(consoleInput).width(300).left().pad(5).row();

        // Toggle Menu Button (Always visible)
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
        

        // Add content table and toggle button to main table
        debugTable.add(contentTable).left().pad(5).row();
        debugTable.add(toggleMenuBtn).left().pad(5);
        
        stage.addActor(debugTable);
    }

    private String handleCommand(String commandLine) {
        String[] parts = commandLine.split("\\s+");
        if (parts.length == 0) return "";
        
        String command = parts[0].toLowerCase();
        
        try {
            switch (command) {
                case "help":
                    return "Commands: hp <add/set>, key <true/false>, god, shield, zoom <in/out>, debug, leaderboard <clear/add>";
                
                case "hp":
                    if (parts.length < 3) return "Usage: hp <add/set> <value>";
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
                    if (parts.length < 2) return "Usage: key <true/false>";
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
                    if (parts.length < 2) return "Usage: zoom <in/out>";
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
                    
                case "achievement":
                     if (parts.length < 2) return "Usage: achievement <unlock/list> [id]";
                     if (parts[1].equalsIgnoreCase("unlock")) {
                         if (parts.length < 3) return "Specify achievement ID.";
                         // For testing UI, we can forcefully unlock or just show popup
                         // But manager handles logic. Let's add a debug method in Manager or just simulate event?
                         // Better: Force unlock by specific ID (bypass conditions)
                         // Check AchievementManager implementation... it doesn't have public unlock or getAchievement.
                         // Let's rely on simulated event if possible, or add a method.
                         // Actually, I can use reflection or add a method to manager.
                         // Let's assume I can call a method I'll add to Manager "debugUnlock(id)"
                         AchievementManager.getInstance().debugUnlock(parts[2]);
                         return "Attempting unlock: " + parts[2];
                     }
                     return "Unknown achievement command.";
                    
                case "leaderboard":
                     if (parts.length < 2) return "Usage: leaderboard <clear/add>";
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

    public void update(Character character) {
        this.character = character;


        String timeStr = gameScreen.getFormattedTime();
        timeLabel.setText(timeStr);

        // Update Heart
        int lives = character.getLives();
        // 4 lives -> index 0
        // 3 lives -> index 1
        // 2 lives -> index 2
        // 1 lives -> index 3
        // 0 lives -> index 4
        int heartIndex = 4 - lives;
        if (heartIndex < 0) heartIndex = 0;
        if (heartIndex > 4) heartIndex = 4;
        
        heartImage.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(heartRegions[heartIndex]));
        
        // Update Key
        if (character.hasKey()) {
             keyImage.setColor(Color.WHITE); // Normal color
        } else {
             keyImage.setColor(Color.DARK_GRAY); // Dimmed if not collected
        }
        
        // Update Debug Label
        if (debugInfoLabel != null) {
            float speed = character.getVelocity().len();
            debugInfoLabel.setText(String.format("Speed: %.2f\nHP: %d\nKey: %b", speed, lives, character.hasKey()));
        }
    }
    
    public Stage getStage() {
        return stage;
    }

    public void render(float delta) {
        // Check for Hotkey
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

    // --- Achievement Popup Logic ---
    public void showAchievementPopup(Achievement achievement) {
        Gdx.app.postRunnable(() -> {
            AchievementPopup popup = new AchievementPopup(achievement, skin, achievementNinePatch);
            stage.addActor(popup);
            popup.animate();
        });
    }

    private static class AchievementPopup extends Table {
        public AchievementPopup(Achievement achievement, Skin skin, com.badlogic.gdx.graphics.g2d.NinePatch bgPatch) {
            this.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(bgPatch));
            
            // Adjust size. User requested wider.
            // Old was 256. Let's increase to 320 (5 * 64) or similar.
            this.setSize(340, 128);
            
            // Align top-center of screen initially (off-screen)
            this.setPosition((1920 - 340) / 2f, 1080 + 10);
            
            Label titleLabel = new Label("Achievement!", skin);
            titleLabel.setFontScale(0.8f);
            this.add(titleLabel).padTop(0).padLeft(100).row();
            Label nameLabel = new Label(achievement.getName(), skin);
            nameLabel.setFontScale(0.8f); 
            this.add(nameLabel).padTop(-5).padLeft(100);
        }

        public void animate() {
            // Slide down, wait, slide up, remove
            this.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo(this.getX(), 1080 - 150, 0.5f, com.badlogic.gdx.math.Interpolation.swingOut),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(3f),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo(this.getX(), 1080 + 10, 0.5f, com.badlogic.gdx.math.Interpolation.swingIn),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor()
            ));
        }
    }
}
