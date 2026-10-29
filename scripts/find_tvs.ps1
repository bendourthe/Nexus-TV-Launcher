$subnet = "10.0.0."
$port = 5555
$timeout = 120 # ms

Write-Output "Scanning 10.0.0.1 - 10.0.0.254 for open port $port (ADB)..."

$jobs = 1..254 | ForEach-Object {
    $ip = "$subnet$_"
    [powershell]::Create().AddScript({
        param($targetIp, $targetPort, $to)
        try {
            $client = New-Object System.Net.Sockets.TcpClient
            $asyncResult = $client.BeginConnect($targetIp, $targetPort, $null, $null)
            $wait = $asyncResult.AsyncWaitHandle.WaitOne($to, $false)
            if ($wait -and $client.Connected) {
                $client.EndConnect($asyncResult)
                $client.Close()
                return $targetIp
            }
            $client.Close()
        } catch {}
        return $null
    }).AddArgument($ip).AddArgument($port).AddArgument($timeout)
}

$results = $jobs | ForEach-Object { $_.BeginInvoke() }

Start-Sleep -Milliseconds 800

for ($i = 0; $i -lt $jobs.Count; $i++) {
    $res = $jobs[$i].EndInvoke($results[$i])
    if ($res) {
        Write-Output "FOUND ADB DEVICE AT: $res"
    }
}
Write-Output "Scan complete."
