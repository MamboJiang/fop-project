package de.tum.cit.fop.maze.GameObj;

/**
 * Interface for objects that can be collected by the player.
 */
public interface Collectable {
    /**
     * Called when the character touches this object.
     * @param character The character collecting the item.
     */
    void collect(Character character);
}
