import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

final class AodReadStatusTest {
    @TempDir Path directory;

    @Test void twoConfirmedRejectionsBlockOnlyThisBandAndFirmware() throws Exception {
        File status = AodReadStatus.file(directory.toFile(), "test-band", "3.2.8");
        assertFalse(AodReadStatus.record(status, 1));
        assertFalse(AodReadStatus.blocksProbe(status, false));
        assertTrue(AodReadStatus.record(status, 1));
        assertTrue(AodReadStatus.blocksProbe(status, false));
        assertFalse(AodReadStatus.record(status, 1));
        assertFalse(AodReadStatus.blocksProbe(AodReadStatus.file(directory.toFile(), "test-band", "3.2.9"), false));
        assertFalse(AodReadStatus.blocksProbe(AodReadStatus.file(directory.toFile(), "other-band", "3.2.8"), false));
    }

    @Test void aSavedModeAlwaysAllowsRestorationAttempts() throws Exception {
        File status = AodReadStatus.file(directory.toFile(), "test-band", "3.2.8");
        AodReadStatus.record(status, 1);
        AodReadStatus.record(status, 1);
        assertFalse(AodReadStatus.blocksProbe(status, true));
    }

    @Test void otherErrorsAndSuccessfulReadsDoNotCountAsRejections() throws Exception {
        File status = AodReadStatus.file(directory.toFile(), "test-band", "3.2.8");
        assertFalse(AodReadStatus.record(status, -6));
        assertFalse(status.exists());
        AodReadStatus.record(status, 1);
        AodReadStatus.record(status, -6);
        assertFalse(AodReadStatus.record(status, 1));
        AodReadStatus.record(status, 0);
        assertFalse(AodReadStatus.record(status, 1));
        assertFalse(AodReadStatus.blocksProbe(status, false));
    }

    @Test void unknownFirmwareDoesNotCreateAnUnscopedBlock() throws Exception {
        File status = AodReadStatus.file(directory.toFile(), "test-band", null);
        assertNull(status);
        assertFalse(AodReadStatus.record(status, 1));
        assertFalse(AodReadStatus.blocksProbe(status, false));
    }
}
