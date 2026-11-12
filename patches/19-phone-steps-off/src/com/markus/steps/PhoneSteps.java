package com.markus.steps;

import androidx.databinding.ObservableArrayList;
import com.markus.Settings;
import com.markus.a;

/**
 * «Не учитывать шаги телефона»: the phone's own step counter (MIUI/HyperOS step provider, PhoneStepsProvider)
 * reports itself as unsupported, so Mi Fitness takes steps from the band only and they are not counted twice.
 * Off by default: then the app behaves as the original. On phones without the MIUI step provider the original
 * already reports "unsupported", and the switch changes nothing.
 */
public final class PhoneSteps {
    /** com.markus.Settings key; the switch row stores into it. */
    static final String KEY = "phone_steps_off";
    /** Sub-menu of the mod's own settings (Устройство -> Ещё -> настройки мода), as in com.markus.a. */
    static final int TYPE_MOD_SETTINGS = 0x7000;

    private PhoneSteps() {
    }

    /** PhoneStepsProvider.supported / PhoneStepsProviderImpl.supported (start): true = report "unsupported". */
    public static boolean ignored() {
        try {
            return Settings.getBoolean(KEY, false);
        } catch (RuntimeException e) {
            // Settings.init() not called yet in this process: behave as the original
            return false;
        }
    }

    /** DeviceSettingListViewModel.createSettingItems, after com.markus.a.insertSubMenus. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void insertSwitch(ObservableArrayList list, int type) {
        if (!isModSettings(type) || list == null) {
            return;
        }
        android.content.Context app = application();
        if (app == null) {
            return;
        }
        list.add(a.createSwitchRow(app, 0, "device_tab_setting_icon_health", "markus_phone_steps_off",
                "markus_phone_steps_off_summary", KEY));
    }

    static boolean isModSettings(int type) {
        return type == TYPE_MOD_SETTINGS;
    }

    /** ActivityThread.currentApplication() is not in the public SDK; the mod's own menu code uses it the same way. */
    private static android.content.Context application() {
        try {
            return (android.content.Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication")
                    .invoke(null);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
