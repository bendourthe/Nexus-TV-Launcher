$subnet = "10.0.0."
$port = 5555
$timeoutMs = 600

Write-Output "Scanning 10.0.0.1 - 10.0.0.254 for ADB port 5555..."
$found = New-Object System.Collections.Concurrent.ConcurrentBag[string]

$body = {
    param($i)
    $ip = "$subnet$i"
    $sock = New-Object System.Net.Sockets.Socket([System.Net.Sockets.AddressFamily]::InterNetwork, [System.Net.Sockets.SocketType]::Stream, [System.Net.Sockets.ProtocolType]::Tcp)
    try {
        $ar = $sock.BeginConnect($ip, $port, $null, $null)
        if ($ar.AsyncWaitHandle.WaitOne($timeoutMs, $false)) {
            $sock.EndConnect($ar)
            $found.Add($ip)
        }
    } catch {
    } finally {
        $sock.Close()
    }
}

$opts = New-Object System.Threading.Tasks.ParallelOptions
$opts.MaxDegreeOfParallelism = 32

[System.Threading.Tasks.Parallel]::For(1, 255, $opts, $body)

if ($found.Count -eq 0) {
    Write-Output "No open ADB ports found on 10.0.0.1-254."
} else {
    foreach ($ip in $found) {
        Write-Output "OPEN ADB: $ip"
    }
}
