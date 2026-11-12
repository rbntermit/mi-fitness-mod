#!/usr/bin/env bash
# On-phone check of a fresh build, before any test with the band:
#   scripts/phone-verify.sh install   adb install -r, then full bytecode verification (cmd package compile -m verify -f)
#   scripts/phone-verify.sh scan      look for verifier/linkage errors and crashes since the install
# Nothing is cleared or uninstalled; the app data and the band pairing stay as they are.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ADB="${ADB:-$LOCALAPPDATA/amelia-build/sdk/platform-tools/adb.exe}"
PKG=com.xiaomi.wearable
APK="${APK:-$(ls -t "$ROOT"/out/MiFitness.*.apk | head -1)}"
STAMP="${TMPDIR:-/tmp}/mifitness-phone-verify.since"

case "${1:-}" in
install)
    "$ADB" get-state > /dev/null
    "$ADB" shell "date '+%m-%d %H:%M:%S.000'" | tr -d '\r' > "$STAMP"
    echo "== install -r $(basename "$APK") ($(sha256sum "$APK" | cut -c1-16)…)"
    "$ADB" install -r "$(cygpath -w "$APK" 2>/dev/null || echo "$APK")"
    echo "== cmd package compile -m verify -f $PKG"
    "$ADB" shell cmd package compile -m verify -f "$PKG"
    ;;
scan)
    since="$(cat "$STAMP")"
    # verifier and linkage problems of the app (dex2oat, ART at runtime) plus crashes of its processes
    "$ADB" logcat -d -v threadtime -T "$since" | tr -d '\r' \
        | grep -E "VerifyError|failed to verify|[Rr]ejecting class|Verification error|Verification failed|hard failure|ClassNotFoundException|NoClassDefFoundError|NoSuchMethodError|NoSuchFieldError|FATAL EXCEPTION|Process: $PKG|has died.*$PKG|ANR in $PKG" \
        | grep -E "com\.markus|MarkusFwBridge|$PKG|FATAL|AndroidRuntime|dex2oat" \
        | perl -pe 's/\b([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}\b/<MAC>/g; s/\b\d{8,}\b/<num>/g' || true
    ;;
*)
    echo "использование: $0 install|scan"; exit 2 ;;
esac
