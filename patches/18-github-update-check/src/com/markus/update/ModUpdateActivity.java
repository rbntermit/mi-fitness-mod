package com.markus.update;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

/** Устройство -> Ещё -> Обновление мода: installed build, the daily-check switch, a manual check. */
public final class ModUpdateActivity extends Activity {
    private TextView status;
    private Button checkNow;
    private Button download;
    private UpdateCheck.Result last;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("Обновление мода");
        int pad = dp(16);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView version = new TextView(this);
        ModVersion cur = ModUpdate.installed(this);
        version.setText("Установлена: " + (cur == null ? "номер сборки неизвестен" : cur.display()));
        version.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        root.addView(version);

        Switch enabled = new Switch(this);
        enabled.setText("Проверять обновления");
        enabled.setChecked(ModUpdate.isEnabled(this));
        enabled.setOnCheckedChangeListener((b, on) -> ModUpdate.setEnabled(this, on));
        enabled.setPadding(0, pad, 0, pad);
        root.addView(enabled, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView hint = new TextView(this);
        hint.setText("Проверка новой сборки — раз в сутки или вручную. APK скачивается только по вашей кнопке, "
                +"затем проверяются SHA-256, подпись и версия. Установку подтверждаете в системном окне Android. "
                +"Обновление прошивки браслета выполняется отдельно.");
        root.addView(hint);

        checkNow = new Button(this);
        checkNow.setText("Проверить сейчас");
        checkNow.setAllCaps(false);
        checkNow.setOnClickListener(v -> runCheck());
        root.addView(checkNow);

        status = new TextView(this);
        status.setPadding(0, pad, 0, pad);
        status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        root.addView(status);

        download = new Button(this);
        download.setText("Скачать");
        download.setAllCaps(false);
        download.setVisibility(android.view.View.GONE);
        download.setOnClickListener(v -> {
            if (last != null && last.pageUrl != null) {
                ModUpdate.startDownload(this, last);
            }
        });
        root.addView(download);

        ScrollView scroll = new ScrollView(this);
        scroll.setFitsSystemWindows(true);
        scroll.addView(root);
        setContentView(scroll);
    }

    private void runCheck() {
        checkNow.setEnabled(false);
        download.setVisibility(android.view.View.GONE);
        status.setText("Проверяю…");
        ModUpdate.check(this, r -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            last = r;
            checkNow.setEnabled(true);
            switch (r.state) {
                case NEWER:
                    status.setText("Доступна новая сборка: " + r.version.display()
                            + (r.body == null || r.body.isEmpty() ? "" : "\n\n" + r.body));
                    download.setText(r.asset==null?"Страница релиза":"Скачать обновление");
                    download.setVisibility(android.view.View.VISIBLE);
                    break;
                case UP_TO_DATE:
                case NO_RELEASE:
                    status.setText("Обновлений нет");
                    break;
                default:
                    status.setText("Сейчас проверить не получилось. Попробуйте позже.");
                    break;
            }
        });
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }
}
