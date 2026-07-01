# Rapit Client

A premium-styled, black (`#0D0D0D`) & yellow (`#FFD400`) themed utility client
for **Minecraft Forge 1.8.9**, built in Java with Gradle/ForgeGradle.

> **Scope note:** this build focuses on UI/UX polish - an animated ClickGUI,
> per-module right-click settings (toggle/slider/color/keybind), a UI Edit
> Mode with drag-to-resize, animated keystrokes, Block Outline with RGB, and
> a clean default cape. It intentionally does **not** include Fly, Reach, or
> Velocity/anti-knockback modules, or an autoclicker - those give an unfair
> advantage over other players on multiplayer servers, generally violate
> server rules, and (in the case of a "humanized" variable-CPS autoclicker)
> are specifically designed to evade anticheat detection. That's a different
> category of software than a cosmetic/QoL client and isn't something this
> project builds.

## Copyright note on Garfield branding

Garfield is a copyrighted character owned by Paws, Inc. This project does
**not** bundle any Garfield artwork. The default cape is a plain dark cape
with no logo (`src/main/resources/assets/rapitclient/textures/cape/cape.png`)
and the main-menu crest is an original placeholder
(`src/main/resources/assets/rapitclient/textures/gui/logo.png`). Swap either
PNG for your own licensed artwork - no code changes needed.

## Features

### ClickGUI (open with `Right Shift`)
- Floating panel anchored center-right (not full-screen), draggable by its title bar
- Fade + slide-in open animation, hover "grow" and click "bounce" on module rows
- Category tabs: HUD / Visual / Performance / Cosmetics / Settings
- **Right-click any module** to open its settings popup:
  - Enabled toggle switch
  - Keybind rebind
  - Opacity slider (HUD modules)
  - Any module-specific sliders (e.g. Block Outline thickness)
  - Color picker: hue slider + RGB-cycle toggle (e.g. Block Outline color)
- "UI Edit Mode" entry under the Settings tab opens the HUD editor

### HUD (all draggable and resizable — see UI Edit Mode below)
- FPS Counter, CPS Counter (smoothed, not instant-jump), Ping, Coordinates,
  Direction, Clock
- Armor HUD, Potion Effects list
- Toggle Sprint (default bind: `R`)
- Watermark

### Visual
- Fullbright (default bind: `F`)
- Zoom (hold, default bind: `C`)
- Clear Water (removes underwater fog)
- **Keystrokes** — animated press: keys scale up with an ease-out-back
  "bounce" and glow on press, easing back down on release
- Item Counter (large held-stack count)
- **Block Outline** — highlights the block you're looking at; color and
  thickness are configurable via its right-click settings panel, including
  an RGB cycling mode

### Performance
- Entity Culling (skips rendering far-away entities via `RenderLivingEvent`)
- Particle Limiter
- Fast GUI (disables Fancy Graphics)

### Cosmetics
- **Cape** — plain dark cape, no logo, client-side only. Positioning is
  driven off the player's own interpolated body yaw/limb-swing (same
  values vanilla uses for the player model) instead of a free-running
  timer, which is what keeps it from jittering/drifting during turns.
- Cosmetics master toggle

### UI Edit Mode
Reachable from the ClickGUI's Settings tab (or press `H` directly):
- Click-drag any HUD element to reposition it
- Click-drag the small handle at an element's bottom-right corner to
  resize it (adjusts a per-element scale factor, 0.5x-2.5x)
- Position, scale, and opacity all persist automatically

### Animation system
`com.rapit.client.animation` provides `Easing` (linear, ease-in-out,
ease-out-cubic, ease-out-back/bounce) and `AnimatedValue` (a wall-clock-time
driven float that eases toward a target). Every animated element in the
client - panel open/close, row hover/press, keystroke press, CPS counter,
settings popup pop-in - is built on these two classes rather than each
screen hand-rolling its own timer logic.

### Font rendering
1.8.9's FontRenderer doesn't support swapping in an arbitrary TTF/OTF
without shipping a full bitmap font resource pack, which wasn't something
that could be authored and verified without a working Forge toolchain in
the environment this was built in. What's included instead:
`RenderUtils.drawScaledString(...)`, which renders vanilla's font through a
GL11 scale transform for crisper, larger "premium" headers (used for the
ClickGUI title). If you want a true custom font, drop a bitmap font
resource pack's assets in under `assets/minecraft/textures/font/` and
Forge/vanilla will pick it up automatically - no code changes needed.

### Config
Every module's enabled state, keybind, HUD position/opacity/scale, and
right-click-panel settings (slider values, color hue + RGB mode) auto-save
to `config/rapitclient.properties` on world exit and reload automatically
on next launch.

## Project structure

```
src/main/java/com/rapit/client/
├── RapitClient.java        # @Mod entry point
├── animation/                # Easing + AnimatedValue (shared by every animated UI element)
├── module/
│   ├── Module.java             # base class: enabled state, keybind, HUD position/opacity/scale
│   └── settings/                # SliderSetting, ColorSetting - right-click panel values
├── modules/
│   ├── hud/                   # FPS, CPS, ping, coords, clock, armor, etc.
│   ├── visual/                  # Fullbright, zoom, keystrokes, clear water, item counter, block outline
│   ├── performance/              # Entity culling, particle limiter, fast GUI
│   └── cosmetics/                 # Cape + cosmetics master toggle
├── gui/                     # ClickGUI, HUD/UI Edit Mode, animated main menu
├── cape/                    # Client-side cosmetic cape renderer
├── render/                  # Shared GL11 drawing helpers (rounded panels, theme colors, opacity)
├── config/                  # Save/load module state + settings
└── events/                  # Keybind registration/handling, config auto-save
```

## Requirements

- JDK 8 (Forge 1.8.9 / ForgeGradle 2.1 will not build on newer JDKs)
- Internet access on first build (ForgeGradle downloads MC 1.8.9 +
  Forge + MCP mappings the first time you run it)

## Build guide (local)

```bash
git clone <your-fork-url>
cd rapit-client

# First time only: generates gradlew (kept out of the repo since it's
# a binary — see .github/workflows/build.yml for why)
gradle wrapper --gradle-version 2.14

./gradlew setupDecompWorkspace
./gradlew build
```

The finished jar lands in `build/libs/RapitClient-1.0.0.jar`.

## Build guide (GitHub Actions)

This repo includes `.github/workflows/build.yml`, which on every push:

1. Sets up JDK 8
2. Installs Gradle 2.14 and generates the wrapper
3. Runs `setupDecompWorkspace` (downloads/deobfuscates Minecraft + Forge)
4. Runs `./gradlew build`
5. Uploads the resulting jar as a workflow artifact ("RapitClient-jar")

Just push to `main`/`master` (or trigger it manually from the Actions tab)
and download the built jar from the run's **Artifacts** section — no local
setup required.

## Installation (playing the client)

1. Install Minecraft Forge **1.8.9** (recommended build `11.15.1.2318`)
   normally through the vanilla launcher.
2. Build (or download) `RapitClient-x.x.x.jar`.
3. Drop the jar into your `.minecraft/mods` folder.
4. Launch the Forge 1.8.9 profile. Rapit Client loads automatically.

## Controls

| Key | Action |
|---|---|
| `Right Shift` | Open ClickGUI |
| `H` | Open UI Edit Mode (also reachable from ClickGUI → Settings) |
| `F` | Toggle Fullbright |
| `C` (hold) | Zoom |
| `R` | Toggle Sprint |
| Right-click a module row in ClickGUI | Open its settings popup |
| In settings popup: click Keybind row, then press a key | Rebind |
| In settings popup: drag hue bar / slider | Adjust color / value |

## Roadmap

Architecture already supports these as drop-in `Module` subclasses
(override `onTick`/`onRenderOverlay`/`onRenderWorld`, register in
`ModuleManager.registerDefaultModules()`); they were left out of this pass
rather than shipped half-working:

- Motion Blur (needs an accumulation-buffer or shader pass)
- Chunk Borders
- Damage Particles indicator
- Better Chat (timestamps, deduping, copy-to-clipboard)
- Crosshair Editor
- Item Physics (dropped-item render tweaks)
- FPS Boost / Memory Optimization / Animation Optimization / Smart Render
  (deeper render-pipeline tuning)
- Simple Cape Manager (swap between multiple saved cape textures)
- True Gaussian blur behind the ClickGUI panel (needs a custom fragment
  shader; current panel uses a layered drop-shadow + translucent backdrop
  as a lighter-weight stand-in - see `ClickGui`'s class javadoc)
- Real bitmap font swap (drop a resource pack's font assets into
  `assets/minecraft/textures/font/` - Forge picks it up automatically,
  no code change needed)

Not planned: Fly, Reach, Velocity/anti-knockback, or an autoclicker. These
give an unfair advantage in multiplayer and generally violate server rules;
see the scope note at the top of this file.

## License

MIT — see [LICENSE](LICENSE). Does not cover Minecraft/Forge/Mojang assets
or any third-party character artwork you may add yourself.
