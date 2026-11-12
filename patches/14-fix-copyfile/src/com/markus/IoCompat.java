package com.markus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Stream helpers for the mod's file-picker code.
 * The original copy loop wrote the whole 4 KB buffer after every read, so copies grew to a multiple
 * of 4 KB and got garbage in the middle when a stream returned short reads (ContentResolver, ZIP entries).
 */
public final class IoCompat {
    private IoCompat() {
    }

    /** Copies everything from {@code in} to {@code out}, writing exactly the bytes read. Returns the byte count. */
    public static long copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[64 * 1024];
        long total = 0;
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
            total += n;
        }
        out.flush();
        return total;
    }

    /** Reads until {@code buf} is full or the stream ends. Returns the number of bytes read. */
    public static int readUpTo(InputStream in, byte[] buf) throws IOException {
        int off = 0;
        while (off < buf.length) {
            int n = in.read(buf, off, buf.length - off);
            if (n == -1) {
                break;
            }
            off += n;
        }
        return off;
    }
}
