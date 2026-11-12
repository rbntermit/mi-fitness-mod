#!/usr/bin/env bash
# Captures a logcat session for one connection attempt and keeps only Mi Fitness and Bluetooth lines,
# with secrets masked. The raw capture lives in a temp folder and is deleted after filtering.
#
#   scripts/logcap.sh start <name>   start capturing (nothing on the phone is changed)
#   scripts/logcap.sh stop  <name>   stop, filter, mask -> logs/<name>.log
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ADB="${ADB:-$LOCALAPPDATA/amelia-build/sdk/platform-tools/adb.exe}"
RAW="${TMPDIR:-/tmp}/mifitness-logcap"
OUT="$ROOT/logs"
cmd="${1:-}"; name="${2:-attempt}"
mkdir -p "$RAW" "$OUT"

case "$cmd" in
start)
    "$ADB" get-state > /dev/null || { echo "телефон не подключён по adb"; exit 1; }
    since="$("$ADB" shell "date '+%m-%d %H:%M:%S.000'" | tr -d '\r')"
    : > "$RAW/$name.pids"
    # logcat from now on (the phone's log buffer is not cleared)
    nohup "$ADB" logcat -v threadtime -T "$since" > "$RAW/$name.raw" 2>&1 &
    echo $! > "$RAW/$name.logcat.pid"
    # Mi Fitness runs several processes that may restart; remember every pid seen
    nohup bash -c "while true; do '$ADB' shell ps -A -o PID,NAME 2>/dev/null | tr -d '\r' \
        | awk '\$2 ~ /^com\\.xiaomi\\.wearable/ {print \$1}' >> '$RAW/$name.pids'; sleep 1; done" > /dev/null 2>&1 &
    echo $! > "$RAW/$name.ps.pid"
    echo "запись начата: $name (с $since)"
    ;;
dump)
    # what is still in the phone's log buffer since a time ("MM-DD HH:MM:SS.mmm"), with the pids running now
    since="${3:?время начала, например \"11-03 20:35:00.000\"}"
    "$ADB" logcat -d -v threadtime -T "$since" > "$RAW/$name.raw"
    "$ADB" shell ps -A -o PID,NAME | tr -d '\r' | awk '$2 ~ /^com\.xiaomi\.wearable/ {print $1}' > "$RAW/$name.pids"
    exec "$0" stop "$name"
    ;;
stop)
    for p in logcat ps; do
        [ -f "$RAW/$name.$p.pid" ] && kill "$(cat "$RAW/$name.$p.pid")" 2> /dev/null || true
        rm -f "$RAW/$name.$p.pid"
    done
    pids="$(sort -u "$RAW/$name.pids" | tr '\n' ' ')"
    # keep: lines of Mi Fitness processes; Bluetooth/companion-device stack lines that mention errors,
    # bonding, GATT or connection state
    awk -v pids=" $pids " '
        { pid = $3 }
        index(pids, " " pid " ") > 0 { print; next }
        /com\.xiaomi\.wearable/ { print; next }
        /(Bluetooth|BtGatt|bt_|GattService|AdapterService|BondStateMachine|RemoteDevices|CompanionDevice|CDM|AssociationRequest|ActivityTaskManager: START.*companion)/ &&
        /( E | W |[Ff]ail|[Ee]rror|bond|Bond|gatt|Gatt|onConnectionState|status=|reason|ACL|disconnect|associat)/ { print; next }
        # BLE scanning in the Bluetooth stack: who starts a scan, with which filters, and the raw results
        /(ScanController|ScanManager|LeScanner|ScanClient|AppScanStats|TransitionalScanHelper)/ &&
        /([Ss]tart[A-Za-z]*[Ss]can|[Ss]top[A-Za-z]*[Ss]can|registerScanner|[Ff]ilter|onScanResult|scannerId|[Ss]can[Mm]ode|throttl|denied|[Ff]ail)/ { print }
    ' "$RAW/$name.raw" | perl -pe '
        # the stack already hides the first four bytes; keep the last two (also part of the public BLE name)
        s/\bXX:XX:XX:XX:([0-9A-Fa-f]{2}):([0-9A-Fa-f]{2})\b/MAC~$1$2/g;
        s/\b([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}\b/<MAC>/g;
        s/\b[\w.+-]+@[\w-]+\.[\w.]+\b/<email>/g;
        s/((?:serviceToken|passToken|token|ssecurity|security|cUserId|userId|user_id|uid|did|deviceId|device_id|sn|serialNumber|serial|encryptKey|encrypt_key|authKey|auth_key|key|nonce|sign|signature|password|pwd|session|cookie|Authorization|mac|address)["\x27]?\s*[:=]\s*["\x27]?)(?!MAC~)[^\s,;&"\x27}\])]+/$1<hidden>/gi;
        s/\b(?=[A-Za-z0-9+\/_-]*\d)(?=[A-Za-z0-9+\/_-]*[A-Za-z])[A-Za-z0-9+\/_-]{24,}={0,2}/<secret>/g;
        s/\b[0-9a-fA-F]{16,}\b/<hex>/g;
        s/\b\d{8,}\b/<num>/g;
    ' > "$OUT/$name.log"
    lines=$(wc -l < "$OUT/$name.log")
    rm -f "$RAW/$name.raw" "$RAW/$name.pids"
    echo "сохранено: logs/$name.log ($lines строк, секреты замаскированы, сырой лог удалён)"
    ;;
*)
    echo "использование: $0 start|stop <имя>"; exit 2 ;;
esac
