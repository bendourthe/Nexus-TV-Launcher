$csharp = @"
using System;
using System.Net.Sockets;
using System.Collections.Concurrent;
using System.Threading.Tasks;

public class PortScanner {
    public static string[] ScanSubnet(string subnet, int port, int timeoutMs) {
        var found = new ConcurrentBag<string>();
        Parallel.For(1, 255, new ParallelOptions { MaxDegreeOfParallelism = 64 }, i => {
            string ip = subnet + i;
            using (var client = new TcpClient()) {
                try {
                    var ar = client.BeginConnect(ip, port, null, null);
                    if (ar.AsyncWaitHandle.WaitOne(timeoutMs, false)) {
                        client.EndConnect(ar);
                        found.Add(ip);
                    }
                } catch {}
            }
        });
        return found.ToArray();
    }
}
"@

Add-Type -TypeDefinition $csharp -Language CSharp
Write-Output "Scanning 10.0.0.1 - 10.0.0.254 for port 5555 (timeout 500ms)..."
$results = [PortScanner]::ScanSubnet("10.0.0.", 5555, 500)
if ($results.Length -eq 0) {
    Write-Output "No ADB ports open on 10.0.0.0/24"
} else {
    foreach ($r in $results) {
        Write-Output "FOUND ADB DEVICE: $r"
    }
}
