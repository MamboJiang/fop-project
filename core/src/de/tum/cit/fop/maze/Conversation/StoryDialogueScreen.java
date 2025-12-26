package de.tum.cit.fop.maze.Conversation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.GameControl.PauseMenu;
import java.util.ArrayList;
import java.util.List;

public class StoryDialogueScreen implements Screen {
    private final MazeRunnerGame game;
    private Stage stage;
    private DialogueBox dialogueBox;
    private int conversationIndex = 0;
    
    // Characters
    private Image leftChar;
    private Image rightChar;
    
    // UI
    private boolean isAutoPlay = false;
    private boolean isPaused = false;
    
    // History
    private List<String> historyLog;
    private Window logWindow;
    private Label logLabel;
    
    private final String[] texts = {
        "Welcome to the Story Mode.",
        "Here, the characters stand tall and proud.",
        "The dialogue box spans the entire bottom of the screen.",
        "It feels just like a visual novel!",
        "You can Auto-Play or Pause from the top right menu.",
        "And now you can review the history too!",
        "Enjoy the story!"
    };
    
    // Associated speaker names for history
    private final String[] speakerNames = {
        "Hero", "Narrator", "Hero", "Narrator", "Hero", "Narrator", "Hero"
    };
    
    private final boolean[] speakers = { true, false, true, false, true, true, true };

    // Pause
    private PauseMenu pauseMenu;
    
    // ... (rest of class)

    public StoryDialogueScreen(MazeRunnerGame game) {
        this.game = game;
        this.historyLog = new ArrayList<>();
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(1920, 1080));
        Gdx.input.setInputProcessor(stage);
        
        // 1. Background
        // ... (Keep existing layout)
        
        // 2. Characters Container
        Table charTable = new Table();
        charTable.setFillParent(true);
        stage.addActor(charTable);
        
        Texture charTexture = new Texture(Gdx.files.internal("character.png"));
        Texture mobsTexture = new Texture(Gdx.files.internal("mobs.png"));
        Texture objectsTexture = new Texture(Gdx.files.internal("objects.png"));
        
        TextureRegion[][] charTmp = TextureRegion.split(charTexture, 16, 32);
        TextureRegion[][] mobsTmp = TextureRegion.split(mobsTexture, 16, 16);
        
        leftChar = new Image(charTmp[0][0]);
        rightChar = new Image(mobsTmp[0][0]);
        
        float rawHeight = 32f;
        float targetHeight = 1720f * 0.75f;
        float targetHeight2 = 1080f * 0.75f;
        float scale = targetHeight / rawHeight;
        float scale2 = targetHeight2 / rawHeight;
        
        charTable.bottom();
        charTable.add(leftChar).height(targetHeight).width(16*scale).padBottom(-100).expandX().left().padLeft(0);
        charTable.add(rightChar).height(targetHeight2).width(32*scale2).padBottom(-100).expandX().right().padRight(-100);
        
        // 3. Dialogue Box Layer
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        stage.addActor(uiTable);
        
        uiTable.bottom();
        
        dialogueBox = new DialogueBox(game.getSkin(), objectsTexture);
        dialogueBox.setTailDirection(DialogueBox.TailDirection.NONE);
        dialogueBox.setAutoSize(false);
        dialogueBox.setSize(1800, 300); 
        
        uiTable.add(dialogueBox).width(1800).height(500).padBottom(50);
        
        // 4. Top Right Menu
        Table menuTable = new Table();
        menuTable.setFillParent(true);
        menuTable.top().right();
        stage.addActor(menuTable);
        
        final TextButton autoBtn = new TextButton("Auto: OFF", game.getSkin());
        autoBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isAutoPlay = !isAutoPlay;
                autoBtn.setText(isAutoPlay ? "Auto: ON" : "Auto: OFF");
            }
        });
        
        TextButton logBtn = new TextButton("Log", game.getSkin());
        logBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showLogWindow();
            }
        });
        
        TextButton menuBtn = new TextButton("Menu", game.getSkin());
        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showPauseMenu();
            }
        });
        
        menuTable.add(autoBtn).pad(20);
        menuTable.add(logBtn).pad(20);
        menuTable.add(menuBtn).pad(20);
        
        // 5. Features
        createLogWindow();
        createPauseMenu(); // Ensure created
        
        updateDialogue();
        
        // Click listener for advancement or skipping
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (logWindow.isVisible()) return; // Let log window handle its own clicks
                if (pauseMenu.isVisible()) return;

                if (!dialogueBox.isFinished()) {
                    dialogueBox.skipTypewriter();
                } else if (!isAutoPlay && !isPaused) {
                    advanceDialogue();
                }
            }
        });
    }
    
    private void createLogWindow() {
        logWindow = new Window("Dialogue History", game.getSkin());
        logWindow.setVisible(false);
        logWindow.setModal(true);
        logWindow.setMovable(true);
        logWindow.setSize(1000, 800);
        logWindow.setPosition(1920/2f - 500, 1080/2f - 400);
        
        logLabel = new Label("", game.getSkin());
        logLabel.setWrap(true);
        logLabel.setAlignment(com.badlogic.gdx.utils.Align.topLeft); // Better reading
        
        ScrollPane scrollPane = new ScrollPane(logLabel, game.getSkin());
        scrollPane.setFadeScrollBars(false);
        
        logWindow.add(scrollPane).grow().pad(20);
        logWindow.row();
        
        TextButton closeBtn = new TextButton("Close", game.getSkin());
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                logWindow.setVisible(false);
            }
        });
        logWindow.add(closeBtn).pad(10);
        
        stage.addActor(logWindow);
    }
    
    private void createPauseMenu() {
        pauseMenu = new PauseMenu(game, 
            () -> { // On Resume
                isPaused = false;
            }, 
            null // On Exit (PauseMenu handles resizing/hiding itself mostly, but exit handles game state)
                 // actually PauseMenu defaults to game.goToMenu() for exit, no callback needed for logic here unless cleanup
        );
        stage.addActor(pauseMenu);
    }
    
    private void showPauseMenu() {
        isPaused = true;
        pauseMenu.show();
    }
    
    private void showLogWindow() {
        StringBuilder sb = new StringBuilder();
        for (String entry : historyLog) {
            sb.append(entry).append("\n\n");
        }
        logLabel.setText(sb.toString());
        logWindow.setVisible(true);
        logWindow.toFront(); // Key fix for visibility
    }
    
    private void advanceDialogue() {
        conversationIndex++;
        if (conversationIndex >= texts.length) {
            game.goToMenu();
            return;
        }
        updateDialogue();
    }
    
    private void updateDialogue() {
        String text = texts[conversationIndex];
        boolean isLeft = speakers[conversationIndex];
        String name = conversationIndex < speakerNames.length ? speakerNames[conversationIndex] : "Unknown";
        
        String logEntry = "[" + name + "]: " + text;
        if (historyLog.isEmpty() || !historyLog.get(historyLog.size()-1).equals(logEntry)) {
            historyLog.add(logEntry);
        }
        
        // Use fixed width for typewriter mode to ensure it doesn't jump
        dialogueBox.show(text, DialogueBox.DialogueType.NORMAL, 1700f);
        
        if (isLeft) {
            leftChar.setColor(1, 1, 1, 1);
            rightChar.setColor(0.5f, 0.5f, 0.5f, 1);
        } else {
            leftChar.setColor(0.5f, 0.5f, 0.5f, 1);
            rightChar.setColor(1, 1, 1, 1);
        }
    }
    
    private float autoTimer = 0;

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        stage.act(delta);
        stage.draw();
        
        if (isAutoPlay && !logWindow.isVisible() && !pauseMenu.isVisible() && dialogueBox.isFinished()) {
            autoTimer += delta;
            if (autoTimer > 2.0f) {
                autoTimer = 0;
                advanceDialogue();
            }
        } else {
            autoTimer = 0;
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        stage.dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
