package de.tum.cit.fop.maze.GameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.GameObj.Character;
import de.tum.cit.fop.maze.GameScreen;

public class HUD {
    private final Stage stage;
    private final Texture objectsTexture;
    private final TextureRegion[] heartRegions;
    private final TextureRegion keyRegion;
    
    private Image heartImage;
    private Image keyImage;
    private Table table;
    private Table debugTable;
    
    // Dependencies
    private final GameScreen gameScreen;
    private final Skin skin;
    private Character character; // Reference to character for debug actions

    public HUD(SpriteBatch spriteBatch, GameScreen gameScreen, Skin skin) {
        this.gameScreen = gameScreen;
        this.skin = skin;
        stage = new Stage(new ScreenViewport(), spriteBatch);
        
        // Load textures
        objectsTexture = new Texture(Gdx.files.internal("objects.png"));
        TextureRegion[][] tmp = TextureRegion.split(objectsTexture, 16, 16);
        
        // Hearts: Row 0, Cols 5-9 (5=4 lives, 6=3 lives, 7=2 lives, 8=1 life, 9=0 lives)
        // Adjusting logic: The user said "5-9 respectively are 4, 3, 2, 1, 0 lives"
        // Index 5: 4 lives
        // Index 6: 3 lives
        // Index 7: 2 lives
        // Index 8: 1 lives
        // Index 9: 0 lives
        heartRegions = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            heartRegions[i] = tmp[0][4 + i];
        }
        
        // Key: Row 4, Col 0
        keyRegion = tmp[4][0];
        
        setupUI();
        setupDebugMenu();
    }

    private void setupUI() {
        table = new Table();
        table.top();
        table.setFillParent(true);
        
        // Left: Heart
        // Initial heart image (4 lives -> index 0)
        heartImage = new Image(heartRegions[0]);
        
        // Right: Key
        // Initially invisible or specific icon? User said "show if key is there". 
        // Let's assume we show the key icon if collected, or maybe a grayed out version?
        // Requirement: "Top Right display if key is still there" (meaning present in map? or collected?)
        // "右上角显示钥匙还在不在" -> "Display in top right whether key is still there (not collected yet?)"
        // Or maybe "Display key if collected".
        // Let's interpret "Key still there" as: Show Key icon if player DOES NOT have key yet? 
        // Or logic: "If key is collected, show key".
        // Let's stick to standard: Show empty slot or key when collected.
        // Wait, "Display whether key is still there" sounds like "Key is on map".
        // Let's try: Always show key, maybe dim it if not collected?
        // Re-reading: "Show whether key is NOT there" or "Show key status".
        // Let's implement: Show Key Image always for now.
        keyImage = new Image(keyRegion);
        
        table.add(heartImage).expandX().left().pad(10).size(64, 64);
        table.add(keyImage).expandX().right().pad(10).size(64, 64);
        
        stage.addActor(table);
        stage.addActor(table);
    }
    
    private void setupDebugMenu() {
        debugTable = new Table();
        debugTable.bottom().left();
        debugTable.setFillParent(true);
        
        // Container for content buttons
        final Table contentTable = new Table();
        contentTable.setVisible(false); // Initially hidden
        
        // Toggle Menu Button (Always visible)
        TextButton toggleMenuBtn = new TextButton("Debug", skin);
        toggleMenuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                contentTable.setVisible(!contentTable.isVisible());
            }
        });
        
        // Debug Toggle Button
        TextButton debugBtn = new TextButton("Debug Box", skin);
        debugBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                gameScreen.toggleDebug();
            }
        });
        
        // HP + Button
        TextButton hpPlusBtn = new TextButton("HP +", skin);
        hpPlusBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (character != null) character.addLives(1);
            }
        });
        
        // HP - Button
        TextButton hpMinusBtn = new TextButton("HP -", skin);
        hpMinusBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (character != null) character.addLives(-1);
            }
        });
        
        // Key Toggle Button
        TextButton keyBtn = new TextButton("Toggle Key", skin);
        keyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (character != null) character.setHasKey(!character.hasKey());
            }
        });
        
        // Add buttons to content table vertically
        contentTable.add(debugBtn).left().pad(5).row();
        contentTable.add(hpPlusBtn).left().pad(5).row();
        contentTable.add(hpMinusBtn).left().pad(5).row();
        contentTable.add(keyBtn).left().pad(5).row();
        
        // Add content table and toggle button to main table
        // We want the toggle button at the bottom, and content above it
        debugTable.add(contentTable).left().pad(5).row();
        debugTable.add(toggleMenuBtn).left().pad(5);
        
        stage.addActor(debugTable);
    }

    public void update(Character character) {
        this.character = character;
        
        // Update Heart
        int lives = character.getLives();
        // 4 lives -> index 0
        // 3 lives -> index 1
        // 2 lives -> index 2
        // 1 lives -> index 3
        // 0 lives -> index 4
        int heartIndex = 4 - lives;
        if (heartIndex < 0) heartIndex = 0;
        if (heartIndex > 4) heartIndex = 4;
        
        heartImage.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(heartRegions[heartIndex]));
        
        // Update Key
        if (character.hasKey()) {
             keyImage.setColor(Color.WHITE); // Normal color
        } else {
             keyImage.setColor(Color.DARK_GRAY); // Dimmed if not collected
        }
    }
    
    public Stage getStage() {
        return stage;
    }

    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }
    
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        objectsTexture.dispose();
    }
}
