# UniSpaceX One-Click Android App Installer
$ErrorActionPreference = "Continue"

$adbPath = "C:\Users\user\AppData\Local\Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adbPath)) {
    $adbCmd = Get-Command "adb" -ErrorAction SilentlyContinue
    if ($adbCmd) {
        $adbPath = $adbCmd.Source
    } else {
        Write-Host "Could not find adb.exe at $adbPath or in system PATH." -ForegroundColor Red
        exit 1
    }
}

Write-Host "Connecting to ADB ($adbPath)..." -ForegroundColor Cyan
& $adbPath start-server | Out-Null
Start-Sleep -Seconds 2

$rawDevices = & $adbPath devices
Write-Host ($rawDevices -join "`n")

$online = $rawDevices | Where-Object { $_ -match "\bdevice\b" -and $_ -notmatch "List of" }
$offline = $rawDevices | Where-Object { $_ -match "\boffline\b" }
$unauthorized = $rawDevices | Where-Object { $_ -match "\bunauthorized\b" }

if ($unauthorized) {
    Write-Host "`nDevice is UNAUTHORIZED. Look at phone screen and tap 'Always allow from this computer' -> 'Allow'." -ForegroundColor Yellow
    exit 1
}

if ($offline -and -not $online) {
    Write-Host "`nDevice is OFFLINE. Trying adb reconnect..." -ForegroundColor Yellow
    & $adbPath reconnect
    Start-Sleep -Seconds 2
    $rawDevices = & $adbPath devices
    $online = $rawDevices | Where-Object { $_ -match "\bdevice\b" -and $_ -notmatch "List of" }
}

if (-not $online) {
    Write-Host "`nNo online Android device detected." -ForegroundColor Red
    Write-Host "Steps to fix:" -ForegroundColor Yellow
    Write-Host " 1. Unlock your phone."
    Write-Host " 2. Ensure USB Debugging is ON in Developer Options."
    Write-Host " 3. On Realme/Oppo/Xiaomi: enable 'Install via USB' in Developer Options."
    Write-Host " 4. Check USB cable connection."
    exit 1
}

Write-Host "`nFound online device! Proceeding with installation..." -ForegroundColor Green

$clientApk = "d:\unispacex\android_app\build\outputs\apk\debug\app-debug.apk"
$adminApk = "d:\unispacex\admin_android\build\outputs\apk\debug\admin-debug.apk"

if (Test-Path $clientApk) {
    Write-Host "`nInstalling UniSpaceX Client App ($clientApk)..." -ForegroundColor Cyan
    & $adbPath install -r -d $clientApk
} else {
    Write-Host "Client APK not found at $clientApk" -ForegroundColor Red
}

if (Test-Path $adminApk) {
    Write-Host "`nInstalling UniSpaceX Admin App ($adminApk)..." -ForegroundColor Cyan
    & $adbPath install -r -d $adminApk
} else {
    Write-Host "Admin APK not found at $adminApk" -ForegroundColor Red
}

Write-Host "`nLaunching UniSpaceX App on phone..." -ForegroundColor Cyan
& $adbPath shell monkey -p com.example -c android.intent.category.LAUNCHER 1 | Out-Null

Write-Host "Installation completed!" -ForegroundColor Green
