package de.tum.cit.fop.maze.Conversation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import de.tum.cit.fop.maze.GameObj.PlayerState;

import java.util.ArrayList;
import java.util.List;

public class DialogueManager {

    private final Stage stage;
    private final Skin skin;

    private boolean isDialogueActive = false;


    private Table uiTable;
    private Table textTable;
    private Image gradientBg;
    private Label dialogueText;
    private Label speakerName;


    private Image portraitImage;
    private Image backImage;
    private Texture portraitTexture;
    private Texture fadingTexture;
    private String currentPortraitPath = "";

    private Image arrowImage;
    private Texture arrowTexture;


    private Image leftChar;
    private Image rightChar;


    private DialogueData currentDialogueData;
    private int conversationIndex = 0;


    private Texture leftTexture;
    private Texture rightTexture;
    private Texture gradientTexture;


    private String currentEffect = "";
    private float effectTimer = 0f;
    private float shakeAmount = 0f;
    private float wiggleAmount = 0f;


    private String targetText = "";
    private float charTimer = 0;
    private float charsPerSecond = 30;
    private boolean typingFinished = true;

    private PlayerState playerState;

    public DialogueManager(Skin skin, PlayerState playerState) {
        this.skin = skin;
        this.stage = new Stage(new ExtendViewport(1920, 1080));
        this.playerState = playerState;

        setupUI();
        setupInput();
    }

    private Table charTable;
    private Image backgroundScrim;

    /**
     * Sets up all the UI elements for the dialogue system, including character
     * display,
     * background scrim, text box, speaker name, dialogue text, portrait, and the
     * advance arrow.
     */
    private void setupUI() {

        Image scrim = new Image(skin.newDrawable("white", 0, 0, 0, 0.5f));


        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0.4f);
        p.fill();
        Texture scrimTex = new Texture(p);
        p.dispose();
        backgroundScrim = new Image(scrimTex);
        backgroundScrim.setFillParent(true);
        stage.addActor(backgroundScrim);

        charTable = new Table();
        charTable.setFillParent(true);
        stage.addActor(charTable);

        leftChar = new Image();
        leftChar.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        rightChar = new Image();
        rightChar.setScaling(com.badlogic.gdx.utils.Scaling.fit);

        float targetHeight = 1080f * 0.75f;
        charTable.bottom();
        leftCell = charTable.add(leftChar).height(targetHeight).padBottom(-50).expandX().left().padLeft(100);
        rightCell = charTable.add(rightChar).height(targetHeight).padBottom(-50).expandX().right().padRight(100);

        leftChar.setColor(1, 1, 1, 1);
        rightChar.setColor(1, 1, 1, 1);

        uiTable = new Table();
        uiTable.setFillParent(true);
        stage.addActor(uiTable);
        uiTable.bottom();

        Pixmap pixmap = new Pixmap(1, 500, Pixmap.Format.RGBA8888);
        for (int y = 0; y < 500; y++) {

            float alpha = 1.0f - ((float) y / 500f);
            pixmap.setColor(0f, 0f, 0.4f, alpha * 0.9f);
            pixmap.drawPixel(0, 499 - y);
        }
        gradientTexture = new Texture(pixmap);
        pixmap.dispose();

        gradientBg = new Image(gradientTexture);


        Table bottomContainer = new Table();
        bottomContainer.setBackground(new TextureRegionDrawable(new TextureRegion(gradientTexture)));

        uiTable.add(bottomContainer).growX().height(450).bottom();


        bottomContainer.left().bottom();

        portraitImage = new Image();
        backImage = new Image();


        com.badlogic.gdx.scenes.scene2d.Group group = new com.badlogic.gdx.scenes.scene2d.Group();
        group.addActor(backImage);
        group.addActor(portraitImage);


        com.badlogic.gdx.scenes.scene2d.ui.Container<com.badlogic.gdx.scenes.scene2d.Group> container = new com.badlogic.gdx.scenes.scene2d.ui.Container<>(
                group);
        container.setClip(true);
        container.align(Align.bottomLeft);
        container.fill();

        portraitCell = bottomContainer.add(container).size(300, 400).bottom().left().pad(20);

        Table textContainer = new Table();
        bottomContainer.add(textContainer).grow().top().padTop(155).padLeft(30).padRight(30).padBottom(30);


        Label.LabelStyle nameStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.WHITE);
        Label.LabelStyle textStyle = new Label.LabelStyle(skin.getFont("hoefler"), Color.WHITE);

        speakerName = new Label("", nameStyle);

        speakerName.setColor(0.5f, 0.8f, 1f, 1f);
        speakerName.setFontScale(1.5f);

        dialogueText = new Label("", textStyle);
        dialogueText.setWrap(true);
        dialogueText.setColor(Color.WHITE);
        dialogueText.setFontScale(1.2f);
        dialogueText.setAlignment(Align.topLeft);


        createArrowTexture();
        arrowImage = new Image(arrowTexture);
        arrowImage.setOrigin(Align.center);
        startBobbing();


        textContainer.add(speakerName).expandX().left().padBottom(10).row();
        textContainer.add(dialogueText).grow().top().left();

        bottomContainer.add(arrowImage).bottom().right().padRight(40).padBottom(20);
    }

    private void setupInput() {
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleInput();

                if (arrowImage != null) {
                    playArrowFeedback();
                }
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.SPACE) {
                    handleInput();

                    if (arrowImage != null) {
                        playArrowFeedback();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void startBobbing() {
        if (arrowImage == null)
            return;
        arrowImage.clearActions();
        arrowImage.addAction(Actions.forever(
                Actions.sequence(
                        Actions.moveBy(0, -5, 0.5f, Interpolation.sine),
                        Actions.moveBy(0, 5, 0.5f, Interpolation.sine))));
    }

    private void playArrowFeedback() {
        if (arrowImage == null)
            return;
        arrowImage.clearActions();
        arrowImage.addAction(Actions.sequence(
                Actions.scaleTo(1.3f, 1.3f, 0.05f),
                Actions.scaleTo(1f, 1f, 0.05f),
                Actions.run(this::startBobbing)));
    }

    private void handleInput() {
        if (isDialogueActive) {
            if (!typingFinished) {

                dialogueText.setText(targetText);
                typingFinished = true;
            } else {
                advanceDialogue();
            }
        }
    }

    private void createArrowTexture() {

        Pixmap p = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        p.setColor(1, 1, 1, 1);
        for (int i = 0; i < 4; i++) {
            p.drawLine(4 + i, 4, 16, 28 - i);
            p.drawLine(28 - i, 4, 16, 28 - i);
        }
        arrowTexture = new Texture(p);
        p.dispose();
    }


    private com.badlogic.gdx.scenes.scene2d.ui.Cell<Image> leftCell;
    private com.badlogic.gdx.scenes.scene2d.ui.Cell<Image> rightCell;
    private com.badlogic.gdx.scenes.scene2d.ui.Cell portraitCell;

    private void updateLayout() {
        if (currentDialogueData == null)
            return;
        if (conversationIndex >= currentDialogueData.getLines().size())
            return;

        float baseHeight = 1080f * 0.75f;


        if (leftCell != null && leftChar.getDrawable() != null) {
            float aspect = leftChar.getDrawable().getMinWidth() / leftChar.getDrawable().getMinHeight();
            float scale = currentDialogueData.getLeftScale();
            float h = baseHeight * scale;
            float w = h * aspect;

            leftCell.size(w, h);
            leftCell.padLeft(currentDialogueData.getLeftOffsetX());
            leftCell.padBottom(-50 + currentDialogueData.getLeftOffsetY());
        }


        if (rightCell != null && rightChar.getDrawable() != null) {
            float aspect = rightChar.getDrawable().getMinWidth() / rightChar.getDrawable().getMinHeight();
            float scale = currentDialogueData.getRightScale();
            float h = baseHeight * scale;
            float w = h * aspect;

            rightCell.size(w, h);
            rightCell.padRight(currentDialogueData.getRightOffsetX());
            rightCell.padBottom(-50 + currentDialogueData.getRightOffsetY());
        }


        if (portraitCell != null) {
            DialogueData.DialogueLine line = currentDialogueData.getLines().get(conversationIndex);
            boolean isLeft = line.isLeft();

            float scale = isLeft ? currentDialogueData.getLeftPortraitScale()
                    : currentDialogueData.getRightPortraitScale();
            float offX = isLeft ? currentDialogueData.getLeftPortraitOffsetX()
                    : currentDialogueData.getRightPortraitOffsetX();
            float offY = isLeft ? currentDialogueData.getLeftPortraitOffsetY()
                    : currentDialogueData.getRightPortraitOffsetY();

            float baseSize = 350f;
            if (portraitImage.getDrawable() != null) {
                float aspect = portraitImage.getDrawable().getMinWidth() / portraitImage.getDrawable().getMinHeight();
                float h = baseSize * scale;
                float w = h * aspect;
                portraitImage.setSize(w, h);
            }

            portraitImage.setPosition(offX, offY);


            portraitCell.size(300, 400);
        }

        charTable.invalidateHierarchy();
        if (uiTable != null)
            uiTable.invalidateHierarchy();
    }

    private void updateLeftImage(String path) {
        if (leftTexture != null)
            leftTexture.dispose();
        try {
            leftTexture = new Texture(Gdx.files.internal(path));
            leftChar.setDrawable(
                    new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(leftTexture)));
        } catch (Exception e) {
            Gdx.app.error("DialogueManager", "Failed to load left image: " + path, e);
        }
    }

    private void updateRightImage(String path) {
        if (rightTexture != null)
            rightTexture.dispose();
        try {
            if (path.contains("mobs.png")) {
                Texture mobsTexture = new Texture(Gdx.files.internal(path));
                mobsTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                TextureRegion[][] mobsTmp = TextureRegion.split(mobsTexture, 16, 16);
                rightChar.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(mobsTmp[0][0]));
                rightTexture = mobsTexture;
            } else {
                rightTexture = new Texture(Gdx.files.internal(path));
                rightChar.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                        new TextureRegion(rightTexture)));
            }
        } catch (Exception e) {
            Gdx.app.error("DialogueManager", "Failed to load right image: " + path, e);
        }
    }

    /**
     * Updates the portrait image with a cross-fade effect.
     * 
     * @param path File path to the new portrait.
     */
    private void updatePortrait(String path) {
        if (path == null) {
            if (currentPortraitPath == null)
                return;
            currentPortraitPath = null;


            if (fadingTexture != null) {
                fadingTexture.dispose();
                fadingTexture = null;
            }
            fadingTexture = portraitTexture;
            portraitTexture = null;

            portraitImage.clearActions();
            portraitImage.addAction(Actions.sequence(
                    Actions.fadeOut(0.5f),
                    Actions.run(() -> {
                        portraitImage.setVisible(false);
                        if (fadingTexture != null) {
                            fadingTexture.dispose();
                            fadingTexture = null;
                        }
                    })));
            return;
        }

        if (path.equals(currentPortraitPath))
            return;
        currentPortraitPath = path;

        if (fadingTexture != null) {
            fadingTexture.dispose();
            fadingTexture = null;
        }


        fadingTexture = portraitTexture;


        try {
            TextureRegion region;

            if (path.contains("mobs.png")) {

                portraitTexture = new Texture(Gdx.files.internal(path));
                portraitTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                TextureRegion[][] tmp = TextureRegion.split(portraitTexture, 16, 16);
                region = tmp[0][0];
            } else {
                portraitTexture = new Texture(Gdx.files.internal(path));
                region = new TextureRegion(portraitTexture);
            }

            Image oldImage = portraitImage;
            Image newImage = backImage;

            portraitImage = newImage;
            backImage = oldImage;


            backImage.clearActions();
            backImage.addAction(Actions.sequence(
                    Actions.fadeOut(0.5f),
                    Actions.run(() -> {
                        backImage.setVisible(false);

                        if (fadingTexture != null) {
                            fadingTexture.dispose();
                            fadingTexture = null;
                        }
                    })));


            portraitImage.setDrawable(new TextureRegionDrawable(region));
            portraitImage.clearActions();
            portraitImage.setColor(1, 1, 1, 0);
            portraitImage.setVisible(true);
            portraitImage.toFront();

            updateLayout();

            portraitImage.addAction(Actions.fadeIn(0.5f));

        } catch (Exception e) {
            Gdx.app.error("DialogueManager", "Failed to load portrait: " + path, e);
        }
    }

    /**
     * Starts the dialogue sequence.
     */
    public void startDialogue() {
        if (isDialogueActive)
            return;
        if (currentDialogueData == null || currentDialogueData.getLines().isEmpty())
            return;

        isDialogueActive = true;
        conversationIndex = 0;

        leftChar.setColor(1, 1, 1, 1);
        rightChar.setColor(1, 1, 1, 1);

        if (uiTable != null)
            uiTable.setVisible(true);

        updateDialogue();
    }

    public void endDialogue() {
        isDialogueActive = false;
        leftChar.setColor(1, 1, 1, 0);
        rightChar.setColor(1, 1, 1, 0);

        if (uiTable != null)
            uiTable.setVisible(false);
    }

    /**
     * Advances to the next line of dialogue.
     * Ends dialogue if no lines remain.
     */
    public void advanceDialogue() {
        conversationIndex++;
        if (currentDialogueData != null && conversationIndex >= currentDialogueData.getLines().size()) {
            endDialogue();
            return;
        }
        updateDialogue();
    }

    public void loadDialogue(String levelName) {
        Json json = new Json();
        FileHandle file = Gdx.files.internal("conversations/" + levelName + ".json");
        if (!file.exists()) {
            Gdx.app.error("DialogueManager", "Dialogue file not found: " + file.path());
            return;
        }

        currentDialogueData = json.fromJson(DialogueData.class, file);


        if (currentDialogueData.getLeftCharacterImage() != null) {
            updateLeftImage(currentDialogueData.getLeftCharacterImage());
        }
        if (currentDialogueData.getRightCharacterImage() != null) {
            updateRightImage(currentDialogueData.getRightCharacterImage());
        }
    }

    public void setBackgroundScrimVisible(boolean visible) {
        if (backgroundScrim != null) {
            backgroundScrim.setVisible(visible);
        }
    }

    private void updateDialogue() {
        if (currentDialogueData == null)
            return;

        DialogueData.DialogueLine line = currentDialogueData.getLines().get(conversationIndex);


        targetText = line.getText();
        dialogueText.setText("");
        typingFinished = false;
        charTimer = 0;

        if (line.getSpeaker().equals("Player")) {
            speakerName.setText(playerState.getUsername());
        } else {
            speakerName.setText(line.getSpeaker());
        }

        if (line.getPortrait() != null) {
            updatePortrait(line.getPortrait());
        }


        currentEffect = line.getEffect() != null ? line.getEffect() : "";
        effectTimer = 0;
        shakeAmount = 0;
        wiggleAmount = 0;


        if (line.isHideLeft()) {
            leftChar.setVisible(false);
        } else {
            leftChar.setVisible(true);
            leftChar.setColor(1, 1, 1, 1);
        }

        if (line.isHideRight()) {
            rightChar.setVisible(false);
        } else {
            rightChar.setVisible(true);
            rightChar.setColor(1, 1, 1, 1);
        }
    }

    public void render(float delta) {
        stage.act(delta);

        if (!typingFinished && targetText != null) {
            charTimer += delta;

            if (charTimer >= 0.03f) {
                charTimer = 0;
                int currentLen = dialogueText.getText().length;
                if (currentLen < targetText.length()) {
                    dialogueText.setText(targetText.substring(0, currentLen + 1));
                } else {
                    typingFinished = true;
                }
            }
        }


        if (currentEffect != null && !currentEffect.isEmpty()) {
            effectTimer += delta;
            float offsetX = 0;
            float offsetY = 0;

            if (currentEffect.equals("shake")) {

                if (effectTimer < 0.5f) {
                    offsetY = (float) Math.sin(effectTimer * 50) * 10f * (1f - effectTimer / 0.5f);
                }
            } else if (currentEffect.equals("wiggle")) {

                if (effectTimer < 0.5f) {
                    offsetX = (float) Math.sin(effectTimer * 50) * 10f * (1f - effectTimer / 0.5f);
                }
            }


            float originalPortraitX = portraitImage.getX();
            float originalPortraitY = portraitImage.getY();

            float originalLeftX = leftChar.getX();
            float originalLeftY = leftChar.getY();

            float originalRightX = rightChar.getX();
            float originalRightY = rightChar.getY();


            portraitImage.setPosition(originalPortraitX + offsetX, originalPortraitY + offsetY);

            DialogueData.DialogueLine line = currentDialogueData.getLines().get(conversationIndex);
            if (line.isLeft()) {
                leftChar.setPosition(originalLeftX + offsetX, originalLeftY + offsetY);
            } else {
                rightChar.setPosition(originalRightX + offsetX, originalRightY + offsetY);
            }

            stage.draw();

            portraitImage.setPosition(originalPortraitX, originalPortraitY);
            leftChar.setPosition(originalLeftX, originalLeftY);
            rightChar.setPosition(originalRightX, originalRightY);
            return;
        }

        stage.draw();
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isActive() {
        return isDialogueActive;
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        if (leftTexture != null)
            leftTexture.dispose();
        if (rightTexture != null)
            rightTexture.dispose();
        if (gradientTexture != null)
            gradientTexture.dispose();
        if (portraitTexture != null)
            portraitTexture.dispose();
        if (arrowTexture != null)
            arrowTexture.dispose();
    }
}
