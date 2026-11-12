package com.markus.guide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class GuideFlowTest {
    // --- the page never comes: the guide ends by itself

    @ParameterizedTest
    @EnumSource(value = GuideFlow.Event.class, names = {"LOAD_FAILED", "MAIN_FRAME_ERROR", "HTTP_ERROR", "SSL_ERROR",
            "TIMEOUT", "SKIP"})
    void anyFailureOrSkipBeforeLoadFinishes(GuideFlow.Event e) {
        GuideFlow f = new GuideFlow();
        assertTrue(f.on(e));
        assertTrue(f.isFinished());
    }

    @Test
    void timeoutIsTenSeconds() {
        assertEquals(10_000, GuideFlow.TIMEOUT_MS);
    }

    // --- the page came: the person reads it

    @Test
    void timeoutAfterLoadDoesNothing() {
        GuideFlow f = new GuideFlow();
        assertFalse(f.on(GuideFlow.Event.LOADED));
        assertFalse(f.on(GuideFlow.Event.TIMEOUT));
        assertFalse(f.isFinished());
    }

    @Test
    void skipStillWorksAfterLoad() {
        GuideFlow f = new GuideFlow();
        f.on(GuideFlow.Event.LOADED);
        assertTrue(f.on(GuideFlow.Event.SKIP));
    }

    @ParameterizedTest
    @EnumSource(value = GuideFlow.Event.class, names = {"LOAD_FAILED", "MAIN_FRAME_ERROR", "HTTP_ERROR", "SSL_ERROR"})
    void laterPageFailureStillFinishes(GuideFlow.Event e) {
        GuideFlow f = new GuideFlow();
        f.on(GuideFlow.Event.LOADED);
        assertTrue(f.on(e));
    }

    // --- finishing happens once (two errors in a row must not open the main screen twice)

    @Test
    void finishesOnlyOnce() {
        GuideFlow f = new GuideFlow();
        assertTrue(f.on(GuideFlow.Event.SSL_ERROR));
        assertFalse(f.on(GuideFlow.Event.MAIN_FRAME_ERROR));
        assertFalse(f.on(GuideFlow.Event.TIMEOUT));
        assertFalse(f.on(GuideFlow.Event.SKIP));
        assertFalse(f.on(GuideFlow.Event.LOADED));
    }

    // --- what counts as a loaded page

    @ParameterizedTest
    @ValueSource(strings = {"https://watch.iot.mi.com/x", "HTTPS://a.b/"})
    void httpsPageIsReal(String url) {
        assertTrue(GuideFlow.isRealPage(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "about:blank", "http://watch.iot.mi.com/x", "https://", "file:///android_asset/x",
            "data:text/html,"})
    void blankOrOtherPageIsNotReal(String url) {
        assertFalse(GuideFlow.isRealPage(url));
    }

    @Test
    void nullPageIsNotReal() {
        assertFalse(GuideFlow.isRealPage(null));
    }

    @Test
    void onlyMainFrameHttpErrorsCount() {
        assertTrue(GuideFlow.isPageHttpFailure(true, 404));
        assertTrue(GuideFlow.isPageHttpFailure(true, 500));
        assertTrue(GuideFlow.isPageHttpFailure(true, 403));
        assertFalse(GuideFlow.isPageHttpFailure(true, 200));
        assertFalse(GuideFlow.isPageHttpFailure(true, 304));
        assertFalse(GuideFlow.isPageHttpFailure(false, 404)); // a missing image does not end the guide
    }

    // --- reopening after restart

    @Test
    void finishedGuideIsNotReopenedByPhoneIdBroadcast() {
        assertTrue(GuideFlow.suppressReopen("com.xiaomi.fitness.device.SWITCH_PHONE_ID", "OOBE", true));
    }

    @Test
    void firstTimeGuideStillOpens() {
        assertFalse(GuideFlow.suppressReopen("com.xiaomi.fitness.device.SWITCH_PHONE_ID", "OOBE", false));
    }

    @Test
    void otherBroadcastsAreUntouched() {
        assertFalse(GuideFlow.suppressReopen("com.xiaomi.fitness.device.SWITCH_PHONE_ID", "OTHER", true));
        assertFalse(GuideFlow.suppressReopen("com.xiaomi.fitness.device.SWITCH_PHONE_ID", null, true));
        assertFalse(GuideFlow.suppressReopen("android.intent.action.BOOT_COMPLETED", "OOBE", true));
        assertFalse(GuideFlow.suppressReopen(null, "OOBE", true));
    }
}
