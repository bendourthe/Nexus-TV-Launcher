param(
    [string]$MacAddress = "AC:CC:FC:40:78:13",
    [string]$IpAddress = "255.255.255.255"
)

try {
    $cleanMac = $MacAddress -replace "[:-]", ""
    $macBytes = [byte[]]@()
    for ($i = 0; $i -lt $cleanMac.Length; $i += 2) {
        $macBytes += [Convert]::ToByte($cleanMac.Substring($i, 2), 16)
    }

    $packet = [byte[]](@(0xFF) * 6)
    for ($i = 0; $i -lt 16; $i++) {
        $packet += $macBytes
    }

    $udp = New-Object System.Net.Sockets.UdpClient
    $udp.Connect([System.Net.IPAddress]::Parse($IpAddress), 9)
    $null = $udp.Send($packet, $packet.Length)
    $udp.Close()
    Write-Output "Wake-on-LAN magic packet sent to $MacAddress"
} catch {
    Write-Error "WOL failed: $_"
}
