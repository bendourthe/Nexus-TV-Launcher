param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$AdbArgs
)
$tv = "10.0.0.169:5555"
$devs = adb devices
if ($devs -notmatch [regex]::Escape($tv)) {
    adb connect $tv | Out-Null
}
& adb -s $tv @AdbArgs
