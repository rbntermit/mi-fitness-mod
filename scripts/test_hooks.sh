#!/usr/bin/env bash
# Regression: nested-class '$' and paths with spaces must survive diff, and diff errors must fail the generator.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d "${TMPDIR:-/tmp}/mifitness-hook-test.XXXXXX")"
trap 'rm -rf "$T"' EXIT
mkdir -p "$T/input tree/smali/test" "$T/bin"
cat > "$T/input tree/smali/test/Outer\$Inner.smali" <<'EOF'
.class public Ltest/Outer$Inner;
.super Ljava/lang/Object;
.method public go()V
    .locals 0
    return-void
.end method
EOF
cat > "$T/hooks.txt" <<'EOF'
CLASS test/Outer$Inner
METHOD go()V
TEMPS 1
CODE
    const/4 v0, 0x1
EOF
perl "$ROOT/scripts/make_hooks.pl" "$T/input tree" "$T/hooks.txt" > "$T/result.patch" 2> "$T/stderr.txt"
grep -Fq -- '--- a/test/Outer$Inner.smali' "$T/result.patch"
grep -Fq -- '+    const/4 v0, 0x1' "$T/result.patch"
grep -Fq -- '+    .locals 1' "$T/result.patch"
patch --batch "$T/input tree/smali/test/Outer\$Inner.smali" < "$T/result.patch" > /dev/null
grep -Fq -- '    const/4 v0, 0x1' "$T/input tree/smali/test/Outer\$Inner.smali"
cat > "$T/bin/diff" <<'EOF'
#!/usr/bin/env bash
exit 2
EOF
chmod +x "$T/bin/diff"
if PATH="$T/bin:$PATH" perl "$ROOT/scripts/make_hooks.pl" "$T/input tree" "$T/hooks.txt" > "$T/failed.patch" 2> "$T/error.txt"; then
    echo 'ERROR: make_hooks ignored diff failure' >&2
    exit 1
fi
grep -Fq 'diff failed' "$T/error.txt"
echo 'Hooks: nested class, spaces, patch application and diff error handling passed.'
