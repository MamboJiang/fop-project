package de.tum.cit.fop.maze.GameControl;

import de.tum.cit.fop.maze.GameObj.PlayerState;
import java.util.HashMap;
import java.util.Map;
import de.tum.cit.fop.maze.GameObj.PlayerState;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class EncyclopediaManager {
    private static EncyclopediaManager instance;
    private Map<String, EncyclopediaEntry> allEntries;


    private EncyclopediaManager() {
        allEntries = new HashMap<>();
        loadEntries();

    }

    public static EncyclopediaManager getInstance() {
        if (instance == null) instance = new EncyclopediaManager();
        return instance;
    }

    // 初始化所有图鉴数据 (你可以硬编码在这里，或者从 JSON 文件读取)
    private void loadEntries() {
        addEntry("main_character", "The Player", "The player", "assets/player/lihui/maincharacterlhupscale.png");
        addEntry("npc_nono", "Unit N0N0", "A hovering bot of unknown origin. It seems to know the maze's layout perfectly and possesses high-level clearance. Is it an assistant built for you, or a monitor from the maze itself?", "assets/player/lihui/nonolh.png");
        addEntry("enemy_trap", "The Melted", "Once human, genetic collapse melted their flesh into a corrosive semi-liquid state. They lie dormant as floor stains, waiting for the next victim.", "assets/player/lihui/nonolh.png");
        addEntry("enemy_robot", "Corrupted Enforcer", "Originally designed to maintain order. After their core directives were overwritten by unknown code, they redefined all living things as 'intruders'. Beware their patrol routes.", "assets/player/lihui/nonolh.png");
        addEntry("enemy_drone", "The Watcher", "An airborne unit originally used to monitor blind spots. Its optical sensors are now locked in 'Exterminate Mode'. If you hear its high-frequency hum, take cover immediately.", "assets/player/lihui/nonolh.png");
        addEntry("boss_guardian", "The Guardian", "Keep your distance!", "assets/player/lihui/boss.png");

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
}
