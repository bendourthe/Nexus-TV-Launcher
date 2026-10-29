#!/bin/bash
# Bash restore script for Android TV debloat & launcher
DEVICE="${1:-10.0.0.96:5555}"

echo "Connecting to $DEVICE..."
adb connect "$DEVICE"

DISABLED_FILE="backup/disabled_by_us.txt"
if [ -f "$DISABLED_FILE" ]; then
    while IFS= read -r pkg || [ -n "$pkg" ]; do
        [[ "$pkg" =~ ^#.*$ ]] && continue
        [ -z "$pkg" ] && continue
        echo "Re-enabling package: $pkg"
        adb -s "$DEVICE" shell pm enable "$pkg"
    done < "$DISABLED_FILE"
fi

echo "Restoring stock Google TV launcher..."
adb -s "$DEVICE" shell pm enable com.google.android.apps.tv.launcherx
adb -s "$DEVICE" shell cmd package set-home-activity com.google.android.apps.tv.launcherx/com.google.android.apps.tv.launcherx.home.HomeActivity

echo "Restoring default DNS configuration..."
adb -s "$DEVICE" shell settings put global private_dns_mode opportunistic
adb -s "$DEVICE" shell settings delete global private_dns_specifier

echo "Restore completed successfully."
