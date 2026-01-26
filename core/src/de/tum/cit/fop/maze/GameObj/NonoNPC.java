package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;

public class NonoNPC extends GameObject {
    
    private float stateTime = 0f;
    private float baseY;
    private String dialogueId;
    
    public NonoNPC(float x, float y, TextureRegion texture) {
        super(x, y, 20, 20, texture);
        this.baseY = y;
        this.dialogueId = "nono-unlock";
    }
    
    public void update(float delta) {
        stateTime += delta;
        float offset = MathUtils.sin(stateTime * 3f) * 5f; 
        position.y = baseY + offset;
        bounds.y = position.y;
    }
    
    public boolean checkProximity(Vector2 playerPos) {
        return position.dst(playerPos) < 30f; 
    }
    
    public String getDialogueId() {
        return dialogueId;
    }
}
