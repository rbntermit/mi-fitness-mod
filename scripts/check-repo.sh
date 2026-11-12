#!/usr/bin/env bash
# Checks a folder meant for the public repository: no signing keys or their passwords, no logs, no samples,
# no original APK/XAPK bundles, no firmware, nothing large, no personal identifiers.
# The .git folder (git's own metadata: config, reflogs) is not published content and is skipped.
#
#   scripts/check-repo.sh [repo]       exit 1 and a list of problems if anything is found
set -euo pipefail
DIR="${1:-$(cd "$(dirname "$0")/.." && pwd)/repo}"
[ -d "$DIR" ] || { echo "нет папки $DIR"; exit 2; }
bad=0
report() { echo "  ЗАПРЕЩЕНО: $1"; bad=1; }

# by name
while IFS= read -r f; do report "$f"; done < <(cd "$DIR" && find . -path ./.git -prune -o -type f \( \
    -iname '*.jks' -o -iname '*.keystore' -o -iname '*.p12' -o -iname '*.pk8' -o -iname '*.pem' -o -iname '*.key' \
    -o -iname 'keystore.properties' -o -iname 'local.properties' \
    -o -iname '*.apk' -o -iname '*.apks' -o -iname '*.xapk' -o -iname '*.apkm' -o -iname '*.idsig' \
    -o -iname '*.bin' -o -iname '*.fw' -o -iname '*.ota' -o -iname 'miwear_product.json' \
    -o -iname '*.log' -o -iname '*.hprof' \) -print | sort)
# by folder
while IFS= read -r d; do report "$d/"; done < <(cd "$DIR" && find . -path ./.git -prune -o -type d \( -name keys -o -name logs \
    -o -name samples -o -name target -o -name 'target-*' -o -name out -o -name work -o -name archive \
    -o -name reference -o -name donor -o -name tools \) -print | sort)
# by size
while IFS= read -r f; do report "$f (больше 1 МБ)"; done < <(cd "$DIR" && find . -path ./.git -prune -o -type f -size +1M -print | sort)
# by content: key material, passwords, personal identifiers of the owner's phone and band
if grep -rIlE --exclude-dir=.git -e '-----BEGIN [A-Z ]*PRIVATE KEY-----' -e '^\s*(store|key)Password\s*=\s*\S' "$DIR" > /tmp/check-repo.$$ 2>/dev/null; then
    while IFS= read -r f; do report "$f (ключ или пароль)"; done < /tmp/check-repo.$$
fi
if grep -rIlE --exclude-dir=.git -e '\b([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}\b' -e '[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+\.[A-Za-z.]{2,}' \
        -e '[A-Za-z]:[/\\]+Users[/\\]' -e '/[a-z]/Users/' "$DIR" > /tmp/check-repo.$$ 2>/dev/null; then
    while IFS= read -r f; do report "$f (MAC-адрес, почта или путь к папке пользователя)"; done < /tmp/check-repo.$$
fi
rm -f /tmp/check-repo.$$
# binary files that are not small images
while IFS= read -r f; do
    case "$f" in *.png|*.webp|*.jpg|*.ico) ;; *) report "$f (двоичный файл)" ;; esac
done < <(cd "$DIR" && grep -rIL --exclude-dir=.git . . 2>/dev/null | sort)

n=$(cd "$DIR" && find . -path ./.git -prune -o -type f -print | wc -l)
if [ $bad -eq 0 ]; then
    echo "repo: $n файлов, запрещённого нет"
else
    echo "repo: найдено запрещённое (см. выше)"
    exit 1
fi
