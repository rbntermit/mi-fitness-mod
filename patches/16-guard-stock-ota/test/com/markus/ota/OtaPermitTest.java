package com.markus.ota;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OtaPermitTest {
    private static final String OURS = "/data/user/0/com.xiaomi.wearable/files/markus_fw/abc.bin";
    private static final String STOCK =
            "/storage/emulated/0/Android/data/com.xiaomi.wearable/files/Download/q66tc/0123456789abcdef.bin";
    private static final long T0 = 1_000_000L;

    // --- without a permit nothing reaches the band

    @Test
    void noPermitRefusesPrepareStartAndFirmwareFile() {
        assertFalse(OtaPermit.prepareAllowed(null, T0));
        assertFalse(OtaPermit.startAllowed(null, OURS, T0));
        assertFalse(OtaPermit.startAllowed(null, STOCK, T0));
        assertFalse(OtaPermit.fileAllowed(OtaPermit.FILE_TYPE_FIRMWARE, null, STOCK, T0));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 3, 4, 5, 6, -1, 99})
    void otherTransfersAreNeverTouched(int type) {
        // sync data, watch faces, resources, apps, fall detection, tracks...
        assertTrue(OtaPermit.fileAllowed(type, null, STOCK, T0));
    }

    @Test
    void onlyTypeTwoIsFirmware() {
        assertTrue(OtaPermit.isFirmwareTransfer(2));
        assertFalse(OtaPermit.isFirmwareTransfer(1));
        assertFalse(OtaPermit.isFirmwareTransfer(4));
    }

    // --- the firmware screen's permit

    @Test
    void permitAllowsExactlyItsFile() {
        OtaPermit p = OtaPermit.grant(OURS, T0);
        assertNotNull(p);
        assertTrue(OtaPermit.prepareAllowed(p, T0 + 1));
        assertTrue(OtaPermit.startAllowed(p, OURS, T0 + 1));
        assertTrue(OtaPermit.fileAllowed(2, p, OURS, T0 + 1));
    }

    @Test
    void permitDoesNotCoverTheStockDownload() {
        OtaPermit p = OtaPermit.grant(OURS, T0);
        assertFalse(OtaPermit.startAllowed(p, STOCK, T0 + 1));
        assertFalse(OtaPermit.fileAllowed(2, p, STOCK, T0 + 1));
        assertFalse(OtaPermit.startAllowed(p, null, T0 + 1));
        assertFalse(OtaPermit.startAllowed(p, "", T0 + 1));
        assertFalse(OtaPermit.startAllowed(p, OURS + ".bak", T0 + 1));
    }

    @Test
    void permitExpires() {
        OtaPermit p = OtaPermit.grant(OURS, T0);
        long end = T0 + OtaPermit.TTL_MS;
        assertTrue(OtaPermit.startAllowed(p, OURS, end - 1));
        assertFalse(OtaPermit.startAllowed(p, OURS, end));
        assertFalse(OtaPermit.prepareAllowed(p, end));
        assertFalse(OtaPermit.fileAllowed(2, p, OURS, end + 60_000));
    }

    @Test
    void ttlIsShort() {
        assertTrue(OtaPermit.TTL_MS <= 5 * 60_000);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "a\nb"})
    void unusablePathGivesNoPermit(String path) {
        assertNull(OtaPermit.grant(path, T0));
    }

    @Test
    void nullPathGivesNoPermit() {
        assertNull(OtaPermit.grant(null, T0));
    }

    @Test
    void sameFileSpelledLikeFileGetAbsolutePathMatches() {
        // GeneralOtaExecutor sends File(path).getAbsolutePath(); a doubled slash must not break our own install
        OtaPermit p = OtaPermit.grant(OURS, T0);
        assertTrue(OtaPermit.startAllowed(p, OURS.replace("/files/", "/files//"), T0 + 1));
        assertTrue(OtaPermit.startAllowed(p, " " + OURS + " ", T0 + 1));
    }

    // --- the copy for the ":device" process

    @Test
    void fileFormatRoundTrips() {
        OtaPermit p = OtaPermit.grant(OURS, T0);
        OtaPermit q = OtaPermit.decode(p.encode());
        assertNotNull(q);
        assertEquals(p.path, q.path);
        assertEquals(p.expiresAt, q.expiresAt);
        assertTrue(OtaPermit.startAllowed(q, OURS, T0 + 1));
        assertFalse(OtaPermit.startAllowed(q, STOCK, T0 + 1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "\n", "abc\n/x", "123", "123\n", "\n/x"})
    void brokenFileGivesNoPermit(String s) {
        assertNull(OtaPermit.decode(s));
    }

    @Test
    void missingFileGivesNoPermit() {
        assertNull(OtaPermit.decode(null));
    }

    @Test
    void expiredFileGivesNothing() {
        OtaPermit q = OtaPermit.decode((T0 - 1) + "\n" + OURS);
        assertNotNull(q);
        assertFalse(OtaPermit.prepareAllowed(q, T0));
        assertFalse(OtaPermit.fileAllowed(2, q, OURS, T0));
    }

    // --- a stock update the policy allowed

    @Test
    void stockPermitCoversTheStockFileForAShortTime() {
        OtaPermit p = OtaPermit.stock(T0);
        assertTrue(p.isStock());
        assertTrue(OtaPermit.prepareAllowed(p, T0 + 1));
        assertTrue(OtaPermit.startAllowed(p, STOCK, T0 + 1));
        assertTrue(OtaPermit.fileAllowed(2, p, STOCK, T0 + 1));
        assertFalse(OtaPermit.fileAllowed(2, p, STOCK, T0 + OtaPermit.TTL_MS));
        assertFalse(OtaPermit.startAllowed(p, null, T0 + 1));
    }

    @Test
    void stockPermitSurvivesTheFileForTheDeviceProcess() {
        OtaPermit q = OtaPermit.decode(OtaPermit.stock(T0).encode());
        assertNotNull(q);
        assertTrue(q.isStock());
        assertTrue(OtaPermit.fileAllowed(2, q, STOCK, T0 + 1));
    }

    @Test
    void screenPermitIsNotAStockPermit() {
        assertFalse(OtaPermit.grant(OURS, T0).isStock());
    }

    // --- the notice

    @Test
    void noticeIsShownOncePerBurst() {
        assertTrue(OtaPermit.toastDue(0, T0));
        assertFalse(OtaPermit.toastDue(T0, T0 + 10));
        assertFalse(OtaPermit.toastDue(T0, T0 + OtaPermit.TOAST_GAP_MS - 1));
        assertTrue(OtaPermit.toastDue(T0, T0 + OtaPermit.TOAST_GAP_MS));
        assertTrue(OtaPermit.toastDue(T0, T0 - 5)); // clock moved back
    }

    @Test
    void noticeText() {
        assertEquals("Штатное обновление отключено, используйте экран «Прошивки»", OtaGuard.MESSAGE);
    }
}
