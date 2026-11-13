package com.markus.logexport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class LogHeaderTest {
    @Test
    void headerNamesModBandPhoneAndStartsWithBom() {
        String h = LogHeader.build("3.60.0i, сборка мода 4", "miwear.watch.o66gl, прошивка 3.2.8",
                "OnePlus CPH2581, Android 16 (API 36)", "запись с 11-13 12:00:00.000", 3);
        assertTrue(h.startsWith("﻿# "), h);
        assertTrue(h.contains("сборка мода 4"));
        assertTrue(h.contains("miwear.watch.o66gl, прошивка 3.2.8"));
        assertTrue(h.contains("OnePlus CPH2581, Android 16 (API 36)"));
        assertTrue(h.contains("процессов приложения: 3"));
        assertTrue(h.contains("тегом MOD"));
        byte[] utf8 = h.getBytes(StandardCharsets.UTF_8);
        assertTrue((utf8[0] & 0xff) == 0xEF && (utf8[1] & 0xff) == 0xBB && (utf8[2] & 0xff) == 0xBF);
    }

    @Test
    void noBandIsSaidPlainly() {
        assertTrue(LogHeader.build("v", null, "p", "t", 1).contains("Браслет: не привязан"));
    }

    @Test
    void markersAreAscii() {
        for (String m : new String[] {LogMasker.HIDDEN, LogMasker.MAC, LogMasker.EMAIL, LogMasker.PHONE}) {
            assertTrue(m.chars().allMatch(c -> c < 128), m);
        }
        assertFalse(LogMasker.mask("token=abc").contains("скрыто"));
    }
}
