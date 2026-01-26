package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Nono is a companion character that follows the player.
 */
public class Nono extends GameObject {

    private Character target;
    private float stateTime;
    private Animation<TextureRegion> idleDown, idleLeft, idleRight, idleUp;
    private Animation<TextureRegion> blinkDown, blinkLeft, blinkRight, blinkUp;
    private Animation<TextureRegion> currentAnim;
    private int currentDirection = 0;
    private float hoverOffset = 0f;
    private Vector2 velocity = new Vector2();

    private float blinkTimer = 0f;
    private float nextBlinkTime;
    private boolean isBlinking = false;
    private float blinkStateTime = 0f;

    private static final float LEASH_RADIUS = 30f;
    private static final float MAX_SPEED = 200f;
    private static final float FRICTION = 0.9f;

    public Nono(float x, float y, Character target) {
        super(x, y, 12, 12, null);
        this.target = target;
        this.nextBlinkTime = MathUtils.random(3f, 5f);
        loadAnimation();
    }

    private void loadAnimation() {
        Texture texture = new Texture(Gdx.files.internal("player/sprite/nono.png"));
        TextureRegion[][] tmp = TextureRegion.split(texture, 32, 32);

        float blinkDuration = 0.1f;


        idleDown = new Animation<>(1f, tmp[0][0]);

        blinkDown = new Animation<>(blinkDuration, tmp[0][1], tmp[0][2]);
        blinkDown.setPlayMode(Animation.PlayMode.NORMAL);


        idleLeft = new Animation<>(1f, tmp[1][0]);
        blinkLeft = new Animation<>(blinkDuration, tmp[1][1], tmp[1][2]);
        blinkLeft.setPlayMode(Animation.PlayMode.NORMAL);

        idleRight = new Animation<>(1f, tmp[2][0]);
        blinkRight = new Animation<>(blinkDuration, tmp[2][1], tmp[2][2]);
        blinkRight.setPlayMode(Animation.PlayMode.NORMAL);


        idleUp = new Animation<>(1f, tmp[3][0]);
        blinkUp = new Animation<>(blinkDuration, tmp[3][1], tmp[3][2]);
        blinkUp.setPlayMode(Animation.PlayMode.NORMAL);

        currentAnim = idleDown;
        this.textureRegion = tmp[0][0];
    }

    private Vector2 objectivePosition = null;
    private java.util.List<GameObject> mapObjects;

    public void setMapObjects(java.util.List<GameObject> mapObjects) {
        this.mapObjects = mapObjects;
    }

    public void update(float delta) {
        stateTime += delta;

        hoverOffset = MathUtils.sin(stateTime * 5f) * 1.5f;

        if (target != null) {
            updateObjective();

            float targetCenterX = target.getPosition().x + target.getWidth() / 2;
            float targetCenterY = target.getPosition().y + target.getHeight() / 2;
            float myCenterX = position.x + width / 2;
            float myCenterY = position.y + height / 2;

            float distToPlayer = Vector2.dst(myCenterX, myCenterY, targetCenterX, targetCenterY);

            Vector2 desiredPosition = new Vector2(targetCenterX, targetCenterY);

            if (objectivePosition != null) {

                float objX = objectivePosition.x;
                float objY = objectivePosition.y;


                float dirX = objX - targetCenterX;
                float dirY = objY - targetCenterY;
                float distToObj = (float) Math.sqrt(dirX * dirX + dirY * dirY);

                if (distToObj > 0.1f) {

                    dirX /= distToObj;
                    dirY /= distToObj;


                    float maxDist = Math.min(LEASH_RADIUS, distToObj);
                    desiredPosition.x = targetCenterX + dirX * maxDist * 0.8f;
                    desiredPosition.y = targetCenterY + dirY * maxDist * 0.8f;
                }
            }


            float pullX = desiredPosition.x - myCenterX;
            float pullY = desiredPosition.y - myCenterY;
            float distToDesired = (float) Math.sqrt(pullX * pullX + pullY * pullY);


            if (distToDesired > 2f) {
                float force = distToDesired * 30f;
                float angle = MathUtils.atan2(pullY, pullX);
                velocity.x += MathUtils.cos(angle) * force * delta;
                velocity.y += MathUtils.sin(angle) * force * delta;
            }


            velocity.scl(FRICTION);

            if (velocity.len() > MAX_SPEED) {
                velocity.setLength(MAX_SPEED);
            }

            position.x += velocity.x * delta;
            position.y += velocity.y * delta;

            if (velocity.len() > 10f) {
                if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
                    if (velocity.x > 0)
                        currentDirection = 2;
                    else
                        currentDirection = 1;
                } else {
                    if (velocity.y > 0)
                        currentDirection = 3;
                    else
                        currentDirection = 0;
                }
            }

        }


        if (!isBlinking) {
            blinkTimer += delta;
            if (blinkTimer >= nextBlinkTime) {
                isBlinking = true;
                blinkStateTime = 0f;
                blinkTimer = 0f;
                nextBlinkTime = MathUtils.random(3f, 5f);
            }
        } else {
            blinkStateTime += delta;

            Animation<TextureRegion> checkAnim = null;
            switch (currentDirection) {
                case 0:
                    checkAnim = blinkDown;
                    break;
                case 1:
                    checkAnim = blinkLeft;
                    break;
                case 2:
                    checkAnim = blinkRight;
                    break;
                case 3:
                    checkAnim = blinkUp;
                    break;
            }
            if (checkAnim != null && checkAnim.isAnimationFinished(blinkStateTime)) {
                isBlinking = false;
            }
        }
        if (isBlinking) {
            switch (currentDirection) {
                case 0:
                    currentAnim = blinkDown;
                    break;
                case 1:
                    currentAnim = blinkLeft;
                    break;
                case 2:
                    currentAnim = blinkRight;
                    break;
                case 3:
                    currentAnim = blinkUp;
                    break;
            }
            this.textureRegion = currentAnim.getKeyFrame(blinkStateTime, false);
        } else {
            switch (currentDirection) {
                case 0:
                    currentAnim = idleDown;
                    break;
                case 1:
                    currentAnim = idleLeft;
                    break;
                case 2:
                    currentAnim = idleRight;
                    break;
                case 3:
                    currentAnim = idleUp;
                    break;
            }
            this.textureRegion = currentAnim.getKeyFrame(stateTime, true);
        }
    }

    private void updateObjective() {
        objectivePosition = null;
        if (mapObjects == null || target == null)
            return;

        float minDst = Float.MAX_VALUE;
        boolean hasKey = target.hasKey();


        for (GameObject obj : mapObjects) {
            if (obj instanceof MaskItem) {
                float dst = Vector2.dst2(target.getPosition().x, target.getPosition().y,
                        obj.getPosition().x, obj.getPosition().y);
                if (dst < minDst) {
                    minDst = dst;
                    objectivePosition = new Vector2(obj.getPosition().x + obj.getWidth() / 2,
                            obj.getPosition().y + obj.getHeight() / 2);
                }
            }
        }

        if (objectivePosition == null && !hasKey) {
            for (GameObject obj : mapObjects) {
                if (obj instanceof Key) {
                    float dst = Vector2.dst2(target.getPosition().x, target.getPosition().y,
                            obj.getPosition().x, obj.getPosition().y);
                    if (dst < minDst) {
                        minDst = dst;
                        objectivePosition = new Vector2(obj.getPosition().x + obj.getWidth() / 2,
                                obj.getPosition().y + obj.getHeight() / 2);
                    }
                }
            }
        }

        if (objectivePosition == null && !hasKey) {
            for (GameObject obj : mapObjects) {
                if (obj instanceof AttackUnlockItem) {
                    float dst = Vector2.dst2(target.getPosition().x, target.getPosition().y,
                            obj.getPosition().x, obj.getPosition().y);
                    if (dst < minDst) {
                        minDst = dst;
                        objectivePosition = new Vector2(obj.getPosition().x + obj.getWidth() / 2,
                                obj.getPosition().y + obj.getHeight() / 2);
                    }
                }
            }
        }

        if (objectivePosition == null || hasKey) {
            for (GameObject obj : mapObjects) {
                if (obj instanceof Exit) {
                    float dst = Vector2.dst2(target.getPosition().x, target.getPosition().y,
                            obj.getPosition().x, obj.getPosition().y);
                    if (dst < minDst) {
                        minDst = dst;
                        objectivePosition = new Vector2(obj.getPosition().x + obj.getWidth() / 2,
                                obj.getPosition().y + obj.getHeight() / 2);
                    }
                }
            }
        }
    }

    public void draw(SpriteBatch batch) {
        if (textureRegion != null) {
            float drawY = position.y + hoverOffset;
            batch.draw(textureRegion, position.x, drawY, width, height);
        }
    }
}
