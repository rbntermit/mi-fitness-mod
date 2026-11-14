#!/usr/bin/env bash
# Builds the personal band-mod of Mi Fitness from a stock split bundle.
#
#   ./build.sh [path/to/target.apks]
#   ./build.sh --tests-only            compile the Java parts of patches/ and run their tests, no APK
#   FIRMWARE_INSTALL=0 ./build.sh      build without patches/12-firmware-install
#
# Steps: compile and test patches/*/src (javac -> d8 -> smali) -> merge splits (APKEditor) ->
# decompile (apktool) -> apply patches/ in order -> build -> zipalign -> sign with the persistent
# key in keys/ -> verify.
# Tools are looked up in tools/ and the Android SDK; override with the variables below.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
TOOLS="${TOOLS:-$ROOT/tools}"
JAVA_HOME="${JAVA_HOME:-$LOCALAPPDATA/amelia-build/jdk21}"
ANDROID_SDK="${ANDROID_SDK:-$LOCALAPPDATA/amelia-build/sdk}"
BUILD_TOOLS="${BUILD_TOOLS:-$(ls -d "$ANDROID_SDK"/build-tools/* | sort -V | tail -1)}"
KEYSTORE="${KEYSTORE:-$ROOT/keys/mifitness-mod.jks}"
KEYPROPS="$ROOT/keys/keystore.properties"
WORK="${WORK:-$ROOT/work/build}"
OUT_DIR="${OUT_DIR:-$ROOT/out}"

JAVA="$JAVA_HOME/bin/java"
KEYTOOL="$JAVA_HOME/bin/keytool"
[ -x "$JAVA" ] || JAVA="$JAVA_HOME/bin/java.exe"
[ -x "$KEYTOOL" ] || KEYTOOL="$JAVA_HOME/bin/keytool.exe"
ZIPALIGN="$BUILD_TOOLS/zipalign"; [ -x "$ZIPALIGN" ] || ZIPALIGN="$BUILD_TOOLS/zipalign.exe"
APKSIGNER=("$JAVA" -jar "$BUILD_TOOLS/lib/apksigner.jar")
APKTOOL=("$JAVA" -Xmx6g -jar "$TOOLS/apktool.jar")
APKEDITOR=("$JAVA" -Xmx4g -jar "$TOOLS/APKEditor.jar")

log() { printf '\n== %s\n' "$*"; }
die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

JAVAC="$JAVA_HOME/bin/javac"; [ -x "$JAVAC" ] || JAVAC="$JAVA_HOME/bin/javac.exe"
JAR="$JAVA_HOME/bin/jar"; [ -x "$JAR" ] || JAR="$JAVA_HOME/bin/jar.exe"
ANDROID_JAR="${ANDROID_JAR:-$(ls -d "$ANDROID_SDK"/platforms/android-* | sort -V | tail -1)/android.jar}"
D8_JAR="$BUILD_TOOLS/lib/d8.jar"
JUNIT="$TOOLS/junit-console.jar"
SAMPLES="${SAMPLES:-$ROOT/samples/firmware}"
FIRMWARE_INSTALL="${FIRMWARE_INSTALL:-1}"
# Build number of the mod for this app version: release tag v<versionName>-band-<BAND_BUILD>
BAND_BUILD="${BAND_BUILD:-1}"
# Temporary Russian for a bundle without the ru split: RU_FROM=<version in archive/> takes values-ru of that
# version (only names, formats and tags that still fit; scripts/ru_from.pl). Leave empty for a bundle with Russian.
RU_FROM="${RU_FROM:-}"
case "$BAND_BUILD" in ""|*[!0-9]*) echo "ERROR: BAND_BUILD must be a number" >&2; exit 1 ;; esac

for t in "$JAVA" "$JAVAC" "$JAR" "$KEYTOOL" "$ZIPALIGN" "$BUILD_TOOLS/lib/apksigner.jar" "$TOOLS/apktool.jar" \
         "$TOOLS/APKEditor.jar" "$ANDROID_JAR" "$D8_JAR" "$JUNIT"; do
    [ -e "$t" ] || die "tool not found: $t"
done

# Java tools on Windows want native paths and ';' between classpath entries.
if command -v cygpath > /dev/null; then
    winpath() { cygpath -w "$1"; }
    CPSEP=';'
else
    winpath() { echo "$1"; }
    CPSEP=':'
fi
cpath() { local out="" p; for p in "$@"; do out="${out:+$out$CPSEP}$(winpath "$p")"; done; echo "$out"; }

# Patch folders in build order; FIRMWARE_INSTALL=0 leaves out the firmware screen completely.
PATCHES=()
for p in "$ROOT"/patches/*/; do
    p="${p%/}"
    [ "$FIRMWARE_INSTALL" = "0" ] && [ "$(basename "$p")" = "12-firmware-install" ] && continue
    # a parked or renamed patch file would be skipped silently; refuse to build instead
    for f in "$p"/smali.patch?*; do
        [ -e "$f" ] && die "$(basename "$p"): unused patch file $(basename "$f"); rename it to smali.patch or remove it"
    done
    PATCHES+=("$p")
done

# Compiles patches/*/src against android.jar and compile-only stubs of app classes (patches/*/stubs),
# runs patches/*/test with JUnit, and turns the classes into smali in $WORK/java/<patch>/smali.
build_java() {
    local J="$WORK/java" p name cp_prev="" srcs tests
    rm -rf "$J"; mkdir -p "$J/stubs"
    srcs="$(for p in "${PATCHES[@]}"; do [ -d "$p/stubs" ] && find "$p/stubs" -name '*.java'; done || true)"
    if [ -n "$srcs" ]; then
        # shellcheck disable=SC2086
        "$JAVAC" -nowarn -encoding UTF-8 --release 8 -cp "$(cpath "$ANDROID_JAR")" -d "$(winpath "$J/stubs")" \
            $(for f in $srcs; do winpath "$f"; done) || die "stubs do not compile"
    fi
    (cd "$J/stubs" && "$JAR" cf ../stubs.jar .)
    for p in "${PATCHES[@]}"; do
        [ -d "$p/src" ] || continue
        name="$(basename "$p")"
        mkdir -p "$J/$name/classes" "$J/$name/smali"
        # shellcheck disable=SC2046
        "$JAVAC" -Xlint:-options -encoding UTF-8 --release 8 \
            -cp "$(cpath "$ANDROID_JAR" "$J/stubs.jar" ${cp_prev})" -d "$(winpath "$J/$name/classes")" \
            $(find "$p/src" -name '*.java' | while read -r f; do winpath "$f"; done) || die "$name: Java code does not compile"
        (cd "$J/$name/classes" && "$JAR" cf "../classes.jar" .)
        # shellcheck disable=SC2046
        "$JAVA" -cp "$(cpath "$D8_JAR")" com.android.tools.r8.D8 --release --min-api 26 \
            --lib "$(winpath "$ANDROID_JAR")" --classpath "$(winpath "$J/stubs.jar")" \
            $(for c in $cp_prev; do echo --classpath "$(winpath "$c")"; done) \
            --output "$(winpath "$J/$name/classes.dex.zip")" "$(winpath "$J/$name/classes.jar")" || die "$name: d8 failed"
        # d8 writes classes.dex into a zip; apktool -r disassembles such a dex-only "apk" into smali
        "${APKTOOL[@]}" d -r -f -o "$J/$name/apk" "$J/$name/classes.dex.zip" > "$J/$name/baksmali.log" 2>&1 \
            || { cat "$J/$name/baksmali.log"; die "$name: baksmali failed"; }
        rm -rf "$J/$name/smali"; mv "$J/$name/apk/smali" "$J/$name/smali"
        echo "   $name: $(find "$J/$name/smali" -name '*.smali' | wc -l) classes"
        if [ -d "$p/test" ]; then
            mkdir -p "$J/$name/test-classes"
            # shellcheck disable=SC2046
            "$JAVAC" -nowarn -encoding UTF-8 -cp "$(cpath "$JUNIT" "$J/$name/classes.jar" "$J/stubs.jar" "$ANDROID_JAR" ${cp_prev})" \
                -d "$(winpath "$J/$name/test-classes")" \
                $(find "$p/test" -name '*.java' | while read -r f; do winpath "$f"; done) || die "$name: tests do not compile"
            "$JAVA" "-Dsamples.dir=$(winpath "$SAMPLES")" -jar "$(winpath "$JUNIT")" execute --disable-banner \
                --details=summary --details-theme=ascii \
                -cp "$(cpath "$J/$name/test-classes" "$J/$name/classes.jar" "$J/stubs.jar" ${cp_prev})" \
                --scan-class-path "$(winpath "$J/$name/test-classes")" > "$J/$name/tests.log" 2>&1 \
                || { cat "$J/$name/tests.log"; die "$name: tests failed"; }
            grep -E "tests (successful|failed|skipped|aborted)" "$J/$name/tests.log" | sed 's/^[^0-9]*/     /'
        fi
        cp_prev="$cp_prev $J/$name/classes.jar"
    done
}

if [ "${1:-}" = "--tests-only" ]; then
    WORK="$ROOT/work/tests"
    log "Java: compile and test (${#PATCHES[@]} patches, FIRMWARE_INSTALL=$FIRMWARE_INSTALL)"
    build_java
    exit 0
fi

TARGET="${1:-}"
if [ -z "$TARGET" ]; then
    for f in "$ROOT"/target/*.apks "$ROOT"/target/*.apkm "$ROOT"/target/*.xapk; do
        [ -f "$f" ] && { TARGET="$f"; break; }
    done
fi
[ -f "$TARGET" ] || die "no target bundle (put the stock .apks into target/)"

# Finds a smali class in any smali*/ directory of the decompiled tree.
find_class() {
    local d
    for d in "$SRC"/smali*; do
        [ -f "$d/$1" ] && { echo "$d/$1"; return 0; }
    done
    return 1
}

# Applies a multi-file unified diff whose paths are class-relative (a/com/x/Y.smali).
apply_patch() {
    local patch="$1" chunks n f rel file
    chunks="$(mktemp -d)"
    awk -v dir="$chunks" '/^--- a\// { n++; f = sprintf("%s/%03d", dir, n) } { print > f }' "$patch"
    for f in "$chunks"/*; do
        rel="$(sed -n '1s#^--- a/##p' "$f")"
        case "$rel" in
            *.smali) file="$(find_class "$rel")" || die "$(basename "$(dirname "$patch")"): class not found: $rel" ;;
            *) file="$SRC/$rel" ;;
        esac
        sed -i 's/\r$//' "$file"
        # keep the class as it was before the first patch: the final check counts lines against it
        if [ ! -f "$WORK/orig/$rel" ]; then
            mkdir -p "$(dirname "$WORK/orig/$rel")"
            cp "$file" "$WORK/orig/$rel"
        fi
        patch --batch --forward --no-backup-if-mismatch -r - "$file" < "$f" > /dev/null \
            || die "$(basename "$(dirname "$patch")"): does not apply to $rel"
        echo "   $rel"
    done
    rm -rf "$chunks"
}

# smali.patch of every patch, then the patch files generated from hooks.txt during this build
patch_files() {
    local p
    for p in "${PATCHES[@]}"; do
        [ -f "$p/smali.patch" ] && echo "$p/smali.patch"
        [ -f "$WORK/hooks/$(basename "$p")/smali.patch" ] && echo "$WORK/hooks/$(basename "$p")/smali.patch"
    done
    return 0
}

# Inserts <string> lines before </resources>, skipping keys that already exist.
add_strings() {
    local xml="$1" frag="$2" line name
    # a bundle without that language split has no such file (3.60.0i from Uptodown ships only config.en)
    if [ ! -f "$xml" ]; then
        mkdir -p "$(dirname "$xml")"
        printf '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n</resources>\n' > "$xml"
        echo "   $(basename "$(dirname "$xml")")/strings.xml was missing: created for the mod strings only"
    fi
    sed -i 's/\r$//' "$xml"
    while IFS= read -r line; do
        name="$(sed -n 's/.*name="\([^"]*\)".*/\1/p' <<< "$line")"
        grep -q "name=\"$name\"" "$xml" && continue
        line="${line//\\/\\\\}"; line="${line//&/\\&}"; line="${line//#/\\#}"
        sed -i "s#^</resources>#    $line\n</resources>#" "$xml"
    done < "$frag"
}

log "Target: $TARGET"
rm -rf "$WORK"; mkdir -p "$WORK" "$OUT_DIR"

log "Java: compile and test (FIRMWARE_INSTALL=$FIRMWARE_INSTALL)"
build_java

log "Merging splits"
"${APKEDITOR[@]}" m -i "$TARGET" -o "$WORK/merged.apk" > "$WORK/merge.log" 2>&1 || { cat "$WORK/merge.log"; die "merge failed"; }

log "Decompiling"
SRC="$WORK/src"
"${APKTOOL[@]}" d -f -o "$SRC" "$WORK/merged.apk" > "$WORK/decompile.log" 2>&1 || { cat "$WORK/decompile.log"; die "apktool d failed"; }
VERSION_NAME="$(sed -n 's/^  versionName: //p' "$SRC/apktool.yml" | tr -d "'\r")"
VERSION_CODE="$(sed -n 's/^  versionCode: //p' "$SRC/apktool.yml" | tr -d "'\r")"
echo "   versionName=$VERSION_NAME versionCode=$VERSION_CODE"

if [ -n "$RU_FROM" ]; then
    log "Russian strings from $RU_FROM (RU_FROM)"
    RUC="$ROOT/work/ru-cache/$RU_FROM"
    if [ ! -f "$RUC/res/values-ru/strings.xml" ]; then
        rub="$(ls "$ROOT/archive/$RU_FROM/target/"*.apk[ms] 2>/dev/null | head -1 || true)"
        [ -n "$rub" ] || die "RU_FROM=$RU_FROM: no bundle in archive/$RU_FROM/target/"
        mkdir -p "$(dirname "$RUC")"
        "${APKEDITOR[@]}" m -i "$rub" -o "$RUC.apk" > "$WORK/ru-merge.log" 2>&1 || die "RU_FROM: merge failed"
        "${APKTOOL[@]}" d -s -f -o "$RUC" "$RUC.apk" > "$WORK/ru-decode.log" 2>&1 || die "RU_FROM: decode failed"
        rm -f "$RUC.apk"
    fi
    perl "$ROOT/scripts/ru_from.pl" "$RUC/res" "$SRC/res" "$WORK/ru-from.txt" || die "RU_FROM: Russian strings not taken"
    cp "$WORK/ru-from.txt" "$OUT_DIR/ru-from-$RU_FROM.txt"
fi

log "Adding mod classes"
last=$(ls -d "$SRC"/smali_classes* | sed 's/.*smali_classes//' | sort -n | tail -1)
NEWDEX="$SRC/smali_classes$((last + 1))"
mkdir -p "$NEWDEX"
for p in "${PATCHES[@]}"; do
    [ -d "$p/smali" ] && cp -r "$p/smali/." "$NEWDEX/"
    [ -d "$WORK/java/$(basename "$p")/smali" ] && cp -r "$WORK/java/$(basename "$p")/smali/." "$NEWDEX/"
done
echo "   $(find "$NEWDEX" -name '*.smali' | wc -l) classes -> $(basename "$NEWDEX")"

# Is the member (" name(args)ret" or " name:type") declared by the class or by one of its parents and interfaces?
# App classes are searched in the decompiled tree (declaration lines only), framework classes (java/, android/)
# in android.jar with javap. Constructors are not inherited: they must be declared by the class itself.
JAVAP="$JAVA_HOME/bin/javap"; [ -x "$JAVAP" ] || JAVAP="$JAVA_HOME/bin/javap.exe"
fw_members() {   # file with the members a framework class declares, then "super <parents>"; prints its path
    local cls="$1" out="$WORK/java/fw/${1//\//.}"
    mkdir -p "$WORK/java/fw"
    if [ ! -f "$out" ]; then
        "$JAVAP" -cp "$(winpath "$ANDROID_JAR")" -s -p "${cls//\//.}" 2>/dev/null | perl -ne '
            s/\r$//;
            # the class line without generics: "class A<K, V> extends B<K, V>" -> "class A extends B"
            if (/^\S.*\b(?:class|interface)\s/) { 1 while s/<[^<>]*>//g }
            if (/^\S.*\bclass\s+(\S+)(?:\s+extends\s+([^{]+?))?(?:\s+implements\s+([^{]+?))?\s*\{/ ||
                /^\S.*\binterface\s+(\S+)(?:\s+extends\s+([^{]+?))?\s*\{/) {
                my @s = map { s/<.*//r =~ s/\./\//gr } grep { length } split /\s*,\s*/, join(",", grep { defined } $2, $3);
                print "super @s\n"; next }
            # the descriptor line first: it ends with ";" too and would pass for a field
            if (/^\s+descriptor: (\S+)/) { my $d = $1;
                if (defined $name) { print " $name$d\n" } elsif (defined $field) { print " $field:$d\n" }
                $name = $field = undef; next }
            if (/^\s+\S.*?([\w\$<>]+)\((?:[^)]*)\)/) { $name = $1; $name = "<init>" if /^\s+(?:public |protected |private )?[\w.\$]+\(/; next }
            if (/^\s+\S.*?([\w\$]+);$/) { $field = $1; $name = undef; next }' > "$out"
        # javap leaves out "extends java.lang.Object"
        grep -q "^super ." "$out" || [ "$cls" = java/lang/Object ] || echo "super java/lang/Object" >> "$out"
    fi
    echo "$out"
}
has_member() {
    local cls="$1" member="$2" f s
    case "$cls" in
        java/*|javax/*|android/*|dalvik/*)
            f="$(fw_members "$cls")"
            # a file, not a pipe: grep -q would end the pipe early and pipefail would report a failure
            grep -qxF -- "$member" "$f" && return 0
            case "$member" in " <init>("*) return 1 ;; esac
            for s in $(sed -n 's/^super //p' "$f"); do has_member "$s" "$member" && return 0; done
            return 1 ;;
    esac
    f="$(find_class "$cls.smali")" || return 1
    awk -v m="$member" '/^\.(method|field) / { if (index($0, m)) { found = 1; exit } } END { exit !found }' "$f" && return 0
    case "$member" in " <init>("*) return 1 ;; esac
    for s in $(sed -n 's/^\.\(super\|implements\) L\(.*\);$/\2/p' "$f"); do
        has_member "$s" "$member" && return 0
    done
    return 1
}

# Every class, method and field the compiled Java code uses must exist in this app version:
# obfuscated names (rp7, der, p9j...) change between releases, and a miss would crash at runtime.
check_java_refs() {
    local c ref cls member f sup ok missing=0
    # the compiled Java code and the hand-written smali classes of patches (00-core)
    { for c in $(cd "$WORK/java" && find . -path '*/smali/*' -name '*.smali'); do cat "$WORK/java/$c"; done
      for p in "${PATCHES[@]}"; do [ -d "$p/smali" ] && find "$p/smali" -name '*.smali' -exec cat {} +; done; true; } \
        | grep -oE 'L[A-Za-z0-9_/$]+;(->[A-Za-z0-9_$<>]+(\([^)]*\)[^ ,"]*|:[^ ,"]+))?' \
        | grep -vE '^L(java|javax|android|dalvik|org/json|org/xmlpull|org/w3c|org/xml)/' | sort -u > "$WORK/java/refs.txt"
    while IFS= read -r ref; do
        cls="${ref%%;*}"; cls="${cls#L}"
        f="$(find_class "$cls.smali")" || { echo "   MISSING class $cls"; missing=1; continue; }
        case "$ref" in *'->'*) member=" ${ref#*->}";; *) continue;; esac
        has_member "$cls" "$member" || { echo "   MISSING member $ref"; missing=1; }
    done < "$WORK/java/refs.txt"
    [ $missing -eq 0 ] || die "compiled Java code uses classes or members this app version does not have"
    echo "   $(wc -l < "$WORK/java/refs.txt") app references of the Java code resolve in this version"
}
check_java_refs

log "Applying patches"
for p in "${PATCHES[@]}"; do
    echo " $(basename "$p")"
    [ -f "$p/smali.patch" ] && apply_patch "$p/smali.patch"
    # hooks.txt: method hooks generated against the tree as it is now (after the earlier patches), so they do
    # not depend on label numbers or obfuscated names around them; applied and checked like a smali.patch
    if [ -f "$p/hooks.txt" ]; then
        hp="$WORK/hooks/$(basename "$p")/smali.patch"; mkdir -p "$(dirname "$hp")"
        perl "$ROOT/scripts/make_hooks.pl" "$SRC" "$p/hooks.txt" > "$hp" 2> "$hp.log" \
            || { cat "$hp.log"; die "$(basename "$p"): hooks.txt does not fit this app version"; }
        apply_patch "$hp"
    fi
    [ -f "$p/strings-en.xml" ] && add_strings "$SRC/res/values/strings.xml" "$p/strings-en.xml"
    [ -f "$p/strings-ru.xml" ] && add_strings "$SRC/res/values-ru/strings.xml" "$p/strings-ru.xml"
    if [ -f "$p/manifest-activity.xml" ]; then
        sed -i 's/\r$//' "$SRC/AndroidManifest.xml"
        while IFS= read -r line; do
            [ -z "$line" ] && continue
            name="$(sed -n 's/.*android:name="\([^"]*\)".*/\1/p' <<< "$line")"
            grep -q "android:name=\"$name\"" "$SRC/AndroidManifest.xml" && continue
            sed -i "s#</application>#        $line\n    </application>#" "$SRC/AndroidManifest.xml"
        done < "$p/manifest-activity.xml"
    fi
done

# rules.txt: exact text edits of resources and of the built-in React Native bundles
# (assets/builtin_bundles_skytree/<module>.zip = zip -> <module>.android.tar -> index.android.bundle, res/...).
BUNDLES="$WORK/bundles"
rm -rf "$BUNDLES"; mkdir -p "$BUNDLES"
: > "$WORK/bundles.touched"
rule_files="$(for p in "${PATCHES[@]}"; do [ -f "$p/rules.txt" ] && echo "$p/rules.txt"; done || true)"
if [ -n "$rule_files" ]; then
    log "Resource and bundle rules"
    BA="$SRC/assets/builtin_bundles_skytree"
    # shellcheck disable=SC2086
    for m in $(sed -n 's#^FILE bundle:\([a-z]*\)/.*#\1#p' $rule_files | sort -u); do
        [ -f "$BA/$m.zip" ] || die "rules: no built-in bundle $m in this app version"
        mkdir -p "$BUNDLES/$m"
        unzip -p "$BA/$m.zip" "$m.android.tar" > "$BUNDLES/$m.orig.tar" || die "rules: cannot read $m.zip"
        tar -tf "$BUNDLES/$m.orig.tar" > "$BUNDLES/$m.list"
        tar -xf "$BUNDLES/$m.orig.tar" -C "$BUNDLES/$m"
    done
    for f in $rule_files; do
        echo " $(basename "$(dirname "$f")")"
        perl "$ROOT/scripts/text_rules.pl" "$SRC" "$BUNDLES" "$f" >> "$WORK/bundles.touched" \
            || die "$(basename "$(dirname "$f")"): rules.txt does not fit this app version"
    done
    builtin=""
    NODE="${NODE:-$ROOT/tools/node/node.exe}"
    [ -x "$NODE" ] || NODE="$(command -v node || true)"
    for m in $(sed -n 's/^bundle://p' "$WORK/bundles.touched" | sort -u); do
        # a syntax error in a changed bundle would break the whole screen: parse it before packing
        [ -n "$NODE" ] || die "rules changed bundle $m: node is needed to check it (tools/node or NODE=...)"
        cp "$BUNDLES/$m/index.android.bundle" "$WORK/check-$m.js"
        "$NODE" --check "$WORK/check-$m.js" || die "bundle $m: JavaScript syntax error after rules.txt"
        for j in $(cd "$BUNDLES/$m" && find . -name '*.json'); do
            "$NODE" -e 'JSON.parse(require("fs").readFileSync(process.argv[1], "utf8"))' "$BUNDLES/$m/$j" \
                || die "bundle $m: $j is not valid JSON after rules.txt"
        done
        rm -f "$WORK/check-$m.js"
        echo "   bundle $m: JavaScript and JSON parse ($("$NODE" --version))"
        # same entries in the same order, owned by root as in the original
        tar --format=ustar --owner=0 --group=0 --numeric-owner --no-recursion -cf "$BUNDLES/$m.android.tar" \
            -C "$BUNDLES/$m" -T "$BUNDLES/$m.list"
        rm -f "$BUNDLES/$m.zip"
        (cd "$BUNDLES" && "$JAR" cfM "$m.zip" "$m.android.tar") || die "rules: cannot pack $m.zip"
        cp "$BUNDLES/$m.zip" "$BA/$m.zip"
        md5="$(md5sum "$BA/$m.zip" | cut -c1-32)"
        # the app checks urlmd5 against the zip; the high version keeps a server copy from replacing it
        # (a real JSON edit: entries carry JSON inside strings, e.g. noticeStrategy)
        perl -MJSON::PP -e 'my ($f, $m, $md5) = @ARGV; local $/; open my $h, "<:raw", $f or die; my $j = <$h>; close $h;
            my $c = JSON::PP->new->utf8->canonical; my $a = $c->decode($j); my $n = 0;
            for my $e (@$a) { next unless ($e->{moduleName} // "") eq $m;
                $e->{urlmd5} = $md5; $e->{version} = "99.99.99"; $e->{buildNum} = "99999999"; $n++ }
            die "module $m: $n entries\n" unless $n == 1;
            open $h, ">:raw", $f or die; print $h $c->encode($a); close $h' "$BA/bundleInfo.json" "$m" "$md5" \
            || die "rules: bundleInfo.json not updated for $m"
        grep -q "\"urlmd5\":\"$md5\"" "$BA/bundleInfo.json" || die "rules: bundleInfo.json not updated for $m"
        builtin="${builtin:+$builtin,}$m"
        echo "   bundle $m repacked (md5 $md5), loaded from the APK only"
    done
    printf '<string name="markus_builtin_bundles" translatable="false">%s</string>\n' "$builtin" > "$WORK/builtin.xml"
    add_strings "$SRC/res/values/strings.xml" "$WORK/builtin.xml"
fi
# the mod's build number for the update check (patch 18)
printf '<string name="markus_mod_build" translatable="false">%s</string>\n' "$BAND_BUILD" > "$WORK/mod-build.xml"
add_strings "$SRC/res/values/strings.xml" "$WORK/mod-build.xml"

# An added instruction that reads p0/p1/... after the method has reused that register makes the dex
# verifier reject the whole class at runtime (VerifyError). Catch it here instead of on the phone.
bad=0
nchecked=0
for pf in $(patch_files); do
    p="$(dirname "$pf")"
    rel=""
    while IFS= read -r line; do
        case "$line" in
            "+++ b/"*) rel="${line#+++ b/}"; continue ;;
            "+"*) code="${line#+}" ;;
            *) continue ;;
        esac
        grep -qE '^\s*invoke\S* .*\bp[0-9]+\b' <<< "$code" || continue
        file="$(find_class "$rel")" || continue
        nchecked=$((nchecked + 1))
        out="$(awk -v needle="$code" -f "$ROOT/scripts/check_param_regs.awk" "$file")"
        [ -z "$out" ] || { echo "   $(basename "$p"): $rel: '$(sed 's/^ *//' <<< "$code")': $out"; bad=1; }
    done < "$pf"
done
[ $bad -eq 0 ] || die "a patch passes a parameter register that the method reused for another kind of value (VerifyError)"
echo "   $nchecked added calls pass parameter registers of the expected kind"

mkdir -p "$SRC/assets"
printf 'v%s-band-%s\n' "$VERSION_NAME" "$BAND_BUILD" > "$SRC/assets/markus-mod-build.txt"

log "Building"
# Patched classes add method references; a dex already near the 65536 limit then overflows.
# Like alpha-4 does, move the largest package of the overflowing dex into a dex of its own and retry.
for attempt in 1 2 3 4 5; do
    "${APKTOOL[@]}" b -o "$WORK/unsigned.apk" "$SRC" > "$WORK/build.log" 2>&1 && break
    full="$(sed -n 's/.*Could not smali folder: \(smali[a-z_0-9]*\).*/\1/p' "$WORK/build.log" | head -1)"
    if [ -z "$full" ] || ! grep -qE 'Unsigned short value out of range: [0-9]+' "$WORK/build.log" || [ $attempt -eq 5 ]; then
        tail -40 "$WORK/build.log"; die "apktool b failed"
    fi
    pkg="$(cd "$SRC/$full" && find . -mindepth 1 -maxdepth 4 -type d | while read -r d; do
        echo "$(find "$d" -name '*.smali' | wc -l) ${d#./}"; done | awk 'split($2, p, "/") == 4' | sort -rn | head -1 | cut -d' ' -f2)"
    [ -n "$pkg" ] || die "$full overflows and has no package to move"
    last=$(ls -d "$SRC"/smali_classes* | sed 's/.*smali_classes//' | sort -n | tail -1)
    spill="$SRC/smali_classes$((last + 1))"
    mkdir -p "$spill/$(dirname "$pkg")"
    mv "$SRC/$full/$pkg" "$spill/$pkg"
    # The failed run aborted the other dex jobs mid-write and left those files newer than their smali, so
    # apktool would reuse them as they are (a dex of zeros in the APK). Rebuild everything.
    rm -rf "$SRC/build"
    echo "   $full is over 65536 references: moved $pkg to $(basename "$spill"), retrying"
done
# Every dex in the APK must start with a dex header (catches a truncated or zeroed dex from any cause).
for d in $(unzip -Z1 "$WORK/unsigned.apk" | grep -E '^classes[0-9]*\.dex$'); do
    [ "$(unzip -p "$WORK/unsigned.apk" "$d" | head -c 4 | od -An -c | tr -d ' ')" = 'dex\n' ] \
        || die "$d in the built APK is not a dex file"
done

log "zipalign (16 KB pages for uncompressed .so)"
"$ZIPALIGN" -f -P 16 4 "$WORK/unsigned.apk" "$WORK/aligned.apk"

log "Signing"
if [ ! -f "$KEYSTORE" ]; then
    echo "   no key yet: creating $KEYSTORE (keep it, every update must be signed with it)"
    mkdir -p "$(dirname "$KEYSTORE")"
    pass="$(head -c 32 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 24)"
    "$KEYTOOL" -genkeypair -keystore "$KEYSTORE" -storetype PKCS12 -alias mifitness-mod \
        -keyalg RSA -keysize 4096 -validity 36500 -storepass "$pass" -keypass "$pass" \
        -dname "CN=MiFitness personal mod" > /dev/null
    printf 'storePassword=%s\nkeyAlias=mifitness-mod\n' "$pass" > "$KEYPROPS"
fi
[ -f "$KEYPROPS" ] || die "missing $KEYPROPS with storePassword"
STOREPASS="$(sed -n 's/^storePassword=//p' "$KEYPROPS" | tr -d '\r')"
ALIAS="$(sed -n 's/^keyAlias=//p' "$KEYPROPS" | tr -d '\r')"
TAG="v$VERSION_NAME-band-$BAND_BUILD"
OUT="$OUT_DIR/MiFitness.$VERSION_NAME-band-$BAND_BUILD.apk"
"${APKSIGNER[@]}" sign --ks "$KEYSTORE" --ks-key-alias "$ALIAS" --ks-pass "pass:$STOREPASS" \
    --out "$OUT" "$WORK/aligned.apk"
rm -f "$OUT.idsig"

log "Verifying"
"$ZIPALIGN" -c -P 16 4 "$OUT" > /dev/null || die "zipalign check failed"
"${APKSIGNER[@]}" verify --verbose --print-certs "$OUT" | grep -E '^Verified using|certificate SHA-256 digest' | sort -u

# Every line added by patches/ must be present in the smali of the built APK.
"${APKTOOL[@]}" d -f -r -o "$WORK/verify" "$OUT" > /dev/null 2>&1 || die "cannot decompile result"
SRC="$WORK/verify"
fail=0
for pf in $(patch_files); do
    rel=""
    while IFS= read -r line; do
        case "$line" in
            "+++ b/"*) rel="${line#+++ b/}"; file="$(find_class "$rel")" || { echo "MISSING class $rel"; fail=1; } ;;
            "+"*) code="$(sed 's/^+[[:space:]]*//' <<< "$line")"
                  # comments and label names do not survive assembling (labels come back as :cond_N)
                  case "$code" in ''|'#'*|':'*) continue ;; esac
                  case "$code" in if-*:*|goto*:*) code="${code%%:*}:" ;; esac
                  grep -qF -- "$code" "$file" || { echo "MISSING in $rel: $code"; fail=1; } ;;
        esac
    done < "$pf"
done
nclasses=0
for p in "${PATCHES[@]}"; do
    for d in "$p/smali" "$WORK/java/$(basename "$p")/smali"; do
        [ -d "$d" ] || continue
        for c in $(cd "$d" && find . -name '*.smali' | sed 's#^\./##'); do
            nclasses=$((nclasses + 1))
            find_class "$c" > /dev/null || { echo "MISSING class $c"; fail=1; }
        done
    done
done
[ $fail -eq 0 ] || die "some patched lines are missing from the result"
echo "   all patched lines present; all $nclasses mod classes present"

# Removed calls and strings must be gone: for every invoke/const-string line a patch adds or
# removes, its count in the result must equal (count before patching) + added - removed.
norm() { sed 's/\r$//; s/^[[:space:]]*//; s#const-string/jumbo#const-string#' "$1"; }
awk '/^\+\+\+ b\// { rel = substr($0, 7); next }
     /^--- a\// { next }
     /^[-+]/ && /(->|const-string)/ { s = substr($0, 1, 1); c = substr($0, 2); sub(/^[ \t]+/, "", c)
                                      sub("const-string/jumbo", "const-string", c)
                                      d[rel "\t" c] += (s == "+" ? 1 : -1) }
     END { for (k in d) print k "\t" d[k] }' \
    $(patch_files) > "$WORK/expected.tsv"
while IFS=$'\t' read -r rel code delta; do
    before="$WORK/orig/$rel"
    file="$(find_class "$rel")" || { echo "MISSING class $rel"; fail=1; continue; }
    n0=$(norm "$before" | grep -cxF -- "$code" || true)
    n1=$(norm "$file" | grep -cxF -- "$code" || true)
    [ "$n1" -eq $((n0 + delta)) ] || { echo "COUNT in $rel: '$code' expected $((n0 + delta)), found $n1"; fail=1; }
done < "$WORK/expected.tsv"
[ $fail -eq 0 ] || die "added/removed lines do not match the patches"
echo "   $(wc -l < "$WORK/expected.tsv") added/removed calls and strings match the patches"

# Bundles changed by rules.txt: the APK carries exactly the repacked zip and bundleInfo.json names its md5.
for m in $(sed -n 's/^bundle://p' "$WORK/bundles.touched" | sort -u); do
    md5="$(unzip -p "$OUT" "assets/builtin_bundles_skytree/$m.zip" | md5sum | cut -c1-32)"
    [ "$md5" = "$(md5sum "$BUNDLES/$m.zip" | cut -c1-32)" ] || die "bundle $m in the APK is not the repacked one"
    unzip -p "$OUT" assets/builtin_bundles_skytree/bundleInfo.json > "$WORK/bundleInfo.out.json"
    grep -q "\"urlmd5\":\"$md5\"" "$WORK/bundleInfo.out.json" || die "bundleInfo.json in the APK does not name $m.zip"
    echo "   bundle $m in the APK: md5 $md5 matches bundleInfo.json"
done

log "Release notes"
# Russian notes for the GitHub release: what differs from the original app, what changed since the previous
# build (fingerprints of the patch folders in $OUT_DIR/.last-build.tsv), and what is checked (release/status.md).
NOTES="$OUT_DIR/release-notes.md"
CUR="$WORK/build-manifest.tsv"
{
    printf 'app\t%s\t%s\n' "$VERSION_NAME" "$VERSION_CODE"
    for p in "${PATCHES[@]}"; do
        printf 'patch\t%s\t%s\n' "$(basename "$p")" \
            "$(cd "$p" && find . -type f | LC_ALL=C sort | xargs sha1sum | sha1sum | cut -c1-12)"
    done
} > "$CUR"
desc() { sed -n "s/^| $1 | \(.*\) |\$/\1/p" "$ROOT/patches/README.md" | head -1; }
{
    echo "# Mi Fitness $VERSION_NAME — мод, сборка $BAND_BUILD ($TAG)"
    echo
    echo "Неофициальная модификация Mi Fitness (com.xiaomi.wearable). Установка поверх возможна только на сборку"
    echo "с той же подписью; с официального приложения нужно удалить его (данные на телефоне пропадут)."
    echo
    echo "## Важно"
    echo
    sections="$(for m in $(sed -n 's/^bundle://p' "$WORK/bundles.touched" 2>/dev/null | sort -u); do
        case "$m" in mine) echo "«Профиль»";; sport) echo "«Тренировка»";; health) echo "«Здоровье» (с «Сном»)";; *) echo "«$m»";; esac
    done | paste -sd, - | sed 's/,/, /g')"
    if [ -n "$sections" ]; then
        echo "- Разделы $sections берутся из APK и **не обновляются с сервера Xiaomi**: иначе скрытые"
        echo "  элементы вернутся. Новые функции этих разделов появятся только с новой сборкой мода."
    fi
    [ -d "$ROOT/patches/19-phone-steps-off" ] && \
        echo "- Переключатель «Не учитывать шаги телефона» работает **только на телефонах Xiaomi** (MIUI/HyperOS): на других телефонах Mi Fitness шаги телефона и так не берёт."
    if [ -n "$RU_FROM" ]; then
        echo "- В пакете $VERSION_NAME нет русского языка; перевод взят из $RU_FROM: $(sed -n '1s/^Перенесено: //p' "$WORK/ru-from.txt"). Новые для $VERSION_NAME строки — на английском ($(sed -n 's/^Осталось на английском[^:]*: //p' "$WORK/ru-from.txt"))."
    fi
    echo
    echo "## Изменения относительно оригинального приложения"
    echo
    for p in "${PATCHES[@]}"; do
        n="$(basename "$p")"; d="$(desc "$n")"
        echo "- **$n** — ${d:-описание в patches/README.md}"
    done
    echo
    echo "## Изменения относительно прошлой сборки"
    echo
    LAST="$OUT_DIR/.last-build.tsv"
    if [ ! -f "$LAST" ]; then
        echo "- Первая сборка: сравнивать не с чем."
    else
        lastapp="$(awk -F'\t' '$1=="app"{print $2}' "$LAST")"
        [ "$lastapp" = "$VERSION_NAME" ] || echo "- Приложение обновлено: $lastapp → $VERSION_NAME."
        awk -F'\t' 'FNR==NR && $1=="patch" {old[$2]=$3; next}
                    $1=="patch" { if (!($2 in old)) print "- Новый патч: **" $2 "**."
                                  else if (old[$2] != $3) print "- Изменён патч: **" $2 "**."
                                  seen[$2]=1 }
                    END { for (k in old) if (!(k in seen)) print "- Убран патч: **" k "**." }' "$LAST" "$CUR"
        cmp -s <(grep '^patch' "$LAST") <(grep '^patch' "$CUR") && echo "- Патчи не менялись."
    fi
    echo
    echo "## Проверено и не проверено"
    echo
    if [ -f "$ROOT/release/status.md" ]; then cat "$ROOT/release/status.md"; else echo "- Нет данных (release/status.md)."; fi
    echo
    echo "## Файл"
    echo
    echo "- \`$(basename "$OUT")\`, SHA-256 \`$(sha256sum "$OUT" | cut -c1-64)\`"
    echo "- Сертификат подписи SHA-256 \`$("${APKSIGNER[@]}" verify --print-certs "$OUT" | sed -n 's/.*certificate SHA-256 digest: //p' | head -1)\`"
} > "$NOTES"
cp "$CUR" "$OUT_DIR/.last-build.tsv"
echo "   $NOTES"

log "Done: $OUT"
sha256sum "$OUT"
