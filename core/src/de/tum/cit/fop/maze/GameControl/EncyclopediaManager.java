package de.tum.cit.fop.maze.GameControl;

import de.tum.cit.fop.maze.GameObj.PlayerState;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import de.tum.cit.fop.maze.GameObj.PlayerState;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class EncyclopediaManager {
    private static EncyclopediaManager instance;
    private Map<String, EncyclopediaEntry> allEntries;


    private EncyclopediaManager() {
        allEntries = new LinkedHashMap<>();
        loadEntries();

    }

    public static EncyclopediaManager getInstance() {
        if (instance == null) instance = new EncyclopediaManager();
        return instance;
    }

    // 初始化所有图鉴数据 (你可以硬编码在这里，或者从 JSON 文件读取)
    private void loadEntries() {
        addEntry("main_character", "The Player", "The Runner. She woke up with no memory, knowing only that she must run. The lab's layout feels inexplicably familiar. Maybe it's talent, or maybe... this isn't the first time she has tried to run.", "player/lihui/maincharacterlhmask.png");
        addEntry("npc_nono", "Unit N0N0", "A hovering bot of unknown origin. It seems to know the maze's layout perfectly and possesses high-level clearance. Is it an assistant built for you, or a monitor from the maze itself?", "player/lihui/nonolh.png");
        addEntry("enemy_trap", "The Melted", "Once human, genetic collapse melted their flesh into a corrosive semi-liquid state. They lie dormant as floor stains, waiting for the next victim.", "player/lihui/molter.png");
        addEntry("enemy_robot", "Corrupted Enforcer", "Originally designed to maintain order. After their core directives were overwritten by unknown code, they redefined all living things as 'intruders'. Beware their patrol routes.", "player/lihui/robot.png");
        addEntry("enemy_drone", "The Watcher", "An airborne unit originally used to monitor blind spots. Its optical sensors are now locked in 'Exterminate Mode'. If you hear its high-frequency hum, take cover immediately.", "player/lihui/drone.png");
        addEntry("boss_guardian", "The Guardian", "Keep your distance!", "player/lihui/boss.png");

        // ... 添加更多
    }

    private void addEntry(String id, String name, String desc, String path) {
        allEntries.put(id, new EncyclopediaEntry(id, name, desc, path));
    }

    public Map<String, EncyclopediaEntry> getAllEntries() {
        return allEntries;
    }

    // 核心逻辑：解锁
    public void unlock(MazeRunnerGame game, String id) {
        PlayerState state = game.getPlayerState();
        if (state != null && allEntries.containsKey(id)) {
            if (!state.getDiscoveredEncyclopediaIds().contains(id)) {
                state.addDiscoveredId(id);
                // 这里可以播放一个音效或显示 HUD 提示 "New Entry: " + allEntries.get(id).getName()
                System.out.println("Unlocked Encyclopedia: " + id);
                game.saveGame();
            }
        }
    }

    public void updateEntriesContent(PlayerState state) {
        // 1. 获取通关状态
        boolean isStoryCompleted = state.getCompletedLevels().contains("level-5");

        // 2. 获取你要修改的条目
        EncyclopediaEntry character = allEntries.get("main_character");
        EncyclopediaEntry nono = allEntries.get("npc_nono");
        EncyclopediaEntry boss = allEntries.get("boss_guardian");
        EncyclopediaEntry robot = allEntries.get("enemy_robot");
        EncyclopediaEntry drone = allEntries.get("enemy_drone");
        // ... 其他条目

        // 3. 根据状态，用 Setter 暴力覆盖文案
        if (isStoryCompleted) {
            // --- 真相模式 ---
            character.setTexturePath("player/lihui/maincharacterlh.png");
            character.setDescription("The mask is shattered. You realize the 'enemy' you killed was just a human trying to stop the virus. " +
                    "Now you must take her place. " +
                    "You wait for the next Subject, knowing that to their masked eyes... the AI will render YOU as a heartless machine to be destroyed.");

            nono.setDescription("It was never your companion; it was your handler. Its innocent appearance was a user interface designed to ensure compliance. " +
                    "It guided you solely to deliver the 'Virus'—which you believed to be the Serum—straight to the heart of the resistance.");

            robot.setName("Human Survivor (Exo-Suit)");
            robot.setDescription(
                    "That wasn't the sound of metal grinding. That was a human screaming 'STOP!' inside a heavy industrial exo-suit. " +
                            "They were trying to quarantine the virus you were carrying."
            );

            drone.setName("Resistance Scout");
            drone.setDescription(
                    "A human-operated drone trying to warn the colony of your approach. " +
                            "You perceived it as a 'Watcher' bot. You chose to silence their warning system."
            );

            boss.setName("The Redeemer");
            boss.setTexturePath("player/lihui/bosssit.png");
            boss.setDescription("She was never a machine. The steel plating, the gears, the mechanical roar—it was all a digital hallucination generated by your mask. " +
                    "You didn't dismantle a war-mech. You executed a fellow survivor who was screaming for you to wake up.");
        } else {
            // --- 表象模式 (必须写！否则切换存档时切不回来) ---
            character.setName(state.getUsername()); // 恢复成玩家名
            character.setDescription("The Runner. You woke up with no memory, knowing only that you must run. " +
                    "The maze feels dangerous, but you are determined to deliver the Serum to save humanity.");


            nono.setDescription("Your AI companion. In this dark, infested facility, it is the only voice you can trust. " +
                    "It guides you toward the central console to deploy the cure.");

            robot.setName("Corrupted Enforcer");
            robot.setDescription("Originally security bots designed to protect humans. Now corrupted by an unknown error, " +
                    "they view you as a threat. Their heavy armor makes them dangerous.");

            drone.setName("The Watcher");
            drone.setDescription(
                    "An airborne surveillance unit. Its sensors are locked in 'Exterminate Mode'. " +
                            "It hunts you relentlessly from the sky."
            );

            boss.setDescription("The ultimate obstacle guarding the Control Center. A heavily armored, high-tech colossus " +
                    "that stands between you and the salvation of humanity.");
        }
    }
}
