Write-Output "Starting comprehensive backup..."
$backupDir = "backup"
New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

Write-Output "Backing up installed packages..."
.\scripts\adb_cmd.ps1 shell "pm list packages -f -u" | Out-File -FilePath "$backupDir/packages_all.txt" -Encoding utf8

Write-Output "Backing up disabled packages..."
.\scripts\adb_cmd.ps1 shell "pm list packages -d" | Out-File -FilePath "$backupDir/packages_disabled.txt" -Encoding utf8

Write-Output "Backing up getprop..."
.\scripts\adb_cmd.ps1 shell "getprop" | Out-File -FilePath "$backupDir/getprop.txt" -Encoding utf8

Write-Output "Backing up settings (system, secure, global)..."
.\scripts\adb_cmd.ps1 shell "settings list system" | Out-File -FilePath "$backupDir/settings_system.txt" -Encoding utf8
.\scripts\adb_cmd.ps1 shell "settings list secure" | Out-File -FilePath "$backupDir/settings_secure.txt" -Encoding utf8
.\scripts\adb_cmd.ps1 shell "settings list global" | Out-File -FilePath "$backupDir/settings_global.txt" -Encoding utf8

Write-Output "Backing up HOME activities..."
.\scripts\adb_cmd.ps1 shell "cmd package query-activities -a android.intent.action.MAIN -c android.intent.category.HOME" | Out-File -FilePath "$backupDir/home_activities.txt" -Encoding utf8

Write-Output "Backing up Wi-Fi dump..."
.\scripts\adb_cmd.ps1 shell "dumpsys wifi" | Out-File -FilePath "$backupDir/wifi_dump.txt" -Encoding utf8

Write-Output "Backup completed successfully!"
Get-ChildItem -Path $backupDir | Select-Object Name, Length
