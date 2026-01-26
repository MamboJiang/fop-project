package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * An object that triggers a dialogue sequence when interacted with.
 */
public class DialogueTrigger extends GameObject {

    private boolean isNearPlayer = false;
    private String dialogueId;

    /**
     * Creates a DialogueTrigger.
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width
     * @param height Height
     * @param textureRegion Texture
     */
    public DialogueTrigger(float x, float y, float width, float height, TextureRegion textureRegion) {
        this(x, y, width, height, textureRegion, null);
    }

    /**
     * Creates a DialogueTrigger with ID.
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width
     * @param height Height
     * @param textureRegion Texture
     * @param dialogueId ID of dialogue
     */
    public DialogueTrigger(float x, float y, float width, float height, TextureRegion textureRegion, String dialogueId) {
        super(x, y, width, height, textureRegion);
        this.dialogueId = dialogueId;
    }

    /**
     * Checks if the player is close enough to interact.
     * 
     * @param playerPosition The position of the player.
     * @return True if within interaction range.
     */
    public boolean checkProximity(com.badlogic.gdx.math.Vector2 playerPosition) {
        float distance = position.dst(playerPosition);
        isNearPlayer = distance < 32.0f;
        return isNearPlayer;
    }

    /**
     * Checks if near player.
     * @return true if near
     */
    public boolean isNearPlayer() {
        return isNearPlayer;
    }
    
    /**
     * Gets dialogue ID.
     * @return Dialogue ID
     */
    public String getDialogueId() {
        return dialogueId;
    }

    /**
     * Sets dialogue ID.
     * @param dialogueId Dialogue ID
     */
    public void setDialogueId(String dialogueId) {
        this.dialogueId = dialogueId;
    }
}
