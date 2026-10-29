# PowerShell restore script for Android TV debloat & launcher
param(
    [string]$Device = "10.0.0.96:5555"
)

Write-Output "Connecting to $Device..."
adb connect $Device

$disabledList = "backup/disabled_by_us.txt"
if (Test-Path $disabledList) {
    $packages = Get-Content $disabledList | Where-Object { $_ -and -not $_.StartsWith("#") }
    foreach ($pkg in $packages) {
        Write-Output "Re-enabling package: $pkg"
        adb -s $Device shell "pm enable $pkg"
    }
} else {
    Write-Output "No backup/disabled_by_us.txt found."
}

Write-Output "Restoring stock Google TV launcher..."
adb -s $Device shell "pm enable com.google.android.apps.tv.launcherx"
adb -s $Device shell "cmd package set-home-activity com.google.android.apps.tv.launcherx/com.google.android.apps.tv.launcherx.home.HomeActivity"

Write-Output "Restoring default DNS configuration..."
adb -s $Device shell "settings put global private_dns_mode opportunistic"
adb -s $Device shell "settings delete global private_dns_specifier"

Write-Output "Restore completed successfully."
