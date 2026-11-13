package com.markus.logexport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns `logcat -v threadtime` output into the exported text: only lines of the app's own processes, every line
 * masked, size capped. Plain Java so it is tested without a phone.
 */
public final class LogLines {
    /** "11-13 00:12:25.855 17816 18135 I Tag: text" */
    private static final Pattern THREADTIME = Pattern.compile(
            "^\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d{3}\\s+(\\d+)\\s+\\d+\\s+[VDIWEFA]\\s");
    /** The export stays small enough for a messenger. */
    public static final long MAX_CHARS = 8L * 1024 * 1024;

    public static final class Result {
        public int kept;
        public int dropped;
        public boolean truncated;
    }

    private LogLines() {
    }

    /** Process id of a threadtime line, or -1 when the line is not one. */
    static int pidOf(String line) {
        Matcher m = THREADTIME.matcher(line);
        if (!m.find()) {
            return -1;
        }
        try {
            return Integer.parseInt(m.group(1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static final Pattern DID_PREFIX = Pattern.compile("(^|/)\\d{5,}_");

    /**
     * Name of a band log file inside the exported ZIP. The app keeps the log in "&lt;did&gt;_&lt;model&gt;/…"; the did is
     * an account-bound device id and is dropped, the rest goes through LogMasker.
     */
    public static String zipEntryName(String relativePath) {
        String n = DID_PREFIX.matcher(relativePath.replace('\\', '/')).replaceAll("$1");
        return LogMasker.mask(n);
    }

    /** Copies the lines of the given processes, masked, until {@link #MAX_CHARS}. Everything else is dropped. */
    public static Result filter(BufferedReader in, Writer out, Set<Integer> ownPids, long maxChars) throws IOException {
        Result r = new Result();
        long written = 0;
        String line;
        while ((line = in.readLine()) != null) {
            int pid = pidOf(line);
            if (pid < 0 || !ownPids.contains(pid)) {
                r.dropped++;
                continue;
            }
            String masked = LogMasker.mask(line);
            if (written + masked.length() + 1 > maxChars) {
                r.truncated = true;
                break;
            }
            out.write(masked);
            out.write('\n');
            written += masked.length() + 1;
            r.kept++;
        }
        return r;
    }
}
