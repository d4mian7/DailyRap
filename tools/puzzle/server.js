// Lokalny serwer kalendarza do planowania zestawow.
// Uruchom:  .\puzzle.ps1 plan  albo  npm run serve   (http://localhost:5173)
// Ustawienia AI (AI_BACKEND, AI_MODEL, ANTHROPIC_API_KEY) wczytywane z tools/puzzle/.env (flaga --env-file-if-exists).
import { createServer } from "node:http";
import { readFileSync, writeFileSync, existsSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";
import { readItems, upsert, removeByDate, FILES, entryLabel, nextFreeDate } from "./src/io.js";
import { validateEntry, VALIDATORS, crossCheck, artistEntryErrors } from "./src/schema.js";
import { read } from "./src/io.js";
import { generate, aiStatus } from "./src/generate.js";

const KIND_ALIAS = { word: "words", connect: "puzzles", crossword: "crosswords", artist: "artists" };

// Bledy blokujace zapis jednego zestawu. „Kim jestem?” sprawdzamy na calym pliku, bo reguly zaleza od puli.
function entryErrors(kind, entry, all) {
  const errs = kind === "artists"
    ? artistEntryErrors(entry, all, read("artists").pool)
    : [...validateEntry(kind, entry), ...crossCheck(kind, entry, all).filter((e) => e.level === "error")];
  return errs.map((e) => `${e.where}: ${e.msg}`);
}

const __dirname = dirname(fileURLToPath(import.meta.url));
// Kazda wersja poprawki dostaje inny kierunek, inaczej modele trafiaja w ten sam pomysl.
const ANGLES = [
  "dyskografia: plyty, numery, wspolprace",
  "zyciorys i kariera: poczatki, nagrody, wytwornie, koncerty",
  "gra slow na tytule albo cecha stylu artysty",
];
const SKILL_PATH = join(__dirname, "..", "..", ".claude", "skills", "uloz-zestaw", "SKILL.md");
// Gdzie trafiaja uwagi z panelu: naglowek sekcji i miejsce, przed ktorym ja wstawic, jesli jej jeszcze nie ma.
// Sekcje musza lezec tam, gdzie generator czyta reguly danego trybu (RULE_SECTIONS w src/generate.js).
const FEEDBACK = {
  artists: { heading: "### Uwagi Damiana z panelu (Kim jestem?)", before: "## Procedura" },
  crosswords: { heading: "### RapCrossword — uwagi Damiana z panelu", before: "## Tryb „Kim jestem?" },
};

// Uwagi z panelu trafiaja do regul w SKILL.md, ktore generator czyta przy kazdym zestawie.
function appendFeedback(lines, kind = "artists") {
  const target = FEEDBACK[kind];
  if (!target || !existsSync(SKILL_PATH)) return;
  const raw = readFileSync(SKILL_PATH, "utf8");
  // Kolejne rundy poprawek wysylaja te sama uwage - zapisujemy ja raz.
  lines = lines.filter((l) => !raw.includes(`: ${l}`));
  if (!lines.length) return;
  const nl = raw.includes("\r\n") ? "\r\n" : "\n";
  const entry = lines.map((l) => `- ${new Date().toISOString().slice(0, 10)}: ${l}`).join(nl);
  let out;
  if (raw.includes(target.heading)) {
    out = raw.replace(target.heading + nl, target.heading + nl + entry + nl);
  } else {
    out = raw.replace(nl + target.before, nl + target.heading + nl + entry + nl + nl + target.before);
  }
  writeFileSync(SKILL_PATH, out, "utf8");
}

// Miernik „bez poprawek”: jeden wiersz JSON na kazdy zapisany zestaw z generatora.
// Tylko daty i liczby (ile elementow przyjetych bez zmian), bez tresci zestawow.
const METRICS_PATH = join(__dirname, "metrics.jsonl");
function readMetrics() {
  if (!existsSync(METRICS_PATH)) return [];
  return readFileSync(METRICS_PATH, "utf8").split(/\r?\n/).filter(Boolean).map((l) => JSON.parse(l));
}
function appendMetric(m) {
  const rec = { at: new Date().toISOString(), kind: m.kind, date: m.date, total: m.total, changed: m.changed };
  writeFileSync(METRICS_PATH, JSON.stringify(rec) + "\n", { encoding: "utf8", flag: "a" });
}

const PORT = process.env.PORT || 5173;

const today = () => new Date().toLocaleDateString("sv-SE"); // YYYY-MM-DD lokalnie

function buildData() {
  const modes = {};
  for (const kind of Object.keys(FILES)) {
    const all = readItems(kind);
    const items = all.map((it) => {
      const errors = entryErrors(kind, it, all);
      return { ...it, _valid: errors.length === 0, _errors: errors };
    });
    // Globalne bledy (duplikaty dat itd.) liczone na calym pliku:
    const fileIssues = VALIDATORS[kind](read(kind)).filter((i) => i.level === "error");
    modes[kind] = { items, fileErrors: fileIssues.map((e) => `${e.where}: ${e.msg}`) };
  }
  modes.artists.pool = read("artists").pool;
  return { today: today(), modes };
}

function send(res, code, body, type = "application/json") {
  res.writeHead(code, { "Content-Type": type + "; charset=utf-8", "Access-Control-Allow-Origin": "*" });
  res.end(typeof body === "string" ? body : JSON.stringify(body));
}

async function readBody(req) {
  const chunks = [];
  for await (const c of req) chunks.push(c);
  return chunks.length ? JSON.parse(Buffer.concat(chunks).toString("utf8")) : {};
}

const server = createServer(async (req, res) => {
  try {
    const url = new URL(req.url, `http://localhost:${PORT}`);
    const path = url.pathname;

    if (req.method === "GET" && (path === "/" || path === "/index.html")) {
      return send(res, 200, readFileSync(join(__dirname, "public", "index.html"), "utf8"), "text/html");
    }
    if (req.method === "GET" && path === "/api/data") {
      return send(res, 200, buildData());
    }
    if (req.method === "GET" && path === "/api/config") {
      return send(res, 200, aiStatus());
    }
    if (req.method === "POST" && path === "/api/generate") {
      const { kind: rawKind, date, theme, review } = await readBody(req);
      const kind = KIND_ALIAS[rawKind] || rawKind;
      if (!FILES[kind]) return send(res, 400, { ok: false, error: "Nieznany tryb" });
      try {
        // Poprawka zestawu „Kim jestem?”: kilka niezaleznych wersji do wyboru dla odrzuconych tropow.
        if (kind === "artists" && review) {
          // Po kolei: kazda wersja wie, co zaproponowaly poprzednie, i nie moze tego powtorzyc.
          const runs = [];
          for (const angle of ANGLES) {
            // + propozycje z wczesniejszych rund poprawek (panel je pamieta).
            const taken = [...(review.shown || []),
              ...runs.flatMap((r) => r.entry.clues.filter((_, i) => !review.keep.includes(i)))];
            runs.push(await generate(kind, date, theme, { ...review, angle, taken }));
          }
          return send(res, 200, { ok: true, runs });
        }
        // Poprawka krzyzowki: jedno wywolanie daje 3 wersje kazdej poprawianej pozycji (slowa z puli, pytania ze zrodel).
        if (kind === "crosswords" && review) {
          const { runs } = await generate(kind, date, theme, review);
          return send(res, 200, { ok: true, runs });
        }
        const result = await generate(kind, date, theme);
        return send(res, 200, { ok: true, ...result });
      } catch (e) {
        return send(res, 502, { ok: false, error: e.message });
      }
    }
    if (req.method === "GET" && path === "/api/metrics") {
      return send(res, 200, { ok: true, metrics: readMetrics() });
    }
    if (req.method === "POST" && path === "/api/metrics") {
      const m = await readBody(req);
      if (!FILES[m.kind] || !Number.isInteger(m.total) || !Number.isInteger(m.changed)) {
        return send(res, 400, { ok: false, error: "Zly wpis miernika" });
      }
      appendMetric(m);
      return send(res, 200, { ok: true });
    }
    if (req.method === "POST" && path === "/api/feedback") {
      const { lines, kind } = await readBody(req);
      appendFeedback(lines || [], KIND_ALIAS[kind] || kind || "artists");
      return send(res, 200, { ok: true });
    }
    if (req.method === "POST" && path === "/api/save") {
      const { kind, entry, overwrite } = await readBody(req);
      if (!FILES[kind]) return send(res, 400, { ok: false, error: "Nieznany tryb" });
      const items = readItems(kind);
      const errs = entryErrors(kind, entry, items);
      if (errs.length) return send(res, 422, { ok: false, errors: errs });
      // Zajeta data: nie nadpisujemy po cichu (tak zniknal Sentino). Panel pyta i dopiero wtedy wysyla overwrite.
      const existing = items.find((it) => it.date === entry.date);
      if (existing && !overwrite) {
        return send(res, 409, {
          ok: false, conflict: true, existing: entryLabel(kind, existing), free: nextFreeDate(items, entry.date),
          error: `Data ${entry.date} jest zajeta (${entryLabel(kind, existing)}).`,
        });
      }
      upsert(kind, entry);
      return send(res, 200, { ok: true, data: buildData() });
    }
    if (req.method === "POST" && path === "/api/delete") {
      const { kind, date } = await readBody(req);
      if (!FILES[kind]) return send(res, 400, { ok: false, error: "Nieznany tryb" });
      removeByDate(kind, date);
      return send(res, 200, { ok: true, data: buildData() });
    }
    if (req.method === "POST" && path === "/api/move") {
      const { kind, fromDate, toDate } = await readBody(req);
      if (!FILES[kind]) return send(res, 400, { ok: false, error: "Nieznany tryb" });
      const item = readItems(kind).find((it) => it.date === fromDate);
      if (!item) return send(res, 404, { ok: false, error: "Nie znaleziono wpisu" });
      const target = readItems(kind).find((it) => it.date === toDate);
      if (target && toDate !== fromDate) {
        return send(res, 409, { ok: false, error: `Data ${toDate} jest zajeta (${entryLabel(kind, target)}). Najpierw usun albo przenies tamten zestaw. Pierwszy wolny dzien: ${nextFreeDate(readItems(kind), toDate)}.` });
      }
      const moved = { ...item, date: toDate };
      const errs = entryErrors(kind, moved, readItems(kind).filter((it) => it.date !== fromDate));
      if (errs.length) return send(res, 422, { ok: false, errors: errs });
      removeByDate(kind, fromDate);
      upsert(kind, moved);
      return send(res, 200, { ok: true, data: buildData() });
    }
    return send(res, 404, { ok: false, error: "Not found" });
  } catch (e) {
    return send(res, 500, { ok: false, error: e.message });
  }
});

server.listen(PORT, () => {
  console.log(`\n  Kalendarz DailyRap:  http://localhost:${PORT}\n  (Ctrl+C aby zatrzymac)\n`);
});
