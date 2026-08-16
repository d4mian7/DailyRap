# DailyRap 🎤

Codzienne gry słowne o polskim rapie nowej fali — natywna aplikacja Android napisana w **Kotlinie + Jetpack Compose**.

Każdego dnia nowe wyzwanie w trzech trybach:

| Tryb | Inspiracja | Zasady |
|---|---|---|
| **Rap Connections** | NYT Connections | 16 haseł → znajdź 4 ukryte kategorie po 4 elementy (raperzy, albumy, wytwórnie, beefy, featy). 4 próby na błąd. |
| **Literalnie Rapowe** | Wordle / Literalnie | Odgadnij hasło rapowe (pseudonim, album, slang). 3 warianty długości (4–6 liter), liczba prób = długość + 1. Wariant losowany codziennie. |
| **Krzyżówka rapowa** | klasyczna krzyżówka | Codzienna krzyżówka z hasłami ze świata polskiego rapu. |

Do tego: statystyki gracza (serie, skuteczność), ekran pomocy, ustawienia i reklamy z nagrodą (AdMob — obecnie testowe ID Google).

## Stack

- **Kotlin + Jetpack Compose** (Material 3)
- Architektura ekranowa per tryb gry (`ui/screen/{connections,literalnie,crossword,home,stats,...}`)
- Dane gier w JSON (`app/src/main/assets/`) — osobne bazy haseł dla każdego trybu
- Google AdMob (rewarded + banner, testowe identyfikatory)
- Narzędzia deweloperskie: skrypty PowerShell do generowania łamigłówek (`tools/`, `puzzle.ps1`)

## Build

```bash
./gradlew assembleDebug
```

Wymaga Android Studio / SDK (min. konfiguracja w `gradle/libs.versions.toml`).

## Status

Projekt osobisty rozwijany hobbystycznie (2026). Wersja 5.0 — dwie gry dzienne + krzyżówka; w planach publikacja w Google Play.

---

*Autor: Damian Robaczewski*
