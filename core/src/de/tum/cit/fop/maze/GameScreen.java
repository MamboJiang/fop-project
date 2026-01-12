package de.tum.cit.fop.maze;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.GameControl.LeaderboardManager;
import de.tum.cit.fop.maze.GameObj.Character;
import de.tum.cit.fop.maze.GameObj.EntryPoint;
import de.tum.cit.fop.maze.GameObj.GameObject;
import de.tum.cit.fop.maze.GameControl.HUD;
import de.tum.cit.fop.maze.GameControl.PauseMenu;
import de.tum.cit.fop.maze.GameControl.GameOverMenu;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.List;


/**
 * The GameScreen class is responsible for the main gameplay loop.
 * It handles rendering, updates, input, and game logic like level generation and game over states.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private HUD hud;
    

    private boolean debugEnabled = false;


    private boolean isPaused = false;
    private boolean isGameOver = false;
    private Stage pauseStage;
    private PauseMenu pauseMenu;
    private GameOverMenu GameOverMenu;


    private de.tum.cit.fop.maze.GameObj.Character character;
    private List<GameObject> mapObjects;
    private List<de.tum.cit.fop.maze.GameObj.Enemy> enemies;
    private FileHandle mapFile;
    private de.tum.cit.fop.maze.AI.Grid grid;
    private List<de.tum.cit.fop.maze.VFX.DamageNumber> damageNumbers;
    private boolean isProcedural = false;
    private int currentDifficulty = 1;
    private float levelTimer = 0f;
    private int score = 0;
    private static final int BASE_SCORE_PER_LEVEL = 1000;
    private static final int PENALTY_PER_SECOND = 10;
    private static final int SCORE_PER_LIFE = 500;

    private String playerName = "Player";
    private int totalRunScore = 0;
    

    private String currentLevelName = "Unknown";

    /**
     * Constructor for loading a specific map file.
     * @param game The main game class.
     * @param mapFile The map file to load.
     */
    public GameScreen(MazeRunnerGame game, FileHandle mapFile) {
        this.game = game;
        this.mapFile = mapFile;
        this.isProcedural = false;
        if (mapFile != null) {
            this.currentLevelName = mapFile.nameWithoutExtension();
        }

        initCommon();
        setupLevel();
    }
    

    /**
     * Constructor for Procedural / Endless modes.
     * @param game The main game class.
     * @param isProcedural Whether the level should be generated procedurally.
     * @param playerName The name of the player.
     */
    public GameScreen(MazeRunnerGame game, boolean isProcedural, String playerName) {
        this.game = game;
        this.isProcedural = isProcedural;
        this.currentDifficulty = 1;

        this.playerName = playerName;
        this.totalRunScore = 0;
        
        initCommon();
        setupLevel();
    }
    
    /**
     * Sets the difficulty for procedural generation.
     * @param difficulty The difficulty level.
     */
    public void setDifficulty(int difficulty) {
        this.currentDifficulty = difficulty;

        generateProceduralLevel();
    }
    
    /**
     * Common initialization for camera, viewport, HUD, and other systems.
     */
    private void initCommon() {
        camera = new OrthographicCamera();
        camera.zoom = 0.7f; 
        
        viewport = new ExtendViewport(640, 360, camera);


        font = game.getSkin().getFont("font");
        
        shapeRenderer = new ShapeRenderer();
        hud = new HUD(game.getSpriteBatch(), this, game.getSkin());
        screenShake = new de.tum.cit.fop.maze.VFX.ScreenShake();

        setupPauseMenu();
    }

    /**
     * Sets up the level, either loading from file or generating procedurally.
     */
    private void setupLevel() {
        if (isProcedural) {
             generateProceduralLevel();
             return;
        }


        if(this.mapFile ==null || !this.mapFile.exists()){
            Gdx.app.error("GameScreen", "Map file is null or does not exist!");
            this.mapFile = Gdx.files.internal("maps/level-6.properties");
        }

        mapObjects = MapLoader.loadMap(this.mapFile);

        initMapObjects();
    }

    /**
     * Generates a procedurally generated level using DungeonGenerator.
     */
    private void generateProceduralLevel() {

        int size = 40 + (currentDifficulty * 2);
        if (size > 100) size = 100;
        
        de.tum.cit.fop.maze.Procedure.DungeonGenerator generator = new de.tum.cit.fop.maze.Procedure.DungeonGenerator(size, size);
        mapObjects = generator.generate(currentDifficulty);
        
        initMapObjects();
    }
    
    /**
     * Initializes game objects (Player, Enemies, Items) from the map data.
     */
    private void initMapObjects() {

        grid = new de.tum.cit.fop.maze.AI.Grid(0, 0, mapObjects);

        float spawnX = 0;
        float spawnY = 0;
        for (GameObject obj : mapObjects) {
            if (obj instanceof EntryPoint) {
                spawnX = obj.getPosition().x;
                spawnY = obj.getPosition().y;
                break;
            }
        }

        if (character == null) {

            character = new Character(spawnX+16, spawnY, game.getPlayerState(), game);
        } else {
            character.setPosition(spawnX + 16, spawnY);
        }

        if (isProcedural) {
            de.tum.cit.fop.maze.GameObj.PlayerState state = game.getPlayerState();
            if (state.getCurrentRunScore() > 0) {
                this.totalRunScore = state.getCurrentRunScore();
            }
            if (state.getCurrentRunHealth() > 0) {
                character.setCurrentHealth(state.getCurrentRunHealth());
            }
        }

        

        enemies = new java.util.ArrayList<>();

        List<GameObject> toRemove = new java.util.ArrayList<>();
        for (GameObject obj : mapObjects) {
             if (obj instanceof de.tum.cit.fop.maze.GameObj.EnemySpawnPoint) {
                 enemies.add(new de.tum.cit.fop.maze.GameObj.Enemy(
                     obj.getPosition().x, 
                     obj.getPosition().y, 
                     de.tum.cit.fop.maze.MapLoader.getMobAnimations(0, 0), // Base Enemy: Col 0, Row 0
                     grid, 
                     character
                 ));
                 toRemove.add(obj);
             } else if (obj instanceof de.tum.cit.fop.maze.GameObj.GhostSpawnPoint) {
                 enemies.add(new de.tum.cit.fop.maze.GameObj.Ghost(
                     obj.getPosition().x, 
                     obj.getPosition().y, 
                     grid, 
                     character
                 ));
                 toRemove.add(obj);
             }
        }

        mapObjects.removeAll(toRemove);

        java.util.Map<String, java.util.List<GameObject>> chunks = new java.util.HashMap<>();
        int chunkSize = 16 * 16;

        for (GameObject obj : mapObjects) {
            if (obj instanceof de.tum.cit.fop.maze.GameObj.Path) {
                int cx = (int) (obj.getPosition().x / chunkSize);
                int cy = (int) (obj.getPosition().y / chunkSize);
                String key = cx + "," + cy;
                
                if (!chunks.containsKey(key)) {
                    chunks.put(key, new java.util.ArrayList<>());
                }
                chunks.get(key).add(obj);
            }
        }
        

        for (java.util.List<GameObject> chunkPaths : chunks.values()) {
            if (com.badlogic.gdx.math.MathUtils.randomBoolean(0.5f)) { 
                GameObject randomPath = chunkPaths.get(com.badlogic.gdx.math.MathUtils.random(chunkPaths.size() - 1));
                de.tum.cit.fop.maze.GameObj.Heart heart = new de.tum.cit.fop.maze.GameObj.Heart(randomPath.getPosition().x, randomPath.getPosition().y);
                mapObjects.add(heart);
            }

            if (com.badlogic.gdx.math.MathUtils.randomBoolean(0.2f)) {
                GameObject randomPath = chunkPaths.get(com.badlogic.gdx.math.MathUtils.random(chunkPaths.size() - 1));

                de.tum.cit.fop.maze.GameObj.ShieldItem shield = new de.tum.cit.fop.maze.GameObj.ShieldItem(randomPath.getPosition().x, randomPath.getPosition().y);
                mapObjects.add(shield);
            }
        }
        
        damageNumbers = new java.util.ArrayList<>();
    }

    /**
     * Sets up the pause menu UI.
     */
    private void setupPauseMenu() {
        pauseStage = new Stage(new FitViewport(1920, 1080), game.getSpriteBatch());
        
        pauseMenu = new PauseMenu(game, 
            () -> togglePause(),
            () -> {
                if (isProcedural) {
                    Dialog dialog = new Dialog("Endless Mode", game.getSkin()) {
                        @Override
                        protected void result(Object object) {
                            if (object instanceof Integer) {
                                int choice = (Integer) object;
                                if (choice == 1) {

                                    game.getPlayerState().setEndlessWave(currentDifficulty); 

                                    game.getPlayerState().setCurrentRunScore(totalRunScore);
                                    if (character != null) {
                                        game.getPlayerState().setCurrentRunHealth(character.getCurrentHealth());
                                    }
                                    
                                    game.saveGame();
                                    game.goToMenu();
                                } else if (choice == 2) {

                                    game.getPlayerState().resetEndlessWave(); 
                                    game.getPlayerState().resetRunState();
                                    pauseMenu.hide();
                                    isPaused = false; 
                                    showGameOverMenu(false); 
                                } else {

                                }
                            }
                        }
                    };
                    dialog.text("Save Difficulty or End Run (Submit Score)?\nNew Run will be: Lv " + currentDifficulty);
                    dialog.button("Save & Quit", 1);
                    dialog.button("End Game (Submit Score)", 2);
                    dialog.button("Cancel", 0);
                    dialog.show(pauseStage);
                } else {
                    game.goToMenu();
                }
            }
        );
        pauseStage.addActor(pauseMenu);
    }

    /**
     * Loads the next level. If procedural, generates a new harder level.
     * If story/classic, loads the next map file.
     */
    private void loadNextLevel() {
        if (isProcedural) {
            totalRunScore += calculateScore();


            currentDifficulty++;
            de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance().onStatusUpdate(de.tum.cit.fop.maze.GameControl.EventType.REACH_DIFFICULTY, currentDifficulty);
            

            isGameOver = false;
            isPaused = false;
            levelTimer = 0f;
            if (character != null) {
                character.resetForNewLevel();
            }
            

            if (GameOverMenu != null) {
                GameOverMenu.remove();
            }
            if (pauseMenu != null) {
                pauseMenu.hide();
            }
            

            updateInputProcessor();
            

            generateProceduralLevel();
            return;
        }


        List<FileHandle> maps = MapLoader.getMapFiles();
        int currentIndex = -1;


        for (int i = 0; i < maps.size(); i++) {

            if (maps.get(i).name().equals(this.mapFile.name())) {
                currentIndex = i;
                break;
            }
        }


        if (currentIndex != -1 && currentIndex + 1 < maps.size()) {

            FileHandle nextMap = maps.get(currentIndex + 1);
            game.goToGame(nextMap);
        } else {

            game.goToMenu();
        }
    }

    /**
     * Awards XP to the player.
     * @param win Whether the level was won.
     * @return The amount of XP awarded.
     */
    private int awardXP(boolean win) {
        if (win) {

            if (!isProcedural && currentLevelName != null) {
                game.getPlayerState().addCompletedLevel(currentLevelName);
            }
            
            int xpEarned = 50;


            if (isProcedural) {
                game.getPlayerState().addCurrentRunXP(xpEarned);
                System.out.println("Run XP: " + game.getPlayerState().getCurrentRunXP());
            } else {
                game.getPlayerState().addXP(xpEarned);
                System.out.println("Awarded " + xpEarned + " XP");
            }
            

            game.saveGame();
            
            return xpEarned;
        }
        return 0;
    }

    /**
     * Shows the Game Over menu with appropriate options (Win/Loss).
     * @param win True if the player won the level/run.
     */
    private void showGameOverMenu(boolean win) {
        if (isGameOver) return;
        isGameOver = true;

        int awardXP = awardXP(win);
        

        if (isProcedural && !win) {

            int runXP = game.getPlayerState().getCurrentRunXP();
            game.getPlayerState().addXP(runXP);
            awardXP = runXP;
            
            game.getPlayerState().resetEndlessWave();
            game.getPlayerState().resetRunState();
            game.saveGame();
        }


        int currentLevelScore = win ? calculateScore() : 0;
        int finalDisplayScore = isProcedural ? (totalRunScore + currentLevelScore) : currentLevelScore;
        

        int waves = -1;
        if (isProcedural) {
            waves = win ? currentDifficulty : currentDifficulty - 1;
        }
        
        GameOverMenu = new GameOverMenu(game,
                () -> {

                    if (isProcedural) {
                        game.goToEndlessMode(playerName);
                    } else {
                        game.goToGame(this.mapFile);
                    }
                },
                () -> {

                    if (isProcedural && win) {
                         com.badlogic.gdx.scenes.scene2d.ui.Dialog dialog = new com.badlogic.gdx.scenes.scene2d.ui.Dialog("Endless Mode", game.getSkin()) {
                            @Override
                            protected void result(Object object) {
                                if (object instanceof Integer) {
                                    int choice = (Integer) object;
                                    if (choice == 1) {

                                        game.getPlayerState().setEndlessWave(currentDifficulty + 1);

                                        game.getPlayerState().setCurrentRunScore(totalRunScore + calculateScore());
                                        if (character != null) {
                                            game.getPlayerState().setCurrentRunHealth(character.getCurrentHealth());
                                        }
                                        
                                        game.saveGame();
                                        game.goToMenu();
                                    } else if (choice == 2) {

                                        totalRunScore += calculateScore();
                                        game.getPlayerState().resetEndlessWave();
                                        game.getPlayerState().resetRunState();
                                        
                                        if (GameOverMenu != null) GameOverMenu.remove();
                                        isGameOver = false;
                                        showGameOverMenu(false);
                                    } else {

                                    }
                                }
                            }
                        };
                        dialog.text("Save Difficulty or End Run (Submit Score)?\nNew Run will be: Lv " + (currentDifficulty + 1));
                        dialog.button("Save & Quit", 1);
                        dialog.button("End Game (Submit Score)", 2);
                        dialog.button("Cancel", 0);
                        dialog.show(pauseStage);
                    } else {
                        game.goToMenu();
                    }
                },
                () -> {
                    loadNextLevel();
                },
                win,
                waves,
                finalDisplayScore,
                awardXP
        );

        if (isProcedural) {

            if (!win) {
                de.tum.cit.fop.maze.GameControl.LeaderboardManager.saveScore(playerName, finalDisplayScore, () -> {
                    if (GameOverMenu != null) GameOverMenu.loadLeaderboard();
                });
                de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance().onEvent(de.tum.cit.fop.maze.GameControl.EventType.GAME_OVER, 1);
            }
        } else {
            if (!win) {
                de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance().onEvent(de.tum.cit.fop.maze.GameControl.EventType.GAME_OVER, 1);
            }
        }

        pauseStage.addActor(GameOverMenu);
        GameOverMenu.show();
        game.playGameOverSound();


        Gdx.input.setInputProcessor(pauseStage);
    }


    /**
     * Toggles the pause state of the game.
     */
    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            if (isProcedural) {
                pauseMenu.setStatsVisible(true);
                int xpToShow = game.getPlayerState().getCurrentRunXP();
                pauseMenu.updateStats(totalRunScore, currentDifficulty, xpToShow);
            } else {
                pauseMenu.setStatsVisible(false);
            }
            pauseMenu.show();
        } else {
            pauseMenu.hide();
        }
        updateInputProcessor();
    }
    
    /**
     * Updates the input processor based on current game state (Paused vs Running).
     */
    public void updateInputProcessor() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        if (isPaused) {
            multiplexer.addProcessor(pauseStage);
        } else {

            multiplexer.addProcessor(hud.getStage());
        }
        Gdx.input.setInputProcessor(multiplexer);
    }
    
    /**
     * Toggles debug rendering mode.
     */
    public void toggleDebug() {
        debugEnabled = !debugEnabled;
    }
    
    /**
     * Zooms the camera in.
     */
    public void zoomIn() {
        camera.zoom = Math.max(0.1f, camera.zoom - 0.1f);
        camera.update();
    }
    
    /**
     * Zooms the camera out.
     */
    public void zoomOut() {
        camera.zoom = Math.min(2.0f, camera.zoom + 0.1f);
        camera.update();
    }



    private de.tum.cit.fop.maze.VFX.ScreenShake screenShake;


    /**
     * Main render loop.
     * @param delta Time since last frame in seconds.
     */
    @Override
    public void render(float delta) {

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F3)) {
            toggleDebug();
        }


        if (Gdx.input.isKeyJustPressed(game.getConfigManager().getKey("PAUSE"))) {
            togglePause();
        }

        ScreenUtils.clear(0, 0, 0, 1);
        boolean isLevelCompleted = character.isLevelCompleted();
        levelTimer += delta;

        if (!isPaused && !isGameOver && !isLevelCompleted) {
            if (character != null) {

                if (character.isScreenShakeRequested()) {
                    if (screenShake != null) screenShake.start(0.3f, 0.8f);
                    character.clearScreenShakeRequest();
                }

                if (character.isBlockEffectRequested()) {
                    System.out.println("Spawning BLOCK effect!");
                    damageNumbers.add(new de.tum.cit.fop.maze.VFX.DamageNumber(
                            character,
                            "BLOCK",
                            com.badlogic.gdx.graphics.Color.CYAN
                    ));
                    character.clearBlockEffectRequest();
                }



                if (character.isDamageNumberRequested()) {
                     damageNumbers.add(new de.tum.cit.fop.maze.VFX.DamageNumber(character, 1));
                     character.clearDamageNumberRequest();
                }

                character.update(delta, mapObjects, game.getConfigManager());


                float targetX = character.getPosition().x + 8;
                float targetY = character.getPosition().y + 16;
                

                float lerpSpeed = 5f;
                camera.position.x += (targetX - camera.position.x) * lerpSpeed * delta;
                camera.position.y += (targetY - camera.position.y) * lerpSpeed * delta;
                

                if (screenShake != null) {
                    screenShake.update(delta, camera);
                } else {
                    camera.update();
                }
            }
            if(mapObjects != null){
                mapObjects.removeIf(GameObject::isMarkedForRemoval);
            }
            if (character.isLevelCompleted()) {
                de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance().onEvent(de.tum.cit.fop.maze.GameControl.EventType.LEVEL_COMPLETE, 1);
                showGameOverMenu(true);

            }
            if(character.isDead()){
                showGameOverMenu(false);
            }
        }


        viewport.apply();
        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        game.getSpriteBatch().begin();


        if (mapObjects != null) {
            for (GameObject obj : mapObjects) {
                if (obj instanceof de.tum.cit.fop.maze.GameObj.Heart) {
                     ((de.tum.cit.fop.maze.GameObj.Heart) obj).update(delta);
                } else if (obj instanceof de.tum.cit.fop.maze.GameObj.ShieldItem) {
                     ((de.tum.cit.fop.maze.GameObj.ShieldItem) obj).update(delta);
                }
                
                if (obj.getTextureRegion() != null) {
                    game.getSpriteBatch().draw(obj.getTextureRegion(), obj.getPosition().x, obj.getPosition().y, obj.getWidth(), obj.getHeight());
                }
            }
        }


        if (character != null) {
            character.draw(game.getSpriteBatch());
        }
        

        for (de.tum.cit.fop.maze.GameObj.Enemy enemy : enemies) {
            enemy.draw(game.getSpriteBatch());

            enemy.drawStatus(game.getSpriteBatch(), font, debugEnabled);
        }
        

        if (damageNumbers != null) {
            java.util.Iterator<de.tum.cit.fop.maze.VFX.DamageNumber> iter = damageNumbers.iterator();
            while (iter.hasNext()) {
                de.tum.cit.fop.maze.VFX.DamageNumber dn = iter.next();
                dn.render(game.getSpriteBatch(), font);
                
                if (!isPaused && !isGameOver && !character.isLevelCompleted()) {
                    dn.update(delta);
                }
                
                if (dn.isFinished()) {
                    iter.remove();
                }
            }
        }

        game.getSpriteBatch().end();
        

        if (!isPaused && !isGameOver && !character.isLevelCompleted()) {
            java.util.Iterator<de.tum.cit.fop.maze.GameObj.Enemy> enemyIter = enemies.iterator();
            while (enemyIter.hasNext()) {
                de.tum.cit.fop.maze.GameObj.Enemy enemy = enemyIter.next();
                enemy.update(delta);
                if (enemy.isMarkedForRemoval()) {
                    de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance().onEvent(de.tum.cit.fop.maze.GameControl.EventType.KILL_ENEMY, 1);
                    enemyIter.remove();
                }
            }
        }
        

        if (character != null) {
            hud.update(character);
            hud.render(delta);
        }
        
        

        if (debugEnabled && character != null) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            

            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(
                character.getBounds().x, 
                character.getBounds().y, 
                character.getBounds().width, 
                character.getBounds().height
            );
            

            shapeRenderer.setColor(Color.GREEN);
            if (mapObjects != null) {
                for (GameObject obj : mapObjects) {
                    if (obj instanceof de.tum.cit.fop.maze.GameObj.Wall) {
                        shapeRenderer.rect(
                            obj.getBounds().x, 
                            obj.getBounds().y, 
                            obj.getBounds().width, 
                            obj.getBounds().height
                        );
                    }
                }
            }

            for (de.tum.cit.fop.maze.GameObj.Enemy enemy : enemies) {
                enemy.drawDebug(shapeRenderer);
            }
            
            shapeRenderer.end();
        }


        if (isPaused || isGameOver || isLevelCompleted) {
            pauseStage.act(delta);
            pauseStage.draw();
        }
    }


    /**
     * Resizes the viewport and UI stages.
     * @param width New width.
     * @param height New height.
     */
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        pauseStage.getViewport().update(width, height, true);
        hud.resize(width, height);
    }


    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    /**
     * Called when this screen becomes the current screen.
     */
    @Override
    public void show() {
        updateInputProcessor();
    }

    @Override
    public void hide() {
    }

    /**
     * Disposes of the screen resources.
     */
    @Override
    public void dispose() {
        if (pauseStage != null) pauseStage.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (hud != null) hud.dispose();
    }

    /**
     * Calculates the score for the current level.
     * @return The calculated score.
     */
    public int calculateScore() {
        int lifeScore = 0;
        if (character != null) {
            lifeScore = character.getLives() * SCORE_PER_LIFE;
        }

        int timePenalty = (int) (levelTimer * PENALTY_PER_SECOND);
        int totalScore = BASE_SCORE_PER_LEVEL - timePenalty;


        if (isProcedural) {
            totalScore *= currentDifficulty;
        }

        totalScore += lifeScore;

        return Math.max(0, totalScore);
    }


    /**
     * Gets the formatted level timer.
     * @return String in MM:SS format.
     */
    public String getFormattedTime() {
        int minutes = (int) levelTimer / 60;
        int seconds = (int) levelTimer % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Returns the main game instance.
     * @return MazeRunnerGame instance.
     */
    public MazeRunnerGame getGame() {
        return game;
    }
}
