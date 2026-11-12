package com.markus.guide;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import com.xiaomi.fitness.main.export.MainExtKt;
import com.xiaomi.fitness.main.export.MainTab;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;

/**
 * The last step of the setup guide (GuideWebViewFragment) loads a web page from the Xiaomi server; when that
 * server cannot be reached the step never ends and the guide has no way out (GuideActivity ignores Back).
 * This adds a permanent "Пропустить" button and finishes the guide by itself on a load error, a certificate
 * error or no page within 10 s. Finishing does what the guide's own last button does (DoneFragment): open the
 * main screen on the device tab and close the guide.
 *
 * TLS checking is untouched: the stock client has already cancelled the request on a certificate error; this
 * only skips the stock dialog that would offer to continue anyway, and closes the guide.
 */
public final class GuideSkip {
    private static final String TAG = "MarkusGuideSkip";
    private static final String DONE_FILE = "markus_guide_done";
    private static final String KEY_TAB = "key_tab";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private static WeakReference<Object> fragmentRef = new WeakReference<>(null);
    private static WeakReference<View> buttonRef = new WeakReference<>(null);
    private static GuideFlow flow;
    private static Runnable timeout;

    private GuideSkip() {
    }

    /** GuideWebViewFragment.initView (start): the button and the 10 s timer. */
    public static void attach(final Object fragment) {
        detachNow();
        Activity a = activityOf(fragment);
        if (a == null) {
            return;
        }
        fragmentRef = new WeakReference<>(fragment);
        flow = new GuideFlow();
        addButton(a, fragment);
        timeout = () -> handle(fragment, GuideFlow.Event.TIMEOUT, "no page within 10 s");
        MAIN.postDelayed(timeout, GuideFlow.TIMEOUT_MS);
        Log.i(TAG, "guide web step: skip button shown, timer started");
    }

    /** GuideWebViewFragment.onDestroy (start). */
    public static void detach(Object fragment) {
        if (fragment != null && fragment == fragmentRef.get()) {
            detachNow();
        }
    }

    /** GuideWebViewFragment.onLoadFinish (start): the page reached 100 %; ok = no main-frame error. */
    public static void onLoadFinish(Object fragment, boolean ok) {
        if (fragment == null || fragment != fragmentRef.get()) {
            return;
        }
        String url = urlOf(fragment);
        boolean real = ok && GuideFlow.isRealPage(url);
        handle(fragment, real ? GuideFlow.Event.LOADED : GuideFlow.Event.LOAD_FAILED,
                ok ? "no https page loaded" : "page load error");
    }

    /** b46.onReceivedError (start). */
    public static void onMainFrameError(WebView view, WebResourceRequest request) {
        if (request != null && request.isForMainFrame() && isOurs(view)) {
            handle(fragmentRef.get(), GuideFlow.Event.MAIN_FRAME_ERROR, "page network error");
        }
    }

    /** b46.onReceivedHttpError (start). */
    public static void onHttpError(WebView view, WebResourceRequest request, WebResourceResponse response) {
        if (request == null || response == null || !isOurs(view)) {
            return;
        }
        if (GuideFlow.isPageHttpFailure(request.isForMainFrame(), response.getStatusCode())) {
            handle(fragmentRef.get(), GuideFlow.Event.HTTP_ERROR, "page HTTP " + response.getStatusCode());
        }
    }

    /**
     * b46.onReceivedSslError, after super (which cancels the request). True = this is the guide page: the
     * guide is being finished, so the stock "continue anyway?" dialog is not shown.
     */
    public static boolean onSslError(WebView view) {
        if (!isOurs(view)) {
            return false;
        }
        handle(fragmentRef.get(), GuideFlow.Event.SSL_ERROR, "page certificate error");
        return true;
    }

    /** DoneFragment button (start): the guide was finished the stock way. */
    public static void markDoneFrom(Object doneFragment) {
        Activity a = activityOf(doneFragment);
        if (a != null) {
            markDone(a);
        }
    }

    /** SwitchPhoneIdReceiver.onReceive (start). True = do not open the guide again. */
    public static boolean suppressReopen(Context context, Intent intent) {
        if (context == null || intent == null) {
            return false;
        }
        String extra;
        try {
            extra = intent.getStringExtra(GuideFlow.EXTRA_SWITCH_PHONE);
        } catch (RuntimeException e) {
            return false;
        }
        boolean suppress = GuideFlow.suppressReopen(intent.getAction(), extra, guideDone(context));
        if (suppress) {
            Log.i(TAG, "guide already finished on this phone; not opened again");
        }
        return suppress;
    }

    private static void handle(Object fragment, GuideFlow.Event e, String reason) {
        if (flow == null || fragment == null || fragment != fragmentRef.get()) {
            return;
        }
        if (e == GuideFlow.Event.LOADED && timeout != null) {
            MAIN.removeCallbacks(timeout);
            Log.i(TAG, "guide page loaded");
        }
        if (flow.on(e)) {
            finishGuide(fragment, reason);
        }
    }

    /** Same as the guide's own last button: main screen on the device tab, guide closed. */
    private static void finishGuide(Object fragment, String reason) {
        final Activity a = activityOf(fragment);
        detachNow();
        if (a == null || a.isFinishing()) {
            return;
        }
        Log.i(TAG, "guide finished: " + reason);
        markDone(a);
        MAIN.post(() -> {
            try {
                Bundle b = new Bundle();
                b.putInt(KEY_TAB, MainTab.getINDEX_DEVICE());
                MainExtKt.showMainActivity(a, null, b);
            } catch (RuntimeException ex) {
                Log.w(TAG, "main screen not opened", ex);
            } finally {
                a.finish();
            }
        });
    }

    private static void detachNow() {
        if (timeout != null) {
            MAIN.removeCallbacks(timeout);
            timeout = null;
        }
        View b = buttonRef.get();
        if (b != null && b.getParent() instanceof ViewGroup) {
            ((ViewGroup) b.getParent()).removeView(b);
        }
        buttonRef = new WeakReference<>(null);
        fragmentRef = new WeakReference<>(null);
        flow = null;
    }

    private static void addButton(Activity a, final Object fragment) {
        Button b = new Button(a);
        b.setText("Пропустить");
        b.setAllCaps(false);
        b.setContentDescription("Пропустить последний шаг настройки");
        b.setOnClickListener(v -> handle(fragment, GuideFlow.Event.SKIP, "skip button"));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.END);
        // below the status bar (the guide draws edge to edge), away from the page's own buttons at the bottom
        lp.topMargin = dp(a, 44);
        lp.rightMargin = dp(a, 12);
        b.setElevation(dp(a, 8));
        a.addContentView(b, lp);
        buttonRef = new WeakReference<>(b);
    }

    private static int dp(Context c, int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                c.getResources().getDisplayMetrics()));
    }

    private static boolean isOurs(WebView view) {
        Object f = fragmentRef.get();
        if (view == null || f == null || flow == null) {
            return false;
        }
        Object w = call(f, "getWebView");
        if (w != null) {
            return w == view;
        }
        Activity a = activityOf(f);
        Context c = view.getContext();
        while (c instanceof ContextWrapper && !(c instanceof Activity)) {
            c = ((ContextWrapper) c).getBaseContext();
        }
        return a != null && c == a;
    }

    private static String urlOf(Object fragment) {
        Object w = call(fragment, "getWebView");
        return w instanceof WebView ? ((WebView) w).getUrl() : null;
    }

    private static Activity activityOf(Object fragment) {
        Object a = call(fragment, "getActivity");
        return a instanceof Activity ? (Activity) a : null;
    }

    private static Object call(Object target, String method) {
        if (target == null) {
            return null;
        }
        try {
            return target.getClass().getMethod(method).invoke(target);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static void markDone(Context c) {
        try (FileOutputStream out = new FileOutputStream(new File(c.getFilesDir(), DONE_FILE))) {
            out.write(Long.toString(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.w(TAG, "guide mark not written", e);
        }
    }

    private static boolean guideDone(Context c) {
        return new File(c.getFilesDir(), DONE_FILE).isFile();
    }
}
