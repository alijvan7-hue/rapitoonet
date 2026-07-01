# Rapit Client

A lightweight, black (`#111111`) & yellow (`#FFD400`) themed utility client
for **Minecraft Forge 1.8.9**, built in Java with Gradle/ForgeGradle.

> **Honesty note on scope:** this repo ships a fully working core
> (module system, config persistence, keybinds, HUD, ClickGUI, cape
> hook, custom main menu) with **19 real, functioning modules**. It
> does not yet include every module named in the original wishlist
> (Motion Blur, Block Overlay, Chunk Borders, Damage Particles, Better
> Chat, Crosshair Editor, Item Physics, and a few performance/cosmetic
> extras). Those were left out rather than shipped as fake/empty
> "modules" — see [Roadmap](#roadmap) below for what's next and how
> the architecture already supports adding them cleanly.

## Copyright note on Garfield branding

Garfield is a copyrighted character owned by Paws, Inc. This project does
**not** bundle any Garfield artwork. What it does ship:

- A generic original placeholder crest at
  `src/main/resources/assets/rapitclient/textures/gui/logo.png`
- A generic original placeholder cape texture at
  `src/main/resources/assets/rapitclient/textures/cape/cape.png`
- Full code plumbing (`CapeRenderer`, `RapitMainMenu`) that loads whatever
  PNG lives at those paths

If you own or have a license for Garfield artwork, drop your own PNGs in at
those exact paths and rebuild — no code changes needed.

## Features

### HUD (all draggable — press `H` in-game to enter the HUD editor)
- FPS Counter, CPS Counter, Ping, Coordinates, Direction, Clock
- Armor HUD, Potion Effects list
- Toggle Sprint (default bind: `R`)
- Watermark

### Visual
- Fullbright (default bind: `F`)
- Zoom (hold, default bind: `C`)
- Clear Water (removes underwater fog)
- Keystrokes (WASD + mouse buttons)
- Item Counter (large held-stack count)

### Performance
- Entity Culling helper (distance-based skip logic)
- Particle Limiter
- Fast GUI (disables background blur pass)

### Cosmetics
- Garfield Cape slot (client-side only cosmetic layer)
- Cosmetics master toggle

### GUI
- **ClickGUI** — open with `Right Shift`. Black/yellow theme, category
  tabs (HUD / Visual / Performance / Cosmetics / Settings), live search,
  draggable panel, left-click to toggle a module, right-click to rebind it.
- **HUD Editor** — open with `H`. Click-drag any enabled HUD element to
  reposition it live.
- **Custom animated main menu** replacing vanilla's, with the branded
  wordmark, animated gradient background, and Singleplayer/Multiplayer/
  Options/Quit buttons.

### Config
Every module's enabled state, keybind, and (for HUD modules) screen
position auto-saves to `config/rapitclient.properties` on world exit /
game close, and reloads automatically on next launch.

## Project structure

```
src/main/java/com/rapit/client/
├── RapitClient.java        # @Mod entry point
├── module/                 # Module base class, categories, manager/dispatcher
├── modules/
│   ├── hud/                 # FPS, CPS, ping, coords, clock, armor, etc.
│   ├── visual/               # Fullbright, zoom, keystrokes, clear water, item counter
│   ├── performance/          # Entity culling, particle limiter, fast GUI
│   └── cosmetics/             # Cape + cosmetics master toggle
├── gui/                     # ClickGUI, HUD editor, animated main menu
├── cape/                    # Client-side cosmetic cape renderer
├── render/                  # Shared GL11 drawing helpers (rounded panels, theme colors)
├── config/                  # Save/load module state
└── events/                  # Keybind registration/handling
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
| `H` | Open HUD position editor |
| `F` | Toggle Fullbright |
| `C` (hold) | Zoom |
| `R` | Toggle Sprint |
| Right-click a module row in ClickGUI | Rebind its key |

## Roadmap

Architecture already supports these as drop-in `Module` subclasses
(override `onTick`/`onRenderOverlay`/`onRenderWorld`, register in
`ModuleManager.registerDefaultModules()`); they were left out of this pass
rather than shipped half-working:

- Motion Blur (needs an accumulation-buffer or shader pass)
- Block Overlay / X-ray-style outline rendering
- Chunk Borders
- Damage Particles indicator
- Better Chat (timestamps, deduping, copy-to-clipboard)
- Crosshair Editor
- Item Physics (dropped-item render tweaks)
- Hit Color customization
- FPS Boost / Memory Optimization / Animation Optimization / Smart Render
  (deeper render-pipeline tuning)
- Simple Cape Manager (swap between multiple saved cape textures)
- Client Color Theme picker (currently theme is fixed black/yellow)

## License

MIT — see [LICENSE](LICENSE). Does not cover Minecraft/Forge/Mojang assets
or any third-party character artwork you may add yourself.
