// Walidacja + zapis pojedynczego zestawu. Uzywane przez skill /uloz-zestaw.
//
// Uzycie:
//   node add.js <word|connect|crossword> [--check] < entry.json
//   (JSON wpisu podawany na stdin)
//
//   --check  -> tylko waliduj, nie zapisuj
//   --force  -> pozwol nadpisac zestaw na zajetej dacie (bez tego: blad, kod 3)
//
// Mapowanie nazw przyjaznych -> pliki:
//   word      -> words.json      (RapWord)
//   connect   -> puzzles.json    (RapConnect)
//   crossword -> crosswords.json (RapCrossword)
import { validateEntry, crossCheck, validateArtists } from "./src/schema.js";
import { upsert, read, readItems, entryLabel, nextFreeDate } from "./src/io.js";

const ALIAS = { word: "words", words: "words", connect: "puzzles", puzzles: "puzzles", crossword: "crosswords", crosswords: "crosswords", artist: "artists", artists: "artists" };

const arg = process.argv[2];
const checkOnly = process.argv.includes("--check");
const force = process.argv.includes("--force");
const kind = ALIAS[arg];

if (!kind) {
  console.error("Podaj tryb: word | connect | crossword");
  process.exit(2);
}

const chunks = [];
for await (const c of process.stdin) chunks.push(c);
let entry;
try {
  entry = JSON.parse(Buffer.concat(chunks).toString("utf8"));
} catch (e) {
  console.error("Bledny JSON na wejsciu: " + e.message);
  process.exit(2);
}

const corpus = readItems(kind);
const cross = crossCheck(kind, entry, corpus);
// Kim jestem?: wpis bez puli dalby falszywe bledy, wiec sprawdza go dopiero walidacja calego pliku nizej.
const errs = [...(kind === "artists" ? [] : validateEntry(kind, entry)), ...cross.filter((e) => e.level === "error")];
// Kim jestem?: pula, cykl i powtorki tropow zaleza od calego pliku, wiec walidujemy go po zmianie.
if (kind === "artists") {
  const merged = [...corpus.filter((it) => it.date !== entry.date), { id: 0, ...entry }];
  errs.push(...validateArtists({ pool: read(kind).pool, artists: merged }).filter((e) => e.level === "error" && !/Duplikat id/.test(e.msg)));
}
if (errs.length) {
  console.error(`NIEZGODNY (${errs.length} bledow):`);
  errs.forEach((e) => console.error("  x " + e.where + ": " + e.msg));
  process.exit(1);
}
cross.filter((e) => e.level === "warn").forEach((e) => console.error("  ! " + e.where + ": " + e.msg));

// Kolizja daty: bez --force NIE nadpisujemy (po cichu zniknal tak zestaw Sentino).
const existing = corpus.find((it) => it.date === entry.date);
const conflictMsg = existing
  ? `Data ${entry.date} jest zajeta (${entryLabel(kind, existing)}). Pierwszy wolny dzien: ${nextFreeDate(corpus, entry.date)}. Nadpisanie tylko z --force.`
  : "";

if (checkOnly) {
  console.log(`OK — zestaw zgodny.${existing ? " UWAGA: " + conflictMsg : ""}`);
  process.exit(0);
}

if (existing && !force) {
  console.error("ZAJETA DATA: " + conflictMsg);
  process.exit(3);
}

upsert(kind, entry);
console.log(`Zapisano ${kind} na ${entry.date}.${existing ? " (nadpisano istniejacy)" : ""}`);
