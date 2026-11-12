package com.markus.update;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Version of a mod build: the Mi Fitness version it is made from plus the mod's own build number.
 * Release tags look like v3.60.0i-band-1. Pure logic, no Android.
 */
final class ModVersion {
    private static final Pattern TAG = Pattern.compile("v(\\d{1,4})\\.(\\d{1,4})\\.(\\d{1,4})([a-z]{0,4})-band-(\\d{1,6})");
    private static final Pattern APP = Pattern.compile("(\\d{1,4})\\.(\\d{1,4})\\.(\\d{1,4})([a-z]{0,4})");

    final int major;
    final int minor;
    final int patch;
    /** Distribution letters after the numbers ("i" = international); builds of another distribution are ignored. */
    final String suffix;
    final int band;

    private ModVersion(int major, int minor, int patch, String suffix, int band) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.suffix = suffix;
        this.band = band;
    }

    /** Null for anything that is not exactly vX.Y.Zs-band-N. */
    static ModVersion parseTag(String tag) {
        if (tag == null) {
            return null;
        }
        Matcher m = TAG.matcher(tag.trim());
        if (!m.matches()) {
            return null;
        }
        return new ModVersion(num(m.group(1)), num(m.group(2)), num(m.group(3)), m.group(4), num(m.group(5)));
    }

    /** The installed build: versionName of the app (3.60.0i) and the mod build number from the build. */
    static ModVersion local(String appVersionName, int band) {
        if (appVersionName == null || band < 0) {
            return null;
        }
        Matcher m = APP.matcher(appVersionName.trim());
        if (!m.matches()) {
            return null;
        }
        return new ModVersion(num(m.group(1)), num(m.group(2)), num(m.group(3)), m.group(4), band);
    }

    /** True when the release is a later build of the same distribution: higher app version, or same and higher band. */
    static boolean isNewer(ModVersion release, ModVersion installed) {
        if (release == null || installed == null || !release.suffix.equals(installed.suffix)) {
            return false;
        }
        int c = compareApp(release, installed);
        return c > 0 || (c == 0 && release.band > installed.band);
    }

    private static int compareApp(ModVersion a, ModVersion b) {
        if (a.major != b.major) {
            return Integer.compare(a.major, b.major);
        }
        if (a.minor != b.minor) {
            return Integer.compare(a.minor, b.minor);
        }
        return Integer.compare(a.patch, b.patch);
    }

    String tag() {
        return "v" + major + "." + minor + "." + patch + suffix + "-band-" + band;
    }

    /** As shown to the person: 3.60.0i, сборка 1. */
    String display() {
        return major + "." + minor + "." + patch + suffix + ", сборка " + band;
    }

    private static int num(String s) {
        return Integer.parseInt(s);
    }
}
