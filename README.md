# Maze Runner Game

A Java-based maze runner game built with LibGDX. Navigate through mazes, avoid enemies, collect keys, and find the exit!

## Project Structure

The source code is organized into the following packages under `de.tum.cit.fop.maze`:

### **Root Package** (`de.tum.cit.fop.maze`)
Contains the core game loop and screen management.
-   **`MazeRunnerGame`**: The main entry point extending `LibGDX Game`. It initializes global resources (SpriteBatch, Skin, Audio) and manages screen transitions. Use `goToStory()`, `goToEndlessMode()`, etc., to switch contexts.
-   **`GameScreen`**: The primary gameplay screen. Handles the game loop, rendering of the world, and updates for all game objects.

### **GameObj** (`de.tum.cit.fop.maze.GameObj`)
Defines all interactive and static entities within the game world.
-   **`GameObject`**: Abstract base class for all entities (holds position, texture).
-   **`MovableObject`**: Extends `GameObject` with physics (velocity) and health.
-   **`Character`**: The player-controlled hero. Implements input handling and interaction logic.
-   **`Enemy`**: Base class for hostile mobs with FSM AI (Patrolling, Chasing).
-   **`Ghost`**: A specific enemy type that can fly through walls.
-   **Items**: `Key`, `Heart`, `ShieldItem`.
-   **Environment**: `Wall`, `Trap`, `EntryPoint`, `Exit`.

### **GameControl** (`de.tum.cit.fop.maze.GameControl`)
Manages game systems, UI screens, and persistent data.
-   **Screens**:
    -   **`StoryMenu`**: The new main menu hub with an interactive background.
    -   **`CinematicScreen`**: visual-novel style storytelling screen used for intros and cutscenes.
    -   **`EncyclopediaScreen`**: A gallery to view unlocked characters and items.
    -   **`SkillTreeScreen`**: Interface for upgrading player stats in Endless Mode.
    -   **`LevelSelectionScreen`**: Choose specific levels in Story Mode.
    -   **`SettingsScreen`**: Configure volume and controls.
    -   **`GameOverMenu`** / **`PauseMenu`**: In-game overlays.
-   **Managers**:
    -   **`HUD`**: Heads-Up Display for health, score, and keys.
    -   **`ConfigManager`**: Handles user settings (volume, keybindings).
    -   **`GameSaveManager`**: Serializes game state to JSON for Save/Load functionality.
    -   **`AchievementManager`**: Tracks in-game achievements.
    -   **`LeaderboardManager`**: Manages local and online high scores.
    -   **`EncyclopediaManager`**: Tracks discovered content.

### **AI** (`de.tum.cit.fop.maze.AI`)
Provides navigation logic.
-   **`Grid`**: Boolean grid of the world.
-   **`PathFinder`**: A* (A-Star) algorithm implementation.

### **Conversation** (`de.tum.cit.fop.maze.Conversation`)
Refactored system for dialogue and narrative.
-   **`DialogueManager`**: Central manager for loading and displaying dialogues using JSON data.
-   **`DialogueBox`**: UI component for rendering speech bubbles.
-   **`DialogueData`**: Data model for conversation trees.

### **Procedure** (`de.tum.cit.fop.maze.Procedure`)
Procedural content generation (PCG).
-   **`DungeonGenerator`**: Algorithms to generate random layouts for Endless Mode.
-   **`Room`**: Helper for dungeon geometry.

### **VFX** (`de.tum.cit.fop.maze.VFX`)
Visual effects.
-   **`LightManager`**: Dynamic lighting using FBOs.
-   **`ScreenShake`**: Impact feedback effect.
-   **`DamageNumber`**: Floating combat text.

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
        +goToEndlessMode()
    }
    
    %% Screens
    class Screen {
        <<interface>>
        +render(delta)
        +resize(width, height)
        +dispose()
    }
    
    MazeRunnerGame --> Screen : manages
    Screen <|.. GameScreen
    Screen <|.. StoryMenu
    Screen <|.. CinematicScreen
    Screen <|.. EncyclopediaScreen
    Screen <|.. LevelSelectionScreen
    Screen <|.. SkillTreeScreen
    Screen <|.. SettingsScreen
    
    %% Game Objects
    class GameObject
    class MovableObject
    class Character
    class Enemy
    class Ghost
    
    GameObject <|-- MovableObject
    MovableObject <|-- Character
    MovableObject <|-- Enemy
    Enemy <|-- Ghost
    
    %% Systems
    class HUD
    class ConfigManager
    class GameSaveManager
    class DialogueManager
    class EncyclopediaManager
    
    GameScreen --> HUD
    MazeRunnerGame --> ConfigManager
    GameScreen --> DialogueManager
    StoryMenu --> EncyclopediaManager
    
    %% AI & PCG
    class PathFinder
    class DungeonGenerator
    
    Enemy --> PathFinder
    GameScreen --> DungeonGenerator
```

## How to Run

1.  **Prerequisites**: Ensure you have a Java Development Kit (JDK) 17 or higher installed.
2.  **Build & Run**:
    This project uses Gradle. Open a terminal (PowerShell or CMD) in the project root.
    
    **Windows (PowerShell)**:
    ```powershell
    .\gradlew desktop:run
    ```
    
    **Windows (CMD)**:
    ```cmd
    gradlew desktop:run
    ```
    
    **Mac/Linux**:
    ```bash
    ./gradlew desktop:run
    ```

## How to Play

### Controls
-   **Movement**: `W`, `A`, `S`, `D` keys.
-   **Pause**: `ESC` key.
-   **Console**: `GRAVE` (`) key (Debug mode).

### Rules
-   **Objective**: Explore the maze to find the **Key**. Once collected, the **Exit** door will open.
-   **Enemies**: Avoid Slimes and Ghosts. Use your shield or skills to survive.
-   **Health**: Lose all lives and it's Game Over.

### Game Modes
1.  **Story Mode**: 
    -   Experience the narrative through **Cinematic Cutscenes**.
    -   Progress through handcrafted levels.
    -   Uncover the mystery of the maze.
2.  **Endless Mode**: 
    -   Face infinite, procedurally generated dungeons.
    -   **XP & Leveling**: Earn XP to level up.
    -   **Skill Tree**: Spend points to upgrade Health, Speed, and Defense in the Skill Tree.
    -   Compete on the global **Leaderboard**.

## Key Features

-   **Cinematic Storytelling**: Immersive visual-novel style intros and endings.
-   **Encyclopedia System**: Automatically unlocks entries for characters, enemies, and items as you encounter them.
-   **RPG Elements**: Level up and customize your character's stats in Endless Mode.
-   **Dynamic Lighting**: Atmospheric visual effects including fog of war.
-   **Smooth Movement**: Physics-based controls with smart corner sliding.
-   **Online Leaderboard**: Track your best runs against players worldwide.
-   **Original Soundtrack**: Enjoy custom-composed music and sound effects created specifically for this game.

