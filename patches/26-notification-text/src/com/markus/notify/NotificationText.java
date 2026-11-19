package com.markus.notify;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.databinding.ObservableArrayList;
import com.markus.Settings;
import com.markus.a;
import com.markus.dnd.SharedFlag;
import com.markus.dnd.ZenSyncRules;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Opt-in options for forwarding app notifications to the wearable. */
public final class NotificationText {
    static final String NO_EMOJI = "notification_no_emoji";
    static final String UPPERCASE = "notification_uppercase";
    static final String PAUSE_ON_DND = "notification_pause_on_phone_dnd";
    private static volatile int options;
    // Settings callbacks run in the UI process. This writer persists flags for the notification process.
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

    /** Called before Xiaomi hands off ordinary notifications or starts its Focus notification path. */
    public static boolean shouldPause(Context context, StatusBarNotification posted, boolean weChatCall) {
        Notification notification = posted.getNotification();
        String category = notification == null ? null : notification.category;
        if (keepDuringDnd(category, weChatCall)) return false;
        try {
            if (!SharedFlag.read(flag(context.getFilesDir(), PAUSE_ON_DND), Settings.getBoolean(PAUSE_ON_DND, false))) {
                return false;
            }
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            return manager != null && isDndActive(manager.getCurrentInterruptionFilter());
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    static boolean keepDuringDnd(String category, boolean weChatCall) {
        return weChatCall || Notification.CATEGORY_CALL.equals(category)
                || Notification.CATEGORY_ALARM.equals(category) || Notification.CATEGORY_STOPWATCH.equals(category);
    }

    static boolean isDndActive(int filter) {
        return Boolean.TRUE.equals(ZenSyncRules.phoneOn(filter));
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

    static boolean isOption(String key) {
        return NO_EMOJI.equals(key) || UPPERCASE.equals(key) || PAUSE_ON_DND.equals(key);
    }

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
        list.add(a.createSwitchRow(app, 0, "device_tab_setting_icon_notification", "markus_notification_pause_dnd",
                "markus_notification_pause_dnd_summary", PAUSE_ON_DND));
    }

    private static Context application() {
        try {
            return (Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
