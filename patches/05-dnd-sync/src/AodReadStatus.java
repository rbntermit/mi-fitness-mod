import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Persists confirmed read rejections for one band and firmware. Only the device worker writes. */
final class AodReadStatus {
    static final int READ_REJECTED = 1;
    private static final int LIMIT = 2;

    private AodReadStatus() {}

    static File file(File directory, String identity, String firmware) {
        if (identity == null || identity.isEmpty() || firmware == null || firmware.trim().isEmpty()) return null;
        UUID key = UUID.nameUUIDFromBytes((identity + "\n" + firmware.trim()).getBytes(StandardCharsets.UTF_8));
        return new File(directory, "mod-aod-read-" + key + ".flag");
    }

    static boolean blocksProbe(File file, boolean hasSnapshot) throws IOException {
        if (hasSnapshot || file == null) return false;
        try (RandomAccessFile input = new RandomAccessFile(file, "r")) {
            return input.read() == LIMIT;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    static boolean record(File file, int result) throws IOException {
        if (file == null || (result != READ_REJECTED && !file.isFile())) return false;
        try (RandomAccessFile state = new RandomAccessFile(file, "rw")) {
            int previous = state.read();
            int next = result == READ_REJECTED ? Math.min(LIMIT, Math.max(0, previous) + 1) : 0;
            if (next != previous) {
                state.seek(0);
                state.write(next);
                state.getFD().sync();
            }
            return next == LIMIT && previous != LIMIT;
        }
    }
}
