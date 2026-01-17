package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.GameControl.ConfigManager;
import de.tum.cit.fop.maze.GameControl.LevelSelectionScreen;
import de.tum.cit.fop.maze.GameControl.SettingsScreen;
import de.tum.cit.fop.maze.GameControl.SkillTreeScreen;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import de.tum.cit.fop.maze.GameObj.PlayerState;


/**
 * The MazeRunnerGame class represents the core of the Maze Runner game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */
public class MazeRunnerGame extends Game {
    // Screens
    private MenuScreen menuScreen;
    private GameScreen gameScreen;

    // Sprite Batch for rendering
    private SpriteBatch spriteBatch;

    // UI Skin
    private Skin skin;

    // Character animation downwards
    private Animation<TextureRegion> characterDownAnimation;
    
    // Configuration Manager
    private ConfigManager configManager;
    public Music backgroundMusic;
    public Music warFogMusic;
    public Music bossFightMusic;

    // Transition Effect
    private de.tum.cit.fop.maze.VFX.TransitionEffect transitionEffect;

    private PlayerState playerState;
    private Sound hitSound;
    private Sound pickupSound;
    private Sound powerUpSound;
    private Sound footstepSound;
    private Sound gameOverSound;
    private Sound blockSound;


    /**
     * Constructor for MazeRunnerGame.
     * @param fileChooser The file chooser for selecting map files.
     */
    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
    }


    /**
     * Called when the game is created. Initializes assets, sounds, and the initial screen.
     */
    @Override
    public void create() {
        hitSound = Gdx.audio.newSound(Gdx.files.internal("Hit.wav"));
        pickupSound = Gdx.audio.newSound(Gdx.files.internal("Pickup.wav"));
        powerUpSound = Gdx.audio.newSound(Gdx.files.internal("PowerUp.wav"));
        footstepSound = Gdx.audio.newSound(Gdx.files.internal("footstep.wav"));
        gameOverSound = Gdx.audio.newSound(Gdx.files.internal("gameover.wav"));
        blockSound = Gdx.audio.newSound(Gdx.files.internal("block.wav"));
        configManager = new ConfigManager();

        spriteBatch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));
        
        transitionEffect = new de.tum.cit.fop.maze.VFX.TransitionEffect();

        // --- Custom UI Setup ---
        try {
            // Load custom font
            com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator generator = 
                new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator(Gdx.files.internal("other/Hoefler Text Regular.ttf"));
            com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter parameter = 
                new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 36;
            // Optional: Add border for visibility if buttons are light, but assuming dark text or white on dark
            // User requested specific font, I'll keep it simple first
            com.badlogic.gdx.graphics.g2d.BitmapFont customFont = generator.generateFont(parameter);
            generator.dispose();
            skin.add("hoefler", customFont);
            
            // Load button textures
            Texture btnBase = new Texture(Gdx.files.internal("selfmade/uielements/buttonbase.png"));
            Texture btnOn = new Texture(Gdx.files.internal("selfmade/uielements/buttonon.png"));
            Texture btnPressed = new Texture(Gdx.files.internal("selfmade/uielements/buttonpressed.png"));
            
            // Overwrite "default" TextButtonStyle
            com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle style = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle();
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable drawableUp = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnBase));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable drawableOver = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnOn));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable drawableDown = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnPressed));
            
            float scale = 0.4f;
            drawableUp.setMinWidth(drawableUp.getMinWidth() * scale);
            drawableUp.setMinHeight(drawableUp.getMinHeight() * scale);
            drawableOver.setMinWidth(drawableOver.getMinWidth() * scale);
            drawableOver.setMinHeight(drawableOver.getMinHeight() * scale);
            drawableDown.setMinWidth(drawableDown.getMinWidth() * scale);
            drawableDown.setMinHeight(drawableDown.getMinHeight() * scale);
            
            style.up = drawableUp;
            style.over = drawableOver;
            style.down = drawableDown;
            style.font = customFont;
            style.fontColor = com.badlogic.gdx.graphics.Color.BLACK; // Using Black text for now, assuming light button or vice versa. 
            // Wait, "buttonbase" often implies wood/stone. Let's try White first? Or check user intent.
            // craft/craftacular usually has white text on dark buttons.
            style.fontColor = com.badlogic.gdx.graphics.Color.WHITE; 
            
            skin.add("default", style, com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle.class);

            // Load SHORT button textures
            Texture btnBaseShort = new Texture(Gdx.files.internal("selfmade/uielements/buttonbaseshort.png"));
            Texture btnOnShort = new Texture(Gdx.files.internal("selfmade/uielements/buttononshort.png"));
            Texture btnPressedShort = new Texture(Gdx.files.internal("selfmade/uielements/buttonpressedshort.png"));

            // Define "short" style
            com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle shortStyle = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle();
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable drawableUpShort = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnBaseShort));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable drawableOverShort = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnOnShort));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable drawableDownShort = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnPressedShort));
            
            // Apply scale to short buttons too
            drawableUpShort.setMinWidth(drawableUpShort.getMinWidth() * scale);
            drawableUpShort.setMinHeight(drawableUpShort.getMinHeight() * scale);
            drawableOverShort.setMinWidth(drawableOverShort.getMinWidth() * scale);
            drawableOverShort.setMinHeight(drawableOverShort.getMinHeight() * scale);
            drawableDownShort.setMinWidth(drawableDownShort.getMinWidth() * scale);
            drawableDownShort.setMinHeight(drawableDownShort.getMinHeight() * scale);
            
            shortStyle.up = drawableUpShort;
            shortStyle.over = drawableOverShort;
            shortStyle.down = drawableDownShort;
            shortStyle.font = customFont;
            shortStyle.fontColor = com.badlogic.gdx.graphics.Color.WHITE;

            skin.add("short", shortStyle, com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle.class);

            // Define "keybinding" style (Short texture, 0.75 scale)
            com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle keyBindingStyle = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle();
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable kbUp = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnBaseShort));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable kbOver = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnOnShort));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable kbDown = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnPressedShort));
            
            float kbScale = 0.3f;
            kbUp.setMinWidth(kbUp.getMinWidth() * kbScale);
            kbUp.setMinHeight(kbUp.getMinHeight() * kbScale);
            kbOver.setMinWidth(kbOver.getMinWidth() * kbScale);
            kbOver.setMinHeight(kbOver.getMinHeight() * kbScale);
            kbDown.setMinWidth(kbDown.getMinWidth() * kbScale);
            kbDown.setMinHeight(kbDown.getMinHeight() * kbScale);
            
            keyBindingStyle.up = kbUp;
            keyBindingStyle.over = kbOver;
            keyBindingStyle.down = kbDown;
            keyBindingStyle.font = customFont;
            keyBindingStyle.fontColor = com.badlogic.gdx.graphics.Color.WHITE;

            skin.add("keybinding", keyBindingStyle, com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle.class);
            
            // Load MIDDLE button textures
            Texture btnBaseMiddle = new Texture(Gdx.files.internal("selfmade/uielements/buttonbasemiddle.png"));
            Texture btnOnMiddle = new Texture(Gdx.files.internal("selfmade/uielements/buttononmiddle.png"));
            Texture btnPressedMiddle = new Texture(Gdx.files.internal("selfmade/uielements/buttonpressedmiddle.png"));

            // Define "middle" style
            com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle middleStyle = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle();
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable drawableUpMiddle = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnBaseMiddle));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable drawableOverMiddle = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnOnMiddle));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable drawableDownMiddle = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnPressedMiddle));
            
            // Apply scale (assuming same scale as others)
            float middleScale = 0.4f;
            drawableUpMiddle.setMinWidth(drawableUpMiddle.getMinWidth() * middleScale);
            drawableUpMiddle.setMinHeight(drawableUpMiddle.getMinHeight() * middleScale);
            drawableOverMiddle.setMinWidth(drawableOverMiddle.getMinWidth() * middleScale);
            drawableOverMiddle.setMinHeight(drawableOverMiddle.getMinHeight() * middleScale);
            drawableDownMiddle.setMinWidth(drawableDownMiddle.getMinWidth() * middleScale);
            drawableDownMiddle.setMinHeight(drawableDownMiddle.getMinHeight() * middleScale);
            
            middleStyle.up = drawableUpMiddle;
            middleStyle.over = drawableOverMiddle;
            middleStyle.down = drawableDownMiddle;
            middleStyle.font = customFont;
            middleStyle.fontColor = com.badlogic.gdx.graphics.Color.WHITE;

            skin.add("middle", middleStyle, com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle.class);

            // Load LEVEL SELECT button textures
            Texture btnLevelBase = new Texture(Gdx.files.internal("selfmade/uielements/levelbuttonbase.png"));
            Texture btnLevelOn = new Texture(Gdx.files.internal("selfmade/uielements/levelbuttonon.png"));
            
            com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle levelStyle = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle();
            levelStyle.up = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnLevelBase));
            levelStyle.over = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnLevelOn));
            levelStyle.down = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(btnLevelOn));
            levelStyle.font = customFont;
            levelStyle.fontColor = com.badlogic.gdx.graphics.Color.WHITE; 
            
            skin.add("level", levelStyle, com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle.class);
            
        } catch (Exception e) {
            Gdx.app.error("MazeRunnerGame", "Failed to load custom UI assets", e);
        }
        // -----------------------

        this.loadCharacterAnimation();
        playerState = null;

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("Pixel Crypt Keep.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(configManager.getMusicVolume());
        backgroundMusic.play();

        warFogMusic = Gdx.audio.newMusic(Gdx.files.internal("fog of war.mp3"));
        warFogMusic.setLooping(true);
        warFogMusic.setVolume(configManager.getMusicVolume());

        bossFightMusic = Gdx.audio.newMusic(Gdx.files.internal("boss fight music.mp3"));
        bossFightMusic.setLooping(true);
        bossFightMusic.setVolume(configManager.getMusicVolume());

        // Start with StoryMenu instead of MenuScreen
        setScreen(new de.tum.cit.fop.maze.GameControl.StoryMenu(this));
    }


    /**
     * Switches to the Main Menu screen (Now redirects to StoryMenu in Game Hub mode).
     */
    public void goToMenu() {
        playTransition(() -> {
            // Stop Level/Boss Music
            if (warFogMusic != null && warFogMusic.isPlaying()) {
                warFogMusic.stop();
            }
            if (bossFightMusic != null && bossFightMusic.isPlaying()) {
                bossFightMusic.stop();
            }

            // Resume Background Music
            if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
                backgroundMusic.play();
            }

            this.setScreen(new de.tum.cit.fop.maze.GameControl.StoryMenu(this, true));
            if (gameScreen != null) {
                gameScreen.dispose();
                gameScreen = null;
            }
        });
    }


    /**
     * Switches to the Game screen to play a level.
     * @param mapFile The map file to load.
     */
    public void goToGame(FileHandle mapFile) {
        playTransition(() -> {
            this.setScreen(new GameScreen(this, mapFile));
            if (menuScreen != null) {
                menuScreen.dispose();
                menuScreen = null;
            }
        });
    }


    /**
     * Switches to the Story screen (e.g., intro or cutscene).
     * @param mapFile The map file associated with the story.
     */
    public void goToStory(FileHandle mapFile) {
        playTransition(() -> {
            this.setScreen(new StoryScreen(this, mapFile));
            if (menuScreen != null) {
                menuScreen.dispose();
                menuScreen = null;
            }
        });
    }
    

    /**
     * Switches to the Level Selection screen.
     */
    public void goToLevelSelect() {
        this.setScreen(new LevelSelectionScreen(this));
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }


    /**
     * Switches to the Settings screen.
     */
    public void goToSettings() {
        this.setScreen(new SettingsScreen(this));
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }
    

    public void goToEndlessMode(String playerName) {
        playTransition(() -> {
            int startDifficulty = 1;
            if (playerState != null) {
                startDifficulty = playerState.getEndlessWave();
            }

            GameScreen gs = new GameScreen(this, true, playerName);
            gs.setDifficulty(startDifficulty);
            this.setScreen(gs);

            if (menuScreen != null) {
                menuScreen.dispose();
                menuScreen = null;
            }
        });
    }

    /**
     * Starts an Endless Mode Ver2 game.
     * @param playerName The name of the player starting the run.
     */
    public void goToEndlessModeVer2(String playerName) {
        playTransition(() -> {
            // Ver2 uses a different tracking or resets wave?
            // For now, let's reuse playerState.endlessWave but assume it starts at 1
            // or modify GameScreen to handle "Floor 1-1" from difficulty 1.

            int startDifficulty = 1;
            if (playerState != null) {
                startDifficulty = playerState.getEndlessWave();
            }

            // Pass isEndlessVer2 = true (Need to modify GameScreen constructor first, but I will do it next)
            // For now, I will use a temporary constructor signature or setter.
            // Actually, I'll update GameScreen constructor signature in the next step.
            // I'll call a new setter setEndlessVer2(true).
            
            GameScreen gs = new GameScreen(this, true, playerName);
            gs.setEndlessVer2(true); // Will add this method in GameScreen
            gs.setDifficulty(startDifficulty);
            this.setScreen(gs);

            if (menuScreen != null) {
                menuScreen.dispose();
                menuScreen = null;
            }
        });
    }
    

    /**
     * Updates the background music volume from the configuration manager.
     */
    public void updateMusicVolume() {
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(configManager.getMusicVolume());
        }
    }

    /**
     * gets the configuration manager.
     * @return The ConfigManager instance.
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }


    /**
     * Loads the character walking animation from texture files.
     */
    private void loadCharacterAnimation() {
        Texture walkSheet = new Texture(Gdx.files.internal("character.png"));

        int frameWidth = 16;
        int frameHeight = 32;
        int animationFrames = 4;


        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);


        for (int col = 0; col < animationFrames; col++) {
            walkFrames.add(new TextureRegion(walkSheet, col * frameWidth, 0, frameWidth, frameHeight));
        }

        characterDownAnimation = new Animation<>(0.1f, walkFrames);
    }


    /**
     * Disposes of all resources used by the game.
     */
    @Override
    public void dispose() {
        getScreen().hide();
        getScreen().dispose();
        spriteBatch.dispose();
        skin.dispose();
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
        if(hitSound != null) {
            hitSound.dispose();
        }
        if(pickupSound != null) {
            pickupSound.dispose();
        }
        if(powerUpSound != null) {
            powerUpSound.dispose();
        }
        if(footstepSound != null) {
            footstepSound.dispose();
        }
        if(gameOverSound != null) {
            gameOverSound.dispose();
        }
    }


    /**
     * Gets the UI skin.
     * @return The Skin instance.
     */
    public Skin getSkin() {
        return skin;
    }

    /**
     * Gets the character's downward animation.
     * @return The Animation for walking down.
     */
    public Animation<TextureRegion> getCharacterDownAnimation() {
        return characterDownAnimation;
    }

    /**
     * Gets the main sprite batch.
     * @return The SpriteBatch instance.
     */
    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }


    /**
     * Gets the current player state.
     * @return The PlayerState instance.
     */
    public PlayerState getPlayerState() {
        return playerState;
    }

    private int currentSlotIndex = -1;

    /**
     * Switches to the Skill Tree screen.
     */
    public void goToSkillTree() {
        this.setScreen(new SkillTreeScreen(this));
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }


    /**
     * Plays the hit sound effect.
     */
    public void playHitSound() {
        hitSound.play(configManager.getSoundVolume());
    }

    /**
     * Plays the pickup sound effect.
     */
    public void playPickupSound() {
        pickupSound.play(configManager.getSoundVolume());
    }

    /**
     * Plays the power-up sound effect.
     */
    public void playPowerUpSound() {
        powerUpSound.play(configManager.getSoundVolume());
    }

    /**
     * Plays the footstep sound effect.
     */
    public void playFootstepSound() {

        footstepSound.play(configManager.getSoundVolume() * 0.6f);
    }

    /**
     * Plays the game over sound effect.
     */
    public void playGameOverSound() {
        gameOverSound.play(configManager.getSoundVolume());
    }

    /**
     * Plays the block sound effect.
     */
    public void playBlockSound(){
        blockSound.play(configManager.getSoundVolume());
    }




    /**
     * Saves the current game state to the current slot.
     */
    public void saveGame() {
        if (currentSlotIndex != -1) {
            de.tum.cit.fop.maze.GameControl.GameSaveManager.saveGame(playerState, currentSlotIndex);
        }
    }

    /**
     * Loads the game state from the specified slot.
     * @param slotIndex The index of the save slot.
     * @return True if load was successful, false otherwise.
     */
    public boolean loadGame(int slotIndex) {
        PlayerState loaded = de.tum.cit.fop.maze.GameControl.GameSaveManager.loadGame(slotIndex);
        if (loaded != null) {
            this.playerState = loaded;
            this.currentSlotIndex = slotIndex;
            return true;
        }
        return false;
    }

    /**
     * Starts a new game with the given player name in the specified slot.
     * @param name The player's name.
     * @param slotIndex The save slot index.
     */
    public void startNewGame(String name, int slotIndex) {
        this.playerState = new PlayerState();
        this.playerState.setUsername(name);
        this.currentSlotIndex = slotIndex;


        de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance().resetAchievements();

        saveGame();
    }

    /**
     * Unloads the current game and resets player state.
     */
    public void unloadGame() {
        this.playerState = null;
        this.currentSlotIndex = -1;
    }

    public Music getCurrentMusic(){
        if(backgroundMusic != null){
            if(backgroundMusic.isPlaying()){
                return backgroundMusic;
            }
        }
        if(warFogMusic!=null){
            if(warFogMusic.isPlaying()) {
                return warFogMusic;
            }
        }
        if(bossFightMusic != null){
            if(bossFightMusic.isPlaying()){
                return bossFightMusic;
            }
        }
        return null;
    }
    @Override
    public void render() {
        super.render();
        if (transitionEffect != null) {
            float delta = Gdx.graphics.getDeltaTime();
            transitionEffect.update(delta);
            transitionEffect.render(spriteBatch);
        }
    }

    /**
     * Plays the transition effect and executes the given action when screen is covered.
     * @param onCovered The action to execute (e.g., switching screens).
     */
    public void playTransition(Runnable onCovered) {
        if (transitionEffect != null) {
            transitionEffect.start(onCovered);
        } else {
            // Fallback if transition is null for some reason
            onCovered.run();
        }
    }
}
