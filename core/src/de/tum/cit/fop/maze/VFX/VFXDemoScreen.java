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

/**
 * Screen for demonstrating Visual Effects (VFX) like lighting.
 */
public class VFXDemoScreen implements Screen {

    private final MazeRunnerGame game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private FitViewport viewport;
    
    private Texture bgTexture;
    private LightManager lightManager;
    private Array<PointLight> lights;
    

    private Stage stage;
    private PointLight mouseLight;

    /**
     * Constructor for VFXDemoScreen.
     * @param game Main game instance.
     */
    public VFXDemoScreen(MazeRunnerGame game) {
        this.game = game;
        this.batch = game.getSpriteBatch();
        

        camera = new OrthographicCamera();
        viewport = new FitViewport(1920, 1080, camera);
        

        lightManager = new LightManager();
        lights = new Array<>();
        

        for (int i = 0; i < 10; i++) {
            float x = MathUtils.random(200, 1700);
            float y = MathUtils.random(200, 900);
            Color c = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
            float dist = MathUtils.random(300, 600);
            lights.add(new PointLight(x, y, dist, c, 1.0f));
        }
        

        mouseLight = new PointLight(0, 0, 800, new Color(1, 0.8f, 0.6f, 1), 1.0f);
        lights.add(mouseLight);

        bgTexture = new Texture(Gdx.files.internal("basictiles.png")); 

        
        setupUI();
    }
    
    /**
     * Sets up the UI stage and elements.
     */
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

    /**
     * Called when this screen becomes the current screen for the game.
     */
    @Override
    public void show() {
        lightManager.resize((int)viewport.getWorldWidth(), (int)viewport.getWorldHeight());
    }

    /**
     * Called when the screen should render itself.
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePos);
        mouseLight.setPosition(mousePos.x, mousePos.y);

        mouseLight.distance = 800 + MathUtils.sin(System.currentTimeMillis() / 200f) * 50;
        
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        
        batch.begin();
        for (int x = 0; x < 1920; x+=64) {
            for (int y = 0; y < 1080; y+=64) {
                batch.setColor(0.5f, 0.5f, 0.5f, 1);
                if (bgTexture != null) batch.draw(bgTexture, x, y, 64, 64);
            }
        }
        batch.setColor(Color.WHITE);
        batch.end();

        lightManager.render(batch, viewport, lights);

        stage.act(delta);
        stage.draw();
    }

    /**
     * Called when the application is resized.
     * @param width The new width.
     * @param height The new height.
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        lightManager.resize(width, height);
    }

    /**
     * Called when the application is paused.
     */
    @Override
    public void pause() {}

    /**
     * Called when the application is resumed from a paused state.
     */
    @Override
    public void resume() {}

    /**
     * Called when this screen is no longer the current screen for the game.
     */
    @Override
    public void hide() {
        dispose();
    }

    /**
     * Called when this screen should release all resources.
     */
    @Override
    public void dispose() {
        lightManager.dispose();
        stage.dispose();
        if (bgTexture != null) bgTexture.dispose();
    }
}
