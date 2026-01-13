package de.tum.cit.fop.maze.Conversation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.ArrayList;
import java.util.List;

public class DialogueManager {

    private final Stage stage;
    private final Skin skin;
    
    private boolean isDialogueActive = false;
    private DialogueBox dialogueBox;
    private Image leftChar;
    private Image rightChar;
    
    // Data
    private DialogueData currentDialogueData;
    private int conversationIndex = 0;
    
    // For cleaning up textures if we load them dynamically
    private Texture leftTexture;
    private Texture rightTexture;

    public DialogueManager(Skin skin) {
        this.skin = skin;
        this.stage = new Stage(new FitViewport(1920, 1080));
        
        setupUI();
        setupInput();
    }

    private Table charTable;

    private void setupUI() {
        charTable = new Table();
        charTable.setFillParent(true);
        stage.addActor(charTable);

        // Placeholder images, will be updated when dialogue loads
        leftChar = new Image(); 
        rightChar = new Image();

        float rawHeight = 32f;
        float targetHeight = 1720f * 0.75f; 
        float targetHeight2 = 1080f * 0.75f;
        
        // We might need to adjust scaling dynamically if image sizes differ
        // For now, keeping the scaling logic but checking image size later might be better.
        // But let's stick to the structure from GameScreen first.

        charTable.bottom();
        // Position characters at sides. 
        leftCell = charTable.add(leftChar).height(targetHeight).padBottom(-100).expandX().left().padLeft(0);
        rightCell = charTable.add(rightChar).height(targetHeight2).padBottom(-100).expandX().right().padRight(-100);


        leftChar.setColor(1, 1, 1, 0); // Hide initially
        rightChar.setColor(1, 1, 1, 0);

        // UI Layout
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        stage.addActor(uiTable);
        uiTable.bottom();

        // We need a texture for objects (arrow tail etc), relying on existing specific loading or passing it in
        // For now let's assume we can load it here or pass it. 
        // GameScreen used "objects.png".
        Texture objectsTexture = new Texture(Gdx.files.internal("objects.png")); 
        
        dialogueBox = new DialogueBox(skin, objectsTexture);
        dialogueBox.setTailDirection(DialogueBox.TailDirection.NONE);
        dialogueBox.setAutoSize(false);
        dialogueBox.setSize(1800, 300);

        uiTable.add(dialogueBox).width(1800).height(500).padBottom(50);
    }
    
    private void setupInput() {
        // Click to advance
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isDialogueActive) {
                    if (!dialogueBox.isFinished()) {
                        dialogueBox.skipTypewriter();
                    } else {
                        advanceDialogue();
                    }
                }
            }
        });
    }

    // We need to keep references to the cells to update layout dynamically
    private com.badlogic.gdx.scenes.scene2d.ui.Cell<Image> leftCell;
    private com.badlogic.gdx.scenes.scene2d.ui.Cell<Image> rightCell;

    public void loadDialogue(String levelName) {
        Json json = new Json();
        FileHandle file = Gdx.files.internal("conversations/" + levelName + ".json");
        if (!file.exists()) {
            Gdx.app.error("DialogueManager", "Dialogue file not found: " + file.path());
            return;
        }
        
        currentDialogueData = json.fromJson(DialogueData.class, file);
        
        // Load Textures
        if (currentDialogueData.getLeftCharacterImage() != null) {
            updateLeftImage(currentDialogueData.getLeftCharacterImage());
        }
        if (currentDialogueData.getRightCharacterImage() != null) {
            updateRightImage(currentDialogueData.getRightCharacterImage());
        }
        
        updateLayout();
    }

    private void updateLayout() {
        if (currentDialogueData == null) return;

        float targetHeight = 1720f * 0.75f;
        float targetHeight2 = 1080f * 0.75f; // Base target height

        // Update Left Character Layout
        if (leftCell != null) {
             float scale = currentDialogueData.getLeftScale();
             // If user sets scale, we multiply width. 
             // Default logic was: height=targetHeight.
             // We can let user override width/height via scale? 
             // Or just scale the width relative to the image aspect ratio?
             // GameScreen had hardcoded behavior. 
             // Let's assume Scale=1.0 means "Standard Size defined in GameSreen"
             // Standard Left: height=targetHeight (1290), width=16 * (1290/32) = 645.
             
             // Let's rely on drawable size if possible, or force size.
             // If we want to allow user to tweak position:
             leftCell.padLeft(currentDialogueData.getLeftOffsetX());
             leftCell.padBottom(-100 + currentDialogueData.getLeftOffsetY()); // Base pad -100
             
             // Apply scale to dimensions? 
             // Let's say scale acts as a multiplier on top of the "Standard" size.
             // But simpler: just use scale to multiply the width/height.
             
             // Re-calculating base size from GameScreen logic for consistency:
             // float rawHeight = 32f; float scale = targetHeight / rawHeight; -> Width = 16 * scale;
             // That logic depended on the input texture being 16x32.
             // If the new texture is different, we might distort it.
             // Better: preserve aspect ratio. Height = targetHeight * scale. 
             
             if (leftChar.getDrawable() != null) {
                 float aspect = leftChar.getDrawable().getMinWidth() / leftChar.getDrawable().getMinHeight();
                 float h = targetHeight * scale;
                 float w = h * aspect;
                 leftCell.size(w, h);
             }
        }

        // Update Right Character Layout
        if (rightCell != null) {
             float scale = currentDialogueData.getRightScale();
             
             // Base logic: height=targetHeight2 (810).
             // Mobs were 16x16.
             
             if (rightChar.getDrawable() != null) {
                 float aspect = rightChar.getDrawable().getMinWidth() / rightChar.getDrawable().getMinHeight();
                 float h = targetHeight2 * scale;
                 float w = h * aspect;
                 rightCell.size(w, h);
             }
             
             rightCell.padRight(-100 + currentDialogueData.getRightOffsetX());
             rightCell.padBottom(-100 + currentDialogueData.getRightOffsetY());
        }
        
        charTable.invalidateHierarchy();
    }
    
    private void updateLeftImage(String path) {
        if (leftTexture != null) leftTexture.dispose();
        try {
            leftTexture = new Texture(Gdx.files.internal(path));
            leftChar.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(leftTexture)));
        } catch (Exception e) {
            Gdx.app.error("DialogueManager", "Failed to load left image: " + path, e);
        }
    }

    private void updateRightImage(String path) {
        if (rightTexture != null) rightTexture.dispose();
        try {
            if (path.contains("mobs.png")) {
                 Texture mobsTexture = new Texture(Gdx.files.internal(path));
                 TextureRegion[][] mobsTmp = TextureRegion.split(mobsTexture, 16, 16);
                 rightChar.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(mobsTmp[0][0]));
                 rightTexture = mobsTexture; 
            } else {
                rightTexture = new Texture(Gdx.files.internal(path));
                rightChar.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new TextureRegion(rightTexture)));
            }
        } catch (Exception e) {
            Gdx.app.error("DialogueManager", "Failed to load right image: " + path, e);
        }
    }

    public void startDialogue() {
        if (isDialogueActive) return;
        if (currentDialogueData == null || currentDialogueData.getLines().isEmpty()) return;
        
        isDialogueActive = true;
        conversationIndex = 0;

        leftChar.setColor(1, 1, 1, 1);
        rightChar.setColor(1, 1, 1, 1);

        updateDialogue();
    }

    public void endDialogue() {
        isDialogueActive = false;
        leftChar.setColor(1, 1, 1, 0);
        rightChar.setColor(1, 1, 1, 0);
        // Maybe callback to game screen?
    }

    public void advanceDialogue() {
        conversationIndex++;
        if (currentDialogueData != null && conversationIndex >= currentDialogueData.getLines().size()) {
            endDialogue();
            return;
        }
        updateDialogue();
    }

    private void updateDialogue() {
        if (currentDialogueData == null) return;
        
        DialogueData.DialogueLine line = currentDialogueData.getLines().get(conversationIndex);
        
        dialogueBox.show(line.getText(), DialogueBox.DialogueType.NORMAL, 1700f);

        // Dim the non-speaking character
        if (line.isLeft()) {
            leftChar.setColor(1, 1, 1, 1);
            rightChar.setColor(0.5f, 0.5f, 0.5f, 1);
        } else {
            leftChar.setColor(0.5f, 0.5f, 0.5f, 1);
            rightChar.setColor(1, 1, 1, 1);
        }
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isActive() {
        return isDialogueActive;
    }
    
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }
    
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    
    public void dispose() {
        stage.dispose();
        if (leftTexture != null) leftTexture.dispose();
        if (rightTexture != null) rightTexture.dispose();
    }
}
