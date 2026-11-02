# GEN CLIENT

Client-side mod do Minecraft Java **1.21.1** na **Fabric**.

## Wymagania
- Java 21
- Gradle 8.10.2
- Minecraft 1.21.1
- Fabric Loader 0.16.10 lub nowszy zgodny z 1.21.1
- Fabric API (wersja dla Minecraft 1.21.1)

## Budowanie
Projekt korzysta ze standardowego przepływu Fabric Loom:
1. `compileJava` i `processResources` przygotowują klasy oraz metadane moda.
2. `jar` tworzy archiwum developerskie.
3. `remapJar` tworzy końcowy JAR z nazwami klas wymaganymi przez Fabric.

Uruchom:
```bash
gradle --no-daemon clean build
```

Końcowy, instalowalny plik powinien znajdować się w:
`build/libs/gen-client-0.1.0.jar`

Nie używaj archiwum `*-dev.jar` ani `*-sources.jar` jako moda do instalacji.

## Sterowanie
- **Prawy Shift** — otwiera panel GEN CLIENT.

## Aktualny zakres
Repozytorium zawiera fundament moda: entrypoint Fabric, skrót klawiszowy, bazowy panel Visuals oraz pasek oznaczeń wykrytych modów Flashback / Mod Menu.

Przełączniki w panelu są obecnie szkieletem interfejsu — nie uruchamiają jeszcze docelowych efektów renderowania. Jump Circle, Glass Vapor, efekty 3D, docelowe menu liquid-glass i pozostałe planowane funkcje wymagają dalszej implementacji.
