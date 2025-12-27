package de.tum.cit.fop.maze.GameObj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.AI.Grid;
import de.tum.cit.fop.maze.AI.PathFinder;

import java.util.List;

public class Enemy extends MovableObject {

    protected enum State {
        PATROL, CHASE, RETREAT, CONFUSED 
    }

    protected State currentState;
    private float speed = 20f;
    // private int health = 100; // REMOVED: Shadowing MovableObject.health
    
    // AI
    private Grid grid;
    protected Character target;
    private List<Vector2> currentPath;
    private int pathIndex = 0;
    private float pathTimer = 0;
    private static final float PATH_UPDATE_INTERVAL = 0.5f;
    private float detectionRange = 64f; // 4 Tiles (16 * 4)
    
    private float confusedTimer = 0f;
    
    // Animation
    protected Animation<TextureRegion> walkDown;
    protected Animation<TextureRegion> walkLeft;
    protected Animation<TextureRegion> walkRight;
    protected Animation<TextureRegion> walkUp;
    
    protected float stateTime; // Renamed from animTime to stateTime (check if field existed)
    // Wait, stateTime field existed in line 38. Use that.

    private float waitTimer = 0f;

    public Enemy(float x, float y, Animation<TextureRegion>[] animations, Grid grid, Character target) {
        // Init with first frame of Down animation
        super(x, y, 16, 16, animations[0].getKeyFrame(0)); 
        
        this.walkDown = animations[0];
        this.walkLeft = animations[1];
        this.walkRight = animations[2];
        this.walkUp = animations[3];
        
        this.grid = grid;
        this.target = target;
        this.currentState = State.PATROL;
        
        // Match Character Hitbox: 8x8 centered
        this.bounds = new Rectangle(x+4, y+4, 8, 8);
        
        // Physics Setup
        this.maxSpeed = speed; 
        this.acceleration = 50f; 
        this.friction = 50f;   
    }
    
    public void update(float delta) {
        stateTime += delta;
        
        // Reset input vector every frame (AI drives input)
        inputVector.set(0, 0);

        // Adjust Speed based on State
        if (currentState == State.CHASE || currentState == State.RETREAT) {
            this.maxSpeed = speed * 4f; 
            this.acceleration = 500f;
            this.friction = 500f;
        } else {
            this.maxSpeed = speed;
            this.acceleration = 50f;
            this.friction = 50f;
        }

        switch (currentState) {
            case PATROL:
                updatePatrol(delta);
                break;
            case CHASE:
                updateChase(delta);
                break;
            case RETREAT:
                updateRetreat(delta);
                break;
            case CONFUSED:
                updateConfused(delta);
                break;
        }
        
        // Update Combat Input (Must be before Physics!)
        updateCombat(delta);

        // Follow Path with Input Vector logic
        if (currentPath != null && pathIndex < currentPath.size() && currentState != State.CONFUSED) {
            Vector2 targetNode = currentPath.get(pathIndex);
            
            // Move "Center" to "Target Node"
            float targetX = targetNode.x - width/2;
            float targetY = targetNode.y - height/2;
            
            float dist = Vector2.dst(position.x, position.y, targetX, targetY);
            
            // Increased tolerance to 5f to prevent being stuck near walls
            if (dist < 5f) {
                pathIndex++;
            } else {
                // Set Input Vector towards target (Normalized)
                // If combat is active (overlapping), combat input takes priority? 
                // Or blend them? For now, if overlapping, combat logic returns early so path logic skipped?
                // Wait, updateCombat doesn't return from the whole method, just itself.
                // But updateCombat sets inputVector. If we overwrite it here, jitter is lost.
                // So only follow path if NOT in close combat (jittering).
                
                // Let's rely on updateCombat setting inputVector. If it did, inputVector is non-zero.
                if (inputVector.len2() == 0) { // Only use path if no combat input
                    inputVector.set(targetX, targetY).sub(position.x, position.y).nor();
                }
            }
        }
        
        // Apply Physics (Acceleration/Friction/Velocity)
        updatePhysics(delta);
        
        // Apply Movement & Collision
        if (velocity.len() > 1f) {
            Vector2 dir = velocity.cpy().nor(); // Used for sliding
            
            // Move X
            float oldX = position.x;
            position.x += velocity.x * delta;
            updateBounds();
            GameObject colX = checkCollision();
            if (colX != null) {
                position.x = oldX; // Revert
                updateBounds();
            }

            // Move Y
            float oldY = position.y;
            position.y += velocity.y * delta;
            updateBounds();
            GameObject colY = checkCollision();
            if (colY != null) {
                position.y = oldY; // Revert
                updateBounds();
            }
            
            handleWallSliding(delta, inputVector.len() > 0 ? inputVector : dir, colX, colY);
            
            // Animation Selection
            Animation<TextureRegion> currentAnim = walkDown;
            if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
                if (velocity.x > 0) currentAnim = walkRight;
                else currentAnim = walkLeft;
            } else {
                if (velocity.y > 0) currentAnim = walkUp;
                else currentAnim = walkDown;
            }
            this.textureRegion = currentAnim.getKeyFrame(stateTime, true);
            
        } else {
            // Idle
            this.textureRegion = walkDown.getKeyFrame(0.2f, true);
        }
        
        updateBounds();
        // updateCombat(delta); // MOVED UP
    }
    
    // Helper to get Center
    protected Vector2 getCenter() {
        return new Vector2(position.x + width/2, position.y + height/2);
    }
    
    // Fix: Target the actual HITBOX center (Feet), not the Sprite center (Chest)
    // Character Hitbox is 8x8, at x+4, y+4.
    protected Vector2 getTargetCenter() {
        Rectangle tBounds = target.getBounds();
        return new Vector2(tBounds.x + tBounds.width/2, tBounds.y + tBounds.height/2);
    }

    protected void updateCombat(float delta) {
        
        // Attack/Combat Logic
        if (bounds.overlaps(target.getBounds())) {
             // Play damage sound if file exists
             FileHandle damageSoundFile = Gdx.files.internal("assets/damage.mp3");
             if (damageSoundFile.exists()) {
                 Gdx.audio.newSound(damageSoundFile).play();
             }

             Rectangle rect = getBounds();
             if (rect.overlaps(target.getBounds())) { // Changed character.getBounds() to target.getBounds()
                 if (target.isShielded()) { // Changed character.isShielded() to target.isShielded()
                     // Shield Effect: Enemy takes damage, Player is safe
                     this.takeDamage(20);
                     // Pushback enemy slightly to avoid instant multi-hit?
                     // Simple pushback: reverse velocity?
                     // For now, takeDamage logic usually handles death or stats.
                     // We should invoke damage number on enemy?
                     // Enemy doesn't have HP system yet? "takeDamage" method exists?
                     // Enemy.java DOES NOT have takeDamage() currently! I need to check Enemy.java first!
                 } else {
                     target.takeDamage(); // Changed character.takeDamage() to target.takeDamage()
                 }
             }

             if (health <= 40) {
                 currentState = State.RETREAT;
                 return;
             }
             
             // "Combat Jitter": Move randomly around the player to simulate fighting
             // Don't stop moving just because we reached the target!
             float jitterX = MathUtils.random(-4f, 4f);
             float jitterY = MathUtils.random(-4f, 4f);
             
             Vector2 combatTarget = getTargetCenter().add(jitterX, jitterY);
             
             // Set Input Vector for jitter
             inputVector.set(combatTarget).sub(getCenter()).nor();
             
             return; // Skip normal pathfinding while fighting
        }
    }
    
    private void updatePatrol(float delta) {
        float distToPlayer = Vector2.dst(getCenter().x, getCenter().y, getTargetCenter().x, getTargetCenter().y);
        
        // 1. Check Range (3 Tiles = 48px)
        if (target != null && distToPlayer < detectionRange) {
            if (hasLineOfSight(getCenter(), getTargetCenter())) {
                currentState = State.CHASE;
                pathTimer = PATH_UPDATE_INTERVAL; 
                return;
            }
        }
        
        // Patrol Logic
        if (currentPath == null || pathIndex >= currentPath.size()) {
            waitTimer -= delta;
            
            if (waitTimer <= 0) {
                 pickRandomPatrolPoint();
                 waitTimer = MathUtils.random(0f, 2f);
            }
        }
    }
    
    private void updateChase(float delta) {
        pathTimer += delta;
        if (pathTimer > PATH_UPDATE_INTERVAL) {
            pathTimer = 0;
            currentPath = PathFinder.findPath(grid, getCenter(), getTargetCenter());
            pathIndex = 0;
        }
        
        float distToPlayer = Vector2.dst(getCenter().x, getCenter().y, getTargetCenter().x, getTargetCenter().y);
        
        if (health <= 40) {
            currentState = State.RETREAT;
            return;
        }
        
        if (distToPlayer > detectionRange * 1.5f || !hasLineOfSight(getCenter(), getTargetCenter())) { 
            if (distToPlayer > detectionRange * 2f) {
                currentState = State.CONFUSED;
                confusedTimer = 3.0f;
                currentPath = null; 
            }
        }
    }
    
    private void updateRetreat(float delta) {
        pathTimer += delta;
        
        // Don't repath too often to avoid "jittery" movement where they constantly change mind
        // Only repath if timer expired OR path is finished
        boolean pathFinished = currentPath == null || pathIndex >= currentPath.size();
        
        if (pathTimer > 1.5f || pathFinished) { // Increased interval to 1.5s
            pathTimer = 0;
            
            Vector2 center = getCenter();
            Vector2 playerCenter = getTargetCenter();
            Vector2 dirToPlayer = new Vector2(playerCenter).sub(center).nor();
            Vector2 fleeDir = dirToPlayer.scl(-1); 
            
            // Try explicit flee target first
            Vector2 fleeTarget = new Vector2(center).mulAdd(fleeDir, 64f); // 4 tiles away
            
            if (isWalkable(fleeTarget)) {
                 currentPath = PathFinder.findPath(grid, center, fleeTarget);
            } else {
                 currentPath = findRetreatPathFallback();
            }
            pathIndex = 0;
        }
        
        float distToPlayer = Vector2.dst(getCenter().x, getCenter().y, getTargetCenter().x, getTargetCenter().y);
        
        // Only stop retreating if VERY far away (2x range)
        if (distToPlayer > detectionRange * 2f) {
            currentState = State.CONFUSED;
            confusedTimer = 3.0f;
        }
    }
    
    // Helper to check walkability of a vector position
    private boolean isWalkable(Vector2 pos) {
        return grid.isWalkable((int)(pos.x / 16), (int)(pos.y / 16));
    }
    
    private List<Vector2> findRetreatPathFallback() {
        int cx = (int)(getCenter().x / 16);
        int cy = (int)(getCenter().y / 16);
        float currentDist = Vector2.dst2(getCenter().x, getCenter().y, getTargetCenter().x, getTargetCenter().y);
        
        // Reduced iterations to 8 to improve performance
        for (int i = 0; i < 8; i++) {
             int tx = cx + MathUtils.random(-6, 6); // Slightly larger range
             int ty = cy + MathUtils.random(-6, 6);
             
             if (tx >= 0 && ty >= 0 && tx < grid.getWidth() && ty < grid.getHeight() && grid.isWalkable(tx, ty)) {
                 Vector2 targetPos = new Vector2(tx*16+8, ty*16+8);
                 float newDist = Vector2.dst2(targetPos.x, targetPos.y, getTargetCenter().x, getTargetCenter().y);
                 
                 // Only move if it increases distance (or at least maintains it significantly)
                 if (newDist > currentDist + 256) { // 16*16 = 256
                     List<Vector2> path = PathFinder.findPath(grid, getCenter(), targetPos);
                     if (path != null) return path;
                 }
             }
        }
        return null; // Keep moving if current path valid, or just stop briefly
    }
    
    private void updateConfused(float delta) {
         confusedTimer -= delta;
         
         // If player gets close while confused, WAKE UP!
         float distToPlayer = Vector2.dst(getCenter().x, getCenter().y, getTargetCenter().x, getTargetCenter().y);
         if (distToPlayer < detectionRange) {
             if (health <= 40) {
                 currentState = State.RETREAT;
             } else {
                 currentState = State.CHASE;
             }
             return;
         }

         if (confusedTimer <= 0) {
             currentState = State.PATROL;
         }
    }
    
    private void pickRandomPatrolPoint() {
        int cx = (int)(getCenter().x / 16);
        int cy = (int)(getCenter().y / 16);
        
        // Random point within 3 tiles
        for (int i = 0; i < 10; i++) {
            int tx = cx + MathUtils.random(-3, 3);
            int ty = cy + MathUtils.random(-3, 3);
            
            if (grid.isWalkable(tx, ty)) {
                Vector2 targetPos = new Vector2(tx*16+8, ty*16+8);
                
                if (!hasLineOfSight(getCenter(), targetPos)) continue;
                
                currentPath = PathFinder.findPath(grid, getCenter(), targetPos);
                pathIndex = 0;
                if (currentPath != null) break;
            }
        }
    }
    
    // Bresenham's Line Algorithm
    private boolean hasLineOfSight(Vector2 start, Vector2 end) {
        int x0 = (int)(start.x / 16);
        int y0 = (int)(start.y / 16);
        int x1 = (int)(end.x / 16);
        int y1 = (int)(end.y / 16);
        
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        
        while (true) {
            if (!grid.isWalkable(x0, y0)) return false; // Hit wall
            
            if (x0 == x1 && y0 == y1) break;
            
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
        return true;
    }
    
    private GameObject checkCollision() {
        // Bounding box check against grid tiles
        // We expand the check range slightly to ensure we catch neighboring walls
        int minX = (int)(bounds.x / 16);
        int maxX = (int)((bounds.x + bounds.width) / 16);
        int minY = (int)(bounds.y / 16);
        int maxY = (int)((bounds.y + bounds.height) / 16);
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (!grid.isWalkable(x, y)) {
                     // Create a dummy wall object for collision check
                     // Adjust bounds to match wall (16x16)
                     Rectangle tileBounds = new Rectangle(x*16, y*16, 16, 16);
                     if (bounds.overlaps(tileBounds)) {
                         return new de.tum.cit.fop.maze.GameObj.Wall(x*16, y*16, 16, 16, null); 
                     }
                }
            }
        }
        return null;
    }
    
    private void handleWallSliding(float delta, Vector2 dir, GameObject colX, GameObject colY) {
        float SLIDE_THRESHOLD = 8.0f;
        float slideSpeed = speed * 1.5f;

        // X Collision, try slide Y
        if (colX != null && Math.abs(dir.x) > 0 && Math.abs(dir.y) < 0.5f) { 
            Rectangle wallBounds = colX.getBounds();
            float overlapY = Math.min(bounds.y + bounds.height, wallBounds.y + wallBounds.height) - Math.max(bounds.y, wallBounds.y);
            
            if (overlapY > 0 && overlapY <= SLIDE_THRESHOLD) {
                float centerY = bounds.y + bounds.height/2;
                float wallCenterY = wallBounds.y + wallBounds.height/2;
                if (centerY < wallCenterY) position.y -= slideSpeed * delta;
                else position.y += slideSpeed * delta;
            }
        }
        
        // Y Collision, try slide X
        if (colY != null && Math.abs(dir.y) > 0 && Math.abs(dir.x) < 0.5f) {
            Rectangle wallBounds = colY.getBounds();
            float overlapX = Math.min(bounds.x + bounds.width, wallBounds.x + wallBounds.width) - Math.max(bounds.x, wallBounds.x);
            
            if (overlapX > 0 && overlapX <= SLIDE_THRESHOLD) {
                float centerX = bounds.x + bounds.width/2;
                float wallCenterX = wallBounds.x + wallBounds.width/2;
                if (centerX < wallCenterX) position.x -= slideSpeed * delta;
                else position.x += slideSpeed * delta;
            }
        }
    }
    
    private void updateBounds() {
        // Center the 8x8 bounds relative to the 16x16 sprite
        // Sprite Position (position.x, position.y) is bottom-left
        // Bounds should be at x+4, y+4
        this.bounds.setPosition(position.x+4, position.y+4);
    }
    
    public void drawDebug(ShapeRenderer sr) {
        sr.setColor(Color.YELLOW);
        // Draw Collision Box
        sr.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        
        // Draw Center Point (Green)
        sr.setColor(Color.GREEN);
        sr.circle(getCenter().x, getCenter().y, 2);
        
        // Draw Path and Target (Yellow)
        sr.setColor(Color.YELLOW);
        if (currentPath != null) {
            for (int i = 0; i < currentPath.size() - 1; i++) {
                Vector2 p1 = currentPath.get(i);
                Vector2 p2 = currentPath.get(i+1);
                sr.line(p1, p2);
            }
            
            // Draw Target Point
            if (!currentPath.isEmpty()) {
                Vector2 target = currentPath.get(currentPath.size() - 1);
                sr.circle(target.x, target.y, 4);
            }
        }
    }

    public void draw(SpriteBatch batch) {
        setupDamageFlash(batch);
        batch.draw(getTextureRegion(), getPosition().x, getPosition().y, 16, 16);
        endDamageFlash(batch);
    }

    public void drawStatus(SpriteBatch batch, com.badlogic.gdx.graphics.g2d.BitmapFont font, boolean showHP) {
        String statusText = null;
        Color color = Color.WHITE;

        switch (currentState) {
            case CHASE:
                statusText = "!";
                color = Color.RED;
                break;
            case CONFUSED:
                statusText = "?";
                color = Color.YELLOW;
                break;
            default:
                break;
        }
        
        // If Debug HP is on
        if (showHP) {
            statusText = (statusText != null ? statusText + " " : "") + health;
            color = Color.CYAN; // Distinct color for debug
        }

        if (statusText != null) {
            float oldScaleX = font.getData().scaleX;
            float oldScaleY = font.getData().scaleY;
            
            font.getData().setScale(0.3f); // Use smaller scale
            font.setColor(color);
            
            // Draw slightly above the enemy
            float drawX = position.x + width / 2 -2; // Center roughly
            float drawY = position.y + height + 7; 
            
            font.draw(batch, statusText, drawX, drawY);
            
            font.setColor(Color.WHITE); // Reset color
            font.getData().setScale(oldScaleX, oldScaleY); // Reset scale
        }
    }

}

