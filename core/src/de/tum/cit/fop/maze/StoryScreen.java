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
    private final float CHAR_DELAY = 0.08f; // Slightly slow text speed
    private final float FADE_DURATION = 1.0f; // Duration of black screen between parts
    private final float READ_DELAY = 2.0f; // Time to wait after text finishes before fading

    public StoryScreen(MazeRunnerGame game, FileHandle nextMapFile) {
        this.game = game;
        this.nextMapFile = nextMapFile;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // Story Label
        storyLabel = new Label("", game.getSkin());
        storyLabel.setColor(Color.WHITE);
        storyLabel.setAlignment(Align.center);
        storyLabel.setWrap(true);
        rootTable.add(storyLabel).width(1200).padBottom(50).center().expand();
        
        rootTable.row();

        // Start Button (Hidden initially, appears at the very end)
        startButton = new TextButton("Start Mission", game.getSkin());
        startButton.setVisible(false);
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                skipStory();
            }
        });
        rootTable.add(startButton).width(300).height(60).padBottom(50);

        // Skip Button (Bottom Right)
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

    private void skipStory() {
        game.goToGame(nextMapFile);
    }

    @Override
    public void render(float delta) {
        // Clear screen with Black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Handle 'E' key for skipping
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            skipStory();
            return;
        }

        if (isFading) {
            // In transition (Black screen)
            storyLabel.setText(""); 
            fadeTimer += delta;
            
            if (fadeTimer >= FADE_DURATION) {
                // Transition done, start next part
                isFading = false;
                currentPartIndex++;
                if (currentPartIndex < storyParts.length) {
                    charIndex = 0;
                    partComplete = false;
                    timer = 0;
                } else {
                    // Should theoretically not reach here if logic is right, but handle safety
                    currentPartIndex = storyParts.length - 1;
                    partComplete = true;
                    startButton.setVisible(true);
                    storyLabel.setText(storyParts[currentPartIndex]);
                }
            }
        } else {
            // Typing Text or Waiting
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
                            // Last part finished
                            startButton.setVisible(true);
                        } else {
                            // Not last part, prepare to fade
                            fadeTimer = -READ_DELAY; // Wait READ_DELAY seconds before setting isFading
                        }
                    }
                    storyLabel.setText(currentText.substring(0, charIndex));
                }
            } else {
                // Part is complete (text fully displayed)
                if (currentPartIndex < storyParts.length - 1) {
                    // We are waiting to transition to black
                    fadeTimer += delta;
                    if (fadeTimer >= 0) {
                        isFading = true;
                        fadeTimer = 0;
                    }
                }
                // Else: Last part displayed, waiting for user interaction (Start or Skip)
            }
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
