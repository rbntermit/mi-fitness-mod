package com.markus.rpk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RpkFileTest {
    @TempDir
    Path tmp;

    File rpk(String manifest) throws Exception {
        File f = Files.createTempFile(tmp, "app", ".rpk").toFile();
        try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(f))) {
            if (manifest != null) {
                z.putNextEntry(new ZipEntry("manifest.json"));
                z.write(manifest.getBytes(StandardCharsets.UTF_8));
                z.closeEntry();
            }
            z.putNextEntry(new ZipEntry("app.js"));
            z.write("console.log(1)".getBytes(StandardCharsets.UTF_8));
            z.closeEntry();
        }
        return f;
    }

    @Test
    void ordinaryManifest() throws Exception {
        RpkFile r = RpkFile.read(rpk("{\"package\":\"com.example.timer\",\"name\":\"Таймер\",\"versionName\":\"1.2.0\","
                + "\"versionCode\":12,\"features\":[{\"name\":\"system.vibrator\"}],\"icon\":\"/common/logo.png\"}"));
        assertEquals("com.example.timer", r.packageName);
        assertEquals("Таймер", r.name);
        assertEquals("1.2.0", r.versionName);
        assertEquals(12, r.versionCode);
        assertTrue(r.size > 0);
    }

    @Test
    void nestedNameDoesNotWinOverTheTopLevelOne() throws Exception {
        RpkFile r = RpkFile.read(rpk("{\"features\":[{\"name\":\"system.x\"}],\"package\":\"a.b\",\"versionCode\":1,"
                + "\"name\":\"App\"}"));
        assertEquals("App", r.name);
    }

    @Test
    void versionCodeAsStringAndMissingNameAndBom() throws Exception {
        RpkFile r = RpkFile.read(rpk("﻿{ \"package\" : \"com.x.y\", \"versionCode\" : \"7\" }"));
        assertEquals(7, r.versionCode);
        assertEquals("com.x.y", r.name);
        assertEquals("7", r.versionName);
    }

    @Test
    void badFilesAreRejectedWithAReason() throws Exception {
        assertThrows(RpkFile.Problem.class, () -> RpkFile.read(rpk(null)), "no manifest");
        assertThrows(RpkFile.Problem.class, () -> RpkFile.read(rpk("{\"versionCode\":1}")), "no package");
        assertThrows(RpkFile.Problem.class, () -> RpkFile.read(rpk("{\"package\":\"x\",\"versionCode\":1}")), "bad package");
        assertThrows(RpkFile.Problem.class, () -> RpkFile.read(rpk("{\"package\":\"a.b\"}")), "no versionCode");
        assertThrows(RpkFile.Problem.class, () -> RpkFile.read(rpk("{\"package\":\"a.b\",\"versionCode\":1.5}")), "fraction");
        assertThrows(RpkFile.Problem.class, () -> RpkFile.read(rpk("{\"package\":\"a.b\",\"versionCode\":0}")), "zero");
        assertThrows(RpkFile.Problem.class, () -> RpkFile.read(rpk("{\"package\":\"a.b\",")), "broken json");
        assertThrows(RpkFile.Problem.class, () -> RpkFile.read(rpk("[1,2]")), "not an object");
        File notZip = tmp.resolve("x.rpk").toFile();
        Files.write(notZip.toPath(), "hello".getBytes(StandardCharsets.UTF_8));
        RpkFile.Problem p = assertThrows(RpkFile.Problem.class, () -> RpkFile.read(notZip));
        assertTrue(p.getMessage().contains("ZIP"), p.getMessage());
        File empty = tmp.resolve("e.rpk").toFile();
        Files.write(empty.toPath(), new byte[0]);
        assertThrows(RpkFile.Problem.class, () -> RpkFile.read(empty));
    }

    @Test
    void jsonStringsWithEscapes() throws Exception {
        String bs = "\\";
        RpkFile r = RpkFile.read(rpk("{\"package\":\"a.b\",\"versionCode\":3,\"name\":\"Q" + bs + "u0022" + bs + "t\"}"));
        assertEquals("Q\"", r.name, "escapes decoded, edges trimmed");
    }
}
