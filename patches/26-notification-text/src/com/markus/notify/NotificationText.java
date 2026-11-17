package com.markus.notify;

import android.content.Context;
import android.util.Log;
import androidx.databinding.ObservableArrayList;
import com.markus.Settings;
import com.markus.a;
import com.markus.dnd.SharedFlag;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Opt-in formatting of notification payloads, separate from music and the Android notification. */
public final class NotificationText {
    static final String NO_EMOJI = "notification_no_emoji";
    static final String UPPERCASE = "notification_uppercase";
    private static volatile int options;
    // Settings callbacks run in the UI process. This writer only persists these two cross-process flags.
    private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "mod-notification-options");
        t.setDaemon(true);
        return t;
    });

    private NotificationText() {}

    /** Called on Xiaomi's existing notification HandlerThread before its emoji-to-glyph mapping. */
    public static void refresh(Context context) {
        try {
            File dir = context.getFilesDir();
            boolean strip = SharedFlag.read(flag(dir, NO_EMOJI), Settings.getBoolean(NO_EMOJI, false));
            boolean upper = SharedFlag.read(flag(dir, UPPERCASE), Settings.getBoolean(UPPERCASE, false));
            options = (strip ? 1 : 0) | (upper ? 2 : 0);
        } catch (IOException | RuntimeException e) {
            options = 0;
            Log.w("MOD", "notification text: options unavailable; stock text retained");
        }
    }

    public static int options() { return options; }

    /** Strip before stock mapping, which otherwise replaces some emoji by private-use glyphs. */
    public static String beforeEmojiMapping(String text) {
        return TextTransform.apply(text, (options & 1) != 0, false);
    }

    public static void settingChanged(String key, boolean enabled) {
        if (!isOption(key)) return;
        Context app = application();
        if (app == null) return;
        WRITER.execute(() -> {
            try {
                SharedFlag.write(flag(app.getFilesDir(), key), enabled);
                Log.i("MOD", "notification text: option saved");
            } catch (IOException | RuntimeException e) {
                Log.w("MOD", "notification text: option could not be shared");
            }
        });
    }

    static boolean isOption(String key) { return NO_EMOJI.equals(key) || UPPERCASE.equals(key); }

    private static File flag(File dir, String key) { return new File(dir, "mod-" + key + ".flag"); }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void insertSwitches(ObservableArrayList list, int type) {
        if (type != 0x7000 || list == null) return;
        Context app = application();
        if (app == null) return;
        list.add(a.createSwitchRow(app, 0, "device_tab_setting_icon_notification", "markus_notification_no_emoji",
                "markus_notification_no_emoji_summary", NO_EMOJI));
        list.add(a.createSwitchRow(app, 0, "device_tab_setting_icon_notification", "markus_notification_uppercase",
                "markus_notification_uppercase_summary", UPPERCASE));
    }

    private static Context application() {
        try {
            return (Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
