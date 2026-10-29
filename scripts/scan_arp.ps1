$ips = @('10.0.0.4','10.0.0.18','10.0.0.36','10.0.0.38','10.0.0.40','10.0.0.46','10.0.0.62','10.0.0.86','10.0.0.93','10.0.0.96','10.0.0.128','10.0.0.132','10.0.0.145','10.0.0.159','10.0.0.169','10.0.0.171','10.0.0.201','10.0.0.218','10.0.0.240')
foreach ($target in $ips) {
    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $async = $client.BeginConnect($target, 5555, $null, $null)
        $ok = $async.AsyncWaitHandle.WaitOne(300, $false)
        if ($ok -and $client.Connected) {
            $client.EndConnect($async)
            Write-Output "FOUND OPEN ADB: $target"
        }
    } catch {} finally {
        $client.Close()
    }
}
Write-Output "ARP scan done."
