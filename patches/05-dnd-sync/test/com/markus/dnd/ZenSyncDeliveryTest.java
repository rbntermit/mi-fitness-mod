package com.markus.dnd;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ZenSyncDeliveryTest {
    @Test void enqueueDoesNotMeanAcknowledged() {
        ZenSyncDelivery state = new ZenSyncDelivery();
        state.selectDevice("band-a");
        long ticket = state.begin(true);
        assertNull(state.acknowledged());
        assertTrue(state.isPending(true));
        assertTrue(state.complete(ticket, true));
        assertEquals(Boolean.TRUE, state.acknowledged());
        assertFalse(state.isPending(true));
    }

    @Test void failedRequestCanBeRetried() {
        ZenSyncDelivery state = new ZenSyncDelivery();
        long ticket = state.begin(true);
        assertTrue(state.complete(ticket, false));
        assertNull(state.acknowledged());
        assertFalse(state.isPending(true));
    }

    @Test void oppositePendingStateInvalidatesPreviousAcknowledgement() {
        ZenSyncDelivery state = new ZenSyncDelivery();
        state.complete(state.begin(false), true);
        state.begin(true);
        assertNull(state.acknowledged());
        assertFalse(state.isPending(false));
    }

    @Test void oldCallbackCannotOverwriteNewerPhoneState() {
        ZenSyncDelivery state = new ZenSyncDelivery();
        long old = state.begin(true);
        long latest = state.begin(false);
        assertFalse(state.complete(old, true));
        assertTrue(state.isPending(false));
        assertTrue(state.complete(latest, true));
        assertEquals(Boolean.FALSE, state.acknowledged());
    }

    @Test void changingBandInvalidatesCallback() {
        ZenSyncDelivery state = new ZenSyncDelivery();
        state.selectDevice("band-a");
        long ticket = state.begin(true);
        state.selectDevice("band-b");
        assertFalse(state.complete(ticket, true));
        assertNull(state.acknowledged());
    }

    @Test void disablingSwitchInvalidatesCallback() {
        ZenSyncDelivery state = new ZenSyncDelivery();
        long ticket = state.begin(true);
        state.reset();
        assertFalse(state.complete(ticket, true));
        assertNull(state.acknowledged());
    }

    @Test void repeatedCallbackIsIgnored() {
        ZenSyncDelivery state = new ZenSyncDelivery();
        long ticket = state.begin(true);
        assertTrue(state.complete(ticket, true));
        assertFalse(state.complete(ticket, false));
        assertEquals(Boolean.TRUE, state.acknowledged());
    }

    @Test void currentBandSelectionPreservesAcknowledgement() {
        ZenSyncDelivery state = new ZenSyncDelivery();
        state.selectDevice("band-a");
        state.complete(state.begin(true), true);
        state.selectDevice("band-a");
        assertEquals(Boolean.TRUE, state.acknowledged());
    }
}
