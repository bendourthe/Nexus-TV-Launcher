$target = "10.0.0.169:5555"
$apk = "launcher\build\clean-launcher-release.apk"

Write-Output "Waiting for Fire TV authorization on $target..."
$maxAttempts = 12
$attempt = 0
$authorized = $false

while ($attempt -lt $maxAttempts) {
    $devices = adb devices
    if ($devices -match "$target\s+device") {
        $authorized = $true
        break
    }
    Start-Sleep -Seconds 2
    $attempt++
}

if (-not $authorized) {
    Write-Output "STILL_UNAUTHORIZED"
    exit 1
}

Write-Output "Device authorized! Installing Clean TV Launcher..."
adb -s $target install -r -d $apk

Write-Output "Enabling Home Button Accessibility Service..."
adb -s $target shell settings put secure enabled_accessibility_services com.cleantv.launcher/.HomeButtonAccessibilityService
adb -s $target shell settings put secure accessibility_enabled 1

Write-Output "Granting permissions..."
adb -s $target shell pm grant com.cleantv.launcher android.permission.ACCESS_FINE_LOCATION
adb -s $target shell pm grant com.cleantv.launcher android.permission.ACCESS_COARSE_LOCATION
adb -s $target shell pm grant com.cleantv.launcher android.permission.WRITE_SECURE_SETTINGS
adb -s $target shell pm grant com.cleantv.launcher android.permission.READ_LOGS

Write-Output "Deploying UID-2000 Instant Home Helper daemon..."
$helperPath = Join-Path $PSScriptRoot "clean_home_helper.sh"
if (Test-Path $helperPath) {
    $raw = [System.IO.File]::ReadAllText($helperPath).Replace("`r`n", "`n")
    [System.IO.File]::WriteAllText($helperPath, $raw, (New-Object System.Text.UTF8Encoding($false)))
    adb -s $target push $helperPath /data/local/tmp/clean_home_helper.sh
    adb -s $target shell "chmod 755 /data/local/tmp/clean_home_helper.sh && setsid /data/local/tmp/clean_home_helper.sh </dev/null >/data/local/tmp/helper.log 2>&1 &"
}

Write-Output "Starting Clean Launcher on Fire TV..."
adb -s $target shell am start -n com.cleantv.launcher/.MainActivity

Start-Sleep -Seconds 2
Write-Output "Capturing verification screenshot..."
adb -s $target shell screencap -p /data/local/tmp/firetv_screen.png
adb -s $target pull /data/local/tmp/firetv_screen.png firetv_screen.png

Write-Output "FIRETV_INSTALL_SUCCESSFUL"
