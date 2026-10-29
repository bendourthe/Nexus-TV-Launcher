param([string]$ip = "10.0.0.169", [int]$port = 5555)
$client = New-Object System.Net.Sockets.TcpClient
try {
    $async = $client.BeginConnect($ip, $port, $null, $null)
    $ok = $async.AsyncWaitHandle.WaitOne(1500, $false)
    if ($ok -and $client.Connected) {
        $client.EndConnect($async)
        Write-Output "SUCCESS: ${ip}:${port} is OPEN"
    } else {
        Write-Output "TIMEOUT: ${ip}:${port} did not respond within 1500ms"
    }
} catch {
    Write-Output "ERROR: $($_.Exception.Message)"
} finally {
    $client.Close()
}
