# MaromStory
We have built a 2D action game inspired by MapleStory, developed for a project in Embedded Systems course, as part of MSE degree in Systems Engineering, Marom program.

The developers: Eden, Agam, and Yaniv.

## Overview
MaromStory is a small Java-based game demonstrating game loops, simple physics, input handling and a modular project structure suitable for student projects and local play.

## Features
- Player and enemy entities
- Multiple attack types and items
- Simple map/level system
- Game mode selection inlcuding multiplayer

## Requirements
- Java JDK 17+ (JDK 24 was used during development)

## Run (quick)
Download the repository as a zip file and exctract. Open the folder with Vscode, install the Code Runner Vscode extention, go to scr/my_base/App.java, wait until there appears a run button above the function "main" and click it. This will open the starting window of the game.

## Project structure (important folders)
- `src/` — Java source tree
	- `src/base/` — core utilities and scheduler
	- `src/team/model/` — game model: players, enemies, attacks, items
	- `src/team/control/` — backend and game state
	- `src/ui/` — UI and drawing components
	- `src/my_base/` — application entry point (`my_base.App`)
- `lib/` — external libraries (if any)
- `resources/` — images, sounds, and other assets (we used only images)

## Contact
Authors:
* Eden Rosen
* Agam Yehuda
* Yaniv Ghelber


Enjoy playing and experimenting with the code!
