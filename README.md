# GEN CLIENT

GEN CLIENT is a client-side Minecraft **1.21.1** mod for **Fabric**, styled after the supplied GEN CLIENT launcher: Sakura Dark, charcoal surfaces, soft pink accent, sharp readable text, restrained glass-like panels and custom navigation.

## Requirements
- Minecraft Java Edition 1.21.1
- Fabric Loader 0.16.10+
- Fabric API for Minecraft 1.21.1
- Java 21

## Build
The project uses the standard Fabric Loom pipeline. No custom archive task overrides Loom's remapping input.

```bash
gradle --no-daemon clean build
```

The installable remapped mod is generated at `build/libs/gen-client-0.2.0.jar`. Do not install `*-dev.jar` or `*-sources.jar`.

## Controls
- **Right Shift** — open or close the GEN CLIENT Visuals screen.
- **Escape** — close the custom pause menu / return to game.

## Included
- Custom GEN CLIENT title screen with Singleplayer, Multiplayer, Visuals, Style, Options and Quit.
- Custom pause screen.
- Visuals panel with persistent client-side visual toggles.
- Style panel with accent palette, wallpaper choices, Sakura petals, Dynamic Island and companion mod badges.
- Dynamic Island HUD with local clock, FPS and server/ping.
- Companion badges for Flashback and Mod Menu.
- Cosmetic Jump Circle, Glass Vapor, Hit Sparks, Aura Glint and Armor Glass Light particles.
- Settings stored in `config/genclient.json`.

## Rendering note
The custom screens draw their own backgrounds and panels. They do not call Minecraft's vanilla screen-background blur. UI text and controls are rendered sharply; translucent surfaces use solid alpha fills and fine borders rather than blurring the entire screen.

## Scope
All included effects are client-side cosmetics and interface features. The mod does not implement combat automation, reach changes, anti-cheat bypasses or other gameplay advantages.
