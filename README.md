# GEN CLIENT 0.2.0

Client-side mod do Minecraft Java **1.21.1** na **Fabric**, inspirowany dostarczonym launcherem GEN CLIENT: układ Play / Mods / Style, monochromatyczne tło z górskim pasmem, logo, podgląd skina, ciemny panel i różowy akcent Sakura.

## Wymagania
- Minecraft Java Edition 1.21.1
- Fabric Loader 0.16.10+
- Fabric API dla Minecraft 1.21.1
- Java 21

## Budowanie
Projekt używa standardowego pipeline Fabric Loom — bez własnych zadań nadpisujących archiwum lub wejście `remapJar`.

```bash
gradle --no-daemon clean build
```

Gotowy JAR do instalacji:
`build/libs/gen-client-0.2.0.jar`

Nie instaluj plików `*-dev.jar` ani `*-sources.jar`.

## Ekran główny
- Układ inspirowany launcherem: panel Play po lewej, logo i podgląd skina pośrodku, nawigacja Play / Mods / Style u dołu.
- Play otwiera wybór świata; Multiplayer i Options są dostępne w panelu.
- Nick pochodzi z aktualnej sesji Minecrafta, a wersja jest stała (1.21.1 / Fabric). Mod nie jest osobnym launcherem, więc nie zmienia wersji gry ani konta Microsoft.
- Podgląd skina używa tekstury aktualnego profilu. Przycisk Choose Skin otwiera stronę profilu Minecraft. Quit Game zamyka grę; nie wykonuje wylogowania konta Microsoft.
- Mods pokazuje mody wykryte przez Fabric Loader.
- Style przełącza akcent, tapetę, płatki sakury, Dynamic Island i oznaczenia dodatkowych modów.

## Panel Visuals i sterowanie
- **Prawy Shift** — otwiera lub zamyka panel Visuals.
- **Escape** — zamyka menu pauzy i wraca do gry.
- Przełączniki Visuals są zapisywane do `config/genclient.json`.
- W Style użyj **EXPORT CONFIG**, aby zapisać kopię `config/genclient-export.json`.
- Aby zaimportować ustawienia, skopiuj plik eksportu jako `config/genclient-import.json` i naciśnij **IMPORT FILE**. Import waliduje znane pola, zapisuje aktywną konfigurację i odświeża przełączniki.

Dostępne efekty kosmetyczne:
- Jump Circle
- Glass Vapor
- Target ESP (informacja o obiekcie wskazanym celownikiem)
- Kill Effect
- Hit Sparks
- Custom Sky (dekoracyjne punkty świetlne nad graczem, nie podmiana renderera nieba)
- Glint Hand
- Aura Glint Light 3D
- Armor Glass Light 3D

## HUD
- Dynamic Island: zegar lokalny, FPS, adres serwera, ping i rzeczywisty stan muzyki Minecrafta (ON/OFF).
- Oznaczenia wykrytych modów Flashback i Mod Menu.
- Tygodniowy czas gry liczony podczas aktywnej, niepauzowanej sesji świata.

## Ostre renderowanie
Własne ekrany rysują swoje tła i panele. Nie wywołują waniliowego rozmycia tła menu Minecrafta. Tekst i przyciski są ostre; powierzchnie używają przezroczystych wypełnień i cienkich obramowań zamiast blur całego ekranu.

## Zakres
To mod client-side skupiony na interfejsie i efektach kosmetycznych. Nie zawiera automatyzacji walki, zmian zasięgu, bypassów antycheatów ani innych przewag gameplayowych.
