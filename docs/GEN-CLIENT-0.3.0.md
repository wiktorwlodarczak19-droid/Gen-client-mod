# GEN CLIENT 0.3.0 — development notes

This branch begins the next UI iteration for Minecraft Fabric 1.21.1.

## Included in this stage
- New modular Right Shift control-center screen.
- Main sections: Mods, Visual, GenLand, Addons and Plugs.
- Visual module rows with ON/OFF controls and separate settings entry points.
- GenLand controls for the existing Dynamic Island HUD and Sakura sky petals.
- Existing visual modules remain connected to the saved client configuration.
- Build workflow runs for pushes to this feature branch and checks that the new screen is packaged.

## Next implementation stages
- Replace the current top HUD island with the supplied compact layout: local time, actual FPS, album art, track metadata, lyrics and progress.
- Add a real Spotify playback bridge and synchronized lyrics provider.
- Expand each visual settings page with actual render controls (sky tint, sun tint, glass and glint parameters).
- Add GenLand appearance controls and plugin/add-on registry.

FPS readouts must remain measurements from the Minecraft client. Performance features should be evaluated by actual frame-time/FPS improvements, not by changing the displayed number.
