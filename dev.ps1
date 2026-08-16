# dev.ps1 - build i wgrywanie DailyRap na telefon bez Android Studio
#
# Uzycie:
#   .\dev.ps1            -> build debug + instalacja + uruchomienie na telefonie
#   .\dev.ps1 build      -> tylko build debug APK
#   .\dev.ps1 install    -> build + instalacja (bez uruchamiania)
#   .\dev.ps1 run        -> build + instalacja + uruchomienie (domyslne)
#   .\dev.ps1 logcat     -> podglad logow aplikacji na zywo
#   .\dev.ps1 release    -> build niepodpisanego release APK
#   .\dev.ps1 devices    -> lista podlaczonych urzadzen

param(
    [string]$cmd = "run"
)

$ErrorActionPreference = "Stop"
$root    = $PSScriptRoot
$pkg     = "com.dailyrap.app"
$sdk     = $env:ANDROID_HOME
if (-not $sdk) { $sdk = "$env:LOCALAPPDATA\Android\Sdk" }
$adb     = Join-Path $sdk "platform-tools\adb.exe"
$gradlew = Join-Path $root "gradlew.bat"
$apk     = Join-Path $root "app\build\outputs\apk\debug\app-debug.apk"

function Require-Device {
    $lines = & $adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "\tdevice$" }
    if (-not $lines) {
        Write-Host "BLAD: brak podlaczonego telefonu." -ForegroundColor Red
        Write-Host "  1) Podlacz Samsunga przez USB" -ForegroundColor Yellow
        Write-Host "  2) Wlacz 'Debugowanie USB' w Opcjach programistycznych" -ForegroundColor Yellow
        Write-Host "  3) Na telefonie potwierdz 'Zezwol na debugowanie USB'" -ForegroundColor Yellow
        exit 1
    }
}

function Build-Debug {
    Write-Host ">> Buduje debug APK..." -ForegroundColor Cyan
    & $gradlew -p $root assembleDebug
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    Write-Host ">> Gotowe: $apk" -ForegroundColor Green
}

function Install-App {
    Require-Device
    Write-Host ">> Instaluje na telefonie..." -ForegroundColor Cyan
    & $adb install -r $apk
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

function Launch-App {
    Require-Device
    Write-Host ">> Uruchamiam aplikacje..." -ForegroundColor Cyan
    & $adb shell monkey -p $pkg -c android.intent.category.LAUNCHER 1 | Out-Null
}

switch ($cmd.ToLower()) {
    "build"   { Build-Debug }
    "install" { Build-Debug; Install-App }
    "run"     { Build-Debug; Install-App; Launch-App; Write-Host ">> Odpalone na telefonie." -ForegroundColor Green }
    "logcat"  { Require-Device; & $adb logcat --pid=$(& $adb shell pidof -s $pkg) }
    "release" {
        Write-Host ">> Buduje release APK (niepodpisany)..." -ForegroundColor Cyan
        & $gradlew -p $root assembleRelease
        Write-Host ">> APK: $root\app\build\outputs\apk\release\" -ForegroundColor Green
    }
    "devices" { & $adb devices -l }
    default   { Write-Host "Nieznana komenda: $cmd" -ForegroundColor Red; Write-Host "Dostepne: build | install | run | logcat | release | devices" }
}
