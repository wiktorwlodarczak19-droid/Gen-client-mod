# GEN CLIENT 0.2.0

Client-side mod do **Minecraft Java 1.21.1 + Fabric**. Interfejs i grafiki bazują na przesłanym launcherze GEN CLIENT: Sakura Dark, szkło bez dodatkowego rozmycia, różowy akcent i tapety.

## Wymagania
- Minecraft Java 1.21.1
- Java 21
- Fabric Loader 0.16.10 lub nowszy zgodny z 1.21.1
- Fabric API dla Minecraft 1.21.1

## Instalacja
1. Zainstaluj Fabric dla Minecraft 1.21.1 i dodaj Fabric API.
2. Skopiuj `gen-client-0.2.0.jar` do folderu `mods`.
3. Uruchom profil Fabric.

## Sterowanie
- **Right Shift** — otwiera / zamyka Visuals w trakcie gry.
- **Esc** — wraca z paneli do poprzedniego ekranu; własne menu pauzy ma przycisk powrotu do gry.

## Co zawiera wersja 0.2.0
- Własne menu startowe GEN CLIENT i własne menu pauzy.
- Własny panel Visuals z ostrym tekstem i przyciskami w stylu Sakura Dark. Ekrany nie wywołują blur pipeline Minecrafta.
- Dynamic Island na HUD: zegar, FPS i adres serwera / tryb singleplayer.
- Style: tapety Sakura Dark i Midnight Ink oraz proceduralny Petal Mist, pięć akcentów kolorystycznych (Sakura, Pearl, Ice, Lavender, Mint), płatki sakury, Dynamic Island i odznaki wykrytych modów Flashback / Mod Menu.
- Zapisywanie ustawień w `config/genclient.json`.
- Działające efekty kosmetyczne: Jump Circle, Glass Vapor, Target ESP marker, Hit Sparks, Aura Glint i Armor Glass Light.

## Budowanie
Projekt używa standardowego przepływu Fabric Loom. Nie ma własnych zadań podmieniających wejście `remapJar`.

W katalogu projektu:

```bash
gradle --no-daemon clean build
```

Gotowy mod: `build/libs/gen-client-0.2.0.jar`.

## Zakres i uczciwe ograniczenia
- Target ESP w tej wersji jest dyskretnym markerem celu pod celownikiem, a nie podświetlaniem bytów przez ściany.
- Hit Sparks uruchamiają kosmetyczne cząsteczki przy próbie uderzenia w byt; nie ingerują w obrażenia ani zasięg.
- Armor Glass Light i Aura Glint używają cząsteczek, nie zmieniają modeli ani właściwości ekwipunku.
- Mod nie zawiera aimbota, kill-aury, auto-clickera, X-raya, exploitów ani obchodzenia zabezpieczeń serwerów.
