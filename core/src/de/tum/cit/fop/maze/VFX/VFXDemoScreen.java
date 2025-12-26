package de.tum.cit.fop.maze.VFX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class VFXDemoScreen implements Screen {

    private final MazeRunnerGame game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private FitViewport viewport;
    
    private Texture bgTexture;
    private LightManager lightManager;
    private Array<PointLight> lights;
    
    // UI
    private Stage stage;
    private PointLight mouseLight;

    public VFXDemoScreen(MazeRunnerGame game) {
        this.game = game;
        this.batch = game.getSpriteBatch();
        
        // Setup Camera (1920x1080)
        camera = new OrthographicCamera();
        viewport = new FitViewport(1920, 1080, camera);
        
        // Setup Lights
        lightManager = new LightManager();
        lights = new Array<>();
        
        // Random Lights
        for (int i = 0; i < 10; i++) {
            float x = MathUtils.random(200, 1700);
            float y = MathUtils.random(200, 900);
            Color c = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
            float dist = MathUtils.random(300, 600);
            lights.add(new PointLight(x, y, dist, c, 1.0f));
        }
        
        // Mouse Light (Torch)
        mouseLight = new PointLight(0, 0, 800, new Color(1, 0.8f, 0.6f, 1), 1.0f);
        lights.add(mouseLight);
        
        // Background (Use existing map assets or generate simple pattern)
        // We'll just draw a tiled floor pattern or solid color to see lights
        bgTexture = new Texture(Gdx.files.internal("basictiles.png")); 
        // fallback? Let's check logic later.
        
        setupUI();
    }
    
    private void setupUI() {
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);
        
        Table table = new Table();
        table.setFillParent(true);
        table.top().left();
        
        Label title = new Label("VFX Demo: Dynamic Lighting (FBO)", game.getSkin(), "title");
        table.add(title).pad(20).row();
        
        TextButton backBtn = new TextButton("Back to Menu", game.getSkin());
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.goToMenu();
            }
        });
        table.add(backBtn).pad(20);
        
        stage.addActor(table);
    }

    @Override
    public void show() {
        lightManager.resize((int)viewport.getWorldWidth(), (int)viewport.getWorldHeight()); // Init FBO
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Update Mouse Light
        Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePos);
        mouseLight.setPosition(mousePos.x, mousePos.y);
        
        // Pulse Effect
        mouseLight.distance = 800 + MathUtils.sin(System.currentTimeMillis() / 200f) * 50;
        
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        
        batch.begin();
        // Draw Tiled Background
        for (int x = 0; x < 1920; x+=64) {
            for (int y = 0; y < 1080; y+=64) {
                batch.setColor(0.5f, 0.5f, 0.5f, 1); // Dim base color
                if (bgTexture != null) batch.draw(bgTexture, x, y, 64, 64);
            }
        }
        batch.setColor(Color.WHITE);
        batch.end();
        
        // Render Lights
        lightManager.render(batch, viewport, lights);
        
        // Render UI
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        lightManager.resize(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        lightManager.dispose();
        stage.dispose();
        if (bgTexture != null) bgTexture.dispose();
    }
}
