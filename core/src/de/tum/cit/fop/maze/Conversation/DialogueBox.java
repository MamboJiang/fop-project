package de.tum.cit.fop.maze.Conversation;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import java.util.HashMap;
import java.util.Map;

/**
 * UI Component for displaying dialogue bubbles.
 * Supports different styles (Normal, Shout, Think) and tail directions.
 */
public class DialogueBox extends Group {
    
    /**
     * Enumeration for different dialogue bubble styles.
     */
    public enum DialogueType {
        NORMAL,
        SHOUT,
        THINK
    }
    
    /**
     * Enumeration for tail directions.
     */
    public enum TailDirection {
        DOWN,
        LEFT_DOWN,
        RIGHT_DOWN,
        NONE
    }
    
    private Stack stack;
    private Table backgroundTable;
    private Table contentTable;
    private Label textLabel;
    

    private Image tailImage;

    private Map<DialogueType, Map<TailDirection, TextureRegion>> tailRegions;
    

    private DialogueType currentType = DialogueType.NORMAL;
    private TailDirection currentTailDirection = TailDirection.DOWN;
    private float tailX = 0;
    

    private Map<DialogueType, TextureRegion[][]> styleRegions;
    
    private static final int TILE_SIZE = 16;
    
    /**
     * Constructor for DialogueBox.
     * @param skin UI Skin.
     * @param objectsTexture Texture atlas containing bubble assets.
     */
    public DialogueBox(Skin skin, Texture objectsTexture) {

        loadAssets(objectsTexture);
        
        stack = new Stack();
        

        backgroundTable = new Table();
        stack.add(backgroundTable);
        

        contentTable = new Table();
        textLabel = new Label("", skin);
        textLabel.setColor(Color.BLACK);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.center);
        

        contentTable.add(textLabel).pad(18).width(300f);
        stack.add(contentTable);
        
        this.addActor(stack);
        

        tailImage = new Image();
        this.addActor(tailImage);
        

        setType(DialogueType.NORMAL);
        setTailDirection(TailDirection.DOWN);
        setVisible(false);
    }
    
    /**
     * Loads assets and splits textures for 9-patch bubbles.
     * @param texture The source texture.
     */
    private void loadAssets(Texture texture) {
        TextureRegion[][] grid = TextureRegion.split(texture, 16, 16);
        styleRegions = new HashMap<>();
        tailRegions = new HashMap<>();
        
        try {

            styleRegions.put(DialogueType.NORMAL, get3x3Grid(grid, 14, 9));

            styleRegions.put(DialogueType.SHOUT, get3x3Grid(grid, 17, 9));

            styleRegions.put(DialogueType.THINK, get3x3Grid(grid, 14, 15));

            loadTailsForType(grid, DialogueType.NORMAL, 14);
            loadTailsForType(grid, DialogueType.SHOUT, 16);
            loadTailsForType(grid, DialogueType.THINK, 18);
            
        } catch (Exception e) {
            System.err.println("Error loading assets: " + e.getMessage());
        }
    }
    
    /**
     * Loads tail textures for a specific dialogue type.
     * @param grid Texture grid.
     * @param type Dialogue type.
     * @param startRow Start row in the grid.
     */
    private void loadTailsForType(TextureRegion[][] grid, DialogueType type, int startRow) {
        Map<TailDirection, TextureRegion> map = new HashMap<>();
        map.put(TailDirection.DOWN, mergeRegions(grid[startRow][12], grid[startRow+1][12]));
        map.put(TailDirection.LEFT_DOWN, mergeRegions(grid[startRow][13], grid[startRow+1][13]));
        map.put(TailDirection.RIGHT_DOWN, mergeRegions(grid[startRow][14], grid[startRow+1][14]));
        tailRegions.put(type, map);
    }
    
    /**
     * Extracts a 3x3 grid for 9-patch rendering.
     * @param fullGrid Full texture grid.
     * @param startRow Start row.
     * @param startCol Start column.
     * @return 3x3 TextureRegion array.
     */
    private TextureRegion[][] get3x3Grid(TextureRegion[][] fullGrid, int startRow, int startCol) {
        TextureRegion[][] block = new TextureRegion[3][3];
        for(int r=0; r<3; r++) {
            for(int c=0; c<3; c++) {
                block[r][c] = fullGrid[startRow+r][startCol+c];
            }
        }
        return block;
    }
    
    /**
     * Merges two texture regions vertically.
     * @param top Top region.
     * @param bottom Bottom region.
     * @return Merged region.
     */
    private TextureRegion mergeRegions(TextureRegion top, TextureRegion bottom) {
       return new TextureRegion(top.getTexture(), top.getRegionX(), top.getRegionY(), 16, 32);
    }
    
    /**
     * Sets the dialogue bubble type.
     * @param type Dialogue type.
     */
    private void setType(DialogueType type) {
        this.currentType = type;
        rebuildBackground();
        updateTail();
    }
    
    /**
     * Rebuilds the background table based on the current style.
     */
    private void rebuildBackground() {
        backgroundTable.clear();
        TextureRegion[][] reg = styleRegions.get(currentType);
        if (reg == null) return;
        

        backgroundTable.add(new Image(reg[0][0]));
        backgroundTable.add(new Image(new TiledDrawable(reg[0][1]))).fillX().expandX();
        backgroundTable.add(new Image(reg[0][2]));
        backgroundTable.row();
        

        backgroundTable.add(new Image(new TiledDrawable(reg[1][0]))).fillY().expandY();
        backgroundTable.add(new Image(new TiledDrawable(reg[1][1]))).fill().expand();
        backgroundTable.add(new Image(new TiledDrawable(reg[1][2]))).fillY().expandY();
        backgroundTable.row();
        

        backgroundTable.add(new Image(reg[2][0]));
        backgroundTable.add(new Image(new TiledDrawable(reg[2][1]))).fillX().expandX();
        backgroundTable.add(new Image(reg[2][2]));
    }
    

    /**
     * Shows text with default settings.
     * @param text Text to show.
     * @param type Dialogue type.
     */
    public void show(String text, DialogueType type) {
        show(text, type, 300f);
    }


    private boolean autoSize = true;

    /**
     * Enables or disables auto-sizing of the bubble.
     * @param autoSize True to auto-size, false otherwise.
     */
    public void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
    }


    private String targetText = "";
    private float charTimer = 0;
    private float charsPerSecond = 30;
    private boolean typingFinished = true;


    /**
     * Displays text in the dialogue box.
     * @param text The text to display.
     * @param type The type of bubble (NORMAL, SHOUT, THINK).
     * @param preferredWidth Preferred maximum width of the text area.
     */
    public void show(String text, DialogueType type, float preferredWidth) {
        setType(type);
        this.setVisible(true);

        this.targetText = text;
        this.typingFinished = false;
        this.charTimer = 0;
        textLabel.setText("");
        

        
        contentTable.clearChildren();
        
        if (autoSize) {

             contentTable.add(textLabel).pad(18).width(preferredWidth);
             stack.pack();
             this.setSize(stack.getWidth(), stack.getHeight());
        } else {
             textLabel.setAlignment(Align.topLeft);
             contentTable.add(textLabel).pad(18).fill().expand();
        }
        
        updateTail();
    }
    
    /**
     * Updates the typing effect.
     * @param delta Time delta.
     */
    @Override
    public void act(float delta) {
        super.act(delta);
        
        if (!typingFinished && targetText != null) {
            charTimer += delta;
            if (charTimer >= 1f / charsPerSecond) {
                charTimer = 0;
                int currentLen = textLabel.getText().length;
                if (currentLen < targetText.length()) {
                    textLabel.setText(targetText.substring(0, currentLen + 1));
                    

                    if (autoSize) {
                        stack.pack();
                        this.setSize(stack.getWidth(), stack.getHeight());
                        updateTail();
                    }
                } else {
                    typingFinished = true;
                }
            }
        }
    }
    
    /**
     * Checks if typing is finished.
     * @return True if finished, false otherwise.
     */
    public boolean isFinished() {
        return typingFinished;
    }
    
    /**
     * Skips the typing effect and shows full text immediately.
     */
    public void skipTypewriter() {
        if (!typingFinished) {
            textLabel.setText(targetText);
            typingFinished = true;
            if (autoSize) {
                stack.pack();
                this.setSize(stack.getWidth(), stack.getHeight());
                updateTail();
            }
        }
    }
    

    /**
     * Sets the tail X position.
     * @param relativeX X position relative to the bubble.
     */
    public void setTailPosition(float relativeX) {
        this.tailX = relativeX;
        updateTail();
    }
    
    /**
     * Sets the tail direction.
     * @param direction Tail direction.
     */
    public void setTailDirection(TailDirection direction) {
        this.currentTailDirection = direction;
        updateTail();
    }
    
    /**
     * Updates tail visibility and position.
     */
    private void updateTail() {
        if (currentTailDirection == TailDirection.NONE) {
            tailImage.setVisible(false);
            return;
        }
        
        tailImage.setVisible(true);
        

        Map<TailDirection, TextureRegion> typeTails = tailRegions.get(currentType);
        if (typeTails != null) {
            TextureRegion reg = typeTails.get(currentTailDirection);
            if (reg != null) {
                tailImage.setDrawable(new TextureRegionDrawable(reg));
                tailImage.setSize(reg.getRegionWidth(), reg.getRegionHeight());
                

                tailImage.setPosition(tailX, -16);
            }
        }
    }
    
    /**
     * Sets the size of the dialogue box.
     * @param width Width.
     * @param height Height.
     */
    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        if (stack != null) {
            stack.setSize(width, height);
        }
    }
    
    /**
     * Hides the dialogue box.
     */
    public void hide() {
        this.setVisible(false);
    }
}
