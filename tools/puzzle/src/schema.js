// Walidacja zestawow DailyRap. Jeden zrodlo prawdy dla CLI i serwera web.
//
// Twarde reguly (blad => zestaw NIE moze trafic do apki):
//  RapWord (words.json):     variant == dlugosc slowa; word/hint niepuste
//  RapConnect (puzzles.json): 4 kategorie x 4 slowa; difficulty dokladnie {0,1,2,3};
//                             16 slow unikalnych (bez wzgledu na wielkosc liter)
//  RapCrossword (crosswords.json): liczba pytan == dlugosc hasla;
//                             dla kazdego pytania answer[highlightIndex] == secretWord[i]
//
// Plus reguly wspolne: poprawny format daty, daty unikalne w pliku, id unikalne.

// Liczymy znaki po code-pointach (polskie znaki = 1).
const chars = (s) => [...String(s ?? "")];
const up = (s) => String(s ?? "").toUpperCase();
const NORM = (s) => up(String(s ?? "").trim());

// Roznica dni miedzy dwiema datami YYYY-MM-DD (wartosc bezwzgledna).
const dayDiff = (a, b) => Math.abs(Math.round((Date.parse(a + "T00:00:00Z") - Date.parse(b + "T00:00:00Z")) / 86400000));
// Minimalny odstep (w dniach) zanim ta sama odpowiedz/haslo moze wrocic w krzyzowce.
export const GAP_DAYS = 7;
// Normalizacja pytania do porownan (wielkie litery, bez interpunkcji, pojedyncze spacje).
const normQ = (s) => up(String(s ?? "")).replace(/[^\p{L}\p{N}\s]/gu, " ").replace(/\s+/g, " ").trim();
const qWords = (s) => new Set(normQ(s).split(" ").filter((w) => w.length > 2));
const jaccard = (a, b) => {
  const A = qWords(a), B = qWords(b);
  if (!A.size || !B.size) return 0;
  let inter = 0;
  for (const w of A) if (B.has(w)) inter++;
  return inter / (A.size + B.size - inter);
};
const SIM = 0.6; // prog podobienstwa pytan (powyzej => ostrzezenie o przeformulowaniu)

function isValidDate(d) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(String(d ?? ""))) return false;
  const dt = new Date(d + "T00:00:00Z");
  return !Number.isNaN(dt.getTime()) && d === dt.toISOString().slice(0, 10);
}

// Wspolne reguly: format daty, unikalnosc dat i id.
function commonChecks(items, issues, label) {
  const seenDate = new Map();
  const seenId = new Map();
  items.forEach((it, i) => {
    const at = `${label}[${i}] (${it?.date ?? "?"})`;
    if (!isValidDate(it?.date)) {
      issues.push({ level: "error", where: at, msg: `Niepoprawna data: "${it?.date}" (wymagane YYYY-MM-DD)` });
    } else if (seenDate.has(it.date)) {
      issues.push({ level: "error", where: at, msg: `Duplikat daty ${it.date} (juz uzyta w ${label}[${seenDate.get(it.date)}])` });
    } else {
      seenDate.set(it.date, i);
    }
    if (it?.id == null || !Number.isInteger(it.id)) {
      issues.push({ level: "warn", where: at, msg: `Brak/niepoprawne id` });
    } else if (seenId.has(it.id)) {
      issues.push({ level: "error", where: at, msg: `Duplikat id ${it.id} (juz uzyte w ${label}[${seenId.get(it.id)}])` });
    } else {
      seenId.set(it.id, i);
    }
  });
}

export function validateWords(data) {
  const issues = [];
  const list = data?.words;
  if (!Array.isArray(list)) return [{ level: "error", where: "words.json", msg: 'Brak tablicy "words"' }];
  commonChecks(list, issues, "words");
  list.forEach((w, i) => {
    const at = `words[${i}] (${w?.date ?? "?"})`;
    const len = chars(w?.word).length;
    if (!w?.word || len === 0) issues.push({ level: "error", where: at, msg: "Puste slowo" });
    if (!w?.hint) issues.push({ level: "warn", where: at, msg: "Brak podpowiedzi (hint)" });
    if (w?.variant == null) {
      issues.push({ level: "error", where: at, msg: "Brak pola variant" });
    } else if (w.variant !== len) {
      issues.push({ level: "error", where: at, msg: `variant=${w.variant}, ale slowo "${w.word}" ma ${len} liter` });
    }
  });
  issues.push(...crossDayIssues("words", list));
  return issues;
}

export function validatePuzzles(data) {
  const issues = [];
  const list = data?.puzzles;
  if (!Array.isArray(list)) return [{ level: "error", where: "puzzles.json", msg: 'Brak tablicy "puzzles"' }];
  commonChecks(list, issues, "puzzles");
  list.forEach((p, i) => {
    const at = `puzzles[${i}] (${p?.date ?? "?"})`;
    const cats = p?.categories;
    if (!Array.isArray(cats) || cats.length !== 4) {
      issues.push({ level: "error", where: at, msg: `Wymagane dokladnie 4 kategorie (jest ${cats?.length ?? 0})` });
      return;
    }
    const diffs = [];
    const names = new Set();
    const allWords = [];
    cats.forEach((c, ci) => {
      const cat = `${at} kat.${ci} "${c?.name ?? "?"}"`;
      if (!c?.name) issues.push({ level: "error", where: cat, msg: "Pusta nazwa kategorii" });
      else if (names.has(c.name)) issues.push({ level: "error", where: cat, msg: "Zduplikowana nazwa kategorii" });
      else names.add(c.name);
      if (!Array.isArray(c?.words) || c.words.length !== 4) {
        issues.push({ level: "error", where: cat, msg: `Kategoria musi miec 4 slowa (ma ${c?.words?.length ?? 0})` });
      } else {
        c.words.forEach((w) => {
          if (!w || !String(w).trim()) issues.push({ level: "error", where: cat, msg: "Puste slowo w kategorii" });
          allWords.push(up(String(w).trim()));
        });
      }
      diffs.push(c?.difficulty);
    });
    const sortedDiffs = [...diffs].sort();
    if (JSON.stringify(sortedDiffs) !== JSON.stringify([0, 1, 2, 3])) {
      issues.push({ level: "error", where: at, msg: `difficulty musi byc {0,1,2,3} po jednym (jest [${diffs.join(", ")}])` });
    }
    // Unikalnosc 16 slow (bez wzgledu na wielkosc liter)
    const counts = new Map();
    allWords.forEach((w) => counts.set(w, (counts.get(w) ?? 0) + 1));
    [...counts.entries()].filter(([, n]) => n > 1).forEach(([w, n]) => {
      issues.push({ level: "error", where: at, msg: `Slowo "${w}" powtarza sie ${n}x (slowa musza byc unikalne)` });
    });
  });
  issues.push(...crossDayIssues("puzzles", list));
  return issues;
}

export function validateCrosswords(data) {
  const issues = [];
  const list = data?.crosswords;
  if (!Array.isArray(list)) return [{ level: "error", where: "crosswords.json", msg: 'Brak tablicy "crosswords"' }];
  commonChecks(list, issues, "crosswords");
  list.forEach((cw, i) => {
    const at = `crosswords[${i}] (${cw?.date ?? "?"})`;
    const secret = chars(up(cw?.secretWord));
    if (secret.length === 0) {
      issues.push({ level: "error", where: at, msg: "Puste haslo (secretWord)" });
      return;
    }
    const clues = cw?.clues;
    if (!Array.isArray(clues)) {
      issues.push({ level: "error", where: at, msg: "Brak listy clues" });
      return;
    }
    if (clues.length !== secret.length) {
      issues.push({ level: "error", where: at, msg: `Liczba pytan (${clues.length}) musi rownac sie dlugosci hasla "${cw.secretWord}" (${secret.length})` });
    }
    clues.forEach((cl, ci) => {
      const where = `${at} pyt.${ci + 1}`;
      if (!cl?.question) issues.push({ level: "warn", where, msg: "Brak tresci pytania" });
      const ans = chars(up(cl?.answer));
      if (ans.length === 0) {
        issues.push({ level: "error", where, msg: "Pusta odpowiedz" });
        return;
      }
      const hi = cl?.highlightIndex;
      if (!Number.isInteger(hi) || hi < 0 || hi >= ans.length) {
        issues.push({ level: "error", where, msg: `highlightIndex=${hi} poza zakresem 0..${ans.length - 1} dla "${cl.answer}"` });
        return;
      }
      // Twarda regula krzyzowki: zaznaczona litera musi tworzyc i-ta litere hasla
      if (ci < secret.length) {
        const expected = secret[ci];
        const actual = ans[hi];
        if (actual !== expected) {
          issues.push({
            level: "error",
            where,
            msg: `"${cl.answer}"[${hi}]="${actual}", a haslo wymaga "${expected}" (litera #${ci + 1} z "${cw.secretWord}")`,
          });
        }
      }
    });
    // Odpowiedzi w jednej krzyzowce nie moga sie powtarzac
    const ansSeen = new Map();
    clues.forEach((cl, ci) => {
      const a = NORM(cl.answer);
      if (!a) return;
      if (ansSeen.has(a)) issues.push({ level: "error", where: `${at} pyt.${ci + 1}`, msg: `Odpowiedz "${a}" powtarza sie w tej krzyzowce` });
      else ansSeen.set(a, ci);
    });
  });
  issues.push(...crossDayIssues("crosswords", list));
  return issues;
}

// === Globalna unikalnosc miedzy dniami ===
// Gracz wchodzi codziennie - zadne haslo/odpowiedz/slowo nie moze sie powtorzyc
// miedzy roznymi dniami w obrebie jednego trybu.

// Wykrywa powtorki tego samego tokenu w roznych dniach (caly plik).
export function crossDayIssues(kind, items) {
  const out = [];
  const reg = (tokensOf, level, label) => {
    const map = new Map(); // token -> Set(dat)
    items.forEach((it) => {
      [...new Set(tokensOf(it))].forEach((tok) => {
        if (!tok) return;
        if (!map.has(tok)) map.set(tok, new Set());
        map.get(tok).add(it.date);
      });
    });
    for (const [tok, dates] of map) {
      if (dates.size > 1) {
        out.push({ level, where: `${kind} (powtorka)`, msg: `${label} "${tok}" powtarza sie w dniach: ${[...dates].sort().join(", ")}` });
      }
    }
  };
  // Powtorka tego samego tokenu dozwolona, ale dopiero po odstepie >= GAP_DAYS.
  const gapIssues = (tokensOf, label) => {
    const map = new Map(); // token -> Set(dates)
    items.forEach((it) => {
      [...new Set(tokensOf(it))].forEach((tok) => {
        if (!tok) return;
        if (!map.has(tok)) map.set(tok, new Set());
        map.get(tok).add(it.date);
      });
    });
    for (const [tok, set] of map) {
      const dates = [...set].sort();
      for (let i = 1; i < dates.length; i++) {
        const d = dayDiff(dates[i], dates[i - 1]);
        if (d < GAP_DAYS) out.push({ level: "error", where: `${kind} (za blisko)`, msg: `${label} "${tok}" powtarza sie zbyt blisko: ${dates[i - 1]} i ${dates[i]} (odstep ${d} dni, wymagane >= ${GAP_DAYS})` });
      }
    }
  };
  if (kind === "words") {
    reg((it) => [NORM(it.word)], "error", "Slowo");
  } else if (kind === "puzzles") {
    // Connect: powtorka slowa to tylko ostrzezenie (nazwiska raperow naturalnie wracaja
    // w innych ukladach); pilnujemy raczej powtorek calych kategorii/ukladow.
    reg((it) => (it.categories || []).flatMap((c) => (c.words || []).map(NORM)), "warn", "Slowo");
    reg((it) => (it.categories || []).map((c) => NORM(c.name)), "warn", "Nazwa kategorii");
  } else {
    // RapCrossword:
    //  - PYTANIA: to samo pytanie (po normalizacji) w innym dniu = twardy blad; podobne (przeformulowane) = ostrzezenie.
    //  - HASLO i ODPOWIEDZI: moga wrocic, ale tylko po odstepie >= GAP_DAYS (nigdy dzien po dniu).
    const qs = [];
    items.forEach((it) => (it.clues || []).forEach((c) => { if (c?.question) qs.push({ raw: c.question, n: normQ(c.question), date: it.date }); }));
    const qMap = new Map();
    qs.forEach((q) => { if (!qMap.has(q.n)) qMap.set(q.n, new Set()); qMap.get(q.n).add(q.date); });
    for (const [, dates] of qMap) {
      if (dates.size > 1) out.push({ level: "error", where: `${kind} (powtorka pytania)`, msg: `To samo pytanie w dniach: ${[...dates].sort().join(", ")}` });
    }
    for (let i = 0; i < qs.length; i++) for (let j = i + 1; j < qs.length; j++) {
      if (qs[i].date === qs[j].date || qs[i].n === qs[j].n) continue;
      if (jaccard(qs[i].raw, qs[j].raw) >= SIM) out.push({ level: "warn", where: `${kind} (podobne pytania)`, msg: `Podobne pytania (${qs[i].date} vs ${qs[j].date}): "${qs[i].raw}" ~ "${qs[j].raw}"` });
    }
    gapIssues((it) => [NORM(it.secretWord)], "Haslo");
    gapIssues((it) => (it.clues || []).map((c) => NORM(c.answer)), "Odpowiedz");
  }
  return out;
}

// Sprawdza pojedynczy wpis wzgledem ISTNIEJACEGO korpusu (inne dni).
export function crossCheck(kind, entry, items) {
  const others = items.filter((it) => it.date !== entry.date);
  const out = [];
  const usedMap = (tokensOf) => {
    const m = new Map();
    others.forEach((it) => tokensOf(it).forEach((t) => { if (t && !m.has(t)) m.set(t, it.date); }));
    return m;
  };
  const check = (entryToks, map, level, label) => {
    [...new Set(entryToks)].forEach((t) => {
      if (t && map.has(t)) out.push({ level, where: `${kind} (${entry.date})`, msg: `${label} "${t}" juz uzyte w dniu ${map.get(t)}` });
    });
  };
  if (kind === "words") {
    check([NORM(entry.word)], usedMap((it) => [NORM(it.word)]), "error", "Slowo");
  } else if (kind === "puzzles") {
    const ew = (entry.categories || []).flatMap((c) => (c.words || []).map(NORM));
    check(ew, usedMap((it) => (it.categories || []).flatMap((c) => (c.words || []).map(NORM))), "warn", "Slowo");
    check((entry.categories || []).map((c) => NORM(c.name)), usedMap((it) => (it.categories || []).map((c) => NORM(c.name))), "warn", "Nazwa kategorii");
  } else {
    // Pytania: ten sam tekst (po normalizacji) gdziekolwiek = blad; podobny = ostrzezenie.
    const otherQ = [];
    others.forEach((it) => (it.clues || []).forEach((c) => { if (c?.question) otherQ.push({ raw: c.question, n: normQ(c.question), date: it.date }); }));
    (entry.clues || []).forEach((c, ci) => {
      if (!c?.question) return;
      const en = normQ(c.question);
      const exact = otherQ.find((o) => o.n === en);
      if (exact) out.push({ level: "error", where: `${kind} (${entry.date}) pyt.${ci + 1}`, msg: `Pytanie "${c.question}" juz uzyte w dniu ${exact.date}` });
      else { const sim = otherQ.find((o) => jaccard(o.raw, c.question) >= SIM); if (sim) out.push({ level: "warn", where: `${kind} (${entry.date}) pyt.${ci + 1}`, msg: `Pytanie podobne do "${sim.raw}" z dnia ${sim.date}` }); }
    });
    // Haslo + odpowiedzi: powtorka dozwolona tylko z odstepem >= GAP_DAYS.
    const gapAgainst = (tok, tokensOf, label) => {
      if (!tok) return;
      others.forEach((it) => {
        if (tokensOf(it).includes(tok)) {
          const d = dayDiff(it.date, entry.date);
          if (d < GAP_DAYS) out.push({ level: "error", where: `${kind} (${entry.date})`, msg: `${label} "${tok}" uzyte juz ${d} dni od dnia ${it.date} (wymagany odstep >= ${GAP_DAYS} dni)` });
        }
      });
    };
    gapAgainst(NORM(entry.secretWord), (it) => [NORM(it.secretWord)], "Haslo");
    [...new Set((entry.clues || []).map((c) => NORM(c.answer)))].forEach((a) => gapAgainst(a, (it) => (it.clues || []).map((x) => NORM(x.answer)), "Odpowiedz"));
  }
  return out;
}

// Lista juz uzytych tokenow - dla podpowiedzi generatorowi (czego unikac).
// Dla krzyzowki: pytania unikalne ZAWSZE; haslo/odpowiedzi tylko z ostatnich GAP_DAYS
// wzgledem refDate (jesli podana) - starsze moga wrocic.
export function usedTokens(kind, items, refDate) {
  if (kind === "words") return { words: [...new Set(items.map((it) => NORM(it.word)))] };
  if (kind === "puzzles")
    return {
      words: [...new Set(items.flatMap((it) => (it.categories || []).flatMap((c) => (c.words || []).map(NORM))))],
      names: [...new Set(items.flatMap((it) => (it.categories || []).map((c) => NORM(c.name))))],
    };
  const recent = refDate ? items.filter((it) => dayDiff(it.date, refDate) < GAP_DAYS) : items;
  return {
    questions: [...new Set(items.flatMap((it) => (it.clues || []).map((c) => String(c.question || "").trim())).filter(Boolean))],
    secretWords: [...new Set(recent.map((it) => NORM(it.secretWord)))],
    answers: [...new Set(recent.flatMap((it) => (it.clues || []).map((c) => NORM(c.answer))))],
    gapDays: GAP_DAYS,
  };
}

export const VALIDATORS = {
  words: validateWords,
  puzzles: validatePuzzles,
  crosswords: validateCrosswords,
};

// Waliduje pojedynczy wpis (dla zapisu z serwera/skilla) — opakowuje go w mini-liste.
export function validateEntry(kind, entry) {
  const wrap = { words: "words", puzzles: "puzzles", crosswords: "crosswords" }[kind];
  return VALIDATORS[kind]({ [wrap]: [entry] }).filter((x) => x.level === "error");
}
