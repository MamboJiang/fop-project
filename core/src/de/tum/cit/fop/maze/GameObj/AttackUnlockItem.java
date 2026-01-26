package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * An item that, when collected, unlocks the attack ability for the player.
 * It also functions as a Key to allow level completion.
 */
public class AttackUnlockItem extends GameObject implements Collectable {

    public AttackUnlockItem(float x, float y, float width, float height, TextureRegion textureRegion) {
        super(x, y, width, height, textureRegion);
    }

    @Override
    public void collect(Character character) {
        character.setAttackUnlocked(true);
        character.setHasKey(true);
        
        setMarkedForRemoval(true);
        System.out.println("Attack Ability Unlocked! (And Key obtained)");

        de.tum.cit.fop.maze.GameControl.AchievementManager.getInstance()
                .onEvent(de.tum.cit.fop.maze.GameControl.EventType.COLLECT_ITEM, 1);
    }
}
