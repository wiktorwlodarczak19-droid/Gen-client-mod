# GEN CLIENT 0.3.0 — development notes

This branch begins the next UI iteration for Minecraft Fabric 1.21.1.

## Included in this stage
- New modular Right Shift control-center screen.
- Main sections: Mods, Visual, GenLand, Addons and Plugs.
- Visual module rows with ON/OFF controls and separate settings entry points.
- GenLand controls for the existing Dynamic Island HUD and Sakura sky petals.
- Dynamic Island follows the supplied layout: local time and actual FPS at the left; a rounded media capsule at the right with GEN mark, current track title/artist, progress strip and animated/static visualizer.
- Spotify metadata integration uses official OAuth PKCE and displays title and artist on separate lines with playback progress. A separate manual capsule second line remains visible alongside the real track title; it is not automatically synchronized lyrics.
- Custom Sky applies a real client-side sky tint and has palette/blend-strength controls.
- Visual particle effects have individual colour palettes and three intensity levels; Low FX Mode reduces their particle count.
- Existing visual modules remain connected to the saved client configuration.
- Build workflow runs for pushes to this feature branch and checks that the new screen is packaged.

## Next implementation stages
- Add dedicated renderers for translucent glass armour and held-item glint rather than representing them only with particles.
- Add the remaining island appearance controls (album art when the provider exposes it and layout sizing).
- Expand GenLand text animation presets and plugin/add-on registry.

Spotify-synchronized lyrics are intentionally not implemented: Spotify's developer policy prohibits synchronizing Spotify content with visual media. The capsule can show Spotify metadata and a separately configured manual line, but does not perform karaoke synchronization.

FPS readouts must remain measurements from the Minecraft client. Performance features should be evaluated by actual frame-time/FPS improvements, not by changing the displayed number.
