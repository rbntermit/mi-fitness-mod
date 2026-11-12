package com.markus.fw;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Synthetic firmware packages shaped like the samples, and device snapshots. */
final class Fixtures {
    private Fixtures() {
    }

    static File samples() {
        return new File(System.getProperty("samples.dir", "samples/firmware"));
    }

    static File sample(String md5Prefix) {
        File[] f = samples().listFiles((d, n) -> n.startsWith(md5Prefix));
        return f == null || f.length == 0 ? null : f[0];
    }

    /** miwear_product.json rows as in 4.200.078-gl and 4.100.179-cn: {board_id, device_model, miwear_model, isnfc}. */
    static final String[][] REAL_ROWS = {
        {"0", "M2616B1", "miwear.watch.q66cn", "false"},
        {"1", "M2617B1", "miwear.watch.q66nfc", "true"},
        {"2", "M2616B1", "miwear.watch.q66cn", "false"},
        {"3", "M2616B1", "miwear.watch.q66gl", "false"},
        {"4", "M2617B1", "miwear.watch.q66gln", "true"},
        {"5", "M2617B1", "miwear.watch.q66tc", "true"},
        {"8", "M2617B1", "miwear.watch.q66glt", "true"},
        {"9", "M2617B1", "miwear.watch.q66u", "true"},
        {"10", "M2617B1", "miwear.watch.q66glu", "true"},
    };

    /** The table block as it appears inside vela_ap.bin. */
    static String table(String[][] rows) {
        StringBuilder sb = new StringBuilder("miwear_product.json\0\0\0{\n    \"product_info\": [\n");
        for (int i = 0; i < rows.length; i++) {
            String[] r = rows[i];
            sb.append("        {\n            \"board_id\": ").append(r[0])
                    .append(",\n            \"device_name\": \"Xiaomi Smart Band 11\",\n            \"device_model\": \"")
                    .append(r[1]).append("\",\n            \"miwear_model\": \"").append(r[2])
                    .append("\",\n            \"miwear_pid\": \"34834\",\n            \"isnfc\": ").append(r[3])
                    .append("\n        }").append(i + 1 < rows.length ? "," : "").append('\n');
        }
        return sb.append("    ]\n}\n").toString();
    }

    /** A full package: build properties placed across a 64 KB boundary inside vela_ap.bin, with the real table. */
    static File full(Path dir, String nameModel, String version, String buildRegion, String miwearModel)
            throws IOException {
        return full(dir, nameModel, version, buildRegion, miwearModel, table(REAL_ROWS));
    }

    /** Same, with any table text (or "" for none). The table also straddles a 64 KB boundary. */
    static File full(Path dir, String nameModel, String version, String buildRegion, String miwearModel, String table)
            throws IOException {
        String props = "ro.build.version=" + version + "\0ro.build.id=user-" + version + "-" + buildRegion
                + "-202609111700\0";
        byte[] ap = image(400_000, 65_536 - 20, props + "\"miwear_model\": \"" + miwearModel + "\",\n");
        byte[] t = table.getBytes(StandardCharsets.ISO_8859_1);
        System.arraycopy(t, 0, ap, 131_072 - t.length / 2, t.length);
        return named(dir, nameModel, zip(dir, "ota.sh", "vela_ap.bin", ap, "vela_ota.bin", "vela_resource.bin"));
    }

    /** A diff package: *.patch entries with a DDELTA header, no readable version. */
    static File diff(Path dir, String nameModel) throws IOException {
        byte[] patch = image(50_000, 0, "DDELTA60");
        return named(dir, nameModel, zipEntries(dir, new String[] {"ota.sh", "vela_ap.patch", "vela_ota.bin",
                "vela_resource.patch", "META-INF/MANIFEST.MF", "META-INF/CERT.SF", "META-INF/CERT.RSA"}, patch));
    }

    static byte[] image(int size, int at, String text) {
        byte[] b = new byte[size];
        new Random(size + at).nextBytes(b);
        for (int i = 0; i < b.length; i++) {
            // keep random bytes away from the ASCII keys so only the planted ones are found
            if (b[i] == 'r' || b[i] == '"') {
                b[i] = 0;
            }
        }
        byte[] t = text.getBytes(StandardCharsets.ISO_8859_1);
        System.arraycopy(t, 0, b, at, t.length);
        return b;
    }

    private static File zip(Path dir, String sh, String apName, byte[] ap, String ota, String res) throws IOException {
        File f = File.createTempFile("pkg", ".zip", dir.toFile());
        try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(f))) {
            put(z, sh, "set +e\n".getBytes(StandardCharsets.US_ASCII));
            put(z, apName, ap);
            put(z, ota, image(10_000, 0, ""));
            put(z, res, image(30_000, 0, ""));
            put(z, "META-INF/MANIFEST.MF", "Manifest-Version: 1.0\n".getBytes(StandardCharsets.US_ASCII));
            put(z, "META-INF/CERT.SF", "sf".getBytes(StandardCharsets.US_ASCII));
            put(z, "META-INF/CERT.RSA", "rsa".getBytes(StandardCharsets.US_ASCII));
        }
        return f;
    }

    static File zipEntries(Path dir, String[] names, byte[] payload) throws IOException {
        File f = File.createTempFile("pkg", ".zip", dir.toFile());
        try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(f))) {
            for (String n : names) {
                put(z, n, payload);
            }
        }
        return f;
    }

    private static void put(ZipOutputStream z, String name, byte[] data) throws IOException {
        z.putNextEntry(new ZipEntry(name));
        z.write(data);
        z.closeEntry();
    }

    /** Renames to "<md5>_upd_<model>.zip" like the distributed files. */
    static File named(Path dir, String model, File f) throws IOException {
        String md5 = FwPackage.hashes(f)[0];
        File out = dir.resolve(md5 + "_upd_" + model + ".zip").toFile();
        Files.move(f.toPath(), out.toPath());
        return out;
    }

    /** A band answering everything, with the given models, version and battery. */
    static DeviceSnapshot band(String bound, String reported, String version, int battery) {
        DeviceSnapshot s = new DeviceSnapshot();
        s.hasDevice = true;
        s.connected = true;
        s.boundModel = bound;
        s.cachedVersion = version;
        s.watchInfoOk = true;
        s.reportedModel = reported;
        s.liveVersion = version;
        s.batteryOk = true;
        s.battery = battery;
        s.otaMode = 2;
        return s;
    }
}
