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

public class LightManager implements Disposable {
    private FrameBuffer fbo;
    private Texture lightTexture;
    private TextureRegion fboRegion;
    private Color ambientColor;
    private boolean enabled = true;

    public LightManager() {
        // Darkness level (Higher A = Darker)
        // RGB is the color of the "shadows". Usually black.
        // If we use MULTIPLY blend:
        // Ambient of (0.2, 0.2, 0.2, 1) means unlit areas are 20% bright.
        // Light of (1, 1, 1, 1) means lit areas are 100% bright.
        this.ambientColor = new Color(0.1f, 0.1f, 0.1f, 1f); // Very dark ambient
        
        createLightTexture();
    }
    
    // Create a fuzzy circle texture procedurally
    private void createLightTexture() {
        int size = 128;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        
        // Draw radial gradient
        // Center
        int cx = size / 2;
        int cy = size / 2;
        float maxDist = size / 2f;
        
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dist = Vector2.dst(x, y, cx, cy);
                if (dist <= maxDist) {
                    float alpha = 1.0f - (dist / maxDist);
                    // Make it fall off non-linearly for better look
                    alpha = alpha * alpha; 
                    pixmap.setColor(1, 1, 1, alpha);
                    pixmap.drawPixel(x, y);
                }
            }
        }
        
        lightTexture = new Texture(pixmap);
        pixmap.dispose();
    }
    
    public void resize(int width, int height) {
        if (fbo != null) fbo.dispose();
        // Create FBO
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true); // FBOs are flipped
    }
    
    public void render(SpriteBatch batch, Viewport viewport, Array<PointLight> lights) {
        if (fbo == null || !enabled) return;
        
        // 1. Render Light Map to FBO
        fbo.begin();
        
        // Clear to Ambient Color
        Gdx.gl.glClearColor(ambientColor.r, ambientColor.g, ambientColor.b, ambientColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Setup Batch for FBO rendering (Additive Blending)
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.begin();
        
        // Draw Lights
        for (PointLight light : lights) {
             float size = light.distance * 2;
             batch.setColor(light.color.r, light.color.g, light.color.b, light.intensity);
             batch.draw(lightTexture, light.position.x - size/2, light.position.y - size/2, size, size);
        }
        
        batch.end();
        batch.setColor(Color.WHITE); // Reset
        fbo.end();
        
        // 2. Render FBO over Screen (Multiplicative Blending)
        // Switch to Screen Projection (render full screen quad)
        // We need a batch set to identity/screen projection. 
        // Or we can just draw it using the game batch if we reset blending.
        
        // Standard lighting blend: Dst * Src
        // Src (LightMap) * Dst (Scene)
        // GL_DST_COLOR, GL_ZERO
        
        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
        batch.setProjectionMatrix(batch.getProjectionMatrix().idt()); // Reset to draw full screen? 
        // Wait, if we use batch.getProjectionMatrix().idt(), we are drawing in NDC (-1 to 1).
        // Easier to just use the viewport camera but draw the fboRegion covering the view.
        // Actually, FBO is screen size. It matches the viewport.
        
        batch.setProjectionMatrix(viewport.getCamera().combined); // Use camera again?
        // NO, FBO is screen-space. If camera moves, FBO stays fixed to screen? 
        // Ideally we rendered lights in World Space. So the FBO image captures "World Lights".
        // But the FBO is constrained by viewport.
        
        // If we rendered lights using `viewport.getCamera().combined`, the FBO contains the lights as seen by camera.
        // So we should draw the FBO texture at the Camera's position?
        // Or simpler: Draw the FBO texture filling the current camera view.
        
        batch.begin();
        float x = viewport.getCamera().position.x - viewport.getWorldWidth()/2;
        float y = viewport.getCamera().position.y - viewport.getWorldHeight()/2;
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();
        
        // Actually FBO region might be flipped. I handled flip in resize.
        batch.draw(fboRegion, x, y, w, h);
        batch.end();
        
        // Reset Blending to Default
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void dispose() {
        if (fbo != null) fbo.dispose();
        if (lightTexture != null) lightTexture.dispose();
    }
    
    public void setAmbientColor(float r, float g, float b, float a) {
        this.ambientColor.set(r, g, b, a);
    }
}
