package com.markus.ota;

import java.io.File;

/**
 * Permission for one firmware transfer: the exact file and a deadline. Issued only by the firmware screen of
 * patch 12 right before it sends a package that passed all its checks. Pure logic, no Android.
 */
final class OtaPermit {
    /** Mass-transfer type of a firmware package (GeneralOtaExecutor sends with type 2; watch faces 1, apps 4...). */
    static final int FILE_TYPE_FIRMWARE = 2;
    /** Covers prepareOta -> startOta -> sendFile of one install; a transfer already running is not cut off. */
    static final long TTL_MS = 120_000;

    final String path;
    final long expiresAt;

    private OtaPermit(String path, long expiresAt) {
        this.path = path;
        this.expiresAt = expiresAt;
    }

    /** Path of the permit for a stock update that StockOtaPolicy allowed: any firmware file, for TTL_MS. */
    static final String STOCK = "*";

    /** Permit for a stock update the policy allowed (the stock code picks its own downloaded file). */
    static OtaPermit stock(long now) {
        return new OtaPermit(STOCK, now + TTL_MS);
    }

    boolean isStock() {
        return STOCK.equals(path);
    }

    /** Null when there is no usable file path. */
    static OtaPermit grant(String path, long now) {
        String p = normalize(path);
        return p == null ? null : new OtaPermit(p, now + TTL_MS);
    }

    boolean valid(long now) {
        return now < expiresAt;
    }

    /** True only for the very file the permit was issued for, before the deadline. */
    boolean allows(String file, long now) {
        String p = normalize(file);
        return p != null && valid(now) && (isStock() || p.equals(path));
    }

    static boolean isFirmwareTransfer(int type) {
        return type == FILE_TYPE_FIRMWARE;
    }

    /** prepareOta carries no path; it only asks the band to get ready, so any live permit is enough. */
    static boolean prepareAllowed(OtaPermit p, long now) {
        return p != null && p.valid(now);
    }

    /** startOta and sendFile name the file: it has to be the one the permit was issued for. */
    static boolean startAllowed(OtaPermit p, String path, long now) {
        return p != null && p.allows(path, now);
    }

    /** Watch faces, apps, music, sync files... (any type but firmware) are never touched. */
    static boolean fileAllowed(int type, OtaPermit p, String path, long now) {
        return !isFirmwareTransfer(type) || startAllowed(p, path, now);
    }

    /** At most one notice per TOAST_GAP_MS, so a burst of blocked calls shows the text once. */
    static final long TOAST_GAP_MS = 3_000;

    static boolean toastDue(long lastToast, long now) {
        return lastToast == 0 || now - lastToast >= TOAST_GAP_MS || now < lastToast;
    }

    /** One line per field; the path is last because it may contain anything but a line break. */
    String encode() {
        return expiresAt + "\n" + path;
    }

    static OtaPermit decode(String s) {
        if (s == null) {
            return null;
        }
        int nl = s.indexOf('\n');
        if (nl <= 0) {
            return null;
        }
        try {
            long exp = Long.parseLong(s.substring(0, nl).trim());
            String p = normalize(s.substring(nl + 1));
            return p == null ? null : new OtaPermit(p, exp);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Same spelling as GeneralOtaExecutor uses for sendFile (File.getAbsolutePath of the path given to startOta). */
    static String normalize(String path) {
        if (path == null) {
            return null;
        }
        String p = path.trim();
        if (p.isEmpty() || p.indexOf('\n') >= 0) {
            return null;
        }
        if (STOCK.equals(p)) {
            return STOCK;
        }
        return new File(p).getAbsolutePath();
    }
}
