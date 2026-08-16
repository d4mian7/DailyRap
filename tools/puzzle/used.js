// Wypisuje juz uzyte tokeny (slowa/hasla/odpowiedzi), zeby unikac powtorek.
// Uzycie: node used.js <word|connect|crossword>
import { readItems } from "./src/io.js";
import { usedTokens } from "./src/schema.js";

const ALIAS = { word: "words", words: "words", connect: "puzzles", puzzles: "puzzles", crossword: "crosswords", crosswords: "crosswords" };
const kind = ALIAS[process.argv[2]];
if (!kind) {
  console.error("Podaj tryb: word | connect | crossword");
  process.exit(2);
}
console.log(JSON.stringify(usedTokens(kind, readItems(kind)), null, 2));
