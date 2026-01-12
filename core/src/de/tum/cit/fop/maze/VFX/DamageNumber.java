package de.tum.cit.fop.maze.VFX;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;

public class DamageNumber {

    private de.tum.cit.fop.maze.GameObj.MovableObject target;


    private float offsetY;
    private float offsetX;

    private String text;
    private Color color; // Added Color field

    private float stateTime;
    private float lifeTime;
    private boolean isFinished;


    private static final float PHASE_1_DURATION = 0.2f; // Pop Up
    private static final float PHASE_2_DURATION = 0.4f; // Float
    private static final float PHASE_3_DURATION = 0.2f; // Fade


    public DamageNumber(de.tum.cit.fop.maze.GameObj.MovableObject target, String text, Color color) {
        this.target = target;
        this.text = text;
        this.color = color;

        this.stateTime = 0f;
        this.lifeTime = PHASE_1_DURATION + PHASE_2_DURATION + PHASE_3_DURATION;
        this.isFinished = false;


        this.offsetX = com.badlogic.gdx.math.MathUtils.random(-3f, 1f);
        this.offsetY = 0f;
    }


    public DamageNumber(de.tum.cit.fop.maze.GameObj.MovableObject target, int value) {
        this(target, "-" + value, Color.RED);
    }

    public void update(float delta) {
        stateTime += delta;
        if (stateTime >= lifeTime) {
            isFinished = true;
            return;
        }


        float startHeight = target.getHeight() / 2;
        float peakHeight = target.getHeight() + 4;

        if (stateTime < PHASE_1_DURATION) {

            float alpha = stateTime / PHASE_1_DURATION;
            this.offsetY = Interpolation.pow2Out.apply(startHeight, peakHeight, alpha);
        } else if (stateTime < PHASE_1_DURATION + PHASE_2_DURATION) {

        } else {

            float alpha = (stateTime - PHASE_1_DURATION - PHASE_2_DURATION) / PHASE_3_DURATION;
            this.offsetY = peakHeight + 5 + Interpolation.linear.apply(0, 10, alpha);
        }
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        if (isFinished || target == null) return;

        float scale = 0.3f;
        float alpha = 1.0f;


        if (stateTime < PHASE_1_DURATION) {
            float progress = stateTime / PHASE_1_DURATION;
            scale = Interpolation.swingOut.apply(0.0f, 0.3f, progress);
        } else if (stateTime > lifeTime - PHASE_3_DURATION) {

            float progress = (stateTime - (lifeTime - PHASE_3_DURATION)) / PHASE_3_DURATION;
            alpha = 1.0f - progress;
            scale += progress * 0.05f;
        }

        if (scale < 0.01f) scale = 0.01f;

        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;

        try {
            font.getData().setScale(scale);

            font.setColor(color.r, color.g, color.b, alpha);


            float drawX = target.getPosition().x + target.getWidth()/2 + offsetX;
            float drawY = target.getPosition().y + offsetY;


            float textWidth = text.length() * 3.5f;

            font.draw(batch, text, drawX - textWidth/2, drawY);
        } catch (Exception e) {

        } finally {

            font.setColor(Color.WHITE);
            font.getData().setScale(oldScaleX, oldScaleY);
        }
    }

    public boolean isFinished() {
        return isFinished;
    }
}