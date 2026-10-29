param(
    [Parameter(Mandatory = $true)]
    [string]$Name
)
$remoteRaw = "/sdcard/shot.raw"
$localRaw = "screenshots/tmp.raw"
$localPath = "screenshots/$Name"

& .\scripts\adb_cmd.ps1 shell "screencap $remoteRaw"
& .\scripts\adb_cmd.ps1 pull $remoteRaw $localRaw
& .\scripts\adb_cmd.ps1 shell "rm -f $remoteRaw"

if (Test-Path $localRaw) {
    python -c "import PIL.Image; data=open('$localRaw','rb').read(); w,h = int.from_bytes(data[0:4],'little'), int.from_bytes(data[4:8],'little'); img=PIL.Image.frombytes('RGBA', (w,h), data[16:]); img.save('$localPath')"
    Remove-Item -Force $localRaw -ErrorAction SilentlyContinue
    if (Test-Path $localPath) {
        Write-Output "Screenshot saved to $localPath ($( (Get-Item $localPath).Length ) bytes)"
    } else {
        Write-Error "Failed to save PNG $localPath"
    }
} else {
    Write-Error "Failed to pull raw screenshot $localRaw"
}
