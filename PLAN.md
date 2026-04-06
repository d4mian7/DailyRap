

RAP CONNECTIONS

\+

LITERALNIE RAPOWE

POLSKI RAP — NOWA FALA

Gra mobilna na Androida

Plan projektu i architektura aplikacji

Wersja: 5.0 — Dwie gry w jednej apce

Platforma: Android (Kotlin + Jetpack Compose) | IDE: Android Studio

Marzec 2026

&#x20;

1\. Opis projektu

Aplikacja zawiera dwie codzienne gry słowne o tematyce polskiego rapu nowej fali (ok. 2015–teraz). Obie gry są dostępne raz dziennie — każdego dnia nowe wyzwanie.

Gra 1: Rap Connections

Inspirowana NYT Connections. Gracz widzi 16 słów i musi pogrupować je w 4 ukryte kategorie po 4 elementy. Tematyka: raperzy, albumy, single, wytwyrnie, beefy, featy i współprace. Ma 4 próby na błąd.

Gra 2: Literalnie Rapowe

Inspirowana Wordle / Literalnie. Gracz odgaduje ukryte hasło związane z polskim rapem. Po każdej próbie litery podswietlają się: zielona = dobra litera na dobrym miejscu, żółta = dobra litera na złym miejscu, szara = litery nie ma w haśle. Gracz wpisuje dowolne słowo jako próbę (nie musi być z bazy rapowej).

3 warianty Literalnie (losowany codziennie):

Wariant	Długość hasła	Liczba prób

A	4 litery	5 prób

B	5 liter	6 prób

C	6 liter	7 prób



Zasada: długość hasła + 1 = liczba prób. Wariant losowany jest automatycznie każdego dnia — gracz nie wie z góry co go czeka.

Hasła — mix wszystkiego:

•	Pseudonimy raperów: MATA, SOBEL, FUKAJ, BETEO, SOLAR, BAMBI, CHIVAS, SZPAKU...

•	Nazwy albumów: Tijara, Marmur, Europa, POLON, FUTURA...

•	Slang rapowy: BEAT, FLOW, DISS, WERS

•	Inne rapowe słowa: KLIP, FEAT, TOUR, DROP, HYPE...

Grupa docelowa

•	Wiek: 14–28 lat

•	Fani aktualnego polskiego rapu (Spotify, YouTube, TikTok)

•	Słuchacze Maty, Bedoesa, Szpaku, Quebonafide, White 2115 i podobnych

•	Użytkownicy Androida szukający szybkiej codziennej rozrywki

Dlaczego dwie gry w jednej apce?

•	Gracz wraca 2x dziennie — raz na Connections, raz na Literalnie

•	Baza haseł do Literalnie jest dużo prostsza do tworzenia niż puzzle Connections

•	Dwie gry = dwa powody żeby polecić apkę znajomym

•	Zero konkurencji na Play Store — nikt nie ma rapowego Wordle + Connections

Funkcje MVP

•	Rap Connections Daily — codzienna runda (16 słów, 4 grupy, 4 błędy)

•	Literalnie Rapowe Daily — codzienne hasło (4/5/6 liter, losowy wariant)

•	System streaków — osobne streaki dla każdej gry

•	Udostępnianie wyników — emoji grid do wrzucenia na social media

•	Statystyki — osobne statystyki per gra (jeśli chodzi o wordle - czyli takie pokazywanie ile razy się zgadlo haslo w danym zakresie prób – od 1 do 7 – czyli takie statystyki co dzień aktualizowanie )

 

2\. Baza artystów

Pełna lista raperów i projektów, na których oparta jest gra. Tylko nowa fala polskiego rapu (ok. 2015–teraz).

Raperzy (26)

\#	Artysta	Kontekst

1	Mata	Topowy raper, Patointeligencja, Młody Matczak, 100 dni do matury

2	Quebonafide	Romantic Psycho, Egzotyka, członek Taconafide, Mochi Miss Ti

3	Bedoes 2115	Kwiat Polskiej Młodzieży, SBM Label, 2115, Opowieści z Doliny Smoków

4	Białas	Hotel Maffija, SBM Label, H8M5, Polon, Solar

5	Szpaku	Atypowy, BOR, GUGU

6	Young Leosia	Raperka nowej fali, PG$, Szklanki

7	Otsochodzi	Nowy Kolor, Nie/nie, OIO

8	Taco Hemingway	Europa, Trójkąt Warszawski, Café Belga, członek Taconafide

9	Sobel	Melodic rap, Fiołkowe Pole, Pop/rap, Pułapka na motyle

10	Kizo	Disney, Chillwagon, Posejdon

11	White 2115	Rockstar, California, 2115, SBM Label

12	Young Igi	OIO, Bestia, Konfetti

14	Malik Montana	Trap, drill, GM2L, Naaajak

15	Żabson	Chillwagon, Holllywood Smile, Internaziomal

16	Chivas	Rap z elementami rocka, Deathcore, GUGU

17	OKI	47, ERA47, PRODUKT47, Jeżyk

18	Guzior	Evil\_Things, Pleśń, Blueberry

19	Bambi	PG$, raperka, współpraca z Young Leosią, IRL, Woda Księżycowa

20	Beteo	SBM Label, Hotel Maffija, indywidualny

21	Solar	Hotel Maffija, SBM Label, Białas

22	Kinny Zimmer	SBM Label, Hotel Maffija, Letnisko

23	Schafter	Futura, Audiotele, hot coffee, hors d'oeuvre

24	Young Multi	Plecak, Trap, Nowa Fala, Toxic

25	Kuqe 2115	2115, Bedoes 2115, KETCHUP

26	Fukaj	SBM Label, Rock, Woda Księżycowa, Hotel Maffija

 

Grupy / projekty / wytwyrnie (baza do pytań)

Nazwa	Typ	Członkowie / opis

Taconafide	Duet	Taco Hemingway + Quebonafide

Chillwagon	Kolektyw	Kizo, Borixon, Qry, Żabson, ZetHa, ReTo

PRO8L3M	Duet	Steez + Oskar83

OIO	Kolektyw	OKI, Young Igi, Otsochodzi

2115	Kolektyw	Bedoes 2115, White 2115, Kuqe 2115, Blacha 2115, Flexxy 2115

SBM Label	Wytwyrnia	Solar, Białas, Bedoes 2115, White 2115, Beteo, Mata, Lanek, Kinny Zimmer, Fukaj, Janusz Walczuk, Kacperczyk, Jan-rapowanie

PG$	Duet	Bambi + Young Leosia

Popkiller Młode Wilki	Projekt	Coroczny cykl z młodymi raperami

2020	Wytwyrnia	OKI, Taco Hemingway, Otsochodzi, PRO8L3M

 

3\. Stos technologiczny

Komponent	Technologia	Opis

Język	Kotlin	Oficjalny język Androida

UI Framework	Jetpack Compose	Deklaratywne UI, animacje, stany

Architektura	MVVM + Clean Architecture	Separacja warstw, testowalność

Nawigacja	Compose Navigation	Nawigacja między ekranami

Baza danych	Room (SQLite)	Puzzle-e, hasła i statystyki

DI	Hilt (Dagger)	Wstrzykiwanie zależności

Serializacja	Kotlinx Serialization	Parsowanie JSON

Animacje	Compose Animation API	Płynne animacje kafelków i liter

Min SDK	API 26 (Android 8.0)	Pokrycie \~95% urządzeń



Na etapie MVP nie potrzebujemy backendu. Wszystkie dane (puzzle Connections + hasła Literalnie) będą w plikach JSON w assets/. Backend dodamy później.

 

4\. Struktura projektu (Android Studio)

Główny pakiet: com.rapconnections.app

app/src/main/java/com/rapconnections/app/

├── di/

│   ├── AppModule.kt

│   └── DatabaseModule.kt

├── data/

│   ├── local/

│   │   ├── AppDatabase.kt

│   │   ├── DailyResultDao.kt

│   │   └── entity/

│   │       ├── ConnectionsResultEntity.kt

│   │       └── LiteralnieResultEntity.kt

│   ├── repository/

│   │   ├── PuzzleRepository.kt          ← Connections puzzle-e

│   │   ├── WordRepository.kt            ← Literalnie hasła

│   │   └── StatsRepository.kt

│   └── model/

│       ├── Puzzle.kt                    ← Connections

│       ├── Category.kt

│       ├── DailyWord.kt                 ← Literalnie

│       ├── LetterResult.kt              ← CORRECT/PRESENT/ABSENT

│       ├── ConnectionsGameState.kt

│       ├── LiteralnieGameState.kt

│       └── PlayerStats.kt

├── domain/

│   ├── ConnectionsEngine.kt         ← Logika Connections

│   ├── LiteralnieEngine.kt          ← Logika Literalnie

│   └── usecase/

│       ├── GetDailyPuzzleUseCase.kt

│       ├── GetDailyWordUseCase.kt

│       └── SaveGameResultUseCase.kt

├── ui/

│   ├── theme/

│   │   ├── Color.kt

│   │   ├── Type.kt

│   │   └── Theme.kt

│   ├── navigation/

│   │   └── NavGraph.kt

│   ├── screen/

│   │   ├── home/

│   │   │   ├── HomeScreen.kt

│   │   │   └── HomeViewModel.kt

│   │   ├── connections/                 ← Ekran Connections

│   │   │   ├── ConnectionsScreen.kt

│   │   │   ├── ConnectionsViewModel.kt

│   │   │   └── components/

│   │   │       ├── WordGrid.kt

│   │   │       ├── WordTile.kt

│   │   │       ├── CategoryReveal.kt

│   │   │       ├── MistakesIndicator.kt

│   │   │       └── ResultsDialog.kt

│   │   ├── literalnie/                  ← Ekran Literalnie

│   │   │   ├── LiteralnieScreen.kt

│   │   │   ├── LiteralnieViewModel.kt

│   │   │   └── components/

│   │   │       ├── LetterGrid.kt            ← Siatka liter (próby)

│   │   │       ├── LetterTile.kt            ← Pojedyncza litera

│   │   │       ├── Keyboard.kt              ← Klawiatura QWERTY

│   │   │       └── LiteralnieResultDialog.kt

│   │   └── stats/

│   │       ├── StatsScreen.kt

│   │       └── StatsViewModel.kt

│   └── components/

│       ├── RapButton.kt

│       └── TopBar.kt

├── util/

│   ├── DateUtils.kt

│   └── ShareUtils.kt

└── RapConnectionsApp.kt



Zasoby

app/src/main/

├── assets/

│   ├── puzzles.json                 ← Baza puzzle-i Connections

│   └── words.json                   ← Baza hasł Literalnie

└── res/

&#x20;   ├── values/colors.xml

&#x20;   ├── values/strings.xml

&#x20;   ├── font/                        ← Montserrat

&#x20;   ├── drawable/

&#x20;   └── mipmap/

 

5\. Modele danych

Connections

Model	Pola	Opis

Puzzle	id: Int, date: String, categories: List<Category>	Jedna runda Connections

Category	name: String, words: List<String>, difficulty: Int	Kategoria (0=łatwa, 3=podchwytliwa)

ConnectionsGameState	puzzle, remainingWords, solvedCategories, selectedWords, mistakesLeft, guessHistory, status	Stan gry Connections



Literalnie

Model	Pola	Opis

DailyWord	id: Int, date: String, word: String, hint: String, variant: Int	Hasło dnia (variant: 4/5/6 liter)

LetterResult	enum: CORRECT, PRESENT, ABSENT	Wynik jednej litery

LiteralnieGameState	dailyWord, guesses: List<String>, maxGuesses: Int, currentGuess: String, status	Stan gry Literalnie



Wyniki (Room DB)

Entity	Pola	Opis

ConnectionsResultEntity	date (PK), puzzleId, won, mistakes, timeSeconds, guessHistory	Wynik Connections

LiteralnieResultEntity	date (PK), wordId, won, guessCount, variant, guessHistory	Wynik Literalnie

 

6\. Format plików JSON

puzzles.json (Connections)

{

&#x20; "puzzles": \[

&#x20;   {

&#x20;     "id": 1, "date": "2026-04-01",

&#x20;     "categories": \[

&#x20;       { "name": "Członkowie Chillwagon",

&#x20;         "words": \["Kizo", "ReTo", "Żabson", "Borixon"], "difficulty": 0 },

&#x20;       { "name": "Raperzy z 2115",

&#x20;         "words": \["Bedoes", "White", "Kuqe", "Blacha"], "difficulty": 1 },

&#x20;       { "name": "OIO",

&#x20;         "words": \["OKI", "Young Igi", "Otsochodzi", "@atutowy"], "difficulty": 2 },

&#x20;       { "name": "Albumy Taco Hemingwaya",

&#x20;         "words": \["Europa", "Café Belga", "Trójkąt Warszawski", "Marmur"], "difficulty": 3 }

&#x20;     ]

&#x20;   }

&#x20; ]

}



words.json (Literalnie)

{

&#x20; "words": \[

&#x20;   { "id": 1, "date": "2026-04-01", "word": "SOBEL", "hint": "Raper", "variant": 5 },

&#x20;   { "id": 2, "date": "2026-04-02", "word": "GUGU", "hint": "Album", "variant": 4 },

&#x20;   { "id": 3, "date": "2026-04-03", "word": "SZPAKU", "hint": "Raper", "variant": 6 },

&#x20;   { "id": 4, "date": "2026-04-04", "word": "BEATS", "hint": "Slang", "variant": 5 },

&#x20;   { "id": 5, "date": "2026-04-05", "word": "FLOW", "hint": "Slang", "variant": 4 },

&#x20;   { "id": 6, "date": "2026-04-06", "word": "FUKAJ", "hint": "Raper", "variant": 5 },

&#x20;   { "id": 7, "date": "2026-04-07", "word": "BETEO", "hint": "Raper", "variant": 5 },

&#x20;   { "id": 8, "date": "2026-04-08", "word": "MOCHI", "hint": "Album", "variant": 5 },

&#x20;   { "id": 9, "date": "2026-04-09", "word": "BAMBI", "hint": "Raperka", "variant": 5 },

&#x20;   { "id": 10, "date": "2026-04-10", "word": "POLON", "hint": "Album", "variant": 5 }

&#x20; ]

}



Każde hasło ma hint (podpowiedź kategorii: Raper/Album/Slang) — można wyświetlić na górze ekranu. Wariant (4/5/6) określa długość hasła i liczbę prób.

 

7\. Ekrany aplikacji

7.1 Ekran główny (HomeScreen)

Główne menu z dwoma grami do wyboru.

•	Logo "Rap Connections" u góry

•	Dwa duże kafelki/przyciski:

◦	  🟣 RAP CONNECTIONS — "Codzienna runda" + streak + status (zagrane/niezagrane)

◦	  🟢 LITERALNIE RAPOWE — "Dzisiejsze hasło: X liter" + streak + status

•	Przycisk "STATYSTYKI" na dole

•	Odliczanie do następnej rundy (wspólne dla obu gier)

7.2 Ekran Connections (ConnectionsScreen)

Bez zmian vs poprzednia wersja — siatka 4x4, zaznaczanie, sprawdzanie, animacje.

•	Siatka 4x4 z kafelkami słów

•	Przyciski: TASUJ, WYCZYŚĆ, SPRAWDŹ

•	4 kropki błędów

•	Animacje: shake (błąd), reveal (trafienie), "Prawie!" (3/4)

•	ResultsDialog z emoji grid na końcu

7.3 Ekran Literalnie (LiteralnieScreen)

Nowy ekran — rapowa wersja Wordle.

Góra ekranu:

•	Tytuł: "Literalnie Rapowe #X"

•	Info: "X liter | Y prób" (np. "5 liter | 6 prób")

•	Hint (podpowiedź): "Kategoria: Raper" lub "Kategoria: Album"

Siatka liter:

•	5-7 wierszy (zależnie od wariantu) x 4-6 kolumn

•	Każdy wiersz = jedna próba

•	Litery kolorują się po zatwierdzeniu: zielona / żółta / szara

•	Animacja flip (obrót kafelka przy odsłanianiu koloru)

Klawiatura:

•	QWERTY na dole ekranu (polski układ z ĄĆĘŁŃÓŚŹŻ)

•	Klawisze kolorują się po użyciu (zielony/żółty/szary)

•	Przycisk ENTER (zatwierdź) i BACKSPACE (cofnij)

Animacje:

•	Flip animation na literach po zatwierdzeniu próby

•	Bounce animation po wpisaniu litery

•	Shake animation gdy słowo za krótkie / za długie

7.4 Ekran statystyk (StatsScreen)

Dwie zakładki: Connections | Literalnie

•	Connections: rozegrane, wygrane, win %, streak, histogram błędów

•	Literalnie: rozegrane, wygrane, win %, streak, histogram prób (1/2/3/4/5/6/7)

 

8\. Logika gry

8.1 ConnectionsEngine (bez zmian)

1\.	Załaduj puzzle wg daty

2\.	Potasuj 16 słów

3\.	Gracz zaznacza 4 i klika SPRAWDŹ

4\.	Sprawdz: trafienie / pudło / prawie (3/4)

5\.	4 kategorie = WON, 0 prób = LOST

8.2 LiteralnieEngine (nowe)

6\.	Załaduj hasło dnia (DailyWord) wg daty

7\.	Ustaw wariant: 4/5/6 liter → 5/6/7 prób

8\.	Gracz wpisuje słowo i klika ENTER

9\.	Walidacja: czy słowo ma odpowiednią długość? (jeśli nie → shake)

10\.	Porównaj każdą literę z hasłem:

•	Litera na dobrym miejscu → CORRECT (zielona)

•	Litera jest w haśle ale na złym miejscu → PRESENT (żółta)

•	Litery nie ma w haśle → ABSENT (szara)

11\.	Zaktualizuj kolory klawiatury

12\.	Słowo == hasło → WON

13\.	Brak prób → LOST (odsłoń hasło)

14\.	Zapisz wynik

Obsługa polskich znaków

Ważne: hasła i próby muszą obsługiwać polskie znaki (Ą, Ć, Ę, Ł, Ń, Ó, Ś, Ź, Ż). Porównanie liter musi być case-insensitive i uwzględniać polskie znaki. Klawiatura na ekranie musi mieć polskie litery.

 

9\. Przykładowe puzzle-e Connections

Puzzle-e Connections zostały poprawione przez autora projektu. Poniżej aktualna wersja:

Runda #1

Kolor	Kategoria	Słowa

🟢 Łatwa	Członkowie Chillwagon	Kizo, ReTo, Żabson, Borixon

🟡 Średnia	Raperzy z 2115	Bedoes, White, Kuqe, Blacha

🟠 Trudna	OIO	OKI, Young Igi, Otsochodzi, @atutowy

🔴 Podchwytliwa	Albumy Taco Hemingwaya	Europa, Café Belga, Trójkąt Warszawski, Marmur



Runda #2

Kolor	Kategoria	Słowa

🟢 Łatwa	Raperzy z SBM Label	Beteo, Solar, Kinny Zimmer, White 2115

🟡 Średnia	Duety / grupy rapowe	Taconafide, PRO8L3M, PG$, OIO

🟠 Trudna	Albumy Quebonafide	Romantic Psycho, Egzotyka, Ezoteryka, Taconafide

🔴 Podchwytliwa	Viral hit na TikToku	Mata, Young Leosia, OKI, Malik Montana



Runda #3

Kolor	Kategoria	Słowa

🟢 Łatwa	Rapowe hity 2022	Jeżyk, Jetlag, KETCHUP, JESTEM POJ384NY

🟡 Średnia	Raperzy trap/drill	Malik Montana, Young Multi, Otsochodzi, Young Igi

🟠 Trudna	Skróty albumów Bedoesa 2115	KPM, AŚMZ, RR, OZDS

🔴 Podchwytliwa	Członkowie 2020label	Otsochodzi, Taco Hemingway, OKI, PRO8L3M



Runda #4

Kolor	Kategoria	Słowa

🟢 Łatwa	Featy z ERA47	Sobel, Otsochodzi, Oskar83, Kuban

🟡 Średnia	Raperzy z Hotel Maffija	Fukaj, Mata, Beteo, Kinny Zimmer

🟠 Trudna	Uczestnicy albumu club2020	Schafter, Young Leosia, Young Igi, Szczyl

🔴 Podchwytliwa	Featy ERA47	Sobel, Otsochodzi, Oskar83, Kuban



Runda #5

Kolor	Kategoria	Słowa

🟢 Łatwa	Utwory Otsochodzi	RAP, Mów, SMS, KFC

🟡 Średnia	Featy z Młody Matczak	White 2115, Popek, Taco Hemingway, Quebonafide

🟠 Trudna	Raperzy z Popkiller Młode Wilki	Bedoes 2115, ReTo, Żabson, White 2115

🔴 Podchwytliwa	Single Żabsona z 2025 roku	BOUNCE BACK, FUTURE TRUNKS, MAGIC CITY, I00

 

10\. Przykładowe hasła Literalnie

Przykładowa baza 30 hasł na pierwszy miesiąc. Mix pseudonimów, albumów i slangu.

Hasła 4-literowe (wariant A: 5 prób)

Hasło	Kategoria	Podpowiedź

MATA	Raper	Topowy raper nowej fali

GUGU	Album	Stara wytwórnia Szpaka

FLOW	Slang	Sposób rapowania

DISS	Slang	Utwór obrażający innego rapera

FEAT	Slang	Współpraca na utworze

HYPE	Slang	Ekscytacja, rozgłos

DROP	Slang	Premiera utworu/albumu

HOOK	Slang	Refren w piosence

KIZO	Raper	Raper z Chillwagon i 2115

KLIP	Slang	Teledysk

WANE	Raper	Odkrycie 2025 nowej fali



Hasła 5-literowe (wariant B: 6 prób)

Hasło	Kategoria	Podpowiedź

SOBEL	Raper	Melodic rap, Fiołkowe Pole

FUKAJ	Raper	SBM Label, Hotel Maffija

BETEO	Raper	SBM Label, indywidualny

BAMBI	Raperka	PG$, współpraca z Young Leosią

SOLAR	Raper	Hotel Maffija, SBM Label

POLON	Album	Album Białasa

QUEBO	Raper	Kuba z Ciechanowa

VERSE	Slang	Zwrotka w utworze

KUBAN	Raper	Jakub z Opoczna

PEZET	Raper	Paweł z Ursynowa

ALBUM	Slang	Wydawnictwo muzyczne

KUKON	Raper	Raper, autor serii Ogrody



Hasła 6-literowe (wariant C: 7 prób)

Hasło	Kategoria	Podpowiedź

SZPAKU	Raper	Atypowy, BOR, GUGU

GUZIOR	Raper	Autor Evil\_Things 

CHIVAS	Raper	Deathcore, rap z elementami rocka

EUROPA	Album	Album Taco Hemingwaya

FUTURA	Album	Album Schaftera

BESTIA	Album	Utwór Young Igiego

DISNEY	Album	Utwór Kizo

REFREN	Slang	Powtarzająca się część utworu

BANGER	Slang	Utwór który grzeje

PALUCH	Raper	Raper z Poznania, lider BOR-u

BIAŁAS	Raper	Założyciel SBM Label

BEDOES	Raper	Lider 2115

ŻABSON	Raper	Mateusz z Opoczna

 

11\. Design i kolorystyka

Paleta kolorów

Element	Kolor	Hex

Tło główne	Ciemny fiolet	#1E1B2E

Kafelek Connections (domyślny)	Ciemny szary	#2D2B3D

Kafelek Connections (zaznaczony)	Fioletowy	#8B5CF6

Kafelek tekst	Biały	#FFFFFF

Kategoria Łatwa / Litera CORRECT	Zielony	#22C55E

Kategoria Średnia / Litera PRESENT	Złoty	#EAB308

Kategoria Trudna	Pomarańczowy	#F97316

Kategoria Podchwytliwa	Czerwony	#EF4444

Litera ABSENT	Ciemny szary	#3F3F46

Accent / CTA	Neonowy fiolet	#A855F7

Tekst drugorzędny	Szary	#9CA3AF



Typografia i styl

•	Font: Montserrat (nagłówki, kafelki) + Inter (body)

•	Dark theme — klimat hip-hopowy

•	Zaokrąglone rogi: 12dp, spacing: 8dp

•	Animacje: 300–400ms, EaseInOut

•	Literalnie: litery w kwadratowych kafelkach (jak Wordle)

 

12\. Harmonogram realizacji MVP

Tydzień	Zadania	Efekt

1 — Setup	Projekt, Hilt, Room, Navigation. Modele danych (oba tryby). puzzles.json + words.json. Repozytoria.	Projekt kompiluje się, dane ładują się.

2 — Logika	ConnectionsEngine + LiteralnieEngine. UseCases. Testy jednostkowe obu silników.	Obie logiki działają w testach.

3 — UI Connections	HomeScreen (z dwoma grami). ConnectionsScreen + komponenty.	Można grać w Connections.

4 — UI Literalnie	LiteralnieScreen + LetterGrid + Keyboard + animacje flip.	Można grać w Literalnie.

5 — Polish	Animacje, ResultsDialogi, Streaki, StatsScreen (2 zakładki), udostępnianie.	Pełne MVP z animacjami.

6 — Release	30+ puzzle-i + 30+ hasł. Bugfixy. Testy. Ikona, screenshoty, Play Store.	Publikacja!



Uwaga: harmonogram wydłużył się o 1 tydzień (z 5 do 6) ze względu na dodanie drugiego trybu gry.

 

13\. Zależności Gradle

Plik build.gradle.kts (moduł :app) — bez zmian vs poprzednia wersja:

plugins {

&#x20;   id("com.android.application")

&#x20;   id("org.jetbrains.kotlin.android")

&#x20;   id("com.google.devtools.ksp")

&#x20;   id("com.google.dagger.hilt.android")

&#x20;   id("org.jetbrains.kotlin.plugin.serialization")

}



android {

&#x20;   namespace = "com.rapconnections.app"

&#x20;   compileSdk = 35

&#x20;   defaultConfig {

&#x20;       applicationId = "com.rapconnections.app"

&#x20;       minSdk = 26

&#x20;       targetSdk = 35

&#x20;       versionCode = 1

&#x20;       versionName = "1.0"

&#x20;   }

}



dependencies {

&#x20;   implementation(platform("androidx.compose:compose-bom:2024.12.01"))

&#x20;   implementation("androidx.compose.ui:ui")

&#x20;   implementation("androidx.compose.material3:material3")

&#x20;   implementation("androidx.compose.ui:ui-tooling-preview")

&#x20;   implementation("androidx.compose.animation:animation")

&#x20;   implementation("androidx.activity:activity-compose:1.9.3")

&#x20;   implementation("androidx.navigation:navigation-compose:2.8.5")

&#x20;   implementation("androidx.room:room-runtime:2.6.1")

&#x20;   implementation("androidx.room:room-ktx:2.6.1")

&#x20;   ksp("androidx.room:room-compiler:2.6.1")

&#x20;   implementation("com.google.dagger:hilt-android:2.51.1")

&#x20;   ksp("com.google.dagger:hilt-compiler:2.51.1")

&#x20;   implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

&#x20;   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

&#x20;   testImplementation("junit:junit:4.13.2")

&#x20;   androidTestImplementation("androidx.compose.ui:ui-test-junit4")

}

 

14\. Format udostępniania wyników

Connections:

Rap Connections 🎤 #42

🟩🟩🟩🟩

🟧🟨🟨🟨

🟨🟨🟨🟨

🟥🟥🟥🟥

Błędy: 1/4 | Czas: 2:34



Literalnie:

Literalnie Rapowe 🎤 #42 (5 liter)

⬛⬛🟨⬛⬛

⬛🟩⬛⬛🟨

🟩🟩🟩🟩🟩

Próby: 3/6

 

15\. Co dalej po MVP?

•	Rap zagraniczny — dodatkowe bazy hasł i puzzle-i z rapem zagranicznym

•	Stary polski rap — Paktofonika, Molesta, Hemp Gru... dla nostalgików

•	Backend API — nowe puzzle-e i hasła bez aktualizacji apki

•	Powiadomienia push — przypomnienie o codziennych grach

•	Ranking globalny — Firebase Leaderboard (osobny per gra)

•	Multiplayer — kto szybciej rozwiąże

•	Tryb Hard (Literalnie) — musisz używać zielonych liter w kolejnych próbach

•	Hints — podpowiedzi za reklamę lub monety

•	Sound effects — scratch, boom bap, ad-libs

•	Monetyzacja — reklamy + premium za 9.99 PLN

•	Widget Android — status obu gier na ekranie głównym



Dwie gry, jedna apka, zero konkurencji. Lecimy! 🎤🔥



