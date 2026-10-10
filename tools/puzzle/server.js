// Lokalny serwer kalendarza do planowania zestawow.
// Uruchom:  .\puzzle.ps1 plan  albo  npm run serve   (http://localhost:5173)
// Ustawienia AI (AI_BACKEND, AI_MODEL, ANTHROPIC_API_KEY) wczytywane z tools/puzzle/.env (flaga --env-file-if-exists).
import { createServer } from "node:http";
import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";
import { readItems, upsert, removeByDate, FILES } from "./src/io.js";
import { validateEntry, VALIDATORS, crossCheck } from "./src/schema.js";
import { read } from "./src/io.js";
import { generate, aiStatus } from "./src/generate.js";

const KIND_ALIAS = { word: "words", connect: "puzzles", crossword: "crosswords" };

const __dirname = dirname(fileURLToPath(import.meta.url));
const PORT = process.env.PORT || 5173;

const today = () => new Date().toLocaleDateString("sv-SE"); // YYYY-MM-DD lokalnie

function buildData() {
  const modes = {};
  for (const kind of Object.keys(FILES)) {
    const all = readItems(kind);
    const items = all.map((it) => {
      const errors = [
        ...validateEntry(kind, it),
        ...crossCheck(kind, it, all).filter((e) => e.level === "error"),
      ].map((e) => `${e.where}: ${e.msg}`);
      return { ...it, _valid: errors.length === 0, _errors: errors };
    });
    // Globalne bledy (duplikaty dat itd.) liczone na calym pliku:
    const fileIssues = VALIDATORS[kind](read(kind)).filter((i) => i.level === "error");
    modes[kind] = { items, fileErrors: fileIssues.map((e) => `${e.where}: ${e.msg}`) };
  }
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
      const { kind: rawKind, date, theme } = await readBody(req);
      const kind = KIND_ALIAS[rawKind] || rawKind;
      if (!FILES[kind]) return send(res, 400, { ok: false, error: "Nieznany tryb" });
      try {
        const result = await generate(kind, date, theme);
        return send(res, 200, { ok: true, ...result });
      } catch (e) {
        return send(res, 502, { ok: false, error: e.message });
      }
    }
    if (req.method === "POST" && path === "/api/save") {
      const { kind, entry } = await readBody(req);
      if (!FILES[kind]) return send(res, 400, { ok: false, error: "Nieznany tryb" });
      const errs = [
        ...validateEntry(kind, entry),
        ...crossCheck(kind, entry, readItems(kind)).filter((e) => e.level === "error"),
      ];
      if (errs.length) return send(res, 422, { ok: false, errors: errs.map((e) => `${e.where}: ${e.msg}`) });
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
      const moved = { ...item, date: toDate };
      const errs = [
        ...validateEntry(kind, moved),
        ...crossCheck(kind, moved, readItems(kind)).filter((e) => e.level === "error"),
      ];
      if (errs.length) return send(res, 422, { ok: false, errors: errs.map((e) => `${e.where}: ${e.msg}`) });
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
