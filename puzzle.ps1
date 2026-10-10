# puzzle.ps1 - narzedzia do zestawow DailyRap
#
#   .\puzzle.ps1 plan       -> otwiera kalendarz w przegladarce (serwer lokalny)
#   .\puzzle.ps1 check      -> waliduje wszystkie pliki zestawow
#   .\puzzle.ps1 check word -> waliduje tylko jeden tryb (word|connect|crossword -> plik)

param([string]$cmd = "plan", [string]$arg = "")

$tools = Join-Path $PSScriptRoot "tools\puzzle"

switch ($cmd.ToLower()) {
    "plan" {
        Start-Process "http://localhost:5173"
        node "--env-file-if-exists=$(Join-Path $tools '.env')" (Join-Path $tools "server.js")
    }
    "check" {
        $map = @{ word = "words"; connect = "puzzles"; crossword = "crosswords" }
        $target = if ($arg -and $map.ContainsKey($arg)) { $map[$arg] } else { $arg }
        node (Join-Path $tools "validate.js") $target
    }
    default { Write-Host "Dostepne: plan | check [word|connect|crossword]" -ForegroundColor Yellow }
}
