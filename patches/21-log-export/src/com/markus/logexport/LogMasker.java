package com.markus.logexport;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Masks what must not leave the phone in a text log: tokens and session secrets (serviceToken, passToken, ssecurity,
 * cookies, Bearer), account and device identifiers (userId, did, serial numbers, IMEI), MAC addresses, e-mail addresses
 * and phone numbers. Plain Java, no Android types, so every rule is tested. Masking is best effort: names and message
 * texts are not recognised, which is why the export screen warns before sharing.
 */
public final class LogMasker {
    static final String HIDDEN = "<hidden>";
    static final String MAC = "<MAC>";
    static final String EMAIL = "<email>";
    static final String PHONE = "<phone>";

    private static final Pattern COOKIE = Pattern.compile("(?i)(\\b(?:set-)?cookie\\b\\s*[:=]\\s*)[^\\r\\n]+");
    private static final Pattern BEARER = Pattern.compile("(?i)(\\b(?:bearer|basic)\\s+)[A-Za-z0-9._~+/=-]{8,}");
    /** key=value, key: value, "key":"value" — the key decides, the value is replaced whatever it is. */
    private static final Pattern KEY_VALUE = Pattern.compile("(?i)(\\b(?:"
            + "\\w*token\\w*|\\w*security\\w*|\\w*secret\\w*|\\w*password\\w*|passwd|pwd|\\w*ticket\\w*"
            + "|sign|signature|nonce|authorization|session(?:id)?"
            + "|c?userid|user_id|uid|xiaomiid|mi_id|miid"
            + "|did|deviceid|device_id|\\w+_did"
            + "|sn|serial\\w*|imei\\w*|meid|iccid|imsi"
            + "|mac|macaddress|mac_address|blemac|ble_mac|bleaddress|ble_address|btaddress|bt_address"
            + "|e-?mail|phone\\w*|mobile\\w*|incomingnumber|phonenumber"
            + ")\\b[\"']?\\s*[:=]\\s*[\"']?)(?!(?:bearer|basic)\\s)([^\\s\"',;&}\\])]+)");
    private static final Pattern MAC_ADDR = Pattern.compile("\\b(?:[0-9A-Fa-fX]{2}[:-]){5}[0-9A-Fa-fX]{2}\\b");
    private static final Pattern EMAIL_ADDR = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    /** +<country code> and 9-14 more digits, spaces, dashes and brackets allowed inside. */
    private static final Pattern PHONE_INTL = Pattern.compile("(?<![\\w+])\\+\\d[\\d\\s()-]{8,16}\\d(?![\\w])");
    /** Russian numbers without "+": 7XXXXXXXXXX or 8XXXXXXXXXX, optionally with separators. */
    private static final Pattern PHONE_RU = Pattern.compile(
            "(?<![\\w.:+-])[78][\\s(-]{0,2}\\d{3}[\\s)-]{0,2}\\d{3}[\\s-]?\\d{2}[\\s-]?\\d{2}(?![\\w.:-])");
    /** Base64- or hex-like strings of 32+ characters with both letters and digits: tokens, keys, ids. */
    private static final Pattern LONG_SECRET = Pattern.compile(
            "(?<![\\w/+=-])(?=[A-Za-z0-9+/_=-]*\\d)(?=[A-Za-z0-9+/_=-]*[A-Za-z])[A-Za-z0-9+/_=-]{32,}");
    /** Hex of 16+ characters with letters and digits (device keys, hashes of ids). */
    private static final Pattern LONG_HEX = Pattern.compile(
            "\\b(?=[0-9a-fA-F]*[a-fA-F])(?=[0-9a-fA-F]*\\d)[0-9a-fA-F]{16,}\\b");
    /** Stand-alone 9-12 digit numbers: account ids and dids. Millisecond times (13 digits) and versions stay. */
    private static final Pattern LONG_NUMBER = Pattern.compile(
            "(?<![0-9A-Za-z.-])(?<!(?i:time|stamp)\\s{0,2}[=:]\\s{0,2})\\d{9,12}(?![0-9A-Za-z.:-])");

    /** Log tags and code names like "XiaomiFit_DeviceManager_16208_16208": a word, then _-separated parts. */
    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z][A-Za-z-]*(?:_[A-Za-z0-9-]+)+$");
    private static final Pattern WORD = Pattern.compile("^[A-Za-z_.-]{3,}$");

    private LogMasker() {
    }

    /** LONG_SECRET, but tags and file paths stay readable: they are what a log is read for. */
    private static String replaceLongSecrets(String s) {
        Matcher m = LONG_SECRET.matcher(s);
        if (!m.find()) {
            return s;
        }
        StringBuffer out = new StringBuffer(s.length());
        do {
            m.appendReplacement(out, Matcher.quoteReplacement(looksSecret(m.group()) ? HIDDEN : m.group()));
        } while (m.find());
        m.appendTail(out);
        return out.toString();
    }

    static boolean looksSecret(String t) {
        if (IDENTIFIER.matcher(t).matches()) {
            return false;
        }
        String[] parts = t.split("/");
        if (parts.length >= 3) {
            int words = 0;
            for (String p : parts) {
                if (WORD.matcher(p).matches()) {
                    words++;
                }
            }
            // a path of ordinary folder names; random base64 has no such runs
            if (words * 2 >= parts.length) {
                return false;
            }
        }
        return true;
    }

    public static String mask(String line) {
        if (line == null || line.isEmpty()) {
            return line;
        }
        String s = line;
        s = COOKIE.matcher(s).replaceAll("$1" + Matcher.quoteReplacement(HIDDEN));
        s = BEARER.matcher(s).replaceAll("$1" + Matcher.quoteReplacement(HIDDEN));
        s = EMAIL_ADDR.matcher(s).replaceAll(Matcher.quoteReplacement(EMAIL));
        s = MAC_ADDR.matcher(s).replaceAll(Matcher.quoteReplacement(MAC));
        s = KEY_VALUE.matcher(s).replaceAll("$1" + Matcher.quoteReplacement(HIDDEN));
        s = PHONE_INTL.matcher(s).replaceAll(Matcher.quoteReplacement(PHONE));
        s = PHONE_RU.matcher(s).replaceAll(Matcher.quoteReplacement(PHONE));
        s = replaceLongSecrets(s);
        s = LONG_HEX.matcher(s).replaceAll(Matcher.quoteReplacement(HIDDEN));
        s = LONG_NUMBER.matcher(s).replaceAll(Matcher.quoteReplacement(HIDDEN));
        return s;
    }
}
