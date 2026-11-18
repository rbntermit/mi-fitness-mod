package com.markus.logexport;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Export of a log the person can actually reach: «Сохранить как…» (ACTION_CREATE_DOCUMENT, the system picks the
 * place) and «Поделиться» (ACTION_SEND through the app's own FileProvider, external-files-path "Download/").
 * No storage permissions, no new provider.
 *
 * MODE_BAND: the band log requested by «Сбор журналов» arrives in Android/data/.../log/devicelog within minutes;
 * the screen waits until files are there and stop growing, then packs them into one ZIP (unchanged, binary).
 * MODE_APP: logcat of the app's own processes for the last minutes (an app without READ_LOGS only sees its own
 * lines anyway), masked by LogMasker.
 */
public class LogExportActivity extends Activity {
    static final String EXTRA_MODE = "markus_log_mode";
    static final String EXTRA_DIR = "markus_log_dir";
    static final String MODE_BAND = "band";
    static final String MODE_APP = "app";
    static final int APP_LOG_MINUTES = 15;
    private static final int REQUEST_SAVE = 7;
    private static final long POLL_MS = 3000;
    private static final int STABLE_POLLS = 3;
    private static final long BAND_WAIT_MS = 12 * 60 * 1000;
    private static final long KEEP_MS = 24L * 60 * 60 * 1000;
    private static final String TAG = "MOD";

    static final String PRIVACY = "Журнал может содержать личные данные: названия приложений, тексты уведомлений, "
            + "имена контактов, места и время. Мод скрывает токены входа, идентификаторы аккаунта и устройства, "
            + "серийные номера, MAC-адреса, e-mail и телефоны, но не всё. Отправляйте журнал только в личные "
            + "сообщения тому, кто помогает разобраться, и не выкладывайте его в общую тему.";

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private TextView status;
    private Button save;
    private Button share;
    private Button record;
    private Button snapshot;
    private File result;
    private String mime;
    private String bandDir;
    private long started;
    private long lastSize = -1;
    private int stable;
    private boolean finished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean band = MODE_BAND.equals(getIntent().getStringExtra(EXTRA_MODE));
        setTitle(band ? "Журнал браслета" : "Журнал приложения");
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        content.setPadding(pad, pad, pad, pad);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);
        setContentView(scroll);

        TextView warn = text(PRIVACY);
        warn.setTypeface(null, Typeface.BOLD);
        content.addView(warn);
        status = text(band ? "Ждём журнал от браслета…"
                : "Чтобы поймать ошибку целиком, нажмите «Начать запись», повторите то, что не работает, вернитесь сюда "
                        + "и нажмите «Остановить и сохранить». На некоторых телефонах (OnePlus и другие) система хранит "
                        + "только последнюю минуту журнала, поэтому запись надёжнее.");
        status.setPadding(0, dp(16), 0, dp(16));
        content.addView(status);
        if (!band) {
            record = button("Начать запись", v -> toggleRecording());
            content.addView(record);
            snapshot = button("Последние " + APP_LOG_MINUTES + " минут", v -> {
                snapshot.setEnabled(false);
                status.setText("Собираю журнал…");
                io.execute(this::collectAppLog);
            });
            content.addView(snapshot);
        }
        save = button("Сохранить как…", v -> saveAs());
        share = button("Поделиться", v -> confirmShare());
        save.setEnabled(false);
        share.setEnabled(false);
        content.addView(save);
        content.addView(share);
        content.addView(button("Закрыть", v -> finish()));

        io.execute(this::cleanOld);
        started = System.currentTimeMillis();
        if (band) {
            bandDir = getIntent().getStringExtra(EXTRA_DIR);
            mime = "application/zip";
            pollBand();
        } else {
            mime = "text/plain";
            showRecorder();
        }
    }

    // ---------------------------------------------------------------- recording

    private void toggleRecording() {
        LogRecorder r = LogRecorder.current();
        if (r != null && !r.finished) {
            record.setEnabled(false);
            status.setText("Останавливаю запись…");
            io.execute(() -> {
                r.stop();
                main.post(this::showRecorder);
            });
            return;
        }
        io.execute(() -> {
            try {
                File out = new File(exportDir(), "mi-fitness-app-log-" + stamp() + ".txt");
                LogRecorder.start(this, out, header("@PERIOD@", ownPids().size()));
                main.post(this::showRecorder);
            } catch (IOException | RuntimeException e) {
                Log.w(TAG, "recording not started", e);
                main.post(() -> failed("Не удалось начать запись: " + e.getMessage()));
            }
        });
    }

    /** Shows the state of the process-wide recording; refreshes every second while it runs. */
    private void showRecorder() {
        if (finished) {
            return;
        }
        LogRecorder r = LogRecorder.current();
        if (r == null) {
            return;
        }
        long s = r.elapsedMs() / 1000;
        String time = s / 60 + ":" + String.format(Locale.US, "%02d", s % 60);
        if (!r.finished) {
            record.setText("Остановить и сохранить");
            record.setEnabled(true);
            snapshot.setEnabled(false);
            status.setText("Идёт запись: " + time + ", строк: " + r.kept + ".\nПовторите то, что не работает "
                    + "(можно уйти с этого экрана), затем вернитесь сюда и нажмите «Остановить и сохранить». "
                    + "Запись остановится сама через " + LogRecorder.MAX_MS / 60000 + " минут.");
            main.postDelayed(this::showRecorder, 1000);
            return;
        }
        record.setText("Начать запись");
        record.setEnabled(true);
        snapshot.setEnabled(true);
        if (r.error != null) {
            failed("Запись прервалась: " + r.error);
        } else if (r.kept == 0) {
            failed("За время записи (" + time + ") приложение ничего не записало в журнал.");
        } else {
            ready(r.file, "Запись остановлена: " + time + ", строк: " + r.kept + ", " + kb(r.file.length())
                    + (r.truncated ? " (остановлена по ограничению)" : "") + ".");
        }
    }

    /** Header of a text log: mod version, band, phone, Android. @PERIOD@ is filled in by the recorder. */
    private String header(String period, int processes) {
        String band = null;
        try {
            com.xiaomi.fitness.device.manager.DeviceManager dm =
                    com.xiaomi.fitness.device.manager.export.DeviceManagerExtKt.getInstance(
                            com.xiaomi.fitness.device.manager.export.WearableDeviceManager.Companion);
            com.xiaomi.fitness.device.manager.DeviceModel m = dm == null ? null : dm.getCurrentDeviceModel();
            com.xiaomi.fitness.device.manager.bean.DeviceInfo info = m == null ? null : m.getDeviceInfo();
            if (info != null) {
                String fw = info.getFirmwareVersion();
                band = info.getModel() + (fw == null || fw.isEmpty() ? "" : ", прошивка " + fw);
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "band not readable for the log header", e);
        }
        String phone = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL + ", Android "
                + android.os.Build.VERSION.RELEASE + " (API " + android.os.Build.VERSION.SDK_INT + ")";
        return LogHeader.build(versionText(), band, phone, period, processes);
    }

    @Override
    protected void onDestroy() {
        finished = true;
        main.removeCallbacksAndMessages(null);
        io.shutdownNow();
        super.onDestroy();
    }

    // ---------------------------------------------------------------- band log

    private void pollBand() {
        if (finished) {
            return;
        }
        File dir = bandDir == null ? null : new File(bandDir);
        long size = dir == null ? 0 : totalSize(dir);
        stable = size > 0 && size == lastSize ? stable + 1 : 0;
        lastSize = size;
        long elapsed = System.currentTimeMillis() - started;
        if (size > 0 && (stable >= STABLE_POLLS || elapsed > BAND_WAIT_MS)) {
            status.setText("Журнал получен (" + kb(size) + "), упаковываю…");
            final File d = dir;
            io.execute(() -> packBand(d));
            return;
        }
        if (elapsed > BAND_WAIT_MS) {
            status.setText("Журнал от браслета не пришёл за " + BAND_WAIT_MS / 60000 + " минут. Проверьте, что браслет "
                    + "подключён, и запустите «Сбор журналов» ещё раз.");
            return;
        }
        status.setText("Ждём журнал от браслета: " + (size > 0 ? "получено " + kb(size) : "пока ничего не пришло")
                + ", прошло " + elapsed / 60000 + ":" + String.format(Locale.US, "%02d", elapsed / 1000 % 60)
                + ".\nОбычно это занимает до 10 минут. Браслет должен оставаться рядом и подключённым.");
        main.postDelayed(this::pollBand, POLL_MS);
    }

    private void packBand(File dir) {
        try {
            File out = new File(exportDir(), "mi-fitness-band-log-" + stamp() + ".zip");
            int n;
            try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(out))) {
                n = addTree(zip, dir, "");
            }
            final int files = n;
            main.post(() -> ready(out, "Готово: " + files + " файл(ов) журнала браслета, " + kb(out.length())
                    + " в ZIP. Файлы переданы без изменений."));
        } catch (IOException | RuntimeException e) {
            Log.w(TAG, "band log not packed", e);
            main.post(() -> failed("Не удалось упаковать журнал: " + e.getMessage()));
        }
    }

    private static int addTree(ZipOutputStream zip, File f, String prefix) throws IOException {
        File[] list = f.listFiles();
        if (list == null) {
            return 0;
        }
        int n = 0;
        byte[] buf = new byte[64 * 1024];
        for (File c : list) {
            if (c.isDirectory()) {
                n += addTree(zip, c, prefix + c.getName() + "/");
            } else if (c.isFile()) {
                zip.putNextEntry(new ZipEntry(LogLines.zipEntryName(prefix + c.getName())));
                try (InputStream in = new FileInputStream(c)) {
                    int r;
                    while ((r = in.read(buf)) != -1) {
                        zip.write(buf, 0, r);
                    }
                }
                zip.closeEntry();
                n++;
            }
        }
        return n;
    }

    private static long totalSize(File f) {
        if (f.isFile()) {
            return f.length();
        }
        File[] list = f.listFiles();
        long s = 0;
        if (list != null) {
            for (File c : list) {
                s += totalSize(c);
            }
        }
        return s;
    }

    // ---------------------------------------------------------------- app log

    private void collectAppLog() {
        Set<Integer> pids = ownPids();
        long since = System.currentTimeMillis() - APP_LOG_MINUTES * 60_000L;
        String sinceText = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US).format(new Date(since));
        java.lang.Process p = null;
        try {
            File out = new File(exportDir(), "mi-fitness-app-log-" + stamp() + ".txt");
            String wearSnapshot = com.markus.facefix.WatchCompat.logSnapshot();
            // without READ_LOGS logcat shows only this app's lines; the pid filter keeps exactly its processes
            p = new ProcessBuilder("logcat", "-d", "-v", "threadtime", "-T", sinceText).redirectErrorStream(true).start();
            LogLines.Result r;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
                    Writer w = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8)) {
                w.write(header("последние " + APP_LOG_MINUTES + " минут, с " + sinceText, pids.size()));
                w.write("# Диагностика Wear OS на момент сохранения: " + wearSnapshot + "\n");
                r = LogLines.filter(in, w, pids, LogLines.MAX_CHARS);
            }
            final LogLines.Result res = r;
            main.post(() -> {
                if (snapshot != null) {
                    snapshot.setEnabled(true);
                }
                if (res.kept == 0) {
                    ready(out, "Сохранено текущее состояние Wear OS. Строк logcat за последние "
                            + APP_LOG_MINUTES + " минут нет. Чтобы записать ошибку, используйте «Начать запись».");
                } else {
                    ready(out, "Готово: " + res.kept + " строк за последние " + APP_LOG_MINUTES + " минут, "
                            + kb(out.length()) + (res.truncated ? " (журнал обрезан по размеру)" : "") + ".");
                }
            });
        } catch (IOException | RuntimeException e) {
            Log.w(TAG, "app log not collected", e);
            main.post(() -> failed("Не удалось собрать журнал: " + e.getMessage()));
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }

    private Set<Integer> ownPids() {
        Set<Integer> pids = new HashSet<>();
        pids.add(Process.myPid());
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = am == null ? null : am.getRunningAppProcesses();
        if (list != null) {
            int uid = Process.myUid();
            for (ActivityManager.RunningAppProcessInfo i : list) {
                if (i.uid == uid) {
                    pids.add(i.pid);
                }
            }
        }
        return pids;
    }

    private String versionText() {
        String v = "";
        try {
            v = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            // the version is only a header line
        }
        int id = getResources().getIdentifier("markus_mod_build", "string", getPackageName());
        return v + (id == 0 ? "" : ", сборка мода " + getString(id));
    }

    // ---------------------------------------------------------------- result

    private void ready(File f, String msg) {
        if (finished) {
            return;
        }
        result = f;
        status.setText(msg + "\nФайл: " + f.getName());
        save.setEnabled(true);
        share.setEnabled(true);
    }

    private void failed(String msg) {
        if (!finished) {
            status.setText(msg);
        }
    }

    private void saveAs() {
        if (result == null) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType(mime)
                .putExtra(Intent.EXTRA_TITLE, result.getName());
        startActivityForResult(i, REQUEST_SAVE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_SAVE) {
            return;
        }
        final Uri uri = resultCode == RESULT_OK && data != null ? data.getData() : null;
        if (uri == null || result == null) {
            return;
        }
        final File src = result;
        io.execute(() -> {
            try (InputStream in = new FileInputStream(src); OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (out == null) {
                    throw new IOException("файл для записи не открылся");
                }
                byte[] buf = new byte[64 * 1024];
                int r;
                while ((r = in.read(buf)) != -1) {
                    out.write(buf, 0, r);
                }
                main.post(() -> Toast.makeText(this, "Журнал сохранён", Toast.LENGTH_SHORT).show());
            } catch (IOException | RuntimeException e) {
                Log.w(TAG, "log not saved", e);
                main.post(() -> Toast.makeText(this, "Не удалось сохранить: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void confirmShare() {
        if (result == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Поделиться журналом?")
                .setMessage(PRIVACY)
                .setPositiveButton("Поделиться", (d, w) -> share())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void share() {
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", result);
            Intent s = new Intent(Intent.ACTION_SEND)
                    .setType(mime)
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .putExtra(Intent.EXTRA_SUBJECT, result.getName())
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            s.setClipData(ClipData.newRawUri(result.getName(), uri));
            startActivity(Intent.createChooser(s, "Поделиться журналом"));
        } catch (RuntimeException e) {
            Log.w(TAG, "log not shared", e);
            Toast.makeText(this, "Не удалось поделиться: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ---------------------------------------------------------------- files

    /** Inside external-files-path "Download/" of the app's FileProvider (res/xml/provider_paths.xml). */
    private File exportDir() throws IOException {
        File base = getExternalFilesDir("Download");
        if (base == null) {
            throw new IOException("внешняя папка приложения недоступна");
        }
        File d = new File(base, "markus-logs");
        if (!d.isDirectory() && !d.mkdirs()) {
            throw new IOException("папка для журнала не создана");
        }
        return d;
    }

    /** Exports are copies for one-off sending; anything older than a day is removed. */
    private void cleanOld() {
        try {
            File[] list = exportDir().listFiles();
            long now = System.currentTimeMillis();
            if (list != null) {
                for (File f : list) {
                    if (f.isFile() && now - f.lastModified() > KEEP_MS && !f.delete()) {
                        Log.w(TAG, "old export not deleted");
                    }
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "old exports not cleaned", e);
        }
    }

    private static String stamp() {
        return new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
    }

    private static String kb(long bytes) {
        return bytes < 1024 ? bytes + " Б" : bytes / 1024 + " КБ";
    }

    private TextView text(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        t.setTextIsSelectable(true);
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
