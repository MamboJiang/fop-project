package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Marker object for boss spawn location.
 */
public class BossSpawnPoint extends GameObject {
    public BossSpawnPoint(float x, float y, float width, float height, TextureRegion textureRegion) {
        super(x, y, width, height, textureRegion);
    }
}
