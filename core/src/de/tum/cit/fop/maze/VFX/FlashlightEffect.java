package de.tum.cit.fop.maze.VFX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Disposable;

/**
 * Handles the flashlight effect for Level 4.
 * Includes a FrameBuffer for the darkness layer and a procedural spotlight.
 */
public class FlashlightEffect implements Disposable {

    private FrameBuffer lightBuffer;
    private TextureRegion spotLightTex;
    private Vector2 currentLightTarget;
    private boolean isEnabled;

    public FlashlightEffect() {
        currentLightTarget = new Vector2();
        isEnabled = false;
        init();
    }

    private void init() {
        try {
            lightBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
                    false);

            // Create Soft Spotlight Texture
            int size = 128;
            Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.CLEAR);
            pixmap.fill();

            float centerX = size / 2f;
            float centerY = size / 2f;
            float radius = size / 2f;

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    float dx = x - centerX;
                    float dy = y - centerY;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);

                    if (dist < radius) {
                        float alpha = 1.0f - (dist / radius);
                        // Quadratic falloff for softer look
                        alpha = alpha * alpha;
                        pixmap.setColor(1f, 1f, 1f, alpha);
                        pixmap.drawPixel(x, y);
                    }
                }
            }

            Texture tex = new Texture(pixmap);
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            spotLightTex = new TextureRegion(tex);
            pixmap.dispose();

        } catch (Exception e) {
            Gdx.app.error("FlashlightEffect", "Failed to init lights", e);
            lightBuffer = null;
        }
    }

    /**
     * Renders the flashlight effect.
     * 
     * @param delta         Time delta.
     * @param camera        The camera.
     * @param viewport      The viewport.
     * @param batch         The SpriteBatch.
     * @param shapeRenderer The ShapeRenderer.
     * @param sourcePos     Light source position.
     * @param rawTargetPos  Light target position.
     */
    public void render(float delta, OrthographicCamera camera, Viewport viewport,
            SpriteBatch batch, ShapeRenderer shapeRenderer,
            Vector2 sourcePos, Vector2 rawTargetPos) {

        if (lightBuffer == null)
            return;

        // 1. Update Light Target (Lag Effect)
        if (rawTargetPos != null) {
            // Lerp factor: 5f * delta gives smooth lag
            currentLightTarget.lerp(rawTargetPos, 5f * delta);
        } else {
            // If no target, stay at source or last pos?
        }

        // 2. Draw Light Map to FBO
        lightBuffer.begin();

        // Clear to AMBIENT darkness (Black)
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (isEnabled && sourcePos != null) {
            // A. Draw Beam (Triangle)
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            // From Source (Nono)
            Vector2 src = sourcePos;
            // To Lagged Target (Player)
            Vector2 target = currentLightTarget;

            float angle = MathUtils.atan2(target.y - src.y, target.x - src.x);
            // Length should reach target roughly + some spot radius overlap
            float distToTarget = src.dst(target);
            float length = distToTarget;

            float coneHalfWidth = 35f * MathUtils.degreesToRadians;

            float x1 = src.x;
            float y1 = src.y;

            // Beam ends at target
            float x2 = src.x + MathUtils.cos(angle - coneHalfWidth) * length;
            float y2 = src.y + MathUtils.sin(angle - coneHalfWidth) * length;

            float x3 = src.x + MathUtils.cos(angle + coneHalfWidth) * length;
            float y3 = src.y + MathUtils.sin(angle + coneHalfWidth) * length;

            // Colors: Tip White(0.6), End White(0.1) - beam is fainter than spot
            shapeRenderer.triangle(
                    x1, y1,
                    x2, y2,
                    x3, y3,
                    new Color(1f, 1f, 1f, 0.8f),
                    new Color(1f, 1f, 1f, 0.2f),
                    new Color(1f, 1f, 1f, 0.2f));

            shapeRenderer.end();

            // B. Draw Spot (Circle Texture)
            if (spotLightTex != null) {
                batch.setProjectionMatrix(camera.combined);
                batch.begin();

                float spotSize = 300f;
                batch.draw(spotLightTex,
                        currentLightTarget.x - spotSize / 2,
                        currentLightTarget.y - spotSize / 2,
                        spotSize, spotSize);

                batch.end();
            }
        }

        lightBuffer.end();

        // 3. Blend FBO over Scene (Multiply)
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO); // Multiply

        // Draw texture covering the viewport
        float w = viewport.getWorldWidth() * camera.zoom;
        float h = viewport.getWorldHeight() * camera.zoom;

        // Note: FBO texture is upside down, so flip height
        batch.draw(lightBuffer.getColorBufferTexture(),
                camera.position.x - w / 2,
                camera.position.y + h / 2,
                w, -h);

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); // Reset
        batch.end();
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void reset() {
        isEnabled = false;
        currentLightTarget.setZero();
    }

    @Override
    public void dispose() {
        if (spotLightTex != null && spotLightTex.getTexture() != null)
            spotLightTex.getTexture().dispose();
        if (lightBuffer != null)
            lightBuffer.dispose();
    }
}
