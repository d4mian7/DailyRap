# Dodatkowe funkcje — Pomoc i Ustawienia

## HomeScreen — nowe ikonki

Na samym dole ekranu głównego (pod timerem "Następna runda za"), dodaj dwie ikonki obok siebie, wyśrodkowane:
- ❓ (ikona znaku zapytania w kółku) — otwiera ekran Pomoc
- ⚙️ (ikona zębatki) — otwiera ekran Ustawienia
- Ikonki powinny być białe/szare (#9CA3AF), rozmiar 28dp, odstęp między nimi 32dp
- Pod ikonkami małe napisy: "Pomoc" i "Ustawienia" (font 10sp, szary)

---

## Ekran POMOC (HelpScreen)

### Nawigacja
- HomeScreen → kliknięcie ❓ → HelpScreen

### Layout główny
- Na górze: strzałka wstecz ← + tytuł "Pomoc" (duży font, biały)
- Trzy duże kafelki/przyciski z nazwami trybów (inspiracja: screeny z aplikacji Literalnie):
  - 🎤 RapConnect — fioletowy gradient tła
  - 🔤 RapWord — zielony gradient tła
  - 🧩 RapCrossword — złoty gradient tła
- Każdy kafelek po kliknięciu otwiera tutorial danego trybu
- Styl kafelków: zaokrąglone rogi 16dp, wysokość ~80dp, pełna szerokość, tekst biały bold wyśrodkowany

### Tutorial RapConnect (3 kroki, swipe)
Ekran pełnoekranowy, przesuwany palcem (HorizontalPager), na dole kropki postępu + przyciski "Pomiń" / "Dalej" / "Zamknij" (na ostatnim kroku).

**Krok 1:**
- Tekst: "Widzisz 16 słów na ekranie. Musisz pogrupować je w 4 ukryte kategorie po 4 słowa."
- Grafika: przykładowa siatka 4x4 z kafelkami słów (np. MATA, BEDOES, KIZO, SOBEL...)
- Na dole: "Zaznacz 4 słowa i kliknij SPRAWDŹ"

**Krok 2:**
- Tekst: "Po sprawdzeniu:"
- Grafika: kolorowy pasek kategorii (żółty, jak w NYT Connections) z nazwą "RAPERZY Z 2115" i słowami pod spodem
- Opis: "✅ Jeśli trafisz — kategoria się odsłania w kolorze"
- Grafika: kafelki trzęsące się (shake)
- Opis: "❌ Jeśli pudło — tracisz jedną z 4 prób"

**Krok 3:**
- Tekst: "Masz 4 próby na błąd. Odgadnij wszystkie 4 kategorie żeby wygrać!"
- Grafika: 4 kolorowe paski (żółty, zielony, niebieski, fioletowy) — wszystkie odgadnięte
- Opis: "Po grze możesz udostępnić wynik znajomym 🎤"

### Tutorial RapWord (3 kroki, swipe)
Inspiracja bezpośrednia ze screenów aplikacji Literalnie:

**Krok 1:**
- Tekst: "Odgadnij dzisiejsze rapowe słowo na RapWord!"
- Tekst: "Wpisz dowolne słowo i naciśnij OK, by spróbować odgadnąć hasło."
- Tekst: "Po każdej próbie, litery zostaną odpowiednio zaznaczone:"
- Grafika: przykładowe słowo w kratkach np. S-O-B-E-L, litera "O" podświetlona na ZIELONO
- Opis pod grafiką: "Jeśli litera podświetlona jest na zielono, występuje ona w haśle w tym samym miejscu"

**Krok 2:**
- Grafika: przykładowe słowo w kratkach np. D-O-M-E-K, litera "M" podświetlona na ŻÓŁTO
- Opis: "Jeśli litera podświetlona jest na żółto, występuje ona w haśle, lecz w innym miejscu"

**Krok 3:**
- Grafika: przykładowe słowo w kratkach np. G-R-A-C-Z, żadna litera nie podświetlona (wszystkie szare)
- Opis: "Jeśli litera nie jest podświetlona, nie występuje w haśle"
- Opis: "Codziennie dostępne jest nowe hasło. Masz od 5 do 7 prób w zależności od długości hasła. Powodzenia!"

### Tutorial RapCrossword (3 kroki, swipe)

**Krok 1:**
- Tekst: "Rozwiąż rapową krzyżówkę i odkryj ukryte hasło!"
- Grafika: przykładowa siatka krzyżówki z fioletową kolumną hasła
- Opis: "Hasło końcowe ukryte jest w fioletowej kolumnie"

**Krok 2:**
- Tekst: "Kliknij numer pytania, przeczytaj wskazówkę i wpisz odpowiedź."
- Grafika: przykładowe pytanie "Raper z Chillwagon, autor Disney" + pole tekstowe z "KIZO" + zielony wiersz
- Opis: "✅ Poprawna odpowiedź — wiersz podświetla się na zielono i odsłania literę hasła"

**Krok 3:**
- Tekst: "Odpowiedz na wszystkie pytania i odkryj ukryte hasło!"
- Grafika: wypełniona krzyżówka z odsłoniętym hasłem
- Opis: "Twój wynik to liczba poprawnych odpowiedzi. Ukryte hasło to bonus! 🧩"

### Styl tutoriali
- Ciemne tło (#1E1B2E), tekst biały, opisy szarym (#9CA3AF)
- Grafiki/przykłady wyśrodkowane, duże kratki z literami (jak na screenach z Literalnie)
- Litery w kratkach: zielona (#22C55E) = poprawne miejsce, żółta (#EAB308) = inne miejsce, szara (#3F3F46) = nie ma
- Kropki postępu na dole: aktywna = zielona, nieaktywna = szara
- Przyciski: "Pomiń" (szary outline, po lewej) + "Dalej" (szary filled, po prawej)
- Ostatni krok: tylko "Zamknij" (szary filled, pełna szerokość)

---

## Ekran USTAWIENIA (SettingsScreen)

### Nawigacja
- HomeScreen → kliknięcie ⚙️ → SettingsScreen

### Layout
- Na górze: strzałka wstecz ← + tytuł "Ustawienia" (duży font, biały)
- Lista opcji w stylu Material3:

### 1. Wibracje
- Switch on/off
- Opis: "Wibracja przy klikaniu klawiszy w RapWord"
- Domyślnie: WŁĄCZONE
- Po włączeniu: każde kliknięcie litery na klawiaturze w RapWord daje lekką wibrację (HapticFeedback, ~10ms)
- Dotyczy: klawiatura w RapWord (litery, OK, backspace) oraz kafelki w RapConnect (zaznaczanie)
- Zapisywane w DataStore/SharedPreferences

### 2. Powiadomienia
- Switch on/off
- Po włączeniu: pokazuje się picker godziny (np. domyślnie 12:00)
- Opis: "Przypomnienie o dzisiejszym wyzwaniu"
- Domyślnie: WYŁĄCZONE
- Treść powiadomienia: "🎤 Dzisiejsze wyzwanie czeka! Zagraj w DailyRap"
- Implementacja: WorkManager + NotificationChannel

### 3. Resetuj statystyki
- Przycisk (nie switch)
- Po kliknięciu: dialog potwierdzenia "Czy na pewno chcesz zresetować wszystkie statystyki? Ta akcja jest nieodwracalna."
- Dwa przyciski: "Anuluj" (szary) + "Resetuj" (czerwony)
- Po potwierdzeniu: wyczyść wszystkie wyniki z Room DB, pokaż Toast "Statystyki zresetowane"

### 4. Udostępnij aplikację
- Przycisk z ikonką 📤
- Po kliknięciu: Share Intent z tekstem "Sprawdź DailyRap — codzienne rapowe łamigłówki! 🎤🔥 [link do Play Store]"
- Na etapie MVP link może być placeholder np. "https://play.google.com/store/apps/details?id=com.dailyrap.app"

### 5. O aplikacji
- Sekcja na dole ekranu, oddzielona separatorem
- Wersja: "DailyRap v1.0"
- Autor: (Twoje imię/nick jeśli chcesz, lub puste)
- Kontakt: "dailyrap@email.com" (placeholder, kliknięcie otwiera mail)
- Link: "Oceń aplikację ⭐" — otwiera stronę Play Store

### Styl ustawień
- Ciemne tło (#1E1B2E)
- Każda opcja w wierszu: ikonka po lewej + nazwa + opis pod spodem + switch/przycisk po prawej
- Switche w kolorze fioletowym (#8B5CF6) gdy włączone
- Separatory między sekcjami (1dp, #3F3F46)
- Zaokrąglone rogi 12dp na elementach

---

## Struktura plików

### Nowe pliki:
```
ui/screen/
├── help/
│   ├── HelpScreen.kt              — ekran wyboru trybu
│   ├── HelpViewModel.kt
│   └── tutorial/
│       ├── RapConnectTutorial.kt   — 3 kroki swipe dla RapConnect
│       ├── RapWordTutorial.kt      — 3 kroki swipe dla RapWord
│       ├── RapCrosswordTutorial.kt — 3 kroki swipe dla RapCrossword
│       └── TutorialStep.kt         — reusable composable dla jednego kroku
├── settings/
│   ├── SettingsScreen.kt
│   └── SettingsViewModel.kt
```

### Nowe modele:
```
data/local/
└── SettingsDataStore.kt            — przechowywanie ustawień (DataStore)
```

### Nawigacja (NavGraph.kt):
- Dodaj route: "help", "help/rapconnect", "help/rapword", "help/rapcrossword", "settings"

---

## Ważne

- Package name: com.dailyrap.app
- Tutoriale powinny być swipe'owalne (HorizontalPager z Compose)
- Wibracje przez HapticFeedbackType.LightClick
- Powiadomienia przez WorkManager (PeriodicWorkRequest co 24h)
- Ustawienia zapisywane w DataStore (Jetpack)
- Styl spójny z resztą aplikacji (dark theme, fioletowy accent)
