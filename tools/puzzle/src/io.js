// Odczyt/zapis plikow z zestawami + formatowanie dopasowane do istniejacego stylu,
// zeby diffy w gicie byly minimalne.
import { readFileSync, writeFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const __dirname = dirname(fileURLToPath(import.meta.url));
export const ASSETS_DIR = join(__dirname, "..", "..", "..", "app", "src", "main", "assets");

export const FILES = {
  words: { file: "words.json", root: "words" },
  puzzles: { file: "puzzles.json", root: "puzzles" },
  crosswords: { file: "crosswords.json", root: "crosswords" },
};

const pathFor = (kind) => join(ASSETS_DIR, FILES[kind].file);
const j = (v) => JSON.stringify(v); // bezpieczne escapowanie stringow, zachowuje polskie znaki

export function read(kind) {
  const raw = readFileSync(pathFor(kind), "utf8");
  return JSON.parse(raw);
}

export function readItems(kind) {
  const data = read(kind);
  return data[FILES[kind].root] ?? [];
}

// --- Formatery (odtwarzaja styl plikow zrodlowych) ---

function fmtWords(items) {
  const lines = items.map(
    (w) => `    { "id": ${j(w.id)}, "date": ${j(w.date)}, "word": ${j(w.word)}, "hint": ${j(w.hint)}, "variant": ${j(w.variant)} }`
  );
  return `{\n  "words": [\n${lines.join(",\n")}\n  ]\n}\n`;
}

function fmtPuzzles(items) {
  const blocks = items.map((p) => {
    const cats = p.categories
      .map((c) => `        { "name": ${j(c.name)}, "words": [${c.words.map(j).join(", ")}], "difficulty": ${j(c.difficulty)} }`)
      .join(",\n");
    return `    {\n      "id": ${j(p.id)}, "date": ${j(p.date)},\n      "categories": [\n${cats}\n      ]\n    }`;
  });
  return `{\n  "puzzles": [\n${blocks.join(",\n")}\n  ]\n}\n`;
}

function fmtCrosswords(items) {
  const blocks = items.map((cw) => {
    const clues = cw.clues
      .map(
        (c) =>
          `        {\n          "question": ${j(c.question)},\n          "answer": ${j(c.answer)},\n          "highlightIndex": ${j(c.highlightIndex)}\n        }`
      )
      .join(",\n");
    return `    {\n      "id": ${j(cw.id)},\n      "date": ${j(cw.date)},\n      "secretWord": ${j(cw.secretWord)},\n      "clues": [\n${clues}\n      ]\n    }`;
  });
  return `{\n  "crosswords": [\n${blocks.join(",\n")}\n  ]\n}\n`;
}

const FORMATTERS = { words: fmtWords, puzzles: fmtPuzzles, crosswords: fmtCrosswords };

export function writeItems(kind, items) {
  // Sortuj po dacie, zeby plik byl czytelny i terminarz spojny.
  const sorted = [...items].sort((a, b) => String(a.date).localeCompare(String(b.date)));
  writeFileSync(pathFor(kind), FORMATTERS[kind](sorted), "utf8");
  return sorted;
}

export function nextId(kind) {
  const items = readItems(kind);
  return items.reduce((m, it) => Math.max(m, Number(it.id) || 0), 0) + 1;
}

// Upsert wpisu wg daty: nadpisuje istniejacy w tym dniu albo dodaje nowy.
export function upsert(kind, entry) {
  const items = readItems(kind);
  const idx = items.findIndex((it) => it.date === entry.date);
  if (idx >= 0) {
    entry.id = entry.id ?? items[idx].id;
    items[idx] = entry;
  } else {
    entry.id = entry.id ?? nextId(kind);
    items.push(entry);
  }
  return writeItems(kind, items);
}

export function removeByDate(kind, date) {
  const items = readItems(kind).filter((it) => it.date !== date);
  return writeItems(kind, items);
}
