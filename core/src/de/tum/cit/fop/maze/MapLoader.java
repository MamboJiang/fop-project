package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.GameObj.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MapLoader {

    public static List<GameObject> loadMap(FileHandle mapFile) {
        List<GameObject> objects = new ArrayList<>();
        Properties props = new Properties();

        // 加载贴图资源 (建议后续移到专门的 ResourceManager 中)
        Texture texture = new Texture(Gdx.files.internal("basictiles.png"));
        TextureRegion[][] regions = TextureRegion.split(texture, 16, 16);

        try {
            props.load(mapFile.read());

            // 1. 动态读取地图宽高，如果没有定义则默认为 15
            int mapWidth = Integer.parseInt(props.getProperty("Width", "15"));
            int mapHeight = Integer.parseInt(props.getProperty("Height", "15"));

            // 2. 遍历整个网格
            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {

                    String key = x + "," + y;
                    float worldX = x * 16;
                    float worldY = y * 16;

                    // 判断当前坐标有什么东西
                    int type = -1;
                    if (props.containsKey(key)) {
                        type = Integer.parseInt(props.getProperty(key).trim());
                    }

                    /**
                     * 核心逻辑：分层渲染
                     * 如果不是墙(type 0)，我们通常都需要先铺一层地板，
                     * 这样陷阱、敌人、钥匙才不会浮在黑色背景上。
                     */
                    if (type != 0) {
                        // 添加地板 (假设 basictiles.png 中 regions[1][1] 是地板)
                        // 注意：这里需要你确认一下地板在图片里的位置，通常是第2行第2列或其他位置
                        objects.add(new Path(worldX, worldY, 16, 16, regions[1][1]));
                    }

                    // 3. 添加具体的游戏对象
                    GameObject obj = null;
                    switch (type) {
                        case 0: // 墙壁 (不需要地板，因为它会遮住)
                            obj = new Wall(worldX, worldY, 16, 16, regions[0][0]);
                            break;
                        case 1: // 入口
                            obj = new EntryPoint(worldX, worldY, 16, 16, regions[6][0]);
                            break;
                        case 2: // 出口
                            obj = new Exit(worldX, worldY, 16, 16, regions[6][2]);
                            break;
                        case 3: // 陷阱
                            obj = new Trap(worldX, worldY, 16, 16, regions[9][2]);
                            break;
                        case 4: // 敌人
                            obj = new Enemy(worldX, worldY, 16, 16, regions[3][6]);
                            break;
                        case 5: // 钥匙/宝箱
                            obj = new Key(worldX, worldY, 16, 16, regions[4][4]);
                            break;
                        default:
                            // 如果 type 是 -1 (props里没有定义)，说明这里是纯地板
                            // 前面已经铺过地板了，所以这里不用做任何事
                            break;
                    }

                    if (obj != null) {
                        objects.add(obj);
                    }
                }
            }
        } catch (IOException e) {
            Gdx.app.error("MapLoader", "Failed to load map: " + mapFile.name(), e);
        }

        return objects;
    }

    public static List<FileHandle> getMapFiles() {
        List<FileHandle> files = new ArrayList<>();
        // Search for maps directory in common locations
        FileHandle dir = Gdx.files.internal("maps");
        if (!dir.exists() || !dir.isDirectory()) {
            dir = Gdx.files.local("maps");
            if (!dir.exists() || !dir.isDirectory()) {
                dir = Gdx.files.local("../maps");
            }
        }

        if (dir.exists() && dir.isDirectory()) {
            FileHandle[] propertiesFiles = dir.list(".properties");
            for (FileHandle file : propertiesFiles) {
                files.add(file);
            }
        }
        return files;
    }

}