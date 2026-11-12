package com.markus.fw;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Reads a firmware ZIP without changing it: hashes, entry CRCs, package type, and the build properties
 * stored as plain text inside vela_ap.bin of a full package ({@code ro.build.id=user-4.200.078-gl-...}).
 */
public final class FwPackage {
    public enum Type { FULL, DIFF, UNKNOWN }

    /** Entries of a full package as seen in every sample; anything else is not a full package. */
    static final Set<String> FULL_ENTRIES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "ota.sh", "vela_ap.bin", "vela_ota.bin", "vela_resource.bin",
            "META-INF/MANIFEST.MF", "META-INF/CERT.SF", "META-INF/CERT.RSA")));
    static final String AP_IMAGE = "vela_ap.bin";
    private static final Pattern BUILD_ID = Pattern.compile("^([a-z]+)-(\\d+\\.\\d+\\.\\d+)-([a-z]{2})-(\\d+)$");

    public static final class Info {
        public long size;
        public String md5;
        public String sha256;
        public boolean zipOk;
        public String zipError;
        public final List<String> entries = new ArrayList<>();
        public Type type = Type.UNKNOWN;
        public final Set<String> buildIds = new TreeSet<>();
        public final Set<String> buildVersions = new TreeSet<>();
        public final Set<String> miwearModels = new TreeSet<>();
        /** From ro.build.id; null when it cannot be read unambiguously. */
        public String version;
        public String region;
        public final List<String> problems = new ArrayList<>();
        /** miwear_product.json inside vela_ap.bin: hardware code (device_model) of every model the image knows. */
        public boolean productTableFound;
        /** Why the table cannot be used; null when it was read completely. */
        public String productTableError;
        public final List<Product> products = new ArrayList<>();

        /** device_model values the table links to the given miwear_model (empty when absent). */
        public Set<String> deviceModelsOf(String miwearModel) {
            Set<String> out = new TreeSet<>();
            for (Product p : products) {
                if (p.miwearModel.equals(miwearModel)) {
                    out.add(p.deviceModel);
                }
            }
            return out;
        }

        public boolean hasDeviceModel(String deviceModel) {
            for (Product p : products) {
                if (p.deviceModel.equals(deviceModel)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Family shared by every miwear_model inside the image, or null when absent or mixed. The image lists all
         * variants it knows (q66cn, q66gl, q66gln, q66glt, q66glu, q66nfc, q66tc, q66u in 4.200.078-gl), so it shows
         * the family, not the region; the region comes from ro.build.id only.
         */
        public String imageFamily() {
            Set<String> f = new HashSet<>();
            for (String m : miwearModels) {
                f.add(FwName.familyOf(m));
            }
            return f.size() == 1 ? f.iterator().next() : null;
        }
    }

    /** One row of miwear_product.json, e.g. board 5: M2617B1 -> miwear.watch.q66tc, NFC. */
    public static final class Product {
        public final String boardId;
        public final String deviceModel;
        public final String miwearModel;
        public final String isNfc;

        Product(String boardId, String deviceModel, String miwearModel, String isNfc) {
            this.boardId = boardId;
            this.deviceModel = deviceModel;
            this.miwearModel = miwearModel;
            this.isNfc = isNfc;
        }
    }

    private static final Pattern P_DEVICE = Pattern.compile("\"device_model\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern P_MIWEAR = Pattern.compile("\"miwear_model\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern P_BOARD = Pattern.compile("\"board_id\"\\s*:\\s*(\\d+)");
    private static final Pattern P_NFC = Pattern.compile("\"isnfc\"\\s*:\\s*(true|false)");
    private static final Pattern P_ENTRY = Pattern.compile("\\{([^{}]*)\\}");

    private FwPackage() {
    }

    /**
     * Parses the array of a captured {@code "product_info": [ ... ]} block. Every entry must carry device_model and
     * miwear_model; anything else makes the whole table unusable.
     */
    static void parseProducts(String block, Info info) {
        int open = block.indexOf('[');
        int close = block.indexOf(']', open + 1);
        if (open < 0 || close < 0) {
            info.productTableError = "в таблице miwear_product.json нет списка product_info";
            return;
        }
        String body = block.substring(open + 1, close);
        Matcher e = P_ENTRY.matcher(body);
        List<Product> found = new ArrayList<>();
        int consumed = 0;
        while (e.find()) {
            if (!body.substring(consumed, e.start()).trim().matches(",?")) {
                info.productTableError = "таблица miwear_product.json неразборчива";
                return;
            }
            String entry = e.group(1);
            Matcher d = P_DEVICE.matcher(entry);
            Matcher m = P_MIWEAR.matcher(entry);
            if (!d.find() || !m.find() || d.group(1).isEmpty() || m.group(1).isEmpty()) {
                info.productTableError = "в таблице miwear_product.json есть запись без device_model или miwear_model";
                return;
            }
            Matcher b = P_BOARD.matcher(entry);
            Matcher n = P_NFC.matcher(entry);
            found.add(new Product(b.find() ? b.group(1) : "?", d.group(1), m.group(1), n.find() ? n.group(1) : "?"));
            consumed = e.end();
        }
        if (found.isEmpty() || !body.substring(consumed).trim().isEmpty()) {
            info.productTableError = found.isEmpty() ? "таблица miwear_product.json пустая или неразборчива"
                    : "таблица miwear_product.json неразборчива";
            return;
        }
        info.products.addAll(found);
    }

    public static Info inspect(File file) {
        Info info = new Info();
        info.size = file.length();
        try {
            String[] h = hashes(file);
            info.md5 = h[0];
            info.sha256 = h[1];
        } catch (IOException e) {
            info.zipError = "файл не читается: " + e.getMessage();
            return info;
        }
        try (ZipFile zip = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> en = zip.entries();
            boolean hasPatch = false;
            while (en.hasMoreElements()) {
                ZipEntry e = en.nextElement();
                info.entries.add(e.getName());
                if (e.getName().endsWith(".patch")) {
                    hasPatch = true;
                }
                if (!e.isDirectory()) {
                    checkEntry(zip, e, info);
                }
            }
            info.zipOk = true;
            Set<String> names = new HashSet<>(info.entries);
            if (hasPatch) {
                info.type = Type.DIFF;
            } else if (names.equals(FULL_ENTRIES) && names.size() == info.entries.size()) {
                info.type = Type.FULL;
            }
        } catch (ZipException e) {
            info.zipError = "архив повреждён или это не ZIP (" + e.getMessage() + ")";
            return info;
        } catch (IOException e) {
            info.zipError = "архив не читается (" + e.getMessage() + ")";
            return info;
        }
        if (info.type == Type.FULL) {
            readVersion(info);
        }
        return info;
    }

    private static void readVersion(Info info) {
        if (info.buildIds.size() != 1) {
            info.problems.add(info.buildIds.isEmpty() ? "в vela_ap.bin нет ro.build.id"
                    : "в vela_ap.bin несколько разных ro.build.id: " + info.buildIds);
            return;
        }
        Matcher m = BUILD_ID.matcher(info.buildIds.iterator().next());
        if (!m.matches()) {
            info.problems.add("ro.build.id не в ожидаемом формате: " + info.buildIds.iterator().next());
            return;
        }
        if (!info.buildVersions.isEmpty()
                && !(info.buildVersions.size() == 1 && info.buildVersions.contains(m.group(2)))) {
            info.problems.add("ro.build.version " + info.buildVersions + " не совпадает с ro.build.id " + m.group(2));
            return;
        }
        info.version = m.group(2);
        info.region = m.group(3);
    }

    /** Reads an entry to the end, compares its CRC32, and scans vela_ap.bin for build properties. */
    private static void checkEntry(ZipFile zip, ZipEntry e, Info info) throws IOException {
        CRC32 crc = new CRC32();
        PropertyScanner scanner = AP_IMAGE.equals(e.getName()) ? new PropertyScanner(info) : null;
        try (InputStream in = zip.getInputStream(e)) {
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) != -1) {
                crc.update(buf, 0, n);
                if (scanner != null) {
                    scanner.feed(buf, n);
                }
            }
        }
        if (scanner != null) {
            scanner.finish();
        }
        if (e.getCrc() != -1 && crc.getValue() != e.getCrc()) {
            throw new ZipException("неверная контрольная сумма записи " + e.getName());
        }
    }

    /** MD5 and SHA-256 of the whole file, lower-case hex. */
    public static String[] hashes(File file) throws IOException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            try (InputStream in = new FileInputStream(file)) {
                byte[] buf = new byte[64 * 1024];
                int n;
                while ((n = in.read(buf)) != -1) {
                    md5.update(buf, 0, n);
                    sha.update(buf, 0, n);
                }
            }
            return new String[] {hex(md5.digest()), hex(sha.digest())};
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

    public static String sha256(File file) throws IOException {
        return hashes(file)[1];
    }

    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) {
            sb.append(Character.forDigit((x >> 4) & 0xf, 16)).append(Character.forDigit(x & 0xf, 16));
        }
        return sb.toString();
    }

    /** Finds ASCII "key=value" properties across buffer boundaries (a 512-byte overlap is kept). */
    static final class PropertyScanner {
        private static final int OVERLAP = 512;
        private static final String[] KEYS = {"ro.build.id=", "ro.build.version=", "\"miwear_model\": \""};
        private static final String TABLE_KEY = "\"product_info\"";
        /** The table is a few kilobytes; anything longer without a closing bracket is not the table. */
        private static final int TABLE_LIMIT = 64 * 1024;
        private final Info info;
        private byte[] window = new byte[0];
        /** Text of "product_info": [...] being collected across chunks; null when not collecting. */
        private StringBuilder table;
        private boolean tableDone;

        PropertyScanner(Info info) {
            this.info = info;
        }

        /** Called after the last chunk: a table that started but never closed is unusable. */
        void finish() {
            if (table != null && !tableDone) {
                info.productTableError = "таблица miwear_product.json оборвана";
            }
        }

        private void collectTable(String s, String fresh) {
            if (tableDone) {
                return;
            }
            if (table == null) {
                int i = s.indexOf(TABLE_KEY);
                if (i < 0) {
                    return;
                }
                info.productTableFound = true;
                table = new StringBuilder(s.substring(i));
            } else {
                table.append(fresh);
            }
            int open = table.indexOf("[");
            int close = open < 0 ? -1 : table.indexOf("]", open);
            if (close >= 0) {
                tableDone = true;
                parseProducts(table.substring(0, close + 1), info);
            } else if (table.length() > TABLE_LIMIT) {
                tableDone = true;
                info.productTableError = "таблица miwear_product.json неразборчива (нет конца списка)";
            }
        }

        void feed(byte[] buf, int n) {
            byte[] w = new byte[window.length + n];
            System.arraycopy(window, 0, w, 0, window.length);
            System.arraycopy(buf, 0, w, window.length, n);
            String s = new String(w, StandardCharsets.ISO_8859_1);
            collectTable(s, new String(buf, 0, n, StandardCharsets.ISO_8859_1));
            for (String key : KEYS) {
                int from = 0;
                int i;
                while ((i = s.indexOf(key, from)) >= 0) {
                    int start = i + key.length();
                    int end = start;
                    while (end < s.length() && end - start <= 96) {
                        char c = s.charAt(end);
                        if (c < 0x20 || c > 0x7e || c == '"') {
                            break;
                        }
                        end++;
                    }
                    // a value cut at the end of the window is picked up again with the next chunk
                    if (end < s.length() && end > start && end - start <= 96) {
                        String value = s.substring(start, end).trim();
                        if (key.startsWith("ro.build.id")) {
                            info.buildIds.add(value);
                        } else if (key.startsWith("ro.build.version")) {
                            info.buildVersions.add(value);
                        } else {
                            info.miwearModels.add(value);
                        }
                    }
                    from = i + 1;
                }
            }
            int keep = Math.min(OVERLAP, w.length);
            window = Arrays.copyOfRange(w, w.length - keep, w.length);
        }
    }
}
