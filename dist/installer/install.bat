<# :
@echo off
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-Expression (Get-Content '%~f0' -Raw)"
pause
exit /b
#>

Add-Type -AssemblyName System.Windows.Forms

Write-Host "Eclipse Dropins mappa keresese..." -ForegroundColor Cyan

$dropinsPath = $null

# 1. Ha fut az Eclipse, abból azonnal kiszámoljuk a dropins útvonalát
$eclipseProc = Get-Process -Name "eclipse" -ErrorAction SilentlyContinue
if ($eclipseProc) {
    $exePath = $eclipseProc.MainModule.FileName
    $possibleDropins = Join-Path (Split-Path $exePath) "dropins"
    if (Test-Path $possibleDropins) {
        $dropinsPath = $possibleDropins
        Write-Host "Megtalalva a futo Eclipse alapján: $dropinsPath" -ForegroundColor Green
    }
}

# 2. Ha nem fut, ellenőrizzük a szokásos Windows telepítési helyeket
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
            Write-Host "Megtalalva az alapertelmezett utvonalon: $dropinsPath" -ForegroundColor Green
            break
        }
    }
}

# 3. Ha automatikusan nem találta meg, felugró ablakban kérdezzük meg
if (-not $dropinsPath) {
    Write-Host "Nem talalhato automatikusan az Eclipse. Kerlek valaszd ki a 'dropins' mappat!" -ForegroundColor Yellow
    
    $folderBrowser = New-Object System.Windows.Forms.FolderBrowserDialog
    $folderBrowser.Description = "Válaszd ki az Eclipse 'dropins' mappáját"
    
    if ($folderBrowser.ShowDialog() -eq [System.Windows.Forms.DialogResult]::OK) {
        $dropinsPath = $folderBrowser.SelectedPath
    } else {
        Write-Host "Telepites megszakitva." -ForegroundColor Red
        return
    }
}

# 4. Megkeressük a JAR fájlt az install.bat mellett
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$jarFile = Get-ChildItem -Path $scriptDir -Filter "*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1

if ($jarFile) {
    # Átmozgatjuk (Move-Item) a jar fájlt a dropins mappába
    Move-Item -Path $jarFile.FullName -Destination $dropinsPath -Force
    
    Write-Host "`n----------------------------------------" -ForegroundColor Green
    Write-Host "SIKERES TELEPITES!" -ForegroundColor Green
    Write-Host "A(z) '$($jarFile.Name)' atmozgatva ide:"
    Write-Host "$dropinsPath" -ForegroundColor Gray
    Write-Host "Inditsd ujra az Eclipse-t a plugin aktiválásához!" -ForegroundColor Yellow
    Write-Host "----------------------------------------" -ForegroundColor Green
} else {
    Write-Host "`nHiba: Nem található .jar fájl az install.bat mellett!" -ForegroundColor Red
}