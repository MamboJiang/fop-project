package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * An item that, when collected:
 * 1. Functions as a key (hasKey = true)
 * 2. Changes player appearance to mask version.
 */
public class MaskItem extends GameObject implements Collectable {

    public MaskItem(float x, float y, float width, float height, TextureRegion textureRegion) {
        // Fix width/height to reasonable size (e.g. 16x16) but keep super generic
        super(x, y, width, height, textureRegion);
    }

    @Override
    public void collect(Character character) {
        // Grant key
        character.setHasKey(true);
        
        // Change sprite
        character.loadMaskAppearance();
        
        // Remove item
        setMarkedForRemoval(true);
        
        System.out.println("Mask obtained! Appearance changed.");
        
        // Sound
        // Need access to game instance for sound? Character has game reference but doesn't expose it easily for sound unless we pass it.
        // But AttackUnlockItem used `game.playPowerUpSound()`? 
        // Wait, current Character logic: `collisionAddressing` calls `((Collectable) hitObject).collect(this);` THEN lines 235 `game.playPowerUpSound();`.
        // So sound is handled by Character/Game loop.
        
        de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance()
                .onEvent(de.tum.cit.fop.maze.GameControl.EventType.COLLECT_ITEM, 1);
    }
}
