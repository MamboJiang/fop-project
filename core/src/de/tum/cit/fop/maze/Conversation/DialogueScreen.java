package de.tum.cit.fop.maze.Conversation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class DialogueScreen implements Screen {
    private final MazeRunnerGame game;
    private Stage stage;
    private DialogueBox dialogueBox;
    private int conversationIndex = 0;
    
    // Placeholder Character Portraits
    private Image leftChar;
    private Image rightChar;
    
    private final String[] texts = {
        "Hello! This is a normal dialogue bubble.",
        "And this is me replying from the right side!",
        "WHAT?!! A SHOUT BUBBLE?!!!",
        "Hmm... I am thinking about coordinates...",
        "This system uses 9-patch for perfect resizing.",
        "Click to go back to menu."
    };
    
    private final DialogueBox.DialogueType[] types = {
        DialogueBox.DialogueType.NORMAL,
        DialogueBox.DialogueType.NORMAL,
        DialogueBox.DialogueType.SHOUT,
        DialogueBox.DialogueType.THINK,
        DialogueBox.DialogueType.NORMAL,
        DialogueBox.DialogueType.NORMAL
    };
    
    // Who is speaking? true=Left, false=Right
    private final boolean[] speakers = {
        true,
        false,
        true,
        false,
        true,
        true
    };

    public DialogueScreen(MazeRunnerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1920, 1080));
        Gdx.input.setInputProcessor(stage);
        
        // Background
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        // Load Assets
        Texture charTexture = new Texture(Gdx.files.internal("character.png"));
        Texture objectsTexture = new Texture(Gdx.files.internal("objects.png"));
        
        // Create Character Portraits (Scaled up sprites)
        TextureRegion[][] tmp = TextureRegion.split(charTexture, 16, 32);
        
        // Left Char (Player)
        leftChar = new Image(tmp[0][0]);
        leftChar.setScale(4f); // Make them big
        leftChar.setPosition(100, 300);
        stage.addActor(leftChar);
        
        // Right Char (Maybe another sprite?)
        rightChar = new Image(tmp[0][0]); // Same sprite for now, maybe tinted
        rightChar.setColor(0.8f, 0.8f, 1f, 1f);
        rightChar.setScale(4f);
        rightChar.setPosition(Gdx.graphics.getWidth() - 200, 300);
        stage.addActor(rightChar);
        
        // Dialogue Box
        dialogueBox = new DialogueBox(game.getSkin(), objectsTexture);
        dialogueBox.setPosition(Gdx.graphics.getWidth() / 2 - 300, 50); // Centered bottom
        dialogueBox.setWidth(400); // Set fixed width for now, or let it expand
        
        stage.addActor(dialogueBox);
        
        // Initial text
        updateDialogue();
        
        // Click listener to advance
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                advanceDialogue();
            }
        });
        
        // Back Button
        TextButton backBtn = new TextButton("Back", game.getSkin());
        backBtn.setPosition(10, Gdx.graphics.getHeight() - 40);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.goToMenu();
            }
        });
        stage.addActor(backBtn);
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
        DialogueBox.DialogueType type = types[conversationIndex];
        boolean isLeft = speakers[conversationIndex];
        
        // Show with default width
        dialogueBox.show(text, type, 400f);
        
        // Configure tail
        if (isLeft) {
            dialogueBox.setTailDirection(DialogueBox.TailDirection.LEFT_DOWN);
            dialogueBox.setTailPosition(40f); // 40px from left
        } else {
            dialogueBox.setTailDirection(DialogueBox.TailDirection.RIGHT_DOWN);
            dialogueBox.setTailPosition(dialogueBox.getWidth() - 40f - 16f); // 40px from right (-16 for tail width)
        }
        
        // Dim non-speakers
        if (isLeft) {
            leftChar.setColor(1, 1, 1, 1);
            rightChar.setColor(0.5f, 0.5f, 0.5f, 1);
        } else {
            leftChar.setColor(0.5f, 0.5f, 0.5f, 1);
            rightChar.setColor(1, 1, 1, 1);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
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
