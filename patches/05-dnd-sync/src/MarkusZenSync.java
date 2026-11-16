import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.databinding.ObservableArrayList;
import com.markus.Settings;
import com.markus.a;
import com.markus.dnd.ZenSyncRules;
import com.markus.dnd.ZenSyncDelivery;
import com.markus.dnd.SharedFlag;
import com.markus.facefix.WatchCompat;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.xiaomi.fitness.device.manager.DeviceManager;
import com.xiaomi.fitness.device.manager.DeviceModel;
import com.xiaomi.fitness.device.manager.export.DeviceManagerExtKt;
import com.xiaomi.fitness.device.manager.export.WearableDeviceManager;
import com.xiaomi.fitness.device.contact.export.OnSyncCallback;
import com.xiaomi.fitness.device.contact.export.SyncResult;

/** Opt-in phone -> BLE band DND using the stock legacy command for bands without zen-rule support. */
public final class MarkusZenSync {
    static final String KEY = "simple_zen_sync";
    static final int TYPE_MOD_SETTINGS = 0x7000;
    private static final long DEBOUNCE_MS = 1500;
    private static final String TAG = "MOD";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final ZenSyncDelivery DELIVERY = new ZenSyncDelivery();
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(r -> new Thread(r, "mod-dnd"));
    private static final Runnable RUN = () -> WORKER.execute(MarkusZenSync::decideAndSend);

    public static void settingChanged(String key, boolean value) {
        if (!KEY.equals(key)) return;
        WORKER.execute(() -> {
            try {
                SharedFlag.write(flagFile(), value);
                DELIVERY.reset();
                Log.i(TAG, "dnd: shared switch " + (value ? "on" : "off"));
                decideAndSend();
            } catch (IOException | RuntimeException e) {
                Log.w(TAG, "dnd: switch could not be shared", e);
            }
        });
    }

    private static File flagFile() {
        Context app = application();
        if (app == null) throw new IllegalStateException("application unavailable");
        return new File(app.getFilesDir(), "mod-simple-dnd.flag");
    }

    private MarkusZenSync() {}

    public static void onStockPath() {
        MAIN.removeCallbacks(RUN);
        MAIN.postDelayed(RUN, DEBOUNCE_MS);
    }

    private static void decideAndSend() {
        boolean switchOn = switchOn();
        DeviceModel model = currentDevice();
        boolean connected = model != null && model.isDeviceConnected();
        String identity = connected ? model.getDid() : null;
        DELIVERY.selectDevice(identity);
        boolean hasRuleSync = model != null && ZenSyncRules.hasRuleSync(capability(model));
        Boolean phone = phoneOn();
        ZenSyncRules.Decision decision = ZenSyncRules.decide(switchOn,
                connected && identity != null && WatchCompat.localFaces(), hasRuleSync, phone, DELIVERY.acknowledged());
        switch (decision) {
            case SEND_ON:
            case SEND_OFF:
                boolean on = decision == ZenSyncRules.Decision.SEND_ON;
                if (DELIVERY.isPending(on)) {
                    Log.i(TAG, "dnd: legacy command pending");
                    return;
                }
                send(identity, on);
                break;
            case SKIP_SWITCH_OFF:
            case SKIP_HAS_RULE_SYNC:
            case SKIP_NO_BAND:
                DELIVERY.reset();
                Log.i(TAG, "dnd: simple sync skipped: " + decision);
                break;
            default:
                Log.i(TAG, "dnd: simple sync skipped: " + decision);
                break;
        }
    }

    private static void send(String identity, boolean on) {
        long ticket = DELIVERY.begin(on);
        try {
            MarkusLegacyZenSender.send(identity, LegacyZenRequest.create(on), new OnSyncCallback() {
                @Override public void onSuccess(String ignored, int type, SyncResult result) {
                    WORKER.execute(() -> finish(identity, ticket, true, type, 0));
                }
                @Override public void onError(String ignored, int type, int error) {
                    WORKER.execute(() -> finish(identity, ticket, false, type, error));
                }
            });
            Log.i(TAG, "dnd: legacy 2/23 queued, phone DND " + (on ? "on" : "off"));
        } catch (RuntimeException e) {
            DELIVERY.complete(ticket, false);
            Log.w(TAG, "dnd: legacy command not queued", e);
        }
    }

    private static void finish(String identity, long ticket, boolean success, int type, int error) {
        if (!DELIVERY.complete(ticket, success)) return;
        DeviceModel current = currentDevice();
        if (!switchOn() || current == null || !current.isDeviceConnected() || !identity.equals(current.getDid())) {
            DELIVERY.reset();
            Log.i(TAG, "dnd: legacy callback ignored after switch/device change");
            return;
        }
        // Transport acknowledgement alone does not prove the band's visible DND mode changed.
        if (success) Log.i(TAG, "dnd: legacy transport acknowledged, type " + type);
        else Log.w(TAG, "dnd: legacy transport error, type " + type + ", code " + error);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void insertSwitch(ObservableArrayList list, int type) {
        if (type != TYPE_MOD_SETTINGS || list == null) return;
        DeviceModel model = currentDevice();
        if (!ZenSyncRules.offerSwitch(model != null, model != null && ZenSyncRules.hasRuleSync(capability(model)))) return;
        Context app = application();
        if (app == null) return;
        list.add(a.createSwitchRow(app, 0, "device_tab_setting_icon_health", "markus_simple_zen_sync",
                "markus_simple_zen_sync_summary", KEY));
    }

    private static boolean switchOn() {
        try {
            return SharedFlag.read(flagFile(), Settings.getBoolean(KEY, false));
        } catch (IOException | RuntimeException e) {
            Log.w(TAG, "dnd: shared switch unreadable; disabled", e);
            return false;
        }
    }

    private static DeviceModel currentDevice() {
        try {
            DeviceManager manager = DeviceManagerExtKt.getInstance(WearableDeviceManager.Companion);
            return manager == null ? null : manager.getCurrentDeviceModel();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static long capability(DeviceModel model) {
        try {
            DeviceManager manager = DeviceManagerExtKt.getInstance(WearableDeviceManager.Companion);
            return manager instanceof WearableDeviceManager
                    ? ((WearableDeviceManager) manager).getDeviceCapabilityV3(model.getDid()) : 0;
        } catch (RuntimeException e) {
            return 0;
        }
    }

    private static Boolean phoneOn() {
        Context app = application();
        NotificationManager manager = app == null ? null : (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        return manager == null ? null : ZenSyncRules.phoneOn(manager.getCurrentInterruptionFilter());
    }

    private static Context application() {
        try {
            return (Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
