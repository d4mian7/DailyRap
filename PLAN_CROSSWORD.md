# RapCrossword — Plan trzeciego trybu gry

## Koncept

Codzienna krzyżówka z hasłem o tematyce polskiego rapu nowej fali.
- 6-7 pytań rapowych dziennie
- Gracz wpisuje całe słowo jako odpowiedź na pytanie
- Po wpisaniu klika Sprawdź — poprawna odpowiedź = zielony wiersz, błędna = czerwony
- W każdej odpowiedzi jedna litera jest wyróżniona (pionowa kolumna)
- Z wyróżnionych liter składa się ukryte hasło końcowe (bonus)
- Wynik = ile odpowiedzi trafił (np. 5/7), hasło końcowe to bonus
- Bez limitu czasu i prób — gracz może zmieniać odpowiedzi
- Co 24h nowa krzyżówka

## Modele danych (data/model/)

### CrosswordPuzzle
- id: Int
- date: String (YYYY-MM-DD, null = dodatkowe)
- clues: List<CrosswordClue>
- secretWord: String (ukryte hasło z wyróżnionych liter)

### CrosswordClue
- question: String (pytanie/opis)
- answer: String (poprawna odpowiedź)
- highlightIndex: Int (indeks litery w odpowiedzi która jest częścią hasła, licząc od 0)

### CrosswordGameState
- puzzle: CrosswordPuzzle
- answers: Map<Int, String> (wpisane odpowiedzi per wiersz)
- checkedRows: Set<Int> (które wiersze sprawdzone)
- correctRows: Set<Int> (które wiersze poprawne)
- secretWordRevealed: Boolean
- status: GameStatus (IN_PROGRESS / WON / LOST)

## Room DB (data/local/entity/)

### CrosswordResultEntity
- date: String (PK)
- puzzleId: Int
- correctAnswers: Int
- totalClues: Int
- secretWordFound: Boolean

## JSON — assets/crosswords.json

Format:
```json
{
  "crosswords": [
    {
      "id": 1,
      "date": "2026-04-01",
      "secretWord": "SZPAKU",
      "clues": [
        {
          "question": "Raper z Chillwagon, autor Disney",
          "answer": "KIZO",
          "highlightIndex": 2
        },
        {
          "question": "Wytwórnia Solara i Białasa",
          "answer": "SBM",
          "highlightIndex": 2
        }
      ]
    }
  ]
}
```

Utwórz 5 przykładowych krzyżówek. Każda ma 6-7 pytań. Pytania dotyczą raperów, albumów, grup i slangu z bazy artystów (patrz PLAN.md sekcja 2). Zaznaczone litery (highlightIndex) muszą tworzyć sensowne hasło rapowe (pseudonim rapera, nazwa albumu lub grupy).

## Logika gry (domain/CrosswordEngine.kt)

- Ładuje krzyżówkę dnia z CrosswordRepository
- Gracz wpisuje odpowiedź w wiersz i klika Sprawdź
- Porównanie case-insensitive z uwzględnieniem polskich znaków (ĄĆĘŁŃÓŚŹŻ)
- Poprawna → wiersz zielony, litera hasła odsłonięta
- Błędna → wiersz czerwony, gracz może wpisać ponownie
- Gdy wszystkie wyróżnione litery odsłonięte → hasło odkryte (bonus animacja)
- Gra kończy się gdy gracz sprawdził wszystkie wiersze
- Wynik: X/7 poprawnych odpowiedzi + czy hasło odkryte

## Repozytorium (data/repository/)

### CrosswordRepository
- Ładuje krzyżówki z crosswords.json
- Zwraca krzyżówkę na dziś (wg daty, lub pierwszą dostępną jeśli brak na dziś)

## UseCase (domain/usecase/)

### GetDailyCrosswordUseCase
- Pobiera krzyżówkę dnia z CrosswordRepository

## UI (ui/screen/crossword/)

### CrosswordScreen.kt
- Tytuł: "RapCrossword" + pod spodem "Odgadnij hasła i odkryj ukryte słowo"
- Lista pytań z polami do wpisania
- Wyróżniona kolumna na hasło (fioletowe tło #8B5CF6)
- Na dole: odkrywane hasło (puste kratki → wypełniane po poprawnych odpowiedziach)

### CrosswordViewModel.kt
- Połączony z CrosswordEngine
- Trzyma stan gry

### components/ClueRow.kt
- Jeden wiersz: numer pytania + pytanie + pole tekstowe na odpowiedź + przycisk Sprawdź
- Po sprawdzeniu: zielone tło (poprawna) lub czerwone (błędna)
- Wyróżniona litera (highlightIndex) ma fioletowe tło

### components/SecretWordReveal.kt
- Na dole ekranu: kratki hasła końcowego
- Puste kratki wypełniają się animacją po poprawnych odpowiedziach
- Gdy całe hasło odkryte → animacja bonus (np. bounce + złoty kolor)

### components/CrosswordResultDialog.kt
- Wynik: "X/7 poprawnych odpowiedzi"
- Hasło: "SZPAKU 🔓" (jeśli odkryte) lub "Hasło nieodkryte 🔒"
- Lista pytań z poprawnymi odpowiedziami (żeby gracz widział rozwiązanie)
- Przycisk UDOSTĘPNIJ + WRÓĆ DO MENU

## HomeScreen

- Dodaj trzeci kafelek: 🧩 RAPCROSSWORD — "Codzienna krzyżówka" + streak + status
- Nawigacja Home → CrosswordScreen

## Statystyki

- Trzecia zakładka "Crossword" w StatsScreen
- Rozegrane, wygrane (5/7+ = wygrana), win %, streak
- Histogram poprawnych odpowiedzi (0/1/2/3/4/5/6/7)

## Udostępnianie wyników

Format kopiowany do schowka:
```
RapCrossword 🎤 #42
✅✅✅❌✅✅✅
Wynik: 6/7
Hasło: SZPAKU 🔓
```

## Styl

- Dark theme jak reszta apki (#1E1B2E tło)
- Wyróżniona kolumna hasła — fioletowe tło (#8B5CF6)
- Poprawny wiersz — zielony (#22C55E)
- Błędny wiersz — czerwony (#EF4444)
- Pytania czytelnym fontem, kratki odpowiedzi obok
- Klawiatura systemowa Androida do wpisywania
- Zaokrąglone rogi 12dp, spacing 8dp
- Animacje 300-400ms EaseInOut

## Ważne

- Package name: com.dailyrap.app
- Jeśli nie ma krzyżówki na dzisiejszą datę, załaduj pierwszą dostępną
- Po zakończeniu powinny działać 3 gry z HomeScreen: RapConnect, RapWord, RapCrossword
