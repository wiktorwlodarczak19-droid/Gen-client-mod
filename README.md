# GEN CLIENT 0.3.0

Client-side mod do Minecraft Java **1.21.1** na **Fabric**, rozwijający własny launcherowy styl, panel Right Shift, Dynamic Island i efekty kosmetyczne.

## Wymagania
- Minecraft Java Edition 1.21.1
- Fabric Loader 0.16.10+
- Fabric API dla Minecraft 1.21.1
- Java 21

## Panel Right Shift
- **Prawy Shift** — otwiera lub zamyka GEN CLIENT Control Center.
- Zakładki: **Mods**, **Visual**, **GenLand**, **Addons**, **Plugs**.
- Moduły mają własne przełączniki ON/OFF i osobne ustawienia pod zębatką.
- Konfiguracja jest przechowywana w `config/genclient.json`.

## Dynamic Island
- Lokalna godzina systemowa (HH:mm) i rzeczywisty FPS po lewej stronie.
- Kompaktowa, półprzezroczysta kapsuła Spotify wyrównana do prawej krawędzi; na wąskich ekranach przechodzi do drugiego wiersza, aby nie nachodzić na zegar i FPS.
- Tytuł utworu, wykonawca i pasek postępu z oficjalnej integracji Spotify.
- Pięcioelementowy, płynnie animowany albo statyczny wizualizer.
- Własna typografia Poppins (TTF) dla tekstu wyspy i Target ESP; domyślna czcionka Minecrafta pozostaje bez zmian w pozostałych ekranach.
- Ikona muzyczna jest tymczasowym znakiem zastępczym; pobieranie i renderowanie okładki albumu nie jest jeszcze zaimplementowane.
- Opcjonalna własna linijka tekstu jest renderowana pod tytułem i wykonawcą; to tekst wpisany przez użytkownika, nie zsynchronizowane lyrics.

**Lyrics:** GEN CLIENT nie pobiera ani nie synchronizuje tekstów piosenek. Własna linijka w Island Text jest niezależna od utworu i nie udaje synchronizacji. Okładki Spotify nie są pobierane ani przechowywane lokalnie.

## Mods
- FPS Counter — rzeczywisty FPS Minecrafta.
- Low FX Mode — ogranicza liczbę cząsteczek GEN CLIENT; nie udaje dodatkowych FPS.

## Visual
- Jump Circle
- Glass Vapor
- Target ESP
- Kill Effect
- Hit Sparks
- Custom Sky — rzeczywisty tint koloru nieba przez ClientWorld mixin.
- Glint Hand
- Aura Glint Light 3D
- Armor Glass Light 3D — efekt cząsteczkowy/świetlny, nie pełny shader pancerza.

Efekty cząsteczkowe mają indywidualny kolor i trzy poziomy intensywności.

## GenLand / Addons / Plugs
- GenLand: Dynamic Island i Sky Petals.
- Addons: Kill Effect, Hit Sparks, Companion Badges.
- Plugs: Spotify OAuth PKCE i własny tekst kapsuły.

## Spotify
W Plugs → Spotify Player otwórz ustawienia, wpisz Client ID aplikacji Spotify Developer i połącz konto. Dźwięk nadal odtwarza Spotify; GEN CLIENT odczytuje tylko metadane i postęp przez oficjalne API.

## Budowanie
```bash
gradle --no-daemon clean build
```

Gotowy JAR: `build/libs/gen-client-0.3.0.jar`. Nie instaluj plików `*-dev.jar` ani `*-sources.jar`.

## Zakres
Mod client-side z interfejsem i efektami kosmetycznymi. Nie zawiera automatyzacji walki, zmian zasięgu, bypassów antycheatów ani sztucznego podbijania FPS.
