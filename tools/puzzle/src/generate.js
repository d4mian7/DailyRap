// Generowanie zestawow przez Claude API (Messages API, structured outputs).
// Dziala tylko gdy ustawiona zmienna ANTHROPIC_API_KEY.
import { validateEntry, crossCheck, usedTokens } from "./schema.js";
import { readItems } from "./io.js";

const MODEL = process.env.ANTHROPIC_MODEL || "claude-opus-4-8";
const chars = (s) => [...String(s ?? "")];
const up = (s) => String(s ?? "").toUpperCase();

export function hasApiKey() {
  return Boolean(process.env.ANTHROPIC_API_KEY);
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

async function callClaude(kind, theme, used) {
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
  if (!hasApiKey()) throw new Error("Brak ANTHROPIC_API_KEY - przycisk AI nieaktywny.");
  const corpus = readItems(kind);
  const used = usedTokens(kind, corpus, date);
  const raw = await callClaude(kind, theme, used);
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
