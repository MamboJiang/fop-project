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
    
    private Image keyImage;
    private Table table;
    private Table heartsTable; // Container for heart images
    private com.badlogic.gdx.utils.Array<Image> heartImages; // List of active heart actors
    private Table debugTable;
    private Label debugInfoLabel;
    private Label timeLabel;
    
    private Table contentTable;
    private com.badlogic.gdx.scenes.scene2d.ui.TextField consoleInput;
    
    private final GameScreen gameScreen;
    private final Skin skin;
    private Character character;

    public HUD(SpriteBatch spriteBatch, GameScreen gameScreen, Skin skin) {
        this.gameScreen = gameScreen;
        this.skin = skin;
        stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1920, 1080), spriteBatch);
        
        objectsTexture = new Texture(Gdx.files.internal("objects.png"));
        TextureRegion[][] tmp = TextureRegion.split(objectsTexture, 16, 16);
        
        heartRegions = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            heartRegions[i] = tmp[0][4 + i];
        }
        
        keyRegion = tmp[4][0];

        TextureRegion bgRegion = new TextureRegion(objectsTexture, 4 * 16, 18 * 16, 4 * 16, 2 * 16);
        achievementNinePatch = new com.badlogic.gdx.graphics.g2d.NinePatch(bgRegion, 16, 16, 0, 0);
        achievementNinePatch.scale(4, 4); 
        
        heartImages = new com.badlogic.gdx.utils.Array<>();
        
        setupUI();
        setupDebugMenu();
        
        AchievementManager.getInstance().setHUD(this);
    }

    private void setupUI() {
        table = new Table();
        table.top();
        table.setFillParent(true);
        
        heartsTable = new Table();
        heartsTable.left();
        
        keyImage = new Image(keyRegion);
        timeLabel = new Label("Time: 00:00\nScore: 1000", skin);
        timeLabel.setAlignment(Align.center); 

        table.add(heartsTable).expandX().left().pad(10).height(64);
        table.add(timeLabel).expandX().center().padTop(10);
        table.add(keyImage).expandX().right().pad(10).size(64, 64);
        
        stage.addActor(table);
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
                if (c == '`' || c == '~') return false;
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

        int currentLives = character.getLives();
        int maxLives = character.getMaxLives();

        int numHearts = (int)Math.ceil(maxLives / 4.0);
        if (numHearts < 1) numHearts = 1;


        if (heartImages.size != numHearts) {
            heartsTable.clearChildren();
            heartImages.clear();
            for (int i = 0; i < numHearts; i++) {
                Image img = new Image(heartRegions[0]); // Default full
                heartsTable.add(img).size(64, 64).padRight(5);
                heartImages.add(img);
            }
        }
        

        for (int i = 0; i < heartImages.size; i++) {
            Image img = heartImages.get(i);
            

            int heartStartHP = i * 4;
            int hpForThisHeart = currentLives - heartStartHP;
            

            if (hpForThisHeart > 4) hpForThisHeart = 4;
            if (hpForThisHeart < 0) hpForThisHeart = 0;
            

            int textureIndex = 4 - hpForThisHeart;
            
            img.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(heartRegions[textureIndex]));
        }
        

        if (character.hasKey()) {
             keyImage.setColor(Color.WHITE);
        } else {
             keyImage.setColor(Color.DARK_GRAY);
        }
        

        if (debugInfoLabel != null) {
            float speed = character.getVelocity().len();
            debugInfoLabel.setText(String.format("Speed: %.2f\nHP: %d/%d\nKey: %b", speed, currentLives, maxLives, character.hasKey()));
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
                com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo(this.getX(), 1080 - 150, 0.5f, com.badlogic.gdx.math.Interpolation.swingOut),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(3f),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo(this.getX(), 1080 + 10, 0.5f, com.badlogic.gdx.math.Interpolation.swingIn),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor()
            ));
        }
    }
}
