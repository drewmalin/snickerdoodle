# snickerdoodle

## Engine

An OpenGL engine meant to act like a simple game engine.

See: `com.drewmalin.snickerdoodle.engine.opengl.OpenGlEngine`

## Game

A demo application that makes use of the engine.

See: `com.drewmalin.snickerdoodle.game.Game`

## Package

Distribution layer tasked with bundling platform-specific artifacts.

### Notes:
This project uses [mise](https://mise.jdx.dev) to establish dependency versions and define tasks.

To see the available tasks, run:

```
> mise tasks
```

### Implementation Notes:

Due to the below error, this project uses **JDK 17**:
```
Unsupported JNI version detected, this may result in a crash. Please inform LWJGL developers.
``` 

Inspirations:
* https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/
* https://gafferongames.com (specifically: https://gafferongames.com/post/fix_your_timestep/)
