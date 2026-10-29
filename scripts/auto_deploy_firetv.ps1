$target = "10.0.0.169:5555"
$rootDir = Split-Path -Parent $PSScriptRoot
$apk = Join-Path $rootDir "launcher\build\clean-launcher-release.apk"
$helper = Join-Path $PSScriptRoot "clean_home_helper.sh"

if (Test-Path $helper) {
    $raw = [System.IO.File]::ReadAllText($helper).Replace("`r`n", "`n")
    [System.IO.File]::WriteAllText($helper, $raw, (New-Object System.Text.UTF8Encoding($false)))
}

while ($true) {
    try {
        $mac = [byte[]]@(0xAC,0xCC,0xFC,0x40,0x78,0x13)
        $packet = [byte[]](,0xFF * 6) + ($mac * 16)
        $udp = New-Object System.Net.Sockets.UdpClient
        $udp.Connect("10.0.0.255", 9)
        [void]$udp.Send($packet, $packet.Length)
        $udp.Close()
    } catch {}

    $conn = adb connect $target 2>&1 | Out-String
    $devs = adb devices 2>&1 | Out-String
    if ($devs -match "10\.0\.0\.169:5555\s+device") {
        Write-Output "Connected to Fire TV ($target)! Installing latest APK and helper daemon..."
        adb -s $target install -r -d $apk
        adb -s $target shell settings put secure enabled_accessibility_services com.cleantv.launcher/.HomeButtonAccessibilityService
        adb -s $target shell settings put secure accessibility_enabled 1
        adb -s $target shell pm grant com.cleantv.launcher android.permission.ACCESS_FINE_LOCATION
        adb -s $target shell pm grant com.cleantv.launcher android.permission.ACCESS_COARSE_LOCATION
        adb -s $target shell pm grant com.cleantv.launcher android.permission.WRITE_SECURE_SETTINGS
        adb -s $target shell pm grant com.cleantv.launcher android.permission.READ_LOGS
        if (Test-Path $helper) {
            adb -s $target push $helper /data/local/tmp/clean_home_helper.sh
            adb -s $target shell "chmod 755 /data/local/tmp/clean_home_helper.sh && setsid /data/local/tmp/clean_home_helper.sh </dev/null >/data/local/tmp/helper.log 2>&1 &"
        }
        adb -s $target shell am start -n com.cleantv.launcher/.MainActivity
        Write-Output "FIRETV_AUTO_DEPLOY_COMPLETE"
        break
    }
    Start-Sleep -Seconds 3
}
