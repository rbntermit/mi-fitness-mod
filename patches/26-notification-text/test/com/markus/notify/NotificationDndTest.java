package com.markus.notify;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationDndTest {
    @Test void pausesInAllActiveDndModes() {
        assertTrue(NotificationText.isDndActive(2));
        assertTrue(NotificationText.isDndActive(3));
        assertTrue(NotificationText.isDndActive(4));
        assertFalse(NotificationText.isDndActive(1));
        assertFalse(NotificationText.isDndActive(0));
        assertFalse(NotificationText.isDndActive(5));
    }

    @Test void preservesCallsAndAlarms() {
        assertTrue(NotificationText.keepDuringDnd("call", false));
        assertTrue(NotificationText.keepDuringDnd("alarm", false));
        assertTrue(NotificationText.keepDuringDnd("stopwatch", false));
        assertTrue(NotificationText.keepDuringDnd(null, true));
    }

    @Test void ordinaryAndUncategorizedNotificationsCanBePaused() {
        assertFalse(NotificationText.keepDuringDnd("msg", false));
        assertFalse(NotificationText.keepDuringDnd("social", false));
        assertFalse(NotificationText.keepDuringDnd(null, false));
    }
}
