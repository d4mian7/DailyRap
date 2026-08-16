// Walidacja + zapis pojedynczego zestawu. Uzywane przez skill /uloz-zestaw.
//
// Uzycie:
//   node add.js <word|connect|crossword> [--check] < entry.json
//   (JSON wpisu podawany na stdin)
//
//   --check  -> tylko waliduj, nie zapisuj
//
// Mapowanie nazw przyjaznych -> pliki:
//   word      -> words.json      (RapWord)
//   connect   -> puzzles.json    (RapConnect)
//   crossword -> crosswords.json (RapCrossword)
import { validateEntry, crossCheck } from "./src/schema.js";
import { upsert, readItems } from "./src/io.js";

const ALIAS = { word: "words", words: "words", connect: "puzzles", puzzles: "puzzles", crossword: "crosswords", crosswords: "crosswords" };

const arg = process.argv[2];
const checkOnly = process.argv.includes("--check");
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
const errs = [...validateEntry(kind, entry), ...cross.filter((e) => e.level === "error")];
if (errs.length) {
  console.error(`NIEZGODNY (${errs.length} bledow):`);
  errs.forEach((e) => console.error("  x " + e.where + ": " + e.msg));
  process.exit(1);
}
cross.filter((e) => e.level === "warn").forEach((e) => console.error("  ! " + e.where + ": " + e.msg));

// Ostrzezenie o kolizji daty (nadpisze istniejacy wpis tego dnia).
const existing = corpus.find((it) => it.date === entry.date);

if (checkOnly) {
  console.log(`OK — zestaw zgodny.${existing ? " (UWAGA: nadpisze istniejacy wpis z tego dnia)" : ""}`);
  process.exit(0);
}

upsert(kind, entry);
console.log(`Zapisano ${kind} na ${entry.date}.${existing ? " (nadpisano istniejacy)" : ""}`);
