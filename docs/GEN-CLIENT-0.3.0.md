# GEN CLIENT 0.3.0 — development notes

This branch begins the next UI iteration for Minecraft Fabric 1.21.1.

## Included in this stage
- New modular Right Shift control-center screen.
- Main sections: Mods, Visual, GenLand, Addons and Plugs.
- Visual module rows with ON/OFF controls and separate settings entry points.
- GenLand controls for the existing Dynamic Island HUD and Sakura sky petals.
- Dynamic Island follows the supplied top-bar layout: local system time and measured Minecraft FPS at the left; a compact rounded Spotify media capsule aligned to the right. Narrow GUI widths use a second row to prevent overlap.
- The capsule shows current track title/artist, playback progress and a five-bar animated/static visualizer. A music glyph remains a placeholder; Spotify album-art download and texture rendering are not implemented yet.
- Spotify metadata integration uses official OAuth PKCE and displays title and artist on separate lines with playback progress. When enabled, Island Text appears as a third, independently configured line; it is not sourced from Spotify and is not synchronized lyrics.
- Custom Sky applies a real client-side sky tint and has palette/blend-strength controls.
- Visual particle effects have individual colour palettes and three intensity levels; Low FX Mode reduces their particle count.
- Existing visual modules remain connected to the saved client configuration.
- Build workflow runs for pushes to this feature branch and checks that the new screen is packaged.

## Next implementation stages
- Add dedicated renderers for translucent glass armour and held-item glint rather than representing them only with particles.
- Add the remaining island appearance controls and layout sizing. Spotify artwork is not downloaded or cached; the HUD keeps a neutral music glyph to respect Spotify content-use restrictions.
- Expand GenLand text animation presets and plugin/add-on registry.

Spotify-synchronized lyrics are intentionally not implemented: Spotify's developer policy prohibits synchronizing Spotify content with visual media. The capsule can show Spotify metadata and a separately configured manual line, but does not perform karaoke synchronization.

FPS readouts must remain measurements from the Minecraft client. Performance features should be evaluated by actual frame-time/FPS improvements, not by changing the displayed number.


## Spotify redirect URI (fixed callback port)
- Register exactly `http://127.0.0.1:8888` in Spotify Developer Dashboard → App settings → Redirect URIs.
- The desktop client binds its local OAuth callback listener to IPv4 loopback port `8888`.
- The same URI is sent to Spotify's authorization and token endpoints. Do not substitute `localhost`, HTTPS, a different port, or a trailing slash.
- If port 8888 is occupied, the client shows an actionable status message; close the process using that port and retry.


## Dynamic Island typography update
- Dynamic Island labels (clock, FPS, track title/artist, optional manual line) and Target ESP text use bundled Poppins TTF fonts instead of Minecraft's default bitmap font.
- The regular and medium Poppins files are exposed as separate resource fonts so title/time weight is distinct from secondary text.
- The default Minecraft font is not replaced globally; this is scoped to GEN CLIENT HUD text.
- The upstream Poppins font is distributed under SIL Open Font License 1.1; the full license is bundled at `licenses/Poppins-OFL.txt`.
- Spotify album artwork remains a placeholder: the current branch does not download or cache Spotify artwork, and the feature is not claimed as implemented.
