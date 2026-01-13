package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * An object that triggers a dialogue sequence when interacted with.
 */
public class DialogueTrigger extends GameObject {

    private boolean isNearPlayer = false;
    private String dialogueId;

    public DialogueTrigger(float x, float y, float width, float height, TextureRegion textureRegion) {
        this(x, y, width, height, textureRegion, null);
    }

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
        // Interaction radius of 32 units (2 blocks)
        isNearPlayer = distance < 32.0f;
        return isNearPlayer;
    }

    public boolean isNearPlayer() {
        return isNearPlayer;
    }
    
    public String getDialogueId() {
        return dialogueId;
    }

    public void setDialogueId(String dialogueId) {
        this.dialogueId = dialogueId;
    }
}
