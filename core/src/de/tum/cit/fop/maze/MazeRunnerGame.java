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
    private Music backgroundMusic;

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
        this.loadCharacterAnimation();
        playerState = null;

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("Pixel Crypt Keep.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(configManager.getMusicVolume());
        backgroundMusic.play();

        goToMenu();
    }


    /**
     * Switches to the Main Menu screen.
     */
    public void goToMenu() {
        this.setScreen(new MenuScreen(this));
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
    }


    /**
     * Switches to the Game screen to play a level.
     * @param mapFile The map file to load.
     */
    public void goToGame(FileHandle mapFile) {
        this.setScreen(new GameScreen(this, mapFile));
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }


    /**
     * Switches to the Story screen (e.g., intro or cutscene).
     * @param mapFile The map file associated with the story.
     */
    public void goToStory(FileHandle mapFile) {
        this.setScreen(new StoryScreen(this, mapFile));
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
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
    

    /**
     * Starts an Endless Mode game.
     * @param playerName The name of the player starting the run.
     */
    public void goToEndlessMode(String playerName) {

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
        goToMenu();
    }

    /**
     * Unloads the current game and resets player state.
     */
    public void unloadGame() {
        this.playerState = null;
        this.currentSlotIndex = -1;
    }

    public void setScreen(Screen screen) {
        super.setScreen(screen);

    }
}
