import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.databinding.ObservableArrayList;
import com.markus.Settings;
import com.markus.a;
import com.markus.dnd.SharedFlag;
import com.markus.dnd.ZenSyncRules;
import com.xiaomi.fitness.device.contact.export.OnSyncCallback;
import com.xiaomi.fitness.device.contact.export.SyncResult;
import com.xiaomi.fitness.device.manager.DeviceModel;
import com.xiaomi.fitness.device.manager.bean.DeviceInfo;
import com.xiaomi.fitness.device.manager.export.bean.TabContentSwitchItem;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Only the device process reconciles AOD. The UI shares the opt-in flag and signals that process. */
public final class MarkusAodSync {
    static final String KEY = "pause_aod_phone_dnd";
    private static final String SETTINGS_CHANGED = "com.xiaomi.wearable.markus.AOD_DND_CHANGED";
    private static final String INTERNAL_PERMISSION = "com.xiaomi.wearable.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION";
    private static final String TAG = "MOD";
    private static final ScheduledExecutorService WORKER =
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "mod-aod-dnd"));
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static volatile Context owner;
    // Device-process worker only, including transport callbacks.
    private static ScheduledFuture<?> scheduled;
    private static Attempt active;
    private static boolean reconcileAgain;

    private MarkusAodSync() {}

    public static synchronized void register() {
        if (owner != null) return;
        Context app = MarkusZenSync.application();
        if (app == null) return;
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                if (SETTINGS_CHANGED.equals(intent.getAction())) onEvent();
            }
        };
        IntentFilter filter = new IntentFilter(SETTINGS_CHANGED);
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                app.registerReceiver(receiver, filter, INTERNAL_PERMISSION, null, Context.RECEIVER_NOT_EXPORTED);
            } else {
                app.registerReceiver(receiver, filter, INTERNAL_PERMISSION, null);
            }
            owner = app;
            onEvent();
        } catch (RuntimeException e) {
            Log.w(TAG, "aod dnd: settings receiver unavailable");
        }
    }

    public static void settingChanged(String key, boolean enabled) {
        if (!KEY.equals(key)) return;
        Context app = MarkusZenSync.application();
        if (app == null) return;
        WORKER.execute(() -> {
            try {
                SharedFlag.write(flag(app), enabled);
                app.sendBroadcast(new Intent(SETTINGS_CHANGED).setPackage(app.getPackageName()), INTERNAL_PERMISSION);
            } catch (IOException | RuntimeException e) {
                Log.w(TAG, "aod dnd: switch could not be shared");
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void insertSwitch(ObservableArrayList list, int type) {
        if (type != MarkusZenSync.TYPE_MOD_SETTINGS || list == null) return;
        DeviceModel model = MarkusZenSync.currentDevice();
        if (!supported(model)) return;
        Context app = MarkusZenSync.application();
        if (app == null) return;
        TabContentSwitchItem row = a.createSwitchRow(app, 0, "device_tab_setting_icon_system", "markus_pause_aod_dnd",
                "markus_pause_aod_dnd_summary", KEY);
        list.add(row);
        WORKER.execute(() -> {
            try {
                File state = readStatus(app, model, model.getDid());
                if (!AodReadStatus.blocksProbe(state, false)) return;
                int message = app.getResources().getIdentifier("markus_pause_aod_dnd_unavailable", "string", app.getPackageName());
                if (message != 0) MAIN.post(() -> row.setSubtitle(app.getString(message)));
            } catch (IOException | RuntimeException e) {
                Log.w(TAG, "aod dnd: saved read status unavailable");
            }
        });
    }

    public static void onEvent() {
        if (owner != null) WORKER.execute(MarkusAodSync::schedule);
    }

    public static void connectionChanged() {
        if (owner == null) return;
        WORKER.execute(() -> {
            if (active != null) {
                active.timeout.cancel(false);
                active = null;
            }
            schedule();
        });
    }

    private static void schedule() {
        if (scheduled != null) scheduled.cancel(false);
        scheduled = WORKER.schedule(MarkusAodSync::reconcile, 1000, TimeUnit.MILLISECONDS);
    }

    private static void reconcile() {
        scheduled = null;
        if (active != null) {
            reconcileAgain = true;
            return;
        }
        reconcileAgain = false;
        try {
            DeviceModel model = MarkusZenSync.currentDevice();
            String identity = connectedBand(model);
            if (identity == null) return;
            Attempt attempt = new Attempt(identity, preferences(), readStatus(owner, model, identity));
            if (desiredPause(attempt) == null || AodReadStatus.blocksProbe(attempt.readStatus, attempt.saved() != null)) return;
            active = attempt;
            attempt.timeout = WORKER.schedule(() -> fail(attempt, "request timed out"), 30, TimeUnit.SECONDS);
            read(attempt, result -> decide(attempt, result));
        } catch (IOException | RuntimeException e) {
            if (active != null) fail(active, "settings or transport unavailable");
            else Log.w(TAG, "aod dnd: settings unavailable");
        }
    }

    /** null means no safe action, including an unreadable environmental setting. */
    private static Boolean desiredPause(Attempt attempt) throws IOException {
        boolean enabled = SharedFlag.read(flag(owner), Settings.getBoolean(KEY, false));
        if (!enabled) return attempt.saved() == null ? null : Boolean.FALSE;
        Boolean dnd = ZenSyncRules.phoneOn(MarkusZenSync.phoneFilter());
        if (Boolean.FALSE.equals(dnd) && attempt.saved() == null) return null;
        return dnd;
    }

    private static void decide(Attempt attempt, AodSetting observed) throws IOException {
        Boolean pause = desiredPause(attempt);
        if (pause == null || observed == null) {
            if (observed == null) fail(attempt, "no valid AOD state from band");
            else finish(attempt);
            return;
        }
        AodSetting saved = attempt.saved();
        switch (AodSetting.decide(pause, observed, saved)) {
            case PAUSE:
                // Persist before writing to the band so a process restart can still restore AOD.
                if (!attempt.store(observed)) {
                    fail(attempt, "could not save original AOD settings");
                    return;
                }
                writeAndCheck(attempt, observed.off());
                break;
            case RESTORE:
                writeAndCheck(attempt, saved);
                break;
            case FORGET:
                // A manually selected active mode wins over the saved snapshot.
                clear(attempt);
                break;
            default:
                finish(attempt);
        }
    }

    private static void read(Attempt attempt, ResultHandler handler) {
        send(attempt, AodRequest.read(), response -> {
            AodSetting observed = AodRequest.decode(MarkusLegacyZenSender.aodResponse(response));
            if (observed != null) {
                try {
                    AodReadStatus.record(attempt.readStatus, 0);
                } catch (IOException | RuntimeException e) {
                    Log.w(TAG, "aod dnd: read status could not be reset");
                }
            }
            handler.accept(observed);
        });
    }

    private static void writeAndCheck(Attempt attempt, AodSetting expected) {
        send(attempt, AodRequest.write(expected), ignored -> read(attempt, observed -> {
            if (!expected.matches(observed)) {
                fail(attempt, "band did not confirm expected AOD settings");
                return;
            }
            if (expected.mode == AodSetting.OFF) {
                Log.i(TAG, "aod dnd: AOD paused");
                finish(attempt);
            } else {
                Log.i(TAG, "aod dnd: previous AOD mode restored");
                clear(attempt);
            }
        }));
    }

    private static void send(Attempt attempt, t3u request, ResponseHandler handler) {
        if (!stillConnected(attempt)) {
            finish(attempt);
            return;
        }
        MarkusLegacyZenSender.sendAod(attempt.identity, request, new OnSyncCallback() {
            @Override public void onSuccess(String ignored, int type, SyncResult response) {
                WORKER.execute(() -> {
                    if (active != attempt) return;
                    try {
                        if (!stillConnected(attempt)) {
                            finish(attempt);
                            return;
                        }
                        handler.accept(response);
                    } catch (IOException | RuntimeException e) {
                        fail(attempt, "settings or transport unavailable");
                    }
                });
            }

            @Override public void onError(String ignored, int type, int error) {
                WORKER.execute(() -> {
                    if (active != attempt) return;
                    try {
                        if (request.d == AodRequest.READ_SUBTYPE && stillConnected(attempt) && attempt.saved() == null
                                && AodReadStatus.record(attempt.readStatus, error)) {
                            Log.w(TAG, "aod dnd: repeated read rejection, automatic AOD paused for this firmware");
                        }
                    } catch (IOException | RuntimeException e) {
                        Log.w(TAG, "aod dnd: read status could not be saved");
                    }
                    fail(attempt, "request failed, code " + error);
                });
            }
        });
    }

    private static boolean stillConnected(Attempt attempt) {
        return attempt.identity.equals(connectedBand(MarkusZenSync.currentDevice()));
    }

    private static File readStatus(Context context, DeviceModel model, String identity) {
        try {
            DeviceInfo info = model.getDeviceInfo();
            String firmware = info == null ? null : info.getFirmwareVersion();
            return AodReadStatus.file(context.getFilesDir(), identity, firmware);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String connectedBand(DeviceModel model) {
        if (!supported(model) || !model.isDeviceConnected()) return null;
        String identity = model.getDid();
        return identity == null || identity.isEmpty() ? null : identity;
    }

    private static boolean supported(DeviceModel model) {
        try {
            DeviceInfo info = model == null ? null : model.getDeviceInfo();
            return info != null && AodSetting.supported(info.getModel());
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static File flag(Context context) {
        return new File(context.getFilesDir(), "mod-aod-dnd.flag");
    }

    private static SharedPreferences preferences() {
        return owner.getSharedPreferences("markus_aod_dnd_restore", Context.MODE_PRIVATE);
    }

    private static void clear(Attempt attempt) {
        if (attempt.store(null)) finish(attempt);
        else fail(attempt, "could not clear saved AOD state");
    }

    private static void fail(Attempt attempt, String reason) {
        if (active != attempt) return;
        Log.w(TAG, "aod dnd: " + reason);
        finish(attempt);
    }

    private static void finish(Attempt attempt) {
        if (active != attempt) return;
        attempt.timeout.cancel(false);
        active = null;
        if (reconcileAgain) schedule();
    }

    private interface ResultHandler {
        void accept(AodSetting setting) throws IOException;
    }

    private interface ResponseHandler {
        void accept(SyncResult response) throws IOException;
    }

    private static final class Attempt {
        final String identity;
        final SharedPreferences preferences;
        final String key;
        final File readStatus;
        ScheduledFuture<?> timeout;

        Attempt(String identity, SharedPreferences preferences, File readStatus) {
            this.identity = identity;
            this.preferences = preferences;
            this.readStatus = readStatus;
            key = UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8)).toString();
        }

        AodSetting saved() {
            return AodSetting.decode(preferences.getString(key, null));
        }

        boolean store(AodSetting setting) {
            SharedPreferences.Editor edit = preferences.edit();
            return (setting == null ? edit.remove(key) : edit.putString(key, setting.encode())).commit();
        }
    }
}
