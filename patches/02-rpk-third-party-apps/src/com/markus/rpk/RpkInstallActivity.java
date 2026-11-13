package com.markus.rpk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.xiaomi.fitness.device.manager.DeviceManager;
import com.xiaomi.fitness.device.manager.DeviceModel;
import com.xiaomi.fitness.device.manager.DeviceModelExtKt;
import com.xiaomi.fitness.device.manager.export.DeviceManagerExtKt;
import com.xiaomi.fitness.device.manager.export.WearableDeviceManager;
import com.xiaomi.fitness.device.manager.export.WearableDeviceModel;
import com.xiaomi.xms.wearable.db.entity.WatchAppItemEntity;
import com.xiaomi.xms.wearable.ui.appshop.manager.AppDownloadManager;
import com.xiaomi.xms.wearable.ui.appshop.manager.AppInstallManager;
import com.xiaomi.xms.wearable.ui.appshop.manager.AppStatusManager;
import com.xiaomi.xms.wearable.ui.appshop.model.AppStatusInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

/**
 * Устройство -> «Сторонние приложения»: install a .rpk from a file on the bound Vela band, and remove apps.
 * 3.60 has no debug page any more (XmsManager.gotoDebugPage is gone), but the app store's own task does the same
 * transfer: the file is put where the store keeps a downloaded app (AppDownloadManager.getAppFilePath) and
 * AppInstallManager.install(pkg, versionCode) runs prepareInstallApp + sendFile(type 4). Removal goes through
 * AppInstallManager.unInstall(pkg, fingerprint) with the fingerprint the band reports for that app.
 */
public class RpkInstallActivity extends Activity {
    private static final int REQUEST_PICK = 11;
    private static final long INSTALL_TIMEOUT_MS = 3 * 60 * 1000;
    private static final String TAG = "MOD";

    // AppStatusInfo statuses of 3.60
    static final int WAITING_TRANSMISSION = 7;
    static final int ON_TRANSMISSION = 8;
    static final int TRANSMIT_FAILED = 9;
    static final int INSTALLING = 10;
    static final int INSTALL_SUCCESS = 11;
    static final int INSTALL_FAILED = 12;
    static final int WAITING_UNINSTALL = 13;
    static final int UNINSTALLING = 14;
    static final int UNINSTALL_SUCCESS = 15;
    static final int UNINSTALL_FAILED = 16;

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private TextView deviceText;
    private TextView fileText;
    private TextView status;
    private Button pick;
    private Button install;
    private Button showApps;
    private LinearLayout appList;
    private File picked;
    private RpkFile rpk;
    private WearableDeviceModel device;
    private String watchedPackage;
    private Function1<AppStatusInfo, Unit> listener;
    private boolean busy;
    private boolean destroyed;

    /** com.markus.a.handleDebugMenu («Сторонние приложения» in the mod menu): opens this screen. */
    public static boolean open(Activity from) {
        if (from == null) {
            return false;
        }
        from.startActivity(new Intent(from, RpkInstallActivity.class));
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Сторонние приложения");
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        content.setPadding(pad, pad, pad, pad);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);
        setContentView(scroll);

        deviceText = text("");
        deviceText.setTypeface(null, Typeface.BOLD);
        content.addView(deviceText);
        content.addView(note("Приложения для браслета (.rpk) ставятся из файла тем же способом, что и из магазина Xiaomi. "
                + "Ставьте только файлы из источников, которым доверяете."));
        pick = button("Выбрать файл .rpk", v -> pickFile());
        content.addView(pick);
        fileText = text("");
        fileText.setPadding(0, dp(8), 0, dp(8));
        content.addView(fileText);
        install = button("Установить на браслет", v -> confirmInstall());
        install.setEnabled(false);
        content.addView(install);
        status = text("");
        status.setPadding(0, dp(8), 0, dp(8));
        content.addView(status);
        showApps = button("Приложения на браслете", v -> loadApps());
        content.addView(showApps);
        appList = new LinearLayout(this);
        appList.setOrientation(LinearLayout.VERTICAL);
        content.addView(appList);
        content.addView(button("Закрыть", v -> finish()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDevice();
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        stopWatching();
        main.removeCallbacksAndMessages(null);
        io.shutdownNow();
        super.onDestroy();
    }

    // ---------------------------------------------------------------- device

    /** The reason the screen cannot install, or null when the band is ready. */
    private String checkDevice() {
        String reason = null;
        device = null;
        try {
            DeviceManager dm = DeviceManagerExtKt.getInstance(WearableDeviceManager.Companion);
            DeviceModel m = dm == null ? null : dm.getCurrentDeviceModel();
            if (m == null) {
                reason = "Браслет не привязан.";
            } else if (DeviceModelExtKt.isWearOS(m)) {
                reason = "Это часы с Wear OS: приложения на них ставятся через Google Play на самих часах, "
                        + "из файла .rpk их поставить нельзя.";
            } else if (!(m instanceof WearableDeviceModel)
                    || !com.xiaomi.xms.wearable.extensions.DeviceModelExtKt.isSupportThirdPartyApp((WearableDeviceModel) m)) {
                reason = "Это устройство не поддерживает сторонние приложения.";
            } else if (!m.isDeviceConnected()) {
                device = (WearableDeviceModel) m;
                reason = "Браслет не подключён. Подключите его и вернитесь на этот экран.";
            } else {
                device = (WearableDeviceModel) m;
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "rpk: device not readable", e);
            reason = "Не удалось узнать, какой браслет подключён.";
        }
        boolean ready = reason == null;
        deviceText.setText(ready ? "Браслет подключён и поддерживает сторонние приложения." : reason);
        pick.setEnabled(ready && !busy);
        showApps.setEnabled(ready && !busy);
        install.setEnabled(ready && !busy && rpk != null);
        return reason;
    }

    // ---------------------------------------------------------------- file

    private void pickFile() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(i, REQUEST_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_PICK) {
            return;
        }
        final Uri uri = resultCode == RESULT_OK && data != null ? data.getData() : null;
        if (uri == null) {
            return;
        }
        rpk = null;
        install.setEnabled(false);
        fileText.setText("Проверяю файл…");
        io.execute(() -> {
            try {
                File dir = new File(getCacheDir(), "markus-rpk");
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    throw new IOException("временная папка не создана");
                }
                File f = new File(dir, "picked.rpk");
                try (InputStream in = getContentResolver().openInputStream(uri); OutputStream out = new FileOutputStream(f)) {
                    if (in == null) {
                        throw new IOException("файл не открылся");
                    }
                    copy(in, out, RpkFile.MAX_SIZE + 1);
                }
                RpkFile r = RpkFile.read(f);
                Log.i(TAG, "rpk: file checked, versionCode " + r.versionCode + ", " + r.size + " bytes");
                main.post(() -> {
                    picked = f;
                    rpk = r;
                    fileText.setText("«" + r.name + "»\nПакет: " + r.packageName + "\nВерсия: " + r.versionName + " ("
                            + r.versionCode + ")\nРазмер: " + kb(r.size));
                    checkDevice();
                });
            } catch (RpkFile.Problem p) {
                Log.i(TAG, "rpk: file rejected: " + p.getMessage());
                main.post(() -> fileText.setText("Файл не подходит: " + p.getMessage()));
            } catch (IOException | RuntimeException e) {
                Log.w(TAG, "rpk: file not copied", e);
                main.post(() -> fileText.setText("Файл не прочитан: " + e.getMessage()));
            }
        });
    }

    // ---------------------------------------------------------------- install

    private void confirmInstall() {
        if (rpk == null || checkDevice() != null) {
            return;
        }
        final RpkFile r = rpk;
        new AlertDialog.Builder(this)
                .setTitle("Установить на браслет?")
                .setMessage("«" + r.name + "»\n" + r.packageName + ", версия " + r.versionName + " (" + r.versionCode
                        + ")\n" + kb(r.size) + "\n\nФайл будет передан на браслет. Не отключайте браслет до конца.")
                .setPositiveButton("Установить", (d, w) -> checkStoreCopy(r))
                .setNegativeButton("Отмена", null)
                .show();
    }

    /** A store install may have left its own <pkg>.rpk: ask before replacing it, with both versions. */
    private void checkStoreCopy(final RpkFile r) {
        final File target;
        try {
            target = new File(AppDownloadManager.Companion.getINSTANCE().getAppFilePath(r.packageName));
        } catch (RuntimeException e) {
            Log.w(TAG, "rpk: store path not known", e);
            status.setText("Не удалось узнать папку магазина: " + e.getMessage());
            return;
        }
        if (!target.exists()) {
            startInstall(r, target);
            return;
        }
        io.execute(() -> {
            String old;
            try {
                RpkFile o = RpkFile.read(target);
                old = "версия " + o.versionName + " (" + o.versionCode + ")";
            } catch (RpkFile.Problem p) {
                old = "версию прочитать не удалось";
            }
            final String oldText = old;
            main.post(() -> new AlertDialog.Builder(this)
                    .setTitle("Заменить файл магазина?")
                    .setMessage("В папке магазина Xiaomi уже есть файл этого приложения: " + oldText + ".\n"
                            + "Заменить его выбранным файлом: версия " + r.versionName + " (" + r.versionCode + ")?")
                    .setPositiveButton("Заменить", (d, w) -> startInstall(r, target))
                    .setNegativeButton("Отмена", null)
                    .show());
        });
    }

    private void startInstall(final RpkFile r, final File target) {
        if (checkDevice() != null) {
            return;
        }
        busy = true;
        checkDevice();
        status.setText("Готовлю установку…");
        final File src = picked;
        io.execute(() -> {
            try {
                File parent = target.getParentFile();
                if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("папка магазина не создана");
                }
                try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(target)) {
                    copy(in, out, RpkFile.MAX_SIZE + 1);
                }
                main.post(() -> {
                    watch(r.packageName);
                    Log.i(TAG, "rpk: install started through the store task, versionCode " + r.versionCode);
                    AppInstallManager.Companion.getINSTANCE().install(r.packageName, r.versionCode);
                    status.setText("Ждёт передачи на браслет…");
                    main.postDelayed(timeout, INSTALL_TIMEOUT_MS);
                });
            } catch (IOException | RuntimeException e) {
                Log.w(TAG, "rpk: install not started", e);
                main.post(() -> finishWith("Установка не началась: " + e.getMessage()));
            }
        });
    }

    private final Runnable timeout = () -> finishWith("Браслет не ответил за " + INSTALL_TIMEOUT_MS / 60000
            + " минуты. Проверьте, что он подключён и рядом, и попробуйте ещё раз.");

    private void watch(String pkg) {
        stopWatching();
        watchedPackage = pkg;
        listener = new Function1<AppStatusInfo, Unit>() {
            @Override
            public Unit invoke(AppStatusInfo info) {
                final int s = info == null ? -1 : info.getStatus();
                final String err = info == null ? null : info.getErrMsg();
                main.post(() -> onStatus(s, err));
                return Unit.INSTANCE;
            }
        };
        AppStatusManager.Companion.getINSTANCE().addStatusListener(pkg, listener);
    }

    private void stopWatching() {
        if (listener != null && watchedPackage != null) {
            try {
                AppStatusManager.Companion.getINSTANCE().removeStatusListener(watchedPackage, listener);
            } catch (RuntimeException e) {
                Log.w(TAG, "rpk: status listener not removed", e);
            }
        }
        listener = null;
        watchedPackage = null;
    }

    private void onStatus(int s, String err) {
        if (destroyed) {
            return;
        }
        Log.i(TAG, "rpk: store status " + s);
        String e = err == null || err.isEmpty() ? "" : " (" + err + ")";
        switch (s) {
            case WAITING_TRANSMISSION:
                status.setText("Ждёт передачи на браслет…");
                break;
            case ON_TRANSMISSION:
                status.setText("Передаётся на браслет…");
                break;
            case INSTALLING:
                status.setText("Устанавливается на браслете…");
                break;
            case INSTALL_SUCCESS:
                finishWith("Установлено на браслет.");
                break;
            case TRANSMIT_FAILED:
                finishWith("Не удалось передать на браслет" + e + ".");
                break;
            case INSTALL_FAILED:
                finishWith("Браслет не установил приложение" + e + ".");
                break;
            case WAITING_UNINSTALL:
            case UNINSTALLING:
                status.setText("Удаляется с браслета…");
                break;
            case UNINSTALL_SUCCESS:
                finishWith("Удалено с браслета.");
                loadApps();
                break;
            case UNINSTALL_FAILED:
                finishWith("Не удалось удалить" + e + ".");
                break;
            default:
                // other store statuses (download etc.) do not concern a file install
                break;
        }
    }

    private void finishWith(String msg) {
        main.removeCallbacks(timeout);
        stopWatching();
        busy = false;
        status.setText(msg);
        checkDevice();
    }

    // ---------------------------------------------------------------- apps on the band

    private void loadApps() {
        if (device == null || checkDevice() != null) {
            return;
        }
        appList.removeAllViews();
        appList.addView(note("Спрашиваю у браслета…"));
        try {
            com.xiaomi.xms.wearable.extensions.DeviceModelExtKt.getInstalledWatchApp(device,
                    new Function2<List<WatchAppItemEntity>, Integer, Unit>() {
                        @Override
                        public Unit invoke(List<WatchAppItemEntity> apps, Integer code) {
                            main.post(() -> showApps(apps, code));
                            return Unit.INSTANCE;
                        }
                    });
        } catch (RuntimeException e) {
            Log.w(TAG, "rpk: app list not requested", e);
            appList.removeAllViews();
            appList.addView(note("Не удалось запросить список: " + e.getMessage()));
        }
    }

    private void showApps(List<WatchAppItemEntity> apps, Integer code) {
        if (destroyed) {
            return;
        }
        appList.removeAllViews();
        Log.i(TAG, "rpk: band app list, code " + code + ", apps " + (apps == null ? 0 : apps.size()));
        if (apps == null || apps.isEmpty()) {
            appList.addView(note(code != null && code != 0 ? "Браслет не ответил (код " + code + ")."
                    : "Сторонних приложений на браслете нет."));
            return;
        }
        for (final WatchAppItemEntity a : apps) {
            String title = (a.getAppName() == null || a.getAppName().isEmpty() ? a.getPackageName() : a.getAppName())
                    + "\n" + a.getPackageName() + ", версия " + a.getVersionCode();
            appList.addView(text(title));
            if (a.getCanRemove() && a.getFingerprint() != null) {
                appList.addView(button("Удалить «" + (a.getAppName() == null ? a.getPackageName() : a.getAppName()) + "»",
                        v -> confirmRemove(a)));
            } else {
                appList.addView(note("Системное приложение браслета, удалить нельзя."));
            }
        }
    }

    private void confirmRemove(final WatchAppItemEntity a) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить с браслета?")
                .setMessage("«" + (a.getAppName() == null ? a.getPackageName() : a.getAppName()) + "»\n" + a.getPackageName()
                        + "\n\nПриложение и его данные на браслете будут удалены.")
                .setPositiveButton("Удалить", (d, w) -> {
                    if (checkDevice() != null) {
                        return;
                    }
                    busy = true;
                    checkDevice();
                    watch(a.getPackageName());
                    Log.i(TAG, "rpk: uninstall started through the store task");
                    AppInstallManager.Companion.getINSTANCE().unInstall(a.getPackageName(), a.getFingerprint());
                    status.setText("Удаляется с браслета…");
                    main.postDelayed(timeout, INSTALL_TIMEOUT_MS);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ---------------------------------------------------------------- helpers

    private static void copy(InputStream in, OutputStream out, long limit) throws IOException {
        byte[] buf = new byte[64 * 1024];
        long total = 0;
        int r;
        while ((r = in.read(buf)) != -1) {
            total += r;
            if (total > limit) {
                throw new IOException("файл слишком большой");
            }
            out.write(buf, 0, r);
        }
    }

    private static String kb(long bytes) {
        return bytes < 1024 * 1024 ? (bytes + 1023) / 1024 + " КБ"
                : String.format(Locale.US, "%.1f МБ", bytes / 1024.0 / 1024.0);
    }

    private TextView text(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        t.setTextIsSelectable(true);
        return t;
    }

    private TextView note(String s) {
        TextView t = text(s);
        t.setAlpha(0.7f);
        return t;
    }

    private Button button(String label, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setOnClickListener(l);
        return b;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
