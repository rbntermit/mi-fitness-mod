package com.markus.dnd;
import java.io.*;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class SharedFlagTest {
    @TempDir Path dir;
    @Test void freshReadOverridesStalePreferencesInOtherProcess() throws Exception {
        File a=dir.resolve("flag").toFile(), b=new File(a.getPath());
        assertFalse(SharedFlag.read(a, false));
        SharedFlag.write(a, true);
        assertTrue(SharedFlag.read(b, false), "device process still has false in preferences");
        SharedFlag.write(b, false);
        assertFalse(SharedFlag.read(a, true), "main process still has true in preferences");
    }
    @Test void existingChoiceMigratesOnce() throws Exception {
        File a=dir.resolve("flag").toFile();
        assertTrue(SharedFlag.read(a,true)); assertTrue(SharedFlag.read(a,false));
    }
    @Test void corruptValueFailsClosed() throws Exception {
        File a=dir.resolve("flag").toFile();
        try(FileOutputStream f=new FileOutputStream(a)){f.write(7);}
        assertFalse(SharedFlag.read(a,true));
    }
}