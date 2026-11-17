// Buduje SZKIC puli slow do krzyzowki: tools/puzzle/crossword-pool.json.
// Zrodlo: biala lista Damiana z SKILL.md (przepisana tu recznie z polskimi znakami) + odpowiedzi
// z dotychczasowych krzyzowek. Generator uzywa TYLKO slow ze statusem "ok".
//
// Uzycie:  node tools/puzzle/build-pool.js
// Istniejace decyzje Damiana (status/typ/notatka w crossword-pool.json) NIE sa nadpisywane.
import { readFileSync, writeFileSync, existsSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { readItems } from "./src/io.js";

const __dirname = dirname(fileURLToPath(import.meta.url));
const OUT = join(__dirname, "crossword-pool.json");

const up = (s) => String(s).toLocaleUpperCase("pl-PL");
// Tylko jedno slowo, same litery (bez cyfr i spacji), 3-9 liter - tak jak dotychczasowe odpowiedzi.
const asWord = (s) => {
  const w = up(s).replace(/[^A-ZĄĆĘŁŃÓŚŹŻ]/g, "");
  // Tytul z cyframi (Produkt47) pomijamy w calosci - po wycieciu cyfr wyszloby nieistniejace slowo.
  return w.length >= 3 && w.length <= 9 && !/[\s0-9]/.test(String(s).trim()) ? w : null;
};

// --- Biala lista Damiana (SKILL.md, 2026-10-27) ---
// Artysci: [nazwa, slowa do krzyzowki]. Slowa ogolne (YOUNG) pomijamy.
const ARTISTS = [
  ["Mata", ["MATA"]], ["Bedoes 2115", ["BEDOES"]], ["Young Leosia", ["LEOSIA"]], ["Bambi", ["BAMBI"]],
  ["Żabson", ["ŻABSON"]], ["Young Igi", ["IGI"]], ["Otsochodzi", ["OTSOCHODZI"]], ["OKI", ["OKI"]],
  ["Kizo", ["KIZO"]], ["Szpaku", ["SZPAKU"]], ["White 2115", ["WHITE"]], ["Sobel", ["SOBEL"]],
  ["Malik Montana", ["MALIK", "MONTANA"]], ["Gedz", ["GEDZ"]], ["Guzior", ["GUZIOR"]], ["Szczyl", ["SZCZYL"]],
  ["Dziarma", ["DZIARMA"]], ["Kinny Zimmer", ["KINNY", "ZIMMER"]], ["Schafter", ["SCHAFTER"]], ["Hubert.", ["HUBERT"]],
  ["Wane", ["WANE"]], ["Kuqe 2115", ["KUQE"]], ["Blacha 2115", ["BLACHA"]], ["Sentino", ["SENTINO"]],
  ["Chivas", ["CHIVAS"]], ["Beteo", ["BETEO"]], ["Fukaj", ["FUKAJ"]], ["ReTo", ["RETO"]],
  ["Kukon", ["KUKON"]], ["Taco Hemingway", ["TACO", "HEMINGWAY"]], ["Quebonafide", ["QUEBO"]], ["Białas", ["BIAŁAS"]],
  ["Solar", ["SOLAR"]], ["Paluch", ["PALUCH"]], ["Kaz Bałagane", ["KAZ", "BAŁAGANE"]], ["Pezet", ["PEZET"]],
  ["Gibbs", ["GIBBS"]], ["Borixon", ["BORIXON"]], ["Magik", ["MAGIK"]], ["Kuban", ["KUBAN"]], ["Young Multi", ["MULTI"]],
  ["Lanek", ["LANEK"]], ["Fagata", ["FAGATA"]],
];
const PRODUCERS = [["Deemz", ["DEEMZ"]], ["Lanek", ["LANEK"]], ["Kubi Producent", ["KUBI"]], ["@atutowy", ["ATUTOWY"]], ["Skolim", ["SKOLIM"]]];
// Plyty / projekty: [tytul, artysta]
const ALBUMS = [
  ["Rewolucja Romantyczna", "Bedoes 2115"], ["Produkt47", "OKI"], ["ERA47", "OKI"], ["Reklamacja47", "OKI"],
  ["RIOTT", "Otsochodzi"], ["Trappist", "Young Igi"], ["Konfetti", "Young Igi"], ["Amfisbena", "Żabson, Young Igi"],
  ["OIO", "OIO"], ["Letnisko", "Kinny Zimmer"], ["Audiotele", "Schafter"], ["Pleśń", "Guzior"], ["Marmur", "Taco Hemingway"],
  ["Wosk", "Taco Hemingway"], ["Posejdon", "Kizo"], ["Trapollo", "Żabson"], ["Soma", "Taconafide"],
];
// Utwory z playlisty „Rap sztosy” (selekcja Damiana) - tylko jednowyrazowe tytuly: [tytul, glowny wykonawca]
const TRACKS = [
  ["VHS", "PRO8L3M"], ["Kulig", "Kaz Bałagane"], ["Luty", "Otsochodzi"], ["Molly", "PRO8L3M"], ["Kamikaze", "Mata"],
  ["Fantazje", "MIÜ"], ["Baciata", "Malik Montana"], ["Interpol", "PRO8L3M"], ["Patolove", "Zdechły Osa"],
  ["Kyoto", "Kuban"], ["Drive", "Gibbs"], ["UA", "ReTo"], ["Mów", "Otsochodzi"], ["Plaster", "Szpaku"],
  ["Hinata", "Szpaku"], ["Tenten", "Wenext"], ["Bogini", "Quebonafide"], ["Lola", "Żabson"], ["Jagodzianki", "Malik Montana"],
  ["Nicea", "PRO8L3M"], ["Impreza", "Sobel"], ["Papierosy", "ReTo"], ["Cowabonga", "Żabson"], ["Miniówa", "Kukon"],
  ["Buzi", "Young Igi"], ["Przester", "Young Leosia"], ["Sextape", "Sobel"], ["BFF", "Bambi"], ["Landlord", "club2020"],
  ["Dresscode", "2115"], ["Dior", "Malik Montana"], ["Atlanta", "Aleshen"], ["Jeżyk", "OKI"], ["Bedoesiara", "2115"],
  ["Bossman", "ReTo"], ["Multisport", "Kaz Bałagane"], ["THC", "Young Igi"], ["Noradrenalina", "Quebonafide"],
  ["Cyrk", "Bambi"], ["Candy", "Quebonafide"], ["Morgan", "Bedoes 2115"], ["Riri", "White 2115"], ["Disney", "Kizo"],
  ["Vogue", "Bedoes 2115"], ["Bali", "Gedz"], ["Kosmita", "Gedz"], ["Taxi", "Kizo"], ["Jetlag", "Malik Montana"],
  ["Szklanki", "Young Leosia"], ["Frascati", "Taco Hemingway"], ["Rundki", "Young Leosia"], ["Janosik", "Gedz"],
  ["Bubbletea", "Quebonafide"], ["Futurama", "Quebonafide"], ["Szaman", "Bedoes 2115"], ["Drift", "Białas"],
];
// Sklady, labele, ekipy
const OTHER = [["SBM", "SBM Label"], ["OIO", "OIO"], ["PROSTO", "Prosto"], ["TACONAFIDE", null]];
// Odrzucone przez Damiana (SKILL.md: NIE mainstream, ❌ w rundach krzyzowek, artysci „NIE”, Lajzol nigdy)
const REJECTED = [
  "MIYABI", "ELO", "TOTORO", "MOMA", "ZDROWIA", "ATMOSFERA", "SMOLASTY", "MBAPPE", "LAJZA", "PIROMAN", "MIESO", "MIĘSO",
  "MAMI", "LUSTRO", "NIEBO", "PEŁNIA", "PELNIA", "KUBANCZYK", "KUBAŃCZYK", "AVI", "SARIUS", "KEKE", "TEDE", "PEJA", "OSTR",
  "LONA", "ŁONA", "SOKÓŁ", "SOKOL", "POPEK", "MIUOSH", "PAWBEATS", "ŁAJZOL", "LAJZOL",
  // utwory odrzucone z playlisty „Rap sztosy”
  "AFTER", "SOPOT", "BRUNETKI", "APOLLO", "MARBELLA", "KAWIOR", "IMAGO", "GUCCI", "AMG", "FALOCHRONY",
];

const pool = new Map();
const add = (word, type, artist, from, status = "ok", main = false) => {
  const w = asWord(word);
  if (!w || pool.has(w)) return;
  pool.set(w, { word: w, type, artist, status: REJECTED.includes(w) ? "odrzucone" : status, from, ...(main ? { main: true } : {}) });
};
// Pierwsze slowo artysty to jego glowna ksywa - tylko ona moze byc haslem glownym (nie MONTANA, ZIMMER).
ARTISTS.forEach(([name, words]) => words.forEach((w, i) => add(w, "artysta", name, "biala-lista", "ok", i === 0)));
PRODUCERS.forEach(([name, words]) => words.forEach((w) => add(w, "producent", name, "biala-lista")));
ALBUMS.forEach(([t, a]) => add(t, "album", a, "biala-lista"));
TRACKS.forEach(([t, a]) => add(t, "utwor", a, "biala-lista"));
OTHER.forEach(([t, a]) => add(t, "inne", a, "biala-lista"));

// Odpowiedzi z dotychczasowych krzyzowek opisane recznie (typ + artysta zrodlowy).
// Pewne -> "ok"; niepewne (stara scena, nieznany kontekst) -> "do-oceny" dla Damiana.
const SLANG = ["ALBUM", "BANGER", "BEAT", "BEEF", "BIBA", "BLOK", "BOOMBAP", "DISS", "DRILL", "DROP", "DZIARA", "EKIPA", "EPKA",
  "FEAT", "FLEX", "FLOW", "FREESTYLE", "GOAT", "HAJS", "HEJTER", "HIPHOP", "HIT", "HYPE", "KLIP", "KONCERT", "MELANŻ",
  "MIXTAPE", "MORDO", "NAWIJKA", "OSIEDLE", "PUNCHLINE", "RAP", "REFREN", "SAMPEL", "SCENA", "SCRATCH", "SINGIEL", "SKIT",
  "STUDIO", "SWAG", "TRAP", "TRASA", "WERS", "WINYL", "ZIOMAL", "ZWROTKA", "SIANO"];
SLANG.forEach((w) => add(w, "slang", null, "historia"));
// Fragmenty tytulow (pytanie w formie dopelnienia „Mlody …”) i skroty: [slowo, typ, artysta]
[["MANSON", "fragment tytulu", "Young Multi"], ["PALMA", "fragment tytulu", "Mata"], ["BELGA", "fragment tytulu", "Taco Hemingway"],
  ["DOMY", "fragment tytulu", "Hubert."], ["KOLOR", "fragment tytulu", "Otsochodzi"], ["HOTEL", "fragment tytulu", "SBM Label"],
  ["KWIAT", "fragment tytulu", "Bedoes 2115"], ["UMOWA", "fragment tytulu", "Taco Hemingway"], ["TARCHO", "fragment tytulu", "Otsochodzi"],
  ["MATCZAK", "fragment tytulu", "Mata"], ["OZDS", "album", "Bedoes 2115"], ["EGZOTYKA", "album", "Quebonafide"],
  ["BŁOTO", "utwor", "Wane"], ["OTSO", "artysta", "Otsochodzi"],
].forEach(([w, t, a]) => add(w, t, a, "historia"));
// Niepewne - do oceny Damiana (glownie starsza scena i slowa bez jasnego kontekstu).
["MEZO", "RAHIM", "SLUMS", "KALIBER", "FOKUS", "WDOWA", "LUKASYNO", "OKEKEL", "POLON", "TOMB", "NOON", "GUGU", "ZIPERA",
  "KINOL", "PAPUGA", "CARDIO", "HADES", "JARMARK", "KRÓL", "EUROPA", "FUTURA", "TYMEK"]
  .forEach((w) => add(w, "historia", null, "historia", "do-oceny"));

const history = readItems("crosswords");
history.forEach((cw) => {
  [cw.secretWord, ...cw.clues.map((c) => c.answer)].forEach((w) => add(w, "historia", null, "historia"));
});

// Zachowaj decyzje Damiana z poprzedniej wersji pliku.
if (existsSync(OUT)) {
  const prev = JSON.parse(readFileSync(OUT, "utf8")).words || [];
  prev.forEach((p) => {
    const cur = pool.get(p.word);
    if (cur) Object.assign(cur, { status: p.status, type: p.type, artist: p.artist, note: p.note });
    else pool.set(p.word, p);
  });
}

const words = [...pool.values()].sort((a, b) => a.type.localeCompare(b.type) || a.word.localeCompare(b.word, "pl"));
writeFileSync(OUT, JSON.stringify({ updated: new Date().toISOString().slice(0, 10), words }, null, 2) + "\n", "utf8");

const by = (k) => words.reduce((m, w) => ((m[w[k]] = (m[w[k]] || 0) + 1), m), {});
console.log(`Zapisano ${words.length} slow -> ${OUT}`);
console.log("wg typu:", by("type"));
console.log("wg statusu:", by("status"));
console.log("historia (typ do opisania):", words.filter((w) => w.type === "historia").map((w) => w.word).join(", "));
