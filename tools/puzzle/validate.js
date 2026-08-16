// CLI walidator. Uzycie:
//   node validate.js            -> waliduje wszystkie 3 pliki
//   node validate.js crosswords -> tylko jeden tryb
import { read, FILES } from "./src/io.js";
import { VALIDATORS } from "./src/schema.js";

const only = process.argv[2];
const kinds = only ? [only] : Object.keys(FILES);

let errors = 0;
let warns = 0;

for (const kind of kinds) {
  if (!VALIDATORS[kind]) {
    console.log(`Nieznany tryb: ${kind} (dostepne: ${Object.keys(FILES).join(", ")})`);
    process.exit(2);
  }
  let issues;
  try {
    issues = VALIDATORS[kind](read(kind));
  } catch (e) {
    console.log(`\n[${kind}] BLAD odczytu/parsowania: ${e.message}`);
    errors++;
    continue;
  }
  const errs = issues.filter((i) => i.level === "error");
  const wrn = issues.filter((i) => i.level === "warn");
  errors += errs.length;
  warns += wrn.length;

  const tag = errs.length ? "BLAD " : "OK   ";
  console.log(`\n[${tag}] ${FILES[kind].file}  (${errs.length} bledow, ${wrn.length} ostrzezen)`);
  for (const it of issues) {
    const mark = it.level === "error" ? "  x" : "  !";
    console.log(`${mark} ${it.where}: ${it.msg}`);
  }
}

console.log(`\n=== Podsumowanie: ${errors} bledow, ${warns} ostrzezen ===`);
process.exit(errors ? 1 : 0);
