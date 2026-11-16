package com.markus.facefix;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import com.xiaomi.fitness.device.manager.DeviceModel;
import com.xiaomi.fitness.device.manager.export.DeviceManagerExtKt;
import com.xiaomi.fitness.device.manager.export.WearableDeviceManager;

/** Runtime guard for the legacy BLE face patch; no guessing when the product is not loaded yet. */
public final class WatchCompat {
    private WatchCompat() {}
    private static volatile Integer knownType;
    private static String wearState;
    private static Boolean peerPresent;
    private static Boolean watchPresent;
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
    /** No peer ids or addresses. Distinguishes a missing SDK, pending initialization, SDK failure and empty pairing. */
    public static synchronized void noteWearState(Object api) {
        String state = "api unavailable";
        try {
            if (api != null) {
                Object stream = Class.forName("com.google.android.libraries.wear.companion.watch.WatchApi").getMethod("getPairedWatches").invoke(api);
                Object result = Class.forName("com.google.android.libraries.wear.common.stream.ValueStream").getMethod("getCurrentValue").invoke(stream);
                state = "paired watches pending";
                if (result != null) {
                    Object value = result.getClass().getMethod("unbox-impl").invoke(result);
                    Object error = result.getClass().getMethod("errorOrNull-impl", Object.class).invoke(null, value);
                    if (error != null) {
                        state = "paired watches SDK error";
                        if (error instanceof Enum) state += " " + ((Enum<?>) error).name();
                    } else {
                        state = value instanceof java.util.List ? "paired watches count=" + ((java.util.List<?>) value).size()
                                : "paired watches result available";
                    }
                }
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            state = "paired watches diagnostics unavailable: " + e.getClass().getSimpleName();
        }
        if (!state.equals(wearState)) {
            wearState = state;
            android.util.Log.i("MOD", "wear: " + state);
        }
    }
    /** Log presence only, never the peer id. Missing ids used to return before all diagnostics. */
    public static synchronized void noteWearLookup(String peerId) {
        boolean present = peerId != null && !peerId.isEmpty();
        if (peerPresent == null || peerPresent.booleanValue() != present) {
            peerPresent = present;
            android.util.Log.i("MOD", present ? "wear: peer id present" : "wear: peer id missing");
        }
    }
    public static synchronized void noteWearResult(Object watch) {
        boolean present = watch != null;
        if (watchPresent == null || watchPresent.booleanValue() != present) {
            watchPresent = present;
            android.util.Log.i("MOD", present ? "wear: watch lookup available" : "wear: watch lookup missing");
        }
    }
}