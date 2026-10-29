$targetIp = "10.0.0.169:5555"
$apkPath = "C:\Users\bdour\.gemini\antigravity-ide\scratch\android-tv-clean-launcher\launcher\build\clean-launcher-release.apk"
$screenshotPath = "C:\Users\bdour\.gemini\antigravity-ide\brain\57236744-3d86-4ee8-aa16-e55136f26fe3\.tempmediaStorage\verify_latest.png"

Write-Host "Attempting ADB connection to $targetIp..."
$connected = $false
for ($i = 1; $i -le 3; $i++) {
    $res = adb connect $targetIp
    Write-Host "Attempt ${i}: $res"
    if ($res -match "connected to") {
        $connected = $true
        break
    }
    Start-Sleep -Seconds 1
}

if (-not $connected) {
    Write-Error "Could not connect to $targetIp. Please ensure the TV is powered on and awake."
    exit 1
}

Write-Host "Installing $apkPath..."
$installRes = adb -s $targetIp install -r $apkPath
Write-Host "Install result: $installRes"

Write-Host "Configuring accessibility service..."
adb -s $targetIp shell settings put secure accessibility_enabled 1
adb -s $targetIp shell settings put secure enabled_accessibility_services com.cleantv.launcher/.HomeButtonAccessibilityService
adb -s $targetIp shell pm grant com.cleantv.launcher android.permission.WRITE_SECURE_SETTINGS
adb -s $targetIp shell pm grant com.cleantv.launcher android.permission.SYSTEM_ALERT_WINDOW

Write-Host "Starting Clean Launcher..."
adb -s $targetIp shell am start -n com.cleantv.launcher/.MainActivity

Start-Sleep -Seconds 2
Write-Host "Capturing verification screenshot..."
adb -s $targetIp exec-out screencap -p > $screenshotPath
Write-Host "Deployment complete! Screenshot saved to $screenshotPath"
