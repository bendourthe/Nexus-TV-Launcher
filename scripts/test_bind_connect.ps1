$localEp = New-Object System.Net.IPEndPoint([System.Net.IPAddress]::Parse('10.0.0.19'), 0)
$client = New-Object System.Net.Sockets.TcpClient($localEp)
try {
    $async = $client.BeginConnect('10.0.0.169', 5555, $null, $null)
    $ok = $async.AsyncWaitHandle.WaitOne(2000, $false)
    if ($ok -and $client.Connected) {
        $client.EndConnect($async)
        Write-Output "SUCCESS: Connected from 10.0.0.19 to 10.0.0.169:5555"
    } else {
        Write-Output "TIMEOUT: Connection from 10.0.0.19 timed out"
    }
} catch {
    Write-Output "ERROR: $($_.Exception.Message)"
} finally {
    $client.Close()
}
