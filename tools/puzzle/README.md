# Narzedzia do zestawow DailyRap

Planowanie i walidacja zagadek dla trzech trybow:
- **RapWord** (`app/src/main/assets/words.json`) — jedno slowo dziennie
- **RapConnect** (`puzzles.json`) — 4 grupy po 4 slowa
- **RapCrossword** (`crosswords.json`) — haslo z zaznaczonych liter

Dopasowanie zagadki do dnia w apce dziala po polu `date` (jeden wpis na tryb na dzien).

## Wymagania
Node.js (jest zainstalowany). Brak zaleznosci npm — wszystko na bibliotekach wbudowanych.

## Panel (jeden interfejs, 3 zakladki)
Z katalogu glownego projektu:
```
.\puzzle.ps1 plan
```
Otwiera http://localhost:5173. Trzy zakladki w jednym miejscu:
- **Kalendarz** — status kazdego dnia (zielony=OK, czerwony=blad, szary=dziura); klik w dzien → podglad / edycja w kreatorze / przeniesienie / usuniecie.
- **Kreator** — maszyna do ukladania: formularz per tryb z walidacja na zywo (krzyzowka sprawdza litery na biezaco, Connections pilnuje 4x4 i unikalnosci) + przycisk **Wygeneruj przez AI**.
- **Walidacja** — przeglad wszystkich bledow w trzech plikach.

### Przycisk AI w przegladarce
Domyslnie przycisk wywoluje zainstalowane **Claude Code** (`claude -p`) i rozlicza sie z Twojej subskrypcji Claude — bez klucza API i bez kredytow. Wymaga tylko, zeby `claude` bylo w PATH i zalogowane (`claude auth status`). Jedno generowanie trwa zwykle od kilku sekund do minuty i zuzywa limit subskrypcji.

Ustawienia opcjonalne w pliku `tools/puzzle/.env` (jest w `.gitignore`, nie trafia do repo):
```
# model: alias (sonnet, opus) albo pelna nazwa; domyslnie sonnet
AI_MODEL=sonnet

# tryb platnego API zamiast subskrypcji (zuzywa kredyty z console.anthropic.com):
# AI_BACKEND=api
# ANTHROPIC_API_KEY=sk-ant-...
```
Uruchamiaj przez `.\puzzle.ps1 plan` (albo `npm run serve` w `tools/puzzle`). Samo `node server.js` pliku `.env` nie wczyta.

Nie ustawiaj `ANTHROPIC_API_KEY` jako globalnej zmiennej systemowej — Claude Code tez by ja wykryl i rozliczal sie z API zamiast subskrypcji. Generator w trybie `claude-code` sam usuwa ten klucz ze srodowiska wywolania.
Gdy Claude Code nie jest dostepne, przycisk jest nieaktywny (powod widac po najechaniu myszka) — reszta panelu (kreator reczny, kalendarz, walidacja) dziala normalnie. AI mozna tez wywolac w terminalu przez skill `/uloz-zestaw`.

## Walidacja (z linii polecen)
```
.\puzzle.ps1 check            # wszystkie pliki
.\puzzle.ps1 check crossword  # tylko jeden tryb
node tools/puzzle/validate.js # bezposrednio
```
Kod wyjscia 1 = sa bledy (przydatne w CI / pre-commit).

## Ukladanie zestawow przez Claude
W Claude Code:
```
/uloz-zestaw crossword 2026-07-01
```
Claude komponuje zestaw, sam waliduje (`add.js --check`) i zapisuje (`add.js`).
Definicja: `.claude/skills/uloz-zestaw/SKILL.md`.

## Twarde reguly zgodnosci
- **RapWord:** `variant` == liczba liter slowa.
- **RapConnect:** dokladnie 4x4; `difficulty` = {0,1,2,3} po jednym; 16 slow unikalnych.
- **RapCrossword:** liczba pytan == dlugosc hasla; dla pytania `i`: `answer[highlightIndex] == secretWord[i]`.

## Struktura
```
tools/puzzle/
  src/schema.js   - reguly walidacji (rdzen, wspoldzielony)
  src/io.js       - odczyt/zapis JSON + formatowanie (minimalne diffy)
  validate.js     - CLI walidator
  add.js          - walidacja + zapis jednego wpisu (uzywa skill)
  server.js       - lokalny serwer kalendarza
  public/index.html - UI kalendarza
```
