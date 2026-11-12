package com.markus.update;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.databinding.ObservableArrayList;
import com.markus.a;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/** Adds "Обновление мода" to Устройство -> Ещё (sub-menu type 11), after the mod's other rows. */
public final class ModUpdateMenu {
    static final int TYPE_MORE = 11;
    static final int TYPE_MOD_UPDATE = 4450;

    private ModUpdateMenu() {
    }

    /** DeviceSettingListViewModel.createSettingItems, right after com.markus.a.insertSubMenus. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void insert(ObservableArrayList list, int type) {
        if (type != TYPE_MORE || list == null) {
            return;
        }
        Context app = application();
        if (app == null) {
            return;
        }
        ModVersion cur = ModUpdate.installed(app);
        list.add(a.createRow(app, TYPE_MOD_UPDATE, "device_tab_setting_icon_system", "Обновление мода",
                cur == null ? null : cur.display(), new Function1<Context, Unit>() {
                    @Override
                    public Unit invoke(Context context) {
                        Intent i = new Intent(context, ModUpdateActivity.class);
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
