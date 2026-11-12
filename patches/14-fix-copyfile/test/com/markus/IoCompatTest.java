package com.markus;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Random;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class IoCompatTest {
    @TempDir
    Path tmp;

    /** A stream that never returns more than {@code chunk} bytes per read, like ContentResolver or ZIP entries. */
    static final class ChunkedStream extends FilterInputStream {
        private final int chunk;

        ChunkedStream(InputStream in, int chunk) {
            super(in);
            this.chunk = chunk;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return super.read(b, off, Math.min(len, chunk));
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }
    }

    /** The copy loop the mod shipped with (4 KB buffer, write of the whole buffer). Kept to show the bug. */
    static void legacyCopy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[4096];
        while (in.read(buf) > 0) {
            out.write(buf);
        }
    }

    static String sha256(File f) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new FileInputStream(f)) {
            byte[] b = new byte[1 << 16];
            int n;
            while ((n = in.read(b)) != -1) {
                md.update(b, 0, n);
            }
        }
        return HexFormat.of().formatHex(md.digest());
    }

    static File randomFile(Path dir, String name, int size, byte[] header) throws IOException {
        byte[] data = new byte[size];
        new Random(size).nextBytes(data);
        if (header != null) {
            System.arraycopy(header, 0, data, 0, Math.min(header.length, size));
        }
        File f = dir.resolve(name).toFile();
        Files.write(f.toPath(), data);
        return f;
    }

    File copyOf(File src, int chunk) throws IOException {
        File dst = tmp.resolve(src.getName() + ".copy" + chunk).toFile();
        try (InputStream in = new ChunkedStream(new FileInputStream(src), chunk);
             OutputStream out = new FileOutputStream(dst)) {
            assertEquals(src.length(), IoCompat.copy(in, out));
        }
        return dst;
    }

    @ParameterizedTest(name = "size {0}")
    @ValueSource(ints = {0, 1, 4095, 4096, 4097, 8192, 12288, 100000, 1048576, 1048577})
    void copyKeepsSizeAndHash(int size) throws Exception {
        File src = randomFile(tmp, "f" + size, size, null);
        for (int chunk : new int[] {1, 1000, 4096, 65536}) {
            File dst = copyOf(src, chunk);
            assertEquals(src.length(), dst.length(), "size, chunk " + chunk);
            assertEquals(sha256(src), sha256(dst), "sha256, chunk " + chunk);
        }
    }

    @Test
    void watchfaceFileIsCopiedExactly() throws Exception {
        byte[] magic = {0x5A, (byte) 0xA5, 0x34, 0x12};
        for (int size : new int[] {4096 * 300, 4096 * 300 + 123}) {
            File src = randomFile(tmp, "face" + size + ".bin", size, magic);
            File dst = copyOf(src, 1500);
            assertEquals(src.length(), dst.length());
            assertEquals(sha256(src), sha256(dst));
        }
    }

    @Test
    void firmwareSamplesAreCopiedExactly() throws Exception {
        File dir = new File(System.getProperty("samples.dir", "samples/firmware"));
        File[] samples = dir.listFiles((d, n) -> n.endsWith(".zip"));
        assumeTrue(samples != null && samples.length > 0, "no samples in " + dir);
        for (File src : samples) {
            assertNotEquals(0, src.length() % 4096, "samples are expected to be non-multiples of 4 KB: " + src);
            File dst = copyOf(src, 3000);
            assertEquals(src.length(), dst.length(), src.getName());
            assertEquals(sha256(src), sha256(dst), src.getName());
            dst.delete();
        }
    }

    @Test
    void legacyLoopCorruptsShortReadsAndPadsSize() throws Exception {
        File src = randomFile(tmp, "legacy", 10000, null);
        File dst = tmp.resolve("legacy.out").toFile();
        try (InputStream in = new ChunkedStream(new FileInputStream(src), 1000);
             OutputStream out = new FileOutputStream(dst)) {
            legacyCopy(in, out);
        }
        assertNotEquals(src.length(), dst.length());
    }

    @Test
    void readUpToFillsBufferAcrossShortReads() throws Exception {
        byte[] data = new byte[5000];
        new Random(7).nextBytes(data);
        byte[] buf = new byte[4096];
        int n = IoCompat.readUpTo(new ChunkedStream(new ByteArrayInputStream(data), 7), buf);
        assertEquals(4096, n);
        byte[] expected = new byte[4096];
        System.arraycopy(data, 0, expected, 0, 4096);
        assertArrayEquals(expected, buf);
    }

    @Test
    void readUpToStopsAtEndOfStream() throws Exception {
        byte[] buf = new byte[4096];
        assertEquals(100, IoCompat.readUpTo(new ChunkedStream(new ByteArrayInputStream(new byte[100]), 9), buf));
        assertEquals(0, IoCompat.readUpTo(new ByteArrayInputStream(new byte[0]), buf));
    }

    @Test
    void copyToMemory() throws Exception {
        byte[] data = new byte[12345];
        new Random(3).nextBytes(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertEquals(12345, IoCompat.copy(new ChunkedStream(new ByteArrayInputStream(data), 333), out));
        assertArrayEquals(data, out.toByteArray());
    }
}
