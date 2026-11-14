package com.markus.dnd;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

/** One byte shared by app processes. A file lock covers migration, reads and writes, unlike cached preferences. */
public final class SharedFlag {
    private SharedFlag() {}
    public static synchronized boolean read(File file, boolean legacy) throws IOException {
        try (RandomAccessFile f = new RandomAccessFile(file, "rw"); FileLock lock = f.getChannel().lock()) {
            if (f.length() == 0) { f.write(legacy ? 1 : 0); f.getFD().sync(); }
            f.seek(0);
            return f.read() == 1;
        }
    }
    public static synchronized void write(File file, boolean enabled) throws IOException {
        try (RandomAccessFile f = new RandomAccessFile(file, "rw"); FileLock lock = f.getChannel().lock()) {
            f.seek(0); f.write(enabled ? 1 : 0); f.setLength(1); f.getFD().sync();
        }
    }
}