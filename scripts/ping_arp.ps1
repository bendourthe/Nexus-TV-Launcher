$ips = @('10.0.0.1','10.0.0.4','10.0.0.18','10.0.0.36','10.0.0.38','10.0.0.40','10.0.0.46','10.0.0.62','10.0.0.86','10.0.0.93','10.0.0.96','10.0.0.128','10.0.0.132','10.0.0.145','10.0.0.159','10.0.0.169','10.0.0.171','10.0.0.201','10.0.0.218','10.0.0.240')
foreach ($ip in $ips) {
    $p = New-Object System.Net.NetworkInformation.Ping
    try {
        $reply = $p.Send($ip, 300)
        if ($reply.Status -eq 'Success') {
            Write-Output "PING OK: $ip ($($reply.RoundtripTime)ms)"
        }
    } catch {}
}
Write-Output "Done."
