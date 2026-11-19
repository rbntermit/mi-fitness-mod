import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class AodSettingTest {
    private static final AodSetting SCHEDULE = AodSetting.of(AodSetting.SCHEDULED, 22 * 60 + 17, 6 * 60 + 5);
    private static final AodSetting SMART = AodSetting.of(AodSetting.SMART, -1, -1);
    private static final AodSetting OFF = AodSetting.of(AodSetting.OFF, -1, -1);

    @Test void allowsOnlyGlobalSmartBand10() {
        assertTrue(AodSetting.supported("miwear.watch.o66gl"));
        assertFalse(AodSetting.supported("miwear.watch.o66cn"));
        assertFalse(AodSetting.supported("miwear.watch.q66gl"));
        assertFalse(AodSetting.supported(null));
    }

    @Test void alreadyOffStaysOffDuringAndAfterDnd() {
        assertEquals(AodSetting.Action.NONE, AodSetting.decide(true, OFF, null));
        assertEquals(AodSetting.Action.NONE, AodSetting.decide(false, OFF, null));
    }

    @Test void restoresSchedulesAndSmartModeAfterPause() {
        for (AodSetting original : new AodSetting[]{SCHEDULE, SMART}) {
            assertEquals(AodSetting.Action.PAUSE, AodSetting.decide(true, original, null));
            assertEquals(AodSetting.Action.NONE, AodSetting.decide(true, OFF, original));
            assertEquals(AodSetting.Action.RESTORE, AodSetting.decide(false, OFF, original));
        }
        AodSetting restored = AodSetting.decode(SCHEDULE.encode());
        assertTrue(SCHEDULE.matches(restored));
        assertEquals(22 * 60 + 17, restored.start);
        assertEquals(6 * 60 + 5, restored.end);
        assertTrue(SMART.matches(AodSetting.decode(SMART.encode())));
    }

    @Test void manualActiveModeWinsWhenLeavingDnd() {
        assertEquals(AodSetting.Action.FORGET, AodSetting.decide(false, SMART, SCHEDULE));
    }

    @Test void missingOrInvalidReadLeavesSavedModeAlone() {
        assertEquals(AodSetting.Action.NONE, AodSetting.decide(true, null, null));
        assertEquals(AodSetting.Action.NONE, AodSetting.decide(false, null, SCHEDULE));
        assertNull(AodSetting.of(0, 0, 0));
        assertNull(AodSetting.of(3, -1, 0));
        assertNull(AodSetting.of(3, 0, 1440));
        assertNull(AodSetting.decode("3:bad:7"));
        assertNull(AodSetting.decode("3:6"));
        assertNull(AodSetting.decode(OFF.encode()));
    }

    @Test void readbackRequiresOriginalHoursAndMode() {
        assertFalse(SCHEDULE.matches(AodSetting.of(AodSetting.SCHEDULED, SCHEDULE.end, SCHEDULE.start)));
        assertFalse(SCHEDULE.matches(SMART));
        assertTrue(SCHEDULE.off().matches(OFF));
    }

    @Test void packetUsesStockAodCommandsAndRetainsOvernightTimes() {
        t3u read = AodRequest.read();
        assertEquals(2, read.c);
        assertEquals(0x41, read.d);
        assertNull(read.u());
        t3u write = AodRequest.write(SCHEDULE);
        assertEquals(2, write.c);
        assertEquals(0x42, write.d);
        xir payload = write.u().B();
        assertEquals(3, payload.a);
        assertEquals(22, payload.b.a);
        assertEquals(17, payload.b.b);
        assertEquals(6, payload.c.a);
        assertEquals(5, payload.c.b);
        assertTrue(SCHEDULE.matches(AodRequest.decode(payload)));
        assertEquals(1, AodRequest.write(SCHEDULE.off()).u().B().a);
    }

    @Test void invalidProtocolTimeAndMissingPacketAreRejected() {
        assertNull(AodRequest.decode(null));
        xir timing = AodRequest.write(SCHEDULE).u().B();
        timing.c.b = 60;
        assertNull(AodRequest.decode(timing));
    }
}
