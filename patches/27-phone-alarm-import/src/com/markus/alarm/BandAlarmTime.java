package com.markus.alarm;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

final class BandAlarmTime {
    private BandAlarmTime() {}

    /** A one-time band alarm stores the next occurrence of HH:mm, without a calendar date. */
    static LocalTime importableTime(Instant now, Instant trigger, ZoneId zone) {
        if (!trigger.isAfter(now)) return null;
        LocalDateTime localNow = LocalDateTime.ofInstant(now, zone);
        LocalDateTime localTrigger = LocalDateTime.ofInstant(trigger, zone).truncatedTo(ChronoUnit.MINUTES);
        LocalTime time = localTrigger.toLocalTime();
        if (!localTrigger.equals(nextOccurrence(localNow, time)) || zone.getRules().getValidOffsets(localTrigger).size() != 1) {
            return null;
        }
        return time;
    }

    static LocalDateTime nextOccurrence(LocalDateTime now, LocalTime time) {
        LocalDateTime today = now.toLocalDate().atTime(time);
        return today.isAfter(now) ? today : today.plusDays(1);
    }
}
