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


        Texture texture = new Texture(Gdx.files.internal("basictiles.png"));
        TextureRegion[][] regions = TextureRegion.split(texture, 16, 16);

        try {
            props.load(mapFile.read());


            int mapWidth = Integer.parseInt(props.getProperty("Width", "15"));
            int mapHeight = Integer.parseInt(props.getProperty("Height", "15"));


            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {

                    String key = x + "," + y;
                    float worldX = x * 16;
                    float worldY = y * 16;


                    int type = -1;
                    if (props.containsKey(key)) {
                        type = Integer.parseInt(props.getProperty(key).trim());
                    }


                    if (type != 0) {

                        objects.add(new Path(worldX, worldY, 16, 16, regions[1][1]));
                    }


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
                            obj = new EnemySpawnPoint(worldX, worldY, 16, 16, regions[3][6]);
                            break;
                        case 5:
                            obj = new Key(worldX, worldY, 16, 16, regions[4][4]);
                            break;
                        case 6:

                            obj = new GhostSpawnPoint(worldX, worldY, 16, 16, regions[3][6]);
                            break;
                        default:

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

    private static Texture mobsTexture;


    public static com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>[] getMobAnimations(int blockCol, int blockRow) {
        if (mobsTexture == null) {
            mobsTexture = new Texture(Gdx.files.internal("mobs.png"));
        }
        
        TextureRegion[][] tmp = TextureRegion.split(mobsTexture, 16, 16);
        com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>[] anims = new com.badlogic.gdx.graphics.g2d.Animation[4];
        
        int gridRow = blockRow * 4;
        int gridCol = blockCol * 3;
        
        float frameDuration = 0.2f;
        

        TextureRegion[] downFrames = new TextureRegion[3];
        downFrames[0] = tmp[gridRow][gridCol];
        downFrames[1] = tmp[gridRow][gridCol+1];
        downFrames[2] = tmp[gridRow][gridCol+2];
        anims[0] = new com.badlogic.gdx.graphics.g2d.Animation<>(frameDuration, downFrames);
        

        TextureRegion[] leftFrames = new TextureRegion[3];
        leftFrames[0] = tmp[gridRow+1][gridCol];
        leftFrames[1] = tmp[gridRow+1][gridCol+1];
        leftFrames[2] = tmp[gridRow+1][gridCol+2];
        anims[1] = new com.badlogic.gdx.graphics.g2d.Animation<>(frameDuration, leftFrames);
        

        TextureRegion[] rightFrames = new TextureRegion[3];
        rightFrames[0] = tmp[gridRow+2][gridCol];
        rightFrames[1] = tmp[gridRow+2][gridCol+1];
        rightFrames[2] = tmp[gridRow+2][gridCol+2];
        anims[2] = new com.badlogic.gdx.graphics.g2d.Animation<>(frameDuration, rightFrames);
        

        TextureRegion[] upFrames = new TextureRegion[3];
        upFrames[0] = tmp[gridRow+3][gridCol];
        upFrames[1] = tmp[gridRow+3][gridCol+1];
        upFrames[2] = tmp[gridRow+3][gridCol+2];
        anims[3] = new com.badlogic.gdx.graphics.g2d.Animation<>(frameDuration, upFrames);
        
        return anims;
    }

    public static List<FileHandle> getMapFiles() {
        List<FileHandle> files = new ArrayList<>();

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