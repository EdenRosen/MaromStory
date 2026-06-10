# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

No build tool (no Maven/Gradle). Compile and run manually from the repo root:

```powershell
# Compile
javac -cp "lib\poi-4.1.2.jar;lib\poi-ooxml-4.1.2.jar;lib\poi-ooxml-schemas-4.1.2.jar;lib\xmlbeans-3.1.0.jar" -d out (Get-ChildItem -Recurse src -Filter "*.java" | Select-Object -ExpandProperty FullName)

# Run
java -cp "out;lib\poi-4.1.2.jar;lib\poi-ooxml-4.1.2.jar;lib\poi-ooxml-schemas-4.1.2.jar;lib\xmlbeans-3.1.0.jar" my_base.App
```

The `lib/` directory contains Apache POI jars (used by `src/db/ExcelDB.java`). The `resources/` directory holds sprite images and backgrounds loaded at runtime via relative paths — the working directory must be the repo root when running.

There are no tests.

## Working in this codebase

**`src/ai/`** — free rein. Refactor, restructure, add rendering features, split files, rename — no need to ask.

**Everything else** — ask before changing. Prefer the smallest edit that solves the problem; avoid restructuring or adding abstractions unless asked.

## Architecture

Java Swing 2D side-scrolling game (MaromQuest) built on a strict layered architecture with enforced dependency rules.

### Boot sequence (`my_base/`)

`App.main()` wires everything in order:
1. `AppContent.initContent()` — creates `Canvas` and `Backend`
2. `new Ui()` — creates the JFrame and `DrawingPanel`
3. `ui.start(mainRouter)` — sets `UiPort` singleton, routes `/system/init` to fire `Backend.initializeApp()`
4. `PeriodicScheduler.start()` — fires `MyPeriodicLoop.execute()` every 30 ms, which calls `Backend.updatePlayer()`

`App.content()` is the global singleton accessor used throughout the codebase to reach `Canvas` and `Backend`.

### Input → Action pipeline

```
KeyEvent (DrawingPanel)
  → getKeyName() maps keycode to string ("left", "attack", …)
  → mainRouter.route("/system/key/down", Params.of(key))
  → SystemRouter.route() switches on subPath + key string
  → Backend method call (e.g. startMoveLeft(), attackEnemy())
```

`Backend` never imports key codes or AWT. `SystemRouter` is the only class that knows the mapping between key strings and `Backend` calls. To add a new key binding: add a case to `getKeyName()` in `DrawingPanel`, then handle it in `SystemRouter`.

To add a new subsystem (e.g. inventory, quests): create a new `SubRouter` implementation and register it in `App.registerRouters()` with a new prefix (e.g. `"inventory"`). Routes will then be `/inventory/…`.

### Game logic layer (`team/`)

- **`Canvas`** — the world model: holds `Map` (platforms as `MapRect` list), `MainPlayer`, `Sword`, and `List<Enemy>`. Rebuilt entirely on `resetScenario()`.
- **`Backend`** — control layer called by `SystemRouter`. Never imports Swing. Calls `UiPort.getInstance()` to trigger redraws.
- **`Character`** (abstract base) — shared physics (`update(platforms)` applies gravity, collision, boundary clamping), jump, attack system, sword pickup/drop. Both `MainPlayer` and `Enemy` extend it.
- **`MainPlayer`** — `MOVE_SPEED=5`, `PICKUP_RANGE=60`, `ATTACK_RANGE=90`, `ATTACK_ANIMATION_TICKS=8`. Manages active attack index; starts with `BasicAttack` (index 0), `SlashAttack` added at index 1 in `Canvas.initCanvas()`.
- **`Enemy`** — AI in `updateAi(player)`: chases if `|dx| > 40`, attacks if `canAttack(player, range)` is true. `DEATH_ANIMATION_TICKS=18`. Death animation and removal are driven by `Backend.updateEnemies()`.
- **`PlayerStats`** — HP, MP, STR, AGI. `equipSword`/`unequipSword` mutates STR. `isDead()` returns `health <= 0`.
- **`Attacks` interface** — implemented by `BasicAttack` and `SlashAttack`. Each has `canExecute()`, `executeAttack()`, `getMpCost()`, `getCooldown()`.

### UI layer (`ai/ui/`)

- **`Ui`** — creates the JFrame (1200×800), `DrawingPanel`, control panel with Reset button, and `UiPortImpl`. Sets the `UiPort` singleton.
- **`DrawingPanel`** — Swing `JPanel`. Owns the key listener and all rendering. `paintComponent` checks `gameStarted` flag: if false, renders the start screen; if true, renders background → map → images → sword → enemies → player → HUD.
- **`UiPortImpl`** — implements `UiPort`. Holds the image map and refs to `Map`/`MainPlayer`. All methods call `panel.repaint()`.
- **`UiPort`** (in `shared/ui_ports/`) — the **only** bridge between `team/` and `ai/ui/`. Backend code may only call `UiPort`; it must never import anything from `ai.ui`.

### Dead code / not yet wired

- `AudioPlayer` (`src/base/`) — never called anywhere.
- `ExcelDB` / `ExcelTable` (`src/db/`) — never wired into the game loop; the DB singleton is never called from `Backend` or `Canvas`.
- `Enemy.attackPlayer()` / `canAttack()` exist but `Backend.updateEnemies()` does not call them — enemies do not currently deal damage to the player.
- `PlayerStats.restoreEnergy()` and `heal()` are implemented but never called.
