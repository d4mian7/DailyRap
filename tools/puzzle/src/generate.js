// Generowanie zestawow przez Claude. Dwa tryby (zmienna AI_BACKEND w tools/puzzle/.env):
//   claude-code (domyslnie) - wywoluje zainstalowane Claude Code (`claude -p`), rozlicza sie z subskrypcji.
//   api                     - Messages API z ANTHROPIC_API_KEY, zuzywa kredyty API.
import { spawn, spawnSync } from "node:child_process";
import { tmpdir } from "node:os";
import { validateEntry, crossCheck, usedTokens } from "./schema.js";
import { readItems } from "./io.js";

const BACKEND = (process.env.AI_BACKEND || "claude-code").toLowerCase();
const MODEL = process.env.AI_MODEL || process.env.ANTHROPIC_MODEL || (BACKEND === "api" ? "claude-opus-5" : "sonnet");
const CLAUDE_BIN = process.env.CLAUDE_BIN || "claude";
const TIMEOUT_MS = Number(process.env.AI_TIMEOUT_MS) || 300_000;
const chars = (s) => [...String(s ?? "")];
const up = (s) => String(s ?? "").toUpperCase();

let claudeCodeCheck = null;
function claudeCodeInstalled() {
  if (claudeCodeCheck === null) {
    const r = spawnSync(CLAUDE_BIN, ["--version"], { encoding: "utf8", timeout: 15_000 });
    claudeCodeCheck = r.status === 0;
  }
  return claudeCodeCheck;
}

export function aiStatus() {
  if (BACKEND === "api") {
    const ok = Boolean(process.env.ANTHROPIC_API_KEY);
    return { available: ok, backend: "api", model: MODEL, reason: ok ? null : "Brak ANTHROPIC_API_KEY w tools/puzzle/.env" };
  }
  const ok = claudeCodeInstalled();
  return { available: ok, backend: "claude-code", model: MODEL, reason: ok ? null : `Nie znaleziono programu "${CLAUDE_BIN}" (Claude Code)` };
}

const SYSTEM = `Jestes ekspertem od polskiego rapu (raperzy, raperki, ksywki, albumy, single, wytwornie: SBM, 2115, Chillwagon, 20lab, QueQuality; kolaboracje, slang). Ukladasz zestawy do gier o tematyce polskiego rapu. Trzymaj sie faktow - nie zmyslaj nieistniejacych albumow ani utworow; gdy nie masz pewnosci, wybierz pewniejsza opcje. Pisz WIELKIMI literami tam gdzie wymagane. Odpowiadaj wylacznie danymi w zadanym formacie JSON.`;

// Schematy structured outputs (bez ograniczen dlugosci - te pilnuje prompt + walidator).
const SCHEMAS = {
  words: {
    type: "object",
    properties: {
      word: { type: "string", description: "Slowo WIELKIMI literami, 4-8 liter" },
      hint: { type: "string", description: "Krotka kategoria, np. Raper/ka, Album, Slang" },
    },
    required: ["word", "hint"],
    additionalProperties: false,
  },
  puzzles: {
    type: "object",
    properties: {
      categories: {
        type: "array",
        items: {
          type: "object",
          properties: {
            name: { type: "string" },
            words: { type: "array", items: { type: "string" } },
            difficulty: { type: "integer" },
          },
          required: ["name", "words", "difficulty"],
          additionalProperties: false,
        },
      },
    },
    required: ["categories"],
    additionalProperties: false,
  },
  crosswords: {
    type: "object",
    properties: {
      secretWord: { type: "string", description: "Haslo WIELKIMI literami" },
      clues: {
        type: "array",
        items: {
          type: "object",
          properties: {
            question: { type: "string" },
            answer: { type: "string", description: "Odpowiedz WIELKIMI literami" },
            highlightIndex: { type: "integer" },
          },
          required: ["question", "answer", "highlightIndex"],
          additionalProperties: false,
        },
      },
    },
    required: ["secretWord", "clues"],
    additionalProperties: false,
  },
};

function avoidBlock(kind, used) {
  // Lista juz uzytych tokenow - model NIE moze ich powtorzyc (gracz gra codziennie).
  const cap = (arr, n = 400) => (arr.length > n ? arr.slice(-n) : arr).join(", ");
  if (kind === "words") {
    return `\n\nUNIKALNOSC (krytyczne): NIE uzywaj zadnego z tych juz wykorzystanych slow: ${cap(used.words)}.`;
  }
  if (kind === "puzzles") {
    return `\n\nUNIKALNOSC (krytyczne): NIE uzywaj zadnego z tych juz wykorzystanych slow: ${cap(used.words)}.
Unikaj tez tych nazw kategorii: ${cap(used.names)}.`;
  }
  return `\n\nUNIKALNOSC (krytyczne):
- PYTANIA: zadne pytanie nie moze byc takie samo ani podobne/przeformulowane do juz uzytych. NIE uzywaj tych pytan ani ich wariacji: ${cap(used.questions)}.
- HASLO/ODPOWIEDZI: NIE uzywaj teraz tych hasel jako secretWord: ${cap(used.secretWords)}. NIE uzywaj teraz tych odpowiedzi: ${cap(used.answers)}. (Te same odpowiedzi sa dozwolone ponownie, ale tylko z NOWYM pytaniem i dopiero po >= ${used.gapDays || 7} dniach.)`;
}

function userPrompt(kind, theme, used) {
  const t = theme ? ` Temat/wskazowka: ${theme}.` : "";
  const avoid = avoidBlock(kind, used);
  if (kind === "words") {
    return `Wymysl jedno haslo do gry typu Wordle o polskim rapie (4-8 liter).${t} Zwroc word (WIELKIMI literami) i hint.${avoid}`;
  }
  if (kind === "puzzles") {
    return `Uloz zestaw Connections: DOKLADNIE 4 kategorie, kazda z DOKLADNIE 4 slowami o polskim rapie.${t}
- difficulty: po jednym z {0,1,2,3} (0=najlatwiej, 3=najtrudniej).
- Wszystkie 16 slow musza byc unikalne.
- KLUCZOWE: zadne slowo nie moze sensownie pasowac do dwoch kategorii - zagadka ma byc jednoznaczna. Dobre zestawy maja pulapki (slowa ktore wygladaja jakby pasowaly gdzie indziej).${avoid}`;
  }
  return `Uloz krzyzowke z haslem o polskim rapie.${t}
- Wybierz haslo (secretWord) dlugosci 5-8 liter, WIELKIMI literami.
- Podaj DOKLADNIE tyle pytan (clues) ile liter ma haslo.
- Pytanie nr i (liczac od 0) tworzy i-ta litere hasla: answer[highlightIndex] MUSI rownac sie i-tej literze hasla.
- Kazda odpowiedz to rozpoznawalne haslo rapowe (3-9 liter) zawierajace wymagana litere; ustaw highlightIndex na jej pozycji.
- Policz dokladnie pozycje liter, zanim zwrocisz wynik.${avoid}`;
}

async function callApi(kind, theme, used) {
  const body = {
    model: MODEL,
    max_tokens: 8000,
    thinking: { type: "adaptive" },
    output_config: { effort: "high", format: { type: "json_schema", schema: SCHEMAS[kind] } },
    system: SYSTEM,
    messages: [{ role: "user", content: userPrompt(kind, theme, used) }],
  };
  const res = await fetch("https://api.anthropic.com/v1/messages", {
    method: "POST",
    headers: {
      "content-type": "application/json",
      "x-api-key": process.env.ANTHROPIC_API_KEY,
      "anthropic-version": "2023-06-01",
    },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const txt = await res.text();
    throw new Error(`Claude API ${res.status}: ${txt.slice(0, 300)}`);
  }
  const data = await res.json();
  if (data.stop_reason === "refusal") throw new Error("Model odmowil wygenerowania (refusal).");
  const textBlock = (data.content || []).find((b) => b.type === "text");
  if (!textBlock) throw new Error("Brak odpowiedzi tekstowej od modelu.");
  return JSON.parse(textBlock.text);
}

function callClaudeCode(kind, theme, used) {
  // Klucz API i znacznik zagniezdzonej sesji usuwamy, inaczej Claude Code rozliczaloby sie z API
  // zamiast subskrypcji albo odmowilo startu wewnatrz innej sesji.
  const env = { ...process.env };
  delete env.ANTHROPIC_API_KEY;
  delete env.CLAUDECODE;

  const args = [
    "-p",
    "--output-format", "json",
    "--json-schema", JSON.stringify(SCHEMAS[kind]),
    "--system-prompt", SYSTEM,
    "--model", MODEL,
    "--effort", "high",
    "--tools", "",
    "--disable-slash-commands",
    "--no-session-persistence",
  ];

  return new Promise((resolve, reject) => {
    // cwd poza repo: bez CLAUDE.md, hookow i ustawien projektu.
    const child = spawn(CLAUDE_BIN, args, { env, cwd: tmpdir(), stdio: ["pipe", "pipe", "pipe"] });
    let out = "";
    let err = "";
    const timer = setTimeout(() => {
      child.kill();
      reject(new Error(`Claude Code nie odpowiedzialo w ${Math.round(TIMEOUT_MS / 1000)} s.`));
    }, TIMEOUT_MS);

    child.stdout.on("data", (d) => (out += d));
    child.stderr.on("data", (d) => (err += d));
    child.on("error", (e) => {
      clearTimeout(timer);
      reject(new Error(e.code === "ENOENT" ? `Nie znaleziono programu "${CLAUDE_BIN}" (Claude Code).` : e.message));
    });
    child.on("close", (code) => {
      clearTimeout(timer);
      let data;
      try {
        data = JSON.parse(out);
      } catch {
        return reject(new Error(`Claude Code (kod ${code}): ${(err || out).trim().slice(0, 300) || "brak odpowiedzi"}`));
      }
      if (data.is_error || data.subtype !== "success") {
        return reject(new Error(`Claude Code: ${String(data.result || data.subtype || "blad").slice(0, 300)}`));
      }
      if (data.structured_output) return resolve(data.structured_output);
      try {
        resolve(JSON.parse(data.result));
      } catch {
        reject(new Error("Claude Code zwrocilo odpowiedz, ktora nie jest JSON-em."));
      }
    });

    child.stdin.end(userPrompt(kind, theme, used));
  });
}

// Auto-naprawa krzyzowki: ustaw highlightIndex tam, gdzie odpowiedz zawiera wymagana litere.
function repairCrossword(entry) {
  const secret = chars(up(entry.secretWord));
  (entry.clues || []).forEach((cl, i) => {
    if (i >= secret.length) return;
    const ans = chars(up(cl.answer));
    if (ans[cl.highlightIndex] === secret[i]) return;
    const pos = ans.indexOf(secret[i]);
    if (pos >= 0) cl.highlightIndex = pos;
  });
  return entry;
}

// Zwraca { entry, valid, errors }. Nie zapisuje - to robi UI po akceptacji.
export async function generate(kind, date, theme) {
  const status = aiStatus();
  if (!status.available) throw new Error(`${status.reason} - przycisk AI nieaktywny.`);
  const corpus = readItems(kind);
  const used = usedTokens(kind, corpus, date);
  const raw = BACKEND === "api" ? await callApi(kind, theme, used) : await callClaudeCode(kind, theme, used);
  let entry;
  if (kind === "words") {
    const word = up(raw.word).trim();
    entry = { date, word, hint: raw.hint, variant: chars(word).length };
  } else if (kind === "puzzles") {
    entry = {
      date,
      categories: (raw.categories || []).map((c) => ({
        name: c.name,
        words: c.words,
        difficulty: c.difficulty,
      })),
    };
  } else {
    entry = repairCrossword({ date, secretWord: up(raw.secretWord), clues: raw.clues });
  }
  const errs = [
    ...validateEntry(kind, entry),
    ...crossCheck(kind, entry, corpus).filter((e) => e.level === "error"),
  ].map((e) => `${e.where}: ${e.msg}`);
  return { entry, valid: errs.length === 0, errors: errs };
}
