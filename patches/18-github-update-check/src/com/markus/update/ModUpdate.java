package com.markus.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

/**
 * Checks the mod's GitHub releases for a newer build. One GET of UpdateCheck.API_URL without a token,
 * cookies or anything about the phone or the account; at most once a day while "Проверять обновления" is on,
 * or by hand from Устройство -> Ещё -> Обновление мода. A newer build is only announced: "Скачать" opens the
 * release page in the browser; nothing is downloaded or installed by the app.
 */
public final class ModUpdate {
    private static final String TAG = "MarkusModUpdate";
    private static final String PREFS = "markus_mod_update";
    private static final String KEY_ENABLED = "check_enabled";
    private static final String KEY_LAST = "last_check";
    /** Written by build.sh (BAND_BUILD) into res/values/strings.xml. */
    private static final String RES_BUILD = "markus_mod_build";
    private static final int TIMEOUT_MS = 10_000;
    private static final int MAX_BYTES = 256 * 1024;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static volatile boolean running;

    interface Callback {
        void onResult(UpdateCheck.Result r);
    }

    private ModUpdate() {
    }

    /** MainActivity.onResume (start): the daily automatic check. Never throws. */
    public static void onMainResume(Activity activity) {
        try {
            SharedPreferences p = prefs(activity);
            long now = System.currentTimeMillis();
            if (running || !UpdateCheck.due(isEnabled(activity), p.getLong(KEY_LAST, 0), now)) {
                return;
            }
            final WeakReference<Activity> ref = new WeakReference<>(activity);
            check(activity, r -> {
                Activity a = ref.get();
                if (r.state == UpdateCheck.State.NEWER && a != null && !a.isFinishing() && !a.isDestroyed()) {
                    showNewer(a, r);
                }
            });
        } catch (RuntimeException e) {
            Log.w(TAG, "automatic check skipped", e);
        }
    }

    static boolean isEnabled(Context c) {
        return prefs(c).getBoolean(KEY_ENABLED, true);
    }

    static void setEnabled(Context c, boolean on) {
        prefs(c).edit().putBoolean(KEY_ENABLED, on).apply();
    }

    /** The installed build, or null when the build number is missing from the resources. */
    static ModVersion installed(Context c) {
        try {
            String name = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
            int id = c.getResources().getIdentifier(RES_BUILD, "string", c.getPackageName());
            int band = id == 0 ? -1 : Integer.parseInt(c.getString(id).trim());
            return ModVersion.local(name, band);
        } catch (Exception e) {
            return null;
        }
    }

    /** Runs the request off the main thread; the callback comes on the main thread. Failures give FAILED. */
    static void check(Context context, final Callback cb) {
        final Context app = context.getApplicationContext();
        final ModVersion installed = installed(app);
        running = true;
        new Thread(() -> {
            UpdateCheck.Result r;
            try {
                r = installed == null ? UpdateCheck.Result.of(UpdateCheck.State.FAILED) : fetch(installed);
            } catch (Throwable t) {
                r = UpdateCheck.Result.of(UpdateCheck.State.FAILED);
            }
            // every attempt counts, also a failed one: no more than one request a day even offline or rate-limited
            prefs(app).edit().putLong(KEY_LAST, System.currentTimeMillis()).apply();
            Log.i(TAG, "check: " + r.state);
            final UpdateCheck.Result res = r;
            MAIN.post(() -> {
                running = false;
                try {
                    cb.onResult(res);
                } catch (RuntimeException e) {
                    Log.w(TAG, "result not shown", e);
                }
            });
        }, "markus-mod-update").start();
    }

    private static UpdateCheck.Result fetch(ModVersion installed) {
        HttpsURLConnection c = null;
        try {
            c = (HttpsURLConnection) new URL(UpdateCheck.API_URL).openConnection();
            c.setConnectTimeout(TIMEOUT_MS);
            c.setReadTimeout(TIMEOUT_MS);
            c.setUseCaches(false);
            c.setInstanceFollowRedirects(false);
            c.setRequestProperty("Accept", "application/vnd.github+json");
            c.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
            // GitHub refuses requests without a User-Agent; this one names the mod only
            c.setRequestProperty("User-Agent", "mi-fitness-mod-update-check");
            int code = c.getResponseCode();
            String body = code == HttpURLConnection.HTTP_OK ? read(c.getInputStream()) : null;
            return UpdateCheck.classify(code, body, installed);
        } catch (IOException | RuntimeException e) {
            return UpdateCheck.classify(-1, null, installed);
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
    }

    private static String read(InputStream in) throws IOException {
        try (InputStream is = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                if (out.size() + n > MAX_BYTES) {
                    throw new IOException("answer too large");
                }
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    /** "Доступно обновление мода": version, release text, "Скачать" (browser) and "Позже". */
    static void showNewer(final Activity a, final UpdateCheck.Result r) {
        StringBuilder m = new StringBuilder("Новая сборка: ").append(r.version.display());
        ModVersion cur = installed(a);
        if (cur != null) {
            m.append("\nУстановлена: ").append(cur.display());
        }
        if (r.body != null && !r.body.isEmpty()) {
            m.append("\n\n").append(r.body);
        }
        new AlertDialog.Builder(a)
                .setTitle("Доступно обновление мода")
                .setMessage(m)
                .setPositiveButton("Скачать", (d, w) -> openPage(a, r.pageUrl))
                .setNegativeButton("Позже", null)
                .show();
    }

    static void openPage(Context c, String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addCategory(Intent.CATEGORY_BROWSABLE);
            if (!(c instanceof Activity)) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            c.startActivity(i);
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.w(TAG, "no browser", e);
        }
    }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
