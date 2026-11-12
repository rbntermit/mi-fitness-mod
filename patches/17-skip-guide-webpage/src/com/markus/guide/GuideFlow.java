package com.markus.guide;

/**
 * When the last step of the setup guide (the web page) ends by itself. Pure logic, no Android.
 *
 * The page only waits for its own content: a load error, a certificate error or no loaded page within
 * TIMEOUT_MS finishes the guide. Once the page has loaded, the person reads it and leaves through the page
 * itself or through the permanent "Пропустить" button; a later main-frame error still finishes the guide.
 */
final class GuideFlow {
    static final long TIMEOUT_MS = 10_000;
    static final String ACTION_SWITCH_PHONE = "com.xiaomi.fitness.device.SWITCH_PHONE_ID";
    static final String EXTRA_SWITCH_PHONE = "switch_phone";
    static final String OOBE = "OOBE";

    enum Event { LOADED, LOAD_FAILED, MAIN_FRAME_ERROR, HTTP_ERROR, SSL_ERROR, TIMEOUT, SKIP }

    private boolean loaded;
    private boolean finished;

    /** True exactly once: the moment the guide has to be finished. */
    boolean on(Event e) {
        if (finished) {
            return false;
        }
        if (e == Event.LOADED) {
            loaded = true;
            return false;
        }
        if (e == Event.TIMEOUT && loaded) {
            return false;
        }
        finished = true;
        return true;
    }

    boolean isFinished() {
        return finished;
    }

    /**
     * GuideWebViewFragment blanks its URL when the host is not the expected one and refuses plain http,
     * so "loaded" counts only for a real https page.
     */
    static boolean isRealPage(String url) {
        return url != null && url.regionMatches(true, 0, "https://", 0, 8) && url.length() > 8;
    }

    /** A sub-resource (image, script) failing does not break the page; the page itself failing does. */
    static boolean isPageHttpFailure(boolean mainFrame, int status) {
        return mainFrame && status >= 400;
    }

    /**
     * SwitchPhoneIdReceiver opens the guide again when a bound band reports another phone id. After the guide
     * has been finished once on this phone (normally or skipped), that broadcast no longer reopens it.
     * The guide right after binding a band (BindDeviceActivity) is not affected.
     */
    static boolean suppressReopen(String action, String extra, boolean guideDone) {
        return guideDone && ACTION_SWITCH_PHONE.equals(action) && OOBE.equals(extra);
    }
}
