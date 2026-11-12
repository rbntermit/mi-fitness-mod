package com.markus.fw;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Firmware file name as distributed: {@code <md5>_upd_miwear.watch.q66gl.zip}.
 * Browser suffixes such as " (1)", "(2)" or "-1" before ".zip" are accepted; the MD5 prefix is optional
 * (a renamed file then has no MD5 to compare). Model and region can only come from the name.
 */
public final class FwName {
    private static final Pattern NAME = Pattern.compile(
            "^(?:([0-9a-f]{32})_)?upd_(miwear\\.watch\\.[a-z]+[0-9]+(?:gl|cn|tc))(\\s?\\(\\d+\\)|-\\d+)?\\.zip$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern MODEL = Pattern.compile("^miwear\\.watch\\.([a-z]+[0-9]+)(gl|cn|tc)$");

    /** The name exactly as the file picker reported it. */
    public final String original;
    /** Lower-case MD5 from the prefix, or null when the name has none. */
    public final String md5;
    /** Full model, e.g. "miwear.watch.q66gl", or null when the name does not show it. */
    public final String model;
    /** Model family without region, e.g. "q66". */
    public final String family;
    /** Region suffix: "gl", "cn" or "tc". */
    public final String region;
    /** Browser suffix that was ignored, e.g. " (1)"; empty when none. */
    public final String suffix;

    private FwName(String original, String md5, String model, String family, String region, String suffix) {
        this.original = original;
        this.md5 = md5;
        this.model = model;
        this.family = family;
        this.region = region;
        this.suffix = suffix;
    }

    public static FwName parse(String name) {
        String n = name == null ? "" : name.trim();
        Matcher m = NAME.matcher(n);
        if (!m.matches()) {
            return new FwName(n, null, null, null, null, "");
        }
        String model = m.group(2).toLowerCase(Locale.ROOT);
        String[] fr = splitModel(model);
        String md5 = m.group(1) == null ? null : m.group(1).toLowerCase(Locale.ROOT);
        return new FwName(n, md5, model, fr[0], fr[1], m.group(3) == null ? "" : m.group(3));
    }

    /** Model and region are known from the name. */
    public boolean recognized() {
        return model != null;
    }

    private static final Pattern FAMILY = Pattern.compile("^miwear\\.watch\\.([a-z]+[0-9]+)[a-z]*$");

    /** "q66" for any "miwear.watch.q66…" id, including variants like q66gln or q66nfc; null otherwise. */
    public static String familyOf(String model) {
        Matcher m = model == null ? null : FAMILY.matcher(model.trim().toLowerCase(Locale.ROOT));
        return m != null && m.matches() ? m.group(1) : null;
    }

    /** {family, region} of a model id like "miwear.watch.q66cn"; both null when it does not look like one. */
    public static String[] splitModel(String model) {
        if (model == null) {
            return new String[] {null, null};
        }
        Matcher m = MODEL.matcher(model.trim().toLowerCase(Locale.ROOT));
        return m.matches() ? new String[] {m.group(1), m.group(2)} : new String[] {null, null};
    }
}
