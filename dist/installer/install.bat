<# :
@echo off
cd /d "%~dp0"
set "INSTALLER_DIR=%~dp0"
chcp 65001 >nul
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-Expression (Get-Content '%~f0' -Raw)"
pause
exit /b
#>

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Add-Type -AssemblyName System.Windows.Forms

Write-Host "Eclipse Dropins mappa keresése..." -ForegroundColor Cyan

$dropinsPath = $null

# 1. Ha fut az Eclipse, abból határozzuk meg a dropins mappát
$eclipseProc = Get-Process -Name "eclipse" -ErrorAction SilentlyContinue
if ($eclipseProc) {
    $exePath = $eclipseProc.MainModule.FileName
    $possibleDropins = Join-Path (Split-Path $exePath) "dropins"
    if (Test-Path $possibleDropins) {
        $dropinsPath = $possibleDropins
        Write-Host "Megtalálva a futó Eclipse alapján: $dropinsPath" -ForegroundColor Green
    }
}

# 2. Ha nem fut, ellenőrizzük a szokásos Windows útvonalakat
if (-not $dropinsPath) {
    $commonPaths = @(
        "$env:LOCALAPPDATA\Programs\Eclipse\dropins",
        "$env:USERPROFILE\eclipse\java-*\eclipse\dropins",
        "$env:USERPROFILE\eclipse\committers-*\eclipse\dropins",
        "$env:USERPROFILE\eclipse\jee-*\eclipse\dropins",
        "C:\Program Files\Eclipse\dropins",
        "C:\eclipse\dropins"
    )

    foreach ($path in $commonPaths) {
        $expanded = Resolve-Path $path -ErrorAction SilentlyContinue
        if ($expanded -and (Test-Path $expanded.Path)) {
            $dropinsPath = $expanded.Path
            Write-Host "Megtalálva az alapértelmezett útvonalon: $dropinsPath" -ForegroundColor Green
            break
        }
    }
}

# 3. Kézi választás, ha nem található
if (-not $dropinsPath) {
    Write-Host "Nem található automatikusan az Eclipse. Kérlek válaszd ki a 'dropins' mappát!" -ForegroundColor Yellow
    
    $folderBrowser = New-Object System.Windows.Forms.FolderBrowserDialog
    $folderBrowser.Description = "Válaszd ki az Eclipse 'dropins' mappáját"
    
    if ($folderBrowser.ShowDialog() -eq [System.Windows.Forms.DialogResult]::OK) {
        $dropinsPath = $folderBrowser.SelectedPath
    } else {
        Write-Host "Telepítés megszakítva." -ForegroundColor Red
        return
    }
}

# 4. Pontos mappa meghatározása a Batch által átadott környezeti változóból
$scriptDir = $env:INSTALLER_DIR
if (-not $scriptDir -or -not (Test-Path $scriptDir)) {
    $scriptDir = (Get-Location).Path
}

$jarFile = Get-ChildItem -Path $scriptDir -Filter "*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1

if ($jarFile) {
    Move-Item -Path $jarFile.FullName -Destination $dropinsPath -Force
    
    Write-Host "`n----------------------------------------" -ForegroundColor Green
    Write-Host "SIKERES TELEPÍTÉS!" -ForegroundColor Green
    Write-Host "A(z) '$($jarFile.Name)' áthelyezve ide:"
    Write-Host "$dropinsPath" -ForegroundColor Gray
    Write-Host "Indítsd újra az Eclipse-t a plugin aktiválásához!" -ForegroundColor Yellow
    Write-Host "----------------------------------------" -ForegroundColor Green
} else {
    Write-Host "`nHiba: Nem található .jar fájl ebben a mappában: $scriptDir" -ForegroundColor Red
}