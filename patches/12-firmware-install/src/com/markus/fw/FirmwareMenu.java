package com.markus.fw;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.databinding.ObservableArrayList;
import com.markus.a;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/** Adds "Прошивки" to Устройство -> Ещё (sub-menu type 11, next to log collection and connection key). */
public final class FirmwareMenu {
    static final int TYPE_MORE = 11;
    static final int TYPE_FIRMWARE = 4449;

    private FirmwareMenu() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void insert(ObservableArrayList list, int type) {
        if (type != TYPE_MORE) {
            return;
        }
        Context app = application();
        if (app == null) {
            return;
        }
        list.add(a.createRow(app, TYPE_FIRMWARE, "device_tab_setting_icon_system", "Прошивки", null,
                new Function1<Context, Unit>() {
                    @Override
                    public Unit invoke(Context context) {
                        Intent i = new Intent(context, FirmwareActivity.class);
                        if (!(context instanceof Activity)) {
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        context.startActivity(i);
                        return Unit.INSTANCE;
                    }
                }));
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
