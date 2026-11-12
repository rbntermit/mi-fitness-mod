package com.markus.fw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Firmware copies kept in the app's own storage: {@code <dir>/<sha256>.zip} plus {@code <sha256>.properties}.
 * Files are copied byte for byte (no unpacking) and identified by SHA-256, so the same file is never stored twice.
 * Deleting a record only removes these two files.
 */
public final class FwLibrary {
    /** Space left free on top of the file itself. */
    static final long RESERVE = 16L * 1024 * 1024;

    public enum AddStatus { ADDED, DUPLICATE, CANCELLED, NO_SPACE, ERROR }

    public enum InstallState { NONE, TRANSFERRING, TRANSFERRED, CONFIRMED, FAILED }

    public static final class Record {
        public String sha256;
        public String originalName;
        public long size;
        public String md5;
        public long addedAt;
        public InstallState state = InstallState.NONE;
        /** Version sent to the band; success is confirmed only when the band reports it. */
        public String targetVersion;
        public String stateNote;
    }

    public static final class AddResult {
        public final AddStatus status;
        public final Record record;
        public final String error;

        AddResult(AddStatus status, Record record, String error) {
            this.status = status;
            this.record = record;
            this.error = error;
        }
    }

    private final File dir;

    public FwLibrary(File dir) {
        this.dir = dir;
    }

    public File file(String sha256) {
        return new File(dir, sha256 + ".zip");
    }

    private File meta(String sha256) {
        return new File(dir, sha256 + ".properties");
    }

    /**
     * Copies a picked file into the library. {@code in == null} means the picker was cancelled.
     * {@code declaredSize} is the size the picker reported (-1 if unknown); {@code usableSpace} the free space of dir.
     */
    public AddResult add(InputStream in, String displayName, long declaredSize, long usableSpace, long now) {
        if (in == null) {
            return new AddResult(AddStatus.CANCELLED, null, null);
        }
        try {
            return copyIn(in, displayName, declaredSize, usableSpace, now);
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
                // the copy already finished or failed; nothing to report
            }
        }
    }

    private AddResult copyIn(InputStream in, String displayName, long declaredSize, long usableSpace, long now) {
        if (declaredSize > 0 && usableSpace < declaredSize + RESERVE) {
            return new AddResult(AddStatus.NO_SPACE, null, "нужно " + mb(declaredSize + RESERVE)
                    + ", свободно " + mb(usableSpace));
        }
        if (!dir.isDirectory() && !dir.mkdirs()) {
            return new AddResult(AddStatus.ERROR, null, "не удалось создать папку для прошивок");
        }
        File tmp = new File(dir, "incoming-" + now + ".tmp");
        long copied = 0;
        MessageDigest sha;
        MessageDigest md5;
        try {
            sha = MessageDigest.getInstance("SHA-256");
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return new AddResult(AddStatus.ERROR, null, e.toString());
        }
        try (InputStream src = in; OutputStream out = new FileOutputStream(tmp)) {
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = src.read(buf)) != -1) {
                out.write(buf, 0, n);
                sha.update(buf, 0, n);
                md5.update(buf, 0, n);
                copied += n;
            }
        } catch (IOException e) {
            tmp.delete();
            return new AddResult(AddStatus.ERROR, null, "файл не скопирован: " + e.getMessage());
        }
        if (declaredSize > 0 && copied != declaredSize) {
            tmp.delete();
            return new AddResult(AddStatus.ERROR, null, "прочитано " + copied + " байт из " + declaredSize);
        }
        String hash = FwPackage.hex(sha.digest());
        Record existing = get(hash);
        if (existing != null && file(hash).isFile()) {
            tmp.delete();
            return new AddResult(AddStatus.DUPLICATE, existing, null);
        }
        if (!tmp.renameTo(file(hash))) {
            tmp.delete();
            return new AddResult(AddStatus.ERROR, null, "не удалось сохранить копию");
        }
        Record r = new Record();
        r.sha256 = hash;
        r.originalName = displayName == null ? "" : displayName;
        r.size = copied;
        r.md5 = FwPackage.hex(md5.digest());
        r.addedAt = now;
        try {
            save(r);
        } catch (IOException e) {
            file(hash).delete();
            return new AddResult(AddStatus.ERROR, null, "не удалось записать данные прошивки: " + e.getMessage());
        }
        return new AddResult(AddStatus.ADDED, r, null);
    }

    public Record get(String sha256) {
        File m = meta(sha256);
        if (!m.isFile()) {
            return null;
        }
        Properties p = new Properties();
        try (Reader r = new InputStreamReader(new FileInputStream(m), StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException e) {
            return null;
        }
        Record rec = new Record();
        rec.sha256 = sha256;
        rec.originalName = p.getProperty("originalName", "");
        rec.size = Long.parseLong(p.getProperty("size", "0"));
        rec.md5 = p.getProperty("md5");
        rec.addedAt = Long.parseLong(p.getProperty("addedAt", "0"));
        try {
            rec.state = InstallState.valueOf(p.getProperty("state", "NONE"));
        } catch (IllegalArgumentException e) {
            rec.state = InstallState.NONE;
        }
        rec.targetVersion = p.getProperty("targetVersion");
        rec.stateNote = p.getProperty("stateNote");
        return rec;
    }

    public void save(Record r) throws IOException {
        Properties p = new Properties();
        p.setProperty("originalName", r.originalName);
        p.setProperty("size", Long.toString(r.size));
        if (r.md5 != null) {
            p.setProperty("md5", r.md5);
        }
        p.setProperty("addedAt", Long.toString(r.addedAt));
        p.setProperty("state", r.state.name());
        if (r.targetVersion != null) {
            p.setProperty("targetVersion", r.targetVersion);
        }
        if (r.stateNote != null) {
            p.setProperty("stateNote", r.stateNote);
        }
        File tmp = new File(dir, r.sha256 + ".properties.tmp");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
            p.store(w, null);
        }
        File m = meta(r.sha256);
        if (m.exists() && !m.delete() || !tmp.renameTo(m)) {
            tmp.delete();
            throw new IOException("не удалось заменить " + m.getName());
        }
    }

    /** Newest first. */
    public List<Record> list() {
        List<Record> out = new ArrayList<>();
        File[] files = dir.listFiles((d, n) -> n.endsWith(".properties"));
        if (files != null) {
            for (File f : files) {
                Record r = get(f.getName().substring(0, f.getName().length() - ".properties".length()));
                if (r != null) {
                    out.add(r);
                }
            }
        }
        Collections.sort(out, (a, b) -> Long.compare(b.addedAt, a.addedAt));
        return out;
    }

    /** Removes the copy and its record. Nothing on the band changes. */
    public boolean delete(String sha256) {
        boolean a = !file(sha256).exists() || file(sha256).delete();
        boolean b = !meta(sha256).exists() || meta(sha256).delete();
        return a && b;
    }

    /** The stored copy still has the SHA-256 it was saved under. */
    public boolean isIntact(String sha256) {
        File f = file(sha256);
        try {
            return f.isFile() && sha256.equals(FwPackage.sha256(f));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Success is shown only after the band reconnects and reports the version that was sent.
     * Returns the record with an updated state (the caller saves it).
     */
    public static Record reconcile(Record r, DeviceSnapshot d) {
        if (r.state == InstallState.TRANSFERRED && r.targetVersion != null && d.connected && d.watchInfoOk
                && r.targetVersion.equals(d.liveVersion)) {
            r.state = InstallState.CONFIRMED;
            r.stateNote = "браслет сообщил версию " + d.liveVersion;
        }
        return r;
    }

    static String mb(long bytes) {
        return (bytes + 1024 * 1024 - 1) / (1024 * 1024) + " МБ";
    }
}
