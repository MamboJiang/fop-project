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

public class DialogueBox extends Group {
    
    public enum DialogueType {
        NORMAL,
        SHOUT,
        THINK
    }
    
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
    
    // Tail
    private Image tailImage;
    // Map<Type, Map<Direction, Region>>
    private Map<DialogueType, Map<TailDirection, TextureRegion>> tailRegions;
    
    // Config
    private DialogueType currentType = DialogueType.NORMAL;
    private TailDirection currentTailDirection = TailDirection.DOWN;
    private float tailX = 0; // Relative X position of tail
    
    // Assets Cache
    private Map<DialogueType, TextureRegion[][]> styleRegions;
    
    private static final int TILE_SIZE = 16;
    
    public DialogueBox(Skin skin, Texture objectsTexture) {
        // Load Assets
        loadAssets(objectsTexture);
        
        stack = new Stack();
        
        // 1. Background Layer
        backgroundTable = new Table();
        stack.add(backgroundTable);
        
        // 2. Content Layer
        contentTable = new Table();
        textLabel = new Label("", skin);
        textLabel.setColor(Color.BLACK);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.center);
        
        // Padding is crucial to not overlap borders. Border is 16px.
        contentTable.add(textLabel).pad(18).width(300f); // Default width
        stack.add(contentTable);
        
        this.addActor(stack);
        
        // Tail (Added to group, not stack, so it can hang outside)
        tailImage = new Image();
        this.addActor(tailImage);
        
        // Apply default style
        setType(DialogueType.NORMAL);
        setTailDirection(TailDirection.DOWN);
        setVisible(false);
    }
    
    private void loadAssets(Texture texture) {
        TextureRegion[][] grid = TextureRegion.split(texture, 16, 16);
        styleRegions = new HashMap<>();
        tailRegions = new HashMap<>();
        
        try {
            // --- BACKGROUNDS ---
            // User said: "x 10th" -> Index 9. "y 15th" -> Index 14.
            // Normal Bubble: 3x3 starting at [14][9]
            styleRegions.put(DialogueType.NORMAL, get3x3Grid(grid, 14, 9));
            
            // Shout Bubble: 10,18 -> Index 17,9 ?
            // User said "10,18 is top right". Wait. "10,18 is Right-Top".
            // If 10 is X, 18 is Y.
            // If 10 is 'Right-Top' X? No, usually start.
            // Let's assume user defined the Start (Top-Left) for Normal as 10,15.
            // For Shout, they said "10,18 is Right-Top".
            // That's confusing. "10,18 is top right".
            // If X=10 is Right side, then Left side is 8?
            // OR maybe user meant "10,18 is Top Left" just like Normal?
            // Let's look at pattern. 15,16,17 rows are normal. Next 3 rows 18,19,20 Shout?
            // If so, 10,18 Top Left makes sense.
            // Let's use 10,18 (Index 9, 17) as Top Left for Shout for now.
            styleRegions.put(DialogueType.SHOUT, get3x3Grid(grid, 17, 9));
            
            // Think Bubble: "16,15 is Top Right" ??
            // If Normal is 10,11,12.
            // Maybe Think is 13,14,15? Or 14,15,16?
            // "16,15 is Top Right". So X=16. Left is 14?
            // Let's try grabbing Grid at [14][13] (Index 14, 13)?
            // Wait, previous attempt assumed just to right of normal.
            // Normal X: 9, 10, 11 (Indices).
            // Gap?
            // Let's try X=13,14,15 (Indices). Top Right is 15 -> User said 16th? Matches.
            // So Top Left Index = 13. Row Index = 14.
            // User Edit in Step 495: `styleRegions.put(DialogueType.THINK, get3x3Grid(grid, 14, 15));`
            // So [14][15] is Top-Left of Think Bubble. Consistent with 10(Normal)+3(3x3)+gap?
            // 9 (Normal start) + 3 = 12. 13,14 are Tails. 15 is Think Start. Matches perfectly!
            styleRegions.put(DialogueType.THINK, get3x3Grid(grid, 14, 15));
            
            // --- TAILS ---
            // Columns: Down=12, Left=13, Right=14 (0-based) based on User "13,15"(14,12), "14,15"(14,13)
            loadTailsForType(grid, DialogueType.NORMAL, 14);
            loadTailsForType(grid, DialogueType.SHOUT, 16); // "13,17" -> 16,12
            loadTailsForType(grid, DialogueType.THINK, 18); // "13,19" -> 18,12
            
        } catch (Exception e) {
            System.err.println("Error loading assets: " + e.getMessage());
        }
    }
    
    private void loadTailsForType(TextureRegion[][] grid, DialogueType type, int startRow) {
        Map<TailDirection, TextureRegion> map = new HashMap<>();
        // Cols: 12=Down, 13=LeftDown, 14=RightDown
        map.put(TailDirection.DOWN, mergeRegions(grid[startRow][12], grid[startRow+1][12]));
        map.put(TailDirection.LEFT_DOWN, mergeRegions(grid[startRow][13], grid[startRow+1][13]));
        map.put(TailDirection.RIGHT_DOWN, mergeRegions(grid[startRow][14], grid[startRow+1][14]));
        tailRegions.put(type, map);
    }
    
    private TextureRegion[][] get3x3Grid(TextureRegion[][] fullGrid, int startRow, int startCol) {
        TextureRegion[][] block = new TextureRegion[3][3];
        for(int r=0; r<3; r++) {
            for(int c=0; c<3; c++) {
                block[r][c] = fullGrid[startRow+r][startCol+c];
            }
        }
        return block;
    }
    
    private TextureRegion mergeRegions(TextureRegion top, TextureRegion bottom) {
       return new TextureRegion(top.getTexture(), top.getRegionX(), top.getRegionY(), 16, 32);
    }
    
    private void setType(DialogueType type) {
        this.currentType = type;
        rebuildBackground();
        updateTail(); // Update tail texture to match new type
    }
    
    private void rebuildBackground() {
        backgroundTable.clear();
        TextureRegion[][] reg = styleRegions.get(currentType);
        if (reg == null) return;
        
        // Row 0 (Top)
        backgroundTable.add(new Image(reg[0][0])); // TL
        backgroundTable.add(new Image(new TiledDrawable(reg[0][1]))).fillX().expandX(); // TC
        backgroundTable.add(new Image(reg[0][2])); // TR
        backgroundTable.row();
        
        // Row 1 (Mid)
        backgroundTable.add(new Image(new TiledDrawable(reg[1][0]))).fillY().expandY(); // ML
        backgroundTable.add(new Image(new TiledDrawable(reg[1][1]))).fill().expand(); // Center
        backgroundTable.add(new Image(new TiledDrawable(reg[1][2]))).fillY().expandY(); // MR
        backgroundTable.row();
        
        // Row 2 (Bot)
        backgroundTable.add(new Image(reg[2][0])); // BL
        backgroundTable.add(new Image(new TiledDrawable(reg[2][1]))).fillX().expandX(); // BC
        backgroundTable.add(new Image(reg[2][2])); // BR
    }
    
    /**
     * Show dialogue with default width (300)
     */
    public void show(String text, DialogueType type) {
        show(text, type, 300f);
    }

    /**
     * Show dialogue with custom preferred width
     */
    private boolean autoSize = true;

    public void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
    }

    /**
     * Show dialogue with custom preferred width
     */
    // Typewriter
    private String targetText = "";
    private float charTimer = 0;
    private float charsPerSecond = 30; // Speed
    private boolean typingFinished = true;

    /**
     * Show dialogue with custom preferred width
     */
    public void show(String text, DialogueType type, float preferredWidth) {
        setType(type);
        this.setVisible(true);
        
        // Typewriter Setup
        this.targetText = text;
        this.typingFinished = false;
        this.charTimer = 0;
        textLabel.setText(""); // Start empty
        
        // Update constraint (Pre-calculate height based on FULL text to avoid jumping?)
        // To avoid jumping, we should probably set the label text to full initially to get size, 
        // then clear it? Or just let it grow. For fixed size box, it doesn't matter.
        // But for bubble, it might grow.
        // Let's set it to empty for now. If AutoSize is true, it might grow as we type.
        // If AutoSize is false (Story), specific size is forced externally.
        
        contentTable.clearChildren();
        
        if (autoSize) {
             // For auto size, we might want to pre-calculate layout? 
             // But for now let's just let it type.
             contentTable.add(textLabel).pad(18).width(preferredWidth);
             stack.pack();
             this.setSize(stack.getWidth(), stack.getHeight());
        } else {
             textLabel.setAlignment(Align.topLeft);
             contentTable.add(textLabel).pad(18).fill().expand();
        }
        
        updateTail();
    }
    
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
                    
                    // Update size if needed for bubble
                    if (autoSize) {
                        stack.pack();
                        this.setSize(stack.getWidth(), stack.getHeight());
                        updateTail(); // Tail follows
                    }
                } else {
                    typingFinished = true;
                }
            }
        }
    }
    
    public boolean isFinished() {
        return typingFinished;
    }
    
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
     * Set tail horizontal position relative to the box (0 to width)
     */
    public void setTailPosition(float relativeX) {
        this.tailX = relativeX;
        updateTail();
    }
    
    public void setTailDirection(TailDirection direction) {
        this.currentTailDirection = direction;
        updateTail();
    }
    
    private void updateTail() {
        if (currentTailDirection == TailDirection.NONE) {
            tailImage.setVisible(false);
            return;
        }
        
        tailImage.setVisible(true);
        
        // Get region for current type and direction
        Map<TailDirection, TextureRegion> typeTails = tailRegions.get(currentType);
        if (typeTails != null) {
            TextureRegion reg = typeTails.get(currentTailDirection);
            if (reg != null) {
                tailImage.setDrawable(new TextureRegionDrawable(reg));
                tailImage.setSize(reg.getRegionWidth(), reg.getRegionHeight());
                
                // Position: X is relative, Y is fixed -16 to overlap
                tailImage.setPosition(tailX, -16);
            }
        }
    }
    
    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        if (stack != null) {
            stack.setSize(width, height);
        }
    }
    
    public void hide() {
        this.setVisible(false);
    }
}
