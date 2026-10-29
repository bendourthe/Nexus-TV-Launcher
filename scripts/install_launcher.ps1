$ErrorActionPreference = "Stop"

$apk = "launcher\build\clean-launcher-release.apk"
if (-not (Test-Path $apk)) {
    Write-Error "APK not found at $apk. Please run build_launcher.ps1 first."
}

Write-Output "Pushing Clean Launcher APK to TV..."
.\scripts\adb_cmd.ps1 push $apk /data/local/tmp/clean_launcher.apk

Write-Output "Installing APK via pm install -r..."
$res = .\scripts\adb_cmd.ps1 shell "pm install -r -d /data/local/tmp/clean_launcher.apk"
Write-Output $res

.\scripts\adb_cmd.ps1 shell "rm -f /data/local/tmp/clean_launcher.apk"

if ($res -match "Success") {
    Write-Output "Clean Launcher installed successfully on TV!"
} else {
    Write-Error "Failed to install Clean Launcher: $res"
}
