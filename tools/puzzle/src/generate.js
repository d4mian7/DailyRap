// Generowanie zestawow przez Claude. Dwa tryby (zmienna AI_BACKEND w tools/puzzle/.env):
//   claude-code (domyslnie) - wywoluje zainstalowane Claude Code (`claude -p`), rozlicza sie z subskrypcji.
//   api                     - Messages API z ANTHROPIC_API_KEY, zuzywa kredyty API.
import { spawn, spawnSync } from "node:child_process";
import { existsSync, readFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { validateEntry, crossCheck, usedTokens, artistEntryErrors, ARTIST_CLUES } from "./schema.js";
import { read, readItems } from "./io.js";

const BACKEND = (process.env.AI_BACKEND || "claude-code").toLowerCase();
const MODEL = process.env.AI_MODEL || process.env.ANTHROPIC_MODEL || (BACKEND === "api" ? "claude-opus-5" : "sonnet");
const CLAUDE_BIN = process.env.CLAUDE_BIN || resolveClaudeBin();

// Windows: `claude` z npm to skrypt claude.cmd, ktorego Node nie uruchomi bez powloki.
// Skrypt tylko odpala claude.exe, wiec wolamy ten plik bezposrednio.
function resolveClaudeBin() {
  if (process.platform !== "win32") return "claude";
  const where = spawnSync("where", ["claude.cmd"], { encoding: "utf8" });
  const cmd = (where.stdout || "").split(/\r?\n/).find(Boolean);
  if (!cmd) return "claude";
  const exe = readFileSync(cmd, "utf8").match(/"%dp0%\\([^"]+\.exe)"/);
  return exe ? join(dirname(cmd), exe[1]) : "claude";
}
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

// Reguly ukladania z ocen Damiana. Ten sam plik czyta skill /uloz-zestaw w Claude Code, wiec
// generator w panelu i praca w terminalu korzystaja z jednego zrodla wiedzy. Plik jest lokalny
// (.claude/ poza gitem) - bez niego generator dziala na samym promptcie bazowym.
const SKILL_PATH = join(dirname(fileURLToPath(import.meta.url)), "..", "..", "..", ".claude", "skills", "uloz-zestaw", "SKILL.md");

// Sekcje SKILL.md (po poczatku naglowka) wazne dla danego trybu.
const RULE_SECTIONS = {
  words: ["## Tematyka", "#### RapWord", "### BIALA LISTA"],
  puzzles: ["## Tematyka", "### RapConnect"],
  crosswords: ["## Tematyka", "### RapCrossword — styl", "### RapCrossword — czego", "### BIALA LISTA"],
  artists: ["## Tryb „Kim jestem?"],
};

// Wycina sekcje od naglowka do nastepnego naglowka tego samego lub wyzszego poziomu.
function section(md, heading) {
  const lines = md.split(/\r?\n/);
  const start = lines.findIndex((l) => l.startsWith(heading));
  if (start < 0) return "";
  const level = lines[start].match(/^#+/)[0].length;
  const end = lines.findIndex((l, i) => i > start && /^#+ /.test(l) && l.match(/^#+/)[0].length <= level);
  return lines.slice(start, end < 0 ? undefined : end).join("\n").trim();
}

export function rulesFor(kind) {
  if (!existsSync(SKILL_PATH)) return "";
  const md = readFileSync(SKILL_PATH, "utf8");
  const parts = (RULE_SECTIONS[kind] || []).map((h) => section(md, h)).filter(Boolean);
  return parts.length ? `\n\nREGULY Z OCEN DAMIANA (stosuj je scisle):\n\n${parts.join("\n\n")}` : "";
}

// Schematy structured outputs (bez ograniczen dlugosci - te pilnuje prompt + walidator).
const SCHEMAS = {
  words: {
    type: "object",
    properties: {
      word: { type: "string", description: "Slowo WIELKIMI literami, 4-6 liter" },
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
  artists: {
    type: "object",
    properties: {
      clues: {
        type: "array",
        description: `Dokladnie ${ARTIST_CLUES} tropow, od najtrudniejszego`,
        items: {
          type: "object",
          properties: {
            text: { type: "string", description: "Tresc tropu pokazywana graczowi" },
            quote: { type: "string", description: "Doslowny fragment ZRODEL, ktory potwierdza trop" },
          },
          required: ["text", "quote"],
          additionalProperties: false,
        },
      },
    },
    required: ["clues"],
    additionalProperties: false,
  },
};

// Kim jestem?: kto czeka w biezacym cyklu puli i jakie tropy juz padly.
function artistContext(items) {
  const pool = read("artists").pool;
  const sorted = [...items].sort((a, b) => String(a.date).localeCompare(String(b.date)));
  let cycle = new Set();
  sorted.forEach((a) => {
    if (cycle.has(a.artist)) cycle = new Set();
    cycle.add(a.artist);
  });
  const available = pool.filter((p) => !cycle.has(p));
  return {
    available: available.length ? available : pool,
    clues: items.flatMap((a) => a.clues || []),
    examples: sorted.slice(-6).map((a) => `${a.artist}: ${a.clues.join(" / ")}`),
  };
}

// === Kim jestem?: tropy tylko z pobranych zrodel, kazdy z cytatem sprawdzanym przez kod ===

// Mocniejszy model niz przy pozostalych trybach: tu liczy sie styl i trafnosc, nie szybkosc.
const ARTIST_MODEL = process.env.AI_MODEL_ARTISTS || (BACKEND === "api" ? MODEL : "opus");
const ARTIST_ATTEMPTS = 2;
const QUOTE_MIN = 12;

const normText = (s) =>
  String(s ?? "").toLowerCase().normalize("NFD").replace(/\p{M}+/gu, "").replace(/ł/g, "l")
    .replace(/[^\p{L}\p{N}]+/gu, " ").trim();

// Wikipedia przy wielu zapytaniach odpowiada 429 - czekamy i ponawiamy.
async function getJson(url, retries = 3) {
  for (let i = 0; ; i++) {
    const res = await fetch(url, { headers: { "user-agent": "DailyRap-puzzle-tools/1.0 (lokalne narzedzie do ukladania zagadek)" } });
    if (res.ok) return res.json();
    if (res.status !== 429 || i >= retries) throw new Error(`${url} -> ${res.status}`);
    await new Promise((r) => setTimeout(r, 3000 * (i + 1)));
  }
}

// Artykul z polskiej Wikipedii (bez przypisow i tabel) + dyskografia z Deezera.
export async function artistSources(name) {
  const parts = [];
  const search = await getJson(`https://pl.wikipedia.org/w/api.php?action=query&list=search&format=json&srlimit=5&srsearch=${encodeURIComponent(name.replace(/-/g, " ") + " raper")}`);
  const key = normText(name).split(" ")[0];
  const results = search.query?.search || [];
  const hit = results.find((r) => normText(r.title).includes(key)) || results[0];
  if (hit) {
    const page = await getJson(`https://pl.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&explaintext=1&redirects=1&titles=${encodeURIComponent(hit.title)}`);
    const text = (Object.values(page.query.pages)[0].extract || "").split(/\n== (Przypisy|Linki zewnętrzne|Bibliografia) ==/)[0];
    // Brak wlasnego artykulu (Oskar83 -> PRO8L3M): model musi wiedziec, ze to zrodlo o kims innym.
    const own = normText(hit.title).includes(key);
    const label = own ? `WIKIPEDIA („${hit.title}”)`
      : `WIKIPEDIA – UWAGA: brak artykulu o ${name}, to artykul „${hit.title}”. Bierz z niego TYLKO fakty o ${name} osobiscie, nie o calej grupie/ekipie`;
    if (text) parts.push(`${label}:\n${text}`);
  }
  try {
    const found = await getJson(`https://api.deezer.com/search/artist?limit=1&q=${encodeURIComponent(name)}`);
    const artist = found.data?.[0];
    if (artist && normText(artist.name) === normText(name)) {
      const albums = await getJson(`https://api.deezer.com/artist/${artist.id}/albums?limit=100`);
      const top = await getJson(`https://api.deezer.com/artist/${artist.id}/top?limit=10`);
      const list = (albums.data || []).filter((a) => a.record_type !== "single")
        .map((a) => `„${a.title}” (${a.record_type}, ${String(a.release_date).slice(0, 4)})`);
      parts.push(`DEEZER – wydawnictwa: ${list.join("; ")}.\nDEEZER – najpopularniejsze numery: ${(top.data || []).map((t) => `„${t.title}”`).join(", ")}.`);
    }
  } catch {
    // Deezer jest dodatkiem - bez niego zostaje Wikipedia.
  }
  try {
    const discogs = await discogsSource(name);
    if (discogs) parts.push(discogs);
  } catch {
    // Discogs tez jest dodatkiem (limit zapytan, brak artysty).
  }
  return parts.join("\n\n");
}

// Discogs: goscinne wystepy i produkcje (tego Wikipedia czesto nie ma, np. Oskar83 bez wlasnego artykulu).
// Wlasne plyty pomijamy - dyskografie daje Deezer.
const DISCOGS_ROLES = { Appearance: "gosc na plycie", TrackAppearance: "gosc w utworze", Producer: "produkcja", Remix: "remiks" };
// Discogs dopisuje numer przy powtarzajacych sie nazwach („Oki (14)”) i gwiazdke przy wariancie nazwy.
const discogsName = (s) => String(s).replace(/\s*\(\d+\)/g, "").replace(/\*/g, "").trim();

async function discogsSource(name) {
  const found = await getJson(`https://api.discogs.com/database/search?type=artist&q=${encodeURIComponent(name)}`);
  const hit = (found.results || []).find((r) => normText(discogsName(r.title)) === normText(name));
  if (!hit) return "";
  const artist = await getJson(`https://api.discogs.com/artists/${hit.id}`);
  const rel = await getJson(`https://api.discogs.com/artists/${hit.id}/releases?per_page=100&sort=year`);
  const lines = (rel.releases || []).filter((r) => DISCOGS_ROLES[r.role])
    .map((r) => `${r.year || "?"}: ${discogsName(r.artist)} – „${r.title.trim()}”${r.trackinfo ? ` (utwor „${r.trackinfo.trim()}”)` : ""} – ${DISCOGS_ROLES[r.role]}`);
  const groups = (artist.groups || []).map((g) => discogsName(g.name));
  const members = (artist.members || []).map((m) => discogsName(m.name));
  if (!lines.length && !groups.length && !members.length) return "";
  return [
    `DISCOGS („${hit.title}”):`,
    groups.length ? `Grupy: ${groups.join(", ")}.` : "",
    members.length ? `Czlonkowie: ${members.join(", ")}.` : "",
    lines.length ? `Wystepy goscinne i produkcje:\n${lines.join("\n")}` : "",
  ].filter(Boolean).join("\n");
}

function pickArtist(theme, available) {
  const wanted = theme && available.find((a) => normText(a) === normText(theme));
  return wanted || available[Math.floor(Math.random() * available.length)];
}

// Poprawka zestawu: zatwierdzone tropy zostaja na swoich miejscach, reszta do wymiany z uwagami Damiana.
function reviewBlock(review) {
  if (!review) return "";
  const lines = review.clues.map((c, i) =>
    review.keep.includes(i) ? `${i + 1}. ZOSTAW BEZ ZMIAN: „${c}”` : `${i + 1}. WYMIEN: „${c}” - uwaga Damiana: ${review.notes[i] || "slaby trop"}`);
  const angle = review.angle
    ? `\nKIERUNEK NOWYCH TROPOW W TEJ WERSJI: ${review.angle}. Wez fakt z tego obszaru i nie powtarzaj pomyslow odrzuconych tropow.`
    : "";
  const taken = review.taken?.length
    ? `\nTE POMYSLY SA JUZ W INNYCH WERSJACH - uzyj ZUPELNIE INNEGO faktu (inny temat, nie przerobka): ${review.taken.join(" | ")}`
    : "";
  return `${angle}${taken}\nTO JEST POPRAWKA ZESTAWU. Tropy oznaczone ZOSTAW przepisz doslownie na tych samych pozycjach (z cytatem). Pozostale zastap NOWYMI, lepszymi tropami zgodnie z uwagami:\n${lines.join("\n")}\n`;
}

// Krytyk: model ocenia wlasny szkic regulami Damiana i wymienia slabe tropy, zanim zestaw trafi do czlowieka.
function criticPrompt(artist, sources, ctx, draft) {
  return `${artistPrompt(artist, sources, ctx, "")}

TWOJ SZKIC DO OCENY:
${draft.map((c, i) => `${i + 1}. ${c.text}  [cytat: ${c.quote}]`).join("\n")}

Jestes teraz surowym recenzentem w stylu Damiana. Sprawdz KAZDY trop wedlug REGUL powyzej: czy jest krotki (max ok. 10 slow, jeden fakt), czy fan polskiego rapu moze go skojarzyc (nie erudycja spoza rapu, nie news, ktorego nikt nie pamieta), czy trudnosc rosnie od 1 do 5, czy tropy roznia sie budowa i tematem, czy nie ma klopotow prawnych/prywatnych. Dobre tropy zostaw bez zmian, slabe zastap lepszymi z ZRODEL. Zwroc pelny, poprawiony zestaw.`;
}

function artistPrompt(artist, sources, ctx, feedback, review) {
  return `Uloz zestaw do gry „Kim jestem?” o artyscie: ${artist}. Gracz zgaduje go po ${ARTIST_CLUES} tropach w pierwszej osobie, odslanianych od najtrudniejszego do najlatwiejszego.
- KAZDY trop opieraj WYLACZNIE na ZRODLACH ponizej. Do kazdego tropu podaj w polu quote doslowny fragment zrodla (min. kilka slow, skopiowany bez zmian), ktory go potwierdza. Trop bez potwierdzenia w zrodlach jest odrzucany automatycznie.
- Forma „-em/am” albo neutralna, krotkie zdania, bez ksywy artysty i bez jej czlonow w tropach.
- Plec NIE moze wyjsc z gramatyki, takze gdy to artystka: zawsze „nagralem/am”, nigdy samo „nagralam” ani „nagralem”; bez slow „kobieta”, „raperka”, „wokalistka”, „dziewczyna”. Role artysty pisz czasownikiem, nie rzeczownikiem z rodzajem: „Reżyseruje teledyski”, „Produkowalem/am bity” zamiast „Jestem rezyserem”, „jako producent”, „ambasadorem marki”.
- NIE powtarzaj zadnego z juz uzytych tropow ani ich przerobek: ${ctx.clues.join(" | ")}.
- Zatwierdzone zestawy (wzor stylu i poziomu trudnosci): ${ctx.examples.join(" || ")}${rulesFor("artists")}
${reviewBlock(review)}${feedback ? `\nPOPRZEDNIA PROBA ZOSTALA ODRZUCONA. Popraw tylko wskazane problemy:\n${feedback}\n` : ""}
ZRODLA:
${sources}`;
}

// Cytat musi naprawde wystepowac w zrodlach (porownanie bez wielkosci liter, znakow i interpunkcji).
function quoteErrors(clues, sources) {
  const hay = normText(sources);
  return clues.flatMap((c, i) => {
    const q = normText(c.quote);
    if (q.length < QUOTE_MIN) return [`Trop ${i + 1}: brak cytatu ze zrodla („${c.text}”)`];
    return hay.includes(q) ? [] : [`Trop ${i + 1}: cytatu nie ma w zrodlach, fakt niepotwierdzony („${c.text}”)`];
  });
}

// Model czasem zamyka „tytul" prostym cudzyslowem - w grze ma byc zawsze „tytul”.
function polishQuotes(text) {
  let open = false;
  return text.replace(/["„”]/g, () => ((open = !open) ? "„" : "”"));
}

// review (opcjonalnie): { artist, clues, keep: [indeksy zatwierdzonych], notes: {indeks: uwaga} }
export async function generateArtist(date, theme, corpus, review) {
  const others = corpus.filter((it) => it.date !== date);
  const ctx = artistContext(others);
  const artist = review?.artist || pickArtist(theme, ctx.available);
  const sources = await artistSources(artist);
  if (!sources) throw new Error(`Nie udalo sie pobrac zrodel o ${artist} - bez nich generator nie zgaduje faktow.`);
  const call = (prompt) => (BACKEND === "api" ? callApi("artists", prompt, ARTIST_MODEL) : callClaudeCode("artists", prompt, ARTIST_MODEL));
  const pool = read("artists").pool;

  // Jeden szkic: odpowiedz modelu -> zatwierdzone tropy na swoje miejsca -> cytaty i reguly sprawdza kod.
  const draft = async (prompt) => {
    const raw = await call(prompt);
    const clues = (raw.clues || []).map((c) => ({ text: polishQuotes(String(c.text || "").trim()), quote: String(c.quote || "").trim() }));
    const kept = new Set(review?.keep || []);
    kept.forEach((i) => { clues[i] = { text: review.clues[i], quote: clues[i]?.quote || "(zatwierdzony przez Damiana)" }; });
    const entry = { date, artist, clues: clues.map((c) => c.text) };
    const errors = [
      ...quoteErrors(clues.filter((_, i) => !kept.has(i)), sources),
      ...artistEntryErrors(entry, others, pool).map((e) => `${e.where}: ${e.msg}`),
    ];
    return { entry, clues, errors };
  };

  let best = await draft(artistPrompt(artist, sources, ctx, "", review));
  const steps = ["szkic"];
  if (!review) {
    const critic = await draft(criticPrompt(artist, sources, ctx, best.clues));
    steps.push("krytyk");
    if (critic.errors.length <= best.errors.length) best = critic;
  }
  for (let attempt = 2; attempt <= ARTIST_ATTEMPTS && best.errors.length; attempt++) {
    best = await draft(artistPrompt(artist, sources, ctx, best.errors.join("\n"), review));
    steps.push("poprawka bledow");
  }
  return { entry: best.entry, valid: best.errors.length === 0, errors: best.errors, quotes: best.clues.map((c) => c.quote), steps };
}

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
  const avoid = avoidBlock(kind, used) + rulesFor(kind);
  if (kind === "words") {
    return `Wymysl jedno haslo do gry typu Wordle o polskim rapie (4-6 liter, rozpoznawalne dla szerokiego grona fanow).${t} Zwroc word (WIELKIMI literami) i hint.${avoid}`;
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

async function callApi(kind, prompt, model = MODEL) {
  const body = {
    model,
    max_tokens: 8000,
    thinking: { type: "adaptive" },
    output_config: { effort: "high", format: { type: "json_schema", schema: SCHEMAS[kind] } },
    system: SYSTEM,
    messages: [{ role: "user", content: prompt }],
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

function callClaudeCode(kind, prompt, model = MODEL) {
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
    "--model", model,
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

    child.stdin.end(prompt);
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
export async function generate(kind, date, theme, review) {
  const status = aiStatus();
  if (!status.available) throw new Error(`${status.reason} - przycisk AI nieaktywny.`);
  const corpus = readItems(kind);
  if (kind === "artists") return generateArtist(date, theme, corpus, review);
  const used = usedTokens(kind, corpus, date);
  const prompt = userPrompt(kind, theme, used);
  const raw = BACKEND === "api" ? await callApi(kind, prompt) : await callClaudeCode(kind, prompt);
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
