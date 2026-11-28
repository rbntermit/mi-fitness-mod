package com.markus.alarm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class PhoneAlarmImportTest {
    private static final ZoneId MOSCOW = ZoneId.of("Europe/Moscow");
    private static final Instant NOW = Instant.parse("2026-11-28T17:30:10Z");

    @Test void usesLocalTimeForTheSameDay() {
        assertEquals(LocalTime.of(21, 15), BandAlarmTime.importableTime(NOW,
                Instant.parse("2026-11-28T18:15:00Z"), MOSCOW));
    }

    @Test void supportsTheNextMorning() {
        assertEquals(LocalTime.of(7, 0), BandAlarmTime.importableTime(NOW,
                Instant.parse("2026-11-29T04:00:00Z"), MOSCOW));
    }

    @Test void rejectsTomorrowWhenTheSameHourWouldRingToday() {
        assertNull(BandAlarmTime.importableTime(NOW, Instant.parse("2026-11-29T18:15:00Z"), MOSCOW));
    }

    @Test void showsTheBandsActualNextDayForATimeOnlyCopy() {
        LocalDateTime localNow = LocalDateTime.ofInstant(NOW, MOSCOW);
        LocalTime time = LocalTime.of(7, 0);
        assertNull(BandAlarmTime.importableTime(NOW, Instant.parse("2026-11-30T04:00:00Z"), MOSCOW));
        assertEquals(LocalDateTime.of(2026, 11, 29, 7, 0), BandAlarmTime.nextOccurrence(localNow, time));
    }

    @Test void rejectsPastAndSameMinuteTriggers() {
        assertNull(BandAlarmTime.importableTime(NOW, Instant.parse("2026-11-28T17:30:00Z"), MOSCOW));
        assertNull(BandAlarmTime.importableTime(NOW, Instant.parse("2026-11-28T17:30:50Z"), MOSCOW));
    }

    @Test void rejectsAmbiguousTimeWhenClocksGoBack() {
        assertNull(BandAlarmTime.importableTime(Instant.parse("2026-10-25T00:00:00Z"),
                Instant.parse("2026-10-25T01:30:00Z"), ZoneId.of("Europe/Berlin")));
    }
}
