# Maze Runner Game

A Java-based maze runner game built with LibGDX. Navigate through mazes, avoid enemies, collect keys, and find the exit!

## Project Structure

- **Root**: Contains the main game entry point `MazeRunnerGame` and various `Screen` implementations (Menu, Game, Story, etc.).
- **GameObj**: Defines all game entities.
    - `GameObject`: Base class for all entities.
    - `MovableObject`: Base for moving entities (Physics, Collision).
    - `Character`: The player character.
    - `Enemy` / `Ghost`: AI-controlled entities.
    - Collectable Items: `Key`, `Heart`, `ShieldItem`.
- **GameControl**: Manages game systems.
    - `HUD`
    - `GameConfig` / `ConfigManager`: Settings management.
    - `AchievementManager`: Tracks player achievements.
    - `GameSaveManager`: Handles save/load functionality.
- **AI**: Pathfinding and navigation.
    - `Grid`: Map representation for AI.
    - `PathFinder`: A* algorithm implementation.
- **Conversation**: Dialogue system.
    - `DialogueBox`: UI components.
    - `StoryDialogueScreen`: Screen for story sequences
- **Procedure**: Random level generation.
    - `DungeonGenerator`: Generates procedural layouts.
- **VFX**: Visual effects.
    - `LightManager`: Dynamic lighting system.
    - `DamageNumber`: Floating damage text.

## Class Hierarchy

```mermaid
classDiagram
    %% Core Game
    class MazeRunnerGame {
        +SpriteBatch spriteBatch
        +Skin skin
        +create()
        +goToGame(FileHandle map)
        +goToMenu()
        +goToStory()
        +goToSettings()
    }
    
    %% Screens
    class Screen {
        <<interface>>
        +show()
        +render(delta)
        +resize(width, height)
        +pause()
        +resume()
        +hide()
        +dispose()
    }
    
    MazeRunnerGame --> Screen : manages
    Screen <|.. GameScreen
    Screen <|.. MenuScreen
    Screen <|.. StoryScreen
    Screen <|.. LevelSelectionScreen
    Screen <|.. SettingsScreen
    Screen <|.. AchievementsScreen
    Screen <|.. SkillTreeScreen
    Screen <|.. SaveSlotScreen
    Screen <|.. StoryDialogueScreen
    Screen <|.. DialogueScreen
    Screen <|.. VFXDemoScreen

    %% Game Objects Hierarchy
    class GameObject {
        +Vector2 position
        +float width
        +float height
        +Rectangle bounds
        +TextureRegion textureRegion
        +draw(SpriteBatch)
        +getBounds()
    }
    
    class MovableObject {
        +float speed
        +Vector2 velocity
        +int health
        +int maxHealth
        +update(delta)
        +takeDamage(amount)
        +heal(amount)
    }
    
    class Character {
        +PlayerState state
        +handleInput()
        +update(delta)
        +hasKey()
    }
    
    class PlayerState {
        +int currentHealth
        +int score
        +addScore(points)
    }
    
    class Enemy {
        +float detectionRange
        +pathFind(target)
        +updateCombat()
    }
    
    class Ghost {
        +boolean passThroughWalls
    }
    
    %% Environment & Items
    class Wall
    class Path
    class Trap
    class EntryPoint
    class Exit
    class EnemySpawnPoint
    class GhostSpawnPoint
    
    class Collectable {
        <<interface>>
        +collect(Character)
    }
    
    class Key
    class Heart
    class ShieldItem

    %% Inheritance
    GameObject <|-- MovableObject
    GameObject <|-- Wall
    GameObject <|-- Path
    GameObject <|-- Trap
    GameObject <|-- EntryPoint
    GameObject <|-- Exit
    GameObject <|-- EnemySpawnPoint
    GameObject <|-- GhostSpawnPoint
    
    GameObject <|-- Key
    GameObject <|-- Heart
    GameObject <|-- ShieldItem
    
    MovableObject <|-- Character
    MovableObject <|-- Enemy
    Enemy <|-- Ghost
    
    %% Interfaces
    Collectable <|.. Key
    Collectable <|.. Heart
    Collectable <|.. ShieldItem
    
    %% Systems & Managers
    class MapLoader {
        +loadMap(FileHandle)
        +getMapFiles()
    }
    
    class HUD {
        +update(Character)
    }
    
    class PauseMenu {
        +updateStats()
    }
    
    class GameOverMenu
    
    class ConfigManager {
        +loadConfig()
        +saveConfig()
    }
    class GameConfig
    ConfigManager --> GameConfig
    
    class AchievementManager {
        +unlockAchievement()
    }
    class Achievement
    AchievementManager --> Achievement
    
    class GameSaveManager {
        +saveGame()
        +loadGame()
    }
    class LeaderboardManager
    
    %% UI & Logic Relations
    GameScreen --> HUD
    GameScreen --> PauseMenu
    GameScreen --> GameOverMenu
    GameScreen --> MapLoader
    Character --> PlayerState
    
    %% AI
    class Grid {
        +isWalkable(x, y)
    }
    
    class PathFinder {
        +findPath(Grid, start, end)
    }
    
    Enemy --> PathFinder
    PathFinder --> Grid
    
    %% Dialogues
    class DialogueBox {
        +show(text)
    }
    StoryDialogueScreen --> DialogueBox
    DialogueScreen --> DialogueBox
    
    %% Procedural Generation
    class DungeonGenerator {
        +generate(difficulty)
    }
    class Room
    
    DungeonGenerator --> Room
    DungeonGenerator --> GameObject : creates
    
    %% VFX
    class LightManager {
        +render(batch, lights)
    }
    class PointLight
    class DamageNumber
    class ScreenShake
    
    LightManager --> PointLight
    GameScreen --> LightManager
    GameScreen --> ScreenShake
```

## How to Play

### Controls
-   **Movement**: `W`, `A`, `S`, `D` keys.
-   **Pause**: `ESC` key.
-   **Console**: `GRAVE` (`) key (Debug mode).

### Rules
-   **Objective**: Explore the maze to find the **Key**. Once collected, the **Exit** door will open. Reach it to complete the level.
-   **Enemies**:
    -   **Slimes**: Patrol hallways. Avoid them!
    -   **Ghosts**: Can fly through walls and chase you.
-   **Health**: You start with 4 Lives. Touching an enemy will reduce your Lives. If you reach 0, Game Over.

### Items
-   ❤️ **Heart**: Restores 1 Life.
-   🛡️ **Shield**: Grants temporary invulnerability (glowing effect) and hurt enemies.

### Game Modes
1.  **Story Mode**: Play through crafted levels with a storyline.
2.  **Endless Mode**: Test your skills in procedurally generated dungeons that get harder over time. Earn XP to upgrade your stats (Health, Speed, Defense).
