package com.markus.fw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** File name parsing and package inspection, on the real samples and on synthetic packages. */
class FwFileTest {
    @TempDir
    Path tmp;

    @Test
    void nameInOriginalFormat() {
        FwName n = FwName.parse("48dc7653daa78d00e50e778f342157f2_upd_miwear.watch.q66gl.zip");
        assertEquals("48dc7653daa78d00e50e778f342157f2", n.md5);
        assertEquals("miwear.watch.q66gl", n.model);
        assertEquals("q66", n.family);
        assertEquals("gl", n.region);
        assertEquals("", n.suffix);
    }

    @Test
    void nameWithBrowserSuffixKeepsModelAndOriginal() {
        for (String s : new String[] {" (1)", "(2)", "-3"}) {
            String orig = "48DC7653DAA78D00E50E778F342157F2_upd_miwear.watch.q66cn" + s + ".zip";
            FwName n = FwName.parse(orig);
            assertTrue(n.recognized(), s);
            assertEquals(orig, n.original);
            assertEquals("cn", n.region);
            assertEquals("48dc7653daa78d00e50e778f342157f2", n.md5);
        }
    }

    @Test
    void nameWithoutMd5StillShowsModel() {
        FwName n = FwName.parse("upd_miwear.watch.q66tc.zip");
        assertTrue(n.recognized());
        assertNull(n.md5);
        assertEquals("tc", n.region);
    }

    @Test
    void renamedFileIsNotRecognized() {
        for (String s : new String[] {"firmware.zip", "q66gl.zip", "48dc7653daa78d00e50e778f342157f2.zip",
                "48dc7653daa78d00e50e778f342157f2_upd_miwear.watch.q66.zip", "upd_miwear.watch.q66gl.bin"}) {
            assertFalse(FwName.parse(s).recognized(), s);
        }
    }

    @Test
    void sampleFullGlobal() throws Exception {
        File f = Fixtures.sample("48dc");
        assumeTrue(f != null, "sample 48dc missing");
        FwPackage.Info i = FwPackage.inspect(f);
        assertTrue(i.zipOk, i.zipError);
        assertEquals(FwPackage.Type.FULL, i.type);
        assertEquals("4.200.078", i.version);
        assertEquals("gl", i.region);
        assertEquals("q66", i.imageFamily());
        // the image lists every q66 variant, so miwear_model cannot tell the region
        assertEquals(new java.util.TreeSet<>(java.util.Arrays.asList("miwear.watch.q66cn", "miwear.watch.q66gl",
                "miwear.watch.q66gln", "miwear.watch.q66glt", "miwear.watch.q66glu", "miwear.watch.q66nfc",
                "miwear.watch.q66tc", "miwear.watch.q66u")), i.miwearModels);
        assertEquals("48dc7653daa78d00e50e778f342157f2", i.md5);
        assertEquals(FwName.parse(f.getName()).md5, i.md5, "name prefix is the MD5 of the whole file");
        // miwear_product.json of this very file
        assertTrue(i.productTableFound);
        assertEquals(null, i.productTableError);
        assertEquals(9, i.products.size());
        assertEquals(java.util.Collections.singleton("M2617B1"), i.deviceModelsOf("miwear.watch.q66tc"));
        assertEquals(java.util.Collections.singleton("M2616B1"), i.deviceModelsOf("miwear.watch.q66gl"));
    }

    @Test
    void syntheticTableIsReadAcrossBufferBoundary() throws Exception {
        File f = Fixtures.full(tmp, "miwear.watch.q66gl", "4.200.090", "gl", "miwear.watch.q66cn");
        FwPackage.Info i = FwPackage.inspect(f);
        assertTrue(i.productTableFound);
        assertEquals(null, i.productTableError);
        assertEquals(Fixtures.REAL_ROWS.length, i.products.size());
        assertEquals(java.util.Collections.singleton("M2617B1"), i.deviceModelsOf("miwear.watch.q66tc"));
    }

    @Test
    void sampleFullChinese() throws Exception {
        File f = Fixtures.sample("9594");
        assumeTrue(f != null, "sample 9594 missing");
        FwPackage.Info i = FwPackage.inspect(f);
        assertEquals(FwPackage.Type.FULL, i.type);
        assertEquals("4.100.179", i.version);
        assertEquals("cn", i.region);
        assertEquals(FwName.parse(f.getName()).md5, i.md5);
    }

    @Test
    void sampleDiffHasNoVersion() throws Exception {
        File f = Fixtures.sample("c01c");
        assumeTrue(f != null, "sample c01c missing");
        FwPackage.Info i = FwPackage.inspect(f);
        assertTrue(i.zipOk);
        assertEquals(FwPackage.Type.DIFF, i.type);
        assertNull(i.version);
        assertEquals(FwName.parse(f.getName()).md5, i.md5);
    }

    @Test
    void syntheticFullAcrossBufferBoundary() throws Exception {
        File f = Fixtures.full(tmp, "miwear.watch.q66gl", "4.200.090", "gl", "miwear.watch.q66cn");
        FwPackage.Info i = FwPackage.inspect(f);
        assertEquals(FwPackage.Type.FULL, i.type);
        assertEquals("4.200.090", i.version);
        assertEquals("gl", i.region);
        assertEquals("q66", i.imageFamily());
    }

    @Test
    void brokenZip() throws Exception {
        File f = tmp.resolve("x.zip").toFile();
        Files.write(f.toPath(), "this is not a zip".getBytes());
        FwPackage.Info i = FwPackage.inspect(f);
        assertFalse(i.zipOk);
        assertNotNull(i.zipError);
    }

    @Test
    void truncatedZip() throws Exception {
        File f = Fixtures.full(tmp, "miwear.watch.q66gl", "4.200.090", "gl", "miwear.watch.q66cn");
        try (RandomAccessFile r = new RandomAccessFile(f, "rw")) {
            r.setLength(r.length() / 2);
        }
        assertFalse(FwPackage.inspect(f).zipOk);
    }

    @Test
    void corruptedEntryFailsCrc() throws Exception {
        File f = Fixtures.full(tmp, "miwear.watch.q66gl", "4.200.090", "gl", "miwear.watch.q66cn");
        try (RandomAccessFile r = new RandomAccessFile(f, "rw")) {
            long pos = r.length() / 3;
            r.seek(pos);
            int b = r.read();
            r.seek(pos);
            r.write(b ^ 0xff);
        }
        assertFalse(FwPackage.inspect(f).zipOk);
    }

    @Test
    void zipThatIsNotFirmware() throws Exception {
        File f = Fixtures.zipEntries(tmp, new String[] {"readme.txt", "photo.jpg"}, new byte[] {1, 2, 3});
        FwPackage.Info i = FwPackage.inspect(f);
        assertTrue(i.zipOk);
        assertEquals(FwPackage.Type.UNKNOWN, i.type);
        assertNull(i.version);
    }

    @Test
    void fullWithoutBuildIdHasNoVersion() throws Exception {
        File f = Fixtures.named(tmp, "miwear.watch.q66gl", Fixtures.zipEntries(tmp, new String[] {"ota.sh",
                "vela_ap.bin", "vela_ota.bin", "vela_resource.bin", "META-INF/MANIFEST.MF", "META-INF/CERT.SF",
                "META-INF/CERT.RSA"}, Fixtures.image(1000, 0, "")));
        FwPackage.Info i = FwPackage.inspect(f);
        assertEquals(FwPackage.Type.FULL, i.type);
        assertNull(i.version);
        assertFalse(i.problems.isEmpty());
    }
}
