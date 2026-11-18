package com.markus.facefix;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import com.xiaomi.fitness.device.manager.DeviceModel;
import com.xiaomi.fitness.device.manager.bean.DeviceInfo;
import com.xiaomi.fitness.device.manager.export.DeviceManagerExtKt;
import com.xiaomi.fitness.device.manager.export.WearableDeviceManager;

/** Runtime guard for the legacy BLE face patch; no guessing when the product is not loaded yet. */
public final class WatchCompat {
    private WatchCompat() {}
    private static volatile Integer knownType;
    private static final WearDiagnostics.RepeatGate wearState = new WearDiagnostics.RepeatGate();
    private static final WearDiagnostics.RepeatGate peerState = new WearDiagnostics.RepeatGate();
    private static final WearDiagnostics.RepeatGate watchState = new WearDiagnostics.RepeatGate();

    public static boolean localFaces() {
        try {
            DeviceModel m = DeviceManagerExtKt.getInstance(WearableDeviceManager.Companion).getCurrentDeviceModel();
            knownType = m == null || m.getProduct() == null ? null : m.getProduct().getType();
            return DevicePolicy.localFaces(knownType);
        } catch (RuntimeException e) {
            return false;
        }
    }
    public static String corePackage(String stock) {
        // Core service selection runs during device-manager initialization: never query that manager recursively here.
        if (!DevicePolicy.localFaces(knownType) || !"com.xiaomi.mi_connect_service".equals(stock)) return stock;
        try {
            Context app = (Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
            android.content.pm.ServiceInfo service = app.getPackageManager().getServiceInfo(
                    new ComponentName("com.xiaomi.lyra", "com.xiaomi.wearable.core.MiWearCoreService"), 0);
            boolean usable = service.enabled && service.exported
                    && (service.permission == null || app.checkSelfPermission(service.permission) == PackageManager.PERMISSION_GRANTED);
            return DevicePolicy.corePackage(stock, true, usable);
        } catch (ReflectiveOperationException | PackageManager.NameNotFoundException | RuntimeException e) {
            return stock;
        }
    }

    /** Called on the log worker. The returned snapshot is written directly into the export, not only logcat. */
    public static String logSnapshot() {
        synchronized (WatchCompat.class) {
            wearState.reset();
            peerState.reset();
            watchState.reset();
        }
        try {
            DeviceModel model = DeviceManagerExtKt.getInstance(WearableDeviceManager.Companion).getCurrentDeviceModel();
            if (model == null || model.getProduct() == null) return "wear: device=unavailable";
            if (model.getProduct().getType() != 1) return "wear: device=not_wear_os";
            DeviceInfo info = model.getDeviceInfo();
            String peerId = info == null ? null : info.getNodeID();
            return "wear: " + WearDiagnostics.describe(WearDiagnostics.currentApi(), peerId);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return "wear: " + WearDiagnostics.failure(e);
        }
    }

    /** Reads only the existing SDK cache and compares ids internally; no connection or pairing operation. */
    public static synchronized void noteWearState(Object api, String peerId) {
        String state = WearDiagnostics.describe(api, peerId);
        if (wearState.take(state, android.os.SystemClock.elapsedRealtime())) {
            android.util.Log.i("MOD", "wear: " + state);
        }
    }
    public static synchronized void noteWearLookup(String peerId) {
        String state = peerId == null || peerId.isEmpty() ? "peer id missing" : "peer id present";
        if (peerState.take(state, android.os.SystemClock.elapsedRealtime())) {
            android.util.Log.i("MOD", "wear: " + state);
        }
    }
    public static synchronized void noteWearResult(Object watch) {
        String state = watch == null ? "watch lookup missing" : "watch lookup available";
        if (watchState.take(state, android.os.SystemClock.elapsedRealtime())) {
            android.util.Log.i("MOD", "wear: " + state);
        }
    }
}
