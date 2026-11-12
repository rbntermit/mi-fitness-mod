package com.markus.declutter;

import android.content.Context;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/** Code side of patch 20; the hidden items themselves are described in rules.txt and hooks.txt. */
public final class Declutter {
    /** MineItemListModel.MineItem type of the «Отчёт» row (string mine_bugreport). */
    static final int MINE_ITEM_REPORT = 9;
    /** Written by build.sh: the React Native modules whose built-in bundle the build changed, comma-separated. */
    private static final String RES_BUILTIN = "markus_builtin_bundles";

    private static volatile String builtin;

    private Declutter() {
    }

    /** True for a React Native module whose bundle was changed by this build: it is always loaded from the APK. */
    public static boolean builtinOnly(String module) {
        return listed(builtinModules(), module);
    }

    static boolean listed(String csv, String module) {
        if (csv == null || module == null || module.isEmpty()) {
            return false;
        }
        for (String m : csv.split(",")) {
            if (m.trim().equals(module)) {
                return true;
            }
        }
        return false;
    }

    /** MineV4ViewModel.freshItemList, before the list is posted: drops the «Отчёт» row. */
    @SuppressWarnings("rawtypes")
    public static void dropMineReport(List items) {
        if (items == null) {
            return;
        }
        try {
            for (Iterator it = items.iterator(); it.hasNext(); ) {
                if (typeOf(it.next()) == MINE_ITEM_REPORT) {
                    it.remove();
                }
            }
        } catch (RuntimeException e) {
            // the list stays as the app built it
        }
    }

    static int typeOf(Object item) {
        if (item == null) {
            return -1;
        }
        try {
            Method m = item.getClass().getMethod("getType");
            Object v = m.invoke(item);
            return v instanceof Integer ? (Integer) v : -1;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return -1;
        }
    }

    private static String builtinModules() {
        String b = builtin;
        if (b != null) {
            return b;
        }
        Context app = application();
        if (app == null) {
            return "";
        }
        try {
            int id = app.getResources().getIdentifier(RES_BUILTIN, "string", app.getPackageName());
            b = id == 0 ? "" : app.getString(id);
        } catch (RuntimeException e) {
            b = "";
        }
        builtin = b;
        return b;
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
