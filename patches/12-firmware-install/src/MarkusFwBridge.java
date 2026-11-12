import android.os.Handler;
import android.os.Looper;
import com.markus.fw.DeviceBridge;
import com.markus.fw.DeviceSnapshot;
import com.mi.fitness.checkupdate.util.DeviceSender;
import com.xiaomi.fitness.device.contact.export.DeviceSyncCallback;
import com.xiaomi.fitness.device.manager.DeviceBasicStatusDataHandler;
import com.xiaomi.fitness.device.manager.DeviceManager;
import com.xiaomi.fitness.device.manager.DeviceModel;
import com.xiaomi.fitness.device.manager.bean.DeviceInfo;
import com.xiaomi.fitness.device.manager.bean.Product;
import com.xiaomi.fitness.device.manager.export.DeviceManagerExtKt;
import com.xiaomi.fitness.device.manager.export.WearableDeviceManager;
import com.xiaomi.fitness.device.manager.export.WearableDeviceModel;
import com.xiaomi.fitness.device.manager.export.bean.WatchInfo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * The only class that touches the app's obfuscated types (rr7, jir, edj). It lives in the unnamed package
 * because those types do; com.markus.fw reaches it through DeviceBridge.Holder.
 */
public final class MarkusFwBridge implements DeviceBridge {
    /**
     * Members used by reflection. Kept as smali-style strings so build.sh checks that they exist in the app
     * version being built (obfuscated names change between releases).
     */
    static final String[] REFLECTED = {
        "Lcom/xiaomi/fitness/device/manager/export/WearableDeviceModel;->readWatchInfo(Lrr7;)V",
        "Lcom/mi/fitness/checkupdate/util/DeviceSender;->startOta(IILjava/lang/String;ILedj;)V",
    };
    private static final long TIMEOUT_MS = 10_000;
    /** prepareOta answer codes, as handled by the stock BluetoothUpgradeViewModel.startGeneralOta. */
    private static final int PREPARE_OK = 0;
    private static final int PREPARE_DONE = 2;
    private static final int PREPARE_REJECTED = 3;
    private static final int PREPARE_LOW_BATTERY = 4;

    private final Handler main = new Handler(Looper.getMainLooper());

    @Override
    public void readSnapshot(final SnapshotCallback callback) {
        main.post(() -> read(callback));
    }

    private void read(final SnapshotCallback callback) {
        final DeviceSnapshot s = new DeviceSnapshot();
        final DeviceModel m = currentDevice();
        if (m == null) {
            s.watchInfoError = "нет устройства";
            s.batteryError = "нет устройства";
            callback.onSnapshot(s);
            return;
        }
        s.hasDevice = true;
        s.connected = m.isDeviceConnected();
        DeviceInfo info = m.getDeviceInfo();
        if (info != null) {
            s.boundModel = info.getModel();
            s.cachedVersion = info.getFirmwareVersion();
        }
        Product p = m.getProduct();
        s.otaMode = p == null ? -1 : p.getOta();
        if (!s.connected) {
            s.watchInfoError = "браслет не подключён";
            s.batteryError = "браслет не подключён";
            callback.onSnapshot(s);
            return;
        }
        final int[] pending = {2};
        final boolean[] done = {false};
        final Runnable finish = () -> {
            if (!done[0] && --pending[0] <= 0) {
                done[0] = true;
                callback.onSnapshot(s);
            }
        };
        main.postDelayed(() -> {
            if (!done[0]) {
                done[0] = true;
                if (!s.watchInfoOk && s.watchInfoError == null) {
                    s.watchInfoError = "браслет не ответил за 10 с";
                }
                if (!s.batteryOk && s.batteryError == null) {
                    s.batteryError = "браслет не ответил за 10 с";
                }
                callback.onSnapshot(s);
            }
        }, TIMEOUT_MS);

        if (m instanceof WearableDeviceModel) {
            try {
                Method readWatchInfo = WearableDeviceModel.class.getMethod("readWatchInfo", rr7.class);
                readWatchInfo.invoke(m, new rr7() {
                    @Override
                    public void onError(int code) {
                        main.post(() -> {
                            s.watchInfoError = "ошибка " + code;
                            finish.run();
                        });
                    }

                    @Override
                    public void onResult(Object result) {
                        main.post(() -> {
                            if (result instanceof WatchInfo) {
                                WatchInfo w = (WatchInfo) result;
                                s.watchInfoOk = true;
                                s.reportedModel = w.getModel();
                                s.liveVersion = w.getFirmwareVersion();
                                s.productDevice = w.getProductDevice();
                                s.versionChannel = w.getVersionChannel();
                            } else {
                                s.watchInfoError = "пустой ответ";
                            }
                            finish.run();
                        });
                    }
                });
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                s.watchInfoError = "вызов недоступен: " + e;
                finish.run();
            }
        } else {
            s.watchInfoError = "устройство не поддерживает readWatchInfo";
            finish.run();
        }

        DeviceBasicStatusDataHandler.INSTANCE.syncBattery(m.getDid(), new DeviceSyncCallback() {
            @Override
            public void onSyncError(String did, int type, String msg) {
                main.post(() -> {
                    s.batteryError = "ошибка " + msg;
                    finish.run();
                });
            }

            @Override
            public void onSyncSuccess(String did, int type, Object data) {
                main.post(() -> {
                    // syncBattery stores the value it just received in DeviceInfo before calling back
                    DeviceInfo di = m.getDeviceInfo();
                    int b = di == null ? -1 : di.getBattery();
                    s.batteryOk = b >= 0 && b <= 100;
                    s.battery = b;
                    if (!s.batteryOk) {
                        s.batteryError = "браслет вернул " + b;
                    }
                    finish.run();
                });
            }
        });
    }

    @Override
    public void install(final String path, final String version, final String md5, final int size,
            final InstallListener caller) {
        if (version == null || version.isEmpty() || "0".equals(version) || size <= 0 || md5 == null) {
            caller.onFailed("нет реальной версии, размера или MD5 файла — отправка отменена");
            return;
        }
        if (!otaPermit(true, path)) {
            caller.onFailed("не удалось выдать разрешение на передачу этого файла — отправка отменена");
            return;
        }
        final InstallListener listener = revokingPermit(caller);
        DeviceSender.INSTANCE.prepareOta(false, 0, version, md5, Integer.valueOf(size), "",
                new Function1<Object, Unit>() {
                    @Override
                    public Unit invoke(Object answer) {
                        onPrepared(answer, path, listener);
                        return Unit.INSTANCE;
                    }
                },
                new Function1<Object, Unit>() {
                    @Override
                    public Unit invoke(Object code) {
                        listener.onFailed("браслет не ответил на запрос подготовки (код " + code + ")");
                        return Unit.INSTANCE;
                    }
                });
    }

    private void onPrepared(Object answer, String path, final InstallListener listener) {
        if (!(answer instanceof jir)) {
            listener.onFailed("браслет не ответил на запрос подготовки");
            return;
        }
        jir d = (jir) answer;
        switch (d.a) {
            case PREPARE_OK:
                break;
            case PREPARE_LOW_BATTERY:
                listener.onFailed("браслет отказался: заряд слишком низкий для обновления. Зарядите браслет и повторите");
                return;
            case PREPARE_DONE:
                listener.onFailed("браслет ответил, что пакет не нужен или уже установлен. Успехом это не считается");
                return;
            case PREPARE_REJECTED:
                listener.onFailed("браслет отказался принять пакет (код 3)");
                return;
            default:
                listener.onFailed("браслет отказался (код " + d.a + ")");
                return;
        }
        edj otaListener = new edj() {
            @Override
            public void onComplete() {
                main.post(listener::onTransferred);
            }

            @Override
            public void onError(int code, String msg) {
                main.post(() -> listener.onFailed("передача прервана (код " + code + (msg == null || msg.isEmpty()
                        ? "" : ", " + msg) + "). Повтор не выполняется автоматически"));
            }

            @Override
            public void onProgress(int progress) {
                main.post(() -> listener.onProgress(progress));
            }

            @Override
            public void onStart() {
            }
        };
        try {
            Method startOta = DeviceSender.class.getMethod("startOta", int.class, int.class, String.class, int.class,
                    edj.class);
            // otaMode 2 = general OTA, type 0 = firmware, segment length as the band asked in its answer
            startOta.invoke(DeviceSender.INSTANCE, 2, 0, path, d.c, otaListener);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            listener.onFailed("передача недоступна: " + e);
        }
    }

    /**
     * Patch 16 blocks every firmware transfer to the band unless this screen holds a permit for the exact file.
     * Reached by reflection so that patch 12 still builds and works without patch 16 (then there is no gate).
     */
    private static boolean otaPermit(boolean on, String path) {
        Class<?> guard;
        try {
            guard = Class.forName("com.markus.ota.OtaGuard");
        } catch (ClassNotFoundException e) {
            return true;
        }
        try {
            if (on) {
                return Boolean.TRUE.equals(guard.getMethod("grant", String.class).invoke(null, path));
            }
            guard.getMethod("revoke").invoke(null);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    /** The permit ends with the transfer: on success and on every failure. */
    private static InstallListener revokingPermit(final InstallListener l) {
        return new InstallListener() {
            @Override
            public void onProgress(int percent) {
                l.onProgress(percent);
            }

            @Override
            public void onTransferred() {
                otaPermit(false, null);
                l.onTransferred();
            }

            @Override
            public void onFailed(String reason) {
                otaPermit(false, null);
                l.onFailed(reason);
            }
        };
    }

    private static DeviceModel currentDevice() {
        DeviceManager dm = DeviceManagerExtKt.getInstance(WearableDeviceManager.Companion);
        return dm == null ? null : dm.getCurrentDeviceModel();
    }
}
