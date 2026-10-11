# Camera Flick (Fabric 1.21.11)

Client-side source project for a smooth configurable camera flick.

## Defaults
- Activate: `G`
- Direction toggle: unbound by default
- Angle preset: unbound by default
- Angle: 45 degrees
- Rotate out: 70 ms
- Hold: 0 ms
- Return: 70 ms
- Smoothness: 0.65

## Configure
Run once to create `config/camera_flick.properties`, then close Minecraft and edit:

```properties
angle=45
# RIGHT or LEFT
direction=RIGHT
outMs=70
delayMs=0
backMs=70
# 0.0 = linear, 1.0 = smoothstep
smoothness=0.65
```

Times are milliseconds. Very short times may look less smooth at low FPS, because the number of displayed frames is limited by the frame rate.

## Build
Use Java 21 and Gradle, then run `gradle build`. The jar is placed in `build/libs/`.

## Notes
This is a source project, not a precompiled/verified jar. The exact Fabric Loom/API versions may need updating to versions available for Minecraft 1.21.11 in your Gradle environment. Test in single-player first. This mod changes the local player's yaw during a client render callback; multiplayer servers may apply their own rotation or anti-cheat rules.
