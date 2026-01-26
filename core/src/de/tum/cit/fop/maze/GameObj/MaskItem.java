package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * An item that, when collected:
 * 1. Functions as a key (hasKey = true)
 * 2. Changes player appearance to mask version.
 */
public class MaskItem extends GameObject implements Collectable {

    /**
     * Creates a MaskItem.
     * @param x X pos
     * @param y Y pos
     * @param width Width
     * @param height Height
     * @param textureRegion Texture
     */
    public MaskItem(float x, float y, float width, float height, TextureRegion textureRegion) {
        super(x, y, width, height, textureRegion);
    }

    /**
     * Collects mask and changes appearance.
     * @param character Collecting character
     */
    @Override
    public void collect(Character character) {

        character.setHasKey(true);


        character.loadMaskAppearance();

        setMarkedForRemoval(true);

        System.out.println("Mask obtained! Appearance changed.");


        de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance()
                .onEvent(de.tum.cit.fop.maze.GameControl.EventType.COLLECT_ITEM, 1);
    }
}
