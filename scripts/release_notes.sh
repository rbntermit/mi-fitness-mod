#!/usr/bin/env bash
# Renders only curated public release text. Private reports are never input.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
VERSION="${1:?version}"; N="${2:?build}"; APK="${3:?apk}"; OUT="${4:?output}"; CERT="${5:?certificate}"
case "$N" in *[!0-9]*|"") echo 'invalid build number' >&2; exit 1 ;; esac
TEXT="$ROOT/release/notes-band-$N.md"
[ -f "$TEXT" ] || { echo "public description missing: $TEXT" >&2; exit 1; }
[ -f "$APK" ] || { echo 'APK missing' >&2; exit 1; }
[[ "$CERT" =~ ^[0-9a-fA-F]{64}$ ]] || { echo 'invalid certificate digest' >&2; exit 1; }
{
    printf '# Mi Fitness Mod %s — сборка %s (v%s-band-%s)\n\n' "$VERSION" "$N" "$VERSION" "$N"
    cat "$TEXT"
    printf '\n## Файл\n\n'
    printf -- '- `%s`, SHA-256 `%s`\n' "$(basename "$APK")" "$(sha256sum "$APK" | cut -c1-64)"
    printf -- '- Сертификат подписи SHA-256 `%s`\n' "$CERT"
} > "$OUT"
