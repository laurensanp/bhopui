# BhopUI

Springy menu animations for Minecraft 26.3 (Fabric, client only).

- Menus pop open: 77% → a hair past 100% → settle.
- Buttons and options are dealt in top to bottom, 45 ms apart: each slides up 8 px, grows from 90% and fades in.
- Buttons grow to 104% on hover, squash to 95% while pressed, spring back on release.
- Slider knobs spring to new values with ~7% overshoot; while you drag, they follow the mouse directly.
- Text fields pulse to 102% when focused.

Clickable areas never move during animations, motion is computed every frame (not per tick), and an interrupted
animation continues from where it is at the speed it has.

## Install

Put the jar in the `mods` folder of a Fabric 26.3 profile (Fabric Loader 0.20.1+, Java 25). Fabric API is not needed.
Works in Lunar Client profiles that load Fabric mods.

## Settings

`config/bhopui.properties` is created on first launch:

```
speed=1   # 2 = twice as fast, 0.5 = slow motion, 0 = animations off
```

## What gets animated

| Menu | Animation |
|---|---|
| Vanilla button menus (pause, options, …) | pop, cascade, button squash, sliders |
| Scrolling lists (worlds, servers, controls) | pop, button squash; the list arrives as one block |
| Inventories and containers | pop only |
| Other mods' menus | pop; buttons only if they use vanilla widgets |

Not yet: closing animation, scroll glide and overscroll, tooltip/dropdown pops, switches, tab highlights, custom colours.

## Springs

| | Stiffness | Damping | Overshoot | At rest |
|---|---|---|---|---|
| Fast | 2500 | 72 | ~4% | ~130 ms |
| Soft | 332 | 23.4 | ~7% | ~340 ms |

Check them with `java src/client/java/com/bhopui/Spring.java`.

## Build

```
./gradlew build
```

The jar lands in `build/libs/`. `./gradlew runClient` starts a test game.

## License

MIT
