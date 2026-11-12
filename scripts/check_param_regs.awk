# Flags an added method call that passes a parameter register (p0, p1, ...) whose kind no longer matches:
# the method above the call may have reused the register for an object while the call expects a primitive
# (or the other way round). The dex verifier then rejects the whole class at runtime (VerifyError), which
# nothing else in the build catches. The scan is linear from the method start and only knows "object" vs
# "primitive", so it is a guard for this class of mistake, not a full verifier.
#
#   awk -v needle="<added invoke line>" -f check_param_regs.awk Class.smali
# Prints one line per problem; prints nothing when the call is safe.

function trim(s) { gsub(/^[ \t]+|[ \t]+$/, "", s); return s }

# kind of value an instruction writes into its first register: "obj", "prim" or "" (not a write)
function writes(op) {
    if (op ~ /^(invoke|if-|return|aput|iput|sput|throw|monitor-|fill-array-data|packed-switch|sparse-switch|filled-new-array|goto|nop)/) return ""
    if (op ~ /^(move-result-object|move-object|move-exception|new-instance|new-array|const-string|const-class|const-method|iget-object|sget-object|aget-object|check-cast)/) return "obj"
    return "prim"
}

BEGIN {
    line = trim(needle)
    if (line !~ /^invoke-/ || line ~ /\.\./) exit
    isStatic = line ~ /^invoke-static/
    regs = line; sub(/^[^{]*\{/, "", regs); sub(/\}.*/, "", regs)
    nr = split(regs, r, /, */)
    desc = line; sub(/^[^(]*\(/, "", desc); sub(/\).*/, "", desc)
    # kinds expected for each register position
    k = 0
    if (!isStatic) kind[++k] = "obj"
    while (desc != "") {
        c = substr(desc, 1, 1)
        if (c == "L") { kind[++k] = "obj"; sub(/^L[^;]*;/, "", desc) }
        else if (c == "[") { kind[++k] = "obj"; sub(/^\[+(L[^;]*;|.)/, "", desc) }
        else { kind[++k] = "prim"; if (c == "J" || c == "D") kind[++k] = "prim"; desc = substr(desc, 2) }
    }
    for (i = 1; i <= nr; i++) if (r[i] ~ /^p[0-9]+$/) want[r[i]] = kind[i]
}

/^\.method / { delete seen }

{
    s = trim($0)
    if (s == line) {
        for (reg in want) if ((reg in seen) && seen[reg] != want[reg])
            print NR ": " reg " must be " want[reg] " but line " seenAt[reg] " stored " seen[reg]
        next
    }
    if (s ~ /^[.:#]/ || s == "") next
    op = s; sub(/ .*/, "", op)
    w = writes(op)
    if (w == "") next
    dst = s; sub(/^[^ ]+ +\{?/, "", dst); sub(/[,} ].*/, "", dst)
    if (dst ~ /^p[0-9]+$/) { seen[dst] = w; seenAt[dst] = NR }
}
