package de.tum.cit.fop.maze.VFX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Matrix4;

/**
 * Manages dynamic lighting effects using FrameBuffer Objects (FBO).
 */
public class LightManager implements Disposable {
    private FrameBuffer fbo;
    private Texture lightTexture;
    private TextureRegion fboRegion;
    private Color ambientColor;
    private boolean enabled = true;

    /**
     * Constructor for LightManager.
     */
    public LightManager() {

        this.ambientColor = new Color(0.1f, 0.1f, 0.1f, 1f);
        
        createLightTexture();
    }
    

    /**
     * Creates the light texture used for point lights.
     */
    private void createLightTexture() {
        int size = 128;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        

        int cx = size / 2;
        int cy = size / 2;
        float maxDist = size / 2f;
        
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dist = Vector2.dst(x, y, cx, cy);
                if (dist <= maxDist) {
                    float alpha = 1.0f - (dist / maxDist);

                    alpha = alpha * alpha; 
                    pixmap.setColor(1, 1, 1, alpha);
                    pixmap.drawPixel(x, y);
                }
            }
        }
        
        lightTexture = new Texture(pixmap);
        pixmap.dispose();
    }
    
    /**
     * Resizes the FBO to match screen dimensions.
     * @param width Screen width.
     * @param height Screen height.
     */
    public void resize(int width, int height) {
        if (fbo != null) fbo.dispose();

        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);
    }
    
    /**
     * Renders the light map and applies it to the scene.
     * @param batch SpriteBatch to use.
     * @param viewport Viewport to project lights to.
     * @param lights List of active point lights.
     */
    public void render(SpriteBatch batch, Viewport viewport, Array<PointLight> lights) {
        if (fbo == null || !enabled) return;

        fbo.begin();

        Gdx.gl.glClearColor(ambientColor.r, ambientColor.g, ambientColor.b, ambientColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.begin();

        for (PointLight light : lights) {
             float size = light.distance * 2;
             batch.setColor(light.color.r, light.color.g, light.color.b, light.intensity);
             batch.draw(lightTexture, light.position.x - size/2, light.position.y - size/2, size, size);
        }
        
        batch.end();
        batch.setColor(Color.WHITE);
        fbo.end();

        
        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
        batch.setProjectionMatrix(batch.getProjectionMatrix().idt());

        batch.setProjectionMatrix(viewport.getCamera().combined);
        
        batch.begin();
        float x = viewport.getCamera().position.x - viewport.getWorldWidth()/2;
        float y = viewport.getCamera().position.y - viewport.getWorldHeight()/2;
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        batch.draw(fboRegion, x, y, w, h);
        batch.end();

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Disposes of the resources used by the light manager.
     */
    @Override
    public void dispose() {
        if (fbo != null) fbo.dispose();
        if (lightTexture != null) lightTexture.dispose();
    }
    
    /**
     * Sets the ambient color of the light manager.
     * @param r Red component.
     * @param g Green component.
     * @param b Blue component.
     * @param a Alpha component.
     */
    public void setAmbientColor(float r, float g, float b, float a) {
        this.ambientColor.set(r, g, b, a);
    }
}
