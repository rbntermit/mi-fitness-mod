import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.databinding.ObservableArrayList;
import com.markus.Settings;
import com.markus.a;
import com.markus.dnd.ZenSyncRules;
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
import com.xiaomi.fitness.devicesettings.common.zenmode.ZenModeSyncHelper;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * «Простая синхронизация „Не беспокоить“» (off by default) for bands that do not report zen rule sync (capability
 * bit 4, e.g. Band 10 by the owner's log): when the phone's DND changes, the band gets the same one rule
 * manual_zen_rule that patch 05 sends to bands with the bit. It is the only phone -> band DND command in 3.60
 * (ZenUtils.postSetZenMode goes the other way: it sets the phone's DND from the band). Debounced, only changes,
 * only phone -> band, so it cannot loop. Unnamed package because the rule class (njr) lives there.
 *
 * Called from ZenModeSyncHelper.trySyncManualZenRule when it takes the stock path (hooks.txt).
 */
public final class MarkusZenSync {
    static final String KEY = "simple_zen_sync";
    static final int TYPE_MOD_SETTINGS = 0x7000;
    private static final long DEBOUNCE_MS = 1500;
    private static final String TAG = "MOD";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static volatile Boolean lastSent;
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(r -> new Thread(r, "mod-dnd"));
    private static final Runnable RUN = () -> WORKER.execute(MarkusZenSync::decideAndSend);
    private static String lastDevice;

    /** Settings.setBoolean hook: carry the choice to the device process and immediately refresh the phone state. */
    public static void settingChanged(String key, boolean value) {
        if (!KEY.equals(key)) return;
        WORKER.execute(() -> {
            try {
                SharedFlag.write(flagFile(), value);
                lastSent = null;
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

    private MarkusZenSync() {
    }

    /** trySyncManualZenRule took the stock path (no band, or no capability bit 4). */
    public static void onStockPath() {
        MAIN.removeCallbacks(RUN);
        MAIN.postDelayed(RUN, DEBOUNCE_MS);
    }

    private static void decideAndSend() {
        boolean switchOn = switchOn();
        DeviceModel m = currentDevice();
        boolean connected = m != null && m.isDeviceConnected();
        String identity = connected ? m.getDid() : null;
        if (identity == null || !identity.equals(lastDevice)) lastSent = null;
        lastDevice = identity;
        boolean hasRuleSync = m != null && ZenSyncRules.hasRuleSync(capability(m));
        Boolean phone = phoneOn();
        ZenSyncRules.Decision d = ZenSyncRules.decide(switchOn, connected && WatchCompat.localFaces(), hasRuleSync, phone, lastSent);
        switch (d) {
            case SEND_ON:
            case SEND_OFF:
                boolean on = d == ZenSyncRules.Decision.SEND_ON;
                if (send(on)) {
                    lastSent = on;
                    Log.i(TAG, "dnd: simple sync queued phone DND " + (on ? "on" : "off") + " to the band");
                }
                break;
            case SKIP_SWITCH_OFF:
                Log.i(TAG, "dnd: band without zen rule sync, simple sync is off (switch)");
                break;
            default:
                Log.i(TAG, "dnd: simple sync skipped: " + d);
                break;
        }
    }

    private static boolean send(boolean on) {
        try {
            njr rule = new njr();
            rule.a = true;
            rule.b = "manual_zen_rule";
            rule.c = on ? 1 : 0;
            rule.e = (int) System.currentTimeMillis();
            List<Object> rules = new ArrayList<>();
            rules.add(rule);
            // the same private sender patch 05 calls for bands with the bit
            Method m = ZenModeSyncHelper.class.getDeclaredMethod("syncZenRule", List.class);
            m.setAccessible(true);
            m.invoke(ZenModeSyncHelper.INSTANCE, rules);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            Log.w(TAG, "dnd: simple sync not sent", e);
            return false;
        }
    }

    /** DeviceSettingListViewModel.createSettingItems, after com.markus.a.insertSubMenus: the switch row. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void insertSwitch(ObservableArrayList list, int type) {
        if (type != TYPE_MOD_SETTINGS || list == null) {
            return;
        }
        DeviceModel m = currentDevice();
        if (!ZenSyncRules.offerSwitch(m != null, m != null && ZenSyncRules.hasRuleSync(capability(m)))) {
            return;
        }
        Context app = application();
        if (app == null) {
            return;
        }
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
            DeviceManager dm = DeviceManagerExtKt.getInstance(WearableDeviceManager.Companion);
            return dm == null ? null : dm.getCurrentDeviceModel();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static long capability(DeviceModel m) {
        try {
            DeviceManager dm = DeviceManagerExtKt.getInstance(WearableDeviceManager.Companion);
            return dm instanceof WearableDeviceManager ? ((WearableDeviceManager) dm).getDeviceCapabilityV3(m.getDid()) : 0;
        } catch (RuntimeException e) {
            return 0;
        }
    }

    private static Boolean phoneOn() {
        Context app = application();
        NotificationManager nm = app == null ? null : (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        return nm == null ? null : ZenSyncRules.phoneOn(nm.getCurrentInterruptionFilter());
    }

    /** ActivityThread.currentApplication() is not in the public SDK; the mod's own menu code uses it the same way. */
    private static Context application() {
        try {
            return (Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
