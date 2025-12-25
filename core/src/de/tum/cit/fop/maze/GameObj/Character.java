package de.tum.cit.fop.maze.GameObj;



import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import java.util.List;

public class Character extends GameObject {
        private int lives;
        private float speed;
        private static final float WALK_SPEED = 100f;
        private static final float RUN_SPEED = 200f;
        private float stateTime;

        private Animation<TextureRegion> walkDown;
        private Animation<TextureRegion> walkRight;
        private Animation<TextureRegion> walkUp;
        private Animation<TextureRegion> walkLeft;

        private Direction currentDirection;
        private boolean isMoving;

        // Damage VFX
        private float damageFlashTime = 0f;
        private static final float DAMAGE_DURATION = 1.0f;

        public enum Direction {
            DOWN, RIGHT, UP, LEFT
        }

        public Character(float x, float y) {
            super(x, y, 16, 32, null); // width 16, height 32
            this.lives = 3; // Default lives
            this.speed = WALK_SPEED;
            this.currentDirection = Direction.DOWN;
            this.stateTime = 0f;

            loadAnimations();

            // precise collision bounds (smaller than texture to allow overlap with head/feet properly)
            // Adjusting bounds to be the bottom part of the character for better perspective
            this.bounds = new Rectangle(x + 4, y, 8, 12);
        }

        private void loadAnimations() {
            Texture texture = new Texture(Gdx.files.internal("character.png"));
            TextureRegion[][] tmp = TextureRegion.split(texture, 16, 32);

            // Row 0: Down
            walkDown = new Animation<>(0.1f, tmp[0]);
            // Row 1: Right
            walkRight = new Animation<>(0.1f, tmp[1]);
            // Row 2: Up
            walkUp = new Animation<>(0.1f, tmp[2]);
            // Row 3: Left
            walkLeft = new Animation<>(0.1f, tmp[3]);

            // Set initial texture
            this.textureRegion = tmp[0][0];
        }

        public void update(float delta, List<GameObject> mapObjects) {
            stateTime += delta;
            handleInput();

            if (isMoving) {
                float moveAmount = speed * delta;
                float oldX = position.x;
                float oldY = position.y;

                switch (currentDirection) {
                    case DOWN: position.y -= moveAmount; break;
                    case RIGHT: position.x += moveAmount; break;
                    case UP: position.y += moveAmount; break;
                    case LEFT: position.x -= moveAmount; break;
                }

                updateBounds();

                if (checkCollision(mapObjects)) {
                    // Revert position if collided
                    position.set(oldX, oldY);
                    updateBounds();
                }
            }

            // Update texture based on animation
            Animation<TextureRegion> currentAnim;
            switch (currentDirection) {
                case DOWN: currentAnim = walkDown; break;
                case RIGHT: currentAnim = walkRight; break;
                case UP: currentAnim = walkUp; break;
                case LEFT: currentAnim = walkLeft; break;
                default: currentAnim = walkDown; break;
            }

            if (isMoving) {
                this.textureRegion = currentAnim.getKeyFrame(stateTime, true);
            } else {
                this.textureRegion = currentAnim.getKeyFrame(0, true); // Stand still frame
            }

            // Handle damage flash
            if (damageFlashTime > 0) {
                damageFlashTime -= delta;
            }
        }

        private void handleInput() {
            isMoving = false;

            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                speed = RUN_SPEED;
            } else {
                speed = WALK_SPEED;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                currentDirection = Direction.DOWN;
                isMoving = true;
            } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                currentDirection = Direction.UP;
                isMoving = true;
            } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                currentDirection = Direction.LEFT;
                isMoving = true;
            } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                currentDirection = Direction.RIGHT;
                isMoving = true;
            }
        }

        private void updateBounds() {
            this.bounds.setPosition(position.x + 4, position.y);
        }

        private boolean checkCollision(List<GameObject> mapObjects) {
            for (GameObject obj : mapObjects) {
                if (obj == this) continue;

                // Wall collision
                if (obj instanceof Wall) {
                    if (bounds.overlaps(obj.getBounds())) {
                        return true;
                    }
                }
            }
            return false;
        }

        public void takeDamage() {
            if (damageFlashTime <= 0) {
                lives--;
                damageFlashTime = DAMAGE_DURATION;
                // Play sound if possible
            }
        }

        public int getLives() {
            return lives;
        }

        public boolean isDead() {
            return lives <= 0;
        }

        public boolean isDamaged() {
            return damageFlashTime > 0;
        }
}

