package com.markus.fw;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Устройство -> Ещё -> Прошивки: a list of firmware copies kept by the app, a diagnostic block, and one screen per
 * firmware with all checks. Files come from the system file picker (no storage permissions) and are copied unchanged.
 * Installation is manual only and needs a separate confirmation; it is off while {@link FwGate#INSTALL_ENABLED} is false.
 */
public class FirmwareActivity extends Activity {
    private static final int REQUEST_PICK = 1;
    private static final String PREFS = "markus_firmware";
    private static final String KEY_CROSSGRADE = "allow_crossgrade_to_global";

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Map<String, FwPackage.Info> infoCache = new HashMap<>();
    private FwLibrary library;
    private DeviceBridge bridge;
    private DeviceSnapshot snapshot;
    private LinearLayout content;
    /** Record shown right now; null while the list is shown. */
    private String openSha;
    private boolean installing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Прошивки");
        library = new FwLibrary(new File(getFilesDir(), "firmware"));
        bridge = DeviceBridge.Holder.get();
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        content.setPadding(pad, pad, pad, pad);
        scroll.addView(content);
        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // statuses depend on the band and are recomputed every time the screen is shown
        if (openSha != null) {
            showRecord(openSha);
        } else {
            showList();
        }
    }

    @Override
    protected void onDestroy() {
        io.shutdownNow();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (openSha != null && !installing) {
            showList();
        } else if (!installing) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Идёт передача прошивки, дождитесь окончания", Toast.LENGTH_LONG).show();
        }
    }

    // ---------------------------------------------------------------- list

    private void showList() {
        openSha = null;
        content.removeAllViews();
        header("Прошивки");
        note("Файлы хранятся внутри приложения. Удаление записи не меняет ничего на браслете.");
        final TextView diag = mono("Читаю данные браслета…");
        content.addView(diag);
        LinearLayout row = new LinearLayout(this);
        row.addView(button("Обновить данные", v -> showList()));
        row.addView(button("Скопировать", v -> copyDiagnostics()));
        content.addView(row);
        content.addView(button("Добавить прошивку", v -> pick()));
        content.addView(crossgradeSwitch());
        final List<FwLibrary.Record> records = library.list();
        if (records.isEmpty()) {
            note("Список пуст.");
        }
        final Map<String, TextView> statusViews = new HashMap<>();
        for (final FwLibrary.Record r : records) {
            TextView t = text(r.originalName + "\nпроверяется…");
            t.setTextIsSelectable(false);
            t.setPadding(0, dp(12), 0, dp(12));
            t.setOnClickListener(v -> showRecord(r.sha256));
            content.addView(t);
            statusViews.put(r.sha256, t);
        }
        bridge.readSnapshot(s -> {
            snapshot = s;
            if (openSha != null) {
                return;
            }
            diag.setText(diagnosticsText(s));
            for (final FwLibrary.Record r : records) {
                io.execute(() -> {
                    FwPackage.Info info = cachedInfo(r);
                    FwPolicy.Verdict v = FwPolicy.evaluate(FwName.parse(r.originalName), info, s, true,
                            FwGate.INSTALL_ENABLED, crossgradeAllowed());
                    main.post(() -> {
                        TextView t = statusViews.get(r.sha256);
                        if (t != null && openSha == null) {
                            t.setText(r.originalName + "\n" + summary(info) + "\n" + (v.allowed()
                                    ? "можно установить" : "нельзя: " + v.firstProblem()) + stateLine(r));
                        }
                    });
                });
            }
        });
    }

    // ---------------------------------------------------------------- crossgrade switch

    /** Off by default; read again before every check and right before sending. */
    private boolean crossgradeAllowed() {
        return getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_CROSSGRADE, false);
    }

    private View crossgradeSwitch() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, dp(12), 0, dp(4));
        Switch sw = new Switch(this);
        sw.setText("Разрешить переход на глобальную прошивку");
        sw.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        sw.setChecked(crossgradeAllowed());
        sw.setOnCheckedChangeListener((b, on) -> {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_CROSSGRADE, on).apply();
            showList();
        });
        box.addView(sw);
        TextView hint = text("Только Smart Band 11 (q66), привязанный как cn или tc, на китайской прошивке 4.100.x, и только "
                + "полный файл q66gl. Возврат на китайскую через мод невозможен.");
        hint.setAlpha(0.7f);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        box.addView(hint);
        return box;
    }

    private String diagnosticsText(DeviceSnapshot s) {
        return "Диагностика (только чтение)\n" + s.diagnostics()
                + (FwGate.INSTALL_ENABLED ? "" : "Установка выключена до подтверждения этих данных.\n");
    }

    private void copyDiagnostics() {
        if (snapshot == null) {
            return;
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("Диагностика", diagnosticsText(snapshot)));
        Toast.makeText(this, "Скопировано", Toast.LENGTH_SHORT).show();
    }

    private FwPackage.Info cachedInfo(FwLibrary.Record r) {
        synchronized (infoCache) {
            FwPackage.Info i = infoCache.get(r.sha256);
            if (i == null) {
                i = FwPackage.inspect(library.file(r.sha256));
                infoCache.put(r.sha256, i);
            }
            return i;
        }
    }

    // ---------------------------------------------------------------- adding

    private void pick() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"application/zip", "application/x-zip-compressed",
                "application/octet-stream"});
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
            Toast.makeText(this, "Выбор файла отменён", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = null;
        long size = -1;
        try (Cursor c = getContentResolver().query(uri,
                new String[] {OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                name = c.getString(0);
                size = c.isNull(1) ? -1 : c.getLong(1);
            }
        }
        final String displayName = name == null ? uri.getLastPathSegment() : name;
        final long declared = size;
        final File dir = new File(getFilesDir(), "firmware");
        progress("Копирую и проверяю «" + displayName + "»…");
        io.execute(() -> {
            FwLibrary.AddResult res;
            try {
                InputStream in = getContentResolver().openInputStream(uri);
                if (in == null) {
                    throw new FileNotFoundException("поток не открылся");
                }
                dir.mkdirs();
                res = library.add(in, displayName, declared, dir.getUsableSpace(), System.currentTimeMillis());
            } catch (IOException | SecurityException e) {
                res = null;
                final String msg = e.getMessage();
                main.post(() -> alert("Файл не добавлен", "Не удалось открыть файл: " + msg, this::showList));
            }
            final FwLibrary.AddResult r = res;
            if (r == null) {
                return;
            }
            main.post(() -> onAdded(r));
        });
    }

    private void onAdded(FwLibrary.AddResult r) {
        switch (r.status) {
            case ADDED:
                Toast.makeText(this, "Прошивка добавлена", Toast.LENGTH_SHORT).show();
                showRecord(r.record.sha256);
                break;
            case DUPLICATE:
                Toast.makeText(this, "Эта прошивка уже есть в списке", Toast.LENGTH_LONG).show();
                showRecord(r.record.sha256);
                break;
            case NO_SPACE:
                alert("Недостаточно места", r.error, this::showList);
                break;
            case CANCELLED:
                showList();
                break;
            default:
                alert("Файл не добавлен", r.error, this::showList);
        }
    }

    // ---------------------------------------------------------------- one firmware

    private void showRecord(final String sha) {
        openSha = sha;
        progress("Проверяю файл и читаю данные браслета…");
        io.execute(() -> {
            final FwLibrary.Record r = library.get(sha);
            if (r == null) {
                main.post(this::showList);
                return;
            }
            final boolean intact = library.isIntact(sha);
            final FwPackage.Info info = FwPackage.inspect(library.file(sha));
            synchronized (infoCache) {
                infoCache.put(sha, info);
            }
            main.post(() -> bridge.readSnapshot(s -> {
                snapshot = s;
                if (sha.equals(openSha)) {
                    render(r, info, intact, s);
                }
            }));
        });
    }

    private void render(FwLibrary.Record r, FwPackage.Info info, boolean intact, DeviceSnapshot s) {
        FwLibrary.InstallState before = r.state;
        FwLibrary.reconcile(r, s);
        if (r.state != before) {
            saveQuietly(r);
        }
        FwName name = FwName.parse(r.originalName);
        FwPolicy.Verdict v = FwPolicy.evaluate(name, info, s, intact, FwGate.INSTALL_ENABLED, crossgradeAllowed());
        content.removeAllViews();
        header(r.originalName);
        content.addView(mono("Исходное имя: " + r.originalName
                + "\nРазмер: " + r.size + " байт"
                + "\nSHA-256: " + r.sha256
                + "\nMD5 файла: " + info.md5
                + "\nMD5 из имени: " + (name.md5 == null ? "нет" : name.md5)
                + "\nТип: " + typeText(info)
                + "\nВерсия (ro.build.id): " + nz(info.version)
                + "\nРегион (ro.build.id): " + nz(info.region)
                + "\nМодель по имени: " + nz(name.model)
                + "\nmiwear_model в образе: " + (info.miwearModels.isEmpty() ? "нет" : info.miwearModels)));
        for (String w : v.warnings) {
            TextView t = text("Внимание: " + w);
            t.setTypeface(null, Typeface.BOLD);
            content.addView(t);
        }
        header("Проверки");
        StringBuilder sb = new StringBuilder();
        for (FwPolicy.Check c : v.checks) {
            sb.append(c.ok ? "✓ " : "✗ ").append(c.title);
            if (!TextUtils.isEmpty(c.detail)) {
                sb.append(" — ").append(c.detail);
            }
            sb.append('\n');
        }
        content.addView(text(sb.toString()));
        if (r.state != FwLibrary.InstallState.NONE) {
            header("Установка");
            content.addView(text(stateLine(r).trim()));
        }
        header("Данные браслета");
        content.addView(mono(diagnosticsText(s)));
        Button install = button("Установить", x -> confirmInstall(r));
        install.setEnabled(v.allowed() && !installing);
        content.addView(install);
        if (!v.allowed()) {
            note("Установить нельзя: " + v.firstProblem());
        }
        content.addView(button("Удалить", x -> confirmDelete(r)));
        content.addView(button("К списку", x -> showList()));
    }

    private void confirmDelete(final FwLibrary.Record r) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить прошивку?")
                .setMessage("Будет удалена только копия «" + r.originalName + "» в приложении. На браслете ничего не изменится.")
                .setPositiveButton("Удалить", (d, w) -> io.execute(() -> {
                    library.delete(r.sha256);
                    synchronized (infoCache) {
                        infoCache.remove(r.sha256);
                    }
                    main.post(this::showList);
                }))
                .setNegativeButton("Отмена", null)
                .show();
    }

    /** Recomputes the hash and every check right before sending, then asks for a separate confirmation. */
    private void confirmInstall(final FwLibrary.Record r) {
        progress("Проверяю всё заново перед установкой…");
        io.execute(() -> {
            final boolean intact = library.isIntact(r.sha256);
            final FwPackage.Info info = FwPackage.inspect(library.file(r.sha256));
            main.post(() -> bridge.readSnapshot(s -> {
                FwPolicy.Verdict v = FwPolicy.evaluate(FwName.parse(r.originalName), info, s, intact,
                        FwGate.INSTALL_ENABLED, crossgradeAllowed());
                if (!v.allowed()) {
                    alert("Установка заблокирована", v.firstProblem(), () -> render(r, info, intact, s));
                    return;
                }
                StringBuilder msg = new StringBuilder()
                        .append("Модель: ").append(s.boundModel)
                        .append("\nБраслет сообщает: ").append(s.reportedModel)
                        .append("\nТекущая версия: ").append(s.liveVersion)
                        .append("\nНовая версия: ").append(info.version)
                        .append("\nТип пакета: полный")
                        .append("\nРазмер: ").append(r.size).append(" байт")
                        .append("\nSHA-256: ").append(r.sha256);
                for (String w : v.warnings) {
                    msg.append("\n\nВнимание: ").append(w);
                }
                msg.append("\n\nФайл будет передан на браслет без изменений. Не отключайте браслет до конца.");
                final boolean crossgrade = v.warnings.contains(FwPolicy.W_CROSSGRADE);
                new AlertDialog.Builder(this)
                        .setTitle(crossgrade ? "Переход на глобальную прошивку (1 из 2)" : "Установить прошивку?")
                        .setMessage(msg)
                        .setPositiveButton(crossgrade ? "Далее" : "Установить", (d, w) -> {
                            if (crossgrade) {
                                confirmCrossgrade(r, info, intact, s);
                            } else {
                                startInstall(r, info);
                            }
                        })
                        .setNegativeButton("Отмена", (d, w) -> render(r, info, intact, s))
                        .setCancelable(false)
                        .show();
            }));
        });
    }

    /** Second confirmation of the crossgrade: the risk text alone, the switch read once more. */
    private void confirmCrossgrade(final FwLibrary.Record r, final FwPackage.Info info, final boolean intact,
            final DeviceSnapshot s) {
        new AlertDialog.Builder(this)
                .setTitle("Переход на глобальную прошивку (2 из 2)")
                .setMessage(FwPolicy.W_CROSSGRADE + ".")
                .setPositiveButton("Перейти на глобальную", (d, w) -> {
                    if (!crossgradeAllowed()) {
                        alert("Установка заблокирована", "Переключатель «Разрешить переход на глобальную прошивку» "
                                + "выключен", () -> render(r, info, intact, s));
                        return;
                    }
                    startInstall(r, info);
                })
                .setNegativeButton("Отмена", (d, w) -> render(r, info, intact, s))
                .setCancelable(false)
                .show();
    }

    private void startInstall(final FwLibrary.Record r, final FwPackage.Info info) {
        if (!FwGate.INSTALL_ENABLED) {
            return;
        }
        installing = true;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        r.state = FwLibrary.InstallState.TRANSFERRING;
        r.targetVersion = info.version;
        r.stateNote = null;
        saveQuietly(r);
        final TextView status = progress("Передача прошивки " + info.version + "…");
        bridge.install(library.file(r.sha256).getAbsolutePath(), info.version, info.md5, (int) r.size,
                new DeviceBridge.InstallListener() {
                    @Override
                    public void onProgress(int percent) {
                        status.setText("Передача прошивки " + info.version + ": " + percent + " %");
                    }

                    @Override
                    public void onTransferred() {
                        finishInstall(r, FwLibrary.InstallState.TRANSFERRED,
                                "файл передан; успех будет показан, когда браслет перезагрузится, подключится "
                                        + "и сообщит версию " + info.version);
                    }

                    @Override
                    public void onFailed(String reason) {
                        finishInstall(r, FwLibrary.InstallState.FAILED, reason);
                    }
                });
    }

    private void finishInstall(FwLibrary.Record r, FwLibrary.InstallState state, String note) {
        installing = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        r.state = state;
        r.stateNote = note;
        saveQuietly(r);
        showRecord(r.sha256);
    }

    // ---------------------------------------------------------------- helpers

    private void saveQuietly(FwLibrary.Record r) {
        try {
            library.save(r);
        } catch (IOException e) {
            Toast.makeText(this, "Не удалось сохранить состояние: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static String stateLine(FwLibrary.Record r) {
        switch (r.state) {
            case TRANSFERRING:
                return "\nпередача не завершилась (приложение было закрыто); повтор только вручную";
            case TRANSFERRED:
                return "\nфайл передан, ждём подтверждения версии " + r.targetVersion + " от браслета";
            case CONFIRMED:
                return "\nустановлено: " + nz(r.stateNote);
            case FAILED:
                return "\nошибка установки: " + nz(r.stateNote);
            default:
                return "";
        }
    }

    private static String summary(FwPackage.Info i) {
        return typeText(i) + (i.version == null ? "" : ", " + i.version) + (i.region == null ? "" : "-" + i.region);
    }

    private static String typeText(FwPackage.Info i) {
        if (!i.zipOk) {
            return "повреждён";
        }
        switch (i.type) {
            case FULL:
                return "полный пакет";
            case DIFF:
                return "дифф-пакет";
            default:
                return "неизвестный состав";
        }
    }

    private static String nz(String s) {
        return s == null ? "нет" : s;
    }

    private TextView progress(String msg) {
        content.removeAllViews();
        TextView t = text(msg);
        content.addView(t);
        return t;
    }

    private void alert(String title, String msg, Runnable after) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(msg)
                .setPositiveButton("OK", (d, w) -> after.run())
                .setOnCancelListener(d -> after.run())
                .show();
    }

    private void header(String s) {
        TextView t = text(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        t.setTypeface(null, Typeface.BOLD);
        t.setPadding(0, dp(16), 0, dp(8));
        content.addView(t);
    }

    private void note(String s) {
        TextView t = text(s);
        t.setAlpha(0.7f);
        content.addView(t);
    }

    private TextView text(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        t.setTextIsSelectable(true);
        return t;
    }

    private TextView mono(String s) {
        TextView t = text(s);
        t.setTypeface(Typeface.MONOSPACE);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
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
