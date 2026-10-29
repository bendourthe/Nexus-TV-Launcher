#!/system/bin/sh
# Sleep-safe UID-2000 Home & Wake helper for Fire OS (bypasses 5s stopAppSwitches lock in ~50ms)

PIDFILE="/data/local/tmp/clean_home_helper.pid"
if [ -f "$PIDFILE" ]; then
    OLD_PID=$(cat "$PIDFILE" 2>/dev/null)
    if [ -n "$OLD_PID" ] && [ "$OLD_PID" != "$$" ]; then
        kill -9 "$OLD_PID" 2>/dev/null
    fi
fi
echo "$$" > "$PIDFILE"
killall -9 logcat 2>/dev/null

LAUNCH_CMD="/system/bin/cmd activity start --activity-no-animation -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -f 0x10204000 -n com.cleantv.launcher/.MainActivity"
LAST_CS=0
LAST_WAKE_CS=0

is_awake() {
    dumpsys power 2>/dev/null | grep -q "mWakefulness=Awake"
}

while [ "$(cat "$PIDFILE" 2>/dev/null)" = "$$" ]; do
    logcat -T 1 -v brief *:I | while read -r line; do
        [ "$(cat "$PIDFILE" 2>/dev/null)" = "$$" ] || exit 0
        case "$line" in
            *"USER_SCREEN_OFF"*)
                # Absolutely never launch activities when screen is off
                LAST_CS=0
                LAST_WAKE_CS=0
                ;;
            *"USER_SCREEN_ON"*|*"SCREEN_ON intent received"*)
                read up _ < /proc/uptime
                now_cs="${up%.*}${up#*.}"
                diff_wake=$((now_cs - LAST_WAKE_CS))
                if [ "$diff_wake" -ge 80 ] || [ "$diff_wake" -lt 0 ]; then
                    LAST_WAKE_CS="$now_cs"
                    (
                        if is_awake; then
                            $LAUNCH_CMD >/dev/null 2>&1
                            usleep 150000 2>/dev/null || sleep 0.2
                            if is_awake; then
                                $LAUNCH_CMD >/dev/null 2>&1
                            fi
                        fi
                    ) &
                fi
                ;;
            *"keyCode=KEYCODE_HOME"*|*"HOME_PRESSED"*|*"Intercept Key event HOME_KEY"*|*"Starting Home Launcher"*|*"REQUEST_SHELL_HOME_LAUNCH"*)
                read up _ < /proc/uptime
                now_cs="${up%.*}${up#*.}"
                diff_cs=$((now_cs - LAST_CS))
                if [ "$diff_cs" -ge 45 ] || [ "$diff_cs" -lt 0 ]; then
                    LAST_CS="$now_cs"
                    (
                        if is_awake; then
                            $LAUNCH_CMD >/dev/null 2>&1
                            usleep 80000 2>/dev/null || sleep 0.1
                            if is_awake; then
                                $LAUNCH_CMD >/dev/null 2>&1
                            fi
                        fi
                    ) &
                fi
                ;;
        esac
    done
    [ "$(cat "$PIDFILE" 2>/dev/null)" = "$$" ] || exit 0
    sleep 1
done
