#!/usr/bin/env bash
# Fills repo/ with what belongs to the public repository github.com/rbntermit/mi-fitness-mod and checks it.
# Nothing is committed, pushed or published.
#
#   scripts/make-repo.sh
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPO="$ROOT/repo"
mkdir -p "$REPO"
# start from a clean copy of the sources, keep the hand-written README.md, .gitignore, .gitattributes and .git of repo/
find "$REPO" -mindepth 1 -maxdepth 1 ! -name README.md ! -name .gitignore ! -name .gitattributes ! -name .git -exec rm -rf {} +
cp -r "$ROOT/patches" "$REPO/patches"
cp "$ROOT/build.sh" "$REPO/build.sh"
mkdir -p "$REPO/scripts" "$REPO/release"
for s in make_hooks.pl text_rules.pl ru_from.pl check_param_regs.awk TreeDiff.java phone-verify.sh logcap.sh check-repo.sh \
         make-repo.sh; do
    cp "$ROOT/scripts/$s" "$REPO/scripts/$s"
done
cp "$ROOT/release/status.md" "$REPO/release/status.md"
"$ROOT/scripts/check-repo.sh" "$REPO"
