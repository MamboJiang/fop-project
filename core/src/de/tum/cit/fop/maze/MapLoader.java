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

        // Load texture (assuming basictiles.png contains necessary tiles)
        // Note: In a real game, textures should be managed by a resource manager to avoid memory leaks
        Texture texture = new Texture(Gdx.files.internal("basictiles.png"));
        TextureRegion[][] regions = TextureRegion.split(texture, 16, 16);

        try {
            props.load(mapFile.read());

            for (String key : props.stringPropertyNames()) {
                String[] parts = key.split(",");
                if (parts.length != 2) continue;
                //获取每一行的x，y坐标和对应的类型
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                int type = Integer.parseInt(props.getProperty(key).trim());

                // Coordinate conversion: grid -> world (16px per tile)
                float worldX = x * 16;
                float worldY = y * 16;

                // Select texture region based on type (modulo to preventOutOfBounds)
                //TextureRegion region = regions[0][Math.min(type, regions[0].length - 1)];

                GameObject obj = null;
                switch (type) {
                    case 0:
                        obj = new Wall(worldX, worldY, 16, 16, regions[0][0]);
                        break;
                    case 1:
                        obj = new EntryPoint(worldX, worldY, 16, 16, regions[6][0]);
                        break;
                    case 2:
                        obj = new Exit(worldX, worldY, 16, 16, regions[6][2]);
                        break;
                    case 3:
                        obj = new Trap(worldX, worldY, 16, 16, regions[9][2]);
                        break;
                    case 4:
                        obj = new Enemy(worldX, worldY, 16, 16, regions[3][6]);
                        break;
                    case 5:
                        obj = new Key(worldX, worldY, 16, 16, regions[4][4]);
                        break;
                    default:
                        // Handle unknown types or skip
                        break;
                }

                if (obj != null) {
                    objects.add(obj);
                }
            }
        } catch (IOException e) {
            Gdx.app.error("MapLoader", "Failed to load map: " + mapFile.name(), e);
        } catch (NumberFormatException e) {
            Gdx.app.error("MapLoader", "Invalid map format in: " + mapFile.name(), e);
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