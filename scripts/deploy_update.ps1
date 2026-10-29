param(
    [string]$TargetIp = "10.0.0.169"
)

$device = "${TargetIp}:5555"
Write-Output "=== Deploying Clean Launcher Update to $device ==="

# 1. Connect
$connectResult = adb connect $device
Write-Output $connectResult
Start-Sleep -Milliseconds 800

$devicesOut = (adb devices | Out-String)
if ($devicesOut -notmatch "10\.0\.0\.169:5555\s+device") {
    Write-Warning "Device not connected or not ready."
    exit 1
}

# 2. Install APK
$apk = "launcher\build\clean-launcher-release.apk"
Write-Output "Installing $apk..."
$installResult = adb -s $device install -r $apk
Write-Output $installResult

# 3. Grant Permissions
Write-Output "Granting permissions..."
adb -s $device shell "pm grant com.cleantv.launcher android.permission.STOP_APP_SWITCHES" | Out-Null
adb -s $device shell "pm grant com.cleantv.launcher android.permission.SYSTEM_ALERT_WINDOW" | Out-Null
adb -s $device shell "pm grant com.cleantv.launcher com.android.providers.tv.permission.ACCESS_ALL_EPG_DATA" | Out-Null
adb -s $device shell "pm grant com.cleantv.launcher com.android.providers.tv.permission.READ_EPG_DATA" | Out-Null
adb -s $device shell "appops set com.cleantv.launcher SYSTEM_ALERT_WINDOW allow" | Out-Null

# Enable notification listener for live app recommendation feeds
adb -s $device shell "cmd notification --user 0 allow_listener com.cleantv.launcher/.WatchNextNotificationListener" | Out-Null
adb -s $device shell "settings put secure enabled_notification_listeners com.cleantv.launcher/.WatchNextNotificationListener" | Out-Null

# 4. Set as default home activity
Write-Output "Setting default home launcher..."
$homeResult = adb -s $device shell "cmd package set-home-activity --user 0 com.cleantv.launcher/.MainActivity"
Write-Output "Set home activity: $homeResult"

# 5. Enable Accessibility Service
Write-Output "Enabling instant home redirect accessibility service..."
adb -s $device shell "settings put secure enabled_accessibility_services com.cleantv.launcher/.HomeButtonAccessibilityService" | Out-Null
adb -s $device shell "settings put secure accessibility_enabled 1" | Out-Null

# 6. Launch Clean Launcher
Write-Output "Launching Clean Launcher..."
adb -s $device shell "am start -n com.cleantv.launcher/.MainActivity" | Out-Null
Start-Sleep -Seconds 2

# 7. Take Screenshot
$screenPath = "C:\Users\bdour\.gemini\antigravity-ide\brain\57236744-3d86-4ee8-aa16-e55136f26fe3\clean_tv_update.png"
adb -s $device shell "screencap -p /sdcard/screen.png" | Out-Null
adb -s $device pull /sdcard/screen.png $screenPath | Out-Null
Write-Output "Deployment complete! Screenshot captured at $screenPath"
