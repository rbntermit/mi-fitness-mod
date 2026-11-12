package com.markus.ota;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.xiaomi.fitness.common.extensions.ApplicationExtKt;
import com.xiaomi.fitness.device.manager.DeviceManager;
import com.xiaomi.fitness.device.manager.DeviceModel;
import com.xiaomi.fitness.device.manager.bean.DeviceInfo;
import com.xiaomi.fitness.device.manager.export.DeviceManagerExtKt;
import com.xiaomi.fitness.device.manager.export.WearableDeviceManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * Guards the stock firmware update. It works as in the original app unless StockOtaPolicy blocks the offer
 * (reflashed band, offer of another branch, not newer, or anything not readable); then offers, badges, dialogs,
 * upgrade screens, silent upgrade and the transfer to the band are refused with MESSAGE.
 *
 * Two ways through the transfer gates (prepareOta, startOta, sendFile of type 2):
 *  - the firmware screen of patch 12: grant(path) right before it sends a package that passed all its checks;
 *  - a stock update the policy allowed: prepareOta then issues a short "stock" permit for the stock file.
 * Permits are also written to a file, because sendFile reaches the band through the ":device" process.
 */
public final class OtaGuard {
    public static final String MESSAGE = "Штатное обновление отключено, используйте экран «Прошивки»";
    private static final String TAG = "MarkusOtaGuard";
    private static final String PERMIT_FILE = "markus_ota_permit";
    private static final String[] OFFER_KEYS = {"key_latest_version", "key_ota_upgrade_result"};

    private static OtaPermit permit;
    private static long lastToast;
    /** Version of the last stock offer seen (from the server answer); the transfer gates re-check it. */
    private static volatile String lastOffer;
    private static volatile String lastLogged;
    /** Version the band reported to the stock check (readWatchInfo), see noteBandVersion. */
    private static volatile String bandVersion;

    private OtaGuard() {
    }

    /** UpdateHelper.requestLatestVersion callbacks (start): the version the band itself has just reported. */
    public static void noteBandVersion(String version) {
        if (StockOtaPolicy.clean(version) != null) {
            bandVersion = version.trim();
        }
    }

    // ---- the policy on the current band

    /** The single decision, for the band that is bound now and the offered version. */
    static StockOtaPolicy.Decision decide(String offeredVersion) {
        String model = null;
        String version = null;
        try {
            DeviceManager dm = DeviceManagerExtKt.getInstance(WearableDeviceManager.Companion);
            DeviceModel m = dm == null ? null : dm.getCurrentDeviceModel();
            DeviceInfo info = m == null ? null : m.getDeviceInfo();
            if (info != null) {
                model = info.getModel();
                version = info.getFirmwareVersion();
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "band not readable", e);
        }
        // the cached DeviceInfo version can be empty; the stock check has just read it from the band
        if (StockOtaPolicy.clean(version) == null) {
            version = bandVersion;
        }
        StockOtaPolicy.Decision d = StockOtaPolicy.decide(model, version, offeredVersion);
        String line = d.toString();
        if (!line.equals(lastLogged)) {
            lastLogged = line;
            Log.i(TAG, "stock OTA " + line);
        }
        return d;
    }

    /** LatestVersion / OtaUpgradeResult (isNeedUpdate, getForce, dialogs): true = not offered. Quiet. */
    public static boolean offerBlocked(Object offer) {
        String v = offerVersion(offer);
        if (v != null) {
            lastOffer = v;
        }
        return !decide(v).allowed;
    }

    /** A stock update entry the person reached (goUpgradeFragment, ...): true = refused with the notice. */
    public static boolean entryBlocked(String where, Object offer) {
        if (!offerBlocked(offer)) {
            return false;
        }
        stockBlocked(where);
        return true;
    }

    /** Stock code that works on the last offer (buttons, silent upgrade, managers): true = refused. */
    public static boolean currentBlocked(String where, boolean loud) {
        if (decide(lastOffer).allowed) {
            return false;
        }
        if (loud) {
            stockBlocked(where);
        } else {
            stockSkipped(where);
        }
        return true;
    }

    /** onViewCreated of a stock upgrade screen: closed with the notice unless its offer is allowed. */
    public static void closeStockScreen(final Object fragment) {
        Object offer = null;
        try {
            Object args = fragment.getClass().getMethod("getArguments").invoke(fragment);
            if (args instanceof Bundle) {
                for (String k : OFFER_KEYS) {
                    Object o = ((Bundle) args).getSerializable(k);
                    if (o != null) {
                        offer = o;
                        break;
                    }
                }
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            Log.w(TAG, "upgrade screen offer not read", e);
        }
        if (!(offer == null ? currentBlocked("screen", true) : entryBlocked("screen", offer))) {
            return;
        }
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                Object a = fragment.getClass().getMethod("getActivity").invoke(fragment);
                // these screens open in their own container activity; never close the main screen
                if (a instanceof Activity && !a.getClass().getName().endsWith("MainActivity")) {
                    ((Activity) a).finish();
                }
            } catch (ReflectiveOperationException | RuntimeException e) {
                Log.w(TAG, "stock upgrade screen not closed", e);
            }
        });
    }

    static String offerVersion(Object offer) {
        if (offer == null) {
            return null;
        }
        for (String getter : new String[] {"getVersion", "getFirmwareVersion"}) {
            try {
                Object v = offer.getClass().getMethod(getter).invoke(offer);
                if (v instanceof String) {
                    return (String) v;
                }
            } catch (ReflectiveOperationException | RuntimeException e) {
                // try the next getter
            }
        }
        return null;
    }

    // ---- permits and transfer gates

    /** Firmware screen (patch 12, by reflection): allow one transfer of exactly this file. */
    public static synchronized boolean grant(String path) {
        OtaPermit p = OtaPermit.grant(path, System.currentTimeMillis());
        if (p == null || p.isStock()) {
            return false;
        }
        setPermit(p);
        Log.i(TAG, "permit granted for the firmware screen");
        return true;
    }

    /** Firmware screen (patch 12, by reflection): the transfer finished, failed or was refused. */
    public static synchronized void revoke() {
        permit = null;
        File f = permitFile();
        if (f != null && f.exists() && !f.delete()) {
            Log.w(TAG, "permit file not deleted");
        }
    }

    /** DeviceSender/EcoDeviceSender.prepareOta. True = refused; the caller's error callback has been told. */
    public static boolean denyPrepare(String where, Object onError) {
        long now = System.currentTimeMillis();
        if (OtaPermit.prepareAllowed(currentPermit(), now)) {
            return false;
        }
        if (decide(lastOffer).allowed) {
            synchronized (OtaGuard.class) {
                setPermit(OtaPermit.stock(now));
            }
            Log.i(TAG, "stock update allowed by policy: transfer permitted for " + OtaPermit.TTL_MS / 1000 + " s");
            return false;
        }
        stockBlocked(where);
        invokeQuietly(onError, "invoke", new Class<?>[] {Object.class}, Integer.valueOf(-1));
        return true;
    }

    /** DeviceSender/EcoDeviceSender.startOta. True = refused; the caller's listener got onError. */
    public static boolean denyStart(String where, String path, Object listener) {
        if (OtaPermit.startAllowed(currentPermit(), path, System.currentTimeMillis())) {
            return false;
        }
        stockBlocked(where);
        invokeQuietly(listener, "onError", new Class<?>[] {int.class, String.class}, Integer.valueOf(-1), MESSAGE);
        return true;
    }

    /** DeviceContactImpl / DeviceContactEngineImpl.sendFile. True = refused (the caller gets id -1). */
    public static boolean denyFile(int type, String path) {
        if (OtaPermit.fileAllowed(type, currentPermit(), path, System.currentTimeMillis())) {
            return false;
        }
        Log.w(TAG, "firmware file transfer blocked (type " + type + ")");
        return true;
    }

    // ---- notice and log

    /** A refused step the person sees: log and the notice. */
    public static void stockBlocked(String where) {
        Log.w(TAG, "stock update blocked: " + where);
        toast();
    }

    /** A refused background step: log only. */
    public static void stockSkipped(String where) {
        Log.i(TAG, "stock update skipped: " + where);
    }

    private static void setPermit(OtaPermit p) {
        permit = p;
        File f = permitFile();
        if (f == null) {
            return;
        }
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(p.encode().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.w(TAG, "permit file not written", e);
        }
    }

    private static synchronized OtaPermit currentPermit() {
        if (permit != null) {
            return permit;
        }
        // the ":device" process never saw grant(); read what the main process wrote
        File f = permitFile();
        if (f == null || !f.isFile() || f.length() > 4096) {
            return null;
        }
        byte[] buf = new byte[(int) f.length()];
        try (FileInputStream in = new FileInputStream(f)) {
            int n = 0;
            while (n < buf.length) {
                int r = in.read(buf, n, buf.length - n);
                if (r < 0) {
                    break;
                }
                n += r;
            }
            return OtaPermit.decode(new String(buf, 0, n, StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.w(TAG, "permit file not read", e);
            return null;
        }
    }

    private static File permitFile() {
        Application app = ApplicationExtKt.getApplication();
        return app == null ? null : new File(app.getFilesDir(), PERMIT_FILE);
    }

    private static void toast() {
        final Application app = ApplicationExtKt.getApplication();
        if (app == null) {
            return;
        }
        synchronized (OtaGuard.class) {
            long now = System.currentTimeMillis();
            if (!OtaPermit.toastDue(lastToast, now)) {
                return;
            }
            lastToast = now;
        }
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(app, MESSAGE, Toast.LENGTH_LONG).show());
    }

    private static void invokeQuietly(Object target, String name, Class<?>[] types, Object... args) {
        if (target == null) {
            return;
        }
        try {
            Method m = target.getClass().getMethod(name, types);
            m.setAccessible(true);
            m.invoke(target, args);
        } catch (ReflectiveOperationException | RuntimeException e) {
            Log.w(TAG, "caller not notified: " + name, e);
        }
    }
}
