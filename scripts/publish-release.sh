#!/usr/bin/env bash
# Publishes a GitHub release of the mod: the tag must already be on GitHub and point at main, the APK must carry our
# signature, repo/ must pass check-repo.sh. Without --yes it only checks and prints what would be published, so the
# owner can confirm first. Uses the logged-in GitHub CLI (gh); no tokens in commands or files.
#
#   scripts/publish-release.sh <band build N> [apk]          checks, prints the summary, publishes nothing
#   scripts/publish-release.sh <band build N> [apk] --yes    the same checks, then publishes and verifies
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPO_SLUG="rbntermit/mi-fitness-mod"
CERT_SHA256="1da8b9cb6fe100fe88bdf5b090cff5b031e5d65b6a4169621f22a088455779df"
JAVA_HOME="${JAVA_HOME:-$LOCALAPPDATA/amelia-build/jdk21}"
ANDROID_SDK="${ANDROID_SDK:-$LOCALAPPDATA/amelia-build/sdk}"
BUILD_TOOLS="${BUILD_TOOLS:-$(ls -d "$ANDROID_SDK"/build-tools/* | sort -V | tail -1)}"
JAVA="$JAVA_HOME/bin/java"; [ -x "$JAVA" ] || JAVA="$JAVA_HOME/bin/java.exe"
AAPT2="$BUILD_TOOLS/aapt2"; [ -x "$AAPT2" ] || AAPT2="$BUILD_TOOLS/aapt2.exe"
NOTES="$ROOT/out/release-notes.md"

die() { echo "ОШИБКА: $*" >&2; exit 1; }
ok() { echo "  ✓ $*"; }

N="${1:-}"; shift || true
case "$N" in ""|*[!0-9]*) die "укажите номер сборки мода: scripts/publish-release.sh 3 [apk] [--yes]" ;; esac
APK=""; YES=0
for a in "$@"; do
    case "$a" in --yes) YES=1 ;; *) APK="$a" ;; esac
done
command -v gh > /dev/null || die "нет GitHub CLI (gh)"
gh auth status > /dev/null 2>&1 || die "gh не авторизован: выполните gh auth login"

# ---- APK: version, name, SHA-256, signature
if [ -z "$APK" ]; then
    APK="$(ls "$ROOT"/out/MiFitness.*-band-"$N".apk 2>/dev/null | head -1)"
fi
[ -f "$APK" ] || die "нет APK сборки band-$N в out/"
VERSION="$("$AAPT2" dump badging "$APK" | sed -n "s/.*versionName='\([^']*\)'.*/\1/p" | head -1)"
[ -n "$VERSION" ] || die "не прочитать versionName из $APK"
TAG="v$VERSION-band-$N"
TITLE="Mi Fitness $VERSION band-$N"
APK_NAME="$(basename "$APK")"
[ "$APK_NAME" = "MiFitness.$VERSION-band-$N.apk" ] || die "имя APK $APK_NAME не совпадает с версией и номером сборки"
SHA="$(sha256sum "$APK" | cut -c1-64)"
CERT="$("$JAVA" -jar "$BUILD_TOOLS/lib/apksigner.jar" verify --print-certs "$APK" \
    | sed -n 's/.*certificate SHA-256 digest: //p' | head -1 | tr -d '\r')"
[ "$CERT" = "$CERT_SHA256" ] || die "подпись APK не наша: $CERT"
ok "APK $APK_NAME, подпись нашим сертификатом ${CERT_SHA256:0:8}…"

# ---- release notes describe exactly this file
[ -f "$NOTES" ] || die "нет out/release-notes.md"
grep -q "($TAG)" "$NOTES" || die "release-notes.md не для $TAG"
grep -q "$SHA" "$NOTES" || die "SHA-256 в release-notes.md не совпадает с APK"
ok "release-notes.md для $TAG, SHA-256 совпадает"

# ---- tag on GitHub = main on GitHub = local repo/
gh_sha() { gh api "repos/$REPO_SLUG/$1" --jq "$2"; }
REF_TYPE="$(gh_sha "git/ref/tags/$TAG" .object.type 2>/dev/null)" || die "тега $TAG нет на GitHub"
REF_SHA="$(gh_sha "git/ref/tags/$TAG" .object.sha)"
if [ "$REF_TYPE" = "tag" ]; then
    TAG_COMMIT="$(gh_sha "git/tags/$REF_SHA" .object.sha)"
else
    TAG_COMMIT="$REF_SHA"
fi
MAIN_COMMIT="$(gh_sha "commits/main" .sha)"
[ "$TAG_COMMIT" = "$MAIN_COMMIT" ] || die "тег $TAG указывает на ${TAG_COMMIT:0:7}, а main на GitHub — ${MAIN_COMMIT:0:7}"
LOCAL_COMMIT="$(git -C "$ROOT/repo" rev-parse HEAD)"
[ "$LOCAL_COMMIT" = "$MAIN_COMMIT" ] || die "repo/ (${LOCAL_COMMIT:0:7}) не совпадает с main на GitHub (${MAIN_COMMIT:0:7})"
ok "тег $TAG на GitHub указывает на текущий main (${MAIN_COMMIT:0:7})"

# ---- nothing forbidden in what is published
bash "$ROOT/scripts/check-repo.sh" "$ROOT/repo" > /dev/null || die "check-repo.sh нашёл запрещённое в repo/"
ok "check-repo.sh: запрещённого нет"

if gh release view "$TAG" --repo "$REPO_SLUG" > /dev/null 2>&1; then
    die "релиз $TAG уже есть на GitHub"
fi

echo
echo "Будет опубликовано:"
echo "  тег:      $TAG"
echo "  название: $TITLE"
echo "  файл:     $APK_NAME"
echo "  SHA-256:  $SHA"
echo "  отметка:  latest, не pre-release, не черновик"
echo "  изменения относительно прошлой сборки:"
sed -n '/^## Изменения относительно прошлой сборки/,/^## /p' "$NOTES" | grep '^- ' | cut -c1-200 | sed 's/^/    /'

if [ "$YES" -ne 1 ]; then
    echo
    echo "Ничего не опубликовано. Для публикации запустите ещё раз с --yes после подтверждения."
    exit 0
fi

# ---- publish
gh release create "$TAG" "$APK" --repo "$REPO_SLUG" --verify-tag --title "$TITLE" \
    --notes-file "$NOTES" --latest > /dev/null
ok "релиз создан"

# ---- verify through the public API, as the mod's update check sees it
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
curl -fsSL -A "mi-fitness-mod-publish" "https://api.github.com/repos/$REPO_SLUG/releases/latest" > "$TMP/latest.json" \
    || die "api.github.com не ответил"
NODE="${NODE:-$ROOT/tools/node/node.exe}"; [ -x "$NODE" ] || NODE="$(command -v node || true)"
[ -n "$NODE" ] || die "нужен node (tools/node), чтобы прочитать ответ api.github.com"
# prints: tag, draft, prerelease, download url of the APK
read -r LATEST_TAG DRAFT PRE URL < <("$NODE" -e '
    const j = JSON.parse(require("fs").readFileSync(process.argv[1], "utf8"));
    const a = (j.assets || []).find(x => x.name === process.argv[2]);
    console.log([j.tag_name, j.draft, j.prerelease, a ? a.browser_download_url : "-"].join(" "));
' "$(cygpath -w "$TMP/latest.json" 2>/dev/null || echo "$TMP/latest.json")" "$APK_NAME")
[ "$LATEST_TAG" = "$TAG" ] || die "latest на GitHub — $LATEST_TAG, а не $TAG"
[ "$DRAFT" = "false" ] || die "релиз — черновик"
[ "$PRE" = "false" ] || die "релиз отмечен как pre-release"
[ "$URL" != "-" ] || URL=""
[ -n "$URL" ] || die "в релизе нет файла $APK_NAME"
curl -fsSL -A "mi-fitness-mod-publish" -o "$TMP/$APK_NAME" "$URL" || die "файл не скачивается: $URL"
GOT="$(sha256sum "$TMP/$APK_NAME" | cut -c1-64)"
[ "$GOT" = "$SHA" ] || die "скачанный файл отличается: $GOT"
ok "latest на GitHub — $TAG; файл скачивается, SHA-256 совпадает"
echo "https://github.com/$REPO_SLUG/releases/tag/$TAG"
