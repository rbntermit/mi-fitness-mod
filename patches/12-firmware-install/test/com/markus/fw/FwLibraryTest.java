package com.markus.fw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Adding, deduplicating, deleting and re-checking stored firmware copies. */
class FwLibraryTest {
    static final String GL = "miwear.watch.q66gl";
    static final long PLENTY = Long.MAX_VALUE / 2;

    @TempDir
    Path tmp;

    FwLibrary lib() {
        return new FwLibrary(tmp.resolve("firmware").toFile());
    }

    /** Short reads, like a ContentResolver stream. */
    static InputStream chunked(File f) throws IOException {
        return new FilterInputStream(new FileInputStream(f)) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return super.read(b, off, Math.min(len, 777));
            }
        };
    }

    FwLibrary.AddResult add(FwLibrary lib, File f, long now) throws IOException {
        return lib.add(chunked(f), f.getName(), f.length(), PLENTY, now);
    }

    @Test
    void cancelledPickChangesNothing() {
        FwLibrary lib = lib();
        assertEquals(FwLibrary.AddStatus.CANCELLED, lib.add(null, null, -1, PLENTY, 1).status);
        assertTrue(lib.list().isEmpty());
    }

    @Test
    void addedCopyIsByteIdenticalAndKeepsOriginalName() throws Exception {
        File f = Fixtures.full(tmp, GL, "4.200.090", "gl", "miwear.watch.q66cn");
        File src = tmp.resolve(f.getName().replace(".zip", " (1).zip")).toFile();
        assertTrue(f.renameTo(src));
        FwLibrary lib = lib();
        FwLibrary.AddResult r = add(lib, src, 10);
        assertEquals(FwLibrary.AddStatus.ADDED, r.status);
        assertEquals(src.getName(), r.record.originalName);
        assertEquals(FwPackage.sha256(src), r.record.sha256);
        assertEquals(FwPackage.hashes(src)[0], r.record.md5);
        assertEquals(src.length(), lib.file(r.record.sha256).length());
        assertTrue(lib.isIntact(r.record.sha256));
    }

    @Test
    void sampleIsCopiedUnchanged() throws Exception {
        File f = Fixtures.sample("48dc");
        assumeTrue(f != null, "sample 48dc missing");
        FwLibrary lib = lib();
        FwLibrary.AddResult r = add(lib, f, 1);
        assertEquals(FwLibrary.AddStatus.ADDED, r.status);
        assertEquals("754da125b2864eb340ab9127c5b15961961943f2798a6ded458c6901168cec5f", r.record.sha256);
        assertEquals(f.length(), lib.file(r.record.sha256).length());
        lib.delete(r.record.sha256);
    }

    @Test
    void sameFileTwiceOpensTheExistingRecord() throws Exception {
        File f = Fixtures.full(tmp, GL, "4.200.090", "gl", "miwear.watch.q66cn");
        FwLibrary lib = lib();
        FwLibrary.AddResult first = add(lib, f, 1);
        FwLibrary.AddResult second = add(lib, f, 2);
        assertEquals(FwLibrary.AddStatus.DUPLICATE, second.status);
        assertEquals(first.record.sha256, second.record.sha256);
        assertEquals(1, lib.list().size());
        assertEquals(1, first.record.addedAt);
        File[] files = tmp.resolve("firmware").toFile().listFiles();
        assertEquals(2, files.length, "one copy and one record, no temp files");
    }

    @Test
    void notEnoughSpaceStoresNothing() throws Exception {
        File f = Fixtures.full(tmp, GL, "4.200.090", "gl", "miwear.watch.q66cn");
        FwLibrary lib = lib();
        FwLibrary.AddResult r = lib.add(chunked(f), f.getName(), f.length(), f.length() + 1000, 1);
        assertEquals(FwLibrary.AddStatus.NO_SPACE, r.status);
        assertNotNull(r.error);
        assertTrue(lib.list().isEmpty());
    }

    @Test
    void streamShorterThanDeclaredIsRejected() {
        FwLibrary lib = lib();
        FwLibrary.AddResult r = lib.add(new ByteArrayInputStream(new byte[100]), "x.zip", 200, PLENTY, 1);
        assertEquals(FwLibrary.AddStatus.ERROR, r.status);
        assertTrue(lib.list().isEmpty());
    }

    @Test
    void brokenFileIsListedButBlocked() throws Exception {
        File f = tmp.resolve("00000000000000000000000000000000_upd_" + GL + ".zip").toFile();
        java.nio.file.Files.write(f.toPath(), "broken".getBytes());
        FwLibrary lib = lib();
        FwLibrary.AddResult r = add(lib, f, 1);
        assertEquals(FwLibrary.AddStatus.ADDED, r.status);
        assertEquals(1, lib.list().size());
        FwPolicy.Verdict v = FwPolicy.evaluate(FwName.parse(r.record.originalName),
                FwPackage.inspect(lib.file(r.record.sha256)), Fixtures.band(GL, GL, "4.200.078", 80), true, true);
        assertTrue(v.failed(FwPolicy.C_ZIP));
        assertFalse(v.allowed());
    }

    @Test
    void modifiedStoredCopyIsDetected() throws Exception {
        File f = Fixtures.full(tmp, GL, "4.200.090", "gl", "miwear.watch.q66cn");
        FwLibrary lib = lib();
        String sha = add(lib, f, 1).record.sha256;
        try (RandomAccessFile r = new RandomAccessFile(lib.file(sha), "rw")) {
            r.seek(10);
            int b = r.read();
            r.seek(10);
            r.write(b ^ 1);
        }
        assertFalse(lib.isIntact(sha));
        FwPolicy.Verdict v = FwPolicy.evaluate(FwName.parse(f.getName()), FwPackage.inspect(lib.file(sha)),
                Fixtures.band(GL, GL, "4.200.078", 80), lib.isIntact(sha), true);
        assertTrue(v.failed(FwPolicy.C_COPY));
        assertFalse(v.allowed());
    }

    @Test
    void deleteRemovesOnlyTheCopy() throws Exception {
        File f = Fixtures.full(tmp, GL, "4.200.090", "gl", "miwear.watch.q66cn");
        FwLibrary lib = lib();
        String sha = add(lib, f, 1).record.sha256;
        assertTrue(lib.delete(sha));
        assertNull(lib.get(sha));
        assertFalse(lib.file(sha).exists());
        assertTrue(lib.list().isEmpty());
        assertTrue(f.isFile(), "the picked source file is untouched");
    }

    @Test
    void statusIsRecomputedWhenTheBandVersionChanges() throws Exception {
        File f = Fixtures.full(tmp, GL, "4.200.090", "gl", "miwear.watch.q66cn");
        FwLibrary lib = lib();
        FwLibrary.Record rec = add(lib, f, 1).record;
        FwPackage.Info info = FwPackage.inspect(lib.file(rec.sha256));
        FwName name = FwName.parse(rec.originalName);
        assertTrue(FwPolicy.evaluate(name, info, Fixtures.band(GL, GL, "4.200.078", 80), true, true).allowed());
        // after the band was updated by other means the same file is no longer newer
        FwPolicy.Verdict after = FwPolicy.evaluate(name, info, Fixtures.band(GL, GL, "4.200.090", 80), true, true);
        assertTrue(after.failed(FwPolicy.C_NEWER));
        assertFalse(after.allowed());
    }

    @Test
    void successOnlyAfterTheBandReportsTheNewVersion() throws Exception {
        File f = Fixtures.full(tmp, GL, "4.200.090", "gl", "miwear.watch.q66cn");
        FwLibrary lib = lib();
        FwLibrary.Record rec = add(lib, f, 1).record;
        rec.state = FwLibrary.InstallState.TRANSFERRED;
        rec.targetVersion = "4.200.090";
        lib.save(rec);

        DeviceSnapshot rebooting = Fixtures.band(GL, GL, "4.200.078", 80);
        rebooting.connected = false;
        assertEquals(FwLibrary.InstallState.TRANSFERRED, FwLibrary.reconcile(lib.get(rec.sha256), rebooting).state);
        DeviceSnapshot oldVersion = Fixtures.band(GL, GL, "4.200.078", 80);
        assertEquals(FwLibrary.InstallState.TRANSFERRED, FwLibrary.reconcile(lib.get(rec.sha256), oldVersion).state);
        DeviceSnapshot updated = Fixtures.band(GL, GL, "4.200.090", 80);
        assertEquals(FwLibrary.InstallState.CONFIRMED, FwLibrary.reconcile(lib.get(rec.sha256), updated).state);
    }

    @Test
    void failedTransferStaysFailed() throws Exception {
        File f = Fixtures.full(tmp, GL, "4.200.090", "gl", "miwear.watch.q66cn");
        FwLibrary lib = lib();
        FwLibrary.Record rec = add(lib, f, 1).record;
        rec.state = FwLibrary.InstallState.FAILED;
        rec.targetVersion = "4.200.090";
        rec.stateNote = "передача прервана";
        lib.save(rec);
        FwLibrary.Record again = FwLibrary.reconcile(lib.get(rec.sha256), Fixtures.band(GL, GL, "4.200.090", 80));
        assertEquals(FwLibrary.InstallState.FAILED, again.state, "nothing is retried or re-labelled automatically");
        assertEquals("передача прервана", again.stateNote);
    }
}
