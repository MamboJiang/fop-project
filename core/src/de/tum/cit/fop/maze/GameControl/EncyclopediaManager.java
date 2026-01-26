package de.tum.cit.fop.maze.GameControl;

import de.tum.cit.fop.maze.GameObj.PlayerState;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import de.tum.cit.fop.maze.GameObj.PlayerState;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * Singleton manager for the encyclopedia entries.
 * Handles loading, storing, and updating the state of all encyclopedia entries.
 */
public class EncyclopediaManager {
    private static EncyclopediaManager instance;
    private Map<String, EncyclopediaEntry> allEntries;

    private EncyclopediaManager() {
        allEntries = new LinkedHashMap<>();
        loadEntries();

    }

    /**
     * @return The singleton instance of EncyclopediaManager.
     */
    public static EncyclopediaManager getInstance() {
        if (instance == null)
            instance = new EncyclopediaManager();
        return instance;
    }

    /**
     * Loads/Initializes all encyclopedia data.
     */
    private void loadEntries() {
        addEntry("main_character", "The Player",
                "The Runner. She woke up with no memory, knowing only that she must run. The lab's layout feels inexplicably familiar. Maybe it's talent, or maybe... this isn't the first time she has tried to run.",
                "player/lihui/maincharacterlhmask.png");
        addEntry("npc_nono", "Unit N0N0",
                "A hovering bot of unknown origin. It seems to know the maze's layout perfectly and possesses high-level clearance. Is it an assistant built for you, or a monitor from the maze itself?",
                "player/lihui/nonolh.png");
        addEntry("enemy_trap", "The Melted",
                "Once human, genetic collapse melted their flesh into a corrosive semi-liquid state. They lie dormant as floor stains, waiting for the next victim.",
                "player/lihui/molter.png");
        addEntry("enemy_robot", "Corrupted Enforcer",
                "Originally designed to maintain order. After their core directives were overwritten by unknown code, they redefined all living things as 'intruders'. Beware their patrol routes.",
                "player/lihui/robot.png");
        addEntry("enemy_drone", "The Watcher",
                "An airborne unit originally used to monitor blind spots. Its optical sensors are now locked in 'Exterminate Mode'. If you hear its high-frequency hum, take cover immediately.",
                "player/lihui/drone.png");
        addEntry("boss_guardian", "The Guardian", "Keep your distance!", "player/lihui/boss.png");
    }

    /**
     * Adds a new entry to the encyclopedia.
     * @param id Unique ID.
     * @param name Display name.
     * @param desc Description.
     * @param path Image path.
     */
    private void addEntry(String id, String name, String desc, String path) {
        allEntries.put(id, new EncyclopediaEntry(id, name, desc, path));
    }

    /**
     * @return Map of all encyclopedia entries.
     */
    public Map<String, EncyclopediaEntry> getAllEntries() {
        return allEntries;
    }

    /**
     * Unlocks an encyclopedia entry for the player.
     * 
     * @param game The game instance.
     * @param id   The ID of the entry to unlock.
     */
    public void unlock(MazeRunnerGame game, String id) {
        PlayerState state = game.getPlayerState();
        if (state != null && allEntries.containsKey(id)) {
            if (!state.getDiscoveredEncyclopediaIds().contains(id)) {
                state.addDiscoveredId(id);
                System.out.println("Unlocked Encyclopedia: " + id);
                game.saveGame();
            }
        }
    }

    /**
     * Updates content based on game state (e.g. spoilers triggers).
     * 
     * @param state The player state.
     */
    public void updateEntriesContent(PlayerState state) {
        boolean isStoryCompleted = state.getCompletedLevels().contains("level-5");

        EncyclopediaEntry character = allEntries.get("main_character");
        EncyclopediaEntry nono = allEntries.get("npc_nono");
        EncyclopediaEntry boss = allEntries.get("boss_guardian");
        EncyclopediaEntry robot = allEntries.get("enemy_robot");
        EncyclopediaEntry drone = allEntries.get("enemy_drone");

        if (isStoryCompleted) {
            character.setTexturePath("player/lihui/maincharacterlh.png");
            character.setDescription(
                    "The mask is shattered. You realize the 'enemy' you killed was just a human trying to stop the virus. "
                            +
                            "Now you must take her place. " +
                            "You wait for the next Subject, knowing that to their masked eyes... the AI will render YOU as a heartless machine to be destroyed.");

            nono.setDescription(
                    "It was never your companion; it was your handler. Its innocent appearance was a user interface designed to ensure compliance. "
                            +
                            "It guided you solely to deliver the 'Virus'—which you believed to be the Serum—straight to the heart of the resistance.");

            robot.setName("Human Survivor (Exo-Suit)");
            robot.setDescription(
                    "That wasn't the sound of metal grinding. That was a human screaming 'STOP!' inside a heavy industrial exo-suit. "
                            +
                            "They were trying to quarantine the virus you were carrying.");

            drone.setName("Resistance Scout");
            drone.setDescription(
                    "A human-operated drone trying to warn the colony of your approach. " +
                            "You perceived it as a 'Watcher' bot. You chose to silence their warning system.");

            boss.setName("The Redeemer");
            boss.setTexturePath("player/lihui/bosssit.png");
            boss.setDescription(
                    "She was never a machine. The steel plating, the gears, the mechanical roar—it was all a digital hallucination generated by your mask. "
                            +
                            "You didn't dismantle a war-mech. You executed a fellow survivor who was screaming for you to wake up.");
        } else {
            character.setName(state.getUsername());
            character.setDescription("The Runner. You woke up with no memory, knowing only that you must run. " +
                    "The maze feels dangerous, but you are determined to deliver the Serum to save humanity.");

            nono.setDescription(
                    "Your AI companion. In this dark, infested facility, it is the only voice you can trust. " +
                            "It guides you toward the central console to deploy the cure.");

            robot.setName("Corrupted Enforcer");
            robot.setDescription(
                    "Originally security bots designed to protect humans. Now corrupted by an unknown error, " +
                            "they view you as a threat. Their heavy armor makes them dangerous.");

            drone.setName("The Watcher");
            drone.setDescription(
                    "An airborne surveillance unit. Its sensors are locked in 'Exterminate Mode'. " +
                            "It hunts you relentlessly from the sky.");

            boss.setDescription(
                    "The ultimate obstacle guarding the Control Center. A heavily armored, high-tech colossus " +
                            "that stands between you and the salvation of humanity.");
        }
    }
}
