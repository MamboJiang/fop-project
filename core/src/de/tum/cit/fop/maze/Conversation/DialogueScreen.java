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

/**
 * Screen for demonstrating dialogue mechanics (not the story mode).
 */
public class DialogueScreen implements Screen {
    private final MazeRunnerGame game;
    private Stage stage;
    private DialogueBox dialogueBox;
    private int conversationIndex = 0;
    

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
    

    private final boolean[] speakers = {
        true,
        false,
        true,
        false,
        true,
        true
    };

    /**
     * Constructor for DialogueScreen.
     * @param game Main game instance.
     */
    public DialogueScreen(MazeRunnerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1920, 1080));
        Gdx.input.setInputProcessor(stage);
        

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        

        Texture charTexture = new Texture(Gdx.files.internal("character.png"));
        Texture objectsTexture = new Texture(Gdx.files.internal("objects.png"));
        

        TextureRegion[][] tmp = TextureRegion.split(charTexture, 16, 32);
        

        leftChar = new Image(tmp[0][0]);
        leftChar.setScale(4f);
        leftChar.setPosition(100, 300);
        stage.addActor(leftChar);
        

        rightChar = new Image(tmp[0][0]);
        rightChar.setColor(0.8f, 0.8f, 1f, 1f);
        rightChar.setScale(4f);
        rightChar.setPosition(Gdx.graphics.getWidth() - 200, 300);
        stage.addActor(rightChar);
        

        dialogueBox = new DialogueBox(game.getSkin(), objectsTexture);
        dialogueBox.setPosition(Gdx.graphics.getWidth() / 2 - 300, 50);
        dialogueBox.setWidth(400);
        
        stage.addActor(dialogueBox);
        

        updateDialogue();
        

        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                advanceDialogue();
            }
        });
        

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
    
    /**
     * Advances to the next dialogue/demo step.
     */
    private void advanceDialogue() {
        conversationIndex++;
        if (conversationIndex >= texts.length) {
            game.goToMenu();
            return;
        }
        updateDialogue();
    }
    
    /**
     * Updates the dialogue box content for the current step.
     */
    private void updateDialogue() {
        String text = texts[conversationIndex];
        DialogueBox.DialogueType type = types[conversationIndex];
        boolean isLeft = speakers[conversationIndex];
        

        dialogueBox.show(text, type, 400f);
        

        if (isLeft) {
            dialogueBox.setTailDirection(DialogueBox.TailDirection.LEFT_DOWN);
            dialogueBox.setTailPosition(40f);
        } else {
            dialogueBox.setTailDirection(DialogueBox.TailDirection.RIGHT_DOWN);
            dialogueBox.setTailPosition(dialogueBox.getWidth() - 40f - 16f);
        }
        

        if (isLeft) {
            leftChar.setColor(1, 1, 1, 1);
            rightChar.setColor(0.5f, 0.5f, 0.5f, 1);
        } else {
            leftChar.setColor(0.5f, 0.5f, 0.5f, 1);
            rightChar.setColor(1, 1, 1, 1);
        }
    }

    /**
     * Renders the screen.
     * @param delta Time delta.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    /**
     * Resizes the viewport.
     * @param width New width.
     * @param height New height.
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Called when the application is paused.
     */
    @Override
    public void pause() {}

    /**
     * Called when the application is resumed.
     */
    @Override
    public void resume() {}

    /**
     * Called when this screen is no longer the current screen.
     */
    @Override
    public void hide() {
        stage.dispose();
    }

    /**
     * Disposes of assets.
     */
    @Override
    public void dispose() {
        stage.dispose();
    }
}
