package com.markus.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.core.content.FileProvider;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** User-owned download screen; Android always confirms installation. Cancels network work on exit. */
public final class ModInstallActivity extends Activity {
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final ApkDownload transfer=new ApkDownload();
    private TextView status;
    private ProgressBar progress;
    private Button install;
    private UpdateAsset asset;
    private File ready;
    private boolean dead;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        setTitle("Обновление мода");
        int pad=(int)(16*getResources().getDisplayMetrics().density+0.5f);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(pad,pad,pad,pad);
        status=new TextView(this);status.setTextSize(16);root.addView(status);
        progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(100);root.addView(progress);
        install=new Button(this);install.setAllCaps(false);install.setVisibility(android.view.View.GONE);
        install.setOnClickListener(v -> install());root.addView(install);
        Button cancel=new Button(this);cancel.setText("Закрыть / отменить загрузку");cancel.setAllCaps(false);
        cancel.setOnClickListener(v -> finish());root.addView(cancel);setContentView(root);
        Intent i=getIntent();
        asset=UpdateAsset.of(i.getStringExtra("tag"),i.getStringExtra("name"),i.getStringExtra("url"),
                i.getStringExtra("digest"),i.getLongExtra("size",-1));
        if(asset==null || !ModVersion.isNewer(asset.version,ModUpdate.installed(this))) {
            status.setText("Не удалось подтвердить данные обновления. Проверьте обновления ещё раз.");return;
        }
        status.setText("Скачиваю "+asset.version.display()+" с GitHub…");
        worker.execute(() -> {
            try {
                File dir=new File(getFilesDir(),"mod_updates");
                File cached=new File(dir,asset.name);
                if(saved!=null && cached.isFile()) {
                    ApkVerifier.verify(this,cached,asset);ready=cached;
                } else {
                    ready=transfer.run(asset,dir,(n,total)->ui(()->{
                        progress.setProgress((int)(100*n/total));
                        status.setText("Скачиваю: "+(n/1024/1024)+" из "+(total/1024/1024)+" МБ");
                    }));
                    ApkVerifier.verify(this,ready,asset);
                }
                ui(()->{
                    progress.setProgress(100);
                    status.setText("Обновление загружено. SHA-256, подпись и версия проверены.\n\n"
                            +"Android попросит подтвердить установку. Данные приложения сохраняются.");
                    install.setVisibility(android.view.View.VISIBLE);updateLabel();
                });
            } catch(Exception e) { ui(()->status.setText("Обновление не подготовлено: "+message(e))); }
        });
    }

    private void install() {
        if(ready==null)return;
        if(!getPackageManager().canRequestPackageInstalls()) {
            new AlertDialog.Builder(this).setTitle("Разрешение Android")
                    .setMessage("Разрешите этому приложению установку обновлений. После возврата нажмите «Установить обновление».")
                    .setPositiveButton("Открыть настройки",(d,w)->{
                        try { startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+getPackageName()))); }
                        catch(RuntimeException e) { status.setText("Не удалось открыть настройку установки приложений."); }
                    }).setNegativeButton("Отмена",null).show();return;
        }
        install.setEnabled(false);status.setText("Повторно проверяю APK перед установкой…");
        worker.execute(()->{
            try {
                ApkVerifier.verify(this,ready,asset);
                ui(()->{
                    try {
                        Uri uri=FileProvider.getUriForFile(this,getPackageName()+".fileProvider",ready);
                        Intent intent=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive")
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setClipData(ClipData.newRawUri("Обновление Mi Fitness",uri));
                        startActivity(intent);
                        status.setText("Установка передана Android. Подтвердите её в системном окне.");
                    } catch(RuntimeException e) {status.setText("Не удалось открыть системный установщик.");}
                    install.setEnabled(true);
                });
            } catch(Exception e) {ui(()->{status.setText("Установка остановлена: "+message(e));install.setEnabled(true);});}
        });
    }
    @Override protected void onResume() {super.onResume();if(ready!=null && install!=null)updateLabel();}
    private void updateLabel(){install.setText(getPackageManager().canRequestPackageInstalls()?"Установить обновление":"Разрешить установку");}
    private void ui(Runnable r){main.post(()->{if(!dead && !isFinishing() && !isDestroyed())r.run();});}
    private static String message(Exception e){String m=e.getMessage();return m!=null && m.matches("(?s).*[А-Яа-яЁё].*")?m:"проверьте интернет и свободное место; затем откройте загрузку ещё раз";}
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putBoolean("restore",true);}
    @Override protected void onDestroy(){dead=true;transfer.cancel();worker.shutdownNow();main.removeCallbacksAndMessages(null);super.onDestroy();}
}
