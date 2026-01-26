package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * The StoryScreen displays the narrative context before a level or the game starts.
 * It features scrolling text and a skip option.
 */
public class StoryScreen implements Screen {

    private final MazeRunnerGame game;
    private final FileHandle nextMapFile;
    private final Stage stage;
    private final Label storyLabel;
    private final TextButton startButton;

    private final String[] storyParts = {
        "Year 2077: \"The Silent Day\"\n\n" +
        "An unknown biological weapon spiraled out of control, consuming surface civilization in a mere 24 hours.\n\n" +
        "Victims lose all human characteristics, eventually collapsing into heaps of static organic matter. " +
        "These masses blanket the streets and buildings, primed to secrete powerful acid that corrodes anything that ventures too close.\n\n" +
        "Even more terrifyingly, a small number of the infected remain mobile. " +
        "They prowl through the darkness, preying upon anything that still draws breath.\n\n" +
        "The survivors of human civilization have retreated to the underground \"Meridian Facility.\"",

        "Before the disaster reached its peak, scientists at the Meridian Foundation labs developed an antidote—a cure that, if released into the atmosphere, could purify the entire planet.\n\n" +
        "But no survivor is capable of crossing that living hell.\n\n" +
        "And so, \"Project Courier\" was initiated."
    };

    private int currentPartIndex = 0;
    private float timer = 0;
    private int charIndex = 0;
    private boolean partComplete = false;
    private boolean isFading = false;
    private float fadeTimer = 0;
    private final float CHAR_DELAY = 0.08f;
    private final float FADE_DURATION = 1.0f;
    private final float READ_DELAY = 2.0f;

    /**
     * Constructor for StoryScreen.
     * @param game The main game instance.
     * @param nextMapFile The map file to load after the story.
     */
    public StoryScreen(MazeRunnerGame game, FileHandle nextMapFile) {
        this.game = game;
        this.nextMapFile = nextMapFile;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        storyLabel = new Label("", game.getSkin());
        storyLabel.setColor(Color.WHITE);
        storyLabel.setAlignment(Align.center);
        storyLabel.setWrap(true);
        rootTable.add(storyLabel).width(1200).padBottom(50).center().expand();
        
        rootTable.row();


        startButton = new TextButton("Start Mission", game.getSkin());
        startButton.setVisible(false);
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                skipStory();
            }
        });
        rootTable.add(startButton).width(300).height(60).padBottom(50);


        Table bottomTable = new Table();
        bottomTable.setFillParent(true);
        bottomTable.bottom().right();
        stage.addActor(bottomTable);

        TextButton skipButton = new TextButton("Skip (E)", game.getSkin());
        skipButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                skipStory();
            }
        });
        bottomTable.add(skipButton).pad(20);
    }

    /**
     * Skips the story and loads the next game level immediately.
     */
    private void skipStory() {
        game.goToGame(nextMapFile);
    }

    /**
     * Renders the story text with a typing effect.
     * @param delta Time since last frame.
     */
    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            skipStory();
            return;
        }

        if (isFading) {

            storyLabel.setText(""); 
            fadeTimer += delta;
            
            if (fadeTimer >= FADE_DURATION) {

                isFading = false;
                currentPartIndex++;
                if (currentPartIndex < storyParts.length) {
                    charIndex = 0;
                    partComplete = false;
                    timer = 0;
                } else {

                    currentPartIndex = storyParts.length - 1;
                    partComplete = true;
                    startButton.setVisible(true);
                    storyLabel.setText(storyParts[currentPartIndex]);
                }
            }
        } else {

            if (!partComplete) {
                timer += delta;
                if (timer >= CHAR_DELAY) {
                    timer -= CHAR_DELAY;
                    charIndex++;
                    String currentText = storyParts[currentPartIndex];
                    
                    if (charIndex > currentText.length()) {
                        charIndex = currentText.length();
                        partComplete = true;
                        
                        if (currentPartIndex == storyParts.length - 1) {

                            startButton.setVisible(true);
                        } else {

                            fadeTimer = -READ_DELAY;
                        }
                    }
                    storyLabel.setText(currentText.substring(0, charIndex));
                }
            } else {

                if (currentPartIndex < storyParts.length - 1) {

                    fadeTimer += delta;
                    if (fadeTimer >= 0) {
                        isFading = true;
                        fadeTimer = 0;
                    }
                }

            }
        }

        stage.act(delta);
        stage.draw();
    }

    /**
     * Resizes the stage viewport.
     * @param width New width.
     * @param height New height.
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Called when the screen shows. Sets input processor.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Called when the screen hides. Resets input processor.
     */
    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    /**
     * Disposes of stage resources.
     */
    @Override
    public void dispose() {
        stage.dispose();
    }
}
