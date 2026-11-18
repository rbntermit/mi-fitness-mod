package com.markus.logexport;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.databinding.ObservableArrayList;
import com.markus.a;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/** Entry points from the app's code (hooks.txt): the band log that has started to arrive, and the new menu row. */
public final class LogExport {
    static final int TYPE_MORE = 11;
    static final int TYPE_APP_LOG = 4451;

    private LogExport() {
    }

    /**
     * LogSyncCallback.onSuccess (patch 00-core «Сбор журналов»): the band accepted the request, the log is on its way
     * into {@code dir} (Android/data/..., which people cannot open on Android 11+). Opens the export screen instead of
     * the old dialog that only showed this path.
     */
    public static void bandLogReady(Context context, String dir) {
        Intent i = new Intent(context, LogExportActivity.class)
                .putExtra(LogExportActivity.EXTRA_MODE, LogExportActivity.MODE_BAND)
                .putExtra(LogExportActivity.EXTRA_DIR, dir);
        start(context, i);
    }

    /** DeviceSettingListViewModel.createSettingItems, right after com.markus.a.insertSubMenus: «Журнал приложения». */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void insertMenu(ObservableArrayList list, int type) {
        if (type != TYPE_MORE || list == null) {
            return;
        }
        Context app = application();
        if (app == null) {
            return;
        }
        list.add(a.createRow(app, TYPE_APP_LOG, "device_tab_setting_icon_safe", "Журнал приложения",
                "Последние " + LogExportActivity.APP_LOG_MINUTES + " минут, для поиска ошибок",
                new Function1<Context, Unit>() {
                    @Override
                    public Unit invoke(Context context) {
                        openAppLog(context);
                        return Unit.INSTANCE;
                    }
                }));
    }

    static void openAppLog(Context context) {
        start(context, new Intent(context, LogExportActivity.class)
                .putExtra(LogExportActivity.EXTRA_MODE, LogExportActivity.MODE_APP));
    }

    private static void start(Context context, Intent i) {
        if (!(context instanceof Activity)) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(i);
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
