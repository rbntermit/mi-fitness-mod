package com.markus.ota;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The one decision about a stock firmware update offered by the Xiaomi server. Stock update works as in the
 * original app except for:
 *   a) a reflashed band: the region of the bound model (cn, tc) does not match the branch of the firmware the band
 *      itself reports (e.g. q66tc running 4.200.x, the global branch);
 *   b) an offer of the other branch (4.100.x instead of 4.200.x or back), an offer not newer than the band's
 *      version, or a version / branch that cannot be read.
 * Anything that cannot be determined reliably is blocked. Pure logic, no Android.
 *
 * Smart Band 11 (models miwear.watch.q66*): branch 4.200.x is the global firmware (4.200.078-gl), 4.100.x the
 * Chinese one (4.100.179-cn) — see FIRMWARE_RESEARCH.md. q66cn and q66tc belong to the Chinese branch, q66gl,
 * q66gln, q66glt, q66glu to the global one; for other q66 codes (q66nfc, q66u) the branch is not known.
 */
final class StockOtaPolicy {
    static final String BRANCH_GLOBAL = "4.200";
    static final String BRANCH_CHINA = "4.100";

    private static final Pattern BAND11 = Pattern.compile("(?:^|\\.)q66([a-z]*)$");
    private static final Pattern VERSION = Pattern.compile("\\d{1,6}(?:\\.\\d{1,6}){1,4}");

    static final class Decision {
        final boolean allowed;
        final String reason;

        private Decision(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }

        @Override
        public String toString() {
            return (allowed ? "allowed: " : "blocked: ") + reason;
        }
    }

    private StockOtaPolicy() {
    }

    static Decision decide(String boundModel, String deviceVersion, String offeredVersion) {
        String model = boundModel == null ? "" : boundModel.trim().toLowerCase(java.util.Locale.ROOT);
        if (model.isEmpty()) {
            return block("bound model unknown");
        }
        String current = clean(deviceVersion);
        String offer = clean(offeredVersion);
        if (current == null) {
            return block("band firmware version unreadable");
        }
        if (offer == null) {
            return block("offered version unreadable");
        }
        if (compare(offer, current) <= 0) {
            return block("offer " + offer + " is not newer than " + current);
        }
        Matcher m = BAND11.matcher(model);
        if (!m.find()) {
            return allow("not Smart Band 11, newer version " + offer);
        }
        String region = m.group(1);
        String expected = expectedBranch(region);
        if (expected == null) {
            return block("Band 11 q66" + region + ": branch of this model not known");
        }
        String bandBranch = branch(current);
        if (bandBranch == null) {
            return block("Band 11: branch of " + current + " not known");
        }
        if (!bandBranch.equals(expected)) {
            return block("Band 11 q66" + region + " runs " + bandBranch + ".x, its model belongs to " + expected
                    + ".x (reflashed)");
        }
        String offerBranch = branch(offer);
        if (!bandBranch.equals(offerBranch)) {
            return block("offer " + offer + " is another branch than " + bandBranch + ".x");
        }
        return allow("Band 11 q66" + region + " " + current + " -> " + offer + " on " + bandBranch + ".x");
    }

    /** Branch of the bound model's region, or null when not known. */
    static String expectedBranch(String region) {
        switch (region) {
            case "cn":
            case "tc":
                return BRANCH_CHINA;
            case "gl":
            case "gln":
            case "glt":
            case "glu":
                return BRANCH_GLOBAL;
            default:
                return null;
        }
    }

    /** 4.200.078 -> 4.200; only the two known Band 11 branches. */
    static String branch(String version) {
        String v = clean(version);
        if (v == null) {
            return null;
        }
        String[] p = v.split("\\.");
        if (p.length < 3) {
            return null;
        }
        String b = Integer.parseInt(p[0]) + "." + Integer.parseInt(p[1]);
        return BRANCH_GLOBAL.equals(b) || BRANCH_CHINA.equals(b) ? b : null;
    }

    /** Null unless the text is a plain dotted number like 4.200.078 (two to five parts). */
    static String clean(String version) {
        if (version == null) {
            return null;
        }
        String v = version.trim();
        return VERSION.matcher(v).matches() ? v : null;
    }

    /** Numeric, part by part; missing parts count as 0. */
    static int compare(String a, String b) {
        String[] x = a.split("\\.");
        String[] y = b.split("\\.");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            long p = i < x.length ? Long.parseLong(x[i]) : 0;
            long q = i < y.length ? Long.parseLong(y[i]) : 0;
            if (p != q) {
                return Long.compare(p, q);
            }
        }
        return 0;
    }

    private static Decision allow(String why) {
        return new Decision(true, why);
    }

    private static Decision block(String why) {
        return new Decision(false, why);
    }
}
